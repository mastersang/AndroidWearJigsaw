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

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by 12086 on 2017/6/30.
 */

public class Gyroscope extends WearableActivity implements SensorEventListener,DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final String TAG ="sensor" ;
    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private SensorManager mSensorManager;
    private Sensor mGyroscope;
    private GoogleApiClient mGoogleApiClient;
    private static final String DataMapKeys="com.bauhaus_uni.mis.myapplication";
    private static final String PATH_SENSOR_DATA = "/mGyroscope";
    private static final String DataKey_Gyroscope_X="Gyroscope_x";
    private static final String DataKey_Gyroscope_Y="Gyroscope_y";
    private static final String DataKey_Gyroscope_Z="Gyroscope_z";
    private static final String DataKey_Gyroscope="Gyroscope";
    private float[] deltaRotationVector = new float[4];
    private float timestamp;
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
        mGyroscope=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(mGyroscope!=null){
            mTextView.setText("HELLO Gyroscope");
        }else{
            mTextView.setText("Sorry, there is no Gyroscope sensor");
        }

        mSensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);

        startDataUpDated();
    }

    private void startDataUpDated() {
        if(mGoogleApiClient==null)
        {
            Log.d(TAG,"no ApiConnected ");
            return;
        }
        PutDataMapRequest putDataMapReq= PutDataMapRequest.create(PATH_SENSOR_DATA);
        putDataMapReq.getDataMap().putFloat(DataKey_Gyroscope_X, deltaRotationVector[0]);
        putDataMapReq.getDataMap().putFloat(DataKey_Gyroscope_Y, deltaRotationVector[1]);
        putDataMapReq.getDataMap().putFloat(DataKey_Gyroscope_Z, deltaRotationVector[2]);
        putDataMapReq.getDataMap().putFloat(DataKey_Gyroscope, deltaRotationVector[3]);
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

        if (timestamp != 0) {
            final float dT=(event.timestamp-timestamp)*NS2S;
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            float omegaMagnitude = (float) sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            if(omegaMagnitude>2){
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) sin(thetaOverTwo);
            float cosThetaOverTwo = (float) cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
            Log.i(TAG,"\n x"+deltaRotationVector[0]);
            Log.i(TAG,"\n y"+deltaRotationVector[1]);
            Log.i(TAG,"\n z"+deltaRotationVector[2]);
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);



        //https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-gyro
        startDataUpDated();


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);
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
