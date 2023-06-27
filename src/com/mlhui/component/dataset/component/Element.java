package com.mlhui.component.dataset.component;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:37
* @description store the transaction information of an item
 */
public class Element {
    private int tid;
    //the utility of item in this transaction
    private double utility;
    //the remaining utility in this transaction based on TWU ascending order
    private double remainingUtility;

    public Element() {
    }

    public Element(int tid, double utility, double remainingUtility) {
        this.tid = tid;
        this.utility = utility;
        this.remainingUtility = remainingUtility;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public double getUtility() {
        return utility;
    }

    public void setUtility(double utility) {
        this.utility = utility;
    }

    public double getRemainingUtility() {
        return remainingUtility;
    }

    public void setRemainingUtility(double remainingUtility) {
        this.remainingUtility = remainingUtility;
    }
}
