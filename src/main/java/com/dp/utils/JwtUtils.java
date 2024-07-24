package com.dp.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.joda.time.DateTime;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

/**
 * 负责生成、解析和验证JWT令牌
 */
public class JwtUtils {
    /**
     * 生成令牌
     * @param map 载荷中的数据
     * @param key 私钥加密
     * @param expireMinutes 过期时间
     * @return
     * @throws Exception
     */
    public static String generateToken(Map<String, Object> map, PrivateKey key, int expireMinutes) throws Exception {
        return Jwts.builder()
                .setClaims(map)
                .setExpiration(DateTime.now().plusMinutes(expireMinutes).toDate())
                .signWith(key, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 解析令牌
     * @param token 用户请求中的 token
     * @param key 公钥解密
     * @return 返回解析后的结果
     */
    private static Jws<Claims> parserToken(String token, PublicKey key) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token);
    }

    /**
     * 从令牌解析的结果中获取用户信息
     * @param token 用户请求中的令牌
     * @param key
     * @return 用户信息
     * @throws Exception
     */
    public static Map<String, Object> getInfoFromToken(String token, PublicKey key) throws Exception {
        Jws<Claims> claimsJws = parserToken(token, key);
        return claimsJws.getBody();
    }

}
