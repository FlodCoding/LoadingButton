package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



/**
 * SimpleDes:
 * Creator: Flood
 * Date: 2019-06-13
 * UseDes:
 * <p>
 * 1、改变loading的大小 √
 * 2、收缩动画后不居中 √
 * 3、收缩后的大小随loading的大小决定 √
 * 4、设置loading 可以设置为上下左右 √
 * 5、重复start的动画处理 √
 * 6、恢复动画还没结束时，点击收缩会变成恢复的状态 √
 * 7、正在显示EndDrawable时，再次点击start会变成恢复状态的加载
 * 先执行 beginShrinkAnim(true) 后执行 beginShrinkAnim(false); √
 * <p>
 * 8、多次start和end 会出错 √
 * 9、设置完Drawable大小后，start后再次设置rootView大小失控,是因为原来是wrap_content √
 * 10、start和compete同时按Loading没有关 √
 * 11、loading完后设置loading大小好像是无效的
 * 12、如果是没有EndDrawable但是有文字，那是否也要停留一段时间呢？
 */
@SuppressWarnings({"UnusedReturnValue,SameParameterValue", "unused"})
public class LoadingButton extends DrawableTextView {
    private int curStatus = STATUS.IDE;      //当前的状态

    interface STATUS {
        int IDE = 0;
        int SHRINKING = 1;
        int LOADING = 2;
        int END_DRAWABLE_SHOWING = 3;
        int RESTORING = 4;
    }

    private Drawable[] mDrawablesSaved;
    private int mDrawablePaddingSaved;
    private CharSequence mTextSaved;
    private boolean mEnableTextInCenterSaved;
    private int[] mRootViewSizeSaved = new int[]{0, 0};


    //Arr
    private boolean disableClickOnLoading;   //Loading中禁用点击，  默认开启
    private boolean enableShrink;            //是否开启收缩动画，    默认开启
    private boolean restoreTextWhenEnd;      //当结束时是否恢复文字， 默认开启
    private ValueAnimator mShrinkAnimator;
    private int mShrinkDuration;             //收缩和恢复的时间，    默认450ms
    private CircularProgressDrawable mLoadingDrawable;
    private OnLoadingListener mOnLoadingListener;
    private EndDrawable mEndDrawable;
    private int mLoadingSize;
    private int mLoadingPosition;

    private boolean isSizeChanging;          //当前布局尺寸正发生改变
    private boolean nextShrinkReverse;       //下一步是否是恢复动画
    private boolean isCancel;                //是取消当前动画
    private boolean isFail;


    public LoadingButton(Context context) {
        super(context);
        init(context, null);
    }

