package com.flod.hardloadingbutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = findViewById(R.id.test);
        CircularProgressDrawable drawable = new CircularProgressDrawable(this);
        view.setBackground(drawable);
        drawable.setStrokeWidth(60);
        drawable.setArrowEnabled(true);
        drawable.setColorSchemeColors(Color.BLUE,Color.GRAY,Color.GREEN);
        //drawable.setStyle(CircularProgressDrawable.LARGE);
        drawable.setArrowDimensions(60,60);
        drawable.setBackgroundColor(Color.BLUE);
        drawable.start();

    }
}
