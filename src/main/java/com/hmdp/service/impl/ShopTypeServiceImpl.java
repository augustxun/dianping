package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.common.Result;
import com.hmdp.model.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.constant.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *

 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Result getTypeList() {
        String typeKey = CACHE_SHOP_TYPE_KEY;
        //从redis中查询
        Long typeListSize = stringRedisTemplate.opsForList().size(typeKey);
        //redis存在数据
        if (typeListSize != null && typeListSize != 0){
            List<String> typeJsonList = stringRedisTemplate.opsForList().range(typeKey, 0, typeListSize-1);
            List<ShopType> typeList = new ArrayList<>();
            for (String typeJson : typeJsonList) {
                typeList.add(JSONUtil.toBean(typeJson,ShopType.class));
            }
            return Result.ok(typeList);
        }
        //redis不存在数据 查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (typeList==null){
            //数据库不存在数据
            return Result.fail("发生错误");
        }
        // 转换
        List<String> typeJsonList=new ArrayList<>();
        for (ShopType shopType : typeList) {
            typeJsonList.add(JSONUtil.toJsonStr(shopType));
        }
        // 数据库存在数据 写入redis
        stringRedisTemplate.opsForList().rightPushAll(typeKey,typeJsonList);
        // 返回数据
        return Result.ok(typeList);
    }
}
