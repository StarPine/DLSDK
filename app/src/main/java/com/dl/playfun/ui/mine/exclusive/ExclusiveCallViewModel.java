package com.dl.playfun.ui.mine.exclusive;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.ExclusiveAccostInfoEntity;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * 修改备注：我的专属招呼viewmodel
 *
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/8/11 20:55
 */
public class ExclusiveCallViewModel extends BaseViewModel<AppRepository> {
    //回传事件
    public SingleLiveEvent<Void> editText = new SingleLiveEvent<>();
    public SingleLiveEvent<Void> editAudio = new SingleLiveEvent<>();

    public final int TEXT_TYPE = 1;
    public final int AUDIO_TYPE = 2;
    public ObservableField<List<ExclusiveAccostInfoEntity>> accostInfoEntity = new ObservableField<>();
    public ObservableField<String> textContent = new ObservableField<>();
    public ObservableField<String> audioContent = new ObservableField<>();
    public ObservableField<List<String>> sensitiveWords = new ObservableField<>();
    public int textTypeId;
    public int audioTypeId;

    /**
     * 删除文本搭讪语
     */
    public BindingCommand delTextAccostOnClick = new BindingCommand(() -> delExclusiveAccost(TEXT_TYPE));

    /**
     * 删除语音搭讪语
     */
    public BindingCommand delAudioAccostOnClick = new BindingCommand(() -> delExclusiveAccost(AUDIO_TYPE));

    /**
     * 编辑文本搭讪语
     */
    public BindingCommand editTextAccostOnClick = new BindingCommand(() -> {
        editText.call();
    });

    /**
     * 编辑语音搭讪语
     */
    public BindingCommand editAudioAccostOnClick = new BindingCommand(() -> {
        editAudio.call();
    });


    public ExclusiveCallViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
        sensitiveWords.set(model.readSensitiveWords());
    }

    public void getExclusiveAccost() {
        model.getExclusiveAccost()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<List<ExclusiveAccostInfoEntity>>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<List<ExclusiveAccostInfoEntity>> response) {
                        List<ExclusiveAccostInfoEntity> data = response.getData();

                        if (data != null) {
                            for (ExclusiveAccostInfoEntity accostInfo : data) {
                                int type = accostInfo.getType();
                                if (type == TEXT_TYPE) {
                                    textContent.set(accostInfo.getContent());
                                    textTypeId = accostInfo.getId();
                                } else if (type == AUDIO_TYPE) {
                                    audioContent.set(accostInfo.getContent());
                                    audioTypeId = accostInfo.getId();
                                }
                            }
                        }

                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

    public void delExclusiveAccost(int type) {
        model.delExclusiveAccost(type)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse>() {
                    @Override
                    public void onSuccess(BaseDataResponse response) {
                        if (type == TEXT_TYPE) {
                            textContent.set(null);
                        } else if (type == AUDIO_TYPE) {
                            audioContent.set(null);
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

    public void setExclusiveAccost(Integer type, String content) {
        model.setExclusiveAccost(type, content)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse>() {
                    @Override
                    public void onSuccess(BaseDataResponse response) {
                        textContent.set(content);
                        ToastCenterUtils.showShort(R.string.playfun_text_accost_tips3);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

}
