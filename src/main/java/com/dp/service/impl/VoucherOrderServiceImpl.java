package com.dp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.common.Result;
import com.dp.constant.RedisConstants;
import com.dp.mapper.VoucherOrderMapper;
import com.dp.model.entity.SeckillVoucher;
import com.dp.model.entity.VoucherOrder;
import com.dp.rabbitmq.MQSender;
import com.dp.rabbitmq.SeckillMessage;
import com.dp.service.ISeckillVoucherService;
import com.dp.service.IVoucherOrderService;
import com.dp.utils.RedisIdWorker;
import com.dp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    // 定义一个线程池
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    // 本地缓存，判断该商品是否被处理过了
    private final HashMap<Long, Boolean> localOverMap = new HashMap<>();
    // 消息发送方
    @Resource
    private MQSender mqSender;
    private IVoucherOrderService proxy;


    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        // 1. 获取用户
        Long userId = voucherOrder.getUserId();
        // 2. 创建锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 3. 获取锁
        boolean isLock = lock.tryLock();
        if (!isLock) {
            // 获取锁失败，返回错误或重试
            log.error("不允许重复下单");
        }
        try {
            proxy.createVoucherOrder(voucherOrder); // 在接口中声明后，此时事务可以生效
        } finally {
            lock.unlock();
        }
    }

    /**
     * 三级缓冲
     *
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 一级缓冲：本地缓存检查
        boolean over = localOverMap.get(voucherId);
        if (over) {
            return Result.fail("优惠券已秒杀完毕");
        }
        // 1. 获取当前下单用户的 Id
        Long userId = UserHolder.getUser().getId();
        // 2. 生成订单id
        long orderId = redisIdWorker.nextId("order");
        // 二级缓冲：执行 lua 脚本（库存预减）, 根据返回值判定用户是否具备下单资格
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId));
        // 4. 判断结果是否为0，结果为 0 才可以下单
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 创建秒杀消息并放入队列
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setUserId(userId).setGoodsId(voucherId);

        // 5. 返回订单 Id
        return Result.ok(orderId);
    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 1. 一人一单
        Long userId = voucherOrder.getUserId(); //  因为是异步下单，所以要从子线程中去取UserId
        Integer count = query().eq("user_id", userId).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            // 用户已经下单了
            log.error("用户已经购买过一次了");
        }
        // 2. 去 Redis 中查询当前商品库存的缓存
        Long voucherId = voucherOrder.getVoucherId();
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherOrder.getVoucherId();
        String stock = stringRedisTemplate.opsForValue().get(stockKey);
        if (stock == null) { // 2.1 Redis 中没有查到库存数据
            // 2.2 查询数据库，将结果加入缓存
            SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
            stringRedisTemplate.opsForValue().set(stockKey, seckillVoucher.getStock().toString());
        }
        boolean success = seckillVoucherService.update().setSql("stock = stock - 1").gt("stock", 0).eq("voucher_id", voucherOrder.getVoucherId()).update();
        if (!success) {
            // 扣减失败
            log.error("库存不足！");
        }
        // 7. 保存订单
        save(voucherOrder);
    }
}
