package com.dl.playfun.ui.mine.wallet.recharge;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.ActivityNotepadBinding;
import com.dl.playfun.databinding.ActivityRechargeBinding;
import com.dl.playfun.ui.message.chatdetail.notepad.NotepadViewModel;
import com.dl.playfun.widget.BasicToolbar;

import me.goldze.mvvmhabit.base.BaseActivity;

/**
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/7/25 14:57
 * 修改备注：充值activity
 */
public class RechargeActivity extends BaseActivity<ActivityRechargeBinding, RechargeViewModel> implements BasicToolbar.ToolbarListener{



    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_recharge;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public RechargeViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(RechargeViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();
        Bundle bundle = this.getIntent().getExtras();
    }

    @Override
    public void initData() {
        super.initData();
        binding.tvOriginalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        binding.basicToolbar.setToolbarListener(this);
    }

    @Override
    public void onBackClick(BasicToolbar toolbar) {
        this.finish();
    }
}
