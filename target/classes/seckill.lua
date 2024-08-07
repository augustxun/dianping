-- 1.1 优惠券id
local voucherId = ARGV[1]
-- 1.2 用户id
local userId = ARGV[2]

-- 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 订单key
local orderKey = 'seckill:order:' .. userId

-- 3. 脚本业务
-- 3.1 判断库存是否充足
if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 3.2 库存不足，返回1

    return 1
end
-- 3.3 判断用户是否下单
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 存在，重复下单，返回2
    return 2;
end
-- 3.4 扣库存
redis.call('incrby', stockKey, -1)
-- 3.5 下单（保存用户）
redis.call('sadd', orderKey, userId)
return 0

