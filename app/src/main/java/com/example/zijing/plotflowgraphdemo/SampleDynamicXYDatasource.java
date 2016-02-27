package com.example.zijing.plotflowgraphdemo;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * Created by Zijing on 2016/2/25.
 */
public class SampleDynamicXYDatasource implements Runnable {

    public void addObserver(Observer observer) {
        notifier.addObserver(observer);
    }

    // encapsulates management of the observers watching this datasource for update events:
    class MyObservable extends Observable {
        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }

    private static final double FREQUENCY = 5; // larger is lower frequency
    private static final int MAX_AMP_SEED = 100;
    private static final int MIN_AMP_SEED = 10;
    private static final int AMP_STEP = 1;
    public static final int SINE1 = 0;
    public static final int SINE2 = 1;
    private static final int SAMPLE_SIZE = 100;
    private int phase = 0;
    private int sinAmp = 1;
    int [] currentData = new int[4];
    int [][] dataChunk = new int[4][SAMPLE_SIZE];
    private MyObservable notifier;
    private boolean keepRunning = false;
    private Random rand = new Random();

    {
        notifier = new MyObservable();
    }

    public void stopThread() {
        keepRunning = false;
    }

    @Override
    public void run() {
        try {
            keepRunning = true;
            boolean isRising = true;
            while (keepRunning) {

                Thread.sleep(100); // decrease or remove to speed up the refresh rate.
                for (int chanIdx = 0; chanIdx < 4; chanIdx++){
                    for (int timeIdx = 0; timeIdx < SAMPLE_SIZE-1; timeIdx++){
                        dataChunk[chanIdx][timeIdx] = dataChunk[chanIdx][timeIdx+1];
                    }
                    dataChunk[chanIdx][SAMPLE_SIZE-1] = rand.nextInt(10) + chanIdx*50 - 100;
                }

                notifier.notifyObservers();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getItemCount(int series) {
        return SAMPLE_SIZE;
    }

    public Number getX(int series, int index) {
        if (index >= SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }
        return index;
    }

    public Number getY(int series, int index) {
        if (index >= SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }

        if (series < 4) {
            return dataChunk[series][index];
        }else{
            throw new IllegalArgumentException();
        }
    }

    public void removeObserver(Observer observer) {
        notifier.deleteObserver(observer);
    }
}
