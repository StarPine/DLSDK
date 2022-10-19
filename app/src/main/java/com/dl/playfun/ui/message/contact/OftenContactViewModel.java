package com.dl.playfun.ui.message.contact;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.ObjectUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.FrequentContactEntity;
import com.dl.playfun.entity.IMTransUserEntity;
import com.dl.playfun.entity.ImUserSigEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.event.LoginExpiredEvent;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.ToastCenterUtils;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.tencent.qcloud.tuicore.TUILogin;
import com.tencent.qcloud.tuicore.interfaces.TUICallback;

import java.util.Objects;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.goldze.mvvmhabit.utils.Utils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * Author: 彭石林
 * Time: 2022/8/11 17:36
 * Description: This is OftenContactViewModel
 */
public class OftenContactViewModel extends BaseViewModel<AppRepository> {

    public BindingRecyclerViewAdapter<ItemOftenContactViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<ItemOftenContactViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<ItemOftenContactViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_contact_park);

    public ObservableBoolean emptyRcvView = new ObservableBoolean(false);

    //跳转进入私聊页面
    public SingleLiveEvent<Integer> startChatUserView = new SingleLiveEvent<>();
    public SingleLiveEvent<Void> loginSuccess = new SingleLiveEvent<>();

    public UIChangeObservable uc = new UIChangeObservable();

    public OftenContactViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

    public BindingCommand<Void> nextPageData = new BindingCommand<>(this::getFrequentContact);

    public void flushSign() {
        model.flushSign()
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<ImUserSigEntity>>() {

                    @Override
                    public void onSuccess(BaseDataResponse<ImUserSigEntity> response) {
                        ImUserSigEntity data = response.getData();
                        TokenEntity tokenEntity = model.readLoginInfo();
                        if (data == null || TextUtils.isEmpty(data.getUserSig()) || tokenEntity == null){
                            RxBus.getDefault().post(new LoginExpiredEvent());
                            return;
                        }
                        tokenEntity.setUserSig(data.getUserSig());
                        TUILogin.login(Utils.getContext(), model.readApiConfigManagerEntity().getImAppId(), tokenEntity.getUserID(), tokenEntity.getUserSig(), new TUICallback() {
                            @Override
                            public void onSuccess() {
                                model.saveLoginInfo(tokenEntity);
                                loginSuccess.call();
                            }

                            @Override
                            public void onError(int errorCode, String errorMessage) {
                                RxBus.getDefault().post(new LoginExpiredEvent());
                            }
                        });
                    }

                    @Override
                    public void onError(RequestException e) {
                        RxBus.getDefault().post(new LoginExpiredEvent());
                    }

                    @Override
                    public void onComplete() {
                    }

                });
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
                            userMessageCollation(imTransUserEntity.getUserId(), userDetailView);
                        }else {
                            dismissHUD();
                        }
                    }
                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        dismissHUD();
                    }
                });
    }

    public void userMessageCollation(int userId, boolean userDetailView) {
        model.userMessageCollation(userId)
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        if(userDetailView){//去往用户主页
                            Bundle bundle = UserDetailFragment.getStartBundle(userId);
                            start(UserDetailFragment.class.getCanonicalName(), bundle);
                        }else{//进入私聊页面
                            startChatUserView.postValue(userId);
                        }
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

    /**
    * @Desc TODO(获取常联系推荐)
    * @author 彭石林
    * @parame []
    * @return void
    * @Date 2022/10/17
    */
    public void getFrequentContact(){
        model.getFrequentContact()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(dispose -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<FrequentContactEntity>>(){

                    @Override
                    public void onSuccess(BaseDataResponse<FrequentContactEntity> dataResponse) {
                            FrequentContactEntity frequentContact = dataResponse.getData();
                            if(ObjectUtils.isNotEmpty(frequentContact) && ObjectUtils.isNotEmpty(frequentContact.getUserList())){
                                observableList.clear();
                                for (FrequentContactEntity.ItemEntity itemEntity : frequentContact.getUserList()){
                                    ItemOftenContactViewModel itemOftenContactViewModel = new ItemOftenContactViewModel(OftenContactViewModel.this,itemEntity);
                                    observableList.add(itemOftenContactViewModel);
                                }
                                if(!observableList.isEmpty()){
                                    emptyRcvView.set(false);
                                }
                            }
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        //if(observableList.isEmpty()){
                            emptyRcvView.set(true);
                        //}
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

    //搭讪
    public void putAccostFirst(int position) {
        FrequentContactEntity.ItemEntity parkItemEntity = observableList.get(position).itemEntity.get();
        model.putAccostFirst(parkItemEntity.getUserProfile().getId())
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        dismissHUD();
                        ToastUtils.showShort(R.string.playfun_text_accost_success1);
                        parkItemEntity.setIsAccost(1);
                        Objects.requireNonNull(adapter.getAdapterItem(position).itemEntity.get()).setIsAccost(1);
                        adapter.getAdapterItem(position).accountCollect.set(true);
                        adapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        if(e.getCode()!=null && e.getCode() ==21001 ){//钻石余额不足
                            ToastCenterUtils.showToast(R.string.playfun_dialog_exchange_integral_total_text3);
                            uc.sendAccostFirstError.call();
                        }
                        parkItemEntity.setIsAccost(0);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

    public static class UIChangeObservable {
        //搭讪失败。充值钻石
        public SingleLiveEvent<Void> sendAccostFirstError = new SingleLiveEvent<>();
    }
}
