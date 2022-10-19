package com.dl.playfun.ui.mine.setting.account;

import android.app.Application;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.lib.util.MPDeviceUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.EaringlSwitchUtil;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.exception.RequestException;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.PrivacyEntity;
import com.dl.playfun.entity.UserBindInfoEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.event.IsAuthBindingEvent;
import com.dl.playfun.ui.mine.setting.account.bind.CommunityAccountBindFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.viewmodel.BaseViewModel;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.RxBus;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @ClassName CommunityAccountModel
 * @Description TODO
 * @Author 彭石林
 * @Date 2021/4/29 11:06
 * @Phone 16620350375
 * @email 15616314565@163.com
 * @Version 1.0
 **/
public class CommunityAccountModel extends BaseViewModel<AppRepository> {

    public ObservableField<Boolean> deleteAccountFlag = new ObservableField<>(false);

    public ObservableField<UserBindInfoEntity> userBindInfoEntity = new ObservableField<>();

    public UIChangeObservable UC = new UIChangeObservable();

    public CommunityAccountModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }


    public BindingCommand<Void> toCancellView = new BindingCommand<>(() -> start(CommunityAccountCancellFragment.class.getCanonicalName()));

    public BindingCommand<Void> clickBindPhone = new BindingCommand<>(()->{
        if(userBindInfoEntity.get()!=null){
            if(StringUtils.isEmpty(userBindInfoEntity.get().getPhone())){
                start(CommunityAccountBindFragment.class.getCanonicalName());
            }
        }
    });

    @Override
    public void onEnterAnimationEnd() {
        super.onEnterAnimationEnd();
        getUserBindInfo();
        //注销账号开关
        Integer accountFlag = model.readSwitches(EaringlSwitchUtil.KEY_DELETE_ACCOUNT);
        if (accountFlag != null && accountFlag == 1) {
            deleteAccountFlag.set(true);
        }
    }

    public void getUserBindInfo(){
        model.getUserBindInfo().doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserBindInfoEntity>>(){
                    @Override
                    public void onSuccess(BaseDataResponse<UserBindInfoEntity> response) {
                        userBindInfoEntity.set(response.getData());
                    }


                    @Override
                    public void onError(RequestException e) {
                        super.onError(e);
                        dismissHUD();
                        pop();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        dismissHUD();
                    }
                });
    }

    public void bindAccount(int authType, String id, String type,String business_token) {
        id += type;
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("type",type);
        //当type为phone时，该字段必填，手机号码
        mapData.put("id", id);
        //	当type为phone时，该字段必填，验证码
        mapData.put("business_token", business_token);
        mapData.put("AndroidDeviceInfo", MPDeviceUtils.getDeviceInfo());
        model.bindAccount(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<UserDataEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<UserDataEntity> response) {
                        dismissHUD();
                        UserDataEntity userDataEntity = model.readUserData();
                        userDataEntity.setIsAuth(1);
                        model.saveUserData(userDataEntity);
                        ToastUtils.showShort(R.string.playfun_binding_auth_success);
                        RxBus.getDefault().post(new IsAuthBindingEvent());
                        pop();
                    }

                    @Override
                    public void onComplete() {
                        dismissHUD();
                    }
                });
    }


    public String getPhoneText(String phoneText) {
        if(!StringUtils.isEmpty(phoneText)){
            int phoneTextLen = phoneText.length();
            if(phoneTextLen>5){
                String beforeText = phoneText.substring(0,phoneTextLen-4);
                StringBuilder beforeFormat = new StringBuilder();
                for(int i = 0; i <beforeText.length();i++){
                    beforeFormat.append("*");
                }
                return beforeFormat.append(phoneText.substring(phoneTextLen-4,phoneTextLen)).toString();
            }else{
                return phoneText;
            }

        }
        return null;
    }

    public boolean phoneShow(UserBindInfoEntity userDataEntity){
        if(userDataEntity != null){
            return !StringUtils.isEmpty(userDataEntity.getPhone());
        }
        return false;
    }

    public Integer getIsAuthBindShow(int type,UserBindInfoEntity userBindInfo){
        //绑定第三方
        if(!ObjectUtils.isEmpty(userBindInfo)){
            int isBind = userBindInfo.getBindAuth();
            if(isBind == 0){
                return View.VISIBLE;
            }else{
                if(type == isBind){
                    return View.VISIBLE;
                }else{
                    return View.GONE;
                }
            }
        }
        return View.GONE;
    }


    public class UIChangeObservable {
        SingleLiveEvent<Boolean> loadUserFlag = new SingleLiveEvent<>();
        SingleLiveEvent<Boolean> loginAuth = new SingleLiveEvent<>();
    }
}