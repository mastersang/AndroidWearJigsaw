package com.bauhaus_uni.mis.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] mImageIds = {R.drawable.image1, R.drawable.image2,};
        Intent intent = new Intent(this, PuzzleSelectActivity.class);
        intent.putExtra("images", mImageIds);
        startActivity(intent);
    }
}
