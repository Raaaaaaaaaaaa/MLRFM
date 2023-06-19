package com.mlhui.others;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:27
* @description used to store the relation in taxonomy
 */
public class RelationalPair {
    private int child;
    private int parent;

    public RelationalPair() {
    }

    public RelationalPair(int child, int parent) {
        this.child = child;
        this.parent = parent;
    }

    public int getChild() {
        return child;
    }

    public void setChild(int child) {
        this.child = child;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }
}
