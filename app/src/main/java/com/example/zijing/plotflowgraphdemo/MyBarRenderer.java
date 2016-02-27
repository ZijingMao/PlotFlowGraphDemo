package com.example.zijing.plotflowgraphdemo;

import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

/**
 * Created by EEGLab on 2/27/2016.
 */
public class MyBarRenderer extends BarRenderer<MyBarFormatter> {

    public MyBarRenderer(XYPlot plot) {
        super(plot);
    }

    /**
     * Implementing this method to allow us to inject our
     * special selection getFormatter.
     * @param index index of the point being rendered.
     * @param series XYSeries to which the point being rendered belongs.
     * @return
     */
    @Override
    public MyBarFormatter getFormatter(int index, XYSeries series) {
        // try to implement this if want to get selection formatter changes
        return getFormatter(series);
    }

}
