package com.healthsys.common.controller;

import com.healthsys.common.dao.CheckItemDAO;
import com.ncu.Common.CheckItem;

import java.util.List;

public class MedicalTestController {
    private CheckItemDAO medicalTestDAO;

    public MedicalTestController() {
        this.medicalTestDAO = new CheckItemDAO();
    }

    public List<CheckItem> getAllTests() {
        return medicalTestDAO.getAllTests();
    }

    public CheckItem getTestById(Long id) {
        return medicalTestDAO.getTestById(id);
    }

    public boolean addTest(CheckItem test) {
        return medicalTestDAO.addTest(test);
    }

    public boolean updateTest(CheckItem test) {
        return medicalTestDAO.updateTest(test);
    }

    public boolean deleteTest(Long id) {
        return medicalTestDAO.deleteTest(id);
    }
}
