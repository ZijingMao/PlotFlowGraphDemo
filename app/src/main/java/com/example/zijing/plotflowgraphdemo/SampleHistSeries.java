package com.example.zijing.plotflowgraphdemo;

import com.androidplot.xy.XYSeries;

/**
 * Created by EEGLab on 2/27/2016.
 */
public class SampleHistSeries implements XYSeries {

    private SampleHistXYDatasource datasource;
    private int seriesIndex;
    private String title;

    public SampleHistSeries(SampleHistXYDatasource datasource, int seriesIndex, String title) {
        this.datasource = datasource;
        this.seriesIndex = seriesIndex;
        this.title = title;
    }

    @Override
    public int size() {
        return datasource.getItemCount(seriesIndex);
    }

    @Override
    public Number getX(int index) {
        return datasource.getX(seriesIndex, index);
    }

    @Override
    public Number getY(int index) {
        return datasource.getY(seriesIndex, index);
    }

    @Override
    public String getTitle() {
        return title;
    }
}
