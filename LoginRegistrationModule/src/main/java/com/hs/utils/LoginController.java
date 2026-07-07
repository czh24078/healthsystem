package com.hs.utils;

import com.hs.dao.UserDAO;
import com.ncu.Common.Users;

public class LoginController {
    private UserDAO userDAO;
    private LoginListener loginListener;

    public LoginController() {
        this.userDAO = new UserDAO();
    }

    public void setLoginListener(LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public void handleLogin(String username, String password) {
        Users user = userDAO.getUserByUsername(username);

        if (user == null) {
            if (loginListener != null) {
                loginListener.onLoginFailed("用户名或密码错误");
            }
            return;
        }

        if (!user.getPassword().equals(password)) {
            if (loginListener != null) {
                loginListener.onLoginFailed("用户名或密码错误");
            }
            return;
        }

        // 判断用户角色
        if ("medical".equals(user.getRole()) || "admin".equals(user.getRole())) {
            // 医护或管理员用户直接跳转到医护界面
            if (loginListener != null) {
                loginListener.onLoginSuccess(user);
            }
        } else {
            // 普通用户跳转到主界面
            if (loginListener != null) {
                loginListener.onLoginSuccess(user);
            }
        }
    }

    public interface LoginListener {
        void onLoginSuccess(Users user);
        void onFirstLogin(Users user);
        void onLoginFailed(String errorMessage);
        void onPasswordChangeSuccess(Users user);
        void onPasswordChangeFailed(String errorMessage);
    }


}

