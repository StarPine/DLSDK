package com.dl.playfun.ui.message.chooselocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentChooseLocationBinding;
import com.dl.playfun.ui.base.BaseRefreshToolbarFragment;
import com.dl.playfun.ui.message.searchaddress.SearchAddressFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;
import java.util.Locale;

import me.goldze.mvvmhabit.utils.KLog;
import me.goldze.mvvmhabit.utils.ToastUtils;
import me.yokeyword.fragmentation.ISupportFragment;

/**
 * 选择地图位置
 *
 * @author wulei
 */
@SuppressLint("MissingPermission")
public class ChooseLocationFragment extends BaseRefreshToolbarFragment<FragmentChooseLocationBinding, ChooseLocationViewModel> implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMyLocationButtonClickListener {

    public static final String ARG_ADDRESS_NAME = "arg_address_name";
    public static final String ARG_ADDRESS_LAT = "arg_address_lat";
    public static final String ARG_ADDRESS_LNG = "arg_address_lng";
    public static final String ARG_ADDRESS = "arg_address";

    private GoogleMap mGoogleMap;
    private Boolean granted = false;

    private Location currentLocation;
    private LatLng centerLatLng;
    private boolean touchItem = false;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_choose_location;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ChooseLocationViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(ChooseLocationViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.clickSend.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {

                ChooseLocationItemViewModel chooseLocation = null;
                for (ChooseLocationItemViewModel chooseLocationItemViewModel : viewModel.observableList) {
                    if (chooseLocationItemViewModel.chooseed.get()) {
                        chooseLocation = chooseLocationItemViewModel;
                        break;
                    }
                }
                if (chooseLocation == null) {
                    ToastUtils.showShort(R.string.please_choose_location);
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(ARG_ADDRESS_NAME, chooseLocation.name.get());
                bundle.putString(ARG_ADDRESS, chooseLocation.address.get());
                bundle.putDouble(ARG_ADDRESS_LAT, chooseLocation.lat.get());
                bundle.putDouble(ARG_ADDRESS_LNG, chooseLocation.lng.get());
                setFragmentResult(ISupportFragment.RESULT_OK, bundle);
                pop();
            }
        });
        viewModel.uc.itemChooseed.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                touchItem = true;
                ChooseLocationItemViewModel chooseLocationItemViewModel = viewModel.observableList.get(position);
                moveCamera(chooseLocationItemViewModel.lat.get(), chooseLocationItemViewModel.lng.get(), true);
            }
        });
        viewModel.uc.clickSearch.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                SearchAddressFragment searchAddressFragment = new SearchAddressFragment();
                if (currentLocation != null) {
                    Bundle bundle = SearchAddressFragment.getStartBundle(currentLocation.getLatitude(), currentLocation.getLongitude());
                    searchAddressFragment.setArguments(bundle);
                }
                startForResult(searchAddressFragment, 2001);
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        binding.refreshLayout.setEnableRefresh(false);
        try {
            new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(granted -> {
                        this.granted = granted;
                        if (granted) {
                            if (mGoogleMap != null) {
                                mGoogleMap.setMyLocationEnabled(true);
                                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                            }
                        } else {
                            if (mGoogleMap != null) {
                                mGoogleMap.setMyLocationEnabled(false);
                                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
        } catch (Exception e) {

        }
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

//        new RxPermissions(this)
//                .request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
//                .subscribe(granted -> {
//                    this.granted = granted;
//                    if (granted) {
//                        if (mGoogleMap != null) {
//                            mGoogleMap.setMyLocationEnabled(true);
//                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
//                        }
//                    } else {
//                        if (mGoogleMap != null) {
//                            mGoogleMap.setMyLocationEnabled(false);
//                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
//                        }
//                    }
//                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (granted) {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        mGoogleMap.setOnCameraIdleListener(this);
        mGoogleMap.setOnMyLocationButtonClickListener(this);

        mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (currentLocation == null) {
                    currentLocation = location;
                    moveCamera(location.getLatitude(), location.getLongitude(), false);
                }
            }
        });
    }

    public void moveCamera(double lat, double lng, boolean animate) {
        if (mGoogleMap != null) {
            LatLng mloc = new LatLng(lat, lng);
            if (animate) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mloc, 17));
            } else {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mloc, 17));
            }
        }
    }

    @Override
    public void onCameraIdle() {
        LatLng latlng = mGoogleMap.getCameraPosition().target;
        double lat = latlng.latitude;
        double lng = latlng.longitude;
        if (centerLatLng != null) {
            if (centerLatLng.latitude == lat || centerLatLng.longitude == lng) {
                return;
            }
        }
        centerLatLng = latlng;
        if (!touchItem) {
            Geocoder geocoder = new Geocoder(mActivity, Locale.TAIWAN);
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    String featureName = addresses.get(0).getFeatureName();
                    ChooseLocationItemViewModel chooseLocationItemViewModel = new ChooseLocationItemViewModel(viewModel, featureName, address, lat, lng, true);
                    viewModel.chooseLocationItemViewModel = chooseLocationItemViewModel;
                    viewModel.research(featureName, address, lat, lng);
                }
            } catch (Exception e) {
                KLog.e(e);
            }

        }
        touchItem = false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        currentLocation = null;
        return false;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        if (2001 == requestCode && resultCode == ISupportFragment.RESULT_OK) {
            String name = data.getString(ARG_ADDRESS_NAME);
            String address = data.getString(ARG_ADDRESS);
            double lat = data.getDouble(ARG_ADDRESS_LAT, 0);
            double lng = data.getDouble(ARG_ADDRESS_LNG, 0);

            touchItem = true;
            moveCamera(lat, lng, true);
            viewModel.research(name, address, lat, lng);
        }
    }
}
