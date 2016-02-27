package com.example.zijing.plotflowgraphdemo;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * Created by EEGLab on 2/27/2016.
 */
public class SampleHistXYDatasource implements Runnable {

    private static final int SAMPLE_SIZE = 5;
    private int phase = 0;
    private int maxCounter = 40;
    private int powerBandSize = MainActivity.powerBandSize;
    int [] currentData = new int[powerBandSize];
    int [] nextData = new int[powerBandSize];
    int [] stepData = new int[powerBandSize];
    int stepSize = 200/powerBandSize;
    private MyObservable notifier;
    private boolean keepRunning = false;
    private Random rand = new Random();

    {
        notifier = new MyObservable();
    }

    @Override
    public void run() {
        try {
            keepRunning = true;
            int counter = 0;
            int stepSize = 1;
            for (int powerIdx = 0; powerIdx < powerBandSize; powerIdx++) {
                nextData[powerIdx] = rand.nextInt(300);
                currentData[powerIdx] = 0;
                stepData[powerIdx] = (nextData[powerIdx] - currentData[powerIdx])/maxCounter;
            }
            while (keepRunning) {
                Thread.sleep(50); // decrease or remove to speed up the refresh rate.
                if (counter == maxCounter) {
                    // get new data
                    for (int powerIdx = 0; powerIdx < powerBandSize; powerIdx++) {
                        nextData[powerIdx] = rand.nextInt(300);
                        stepData[powerIdx] =
                                (nextData[powerIdx] - currentData[powerIdx])/maxCounter;
//                            rand.nextInt(10) + powerIdx*stepSize - 100;
                    }
                    // reset counter to 0
                    counter = 0;
                } else {
                    for (int powerIdx = 0; powerIdx < powerBandSize; powerIdx++) {
                        currentData[powerIdx] += stepData[powerIdx];
                    }
                    counter++;
                }

                notifier.notifyObservers();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        keepRunning = false;
    }

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

    public int getItemCount(int series) {
        return SAMPLE_SIZE;
    }

    public Number getX(int series, int index) {
        if (index >= SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }
        return series;
    }

    public Number getY(int series, int index) {
        if (index >= SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }

        if (series < powerBandSize) {
            return currentData[series];
        }else{
            throw new IllegalArgumentException();
        }
    }

    public void removeObserver(Observer observer) {
        notifier.deleteObserver(observer);
    }
}
