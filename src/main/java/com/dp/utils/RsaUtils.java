package com.dp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA 公钥和私钥读取工具类
 */
public class RsaUtils {
    /**
     * 获取公钥
     * @param filename 公钥文件路径
     * @return 公钥
     * @throws Exception 如果读取或解析公钥失败
     */
    public static PublicKey getPublicKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPublicKey(bytes);
    }

    /**
     * 获取私钥
     * @param filename 私钥文件路径
     * @return 私钥
     * @throws Exception 如果读取或解析私钥失败
     */
    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPrivateKey(bytes);
    }

    /**
     * 根据字节数组生成公钥
     * @param bytes 公钥字节数组
     * @return 公钥
     * @throws Exception 如果解析公钥失败
     */
    public static PublicKey getPublicKey(byte[] bytes) throws Exception {

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * 根据字节数组生成私钥
     * @param bytes 私钥字节数组
     * @return 私钥
     * @throws Exception 如果解析私钥失败
     */
    public static PrivateKey getPrivateKey(byte[] bytes) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    private static byte[] readFile(String filename) throws Exception {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    /**
     *
     * @param publicKeyFilename 公钥文件路径
     * @param privateKeyFilename 私钥文件路径
     * @param secret 生成密钥的密文
     * @throws Exception
     */
    public static void generateKey(String publicKeyFilename, String privateKeyFilename, String secret) throws Exception {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(2048, secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        // 获取公钥并写出
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        writeFile(publicKeyFilename, publicKeyBytes);
        // 获取私钥并写出
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        writeFile(privateKeyFilename, privateKeyBytes);
    }
    /**
     * 将字节数组写入文件
     * @param destPath 目标文件路径
     * @param bytes 写入的字节数组
     * @throws IOException 如果写入文件失败
     */
    private static void writeFile(String destPath, byte[] bytes) throws IOException {
        File dest = new File(destPath);
        if (!dest.exists()) {
            boolean b = dest.createNewFile();
        }
        Files.write(dest.toPath(), bytes);
    }
}
