package com.flod.hardloadingbutton;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.flod.loadingbutton.LoadingButton;

public class MainActivity extends AppCompatActivity {
    boolean mBoolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LoadingButton view = findViewById(R.id.test2);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBoolean) {
                    view.start();
                } else
                    view.stop();
                mBoolean = !mBoolean;
            }
        });

        view.setOnLoadingListener(new LoadingButton.OnLoadingListenerAdapter() {
            @Override
            public void onLoadingStart() {
               // view.setText("加载中");
            }

            @Override
            public void onLoadingEnd() {
                //view.setText("加载成功");
            }

        });
/*
        final DrawableTextView view1 = findViewById(R.id.test3);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/






    }
}
