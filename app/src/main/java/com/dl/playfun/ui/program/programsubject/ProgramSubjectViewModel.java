package com.dl.playfun.ui.program.programsubject;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.BR;
import com.dl.playfun.R;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * @author wulei
 */
public class ProgramSubjectViewModel extends BaseViewModel<AppRepository> {
    //    节目
    public List<ConfigItemEntity> program = new ArrayList<>();
    //    public BindingCommand privacySettingOnClickCommand = new BindingCommand(new BindingAction() {
//        @Override
//        public void call() {
//            Bundle bundle = new Bundle();
//            bundle.putInt("programId", 1);
//            start(ChooseProgramSiteFragment.class.getCanonicalName(),bundle);
//        }
//    });
    public BindingCommand closeClickCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            pop();
        }
    });
    public ObservableList<ProgramChooseItemViewModel> itemList = new ObservableArrayList<>();
    public ItemBinding<ProgramChooseItemViewModel> itemBinding = ItemBinding.of(BR.viewModel, R.layout.item_program_choose);


    public ProgramSubjectViewModel(@NonNull Application application, AppRepository repository) {
        super(application, repository);
        program.addAll(model.readThemeConfig());
        for (ConfigItemEntity configItemEntity : program) {
            ProgramChooseItemViewModel item = new ProgramChooseItemViewModel(this, configItemEntity);
            itemList.add(item);

        }
    }

}