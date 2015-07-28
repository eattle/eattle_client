package com.eattle.phoket.model;

/**
 * Created by CDS on 15. 3. 20..
 */
public class Manager {
    int totalPictureNum;//전체 사진 개수
    int realPictureNum;//분류에 포함되는 사진만
    long averageInterval;
    long standardDerivation;

    public Manager(){
        this.totalPictureNum = 0;
        this.realPictureNum = 0;
        this.averageInterval = 0;
        this.standardDerivation = 0;
    }

    public Manager(int totalPictureNum, int realPictureNum, long averageInterval, long standardDerivation) {
        this.totalPictureNum = totalPictureNum;
        this.realPictureNum = realPictureNum;
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

    public void setRealPictureNum(int realPictureNum) {
        this.realPictureNum = realPictureNum;
    }

    public int getRealPictureNum() {

        return realPictureNum;
    }

    public long getAverageInterval() {
        return averageInterval;
    }

    public long getStandardDerivation() {
        return standardDerivation;
    }
}
