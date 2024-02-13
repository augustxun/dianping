package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.*;

@Component
@Slf4j
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 基于逻辑过期存入缓存
     *
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void setWithLogicExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 基于空值解决缓存穿透的问题
     *
     * @param keyPrefix
     * @param id
     * @param type
     * @param dbFallback
     * @param time
     * @param unit
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> T queryWithPassThrough(String keyPrefix, ID id, Class<T> type, Function<ID, T> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从Redis查询商户缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {// 判断是否为 NULL 或空格
            // 3.命中缓存, 直接返回结果
            return JSONUtil.toBean(json, type);
        }
        // 判断命中的是否是空值
        if ("".equals(json)) {
            // 返回一个错误信息,
            return null;
        }
        // 4. 缓存不存在，根据id查询数据库
        T result = dbFallback.apply(id);
        // 5. 如果数据库也不存在，将空值写入Redis
        if (result == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 6. 存在，写入Redis
        this.set(key, result, time, unit);
        // 7. 返回
        return result;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTORS = Executors.newFixedThreadPool(10);

    /**
     * 基于逻辑过期解决缓存击穿问题
     *
     * @param keyPrefix
     * @param id
     * @param type
     * @param dbFallback
     * @param time
     * @param unit
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> T queryWithLogicExpire(String keyPrefix, ID id, Class<T> type, Function<ID, T> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从Redis查询商户缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否命中缓存
        if (StrUtil.isBlank(json)) {
            // 不命中，直接返回空值
            return null;
        }
        // 3. 命中，将 json 反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        T result = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 4. 判断缓存是否过期
        if (!expireTime.isAfter(LocalDateTime.now())) {
            // 4.1 未过期，直接返回店铺信息
            return result;
        }
        // 4.2 已过期，需要缓存重建
        // 5. 缓存重建
        // 5.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = trylock(key);
        // 5.2 判断是否获取锁成功
        if (isLock) {
            // 6.3 成功，开启独立线程，异步重建
            CACHE_REBUILD_EXECUTORS.submit(() -> {
                // 查询数据库
                T newData = dbFallback.apply(id);
                if (newData == null) {
                    stringRedisTemplate.opsForValue().set(key, "", time, unit);
                    return;
                }
                // 写入Redis
                this.setWithLogicExpire(key, newData, time, unit);
            });
        }
        // 6.4 返回过期的商铺信息
        return result;
    }

    /**
     * 加互斥锁
     *
     * @param key
     * @return
     */
    private boolean trylock(String key) {
        // 设置互斥锁时用 setIfAbsent 可以保证同一时间只有一个线程获取互斥锁
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
