package com.dl.playfun.ui;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.tencent.liteav.trtccalling.model.impl.TUICallingManager;
import com.tencent.qcloud.tuicore.util.ConfigManagerUtil;

/**
 * Author: 彭石林
 * Time: 2022/1/17 11:51
 * Description: This is PlayFunFloatImgView
 */
public class PlayFunFloatImgView extends androidx.appcompat.widget.AppCompatImageView implements View.OnClickListener{
    private Context mContext;
    public PlayFunFloatImgView(Context context) {
        super(context);
        mContext = context;
        setImageResource(R.drawable.my_float_main_mark);
        setOnClickListener(this);
    }

    public PlayFunFloatImgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setImageResource(R.drawable.my_float_main_mark);
        setOnClickListener(this);
        mContext = context;
    }

    public PlayFunFloatImgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImageResource(R.drawable.my_float_main_mark);
        setOnClickListener(this);
        mContext = context;
    }

    @Override
    public void onClick(View view){
        //是否在游戏中
        ConfigManagerUtil.getInstance().putPlayGameFlag(false);
        //游戏中
        AppContext.instance().setGameState(-1);
        Intent intent = new Intent(mContext, MainContainerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}
