package com.mlhui.component.dataset.component;

import java.util.ArrayList;
import java.util.List;
/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:36
* @description used to store the pHUI's information in extend process.
 * remember that all member variable exclude 'item' in here is belong to itemset of PX,
 * that is item x with its prefix P, not only X.
 */
public class UtilityList {
    int item;
    //store the U(item), that is Utility in database.
    double Utility;
    //store the RU(item), that is Remaining Utility in database.
    double RemainingUtility;
    //store the transaction information that contains item
    List<Element> elements = new ArrayList<>();

    public UtilityList() {
    }

    public UtilityList(int item) {
        this.item = item;
    }

    public UtilityList(int item, int utility, int remainingUtility, List<Element> elements) {
        this.item = item;
        Utility = utility;
        RemainingUtility = remainingUtility;
        this.elements = elements;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public double getUtility() {
        return Utility;
    }

    public void setUtility(int utility) {
        Utility = utility;
    }

    public double getRemainingUtility() {
        return RemainingUtility;
    }

    public void setRemainingUtility(int remainingUtility) {
        RemainingUtility = remainingUtility;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public void addElement(Element element){
        Utility += element.getUtility();
        RemainingUtility += element.getRemainingUtility();
        elements.add(element);
    }
}
