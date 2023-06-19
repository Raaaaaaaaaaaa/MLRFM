package com.mlhui.others;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:25
* @description used to store the item profit table
 */
public class ExternalPair {
    private int item;
    private int utility;

    public ExternalPair() {
    }

    public ExternalPair(int item, int utility) {
        this.item = item;
        this.utility = utility;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public int getUtility() {
        return utility;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }
}
