package com.bauhaus_uni.mis.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import com.google.android.gms.wearable.DataMap;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements DataHandler {

    private GraphView graphView;
    private LineGraphSeries<DataPoint> seriesX, seriesY, seriesZ;
    private ListenerService mListenerService;


    private static final String TAG = "mobile";
    private static TextView textViewX,textViewY,textViewZ;
    public static Handler messageHandler = new MessageHandler();
    private static String accX="X",accY="Y",accZ="Z";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent startService = new Intent(this, ListenerService.class);
        startService.putExtra("MESSENGER", new Messenger(messageHandler));
        this.startService(startService);

        textViewX = (TextView) findViewById(R.id.textView);
        textViewY = (TextView) findViewById(R.id.textView1);
        textViewZ = (TextView) findViewById(R.id.textView2);
        graphView = (GraphView) findViewById(R.id.GraphView1);

        seriesX = new LineGraphSeries<>();
        seriesY = new LineGraphSeries<>();
        seriesZ = new LineGraphSeries<>();

        graphView.addSeries(seriesX);
        graphView.addSeries(seriesY);
        graphView.addSeries(seriesZ);

        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setScrollable(true);
        graphView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        graphView.getViewport().setMinX(1);
        graphView.getViewport().setMaxX(10);

        seriesX.setColor(Color.RED);
        seriesY.setColor(Color.GREEN);
        seriesZ.setColor(Color.BLUE);

        mListenerService = new ListenerService();
        mListenerService.setDataHandler(this);


        //http://android-wear-docs.readthedocs.io/en/latest/data.html
    }

    public void handler(float dataX, float dataY, float dataZ) {

        Log.d(TAG, "accelerometerX" + dataX);
        Log.d(TAG, "accelerometerY" + dataY);
        Log.d(TAG, "accelerometerZ" + dataZ);
    }

    public static class MessageHandler extends Handler {
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            Log.i(TAG,"SendMessageX"+ bundle.getString(accX));
            Log.i(TAG,"SendMessageY"+ bundle.getString(accY));
            Log.i(TAG,"SendMessageZ"+ bundle.getString(accZ));
            textViewX.setText(bundle.getString(accX));
            textViewY.setText(bundle.getString(accY));
            textViewZ.setText(bundle.getString(accZ));
            //https://stackoverflow.com/questions/20594936/communication-between-activity-and-service
        }
    }
}
