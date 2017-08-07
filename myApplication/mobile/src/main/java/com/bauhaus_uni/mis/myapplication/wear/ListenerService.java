package com.bauhaus_uni.mis.myapplication.wear;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * Created by 12086 on 2017/6/13.
 */

public class ListenerService extends WearableListenerService {
    private static final String PATH_SENSOR_DATA = "/mGyroscope";
    private static final String DataKey_Accelormeter_X="Gyroscope_x";
    private static final String DataKey_Accelormeter_Y="Gyroscope_y";
    private static final String DataKey_Accelormeter_Z="Gyroscope_z";
    private DataHandler dataHandler;
    private DataMap dataMap;
    private Messenger messageHandler;
    private static String accX="Gyroscope_x",accY="Gyroscope_y",accZ="Gyroscope_z";


    public void setDataHandler(DataHandler dataHandler){
        this.dataHandler=dataHandler;
    }

    public void onDataChanged(DataEventBuffer dataEvents) {
        Toast.makeText(this, "onDataChanged", Toast.LENGTH_SHORT).show();
        super.onDataChanged(dataEvents);
        final List<DataEvent> events= FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            final Uri uri=event.getDataItem().getUri();
            final String path=uri!=null?uri.getPath():null;
            Log.i("myTag", "DataMap received on watch: " + DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
            // Check the data type
            if( PATH_SENSOR_DATA .equals(path)){
                final DataMap map=DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                float x=map.getFloat(DataKey_Accelormeter_X);
                //sendMessage(x);
                float y=map.getFloat(DataKey_Accelormeter_Y);
                float z=map.getFloat(DataKey_Accelormeter_Z);
                sendMessage(x,accX,y,accY,z,accZ);
                if(dataHandler!=null)
                dataHandler.handler(x,y,z);
            }
            //https://stackoverflow.com/questions/25196033/android-wear-data-items
            // https://stackoverflow.com/questions/20594936/communication-between-activity-and-service

            }
        }
   /* public void sendMessage(float x) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("x",String.valueOf(x));
        message.setData(bundle);
        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //https://stackoverflow.com/questions/20594936/communication-between-activity-and-service
    }*/
    public void sendMessage(float x, String a,float y,String b,float z,String c) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(a,String.valueOf(x));
        bundle.putString(b,String.valueOf(y));
        bundle.putString(c,String.valueOf(z));
        message.setData(bundle);

        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //https://stackoverflow.com/questions/20594936/communication-between-activity-and-service
    }
    public int onStartCommand(Intent intent, int flag, int startId){
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Bundle extras = intent.getExtras();
        messageHandler = (Messenger) extras.get("MESSENGER");
        return super.onStartCommand(intent,flag,startId);
    }
}
