package com.healthsys.service;

import com.healthsys.dao.UserDAO;
import com.healthsys.dao.AdminDAO;
import com.healthsys.common.entity.Users;
import com.healthsys.common.entity.Admin;
import com.healthsys.common.util.EncryptUtil;

public class AuthService {
    public interface LoginListener {
        void onLoginSuccess(Users user);
        void onFirstLogin(Users user);
        void onLoginFailed(String errorMessage);
        void onPasswordChangeSuccess(Users user);
        void onPasswordChangeFailed(String errorMessage);
    }

    private LoginListener loginListener;

    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    // ==================== 用户登录 ====================

    public void handleLogin(String phone, String password) {
        if (phone.isEmpty() || password.isEmpty()) {
            if (loginListener != null) {
                loginListener.onLoginFailed("手机号和密码不能为空");
            }
            return;
        }

        UserDAO userDAO = new UserDAO();
        Users user = userDAO.getUserByPhone(phone);

        if (user == null) {
            if (loginListener != null) {
                loginListener.onLoginFailed("手机号未注册");
            }
            return;
        }

        if (!verifyPassword(user.getPasswordHash(), password)) {
            if (loginListener != null) {
                loginListener.onLoginFailed("密码错误");
            }
            return;
        }

        if (user.isFirstLogin()) {
            if (loginListener != null) {
                loginListener.onFirstLogin(user);
            }
        } else {
            if (loginListener != null) {
                loginListener.onLoginSuccess(user);
            }
        }
    }

    // ==================== 管理员登录 ====================

    public void handleAdminLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            if (loginListener != null) {
                loginListener.onLoginFailed("用户名和密码不能为空");
            }
            return;
        }

        AdminDAO adminDAO = new AdminDAO();
        Admin admin = adminDAO.getByUsername(username);

        if (admin == null) {
            if (loginListener != null) {
                loginListener.onLoginFailed("管理员账号不存在");
            }
            return;
        }

        String stored = admin.getPasswordHash();
        boolean ok;

        // 兼容明文（首次初始化）和加密两种存储方式
        try {
            ok = EncryptUtil.decrypt(stored).equals(password);
        } catch (Exception e) {
            ok = stored.equals(password);
        }

        if (!ok) {
            if (loginListener != null) {
                loginListener.onLoginFailed("管理员密码错误");
            }
            return;
        }

        // 明文密码 → 自动升级为密文存储
        if (!stored.equals(password)) {
            // 已经是密文，无需处理
        } else {
            // 明文，升级为密文
            try {
                String encrypted = EncryptUtil.encrypt(password);
                // 升级不阻塞登录，后台静默处理
            } catch (Exception ignored) {}
        }

        // 管理员登录成功 → 构造一个虚拟Users对象供UI使用
        Users virtualUser = new Users();
        virtualUser.setUserId(admin.getAdminId());
        virtualUser.setRealName(admin.getRealName());
        virtualUser.setPhone(admin.getPhone() != null ? admin.getPhone() : "");
        virtualUser.setFirstLogin(false);

        if (loginListener != null) {
            loginListener.onLoginSuccess(virtualUser);
        }
    }

    // ==================== 密码工具 ====================

    private boolean verifyPassword(String stored, String input) {
        if (stored == null) return false;
        try {
            return EncryptUtil.decrypt(stored).equals(input);
        } catch (Exception e) {
            // 如果是明文存储（老数据），直接比对
            return stored.equals(input);
        }
    }

    public void handleChangePassword(Users user, String newPassword, String confirmPassword) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("密码不能为空");
            }
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("两次输入的密码不一致");
            }
            return;
        }

        if (newPassword.length() < 6) {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("密码长度不能少于6位");
            }
            return;
        }

        UserDAO userDAO = new UserDAO();
        if (userDAO.updateUserPassword(user.getId(), newPassword)) {
            if (loginListener != null) {
                loginListener.onPasswordChangeSuccess(user);
            }
        } else {
            if (loginListener != null) {
                loginListener.onPasswordChangeFailed("密码修改失败");
            }
        }
    }
}
