# LoadingButton [![LoadingButton](https://jitpack.io/v/FlodCoding/LoadingButton.svg)](https://jitpack.io/#FlodCoding/LoadingButton)

 一个小巧灵活的带加载功能的按钮控件，继承自[DrawableTextView](https://github.com/FlodCoding/DrawableTextView)，加载动画来自于[CircularProgressDrawable](https://developer.android.google.cn/reference/android/support/v4/widget/CircularProgressDrawable?hl=en)

## 特性
   * 支持按钮收缩
   * 支持加载完成和失败图标显示
   * 可设置加载动画颜色、大小、位置
   * 自定义圆角
   
   
## 如何导入

根目录下的build.gradle
```
	allprojects {
	
		  repositories {
		  	...
		  	maven { url 'https://jitpack.io' }
		  	 
		  }
	}
```

App目录下的build.gradle 
#### 注意！从1.1.0开始与以前的版本有较大的变动,请谨慎升级
``` 
 	dependencies {
		//Androidx
		implementation 'com.github.FlodCoding:LoadingButton:1.1.0-alpha01'
		
	}
```
Support-appcompat 停止更新  
~~implementation 'com.github.FlodCoding:LoadingButton:1.0.5-support'~~



  
 
## Demo [点我下载](https://github.com/FlodCoding/LoadingButton/releases/download/1.1.0-alpha01/demo-1.1.0-alpha01.apk)

![下载.png](https://upload-images.jianshu.io/upload_images/7565394-f96443d70435d4b3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/300)

### Demo截图
![1.gif](https://upload-images.jianshu.io/upload_images/7565394-b799c91e14a8f19a.gif?imageMogr2/auto-orient/strip)
![2.gif](https://upload-images.jianshu.io/upload_images/7565394-018bbbd27694d3b5.gif?imageMogr2/auto-orient/strip)
![3.gif](https://upload-images.jianshu.io/upload_images/7565394-88becf790d21d7fc.gif?imageMogr2/auto-orient/strip)
![4.gif](https://upload-images.jianshu.io/upload_images/7565394-f2ad03c89d715afa.gif?imageMogr2/auto-orient/strip)

## 基本用法

### XML
```
 <com.flod.loadingbutton.LoadingButton
            android:id="@+id/loadingBtn"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:layout_gravity="center_horizontal"
            android:background="@android:color/holo_red_light"
            android:drawablePadding="10dp"
            android:gravity="center"
            android:minHeight="45dp"
            android:padding="8dp"
            android:text="Submit"
            android:textColor="@android:color/white"
            android:textSize="14sp"
            app:endSuccessDrawable="@drawable/ic_successful"
            app:endFailDrawable="@drawable/ic_fail"
            app:radius="50dp"
            app:enableShrink="true"
	        app:enableRestore="true"
            app:shrinkShape="Oval"
            app:loadingDrawablePosition="Start" />
```
### Code
```

loadingBtn.start();              //开始加载
loadingBtn.complete(true);       //加载成功
loadingBtn.complete(false);      //加载失败
loadingBtn.cancel();             //加载取消

loadingBtn.setEnableShrink(true)
	      .setEnableRestore(true)
          .setDisableClickOnLoading(true)
          .setShrinkDuration(450)
          .setLoadingPosition(DrawableTextView.POSITION.START)
          .setSuccessDrawable(R.drawable.ic_successful)
          .setFailDrawable(R.drawable.ic_fail)
          .setEndDrawableKeepDuration(900)
          .setLoadingEndDrawableSize((int) (loadingBtn.getTextSize() * 2));
           
loadingBtn.getLoadingDrawable().setStrokeWidth(loadingBtn.getTextSize() * 0.14f);
loadingBtn.getLoadingDrawable().setColorSchemeColors(loadingBtn.getTextColors().getDefaultColor());
```

### 状态回调
start --> onShrinking --> onLoadingStart  
complete --> onLoadingStop --> onEndDrawableAppear --> onCompleted --> onRestored

```
    public static class OnStatusChangedListener {

        public void onShrinking() {}

        public void onLoadingStart() {}

        public void onLoadingStop() {}

        public void onEndDrawableAppear(boolean isSuccess, EndDrawable endDrawable) {}

        public void onRestoring() {}

        public void onRestored() {}
	
        public void onCompleted(boolean isSuccess) { }

        public void onCanceled() {}
    }
```

## 属性说明
### XML
属性名|类型|默认值|说明
---|:--:|:---:|---:
enableShrink            |boolean    |true                   |开始加载时收缩
disableClickOnLoading   |boolean    |true                   |加载时禁用点击
enableRestore           |boolean    |false                  |完成时，恢复按钮
radius         		|dimension  |0dp                    |设置按钮的圆角,**(需要SDK>=21)** <br>(来自([DrawableTextView](https://github.com/FlodCoding/DrawableTextView))
shrinkDuration          |integer    |450ms                  |收缩动画时间
shrinkShape             |enum<br>(Default,Oval)    |Oval    |收缩后的形状 **(需要SDK>=21)** <br>(Default:保持原来的形状,Oval:圆形)
loadingEndDrawableSize  |dimension  |TextSize \*2           |设置LoadingDrawable和EndDrawable大小
loadingDrawableColor    |reference  |TextColor              |设置Loading的颜色
loadingDrawablePosition |enum<br>(Start,Top,<br>End,Bottom) |Start  |设置Loading的位置
endSuccessDrawable      |reference   | null                 |完成时显示的图标
endFailDrawable         |reference   | null                 |失败时显示的图标
endDrawableAppearTime   |integer     | 300ms                |完成或失败图标从无到有的时间
endDrawableDuration     |integer     | 900ms                |完成或失败图标停留的时间

### Public Func
方法名|参数说明|默认值|说明
---|:--:|:---:|---:
start()                             |-                  |-      |开始加载
complete(boolean isSuccess)         |是否成功           |-      |完成加载
cancel()<br>cancel(boolean withRestoreAnim)     |是否执行恢复动画   |true  |取消
setEnableShrink(boolean enable)     |-                  |true   |设置加载时按钮收缩
setEnableRestore(boolean enable)    |-                  |false  |设置完成时按钮恢复（形状和文字）
setRadius(@Px int px)<br>setRadiusDP(int dp) |Px/Dp    |0    |设置按钮的圆角<br>**(需要SDK>=21)**<br>(来自([DrawableTextView](https://github.com/FlodCoding/DrawableTextView))
setShrinkShape(@ShrinkShape int shrinkShape) |Default:保持原来的形状,<br>Oval:圆形 |Oval  |收缩后的形状<br> **(需要SDK>=21)**
setShrinkDuration(long time) |milliseconds      |450ms  |收缩动画时间
setLoadingEndDrawableSize(@Px int px)  |单位Px  |TextSize \*2   |设置LoadingDrawable和EndDrawable大小
setLoadingPosition(@POSITION int position) |Start,Top,End,Bottom |Start  |设置Loading的位置
setSuccessDrawable(@DrawableRes int drawableRes)<br>setSuccessDrawable(Drawable drawable) |-   | null |成功时显示的图标
setFailDrawable(@DrawableRes int drawableRes)<br>setFailDrawable(Drawable drawable)         |-   | null |失败时显示的图标
setEndDrawableAppearDuration(long time)   |milliseconds     | 300ms                |完成或失败图标从无到有的时间
setEndDrawableKeepDuration(long time)     |milliseconds     | 900ms                |完成或失败图标停留的时间
setOnStatusChangedListener<br>(OnStatusChangedListener listener)|-|null|按钮的各种状态回调

### 常见问题
#### 完成加载时，如何自动恢复到之前按钮的状态？
设置setEnableRestore(true)

#### 当setEnableRestore(false)时，又想某个时机恢复原来的按钮的状态，要怎么做？
执行cancel()


## Demo使用的第三方库

### [Matisse](https://github.com/zhihu/Matisse)

### [Glide](https://github.com/bumptech/glide)
