package com.dl.playfun.ui.certification.verifysuccess;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.entity.TaskRewardReceiveEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseToolbarFragment;
import com.dl.playfun.ui.mine.wallet.girl.TwDollarMoneyFragment;
import com.dl.playfun.widget.dialog.TraceDialog;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentFaceVerifySuccessBinding;

/**
 * @author wulei
 */
public class FaceVerifySuccessFragment extends BaseToolbarFragment<FragmentFaceVerifySuccessBinding, FaceVerifySuccessViewModel> {

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_face_verify_success;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public FaceVerifySuccessViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(FaceVerifySuccessViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        if (basicToolbar != null) {
            basicToolbar.hiddenBack(true);
        }
        //真人认证提示
        if (ConfigManager.getInstance().isCertification() && !ConfigManager.getInstance().isMale()) {

            viewModel.ToaskSubBonus();
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.realPenson.observe(this, new Observer<TaskRewardReceiveEntity>() {
            @Override
            public void onChanged(TaskRewardReceiveEntity taskRewardReceiveEntity) {
                if(taskRewardReceiveEntity!=null){
                    TraceDialog.getInstance(getContext())
                            .setConfirmOnlick(new TraceDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(Dialog dialog) {
                                    AppContext.instance().logEvent(AppsFlyerEvent.task_auth_toWithdrawal);
                                    dialog.dismiss();
                                    viewModel.start(TwDollarMoneyFragment.class.getCanonicalName());
                                }
                            })
                            .setCannelOnclick(new TraceDialog.CannelOnclick() {
                                @Override
                                public void cannel(Dialog dialog) {
                                    dialog.dismiss();
                                }
                            })
                            .AlertTaskMoney(getContext().getDrawable(R.drawable.completed),taskRewardReceiveEntity.getTaskType(),taskRewardReceiveEntity.getTaskName(),taskRewardReceiveEntity.getMsg()).show();
                }
            }
        });
    }

    @Override
    public boolean onBackPressedSupport() {
        return true;
    }

}
