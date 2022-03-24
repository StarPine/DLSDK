package com.dl.playfun.ui.message.searchaddress;

import android.app.Application;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.blankj.utilcode.util.GsonUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.GooglePoiBean;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public class SearchAddressViewModel extends BaseRefreshViewModel<AppRepository> {

    public Double lat, lng;
    public BindingRecyclerViewAdapter<SearchAddressItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<SearchAddressItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<SearchAddressItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_search_address);
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

    public SearchAddressViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

        onEditorActionListener.set(editorActionListener);
    }

    private void search() {
        keyword = searchText.get().trim();
        if (TextUtils.isEmpty(keyword)) {
            ToastUtils.showShort(R.string.search_hint);
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

    public void onItemClick(int position) {
        uc.clickItemAddress.postValue(position);
    }

    public void loadPlaceByKeyword() {
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("query", keyword);
        mapData.put("location", (Double.valueOf(lat) == null) ? null : String.format("%s,%s", lat, lng));
        mapData.put("radius", 6000);
        mapData.put("language", null);
        mapData.put("pagetoken", nextPageToken);
        mapData.put("region", null);
        mapData.put("type", null);
        mapData.put("key", null);
        model.textSearchPlace(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(this)
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
                        }
                        ToastUtils.showShort(R.string.google_map_error);
                        dismissHUD();
                        stopRefreshOrLoadMore();
                    }

                    @Override
                    public void onSuccess(BaseDataResponse<GooglePoiBean> googlePoiBeanBaseDataResponse) {
                        if (nextPageToken == null) {
                            observableList.clear();
                        }
                        if (googlePoiBeanBaseDataResponse.getData() != null) {
                            GooglePoiBean googlePoiBean = googlePoiBeanBaseDataResponse.getData();
                            nextPageToken = googlePoiBean.getNext_page_token();
                            if ("OK".equals(googlePoiBean.getStatus())) {
                                for (GooglePoiBean.ResultsBean result : googlePoiBean.getResults()) {
                                    SearchAddressItemViewModel item = new SearchAddressItemViewModel(SearchAddressViewModel.this, result.getName(), result.getFormatted_address(), result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng());
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
        public SingleLiveEvent<Integer> clickItemAddress = new SingleLiveEvent<>();
    }
}