package com.flod.loadingbutton.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.flod.drawabletextview.DrawableTextView;
import com.flod.loadingbutton.LoadingButton;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RQ_MULTIPLE_PERMISSIONS = 200;
    private static final int RQ_GET_PHOTO_COMPLETE = 10;
    private static final int RQ_GET_PHOTO_FAIL = 11;
    private LoadingButton loadingBtn;
    private Button btCancel;
    private Button btFail;
    private Button btComplete;

    private TextView tvLoadingPosition;
    private ImageView imEndCompleteDrawableIcon;
    private ImageView imEndFailDrawableIcon;

    private TextView tvLoadingText;
    private TextView tvCompleteText;
    private TextView tvFailText;


    private int itemIndexSelected;
    private String editTextString;


    private String loadingText = "Loading";
    private String completeText = "Success";
    private String failText = "Fail";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Reset")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        initView();
                        Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);

    }

    private void initView() {
        loadingBtn = findViewById(R.id.loadingBtn);

        initLoadingButton();

        Switch swEnableShrink = findViewById(R.id.swEnableShrink);
        Switch swEnableRestore = findViewById(R.id.swEnableRestore);
        Switch swDisableClickOnLoading = findViewById(R.id.swDisableOnLoading);
        final TextView tvRadiusValue = findViewById(R.id.tvRadiusValue);
        SeekBar sbRadius = findViewById(R.id.sbRadius);
        RadioGroup rgShrinkShape = findViewById(R.id.rgShrinkShape);
        final TextView tvLoadingDrawableColorValue = findViewById(R.id.tvLoadingDrawableColorValue);
        SeekBar sbLoadingDrawableColor = findViewById(R.id.sbLoadingDrawableColor);
        final TextView tvLoadingStrokeWidthValue = findViewById(R.id.tvLoadingStrokeWidthValue);
        final TextView tvShrinkDurationValue = findViewById(R.id.tvShrinkDurationValue);
        SeekBar sbShrinkDuration = findViewById(R.id.sbShrinkDuration);
        SeekBar sbLoadingStrokeWidth = findViewById(R.id.sbLoadingStrokeWidth);
        tvLoadingStrokeWidthValue.setText(loadingBtn.getLoadingDrawable().getStrokeWidth() + "");
        final TextView tvLoadingEndDrawableSizeValue = findViewById(R.id.tvLoadingEndDrawableSizeValue);
        SeekBar sbLoadingEndDrawableSizeValue = findViewById(R.id.sbLoadingEndDrawableSizeValue);
        final TextView tvEndDrawableDurationValue = findViewById(R.id.tvEndDrawableDurationValue);
        SeekBar sbEndDrawableDuration = findViewById(R.id.sbEndDrawableDuration);

        swEnableShrink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadingBtn.cancel();
                loadingBtn.setEnableShrink(isChecked);
                int defaultStrokeWidth = (int) (loadingBtn.getTextSize() * 0.14f);
                tvLoadingStrokeWidthValue.setText(defaultStrokeWidth + "");
                if (isChecked) {
                    int loadingSize = (int) loadingBtn.getTextSize() * 2;
                    loadingBtn.setLoadingEndDrawableSize(loadingSize);
                    loadingBtn.getLoadingDrawable().setStrokeWidth(defaultStrokeWidth);
                    tvLoadingEndDrawableSizeValue.setText(loadingSize + "");


                } else {
                    int loadingSize = (int) loadingBtn.getTextSize();
                    loadingBtn.setLoadingEndDrawableSize(loadingSize);
                    loadingBtn.getLoadingDrawable().setStrokeWidth(loadingSize * 0.14f);
                    tvLoadingEndDrawableSizeValue.setText(loadingSize + "");
                }
            }
        });

        swEnableRestore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadingBtn.cancel();
                loadingBtn.setEnableRestore(isChecked);
            }
        });

        swDisableClickOnLoading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadingBtn.setDisableClickOnLoading(isChecked);
            }
        });


        tvRadiusValue.setText(35 + "");
        sbRadius.setMax(100);
        sbRadius.setProgress(35);
        sbRadius.setOnSeekBarChangeListener(new EmptyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    loadingBtn.setRadius(progress);
                }
                tvRadiusValue.setText(String.valueOf(progress));
            }
        });


        rgShrinkShape.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (checkedId == R.id.rdDefault) {
                        loadingBtn.setShrinkShape(LoadingButton.ShrinkShape.DEFAULT);

                    } else if (checkedId == R.id.rdOval) {
                        loadingBtn.setShrinkShape(LoadingButton.ShrinkShape.OVAL);
                    }
                }
            }
        });


        tvShrinkDurationValue.setText(String.valueOf(500));
        sbShrinkDuration.setMax(3000);
        sbShrinkDuration.setProgress(500);
        sbShrinkDuration.setOnSeekBarChangeListener(new EmptyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                loadingBtn.setShrinkDuration(progress);
                tvShrinkDurationValue.setText(String.valueOf(progress));
            }
        });


        int loadingDrawableColorValue = loadingBtn.getLoadingDrawable().getColorSchemeColors()[0];
        tvLoadingDrawableColorValue.setText(Integer.toHexString(loadingDrawableColorValue));
        tvLoadingDrawableColorValue.setBackgroundColor(loadingDrawableColorValue);
        sbLoadingDrawableColor.setMax(0xffffff);
        sbLoadingDrawableColor.setProgress(loadingDrawableColorValue - 0xff000000);
        sbLoadingDrawableColor.setOnSeekBarChangeListener(new EmptyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                loadingBtn.getLoadingDrawable().setColorSchemeColors(progress);
                tvLoadingDrawableColorValue.setText(Integer.toHexString(progress + 0xff000000));
                tvLoadingDrawableColorValue.setBackgroundColor(progress + 0xff000000);
            }
        });


        sbLoadingStrokeWidth.setMax(30);
        sbLoadingStrokeWidth.setProgress((int) loadingBtn.getLoadingDrawable().getStrokeWidth());
        sbLoadingStrokeWidth.setOnSeekBarChangeListener(new EmptyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                loadingBtn.getLoadingDrawable().setStrokeWidth(progress);
                tvLoadingStrokeWidthValue.setText(String.valueOf(progress));
            }
        });


        tvLoadingEndDrawableSizeValue.setText(loadingBtn.getLoadingEndDrawableSize() + "");
        sbLoadingEndDrawableSizeValue.setMax(250);
        sbLoadingEndDrawableSizeValue.setProgress(loadingBtn.getLoadingEndDrawableSize());
        sbLoadingEndDrawableSizeValue.setOnSeekBarChangeListener(new EmptyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                loadingBtn.setLoadingEndDrawableSize(progress);
                tvLoadingEndDrawableSizeValue.setText(String.valueOf(progress));
            }
        });


        tvEndDrawableDurationValue.setText(loadingBtn.getEndDrawableDuration() + "");
        sbEndDrawableDuration.setMax(6500);
        sbEndDrawableDuration.setProgress((int) loadingBtn.getEndDrawableDuration());
        sbEndDrawableDuration.setOnSeekBarChangeListener(new EmptyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                loadingBtn.setEndDrawableKeepDuration(progress);
                tvEndDrawableDurationValue.setText(String.valueOf(progress));
            }
        });


        tvLoadingPosition = findViewById(R.id.tvLoadingPosition);
        imEndCompleteDrawableIcon = findViewById(R.id.imEndCompleteDrawableIcon);
        imEndFailDrawableIcon = findViewById(R.id.imEndFailDrawableIcon);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        tvCompleteText = findViewById(R.id.tvCompleteText);
        tvFailText = findViewById(R.id.tvFailText);
        btCancel = findViewById(R.id.btCancel);
        btFail = findViewById(R.id.btFail);
        btComplete = findViewById(R.id.btComplete);

        tvLoadingPosition.setOnClickListener(this);
        findViewById(R.id.layEndCompleteDrawableIcon).setOnClickListener(this);
        findViewById(R.id.layEndFailDrawableIcon).setOnClickListener(this);

        tvLoadingText.setOnClickListener(this);
        tvCompleteText.setOnClickListener(this);
        tvFailText.setOnClickListener(this);
        btCancel.setOnClickListener(this);
        btFail.setOnClickListener(this);
        btComplete.setOnClickListener(this);

        tvLoadingText.setText("Loading");
        tvCompleteText.setText("Success");
        tvFailText.setText("Fail");
        imEndCompleteDrawableIcon.setImageResource(R.drawable.ic_successful);
        imEndFailDrawableIcon.setImageResource(R.drawable.ic_fail);


    }


    private void initLoadingButton() {
        loadingBtn.setOnClickListener(this);
        loadingBtn.cancel();
        loadingBtn.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        loadingBtn.getLoadingDrawable().setStrokeWidth(loadingBtn.getTextSize() * 0.14f);
        loadingBtn.setEnableShrink(true)
                .setDisableClickOnLoading(true)
                .setShrinkDuration(450)
                .setLoadingPosition(DrawableTextView.POSITION.START)
                .setCompleteDrawable(R.drawable.ic_successful)
                .setFailDrawable(R.drawable.ic_fail)
                .setEndDrawableKeepDuration(900)
                .setEnableRestore(true)
                .setLoadingEndDrawableSize((int) (loadingBtn.getTextSize() * 2))
                .setOnLoadingListener(new LoadingButton.OnLoadingListenerAdapter() {

                    @Override
                    public void onShrinking() {
                        Log.d("LoadingButton", "onShrinking");
                    }

                    @Override
                    public void onLoadingStart() {
                        Log.d("LoadingButton","onLoadingStart");
                        loadingBtn.setText(loadingText);
                    }

                    @Override
                    public void onLoadingStop() {
                        Log.d("LoadingButton","onLoadingStop");
                    }

                    @Override
                    public void onEndDrawableAppear(boolean isSuccess, LoadingButton.EndDrawable endDrawable) {
                        Log.d("LoadingButton", "onEndDrawableAppear");
                        if (isSuccess) {
                            loadingBtn.setText(completeText);
                        } else {
                            loadingBtn.setText(failText);
                        }
                    }


                    @Override
                    public void onCompleted(boolean isSuccess) {
                        Log.d("LoadingButton", "onCompleted isSuccess: " + isSuccess);
                        Toast.makeText(getApplicationContext(), isSuccess ? "Success" : "Fail", Toast.LENGTH_SHORT).show();

                    }


                    @Override
                    public void onRestored() {
                        Log.d("LoadingButton", "onRestored");

                    }

                    @Override
                    public void onCanceled() {
                        Log.d("LoadingButton", "onCanceled");
                        Toast.makeText(getApplicationContext(), "onCanceled", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public void onClick(final View v) {
        int id = v.getId();
        switch (id) {
            case R.id.loadingBtn:
                loadingBtn.start();
                return;
            case R.id.btCancel: {
                loadingBtn.cancel();
                return;
            }
            case R.id.btFail: {
                loadingBtn.complete(false);
                return;
            }
            case R.id.btComplete: {
                loadingBtn.complete(true);
                return;
            }
        }


        loadingBtn.cancel();
        switch (id) {

            case R.id.tvLoadingPosition: {
                final List<String> items = Arrays.asList("START", "TOP", "END", "BOTTOM");
                final int curIndex = items.indexOf(tvLoadingPosition.getText().toString());

                showSelectDialog("LoadingPosition", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingBtn.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        loadingBtn.setLoadingPosition(itemIndexSelected);
                        tvLoadingPosition.setText(items.get(itemIndexSelected));
                        itemIndexSelected = 0;
                    }
                }, curIndex, items.toArray(new String[0]));


                break;
            }
            case R.id.layEndCompleteDrawableIcon: {
                if (!requestPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Matisse.from(MainActivity.this)
                            .choose(MimeType.ofImage())
                            .countable(false)
                            .capture(true)
                            .theme(R.style.Matisse_Dracula)
                            .captureStrategy(new CaptureStrategy(true, "com.flod.hardloadingbutton.provider"))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.5f)
                            .imageEngine(new Glide4Engine())
                            .forResult(RQ_GET_PHOTO_COMPLETE);
                }
                break;
            }
            case R.id.layEndFailDrawableIcon: {
                if (!requestPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Matisse.from(MainActivity.this)
                            .choose(MimeType.ofImage())
                            .theme(R.style.Matisse_Dracula)
                            .countable(false)
                            .capture(true)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.5f)
                            .imageEngine(new Glide4Engine())
                            .forResult(RQ_GET_PHOTO_FAIL);
                }
                break;
            }
            case R.id.tvLoadingText: {
                showEditDialog("SetLoadingText", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvLoadingText.setText(editTextString);
                        loadingText = editTextString;
                        editTextString = "";
                    }
                });
                break;
            }
            case R.id.tvCompleteText: {
                showEditDialog("SetCompleteText", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvCompleteText.setText(editTextString);
                        completeText = editTextString;
                        editTextString = "";
                    }
                });
                break;
            }
            case R.id.tvFailText: {
                showEditDialog("SetFailText", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvFailText.setText(editTextString);
                        failText = editTextString;
                        editTextString = "";
                    }
                });
                break;
            }
        }
    }

    private void showSelectDialog(String title, DialogInterface.OnClickListener onConfirmClickListener,
                                  int checkIndex, String... items) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setSingleChoiceItems(items, checkIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemIndexSelected = which;
                    }
                })
                .setNegativeButton("Confirm", onConfirmClickListener);
        builder.create().show();
    }

    private void showEditDialog(String title, DialogInterface.OnClickListener onConfirmClickListener) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = view.findViewById(R.id.et);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextString = s.toString();
            }
        });

        builder.setTitle(title)
                .setView(view)
                .setNegativeButton("Confirm", onConfirmClickListener);
        builder.create().show();
    }


    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == RQ_GET_PHOTO_COMPLETE || requestCode == RQ_GET_PHOTO_FAIL) {
                final ImageView targetImageView = requestCode == RQ_GET_PHOTO_COMPLETE ? imEndCompleteDrawableIcon : imEndFailDrawableIcon;
                List<Uri> mSelected = Matisse.obtainResult(data);
                Log.d("Matisse", "mSelected: " + mSelected);
                Glide.with(this)
                        .asDrawable()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                if (requestCode == RQ_GET_PHOTO_COMPLETE)
                                    loadingBtn.setCompleteDrawable(resource);
                                else
                                    loadingBtn.setFailDrawable(resource);
                                return false;
                            }
                        })
                        .load(mSelected.get(0))
                        .into(targetImageView);

            }
        }

    }

    @SuppressWarnings({"VariableArgumentMethod", "BooleanMethodIsAlwaysInverted"})
    public static boolean requestPermissions(Activity context, String... permissions) {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    context.requestPermissions(permissions, RQ_MULTIPLE_PERMISSIONS);
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RQ_MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                Toast.makeText(this, "Please allow Permissions", Toast.LENGTH_LONG).show();
            }
        }
    }


    static class EmptyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
