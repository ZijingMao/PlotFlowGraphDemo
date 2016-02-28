package com.example.zijing.plotflowgraphdemo;

import android.content.Intent;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * Created by Zijing on 2016/2/25.
 */
public class SampleDynamicXYDatasource {

    private static final double FREQUENCY = 5; // larger is lower frequency
    private static final int MAX_AMP_SEED = 100;
    private static final int MIN_AMP_SEED = 10;
    private static final int AMP_STEP = 1;
    public static final int SINE1 = 0;
    public static final int SINE2 = 1;
    private int sampleSize = DataProcessIntentService.SAMPLE_SIZE;
    private int separateChannelsRange = MainActivity.separateChannelsRange;
    private int channelSize = MainActivity.channelSize;
    public double [][] dataChunk = new double[channelSize][sampleSize];
    private boolean keepRunning = false;
    private Random rand = new Random();
    private int currentSampleIdx = 0;

    public int getItemCount(int series) {
        return sampleSize;
    }

    public Number getX(int series, int index) {
        if (index >= sampleSize) {
            throw new IllegalArgumentException();
        }
        return index;
    }

    public Number getY(int series, int index) {
        if (index >= sampleSize) {
            throw new IllegalArgumentException();
        }

        if (series < channelSize) {
            return dataChunk[series][index];
        }else{
            throw new IllegalArgumentException();
        }
    }

    public void updateDataStream(Intent dataStreamIntent){

        double[] dataStreamIntArray = getDoubleDataStream(dataStreamIntent);

        for (int chanIdx = 0; chanIdx < channelSize; chanIdx++){
            for (int timeIdx = 0; timeIdx < sampleSize-1; timeIdx++){
                dataChunk[chanIdx][timeIdx] = dataChunk[chanIdx][timeIdx+1];
            }
            dataChunk[chanIdx][sampleSize-1] =
                    dataStreamIntArray[chanIdx];
        }

    }

    public double [] getDoubleDataStream(Intent dataStreamIntent){
        String inputDataStream = dataStreamIntent.
                getStringExtra(DataProcessIntentService.EXTRA_MESSAGE);
        String [] eegDataVectors = inputDataStream.split("\\n");

        int dataStreamRow = eegDataVectors.length;

        double[] dataStreamIntArray = new double[dataStreamRow];
        for(int row = 0; row < dataStreamRow; row++){
            double dataVal = Double.parseDouble(eegDataVectors[row]);
            dataStreamIntArray[row] = dataVal;
        }

        return dataStreamIntArray;
    }

}
