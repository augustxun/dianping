## 项目背景

本项目的项目原型是黑马点评，黑马点评是一款类似于大众点评等打卡类 APP 的点评类项目。实现了短信登陆、探店点评、优惠券秒杀、每日签到、好友关注、粉丝关注博主后，主动推送博主的相关博客等多个模块。 用户可以浏览首页的推荐内容，搜索附近的商家，查看商家的详情和评价以及发表探店博客，抢购商家发布的限时秒杀商品。

本项目在原型功能的基础上做了诸多优化。

### 秒杀系统的业务特点有什么？

高并发：秒杀的特点就是这样时间极短、 瞬间用户量大。

库存量少：一般秒杀活动商品量很少，这就导致了只有极少量用户能成功购买到。

业务简单：流程比较简单，一般都是下订单、扣库存、支付订单

恶意请求，数据库压力大



## 项目优化

### 优化点 1：RataLimiter 令牌桶限流 + 降级

秒杀商品时可能会因为访问量太大导致系统崩溃，用Guava 的 RateLimiter 基于令牌桶算法进行**限流**，当峰值流量达到阈值时，对后续的请求进行**降级**，降级方案为：返回排队页面、错误页等；

### 优化点 2：三级缓冲保护系统，减小数据库压力

三级缓冲指的是：本地缓存（ConcurrentHashMap） + Redis预减 + RabbitMQ 异步下单

为了将秒杀请求拦截在上游，避免请求压到数据层导致读写锁冲突严重、超时等问题，对下单业务设置三级缓冲：分别是 1.本地标记；2.Redis + Lua 脚本预减； 3.RabbitMQ 异步下单，最后才会访问数据库。

### 优化点 3：Redis 分布式锁保证接口幂等性

接口幂等性在秒杀场景下指的就是“一人一单”问题。

假设一次商品促销活动中，一个用户只有依次下单机会，但是出现了一些恶意刷单的行为，利用工具进行并发请求，通过分布式锁可以解决并发场景下恶意刷单的行为。

### 优化点 4：优化超卖解决方案

**方案 1**：MySQL 乐观锁（性能差）

```mysql
select version from goods WHERE id= 1001;
update goods set num = num - 1, version = version + 1 WHERE id= 1001 AND ersion = @version;
```

如果使用乐观锁的方案，扣减库存的条件是扣减时的库存量等于查询时的库存量，因此短时间内如果并发量很高，会导致下单成功率非常低。

**方案 2**：MySQL 排他锁

```mysql
update goods set num = num - 1 WHERE id = 1001 and num > 0
```

扣减库存时只有在库存量大于 0 时才会起作用，假设有 100 个线程读取到 num > 0，也只有一个线程会成功。这就是 MySQL 中的排他锁，如一个事务获取了一个数据行的排他锁，其他事务就不能再获取该行的其他锁，包括共享锁和排他锁，但是获取排他锁的事务是可以对数据就行读取和修改。

**方案 3**：Redis 单线程预减库存 + MySQL 排他锁降级

比如商品有100件。那么我在redis存储一个**k,v**。例如 <gs1001, 100>，每一个用户线程进来，key值就减1，等减到0的时候，全部拒绝剩下的请求。那么也就是只有100个线程会进入到后续操作。所以一定不会出现超卖的现象。

### 优化点 5 ：Redis 降级策略





## 秒杀下单模块设计

总体思路就是要减少对数据库的访问，尽可能将数据缓存到Redis缓存中，从缓存中获取数据。

1.  在系统初始化时，将商品的库存数量加载到Redis缓存中，并不是需要先请求一次才能缓存
2.  接收到秒杀请求时，在Redis中进行预减库存，当Redis中的库存不足时，直接返回秒杀失败，减少对数据库的访问。否则继续进行第3步；
3.  将请求放入异步队列（RabbitMQ）中，立即给前端返回一个值，表示正在排队中。
4.  服务端异步队列将请求出队，出队成功的请求可以然后进行秒杀逻辑，减库存–>下订单–>写入秒杀订单，成功了就返回成功。
5.  当后台订单创建成功之后可以通过`websocket`向用户发送一个秒杀成功通知。前端以此来判断是否秒杀成功，秒杀成功则进入秒杀订单详情，否则秒杀失败。

## 性能压测

秒杀系统压测要在满库存的时候进行测试，因为库存不足时，当请求数

优化前，最简秒杀 QPS: 554

![image-20240724234029773](/Users/augustxun/projects/dianping/assets/image-20240724234029773.png)

优化锁，无缓存，无消息队列  QPS: 1194

![image-20240724234155683](/Users/augustxun/projects/dianping/assets/image-20240724234155683.png)

三级缓冲方案 QPS: 4050

![image-20240724234317450](/Users/augustxun/projects/dianping/assets/image-20240724234317450.png)

