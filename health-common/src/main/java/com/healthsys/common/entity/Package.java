package com.healthsys.common.entity;

public class Package {
    private Long packageId;
    private Long testId;
    private String name;

    public Package() {}

    public Package(Long packageId, Long testId) {
        this.packageId = packageId;
        this.testId = testId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }
}

