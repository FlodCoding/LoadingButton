# LoadingButton [![LoadingButton](https://jitpack.io/v/FlodCoding/LoadingButton.svg)](https://jitpack.io/#FlodCoding/LoadingButton)

 一个小巧灵活的带加载功能的按钮控件，继承自[DrawableTextView](https://github.com/FlodCoding/DrawableTextView)，加载动画来自于[CircularProgressDrawable](https://developer.android.google.cn/reference/android/support/v4/widget/CircularProgressDrawable?hl=en)

## 特性
   * 支持按钮收缩
   * 支持加载完成和失败图标显示
   * 可设置加载动画颜色、大小、位置
   
## 如何导入

根目录下的build.gradle

	allprojects {
		  repositories {
		  	...
		  maven { url 'https://jitpack.io' }
		  }
	}
 
 
 App目录下的build.gradle 
 
 	dependencies {
		//Androidx
		implementation 'com.github.FlodCoding:LoadingButton:1.0.4'
		
		//Support-appcompat
		implementation 'com.github.FlodCoding:LoadingButton:1.0.4-support'
     	}
  
 
## Demo
[点我下载](https://github.com/FlodCoding/LoadingButton/raw/master/app/build/outputs/apk/debug/app-debug.apk)

![](/screenrecord/APK_qrcode.png)

### Demo截图
![](/screenrecord/shrink.gif) &ensp;&ensp; ![](/screenrecord/noshrink.gif)

## 基本用法

### XML
```
<com.flod.loadingbutton.LoadingButton
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@drawable/selector_btn"
            android:drawablePadding="10dp"
            android:gravity="center"
            android:padding="8dp"
            android:text="Submit"
            app:endCompleteDrawable="@drawable/ic_successful"
            app:endFailDrawable="@drawable/ic_fail" />
```
### Code
```

loadingBtn.start();     //开始加载
loadingBtn.complete();  //加载成功
loadingBtn.fail();      //加载失败
loadingBtn.cancel();    //加载取消

loadingBtn.setEnableShrink(true)
                .setDisableClickOnLoading(true)
                .setShrinkDuration(450)
                .setRestoreTextWhenEnd(true)
                .setLoadingColor(loadingBtn.getTextColors().getDefaultColor())
                .setLoadingStrokeWidth((int) (loadingBtn.getTextSize() * 0.14f))
                .setLoadingPosition(DrawableTextView.POSITION.START)
                .setCompleteDrawable(R.drawable.ic_successful)
                .setFailDrawable(R.drawable.ic_fail)
                .setEndDrawableKeepDuration(900)
                .setLoadingEndDrawableSize((int) (loadingBtn.getTextSize() * 2))
                .setOnLoadingListener(new LoadingButton.OnLoadingListenerAdapter() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(getApplicationContext(), "onCanceled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {   
                        Toast.makeText(getApplicationContext(), "onFailed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCompleted() {
                        Toast.makeText(getApplicationContext(), "onCompleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLoadingStart() {
                        loadingBtn.setText("Loading");
                    }

                    @Override
                    public void onEndDrawableAppear(boolean isComplete, LoadingButton.EndDrawable endDrawable) {
                        if (isComplete) {
                            loadingBtn.setText("Complete);
                        } else {
                            loadingBtn.setText("Fail");
                        }
                    }
                });
```

## 属性说明
属性名|类型|说明
---|:--:|---:
enableShrink            |boolean  (default:true)                     |设置加载时收缩
disableClickOnLoading   |boolean (default:true)                      |设置加载时禁用点击
restoreTextWhenEnd      |boolean (default:true)                      |设置加载结束时恢复文字
shrinkDuration          |integer (default:450ms)                     |收缩动画时间
loadingEndDrawableSize  |dimension (default:TextSize \*2)            |设置LaodingDrawable和EndDrawable大小
loadingDrawableColor    |color (default:TextColor)                   |设置Loading的颜色
loadingDrawablePosition |enum：Start,Top,End,Bottom (default:Start)  |设置Loading的位置
endCompleteDrawable     |reference                                   |完成时显示的图标
endFailDrawable         |reference                                   |失败时显示的图标
endDrawableAppearTime   |integer                                     |完成或失败图标从无到有的时间
endDrawableDuration     |integer                                     |完成或失败图标停留的时间

## Demo使用的第三方库

### [Matisse](https://github.com/zhihu/Matisse)

### [Glide](https://github.com/bumptech/glide)
