package com.dl.playfun.ui.program.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.ui.program.adapter.ProgramSiteItemViewModel;
import com.dl.playfun.viewmodel.BaseRefreshViewModel;
import com.dl.playfun.BR;
import com.dl.playfun.R;

import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public abstract class BaseProgramSiteViewModel extends BaseRefreshViewModel<AppRepository> {

    public ObservableList<ProgramSiteItemViewModel> observableList = new ObservableArrayList<>();
    public ItemBinding<ProgramSiteItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_program_site);

    public BaseProgramSiteViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);

    }

    public void onItemClick(int position) {

    }

}