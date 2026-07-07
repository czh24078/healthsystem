package com.healthsys.common.controller;

import com.healthsys.common.dao.UserDAO;
import com.ncu.Common.Users;

import java.util.List;

public class UserController {
    private UserDAO userDAO;

    public UserController() {
        this.userDAO = new UserDAO();
    }

    public boolean updateUserProfile(Users user) {
        return userDAO.updateUserProfile(user);
    }

    public List<Users> getAllMedicalUsers() {
        return userDAO.getAllMedicalUsers();
    }

    public boolean resetUserPassword(Long userId) {
        return userDAO.updateUserPassword(userId, UserDAO.INITIAL_PASSWORD);
    }
}