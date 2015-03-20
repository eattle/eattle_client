package com.example.cds.eattle_prototype_2.model;

/**
 * Created by CDS on 15. 3. 20..
 */
public class Manager {
    int totalPictureNum;
    long averageInterval;
    long standardDerivation;

    public Manager(){}

    public Manager(int totalPictureNum, long averageInterval, long standardDerivation) {
        this.totalPictureNum = totalPictureNum;
        this.averageInterval = averageInterval;
        this.standardDerivation = standardDerivation;
    }

    public void setTotalPictureNum(int totalPictureNum) {
        this.totalPictureNum = totalPictureNum;
    }

    public void setAverageInterval(long averageInterval) {
        this.averageInterval = averageInterval;
    }

    public void setStandardDerivation(long standardDerivation) {
        this.standardDerivation = standardDerivation;
    }

    public int getTotalPictureNum() {

        return totalPictureNum;
    }

    public long getAverageInterval() {
        return averageInterval;
    }

    public long getStandardDerivation() {
        return standardDerivation;
    }
}
