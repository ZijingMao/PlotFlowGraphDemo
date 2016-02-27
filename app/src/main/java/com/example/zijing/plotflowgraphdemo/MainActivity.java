package com.example.zijing.plotflowgraphdemo;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private XYPlot dynamicPlot, histPlot;
    private MyPlotUpdater plotUpdater, plotUpdaterHist;

    private SampleDynamicSeries[] sampleDynamicSeries;
    private SampleHistSeries[] sampleHistogramSeries;

    private SampleDynamicXYDatasource data;
    private SampleHistXYDatasource dataPower;

    private Thread myThread, histThread;

    public static int channelSize = 12;
    public static int powerBandSize = 5;

    // redraws a dynamicPlot whenever an update is received:
    private class MyPlotUpdater implements Observer {
        Plot plot;

        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get handles to our View defined in layout.xml:
        dynamicPlot = (XYPlot) findViewById(R.id.xyPlot);
        histPlot = (XYPlot) findViewById(R.id.histplot);

        plotUpdater = new MyPlotUpdater(dynamicPlot);
        plotUpdaterHist = new MyPlotUpdater(histPlot);

        // only display whole numbers in domain labels
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("#"));
        histPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("#"));

        // getInstance and position datasets:
        data = new SampleDynamicXYDatasource();
        dataPower = new SampleHistXYDatasource();

        // three color here
        int powIdx = (int) Math.ceil(Math.pow(channelSize, 1.0/3));
        int powHistIdx = (int) Math.ceil(Math.pow(powerBandSize, 1.0/3));
        sampleDynamicSeries = new SampleDynamicSeries[channelSize];
        sampleHistogramSeries = new SampleHistSeries[powerBandSize];

        // implement the sample dynamics for eeg signals
        for (int chanIdx = 0; chanIdx < channelSize; chanIdx++) {
            sampleDynamicSeries[chanIdx] = new SampleDynamicSeries(data,
                    chanIdx, "Sine "+chanIdx);
            int[] rgbColor = setRGBColor(chanIdx, powIdx);
            LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                    Color.rgb(rgbColor[0], rgbColor[1], rgbColor[2]), null, null, null);
            formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
            formatter1.getLinePaint().setStrokeWidth(3);
            dynamicPlot.addSeries(sampleDynamicSeries[chanIdx], formatter1);
        }

        // implement the histogram for power band
        for (int powerIdx = 0; powerIdx < powerBandSize; powerIdx++) {
            sampleHistogramSeries[powerIdx] = new SampleHistSeries(dataPower,
                    powerIdx, "Sine "+powerIdx);
            int[] rgbColor = setRGBColor(powerIdx, powHistIdx);
            BarFormatter formatter1 = new BarFormatter(
                    Color.rgb(rgbColor[0], rgbColor[1], rgbColor[2]),
                    Color.rgb(rgbColor[0], rgbColor[1], rgbColor[2]));

            histPlot.addSeries(sampleHistogramSeries[powerIdx], formatter1);
        }

        histPlot.setDomainStepValue(3);
        histPlot.setTicksPerRangeLabel(3);
        // per the android documentation, the minimum and maximum readings we can get from
        // any of the orientation sensors is -180 and 359 respectively so we will fix our plot's
        // boundaries to those values.  If we did not do this, the plot would auto-range which
        // can be visually confusing in the case of dynamic plots.
        histPlot.setRangeBoundaries(-180, 359, BoundaryMode.FIXED);

        // update our domain and range axis labels:
        histPlot.setDomainLabel("");
        histPlot.getDomainLabelWidget().pack();
        histPlot.setRangeLabel("Angle (Degs)");
        histPlot.getRangeLabelWidget().pack();
        histPlot.setGridPadding(15, 0, 15, 0);
        histPlot.setRangeValueFormat(new DecimalFormat("#"));

        // hook up the plotUpdater to the data model:
        data.addObserver(plotUpdater);
        dataPower.addObserver(plotUpdaterHist);

        // thin out domain tick labels so they dont overlap each other:
        dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(5);

        dynamicPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setRangeStepValue(10);

        dynamicPlot.setRangeValueFormat(new DecimalFormat("###.#"));

        // uncomment this line to freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);

    }

    private int[] setRGBColor(int chanIdx, int powIdx) {

        int colorStep = (int) Math.floor(255/powIdx);

        int[] rgbColor = new int[3];

        rgbColor[0] = 255 - chanIdx % powIdx * colorStep;
        chanIdx = chanIdx / powIdx;

        rgbColor[1] = 255 - chanIdx % powIdx * colorStep;
        chanIdx = chanIdx / powIdx;

        rgbColor[2] = 255 - chanIdx % powIdx * colorStep;

        return rgbColor;
    }

    @Override
     protected void onPause() {
        data.stopThread();
        dataPower.stopThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // kick off the data generating thread:
        myThread = new Thread(data);
        histThread = new Thread(dataPower);
        myThread.start();
        histThread.start();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
