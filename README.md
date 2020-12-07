# LoadingButton [![LoadingButton](https://jitpack.io/v/FlodCoding/LoadingButton.svg)](https://jitpack.io/#FlodCoding/LoadingButton)

A small and flexible button control with loading function,Extends from [DrawableTextView](https://github.com/FlodCoding/DrawableTextView)，Loading animation comes from [CircularProgressDrawable](https://developer.android.google.cn/reference/android/support/v4/widget/CircularProgressDrawable?hl=en)

## Feature
   * Support button shrink
   * Support loading completion and failure icon
   * Can custom loading drawable color, size, position and loading button shape
   * Custom radius
   
   
## How to install [中文说明](https://github.com/FlodCoding/LoadingButton/blob/master/README_CN.md)

root directory build.gradle
```
	allprojects {
	
		  repositories {
		  	...
		  	maven { url 'https://jitpack.io' }
		  	 
		  }
	}
```

App module build.gradle 

``` 
 	dependencies {
		//Androidx
		implementation 'com.github.FlodCoding:LoadingButton:1.1.0-alpha01'
		
	}
```
Support-appcompat stop update  
~~implementation 'com.github.FlodCoding:LoadingButton:1.0.5-support'~~



  
 
## Demo [Click me to download the apk](https://github.com/FlodCoding/LoadingButton/raw/master/app/build/outputs/apk/debug/app-debug.apk)

![](https://upload-images.jianshu.io/upload_images/7565394-072f52d449ed4a65.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### Demo screenshot
![1.gif](https://upload-images.jianshu.io/upload_images/7565394-b799c91e14a8f19a.gif?imageMogr2/auto-orient/strip)
![2.gif](https://upload-images.jianshu.io/upload_images/7565394-018bbbd27694d3b5.gif?imageMogr2/auto-orient/strip)
![3.gif](https://upload-images.jianshu.io/upload_images/7565394-88becf790d21d7fc.gif?imageMogr2/auto-orient/strip)
![4.gif](https://upload-images.jianshu.io/upload_images/7565394-f2ad03c89d715afa.gif?imageMogr2/auto-orient/strip)

## Basic usage

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

loadingBtn.start();              //Start loading
loadingBtn.complete(true);       //Success
loadingBtn.complete(false);      //failed
loadingBtn.cancel();             //Cancel loading  

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

### State callback
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

## Attribute
### XML
Attribute name|type|Default value|Description
---|:--:|:---:|---:
enableShrink            |boolean    |true                   |Shrink when begin loading
disableClickOnLoading   |boolean    |true                   |Disable click on loading
enableRestore           |boolean    |false                  |When finished, restore button(shape and text)
radius         		|dimension  |0dp                    |Set the rounded corners of the button,**(need SDK>=21)** <br>(from([DrawableTextView](https://github.com/FlodCoding/DrawableTextView))
shrinkDuration          |integer    |450ms                  |Shrink animation duration
shrinkShape             |enum<br>(Default,Oval)    |Oval    |Shape after shrinking **(need SDK>=21)** <br>(Default:Keep the original shape,Oval:Round shape)
loadingEndDrawableSize  |dimension  |TextSize \*2           |Set the size of LoadingDrawable and EndDrawable
loadingDrawableColor    |reference  |TextColor              |Set loading color
loadingDrawablePosition |enum<br>(Start,Top,<br>End,Bottom) |Start  |Set the loading drawable position
endSuccessDrawable      |reference   | null                 |Successful drawable 
endFailDrawable         |reference   | null                 |failed drawable
endDrawableAppearTime   |integer     | 300ms                |Time for completion or failure icon to emerge from nothing
endDrawableDuration     |integer     | 900ms                |endDrawable keeping time

### Public Func
Method name|Parameter description|default value|Description
---|:--:|:---:|---:
start()                             |-                  |-      |Start loading
complete(boolean isSuccess)         |whether succeed           |-      |Complete loading
cancel()<br>cancel(boolean withRestoreAnim)     |Whether to perform restore animation   |true  |Cancel loading
setEnableShrink(boolean enable)     |-                  |true   |Shrink when begin loading
setEnableRestore(boolean enable)    |-                  |false  |When finished, restore button(shape and text)
setRadius(@Px int px)<br>setRadiusDP(int dp) |Px/Dp    |0    |Set the rounded corners of the button,**(need SDK>=21)** <br>(from([DrawableTextView](https://github.com/FlodCoding/DrawableTextView))
setShrinkShape(@ShrinkShape int shrinkShape) |Default:Keep the original shape,Oval:Round shape |Oval  |Shape after shrinking **(need SDK>=21)**
setShrinkDuration(long time) |milliseconds      |450ms  |Shrink animation duration
setLoadingEndDrawableSize(@Px int px)  |Px  |TextSize \*2   |Set the size of LoadingDrawable and EndDrawable
setLoadingPosition(@POSITION int position) |Start,Top,End,Bottom |Start  |Set the loading drawable position
setSuccessDrawable(@DrawableRes int drawableRes)<br>setSuccessDrawable(Drawable drawable) |-   | null |Successful drawable 
setFailDrawable(@DrawableRes int drawableRes)<br>setFailDrawable(Drawable drawable)         |-   | null |failed drawable
setEndDrawableAppearDuration(long time)   |milliseconds     | 300ms                |Time for completion or failure icon to emerge from nothing
setEndDrawableKeepDuration(long time)     |milliseconds     | 900ms                |endDrawable keeping time
setOnStatusChangedListener<br>(OnStatusChangedListener listener)|-|null|State callbacks of buttons


## Third-party libraries used by Demo

### [Matisse](https://github.com/zhihu/Matisse)

### [Glide](https://github.com/bumptech/glide)
