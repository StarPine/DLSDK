package com.dl.playfun.ui.program.chooseprogramsite;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.GsonUtils;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.GooglePoiBean;
import com.dl.playfun.ui.program.adapter.ProgramSiteItemViewModel;
import com.dl.playfun.ui.program.base.BaseProgramSiteViewModel;
import com.dl.playfun.ui.program.searchprogramsite.SearchProgramSiteFragment;
import com.dl.playfun.utils.ApiUitl;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.RxUtils;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;

/**
 * @author wulei
 */
public class ChooseProgramSiteViewModel extends BaseProgramSiteViewModel {
    public int programId;

    public BindingRecyclerViewAdapter<ProgramSiteItemViewModel> adapter = new BindingRecyclerViewAdapter<>();
    public BindingCommand searchOnClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Bundle bundle = new Bundle();
            bundle.putInt("programId", programId);
//            bundle.putString("address", "address");
            start(SearchProgramSiteFragment.class.getCanonicalName(), bundle);
        }
    });

    public ChooseProgramSiteViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        loadPlaceByKeyword();
    }

    @Override
    public void loadDatas(int page) {

    }

    public void loadPlaceByKeyword() {
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("query", null);
        mapData.put("location", null);
        mapData.put("radius", null);
        mapData.put("language", null);
        mapData.put("pagetoken", null);
        mapData.put("region", null);
        mapData.put("type", null);
        mapData.put("key", null);
        model.textSearchPlace(ApiUitl.getBody(GsonUtils.toJson(mapData)))
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .doOnSubscribe(disposable -> showHUD())
                .subscribe(new BaseObserver<BaseDataResponse<GooglePoiBean>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<GooglePoiBean> googlePoiBeanBaseDataResponse) {
                        observableList.clear();
                        if (googlePoiBeanBaseDataResponse.getData() != null) {
                            GooglePoiBean googlePoiBean = googlePoiBeanBaseDataResponse.getData();
                            if ("OK".equals(googlePoiBean.getStatus())) {
                                for (GooglePoiBean.ResultsBean result : googlePoiBean.getResults()) {
                                    ProgramSiteItemViewModel item = new ProgramSiteItemViewModel(ChooseProgramSiteViewModel.this, result.getName(), result.getFormatted_address(), result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng());
                                    observableList.add(item);
                                }
                            }
                        }

                    }
                });
    }
}