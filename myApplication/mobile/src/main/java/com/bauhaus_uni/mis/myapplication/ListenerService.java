package com.bauhaus_uni.mis.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.android.gms.wearable.DataEvent;

import java.util.List;

/**
 * Created by 12086 on 2017/6/13.
 */

public class ListenerService extends WearableListenerService {
    /*private static final String PATH_SENSOR_DATA = "/accelerometer";
    private static final String DataKey_Accelormeter_X="acc_x";
    private static final String DataKey_Accelormeter_Y="acc_y";
    private static final String DataKey_Accelormeter_Z="acc_z";*/

    private static final String PATH_SENSOR_DATA = "/mGyroscope";
    private static final String DataKey_Gyroscope_X = "Gyroscope_x";
    private static final String DataKey_Gyroscope_Y = "Gyroscope_y";
    private static final String DataKey_Gyroscope_Z = "Gyroscope_z";
    private static final String DataKey_Gyroscope = "Gyroscope";

    private DataHandler dataHandler;
    private DataMap dataMap;
    private Messenger messageHandler;
    //private static String accX="X",accY="Y",accZ="Z";
    private static String gyX = "Gyroscope_x", gyY = "Gyroscope_y", gyZ = "Gyroscope_z", gyXYZ = "XYZ";

    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;

            // Check the data type
            if (PATH_SENSOR_DATA.equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        /*        float x=map.getFloat(DataKey_Accelormeter_X);
                //sendMessage(x);
                float y=map.getFloat(DataKey_Accelormeter_Y);
                float z=map.getFloat(DataKey_Accelormeter_Z);
                sendMessage(x,accX,y,accY,z,accZ);*/

                float x = map.getFloat(DataKey_Gyroscope_X);
                //sendMessage(x);
                float y = map.getFloat(DataKey_Gyroscope_Y);
                float z = map.getFloat(DataKey_Gyroscope_Z);
                float xyz = map.getFloat(DataKey_Gyroscope);
                sendMessage(x, gyX, y, gyY, z, gyZ, xyz, gyXYZ);
                if (dataHandler != null)
                    dataHandler.handler(x, y, z);
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
    public void sendMessage(float x, String a, float y, String b, float z, String c, float xyz, String abc) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString(a, String.valueOf(x));
        bundle.putString(b, String.valueOf(y));
        bundle.putString(c, String.valueOf(z));
        bundle.putString(abc, String.valueOf(xyz));
        message.setData(bundle);
        try {
            messageHandler.send(message);
        } catch (Exception e) {
        }
        //https://stackoverflow.com/questions/20594936/communication-between-activity-and-service
    }

    public int onStartCommand(Intent intent, int flag, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Bundle extras = intent.getExtras();
        messageHandler = (Messenger) extras.get("MESSENGER");

        if (messageHandler == null) {
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
        }

        return super.onStartCommand(intent, flag, startId);
        //https://developer.android.com/guide/components/services.html
    }

}
