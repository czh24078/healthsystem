package com.healthsys.service;

import com.healthsys.dao.UserDAO;
import com.healthsys.common.entity.Users;

import java.util.List;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
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