    public LoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        //getConfig
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton);
        enableShrink = array.getBoolean(R.styleable.LoadingButton_enableShrink, true);
        disableClickOnLoading = array.getBoolean(R.styleable.LoadingButton_disableClickOnLoading, true);
        restoreTextWhenEnd = array.getBoolean(R.styleable.LoadingButton_restoreTextWhenEnd, true);
        mShrinkDuration = array.getInt(R.styleable.LoadingButton_shrinkDuration, 450);
        int loadingDrawableSize = array.getDimensionPixelSize(R.styleable.LoadingButton_loadingEndDrawableSize, (int) (enableShrink ? getTextSize() * 2 : getTextSize()));
        int loadingDrawableColor = array.getColor(R.styleable.LoadingButton_loadingDrawableColor, getTextColors().getDefaultColor());
        int loadingDrawablePosition = array.getInt(R.styleable.LoadingButton_loadingDrawablePosition, POSITION.START);
        int endCompleteDrawableResId = array.getResourceId(R.styleable.LoadingButton_endCompleteDrawable, -1);
        int endFailDrawableResId = array.getResourceId(R.styleable.LoadingButton_endFailDrawable, -1);
        int endDrawableAppearTime = array.getInt(R.styleable.LoadingButton_endDrawableAppearTime, EndDrawable.DEFAULT_APPEAR_DURATION);
        int endDrawableDuration = array.getInt(R.styleable.LoadingButton_endDrawableDuration, 900);
        array.recycle();

        //initLoadingDrawable
        mLoadingDrawable = new CircularProgressDrawable(context);
        mLoadingDrawable.setColorSchemeColors(loadingDrawableColor);
        mLoadingDrawable.setStrokeWidth(loadingDrawableSize * 0.14f);

        mLoadingSize = loadingDrawableSize;
        mLoadingPosition = loadingDrawablePosition;
        setDrawable(mLoadingPosition, mLoadingDrawable, loadingDrawableSize, loadingDrawableSize);

        //initLoadingDrawable
        if (endCompleteDrawableResId != -1 || endFailDrawableResId != -1) {
            mEndDrawable = new EndDrawable(endCompleteDrawableResId, endFailDrawableResId);
            mEndDrawable.mAppearAnimator.setDuration(endDrawableAppearTime);
            mEndDrawable.setKeepDuration(endDrawableDuration);
        }

        //initShrinkAnimator
        setUpShrinkAnimator();

        //Start|End -> true  Top|Bottom ->false
        setEnableTextInCenter(mLoadingPosition % 2 == 0);

        if (getRootView().isInEditMode()) {
            mLoadingDrawable.setStartEndTrim(0, 0.8f);
        }

    }


    /**
     * 设置收缩动画，主要用来收缩和恢复布局的宽度，动画开始前会保存一些收缩前的参数（文字，其他Drawable等）
     */
    private void setUpShrinkAnimator() {
        mShrinkAnimator = ValueAnimator.ofFloat(0, 1f);
        mShrinkAnimator.setDuration(mShrinkDuration);
        mShrinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // y = kx + b
                // b = getRootViewSize()
                // k = getRootViewSize() - getLoadingSize
                getLayoutParams().width = (int) ((getShrinkSize() - mRootViewSizeSaved[0]) * (float) animation.getAnimatedValue() + mRootViewSizeSaved[0]);
                getLayoutParams().height = (int) ((getShrinkSize() - mRootViewSizeSaved[1]) * (float) animation.getAnimatedValue() + mRootViewSizeSaved[1]);
                requestLayout();
            }
        });

        mShrinkAnimator.addListener(new AnimatorListenerAdapter() {

            //onAnimationStart(Animator animation, boolean isReverse) 在7.0测试没有调用fuck
            @Override
            public void onAnimationStart(Animator animation) {
                if (!nextShrinkReverse) {
                    //begin shrink
                    curStatus = STATUS.SHRINKING;
                    isSizeChanging = true;
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onShrinking();
                    }

                    saveStatus();
                    LoadingButton.super.setText("", BufferType.NORMAL);
                    setCompoundDrawablePadding(0);
                    setCompoundDrawablesRelative(mLoadingDrawable, null, null, null);
                    setEnableTextInCenter(false);

                } else {
                    //begin restore
                    curStatus = STATUS.RESTORING;
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onRestoring();
                    }

                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!nextShrinkReverse) {
                    //shrink over
                    curStatus = STATUS.LOADING;
                    startLoading();
                    nextShrinkReverse = true;

                } else {
                    //restore over
                    curStatus = STATUS.IDE;
                    isSizeChanging = false;
                    restoreStatus();
                    endCallbackListener();
                    nextShrinkReverse = false;
                }
            }

        });

    }

    /**
     * 开始收缩或恢复
     *
     * @param isReverse true：恢复 ，false：收缩
     * @param lastFrame 是否只显示最后一帧
     */
    private void beginShrinkAnim(boolean isReverse, boolean lastFrame) {
        if (enableShrink) {
            if (mShrinkAnimator.isRunning()) {
                //如果上一个动画还在执行，就结束到最后一帧
                mShrinkAnimator.end();
            }
            nextShrinkReverse = isReverse;
            if (!isReverse) {
                mShrinkAnimator.start();
            } else {
                mShrinkAnimator.reverse();
            }

            if (lastFrame) {
                mShrinkAnimator.end();
            }

        }
    }


    /**
     * 结束时状态的回调，Cancel Fail Complete 三种
     */
    private void endCallbackListener() {
        if (mOnLoadingListener != null) {
            if (isCancel)
                mOnLoadingListener.onCanceled();
            else if (isFail)
                mOnLoadingListener.onFailed();
            else
                mOnLoadingListener.onCompleted();
        }
        isCancel = false;
        isFail = false;
    }


    /**
     * 保存一些即将被改变的数据或状态
     */
    private void saveStatus() {
        mTextSaved = getText();
        mDrawablesSaved = copyDrawables(true);
        mDrawablePaddingSaved = getCompoundDrawablePadding();
        mEnableTextInCenterSaved = isEnableTextInCenter();
    }

    /**
     * 恢复保存的状态
     */
    private void restoreStatus() {
        if (restoreTextWhenEnd)
            setText(mTextSaved);
        setCompoundDrawablePadding(mDrawablePaddingSaved);
        setCompoundDrawablesRelative(mDrawablesSaved[POSITION.START], mDrawablesSaved[POSITION.TOP], mDrawablesSaved[POSITION.END], mDrawablesSaved[POSITION.BOTTOM]);
        setEnableTextInCenter(mEnableTextInCenterSaved);
        getLayoutParams().width = mRootViewSizeSaved[0];
        getLayoutParams().height = mRootViewSizeSaved[1];
        requestLayout();

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                measureTextHeight();
                measureTextWidth();
                removeOnLayoutChangeListener(this);
            }
        });

    }


    /**
     * 如果disableClickOnLoading==true，且不是闲置状态，点击会无效
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //disable click
        if (disableClickOnLoading && curStatus != STATUS.IDE)
            return true;
        return super.onTouchEvent(event);
    }


    /**
     * 开始加载
     */
    private void startLoading() {
        curStatus = STATUS.LOADING;
        mLoadingDrawable.start();

        if (mOnLoadingListener != null) {
            mOnLoadingListener.onLoadingStart();
        }

    }

    /**
     * 停止加载
     */
    private void stopLoading() {
        mLoadingDrawable.stop();
    }

    /**
     * 取消当前所有的动画进程
     *
     * @param withAnim 是否显示恢复动画
     */
    private void cancelAllRunning(boolean withAnim) {
        switch (curStatus) {
            case STATUS.SHRINKING:
                beginShrinkAnim(true, !withAnim);
                stopLoading();
                break;
            case STATUS.LOADING:
                stopLoading();
                if (enableShrink) {
                    beginShrinkAnim(true, !withAnim);
                } else {
                    endCallbackListener();
                    restoreStatus();
                    curStatus = STATUS.IDE;
                }
                break;
            case STATUS.END_DRAWABLE_SHOWING:
                if (mEndDrawable != null) {
                    mEndDrawable.cancel(withAnim);
                } else {
                    beginShrinkAnim(true, !withAnim);
                }
                break;
            case STATUS.RESTORING:
                if (!withAnim)
                    mShrinkAnimator.end();
                else {
                    nextShrinkReverse = true;
                    mShrinkAnimator.reverse();
                }
                break;
        }

    }


    /**
     * 开始加载，默认禁用加载时的点击
     * <p>
     * 步骤：
     * shrink -> startLoading
     */
    public void start() {
        //cancel last loading
        if (curStatus == STATUS.SHRINKING || curStatus == STATUS.LOADING)
            isCancel = true;
        cancelAllRunning(false);

        if (enableShrink) {
            beginShrinkAnim(false, false);
        } else {
            saveStatus();
            if (TextUtils.isEmpty(getText())) {
                setCompoundDrawablePadding(0);
            }
            startLoading();
        }
    }

    /**
     * 结束加载
     * <p>
     * 步骤
     * stopLoading -> showEndDrawable -> restore
     *
     * @param isFail 是否失败，若是失败将回调{@link OnLoadingListener#onFailed()},
     *               否则回调{@link OnLoadingListener#onCompleted()}
     *               并显示对应的EndDrawable
     */
    private void end(boolean isFail) {

        //end running Shrinking
        if (mShrinkAnimator.isRunning()) {
            mShrinkAnimator.end();
        }

        //StopLoading
        stopLoading();
        if (!enableShrink && mOnLoadingListener != null) {
            mOnLoadingListener.onLoadingStop();
        }

        if (mEndDrawable != null) {
            mEndDrawable.show(isFail);
        } else {
            //No EndDrawable,enableShrink
            if (enableShrink) {
                if (curStatus == STATUS.LOADING)
                    beginShrinkAnim(true, false);
                else
                    beginShrinkAnim(true, true);

            } else {
                //No EndDrawable,disableShrink
                curStatus = STATUS.IDE;
                restoreStatus();
                endCallbackListener();
            }


        }
    }

    /**
     * 加载完成
     */
    public void complete() {
        end(false);
    }

    /**
     * 加载错误
     */
    public void fail() {
        end(true);
    }


    /**
     * 取消加载，默认显示恢复动画
     */
    public void cancel() {
        cancel(true);
    }

    /**
     * 取消加载
     *
     * @param withAnim 是否显示收缩动画
     */
    public void cancel(boolean withAnim) {
        if (curStatus != STATUS.IDE) {
            isCancel = true;
            cancelAllRunning(withAnim);
        }
    }

    /**
     * 设置加载中不可点击
     */
    public LoadingButton setDisableClickOnLoading(boolean disable) {
        this.disableClickOnLoading = disable;
        return this;
    }

    /**
     * 设置是否显示收缩动画
     */
    public LoadingButton setEnableShrink(boolean enable) {
        this.enableShrink = enable;
        return this;
    }


    /**
     * 结束时是否恢复文字
     */
    public LoadingButton setRestoreTextWhenEnd(boolean restoreTextWhenEnd) {
        this.restoreTextWhenEnd = restoreTextWhenEnd;
        return this;
    }

    /**
     * 收缩后的尺寸（正方形）
     */
    public int getShrinkSize() {
        return Math.max(Math.min(mRootViewSizeSaved[0], mRootViewSizeSaved[1]), getLoadingEndDrawableSize());
    }

    /**
     * 收缩的Animator，可自行设置参数
     */
    public ValueAnimator getShrinkAnimator() {
        return mShrinkAnimator;
    }

    /**
     * 设置收缩的总时间
     */
    public LoadingButton setShrinkDuration(long milliseconds) {
        mShrinkAnimator.setDuration(milliseconds);
        return this;
    }

    /**
     * 收缩的总时间
     */
    public int getShrinkDuration() {
        return mShrinkDuration;
    }

    /**
     * 拿到CircularProgressDrawable 可自行设置想要的参数
     *
     * @return CircularProgressDrawable
     */
    public CircularProgressDrawable getLoadingDrawable() {
        return mLoadingDrawable;
    }

    /**
     * 加载时的环形颜色，可设置多个
     *
     * @param colors 颜色组
     */
    public LoadingButton setLoadingColor(@NonNull @ColorInt int... colors) {
        mLoadingDrawable.setColorSchemeColors(colors);
        return this;
    }

    public LoadingButton setLoadingStrokeWidth(@Px int size) {
        mLoadingDrawable.setStrokeWidth(size);
        return this;
    }

    /**
     * 设置LoadingDrawable的位置，如果开启收缩动画，则建议放Start或End
     *
     * @param position {@link DrawableTextView.POSITION}
     */
    public LoadingButton setLoadingPosition(@POSITION int position) {
        boolean enableTextInCenter = position % 2 == 0;
        setEnableTextInCenter(enableTextInCenter);
        mEnableTextInCenterSaved = enableTextInCenter;
        setDrawable(mLoadingPosition, null, 0, 0);
        mLoadingPosition = position;
        setDrawable(position, getLoadingDrawable(), getLoadingEndDrawableSize(), getLoadingEndDrawableSize());
        return this;
    }

    /**
     * 设置LoadingDrawable和EnaDrawable的尺寸
     *
     * @param size Px
     */
    public LoadingButton setLoadingEndDrawableSize(@Px int size) {
        mLoadingSize = size;
        setDrawable(mLoadingPosition, mLoadingDrawable, size, size);
        return this;
    }


    public int getLoadingEndDrawableSize() {
        return mLoadingSize;
    }


    public LoadingButton setCompleteDrawable(@DrawableRes int id) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(id, -1);
        else {
            mEndDrawable.setCompleteDrawable(id);
        }
        return this;
    }

    public LoadingButton setCompleteDrawable(Drawable drawable) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(drawable, null);
        else {
            mEndDrawable.setCompleteDrawable(drawable);
        }
        return this;
    }

    public LoadingButton setFailDrawable(@DrawableRes int id) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(-1, id);
        else {
            mEndDrawable.setFailDrawable(id);
        }
        return this;
    }

    public LoadingButton setFailDrawable(Drawable drawable) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(null, drawable);
        else {
            mEndDrawable.setFailDrawable(drawable);
        }
        return this;
    }

    /**
     * EndDrawable 停留显示的时间
     */
    public LoadingButton setEndDrawableKeepDuration(long milliseconds) {
        if (mEndDrawable != null)
            mEndDrawable.setKeepDuration(milliseconds);
        return this;
    }

    public long getEndDrawableDuration() {
        if (mEndDrawable != null)
            return mEndDrawable.mKeepDuration;
        return EndDrawable.DEFAULT_APPEAR_DURATION;
    }


    /**
     * CompleteDrawable或FailDrawable 变大出现的时间
     */
    public LoadingButton setEndDrawableAppearDuration(long milliseconds) {
        if (mEndDrawable != null)
            mEndDrawable.getAppearAnimator().setDuration(milliseconds);
        return this;
    }

    @Nullable
    public ObjectAnimator getEndDrawableAnimator() {
        if (mEndDrawable != null) {
            return mEndDrawable.getAppearAnimator();
        }
        return null;
    }


    @Override
    public void setCompoundDrawablePadding(int pad) {
        super.setCompoundDrawablePadding(pad);
        if (curStatus == STATUS.IDE)
            mDrawablePaddingSaved = pad;
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        if (TextUtils.isEmpty(text) && curStatus != STATUS.IDE) {
            setCompoundDrawablePadding(0);
        }

        if (enableShrink && isSizeChanging) {
            return;
        }
        super.setText(text, type);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (curStatus == STATUS.IDE) {
            mRootViewSizeSaved[0] = getWidth();
            mRootViewSizeSaved[1] = getHeight();
        }
    }

    /**
     * 第一次Layout
     */
    @Override
    protected void onFirstLayout(int left, int top, int right, int bottom) {
        super.onFirstLayout(left, top, right, bottom);
        saveStatus();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mEndDrawable != null)
            mEndDrawable.draw(canvas);
        super.onDraw(canvas);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public class EndDrawable {
        private static final int DEFAULT_APPEAR_DURATION = 300;
        private Bitmap mCompleteBitmap;
        private Bitmap mFailBitmap;
        private Paint mPaint;
        private Rect mBounds = new Rect();
        private Path mCirclePath;   //圆形裁剪路径
        private ObjectAnimator mAppearAnimator;
        private long mKeepDuration;
        private float animValue;
        int[] offsetTemp = new int[]{0, 0};
        private boolean isShowing;
        private Runnable mRunnable;

        private EndDrawable(@Nullable Drawable completeDrawable, @Nullable Drawable failDrawable) {
            setCompleteDrawable(completeDrawable);
            setFailDrawable(failDrawable);
            init();
        }

        private EndDrawable(@DrawableRes int completeResId, @DrawableRes int failResId) {
            setCompleteDrawable(completeResId);
            setFailDrawable(failResId);
            init();
        }

        private void init() {
            setLayerType(LAYER_TYPE_HARDWARE, null);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            mCirclePath = new Path();
            mAppearAnimator = ObjectAnimator.ofFloat(this, "animValue", 1.0f);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    setAnimValue(0);
                    if (enableShrink)
                        beginShrinkAnim(true, !nextShrinkReverse);
                    else {
                        curStatus = STATUS.IDE;
                        restoreStatus();
                        endCallbackListener();
                    }
                    isShowing = false;
                }
            };
            mAppearAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    curStatus = STATUS.END_DRAWABLE_SHOWING;
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onEndDrawableAppear(!isFail, mEndDrawable);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isShowing) {
                        postDelayed(mRunnable, mKeepDuration);
                    }
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onEndDrawableAppearDone(!isFail);
                    }
                }
            });
        }


        /**
         * 显示EndDrawable
         */
        private void show(boolean isFail) {

            //end showing endDrawable
            if (isShowing) {
                cancel(false);
            }

            LoadingButton.this.isFail = isFail;
            mAppearAnimator.start();
            isShowing = true;

        }

        /**
         * 取消出现动画
         *
         * @param withAnim 是否显示恢复动画
         */
        private void cancel(boolean withAnim) {
            isShowing = false;
            if (mAppearAnimator.isRunning()) {
                mAppearAnimator.end();
            }
            getHandler().removeCallbacks(mRunnable);
            if (enableShrink)
                beginShrinkAnim(true, !withAnim);
            else {
                endCallbackListener();
                restoreStatus();
                curStatus = STATUS.IDE;
            }
            setAnimValue(0);
        }

        /**
         * 消失动画，暂不使用
         */
        private void hide() {
            if (isShowing) {
                cancel(false);
            }
            mAppearAnimator.reverse();
            isShowing = true;
        }

        /**
         * 测量EndDrawable需要位移的offsetX和offsetY,(因为EnaDrawable一开始是在左上角开始显示的)
         *
         * @param canvas 当前画布
         * @param bounds LoadingDrawable的显示范围
         * @param pos    EndDrawable的显示位置
         * @return int[0] = offsetX，int[1] = offsetY
         */
        private int[] calcOffset(Canvas canvas, Rect bounds, @POSITION int pos) {
            final int[] offset = offsetTemp;
            offset[0] = canvas.getWidth() / 2;
            offset[1] = canvas.getHeight() / 2;

            switch (pos) {
                case POSITION.START: {
                    offset[0] -= (int) getTextWidth() / 2 + bounds.width() + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[0] += bounds.width() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[0] += (bounds.width() + getCompoundDrawablePadding()) / 2;
                    }

                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.TOP: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] -= (int) getTextHeight() / 2 + bounds.height() + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[1] += bounds.height() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[1] += (bounds.height() + getCompoundDrawablePadding()) / 2;
                    }
                    break;
                }
                case POSITION.END: {
                    offset[0] += (int) getTextWidth() / 2 + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[0] -= bounds.width() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[0] -= (bounds.width() + getCompoundDrawablePadding()) / 2;
                    }
                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.BOTTOM: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] += (int) getTextHeight() / 2 + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[1] -= bounds.height() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[1] -= (bounds.height() + getCompoundDrawablePadding()) / 2;
                    }
                    break;
                }
            }
            return offset;
        }

        /**
         * 绘制
         * <p>
         * 步骤:
         * 将画布平移到LoadingDrawable的位置 -> 裁剪出一个圆形画布（由小到大）-> 在裁剪后的绘制图形
         * ->随animValue值画布逐渐变大，实现出现的效果
         */
        private void draw(Canvas canvas) {
            if (getAnimValue() > 0 && mLoadingDrawable != null) {
                final Bitmap targetBitMap = isFail ? mFailBitmap : mCompleteBitmap;
                if (targetBitMap != null) {
                    final Rect bounds = mLoadingDrawable.getBounds();
                    mBounds.right = bounds.width();
                    mBounds.bottom = bounds.height();

                    final int[] offsets = calcOffset(canvas, mBounds, mLoadingPosition);
                    canvas.save();
                    canvas.translate(offsets[0], offsets[1]);
                    mCirclePath.reset();
                    mCirclePath.addCircle(mBounds.centerX(), mBounds.centerY(),
                            ((getLoadingEndDrawableSize() >> 1) * 1.5f) * animValue, Path.Direction.CW);
                    canvas.clipPath(mCirclePath);
                    canvas.drawBitmap(targetBitMap, null, mBounds, mPaint);
                    canvas.restore();
                }
            }
        }

        private void setAnimValue(float animValue) {
            this.animValue = animValue;
            invalidate();
        }

        private float getAnimValue() {
            return animValue;
        }

        /**
         * EndDrawable的Animator
         *
         * @return ObjectAnimator
         */
        public ObjectAnimator getAppearAnimator() {
            return mAppearAnimator;
        }

        /**
         * EndDrawable的停留时间
         *
         * @param keepDuration millionMs
         */
        public void setKeepDuration(long keepDuration) {
            this.mKeepDuration = keepDuration;
        }


        public void setCompleteDrawable(Drawable drawable) {
            mCompleteBitmap = getBitmap(drawable);
        }

        public void setCompleteDrawable(@DrawableRes int id) {
            if (id != -1) {
                Drawable drawable = ContextCompat.getDrawable(getContext(), id);
                mCompleteBitmap = getBitmap(drawable);
            }
        }

        public void setFailDrawable(@DrawableRes int id) {
            if (id != -1) {
                Drawable failDrawable = ContextCompat.getDrawable(getContext(), id);
                mFailBitmap = getBitmap(failDrawable);
            }
        }

        public void setFailDrawable(Drawable drawable) {
            mCompleteBitmap = getBitmap(drawable);
        }

    }

    @Nullable
    private Bitmap getBitmap(Drawable drawable) {
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }


    @Override
    protected void onDetachedFromWindow() {
        //release
        mShrinkAnimator.cancel();
        mLoadingDrawable.stop();
        if (mEndDrawable != null)
            mEndDrawable.mAppearAnimator.cancel();

        super.onDetachedFromWindow();

    }

    public interface OnLoadingListener {
        void onLoadingStart();

        void onLoadingStop();

        void onShrinking();

        void onRestoring();

        void onEndDrawableAppear(boolean isComplete, EndDrawable endDrawable);

        void onEndDrawableAppearDone(boolean isComplete);

        void onCompleted();

        void onFailed();

        void onCanceled();
    }


    public static class OnLoadingListenerAdapter implements OnLoadingListener {

        @Override
        public void onShrinking() {

        }


        @Override
        public void onLoadingStart() {

        }

        @Override
        public void onLoadingStop() {


        }

        @Override
        public void onEndDrawableAppear(boolean isComplete, EndDrawable endDrawable) {

        }

        @Override
        public void onEndDrawableAppearDone(boolean isComplete) {

        }

        @Override
        public void onRestoring() {

        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onCanceled() {

        }


    }

    public LoadingButton setOnLoadingListener(OnLoadingListener onLoadingListener) {
        mOnLoadingListener = onLoadingListener;
        return this;
    }


}
