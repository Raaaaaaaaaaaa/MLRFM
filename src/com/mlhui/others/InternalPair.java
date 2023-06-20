package com.mlhui.others;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:26
* @description used to store the item and corresponding count in the transaction
 * database.
 */
@Deprecated
public class InternalPair {
    private int item;
    private int count;

    public InternalPair() {
    }

    public InternalPair(int item, int count) {
        this.item = item;
        this.count = count;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
