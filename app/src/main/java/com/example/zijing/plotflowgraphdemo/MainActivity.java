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

    private XYPlot dynamicPlot;
    private MyPlotUpdater plotUpdater;
    private SampleDynamicSeries[] sampleDynamicSeries;
    private SampleDynamicXYDatasource data;
    private Thread myThread;

    private int channelSize = 4;

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

        dynamicPlot = (XYPlot) findViewById(R.id.xyPlot);

        // get handles to our View defined in layout.xml:
        dynamicPlot = (XYPlot) findViewById(R.id.xyPlot);

        plotUpdater = new MyPlotUpdater(dynamicPlot);

        // only display whole numbers in domain labels
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

        // getInstance and position datasets:
        data = new SampleDynamicXYDatasource();

        // three color here
        int powIdx = (int) Math.ceil(Math.pow(channelSize, 1.0/3));
        sampleDynamicSeries = new SampleDynamicSeries[channelSize];
        for (int chanIdx = 0; chanIdx < channelSize; chanIdx++) {
            sampleDynamicSeries[chanIdx] = new SampleDynamicSeries(data,
                    chanIdx, "Sine "+chanIdx);
            int[] rgbColor = setRGBColor(chanIdx, powIdx);
            LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                    Color.rgb(rgbColor[0], rgbColor[1], rgbColor[2]), null, null, null);
            formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
            formatter1.getLinePaint().setStrokeWidth(10);
            dynamicPlot.addSeries(sampleDynamicSeries[chanIdx], formatter1);
        }

        // hook up the plotUpdater to the data model:
        data.addObserver(plotUpdater);

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
        super.onPause();
    }

    @Override
    protected void onResume() {
        // kick off the data generating thread:
        myThread = new Thread(data);
        myThread.start();
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
