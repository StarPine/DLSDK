package com.dl.playfun.ui.login.choose;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.viewmodel.BaseViewModel;

import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * Author: 彭石林
 * Time: 2022/7/5 17:35
 * Description: This is ChooseAreaViewModel
 */
public class ChooseAreaViewModel extends BaseViewModel<AppRepository> {

    //广告列表
    public BindingRecyclerViewAdapter<ItemChooseAreaViewModel> area_adapter = new BindingRecyclerViewAdapter<>();
    public ObservableList<ItemChooseAreaViewModel> area_observableList = new ObservableArrayList<>();
    public ItemBinding<ItemChooseAreaViewModel> area_itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_area_check);

    public ChooseAreaViewModel(@NonNull Application application, AppRepository model) {
        super(application, model);
    }

}
