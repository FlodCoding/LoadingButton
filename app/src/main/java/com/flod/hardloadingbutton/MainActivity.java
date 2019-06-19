package com.flod.hardloadingbutton;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.flod.loadingbutton.DrawableTextView;
import com.flod.loadingbutton.HardLoadingButton;

public class MainActivity extends AppCompatActivity {
    boolean mBoolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final HardLoadingButton view = findViewById(R.id.test2);
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

        final DrawableTextView view1 = findViewById(R.id.test3);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view1.toggle();
            }
        });

        //View view2 = findViewById(R.id.test2);

       /* CircularLoadingDrawable drawable = new CircularLoadingDrawable(this);
        //CircularProgressDrawable drawable = new CircularProgressDrawable(this);
        view.setBackground(drawable);
       *//* drawable.setStrokeWidth(60);
        drawable.setArrowEnabled(true);
        drawable.setColorSchemeColors(Color.BLUE,Color.GRAY,Color.GREEN);
        //drawable.setStyle(CircularProgressDrawable.LARGE);
        drawable.setArrowDimensions(60,60);
        drawable.setBackgroundColor(Color.BLUE);*//*
        drawable.start();*/



      /*  CircularProgressDrawable drawable2 = new CircularProgressDrawable(this);
        view2.setBackground(drawable2);
        drawable2.setStrokeWidth(60);
        drawable2.start();*/

        /*CircularProgressDrawable mProgressDrawable = new CircularProgressDrawable(this);
        mProgressDrawable.setBounds(0, 0, 200, 200);
        mProgressDrawable.setStrokeWidth(20f);
        mProgressDrawable.start();
        view.setCompoundDrawables(mProgressDrawable,null,null,null);*/

    }
}
