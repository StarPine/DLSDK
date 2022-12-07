package com.dl.playfun.ui.message.chatdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;

import com.blankj.utilcode.util.KeyboardUtils;
import com.dl.playfun.R;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.widget.BasicToolbar;

/**
 * @author Shuotao Gong
 * @time 2022/12/7
 */
public abstract class ChatDetailTopBarFragment <V extends ViewDataBinding, VM extends BaseViewModel> extends BaseFragment<V, VM> implements BasicToolbar.ToolbarListener{

    protected ChatDetailTopBar topBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        topBar = view.findViewById(R.id.chat_detail_toolbar);
        if (topBar != null) {
            topBar.setToolbarListener(this);
        }
        return view;
    }

    @Override
    public void onBackClick(BasicToolbar toolbar) {
        KeyboardUtils.hideSoftInput(mActivity);
        mActivity.onBackPressed();
    }

    /**
     * 设置标题
     *
     * @param title
     */
    protected void setTitleBarTitle(String title) {
        if (topBar != null) {
            topBar.setTitle(title);
        }
    }

    protected void setTitleBarTitleColor(int color) {
        if (topBar != null) {
            topBar.setTitleColor(color);
        }
    }

    protected void setTitleBarTitleTag(@DrawableRes int id) {
        if (topBar != null) {
            topBar.setTitleTag(id);
        }
    }

}
