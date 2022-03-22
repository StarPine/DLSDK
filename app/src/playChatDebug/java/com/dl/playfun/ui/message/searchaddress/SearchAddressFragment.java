package com.dl.playfun.ui.message.searchaddress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.KeyboardUtils;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.FragmentSearchAddressBinding;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.utils.ImmersionBarUtils;

import java.util.Objects;

import me.yokeyword.fragmentation.ISupportFragment;


/**
 * @author wulei
 */
public class SearchAddressFragment extends BaseRefreshFragment<FragmentSearchAddressBinding, SearchAddressViewModel> {
    public static final String ARG_LAT = "arg_lat";
    public static final String ARG_LNG = "arg_lng";

    public static final String ARG_ADDRESS_NAME = "arg_address_name";
    public static final String ARG_ADDRESS = "arg_address";
    public static final String ARG_ADDRESS_LAT = "arg_address_lat";
    public static final String ARG_ADDRESS_LNG = "arg_address_lng";

    private double lat, lng;

    public static Bundle getStartBundle(Double lat, Double lng) {
        Bundle bundle = new Bundle();
        bundle.putDouble(ARG_LAT, lat);
        bundle.putDouble(ARG_LNG, lng);
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
        lat = getArguments().getDouble(ARG_LAT, 0);
        lng = getArguments().getDouble(ARG_LNG, 0);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_search_address;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public SearchAddressViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        SearchAddressViewModel searchAddressViewModel = ViewModelProviders.of(this, factory).get(SearchAddressViewModel.class);
        if (lat > 0 && lng > 0) {
            searchAddressViewModel.lat = lat;
            searchAddressViewModel.lng = lng;
        }
        return searchAddressViewModel;
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.clickItemAddress.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {
                KeyboardUtils.hideSoftInput(Objects.requireNonNull(mActivity));
                SearchAddressItemViewModel searchAddressItemViewModel = viewModel.observableList.get(position);
                Bundle bundle = new Bundle();
                bundle.putString(ARG_ADDRESS_NAME, searchAddressItemViewModel.name.get());
                bundle.putString(ARG_ADDRESS, searchAddressItemViewModel.address.get());
                bundle.putDouble(ARG_ADDRESS_LAT, searchAddressItemViewModel.lat.get());
                bundle.putDouble(ARG_ADDRESS_LNG, searchAddressItemViewModel.lng.get());
                setFragmentResult(ISupportFragment.RESULT_OK, bundle);
                viewModel.pop();
            }
        });
    }

    @Override
    public void initData() {
        binding.refreshLayout.setEnableRefresh(false);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        binding.edtSearch.requestFocus();
        KeyboardUtils.showSoftInput();
    }
}
