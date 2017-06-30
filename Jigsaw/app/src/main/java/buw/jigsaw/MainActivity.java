package buw.jigsaw;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import buw.jigsaw.wear.DataHandler;
import buw.jigsaw.wear.ListenerService;

public class MainActivity extends AppCompatActivity implements DataHandler {
    public static Handler messageHandler = new MessageHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] mImageIds = {R.drawable.image1, R.drawable.image2,};
        Intent intent = new Intent(this, PuzzleSelectActivity.class);
        intent.putExtra("images", mImageIds);
        Intent startService = new Intent(this, ListenerService.class);
        startService.putExtra("MESSENGER", new Messenger(messageHandler));
        this.startService(startService);
    }

    public void handler(float dataX, float dataY, float dataZ) {
    }

    public static class MessageHandler extends Handler {
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
        }
    }
}
