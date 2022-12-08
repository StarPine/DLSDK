package com.dl.playfun.ui.message.chatdetail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.dl.playfun.widget.BasicToolbar;

import me.goldze.mvvmhabit.utils.ConvertUtils;

/**
 * @author Shuotao Gong
 * @time 2022/12/7
 */
public class ChatDetailTopBar extends BasicToolbar {

    public ChatDetailTopBar(Context context) {
        super(context);
    }

    public ChatDetailTopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatDetailTopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTitleColor(int color) {
        if (this.tvTitle == null) {
            return;
        }
        this.tvTitle.setTextColor(color);
    }

    public void setTitleTag(@DrawableRes int id) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), id);
        if (drawable == null) return;
        drawable.setBounds(0,  0, ConvertUtils.dp2px(15), ConvertUtils.dp2px(15));
        tvTitle.setCompoundDrawables(null, null, drawable, null);
        tvTitle.setCompoundDrawablePadding(ConvertUtils.dp2px(2));
    }
}
