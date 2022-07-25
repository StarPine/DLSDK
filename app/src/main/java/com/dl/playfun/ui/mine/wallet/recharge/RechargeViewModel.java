package com.dl.playfun.ui.mine.wallet.recharge;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.NoteInfoEntity;
import com.dl.playfun.ui.mine.wallet.coin.CoinFragment;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/6/15 18:23
 * 修改备注：
 */
public class RechargeViewModel extends BaseViewModel<AppRepository> {

    public ObservableField<Boolean> isGooglepay = new ObservableField<>(false);

    /**
     * 选择水晶支付
     */
    public BindingCommand crystalPayOnClick = new BindingCommand(() -> {
        isGooglepay.set(false);
    });

    /**
     * 选择Google支付
     */
    public BindingCommand googlePayOnClick = new BindingCommand(() -> {
        isGooglepay.set(true);
    });

    /**
     * 确认支付
     */
    public BindingCommand confirmPayOnClick = new BindingCommand(() -> {

    });

    public RechargeViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }

    public void putNoteText(int userId, String note) {
        model.putNoteText(userId, note)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<NoteInfoEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<NoteInfoEntity> response) {
                        Toast.makeText(AppContext.instance(), R.string.save_success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }
}
