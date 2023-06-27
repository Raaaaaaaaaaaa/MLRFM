package com.mlhui.component;

import com.mlhui.others.ExternalPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:41
* @description store the transaction information
 */
public class Transaction {
    private int tid;
    //store the containing items  in different level in transaction
    private Map<Integer, List<Integer>> itemListPerLevel = new HashMap<>();
    private double TU;
    //store the recency because, the formulate of recency only relate to the information of transaction
    private double recency;
    @Deprecated
    //store the count of item in this transaction in this transaction
    private Map<Integer, Integer> mapItemToCount = new HashMap<>();
    //store the Utility of item in different level in this transaction
    private Map<Integer, Double> mapItemToUtility = new HashMap<>();


    public Transaction() {
    }

    public Transaction(int tid, Map<Integer, List<Integer>> itemListPerLevel, double TU, double recency, Map<Integer, Integer> mapItemToCount, Map<Integer, Double> mapItemToUtility) {
        this.tid = tid;
        this.itemListPerLevel = itemListPerLevel;
        this.TU = TU;
        this.recency = recency;
        this.mapItemToCount = mapItemToCount;
        this.mapItemToUtility = mapItemToUtility;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public Map<Integer, List<Integer>> getItemListPerLevel() {
        return itemListPerLevel;
    }

    public void setItemListPerLevel(Map<Integer, List<Integer>> itemListPerLevel) {
        this.itemListPerLevel = itemListPerLevel;
    }

    public double getTU() {
        return TU;
    }

    public void setTU(double TU) {
        this.TU = TU;
    }

    public double getRecency() {
        return recency;
    }

    public void setRecency(double recency) {
        this.recency = recency;
    }

    public Map<Integer, Integer> getMapItemToCount() {
        return mapItemToCount;
    }

    public void setMapItemToCount(Map<Integer, Integer> mapItemToCount) {
        this.mapItemToCount = mapItemToCount;
    }

    public Map<Integer, Double> getMapItemToUtility() {
        return mapItemToUtility;
    }

    public void setMapItemToUtility(Map<Integer, Double> mapItemToUtility) {
        this.mapItemToUtility = mapItemToUtility;
    }
}
