package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class HardLoadingButton extends AppCompatTextView {
    private CircularProgressDrawable mProgressDrawable;
    private ValueAnimator mAnimator;
    private int originalWidth = -1;
    private int originalHeight = -1;
    private int[] originalPadding = new int[4];
    private Drawable[] originalDrawables;
    private CharSequence mText;

    private EndDrawable mEndDrawable;

    public HardLoadingButton(Context context) {
        this(context, null);
    }

    public HardLoadingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HardLoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mProgressDrawable = new CircularProgressDrawable(context);
        originalDrawables = getCompoundDrawables();
        setUpAnimator();
        setLayerType(LAYER_TYPE_HARDWARE, null);

    }

    private void setUpAnimator() {
        final ValueAnimator animator = new ValueAnimator();
        animator.setDuration(600);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getLayoutParams().width = (int) animation.getAnimatedValue();
                getLayoutParams().height = getOriginalHeight();
                requestLayout();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                if (!isReverse) {
                    mText = getText();
                    setText("");

                } else {
                    mProgressDrawable.stop();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                if (!isReverse) {
                    startProgressDrawable();
                    mProgressDrawable.start();

                    toggle();
                } else {
                    setCompoundDrawables(originalDrawables[0], originalDrawables[1], originalDrawables[2], originalDrawables[3]);
                    setPadding(originalPadding[0], originalPadding[1], originalPadding[2], originalPadding[3]);
                    setText(mText);
                }
            }
        });

        mAnimator = animator;
    }

    private void startProgressDrawable() {
        setPadding(0, 0, 0, 0);
        final int defaultSize = getProgressDrawableSize();
        mProgressDrawable.setStrokeWidth(defaultSize * 0.12f);
        mProgressDrawable.setColorSchemeColors(getTextColors().getDefaultColor());
        final int left = (getOriginalHeight() - defaultSize) / 2;
        mProgressDrawable.setBounds(left, 0, left + defaultSize, defaultSize);
        setCompoundDrawables(mProgressDrawable, null, null, null);
    }

    public CircularProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public void start() {
        mAnimator.cancel();
        if (mAnimator.getValues() == null) {
            mAnimator.setIntValues(getOriginalWidth(), getShrinkSize());
        }
        mAnimator.start();
    }


    public void stop() {
        mAnimator.cancel();
        mAnimator.reverse();
    }

    public void toggle() {
        mEndDrawable = new EndDrawable();
        mEndDrawable.toggle();
    }


    private int getShrinkSize() {
        return getOriginalHeight();
    }

    private int getProgressDrawableSize() {
        int outSize = Math.min(getOriginalWidth(), getOriginalHeight());
        return (int) (outSize * 0.7);
    }


    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        originalPadding[0] = getPaddingStart();
        originalPadding[1] = getPaddingTop();
        originalPadding[2] = getPaddingEnd();
        originalPadding[3] = getPaddingBottom();
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        if (!(left instanceof CircularProgressDrawable) && originalDrawables != null) {
            originalDrawables[0] = left;
            originalDrawables[1] = top;
            originalDrawables[2] = right;
            originalDrawables[3] = bottom;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    public int getOriginalWidth() {
        if (originalWidth == -1)
            originalWidth = getWidth();
        return originalWidth;
    }

    public int getOriginalHeight() {
        if (originalHeight == -1)
            originalHeight = getHeight();
        return originalHeight;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mEndDrawable != null)
            mEndDrawable.draw(canvas);
    }

    class EndDrawable {
        private Drawable mDrawable;
        private Bitmap mBitmap;

        private Paint mEraserPaint; //擦除画笔

        private Canvas EndDrawableCanvas;
        private Rect mRect = new Rect();

        private ObjectAnimator mObjectAnimator;
        private float animValue;

        private boolean isShowing;


        EndDrawable() {
            mBitmap = getBitmap(getContext(), R.mipmap.ic_launcher, -1);
            mRect = new Rect(0, 0, 100, 100);
            //mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG );
            mEraserPaint.setStyle(Paint.Style.STROKE);
           mEraserPaint.setStrokeWidth(getShrinkSize());
            mEraserPaint.setColor(0);
            //几种擦除方式：http://ssp.impulsetrain.com/porterduff.html
            mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

            // mBitmap = Bitmap.createBitmap(getShrinkSize(), getShrinkSize(), Bitmap.Config.ARGB_8888);
            //mBitmap.eraseColor(0); //去掉试试
            //EndDrawableCanvas = new Canvas(mBitmap);

           /* mObjectAnimator = ObjectAnimator.ofFloat(this, "animValue", isShowing ? 1 : 0f);
            mObjectAnimator.setDuration(300);*/
        }

        void start(boolean isChecked) {
            isShowing = isChecked;
            mObjectAnimator = ObjectAnimator.ofFloat(this, "animValue", isChecked ? 1.0f : 0.0f);
            mObjectAnimator.setDuration(300);
            mObjectAnimator.start();
        }

        void toggle() {
            start(!isShowing);
        }

        private void draw(Canvas canvas) {
           /* final float value = animValue < 0.5f ? 0 : (animValue - 0.5f) / 0.5f;

            int w = mDrawable.getIntrinsicWidth();
            int h = mDrawable.getIntrinsicHeight();
            int x = (getShrinkSize() - w) / 2;
            int y = (getShrinkSize() - h) / 2;
            mDrawable.setBounds(x, y, x + w, y + h);
            mDrawable.draw(EndDrawableCanvas);
            EndDrawableCanvas.drawCircle(getShrinkSize() >> 1, getShrinkSize() >> 1,
                    (getShrinkSize() >> 1) * (1 - value), mEraserPaint);*/


            Log.d("onDraw",((getShrinkSize() >> 1) * (1 - animValue))+"");

            EndDrawableCanvas.drawCircle(getShrinkSize() >> 1, getShrinkSize() >> 1,
                    (getShrinkSize() >> 1) * (1 - animValue), mEraserPaint);


            canvas.drawBitmap(mBitmap, 0, 0, null);
        }


        public void setAnimValue(float animValue) {
            this.animValue = animValue;
            invalidate();
        }


        private Bitmap getBitmap(Context context, int resId, int color) {
            Drawable drawable = ContextCompat.getDrawable(context, resId);
            return getBitmap(context, drawable, color);
        }

        private Bitmap getBitmap(Context context, Drawable drawable, int color) {
            if (drawable != null) {
                if (color != -1) {
                    drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                EndDrawableCanvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, EndDrawableCanvas.getWidth(), EndDrawableCanvas.getHeight());
                drawable.draw(EndDrawableCanvas);
                return bitmap;
            }
            return null;
        }
    }


}
