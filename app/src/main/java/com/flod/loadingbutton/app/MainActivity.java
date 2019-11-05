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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.flod.loadingbutton.DrawableTextView;
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

    private Switch swEnableShrink, swDisableClickOnLoading;
    private TextView tvShrinkDuration;
    private TextView tvLoadingDrawableColor;
    private TextView tvLoadingPosition;
    private ImageView imEndCompleteDrawableIcon;
    private ImageView imEndFailDrawableIcon;
    private TextView tvEndDrawableDuration;
    private TextView tvLoadingEndDrawableSize;
    private TextView tvLoadingStrokeWidth;
    private TextView tvLoadingText;
    private TextView tvCompleteText;
    private TextView tvFailText;


    private int itemIndexSelected;
    private int seekBarProgress;
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
                        resetView();
                        Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);

    }

    private void initView() {
        loadingBtn = findViewById(R.id.loadingBtn);
        swEnableShrink = findViewById(R.id.swEnableShrink);
        swDisableClickOnLoading = findViewById(R.id.swDisableOnLoading);

        tvShrinkDuration = findViewById(R.id.tvShrinkDuration);
        tvLoadingDrawableColor = findViewById(R.id.tvLoadingDrawableColor);
        tvLoadingPosition = findViewById(R.id.tvLoadingPosition);
        imEndCompleteDrawableIcon = findViewById(R.id.imEndCompleteDrawableIcon);
        imEndFailDrawableIcon = findViewById(R.id.imEndFailDrawableIcon);
        tvEndDrawableDuration = findViewById(R.id.tvEndDrawableDuration);
        tvLoadingEndDrawableSize = findViewById(R.id.tvLoadingEndDrawableSize);
        tvLoadingStrokeWidth = findViewById(R.id.tvLoadingStrokeWidth);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        tvCompleteText = findViewById(R.id.tvCompleteText);
        tvFailText = findViewById(R.id.tvFailText);
        btCancel = findViewById(R.id.btCancel);
        btFail = findViewById(R.id.btFail);
        btComplete = findViewById(R.id.btComplete);

        swEnableShrink.setOnClickListener(this);
        tvShrinkDuration.setOnClickListener(this);
        tvLoadingDrawableColor.setOnClickListener(this);
        tvLoadingPosition.setOnClickListener(this);
        findViewById(R.id.layEndCompleteDrawableIcon).setOnClickListener(this);
        findViewById(R.id.layEndFailDrawableIcon).setOnClickListener(this);
        tvEndDrawableDuration.setOnClickListener(this);
        tvLoadingEndDrawableSize.setOnClickListener(this);
        tvLoadingStrokeWidth.setOnClickListener(this);
        tvLoadingText.setOnClickListener(this);
        tvCompleteText.setOnClickListener(this);
        tvFailText.setOnClickListener(this);
        btCancel.setOnClickListener(this);
        btFail.setOnClickListener(this);
        btComplete.setOnClickListener(this);

        initLoadingButton();

        swEnableShrink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadingBtn.cancel();
                loadingBtn.setEnableShrink(isChecked);
                tvLoadingStrokeWidth.setText("LoadingSize * 0.14");
                if (isChecked) {
                    int loadingSize = (int) loadingBtn.getTextSize() * 2;
                    loadingBtn.setLoadingEndDrawableSize(loadingSize);
                    loadingBtn.getLoadingDrawable().setStrokeWidth(loadingSize * 0.14f);
                    tvLoadingEndDrawableSize.setText("TextSize * 2");


                } else {
                    int loadingSize = (int) loadingBtn.getTextSize();
                    loadingBtn.setLoadingEndDrawableSize(loadingSize);
                    loadingBtn.getLoadingDrawable().setStrokeWidth(loadingSize * 0.14f);
                    tvLoadingEndDrawableSize.setText("TextSize");
                }
            }
        });

        swDisableClickOnLoading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadingBtn.setDisableClickOnLoading(isChecked);
            }
        });

    }

    private void resetView() {
        loadingText = "Loading";
        completeText = "Success";
        failText = "Fail";
        swEnableShrink.setChecked(true);
        swDisableClickOnLoading.setChecked(true);
        tvShrinkDuration.setText("500ms");
        tvLoadingDrawableColor.setText("TextColor");
        tvLoadingDrawableColor.setBackground(null);
        tvLoadingPosition.setText("START");
        imEndCompleteDrawableIcon.setImageResource(R.drawable.ic_successful);
        imEndFailDrawableIcon.setImageResource(R.drawable.ic_fail);
        tvEndDrawableDuration.setText("1500ms");
        tvLoadingEndDrawableSize.setText("TextSize * 2");
        tvLoadingStrokeWidth.setText("TextSize * 0.12");
        tvLoadingText.setText(loadingText);
        tvCompleteText.setText(completeText);
        tvFailText.setText(failText);


        initLoadingButton();
    }

    private void initLoadingButton() {
        loadingBtn.setOnClickListener(this);
        loadingBtn.cancel();
        loadingBtn.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        loadingBtn.setEnableShrink(true)
                .setDisableClickOnLoading(true)
                .setShrinkDuration(450)
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
                        Log.d("LoadingButton","onCanceled");
                        Toast.makeText(getApplicationContext(), "onCanceled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                        Log.d("LoadingButton","onFailed");
                        Toast.makeText(getApplicationContext(), "onFailed", Toast.LENGTH_SHORT).show();

                        loadingBtn.setText("Submit");
                    }

                    @Override
                    public void onCompleted() {
                        Log.d("LoadingButton","onCompleted");
                        Toast.makeText(getApplicationContext(), "onCompleted", Toast.LENGTH_SHORT).show();
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
                        Log.d("LoadingButton","onEndDrawableAppear");
                        if (isSuccess) {
                            loadingBtn.setText(completeText);
                        } else {
                            loadingBtn.setText(failText);
                        }
                    }
                });

    }


    @Override
    public void onClick(View v) {
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
                loadingBtn.fail();
                return;
            }
            case R.id.btComplete: {
                loadingBtn.complete();
                return;
            }
        }


        loadingBtn.cancel();
        switch (id) {
            case R.id.tvShrinkDuration: {
                showSeekBarDialog("SetShrinkDuration", 3000, loadingBtn.getShrinkDuration(), false,
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
                showSeekBarDialog("SetLoadingDrawableColor", 0xffffff, loadingBtn.getLoadingDrawable().getColorSchemeColors()[0] - 0xff000000, true,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvLoadingDrawableColor.setText("\t\t\t\t");
                                int color = 0xff000000 + seekBarProgress;
                                tvLoadingDrawableColor.setBackgroundColor(color);
                                loadingBtn.getLoadingDrawable().setColorSchemeColors(color);
                                seekBarProgress = 0;
                            }
                        });
                break;
            }
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
                            .countable(false)
                            .capture(true)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.5f)
                            .imageEngine(new Glide4Engine())
                            .forResult(RQ_GET_PHOTO_FAIL);
                }
                break;
            }
            case R.id.tvEndDrawableDuration: {
                if (loadingBtn.getEndDrawableAnimator() != null) {
                    showSeekBarDialog("SetEndDrawableAppearTime", 6500, (int) loadingBtn.getEndDrawableDuration(), false,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tvEndDrawableDuration.setText(seekBarProgress + "ms");
                                    loadingBtn.setEndDrawableKeepDuration(seekBarProgress);
                                    seekBarProgress = 0;
                                }
                            });
                } else {
                    Toast.makeText(this, "EndDrawable is null", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.tvLoadingEndDrawableSize: {
                showSeekBarDialog("SetLoadingEndDrawableSize", 250, loadingBtn.getLoadingEndDrawableSize(), false,
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
            case R.id.tvLoadingStrokeWidth: {
                showSeekBarDialog("SetLoadingStrokeWidth", 30, (int) loadingBtn.getLoadingDrawable().getStrokeWidth(), false,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tvLoadingStrokeWidth.setText(seekBarProgress + "px");
                                loadingBtn.getLoadingDrawable().setStrokeWidth(seekBarProgress);
                                seekBarProgress = 0;
                            }
                        });
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

    private void showSeekBarDialog(String title, int max, final int value, final boolean setColor, DialogInterface.OnClickListener onConfirmClickListener) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_seek_bar, null);
        final TextView tvValue = view.findViewById(R.id.tvValue);
        final SeekBar seekBar = view.findViewById(R.id.seek_bar);
        seekBar.setMax(max);

        if (setColor) {
            tvValue.setBackgroundColor(value + 0xff000000);
            tvValue.setText(Integer.toHexString(value));
            seekBar.setProgress(value);

        } else {
            seekBar.setProgress(value);
            tvValue.setText(value + "");
        }

        seekBarProgress = value;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (setColor) {
                    tvValue.setText(Integer.toHexString(progress));
                    tvValue.setBackgroundColor(progress + 0xff000000);
                } else {
                    tvValue.setText(progress + "");
                }

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

    @SuppressLint("InflateParams")
    private void showEditDialog(String title, DialogInterface.OnClickListener onConfirmClickListener) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
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
}
