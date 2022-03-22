package com.dl.playfun.ui.program.searchprogramsite;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.GooglePoiBean;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.ui.message.searchaddress.SearchAddressItemViewModel;
import com.dl.playfun.ui.message.searchaddress.SearchAddressViewModel;
import com.dl.playfun.ui.program.adapter.ProgramSiteItemViewModel;
import com.dl.playfun.ui.program.base.BaseProgramSiteViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.R;
import com.dl.playfun.utils.ApiUitl;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.observers.DisposableObserver;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;

/**
 * @author wulei
 */
public class SearchProgramSiteViewModel extends BaseProgramSiteViewModel {

    public Double lat, lng;
    public BindingRecyclerViewAdapter<ProgramSiteItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableField<ConfigItemEntity> chooseCity = new ObservableField<>();
    public ObservableField<String> searchText = new ObservableField<>();
    public ObservableField<TextView.OnEditorActionListener> onEditorActionListener = new ObservableField<>();
    //取消按钮的点击事件
    public BindingCommand cancelOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            pop();
        }
    });
    UIChangeObservable uc = new UIChangeObservable();
    //城市按钮的点击事件
    public BindingCommand cityOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            uc.clickCity.call();
        }
    });
    private ThemeItemEntity themeItemEntity;
    private String nextPageToken = null;
    private String keyword = null;
    private final TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
            }
            return false;
        }
    };

    public SearchProgramSiteViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        onEditorActionListener.set(editorActionListener);
    }

    public void setThemeItemEntity(ThemeItemEntity themeItemEntity) {
        this.themeItemEntity = themeItemEntity;
    }

    private void search() {
        keyword = searchText.get();
        if (TextUtils.isEmpty(keyword)) {
            ToastUtils.showShort(R.string.playfun_search_hint);
            return;
        }
        hideKeyboard();
        nextPageToken = null;
        loadPlaceByKeyword();
    }

    @Override
    public void loadDatas(int page) {
        if (page == 1) {
            nextPageToken = null;
        }
        loadPlaceByKeyword();
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void onItemClick(int position) {
        if (themeItemEntity != null) {
            ProgramSiteItemViewModel itemViewModel = observableList.get(position);
            Bundle bundle = IssuanceProgramFragment.getStartBundle(themeItemEntity, chooseCity.get(), itemViewModel.name.get(), itemViewModel.address.get(), itemViewModel.lat.get(), itemViewModel.lng.get());
            startWithPop(IssuanceProgramFragment.class.getCanonicalName(), bundle);
        } else {
            uc.clickResult.setValue(position);
        }
    }

    public void loadPlaceByKeyword() {
        if (keyword == null && themeItemEntity != null) {
            if (StringUtils.isEmpty(themeItemEntity.getKeyWord())) {
                ProgramSiteItemViewModel itemPending = new ProgramSiteItemViewModel(SearchProgramSiteViewModel.this, StringUtils.getString(R.string.playfun_determined), null, null, null);
                observableList.add(itemPending);
                return;
            }
            keyword = themeItemEntity.getKeyWord();
        }
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("query",keyword);
        mapData.put("location",(Double.valueOf(lat) == null) ? null : String.format("%s,%s", lat, lng));
        mapData.put("radius",50000);
        mapData.put("language",null);
        mapData.put("pagetoken",nextPageToken);
        mapData.put("region",null);
        mapData.put("type",null);
        mapData.put("key",null);
        model.textSearchPlace(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> {
                    if (nextPageToken == null) {
                        showHUD();
                    }
                })
                .subscribe(new BaseObserver<BaseDataResponse<GooglePoiBean>>() {

                    @Override
                    public void onError(Throwable e) {
                        if (nextPageToken == null) {
                            observableList.clear();
                            ProgramSiteItemViewModel itemPending = new ProgramSiteItemViewModel(SearchProgramSiteViewModel.this, StringUtils.getString(R.string.playfun_determined), null, null, null);
                            observableList.add(itemPending);
                        }
                        ToastUtils.showShort(R.string.playfun_google_map_error);
                        dismissHUD();
                        stopRefreshOrLoadMore();
                    }

                    @Override
                    public void onSuccess(BaseDataResponse<GooglePoiBean> googlePoiBeanBaseDataResponse) {
                        if (nextPageToken == null) {
                            observableList.clear();
                            ProgramSiteItemViewModel itemPending = new ProgramSiteItemViewModel(SearchProgramSiteViewModel.this, StringUtils.getString(R.string.playfun_determined), null, null, null);
                            observableList.add(itemPending);
                        }
                        if(googlePoiBeanBaseDataResponse.getData()!=null){
                            GooglePoiBean googlePoiBean = googlePoiBeanBaseDataResponse.getData();
                            nextPageToken = googlePoiBean.getNext_page_token();
                            if ("OK".equals(googlePoiBean.getStatus())) {
                                for (GooglePoiBean.ResultsBean result : googlePoiBean.getResults()) {
                                    ProgramSiteItemViewModel item = new ProgramSiteItemViewModel(SearchProgramSiteViewModel.this, result.getName(), result.getFormatted_address(), result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng());
                                    observableList.add(item);
                                }
                            }
                        }
                    }
                    @Override
                    public void onComplete() {
                        dismissHUD();
                        stopRefreshOrLoadMore();
                    }

                });
    }

    public class UIChangeObservable {
        public SingleLiveEvent<Void> clickCity = new SingleLiveEvent<>();
        public SingleLiveEvent clickResult = new SingleLiveEvent<>();
    }
}