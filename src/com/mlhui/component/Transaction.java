package com.mlhui.component;

import java.util.ArrayList;
import java.util.List;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:41
* @description store the transaction information
 */
public class Transaction {
    private int tid;
    private List<Integer> itemListPerLevel = new ArrayList<>();
    private int TU;
    private double recency;

    public Transaction() {
    }

    public Transaction(int tid, List<Integer> itemListPerLevel, int TU, double recency) {
        this.tid = tid;
        this.itemListPerLevel = itemListPerLevel;
        this.TU = TU;
        this.recency = recency;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public List<Integer> getItemListPerLevel() {
        return itemListPerLevel;
    }

    public void setItemListPerLevel(List<Integer> itemListPerLevel) {
        this.itemListPerLevel = itemListPerLevel;
    }

    public int getTU() {
        return TU;
    }

    public void setTU(int TU) {
        this.TU = TU;
    }

    public double getRecency() {
        return recency;
    }

    public void setRecency(double recency) {
        this.recency = recency;
    }
}
