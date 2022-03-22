package com.dl.playfun.ui.program.searchprogramsite;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.Injection;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.event.LocationChangeEvent;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.widget.RadioFilterView;
import com.dl.playfun.widget.dropdownfilterpop.DropDownFilterPopupWindow;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentSearchProgramSiteBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.RxBus;

/**
 * @author wulei
 */
public class SearchProgramSiteFragment extends BaseRefreshFragment<FragmentSearchProgramSiteBinding, SearchProgramSiteViewModel> implements DropDownFilterPopupWindow.OnItemClickListener, PopupWindow.OnDismissListener {
    public static final String ARG_THEME_ENTITY = "arg_theme_item_entity";

    private FusedLocationProviderClient fusedLocationClient;

    private Location location;

    private ThemeItemEntity themeItemEntity;

    private DropDownFilterPopupWindow regionPop;
    private List<RadioFilterView.RadioFilterItemEntity> regions;
    private String keyword = null;

    public static Bundle getStartBundle(ThemeItemEntity themeItemEntity) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_THEME_ENTITY, themeItemEntity);
        return bundle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImmersionBarUtils.setupStatusBar(this, true, false);
        return view;
    }

    @Override
    public void initParam() {
        super.initParam();
        themeItemEntity = getArguments().getParcelable(ARG_THEME_ENTITY);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_search_program_site;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public SearchProgramSiteViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        SearchProgramSiteViewModel searchProgramSiteViewModel = ViewModelProviders.of(this, factory).get(SearchProgramSiteViewModel.class);
        if (themeItemEntity != null) {
            searchProgramSiteViewModel.setThemeItemEntity(themeItemEntity);
        }
        if (keyword != null) {
            searchProgramSiteViewModel.setKeyword(keyword);
        }
        return searchProgramSiteViewModel;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.clickCity.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                if (regionPop != null) {
                    regionPop.showAsDropDown(binding.cbRegion);
                }
            }
        });
        viewModel.uc.clickResult.observe(this, new Observer() {
            @Override
            public void onChanged(Object position) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(IssuanceProgramFragment.ARG_CHOOSE_CITY, viewModel.chooseCity.get());
                bundle.putString(IssuanceProgramFragment.ARG_ADDRESS_NAME, viewModel.observableList.get((Integer) position).name.get());
                bundle.putString(IssuanceProgramFragment.ARG_ADDRESS, viewModel.observableList.get((Integer) position).address.get());
                if (viewModel.observableList.get((Integer) position).lat.get() != null) {
                    bundle.putDouble(IssuanceProgramFragment.ARG_ADDRESS_LAT, viewModel.observableList.get((Integer) position).lat.get());
                }
                if (viewModel.observableList.get((Integer) position).lng.get() != null) {
                    bundle.putDouble(IssuanceProgramFragment.ARG_ADDRESS_LNG, viewModel.observableList.get((Integer) position).lng.get());
                }
                setFragmentResult(1, bundle);
                viewModel.pop();
            }
        });
    }

    @Override
    public void initData() {
        binding.refreshLayout.setEnableRefresh(false);
        List<ConfigItemEntity> citys = Injection.provideDemoRepository().readCityConfig();
        if (citys != null && !citys.isEmpty()) {
            viewModel.chooseCity.set(citys.get(0));
        }
        regions = new ArrayList<>();
        for (ConfigItemEntity city : citys) {
            regions.add(new RadioFilterView.RadioFilterItemEntity<>(city.getName(), city));
        }
        regionPop = new DropDownFilterPopupWindow(mActivity, regions);
        regionPop.setOnItemClickListener(this);
        regionPop.setOnDismissListener(this);
        try {
            new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) {
                            startLocation();
                        } else {
                            // At least one permission is denied
//                        showDialog("请打开定位");
                            viewModel.loadPlaceByKeyword();
                        }
                    });
        } catch (Exception e) {

        }
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
//        new RxPermissions(this)
//                .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
//                .subscribe(granted -> {
//                    if (granted) {
//                        startLocation();
//                    } else {
//                        // At least one permission is denied
////                        showDialog("请打开定位");
//                        viewModel.loadPlaceByKeyword();
//                    }
//                });
    }

    @SuppressLint("MissingPermission")
    private void startLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setNumUpdates(1);
//        mLocationRequest.setInterval(5000); // two minute interval
//        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        RxBus.getDefault().post(new LocationChangeEvent(LocationChangeEvent.LOCATION_STATUS_START));
        fusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    location = locationList.get(locationList.size() - 1);
                    viewModel.lat = location.getLatitude();
                    viewModel.lng = location.getLongitude();
                } else {
                    viewModel.lat = null;
                    viewModel.lng = null;
                }
                viewModel.loadPlaceByKeyword();
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (!locationAvailability.isLocationAvailable()) {
                    viewModel.lat = null;
                    viewModel.lng = null;
                    viewModel.loadPlaceByKeyword();
                }
            }
        }, Looper.myLooper());
    }

    @Override
    public void onDismiss() {
        binding.cbRegion.setChecked(false);
    }

    @Override
    public void onItemClick(DropDownFilterPopupWindow popupWindow, int position) {
        if (popupWindow == regionPop) {
            popupWindow.dismiss();
            RadioFilterView.RadioFilterItemEntity name = regions.get(position);
            viewModel.chooseCity.set(((ConfigItemEntity) name.getData()));
        }
    }
}
