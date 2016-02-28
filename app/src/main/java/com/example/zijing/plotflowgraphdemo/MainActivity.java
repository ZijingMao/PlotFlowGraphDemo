package com.example.zijing.plotflowgraphdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesRenderer;
import com.androidplot.xy.XYStepMode;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private XYPlot dynamicPlot, histPlot;
    private MyPlotUpdater plotUpdaterHist;

    private SampleDynamicSeries[] sampleDynamicSeries;
    private SampleHistSeries[] sampleHistogramSeries;

    private SampleDynamicXYDatasource data;
    private SampleHistXYDatasource dataPower;

    private Thread histThread;
    private Intent dataStreamIntent;

    private BroadcastReceiver receiver;

    public static int channelSize = 12;
    public static int powerBandSize = 5;
    public static int separateChannelsRange = 10000;

    public static String[] powerbandName = {"", "Delta", "Theta", "Alpha", "Beta", "Gamma" , ""};
    public static String[] channelName = {"", "O2", "Oz", "O1", "Pz", "C4", "Cz", "C3", "Fz", "F8", "Fp2", "Fp1", "F7", ""};

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

        receiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI(intent);
            }
        };

        // get handles to our View defined in layout.xml:
        dynamicPlot = (XYPlot) findViewById(R.id.xyPlot);
        histPlot = (XYPlot) findViewById(R.id.histplot);

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
                    chanIdx, ""+chanIdx);
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
                    powerIdx, ""+powerIdx);
            int[] rgbColor = setRGBColor(powerIdx, powHistIdx);
            MyBarFormatter formatter1 = new MyBarFormatter(
                    Color.argb(200, rgbColor[0], rgbColor[1], rgbColor[2]),
                    Color.argb(200, rgbColor[0], rgbColor[1], rgbColor[2]));

            histPlot.addSeries(sampleHistogramSeries[powerIdx], formatter1);
        }

        histPlot.setDomainStepValue(powerbandName.length);
        histPlot.setTicksPerRangeLabel(3);
        histPlot.setTicksPerDomainLabel(1);
        // per the android documentation, the minimum and maximum readings we can get from
        // any of the orientation sensors is -180 and 359 respectively so we will fix our plot's
        // boundaries to those values.  If we did not do this, the plot would auto-range which
        // can be visually confusing in the case of dynamic plots.
        histPlot.setRangeBoundaries(-180, 359, BoundaryMode.FIXED);
        histPlot.setDomainBoundaries(-1, 5, BoundaryMode.FIXED);

        // update our domain and range axis labels:
        histPlot.setDomainLabel("Frequency Band");
        histPlot.getDomainLabelWidget().pack();
        histPlot.setRangeLabel("Power Level (dB)");
        // histPlot.getRangeLabelWidget().pack();
        histPlot.setGridPadding(15, 10, 15, 0);
        histPlot.setRangeValueFormat(new DecimalFormat("#"));

        histPlot.setDomainValueFormat(new Format() {
            @Override
            public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                int parsedInt = Math.round(Float.parseFloat(object.toString())) + 1;
                String labelString = powerbandName[parsedInt];
                buffer.append(labelString);
                return buffer;
            }

            @Override
            public Object parseObject(String string, ParsePosition position) {
                return java.util.Arrays.asList(powerbandName).indexOf(string);
            }
        });

        // cast to my render bar style
        MyBarRenderer renderer = (MyBarRenderer) histPlot.getRenderer(MyBarRenderer.class);
        // set render style as default
        // renderer.setBarRenderStyle((BarRenderer.BarRenderStyle)spRenderStyle.getSelectedItem());
        renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.FIXED_WIDTH);
        // set render width as desired in the future
        renderer.setBarWidth((float) 50);

        // hook up the plotUpdater to the data model:
        dataPower.addObserver(plotUpdaterHist);

        // thin out domain tick labels so they dont overlap each other:
        dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(20);

        dynamicPlot.setRangeStepValue(channelSize + 2);
        dynamicPlot.setTicksPerRangeLabel(1);

        dynamicPlot.setRangeValueFormat(new DecimalFormat("###.#"));

        // uncomment this line to freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(-1 * separateChannelsRange,
                separateChannelsRange * channelSize,
                BoundaryMode.FIXED);

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);

        dynamicPlot.setRangeValueFormat(new Format() {
            @Override
            public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                int parsedInt =
                        Math.round(Float.parseFloat(object.toString())/separateChannelsRange) + 1;
                String labelString = channelName[parsedInt];
                buffer.append(labelString);
                return buffer;
            }

            @Override
            public Object parseObject(String string, ParsePosition position) {
                return java.util.Arrays.asList(channelName).indexOf(string);
            }
        });

    }

    private void updateUI(Intent intent) {
        data.updateDataStream(intent);
        dynamicPlot.redraw();
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

        DataProcessIntentService.backgroundServiceRunning = false;
        unregisterReceiver(receiver);

        dataPower.stopThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // kick off the data generating thread:

        DataProcessIntentService.startActionSendData(
                MainActivity.this, "EEG", "Cog");

        registerReceiver(receiver,
                new IntentFilter(DataProcessIntentService.EXTRA_RESULT));

        histThread = new Thread(dataPower);

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
