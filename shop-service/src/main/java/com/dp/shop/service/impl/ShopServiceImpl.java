package com.dp.shop.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.common.Result;
import com.dp.model.entity.Shop;
import com.dp.mapper.ShopMapper;
import com.dp.service.IShopService;
import com.dp.utils.CacheClient;
import com.dp.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.dp.constant.RedisConstants.CACHE_SHOP_KEY;
import static com.dp.constant.RedisConstants.CACHE_SHOP_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *

 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        // Shop shop = queryWithPassThrough(id);

        // 互斥锁解决缓存击穿
        // Shop shop = queryWithMutex(id);

        // 逻辑过期解决缓存击穿
        // Shop shop = queryWithLogicExpire(id);
        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    /**
     * 互斥锁解决缓存击穿
     * @param id
     * @return
     */
//    public Shop queryWithMutex(Long id) {
//        String shopKey = CACHE_SHOP_KEY + id;
//        // 1.从Redis查询商户缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);
//        // 2. 判断是否命中数据
//        if (StrUtil.isNotBlank(shopJson)) {
//            //3. 存在，直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//        // 判断命中的是否是空值
//        if ("".equals(shopJson)) {
//            // 返回一个null
//            return null;
//        }
//        // 4.实现缓存重建
//        // 4.1 获取互斥锁
//        String lockKey = "lock:shop" + id;
//        Shop shop = null;
//        try {
//            boolean isLock = trylock(lockKey);
//            // 4.2 判断是否获取成功
//            if (!isLock) {
//                // 4.3 如果失败，则休眠并重试
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//            // 模拟重建的延时
//            Thread.sleep(200);
//            // 4.4 成功，根据id查询数据库
//            shop = getById(id);
//            // 5. 如果数据库也不存在，将空值写入Redis
//            if (shop == null) {
//                stringRedisTemplate.opsForValue().set(shopKey, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//            // 6. 存在，写入Redis
//            stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            // 7. 释放互斥锁
//            unlock(lockKey);
//        }
//        // 8. 返回
//        return shop;
//    }


    /**
     * 加载商户数据到Redis缓存中,并设置逻辑过期时间
     * @param id
     * @param expireSeconds
     */
    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        // 1. 查询店铺数据
        Shop shop = getById(id);
        Thread.sleep(200);
        // 2. 封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3. 写入Redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Transactional
    @Override
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok("更新成功！");
    }
}
