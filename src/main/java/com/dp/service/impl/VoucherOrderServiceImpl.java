package com.dp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.common.Result;
import com.dp.constant.RedisConstants;
import com.dp.mapper.VoucherOrderMapper;
import com.dp.model.entity.SeckillVoucher;
import com.dp.model.entity.VoucherOrder;
import com.dp.rabbitmq.MQSender;
import com.dp.rabbitmq.SeckillMessage;
import com.dp.redis.RedisService;
import com.dp.service.ISeckillVoucherService;
import com.dp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    // 本地缓存，判断该商品是否被处理过了
    private static final ConcurrentHashMap<Long, Boolean> localOverMap = new ConcurrentHashMap<>();
    @Resource
    private ISeckillVoucherService voucherService;
    @Resource
    private RedisService redisService;
    @Resource
    private RedissonClient redissonClient;
    // 消息发送方
    @Resource
    private MQSender mqSender;
    private IVoucherOrderService proxy;

    /**
     * 系统初始化，将商品加载到本地 Map 和 Redis 缓存
     */
    @PostConstruct
    public void init() {
        List<SeckillVoucher> vouchers = voucherService.list();
        for (SeckillVoucher voucher : vouchers) {
            String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucher.getVoucherId().toString();
            redisService.set(stockKey, voucher.getStock().toString());
            localOverMap.put(voucher.getVoucherId(), false);
        }
    }


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
            //            proxy.createVoucherOrder(voucherOrder); // 在接口中声明后，此时事务可以生效
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
        // 1. 一级缓冲：本地缓存检查
        boolean over = localOverMap.get(voucherId);
        if (over) {
            return Result.fail("优惠券已秒杀完毕");
        }
        // 获取当前下单用户的 Id
/*        UserVO user = UserHolder.getUser();
        Long userId = user.getId();*/
        // 为了模拟多用户下单，因此 userId 自动生成，避开 token 认证的步骤
        Long userId = Long.valueOf(RandomUtil.randomNumbers(6));
        // 2. 二级缓冲：执行 lua 脚本（库存预减）, 根据返回值判定用户是否具备下单资格
        Long result = redisService.stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString());

        // 判断结果是否为0，结果为 0 才可以下单
        int r = 0;
        if (result != null) {
            r = result.intValue();
        }
        if (r != 0) {
            if (r == 1) {
                localOverMap.put(voucherId, true);
                return Result.fail("库存不足");
            }
            else return Result.fail("不能重复下单");
        }
        // 2. 生成订单id
        long orderId = redisService.nextId("order");
        // 3. 三级缓冲：创建秒杀消息并放入队列
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setId(orderId).setUserId(userId).setVoucherId(voucherId);
        mqSender.sendSeckillMsg(seckillMessage);
        // 5. 返回订单 Id
        return Result.ok(orderId);
    }

    @Override
    public Result seckillVoucher1(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher voucher = voucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("秒杀尚未开始！");
        }
        // 3.判断秒杀是否已经结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            // 尚未开始
            return Result.fail("秒杀已经结束！");
        }
        // 4.判断库存是否充足
        if (voucher.getStock() < 1) {
            // 库存不足
            return Result.fail("库存不足！");
        }
        // 6.2.用户id (此处为了模拟多用户下单，故使用随机userId)
        Long userId = Long.valueOf(RandomUtil.randomNumbers(6));
//        return createVoucherOrder(voucherId, userId);
        synchronized (String.valueOf(userId).intern()) {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId, userId);
        }
    }
    @Transactional
    public Result createVoucherOrder(Long voucherId, Long userId) {
        synchronized(userId.toString().intern()){
            // 5.1.查询订单
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            // 5.2.判断是否存在
            if (count > 0) {
                // 用户已经购买过了
                return Result.fail("用户已经购买过一次！");
            }

            // 6.扣减库存
            boolean success = voucherService.update()
                    .setSql("stock = stock - 1") // set stock = stock - 1
                    .eq("voucher_id", voucherId).gt("stock", 0) // where id = ? and stock > 0
                    .update();
            if (!success) {
                // 扣减失败
                return Result.fail("库存不足！");
            }

            // 7.创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            // 7.1.订单id
            long orderId = redisService.nextId("order");
            voucherOrder.setId(orderId);
            // 7.2.用户id
            voucherOrder.setUserId(userId);
            // 7.3.代金券id
            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            // 7.返回订单id
            return Result.ok(orderId);
        }
    }

}
