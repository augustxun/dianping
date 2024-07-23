package com.hmdp;

import com.hmdp.utils.JwtUtils;
import com.hmdp.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JwtDemoApplicationTests.class)
public class JwtDemoApplicationTests {
    // 公钥文件生成地址
    private static final String PUB_KEY_PATH = "src/main/resources/rsa.pub";
    // 私钥文件生成地址
    private static final String PRI_KEY_PATH = "src/main/resources/rsa.pri";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    // 生成公钥和私钥
    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(PUB_KEY_PATH, PRI_KEY_PATH, "234");
    }

    // 先生成 再获取 生成之前把@Before注释掉！
    // 获取公钥和私钥
    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(PUB_KEY_PATH);
        this.privateKey = RsaUtils.getPrivateKey(PRI_KEY_PATH);
    }

    // 生成token
    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 1);
        System.out.println("token = " + token);
    }

    // 解析token
    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1ODc2MjYwODZ9.kO22BSWogDJPD6oG92dfFukothGMJej3FKLIfDJRVjGiF_O7kLPSmycmuyByy8wd7X_nOVDCPoMvvhoUzviDsgzIC0xiILoMobwyUDtbYdStCfiLVikqHmnf0Our5tuxwVaPOK2igoWW3zRRI7HG5RLh0p2pUAQe1C-is_8zczn2T5CQ-7vEwPS6U5FLn7_1y8rHNVsKlHqNBdSDxQn7jLOkHkKnRiShZ2_iBuXTzo6uZt2461IV8qk6Lmn35fyX7JHwHIVvuQyniFEsdYNW5t8P3Eo1UEbL3ZD5ZbhcIsK5gnvpXdsne6uK1jHQzClQi-hcGONuHXpS2IkueWEizg";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}