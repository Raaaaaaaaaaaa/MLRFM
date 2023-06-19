package com.mlhui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:43
* @description store the taxonomy infomation will be used in the algorithm
 */
public class Taxonomy {
    private int maxLevel;
    private List<Integer> itemListPerLevel = new ArrayList<>();
    private Map<Integer, List<Integer>> mapItemToAncestor = new HashMap<>();
    private Map<Integer, List<Integer>> mapItemToChildren = new HashMap<>();

    public Taxonomy() {
    }

    public Taxonomy(int maxLevel, List<Integer> itemListPerLevel, Map<Integer, List<Integer>> mapItemToAncestor, Map<Integer, List<Integer>> mapItemToChildren) {
        this.maxLevel = maxLevel;
        this.itemListPerLevel = itemListPerLevel;
        this.mapItemToAncestor = mapItemToAncestor;
        this.mapItemToChildren = mapItemToChildren;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public List<Integer> getItemListPerLevel() {
        return itemListPerLevel;
    }

    public void setItemListPerLevel(List<Integer> itemListPerLevel) {
        this.itemListPerLevel = itemListPerLevel;
    }

    public Map<Integer, List<Integer>> getMapItemToAncestor() {
        return mapItemToAncestor;
    }

    public void setMapItemToAncestor(Map<Integer, List<Integer>> mapItemToAncestor) {
        this.mapItemToAncestor = mapItemToAncestor;
    }

    public Map<Integer, List<Integer>> getMapItemToChildren() {
        return mapItemToChildren;
    }

    public void setMapItemToChildren(Map<Integer, List<Integer>> mapItemToChildren) {
        this.mapItemToChildren = mapItemToChildren;
    }
}
