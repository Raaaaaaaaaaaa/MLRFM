package com.mlhui;

import com.mlhui.component.Taxonomy;
import com.mlhui.component.dataset.DataSet;

public class MLHUI {
    private DataSet dataSet = new DataSet();
    private Taxonomy taxonomy = new Taxonomy();

    public MLHUI() {
    }

    public MLHUI(DataSet dataSet, Taxonomy taxonomy) {
        this.dataSet = dataSet;
        this.taxonomy = taxonomy;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(Taxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }
}
