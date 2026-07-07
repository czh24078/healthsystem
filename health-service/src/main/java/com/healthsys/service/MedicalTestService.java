package com.healthsys.service;

import com.healthsys.dao.CheckItemDAO;
import com.healthsys.common.entity.CheckItem;

import java.util.List;

public class MedicalTestService {
    private CheckItemDAO medicalTestDAO;

    public MedicalTestService() {
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
