package com.dl.playfun.ui.message.contact;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.IMTransUserEntity;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2022/8/11 17:36
 * Description: This is OftenContactViewModel
 */
public class OftenContactViewModel extends BaseViewModel<AppRepository> {

    //跳转进入私聊页面
    public SingleLiveEvent<Integer> startChatUserView = new SingleLiveEvent<>();

    public OftenContactViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    /**
     * @Desc TODO(转换IM用户id)
     * @author 彭石林
     * @parame [ImUserId]
     * @return void
     * @Date 2022/4/2
     */
    public void transUserIM(String ImUserId,boolean userDetailView){
        model.transUserIM(ImUserId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(dispose -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<IMTransUserEntity>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<IMTransUserEntity> response) {
                        IMTransUserEntity  imTransUserEntity = response.getData();
                        if(imTransUserEntity!=null && imTransUserEntity.getUserId()!=null){
                            if(userDetailView){//去往用户主页
                                Bundle bundle = UserDetailFragment.getStartBundle(imTransUserEntity.getUserId());
                                start(UserDetailFragment.class.getCanonicalName(), bundle);
                            }else{//进入私聊页面
                                userMessageCollation(imTransUserEntity.getUserId());
                            }

                        }
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }

    public void userMessageCollation(int userId) {
        model.userMessageCollation(userId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        startChatUserView.postValue(userId);
                    }

                    @Override
                    public void onError(RequestException e) {
                        if (e.getCode() == 11111) { //注销
                            ToastCenterUtils.showShort(R.string.playfun_user_detail_user_disable3);
                        } else {
                            super.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }
}
