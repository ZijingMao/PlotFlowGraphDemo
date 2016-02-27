package com.example.zijing.plotflowgraphdemo;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DataProcessIntentService extends IntentService {
    private final static String TAG = "IntentService";
    public final static int SAMPLE_SIZE = 256;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SEND_DATA = "edu.utsa.eeglab.action.SEND_DATA";

    // TODO: Rename parameters
    private static final String EXTRA_DATA_STREAM = "edu.utsa.eeglab.extra.SEND_DATA_STREAM_HERE";
    private static final String EXTRA_TAG_VERIFY = "edu.utsa.eeglab.extra.TAG_USED_VERIFICATION";

    public static final String EXTRA_RESULT = "edu.utsa.eeglab.extra.SEND_RESULT";
    public static final String EXTRA_MESSAGE = "edu.utsa.eeglab.extra.RECEIVE_MESSAGE";
    public static final String EXTRA_SAMPLE_INFO_CHUNK = "edu.utsa.eeglab.extra.SEND_SAMPLE_INFO_CHUNK";
    public static final String EXTRA_MESSAGE_CHUNK = "edu.utsa.eeglab.extra.RECEIVE_MESSAGE_CHUNK";

    public static boolean backgroundServiceRunning = true;
    public static int sampleRate = 256;
    private static int bufferLengthInSamples = SAMPLE_SIZE;
    public static int bufferLengthInSeconds = bufferLengthInSamples/sampleRate;

    public DataProcessIntentService() {
        super("DataProcessIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSendData(Context context,
                                           String deviceName,
                                           String tagVerify) {
        Intent intent = new Intent(context, DataProcessIntentService.class);
        intent.setAction(ACTION_SEND_DATA);
        intent.putExtra(EXTRA_DATA_STREAM, deviceName);
        intent.putExtra(EXTRA_TAG_VERIFY, tagVerify);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Start collecting data now...", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Stop collecting data.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent Service executed(onHandleIntent)");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_DATA.equals(action)) {
                final String deviceName = intent.getStringExtra(EXTRA_DATA_STREAM);
                final String tagVerify = intent.getStringExtra(EXTRA_TAG_VERIFY);
                handleActionSendData(deviceName, tagVerify);
            }
        }
    }

    /**
     * Handle action send data in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSendData(String deviceName, String tagVerify) {
        // display the message

        // obtain the data from lsl


        // set the variable used for storing data
        long sample_length;
        String final_sample;

        // get the index of the buffer location
        int indexBufferLocation = 0, tmp = 0;
        // set the corresponding buffer
        String [] bufferEEGData = new String [bufferLengthInSamples];

        Log.d(TAG, "Service on start now ...");
        Intent dataBroadcastIntent = new Intent(EXTRA_RESULT);
        while(backgroundServiceRunning) {
            double[] sample = new double[4];
            sample_length = sample.length;
            String curr_sample = "";
            for(int idx=0; idx<sample_length; idx++) {
                curr_sample += sample[idx];
                curr_sample += "\n";
            }
            // remove the last comma
            if(!curr_sample.equals(""))
                curr_sample = curr_sample.substring(0, curr_sample.length() - 1);
            final_sample = curr_sample;

            // TODO implement stuff of processing chunk here

            // broad cast the final sample to other activities
            dataBroadcastIntent.putExtra(EXTRA_MESSAGE, final_sample);
            sendBroadcast(dataBroadcastIntent);
            // SystemClock.sleep(1000);
        }
        // throw new UnsupportedOperationException("Not yet implemented");
    }

}
