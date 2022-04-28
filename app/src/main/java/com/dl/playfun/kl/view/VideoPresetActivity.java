package com.dl.playfun.kl.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.svideo.common.utils.FastClickUtil;
import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.event.CallChatingHangupEvent;
import com.dl.playfun.manager.LocaleManager;
import com.tencent.liteav.trtccalling.model.TRTCCalling;
import com.tencent.liteav.trtccalling.model.TUICalling;

import me.goldze.mvvmhabit.bus.RxBus;

/**
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/3/29 17:13
 * 修改备注：
 */
public class VideoPresetActivity extends AppCompatActivity {

    private RelativeLayout video_preset_container;
    private LinearLayout jm_line;
    private JMTUICallVideoView mCallView;
    private TUICalling.Role role;
    protected TRTCCalling mTRTCCalling;

    private String[] userIds =new String[1];
    private SeekBar seekbar_one;
    private SeekBar seekbar_two;
    private SeekBar seekbar_three;
    private TextView text_one,text_two,text_three;
    private int whitenessProgress;
    private int beautyProgress;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocal(newBase));
    }

    /**
     * 就算你在Manifest.xml设置横竖屏切换不重走生命周期。横竖屏切换还是会走这里

     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig!=null){
            LocaleManager.setLocal(this);
        }
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocal(this);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        LocaleManager.setLocal(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_perset);
        mTRTCCalling = TRTCCalling.sharedInstance(this);
        video_preset_container = findViewById(R.id.video_preset_container);
        jm_line = findViewById(R.id.jm_line);
        role = TUICalling.Role.CALL;
        userIds[0] ="preset";
        mCallView = new JMTUICallVideoView(this, role, userIds, null, null, false) {
            @Override
            public void finish() {
                super.finish();
                //2秒最多发一次
            }
        };
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        video_preset_container.addView(mCallView, params);
        jm_line.bringToFront();
        initView();
    }

    private void initView() {
        seekbar_one = findViewById(R.id.seekbar_one);
        seekbar_two = findViewById(R.id.seekbar_two);
        seekbar_three = findViewById(R.id.seekbar_three);
        text_one = findViewById(R.id.text_one);
        text_two = findViewById(R.id.text_two);
        text_three = findViewById(R.id.text_three);
        seekbar_one.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTRTCCalling.presetWhitenessLevel(progress);
                text_one.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                seekbar_one.setProgress(whitenessProgress);
            }
        });
        seekbar_two.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTRTCCalling.presetRuddyLevel(progress);
                text_two.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                seekbar_two.setProgress(beautyProgress);
            }
        });
        seekbar_three.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTRTCCalling.presetBeautyLevel(progress);
                text_three.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
