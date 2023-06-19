package com.mlhui.component.dataset.component;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:37
* @description store the transaction information of an item
 */
public class Element {
    private int tid;
    private int utility;
    private int remainingUtility;

    public Element() {
    }

    public Element(int tid, int utility, int remainingUtility) {
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

    public int getUtility() {
        return utility;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public int getRemainingUtility() {
        return remainingUtility;
    }

    public void setRemainingUtility(int remainingUtility) {
        this.remainingUtility = remainingUtility;
    }
}
