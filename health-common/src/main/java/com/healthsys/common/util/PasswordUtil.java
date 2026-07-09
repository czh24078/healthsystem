package com.healthsys.common.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码工具类 — 使用 BCrypt 进行安全的密码哈希和验证
 */
public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 12;

    /**
     * 对明文密码进行 BCrypt 哈希
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * 验证明文密码是否匹配 BCrypt 哈希
     */
    public static boolean verify(String plainPassword, String hashed) {
        if (plainPassword == null || hashed == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashed);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为 BCrypt 哈希（以 $2a$ 开头）
     */
    public static boolean isBcryptHash(String stored) {
        return stored != null && stored.startsWith("$2a$");
    }
}
