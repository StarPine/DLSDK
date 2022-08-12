package com.dl.playfun.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuichat.component.AudioPlayer;

import java.io.File;

/**
 * 修改备注：
 *
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/8/12 11:26
 */
public class ExclusiveAccostDialog{
    private static volatile ExclusiveAccostDialog INSTANCE;
    private Context mContext;
    private ExclusiveAccostDialog.DialogOnClickListener onClickListener;
    private int startTime = 0;
    private CountDownTimer downTimer;
    private String playPath = null;
    private int playgDuration = 0;
    private boolean deleteFlag = false;

    public static ExclusiveAccostDialog getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ExclusiveAccostDialog.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExclusiveAccostDialog(context);
                }
            }
        } else {
            init(context);
        }
        return INSTANCE;
    }

    private static void init(Context context) {
        INSTANCE.mContext = context;
    }

    private ExclusiveAccostDialog(Context context) {
        this.mContext = context;
    }

    public ExclusiveAccostDialog setOnClickListener(ExclusiveAccostDialog.DialogOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return INSTANCE;
    }

    /**
     * 编辑搭讪文本内容dialog
     *
     * @return
     */
    public Dialog editAccostContentDialog(String content) {
        Dialog dialog = new Dialog(mContext, R.style.BottomDialog);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_text_accost, null);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        Button confirmBtn = contentView.findViewById(R.id.btn_confirm);
        TextView wordCount = contentView.findViewById(R.id.tv_word_count);
        EditText edAccostText = contentView.findViewById(R.id.et_accost);
        ImageView close = contentView.findViewById(R.id.iv_close);
        if (!TextUtils.isEmpty(content)){
            edAccostText.setText(content);
            wordCount.setText(String.format(mContext.getString(R.string.playfun_text_accost_number_of_fonts),edAccostText.getText().length()+""));
        }else {
            wordCount.setText(String.format(mContext.getString(R.string.playfun_text_accost_number_of_fonts),"0"));

        }
        close.setOnClickListener(v -> dialog.dismiss());
        edAccostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wordCount.setText(String.format(mContext.getString(R.string.playfun_text_accost_number_of_fonts),edAccostText.getText().length()+""));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmBtn.setOnClickListener(v -> {
            if (onClickListener != null){
                onClickListener.onConfirm(dialog,edAccostText.getText().toString());
            }
        });
        return dialog;
    }

    /**
     * 编辑录音搭讪内容dialog
     * @param content
     * @return
     */
    @SuppressLint("ClickableViewAccessibility")
    public Dialog editAccostAudioDialog(String content) {
        Dialog dialog = new Dialog(mContext, R.style.BottomDialog);
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_audio_accost, null);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        contentView.setLayoutParams(layoutParams);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        Button recording = contentView.findViewById(R.id.btn_recording);
        TextView timing = contentView.findViewById(R.id.tv_timing);
        ImageView close = contentView.findViewById(R.id.iv_close);
        ImageView audioNomal = contentView.findViewById(R.id.iv_audio_nomal);
        ImageView audioPlayable = contentView.findViewById(R.id.iv_audio_playable);
        ImageView ivReset = contentView.findViewById(R.id.iv_reset);
        ImageView ivOk = contentView.findViewById(R.id.iv_ok);
        LinearLayout llCompletiion = contentView.findViewById(R.id.ll_completion);

        close.setOnClickListener(v -> dialog.dismiss());
        recording.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    audioNomal.setImageDrawable(mContext.getDrawable(R.drawable.icon_anim_audio_recording));
                    recording.setBackgroundResource(R.drawable.button_purple_background2);
                    recording.setText(mContext.getString(R.string.playfun_audio_accost_recording));
                    startTime = 0;
                    startAudioCall(timing);
                    setTimeText(timing);
                    startAudio(timing);
                    deleteFlag = false;
                    break;
                case MotionEvent.ACTION_UP:
                    stopAudioCall();
                    if (startTime < 3) {
                        ToastUtil.toastShortMessage(StringUtils.getString(R.string.playfun_tape_audio_error_text));
                        startTime = 0;
                        deleteFlag = true;
                        resetStatus(recording, timing, audioNomal, llCompletiion);
                    }else {
                        audioPlayable.setImageDrawable(mContext.getDrawable(R.drawable.icon_stop_audio));
                        llCompletiion.setVisibility(View.VISIBLE);
                        recording.setVisibility(View.GONE);
                        audioPlayable.setVisibility(View.VISIBLE);
                    }
                    stopAudio();
                    break;
            }
            return true;
        });
        ivOk.setOnClickListener(v -> {
            if (onClickListener != null){
                onClickListener.onConfirmAudio(dialog,playPath,startTime);
            }
        });
        audioPlayable.setOnClickListener(v -> {
            if (AudioPlayer.getInstance().isPlaying()) {
                audioPlayable.setImageResource(R.drawable.icon_stop_audio);
                AudioPlayer.getInstance().stopPlay();
                return;
            } else {
                audioPlayable.setImageResource(R.drawable.icon_playing_audio);
            }
            AudioPlayer.getInstance().startPlay(playPath, new AudioPlayer.Callback() {
                @Override
                public void onCompletion(Boolean success, Boolean isOutTime) {
                    audioPlayable.setImageResource(R.drawable.icon_stop_audio);
                }
            });
        });
        ivReset.setOnClickListener(v -> {
            resetStatus(recording, timing, audioNomal, llCompletiion);
        });

        return dialog;
    }

    private void resetStatus(Button recording, TextView timing, ImageView audioNomal, LinearLayout llCompletiion) {
        audioNomal.setImageDrawable(mContext.getDrawable(R.drawable.icon_audio_nomal));
        timing.setText(mContext.getString(R.string.playfun_audio_accost_tip));
        recording.setBackgroundResource(R.drawable.button_purple_background);
        recording.setText(mContext.getString(R.string.playfun_audio_accost_long_click));
        llCompletiion.setVisibility(View.GONE);
        recording.setVisibility(View.VISIBLE);
        audioNomal.setVisibility(View.VISIBLE);
    }

    private void setTimeText(TextView timing) {
        timing.setText(String.format("00:%02d", startTime));
    }

    public void startAudio(TextView timing){
        /**
         * 倒计时15秒，一次1秒
         */
        downTimer = new CountDownTimer(15 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                startTime ++;
                setTimeText(timing);
            }

            @Override
            public void onFinish() {
                stopAudioCall();
                stopAudio();
            }
        };
        downTimer.start();
    }

    public void stopAudio(){
        if(downTimer!=null){
            downTimer.cancel();
        }
    }

    public void startAudioCall(TextView timing){
        AudioPlayer.getInstance().startRecord(new AudioPlayer.Callback() {
            @Override
            public void onCompletion(Boolean success, Boolean isOutTime) {
                if(deleteFlag){
                    try {
                        File deleteFile = new File(AudioPlayer.getInstance().getPath());
                        deleteFile.delete();
                    }catch(Exception e){

                    }
                }else{
                    if(success){
                        playPath = AudioPlayer.getInstance().getPath();
                        playgDuration = AudioPlayer.getInstance().getDuration();
                        setTimeText(timing);
                    }
                }
            }
        });
    }

    public void stopAudioCall(){
        AudioPlayer.getInstance().stopRecord();
    }

    public interface DialogOnClickListener {

        default void close() {
        }

        default void onConfirm(Dialog dialog, String content) {

        }

        default void onConfirmAudio(Dialog dialog, String content,int second) {

        }

    }
}
