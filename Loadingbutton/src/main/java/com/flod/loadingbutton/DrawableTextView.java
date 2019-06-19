package com.flod.loadingbutton;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * SimpleDes:
 * Creator: Flood
 * Date: 2019-06-13
 * UseDes:
 */
public class DrawableTextView extends AppCompatImageView {

    private Bitmap mBitmap;
    private Paint mPaint;
    private float animValue;
    private ObjectAnimator mObjectAnimator;
    private boolean isShowing;

    private Path mCirclePath;


    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mObjectAnimator = ObjectAnimator.ofFloat(this, "animValue", 1.0f);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        mCirclePath = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int r = mBitmap.getHeight() / 2;
        Log.d("onDraw", r * animValue + "");
        canvas.save();
        mCirclePath.reset();
        mCirclePath.addCircle(r, r, r * animValue, Path.Direction.CW);
        canvas.clipPath(mCirclePath);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.restore();
    }


    public float getAnimValue() {
        return animValue;
    }

    public void setAnimValue(float animValue) {
        this.animValue = animValue;
        invalidate();


    }

    public void toggle() {
        isShowing = !isShowing;
        if (isShowing)
            mObjectAnimator.start();
        else {
            mObjectAnimator.reverse();
        }
    }


}
