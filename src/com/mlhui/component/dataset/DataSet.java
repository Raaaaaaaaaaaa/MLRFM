package com.mlhui.component.dataset;

import com.mlhui.component.Transaction;
import com.mlhui.component.dataset.component.UtilityList;
import com.mlhui.others.ExternalPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:40
* @description store the dataset will be used in the MLRFM
 */
public class DataSet {
    //store the UtilityList in different level
    private Map<Integer, List<UtilityList>> UtilityListPerLevel = new HashMap<>();
    //store the transaction database
    private List<Transaction> transactionList = new ArrayList<>();
    @Deprecated
    //store the item and corresponding utility/monetary
    private List<ExternalPair> itemProfitTable = new ArrayList<>();
    //
    private List<Integer> itemTable = new ArrayList<>();
    public DataSet() {
    }

    public DataSet(Map<Integer, List<UtilityList>> utilityListPerLevel, List<Transaction> transactionList, List<ExternalPair> itemProfitTable) {
        UtilityListPerLevel = utilityListPerLevel;
        this.transactionList = transactionList;
        this.itemProfitTable = itemProfitTable;
    }

    public List<Integer> getItemTable() {
        return itemTable;
    }

    public void setItemTable(List<Integer> itemTable) {
        this.itemTable = itemTable;
    }

    public Map<Integer, List<UtilityList>> getUtilityListPerLevel() {
        return UtilityListPerLevel;
    }

    public void setUtilityListPerLevel(Map<Integer, List<UtilityList>> utilityListPerLevel) {
        UtilityListPerLevel = utilityListPerLevel;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public List<ExternalPair> getItemProfitTable() {
        return itemProfitTable;
    }

    public void setItemProfitTable(List<ExternalPair> itemProfitTable) {
        this.itemProfitTable = itemProfitTable;
    }
}
