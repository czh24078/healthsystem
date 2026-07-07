package com.healthsys.service;

import com.healthsys.dao.CheckItemDAO;
import com.healthsys.dao.CheckItemGroupDAO;
import com.healthsys.dao.PackageDAO;
import com.healthsys.common.entity.CheckItem;
import com.healthsys.common.entity.CheckItemGroup;

import java.util.List;

public class TestPackageService {
    private CheckItemGroupDAO testPackageDAO;
    private final CheckItemDAO medicalTestDAO;
    private final PackageDAO packageTestDAO;

    public TestPackageService() {
        this.testPackageDAO = new CheckItemGroupDAO();
        this.medicalTestDAO = new CheckItemDAO();
        this.packageTestDAO = new PackageDAO(); // 确保 PackageTestDAO 已创建
    }

    public List<CheckItemGroup> getAllPackages() {
        return testPackageDAO.getAllPackages();
    }

    public CheckItemGroup getPackageById(Long id) {
        return testPackageDAO.getPackageById(id);
    }

    public boolean addPackage(CheckItemGroup testPackage) {
        return testPackageDAO.addPackage(testPackage);
    }

    public boolean updatePackage(CheckItemGroup testPackage) {
        return testPackageDAO.updatePackage(testPackage);
    }

    public boolean deletePackage(Long id) {
        return testPackageDAO.deletePackage(id);
    }

    // 新增获取套餐包含的项目列表的方法
    public List<CheckItem> getTestsInPackage(Long packageId) {
        return packageTestDAO.getTestsByPackage(packageId).stream()
                .map(pt -> medicalTestDAO.getTestById(pt.getTestId()))
                .toList();
    }


    public boolean createCustomPackage(CheckItemGroup testPackage, List<Long> testIds) {
        if (!testPackageDAO.addPackage(testPackage)) {
            return false;
        }

        for (Long testId : testIds) {
            if (!packageTestDAO.addTestToPackage(testPackage.getId(), testId)) {
                return false;
            }
        }

        return true;
    }


}
