package com.flod.hardloadingbutton;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
import com.flod.loadingbutton.LoadingButton;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RQ_MULTIPLE_PERMISSIONS = 200;
    private LoadingButton loadingBtn;
    private Button start;
    private Button cancel;
    private Button end;

    private Switch swEnableShrink;
    private TextView tvShrinkDuration;
    private TextView tvLoadingDrawableColor;
    private TextView tvLoadingPosition;
    private ImageView imEndDrawableIcon;
    private TextView tvEndDrawableAppearTime;
    private TextView tvEndDrawableDuration;
    private TextView tvLoadingEndDrawableSize;
    private TextView tvLoadingText;
    private TextView tvLoadingCompleteText;


    private int itemIndexSelected;
    private int seekBarProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadingBtn.setOnLoadingListener(new LoadingButton.OnLoadingListenerAdapter() {
            @Override
            public void onLoadingStart() {
                Log.d("LoadingListener", "onLoadingStart");
            }

            @Override
            public void onLoadingEnd() {
                Log.d("LoadingListener", "onLoadingEnd");

            }

            @Override
            public void onShrinkStart(boolean isShrink) {
                Log.d("LoadingListener", "onShrinkStart isReverse = " + isShrink);
            }

            @Override
            public void onShrinkEnd(boolean isShrink) {
                Log.d("LoadingListener", "onShrinkEnd isReverse = " + isShrink);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingBtn.start();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingBtn.cancel(true);
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingBtn.complete();
            }
        });


        swEnableShrink.setChecked(true);
        swEnableShrink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadingBtn.setEnableShrink(isChecked);
            }
        });

    }


    private void initView() {
        loadingBtn = findViewById(R.id.loadingBtn);
        swEnableShrink = findViewById(R.id.swEnableShrink);
        tvShrinkDuration = findViewById(R.id.tvShrinkDuration);
        tvLoadingDrawableColor = findViewById(R.id.tvLoadingDrawableColor);
        tvLoadingPosition = findViewById(R.id.tvLoadingPosition);
        imEndDrawableIcon = findViewById(R.id.imEndDrawableIcon);
        tvEndDrawableAppearTime = findViewById(R.id.tvEndDrawableAppearTime);
        tvEndDrawableDuration = findViewById(R.id.tvEndDrawableDuration);
        tvLoadingEndDrawableSize = findViewById(R.id.tvLoadingEndDrawableSize);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        tvLoadingCompleteText = findViewById(R.id.tvLoadingCompleteText);
        start = findViewById(R.id.start);
        cancel = findViewById(R.id.cancel);
        end = findViewById(R.id.end);

        swEnableShrink.setOnClickListener(this);
        tvShrinkDuration.setOnClickListener(this);
        tvLoadingDrawableColor.setOnClickListener(this);
        tvLoadingPosition.setOnClickListener(this);
        imEndDrawableIcon.setOnClickListener(this);
        tvEndDrawableAppearTime.setOnClickListener(this);
        tvEndDrawableDuration.setOnClickListener(this);
        tvLoadingEndDrawableSize.setOnClickListener(this);
        tvLoadingText.setOnClickListener(this);
        tvLoadingCompleteText.setOnClickListener(this);
        start.setOnClickListener(this);
        cancel.setOnClickListener(this);
        end.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.swEnableShrink: {
                break;
            }
            case R.id.tvShrinkDuration: {
                showSeekBarDialog("SetShrinkDuration", 3000, loadingBtn.getShrinkDuration(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvShrinkDuration.setText(seekBarProgress + "ms");
                                loadingBtn.setShrinkDuration(seekBarProgress);
                                seekBarProgress = 0;
                            }
                        });
                break;
            }
            case R.id.tvLoadingDrawableColor: {
                break;
            }
            case R.id.tvLoadingPosition: {
                final List<String> items = Arrays.asList("START", "TOP", "END", "BOTTOM");
                final int curIndex = items.indexOf(tvLoadingPosition.getText().toString());

                showSelectDialog("LoadingPosition", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingBtn.setLoadingPosition(itemIndexSelected);
                        tvLoadingPosition.setText(items.get(itemIndexSelected));
                        itemIndexSelected = 0;
                    }
                }, curIndex, items.toArray(new String[0]));


                break;
            }
            case R.id.imEndDrawableIcon: {
                if (!requestPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Matisse.from(MainActivity.this)
                            .choose(MimeType.ofImage())
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.5f)
                            .imageEngine(new Glide4Engine())
                            .forResult(10);
                }
                break;
            }
            case R.id.tvEndDrawableAppearTime: {
                if (loadingBtn.getEndDrawableAnimator() != null) {
                    showSeekBarDialog("SetEndDrawableAppearTime", 1500, (int) loadingBtn.getEndDrawableAnimator().getDuration(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tvEndDrawableAppearTime.setText(seekBarProgress + "ms");
                                    loadingBtn.setEndDrawableAppearTime(seekBarProgress);
                                    seekBarProgress = 0;
                                }
                            });
                } else {
                    Toast.makeText(this, "EndDrawable is null", Toast.LENGTH_LONG).show();
                }

                break;
            }
            case R.id.tvEndDrawableDuration: {
                if (loadingBtn.getEndDrawableAnimator() != null) {
                    showSeekBarDialog("SetEndDrawableAppearTime", 6500, (int) loadingBtn.getEndDrawableDuration(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tvEndDrawableDuration.setText(seekBarProgress + "ms");
                                    loadingBtn.setEndDrawableDuration(seekBarProgress);
                                    seekBarProgress = 0;
                                }
                            });
                } else {
                    Toast.makeText(this, "EndDrawable is null", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.tvLoadingEndDrawableSize: {
                showSeekBarDialog("SetLoadingEndDrawableSize", 250, loadingBtn.getLoadingDrawableSize(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvLoadingEndDrawableSize.setText(seekBarProgress + "px");
                                loadingBtn.setLoadingEndDrawableSize(seekBarProgress);
                                seekBarProgress = 0;
                            }
                        });

                break;

            }
            case R.id.tvLoadingText: {
                break;
            }
            case R.id.tvLoadingCompleteText: {
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

    private void showSeekBarDialog(String title, int max, final int value, DialogInterface.OnClickListener onConfirmClickListener) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_seek_bar, null);
        final TextView tvValue = view.findViewById(R.id.tvValue);
        final SeekBar seekBar = view.findViewById(R.id.seek_bar);
        seekBar.setMax(max);
        seekBar.setProgress(value);
        tvValue.setText(value + "");
        seekBarProgress = value;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvValue.setText(progress + "");
                seekBarProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view)
                .setTitle(title)
                .setNegativeButton("Confirm", onConfirmClickListener)
                .create().show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            List<Uri> mSelected = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + mSelected);
            Glide.with(this)
                    .asDrawable()
                    .load(mSelected.get(0))
                    .into(imEndDrawableIcon);

            loadingBtn.setEndDrawable(imEndDrawableIcon.getDrawable());
        }
    }

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
}
