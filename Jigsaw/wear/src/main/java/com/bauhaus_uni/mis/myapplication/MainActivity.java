package com.bauhaus_uni.mis.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity implements SensorEventListener,DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final String TAG ="sensor" ;

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private GoogleApiClient mGoogleApiClient;
    private static final String DataMapKeys="com.bauhaus_uni.mis.myapplication";
    private static final String PATH_SENSOR_DATA = "/accelerometer";
    private static final String DataKey_Accelormeter_X="acc_x";
    private static final String DataKey_Accelormeter_Y="acc_y";
    private static final String DataKey_Accelormeter_Z="acc_z";
    private float[] mCurAccelerometerVal = new float[3];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                // Request access only to the wearable API
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        //Wearable Data layer:  https://developer.android.com/training/wearables/data-layer/accessing.html
        //Wearable Api: https://developers.google.com/android/guides/api-client#WearableApi

        // For accelerometer sensor
        mSensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAccelerometer!=null){
            mTextView.setText("HELLO Accelerometer");
        }else{
            mTextView.setText("Sorry, there is no accelerometer sensor");
        }

        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        startDataUpDated();
    }

    private void startDataUpDated() {
        if(mGoogleApiClient==null)
        {
            Log.d(TAG,"no ApiConnected ");
            return;
        }
        PutDataMapRequest putDataMapReq= PutDataMapRequest.create(PATH_SENSOR_DATA);
        putDataMapReq.getDataMap().putFloat(DataKey_Accelormeter_X, mCurAccelerometerVal[0]);
        putDataMapReq.getDataMap().putFloat(DataKey_Accelormeter_Y, mCurAccelerometerVal[1]);
        putDataMapReq.getDataMap().putFloat(DataKey_Accelormeter_Z, mCurAccelerometerVal[2]);
        putDataMapReq.setUrgent();
        PutDataRequest putDataReq=putDataMapReq.asPutDataRequest();
        //PendingResult<DataApi.DataItemResult>pendingResult=Wearable.DataApi.putDataItem(mGoogleApiClient,putDataReq);
        Wearable.DataApi.putDataItem(mGoogleApiClient,putDataReq);
        // https://developer.android.com/training/wearables/data-layer/data-items.html?hl=zh-cn
        // https://stackoverflow.com/questions/26224298/realtime-data-exchange-between-android-wearable-and-handheld
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.i(TAG,"\n x"+event.values[0]);
        Log.i(TAG,"\n y"+event.values[1]);
        Log.i(TAG,"\n z"+event.values[2]);

        mCurAccelerometerVal[0] = event.values[0];
        mCurAccelerometerVal[1] = event.values[1];
        mCurAccelerometerVal[2] = event.values[2];
        startDataUpDated();


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        mGoogleApiClient.connect();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient,this);
        Log.d(TAG,"connected:"+ bundle);
        //The Data Layer API can be used
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/accelerometer") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}
//debug: https://developer.android.com/training/wearables/apps/debugging.html#usb-debugging