package com.dl.playfun.ui.program.programdetail;

//import android.support.annotation.NonNull;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.dl.playfun.ui.mine.broadcast.mytrends.HeadItemViewModel;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.BR;
import com.dl.playfun.R;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.tatarka.bindingcollectionadapter2.ItemBinding;
import me.tatarka.bindingcollectionadapter2.OnItemBind;


public class ProgramHeadViewModel extends MultiItemViewModel {

    //  头部的recycleview
    public ObservableList<HeadItemViewModel> itemList = new ObservableArrayList<>();
    //RecyclerView多布局添加ItemBinding
    public ItemBinding<HeadItemViewModel> headItemBinding = ItemBinding.of(new OnItemBind<HeadItemViewModel>() {

        @Override
        public void onItemBind(ItemBinding itemBinding, int position, HeadItemViewModel item) {
            itemBinding.set(BR.viewModel, R.layout.item_head);
        }
    });
    //更多的点击事件
    public BindingCommand moreClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            //拿到position
//            int position = viewModel.observableList.indexOf(TrendItemViewModel.this);
//            ToastUtils.showShort("position：" + position);
//            viewModel.uc.clickMore.setValue(position);
        }
    });
    //点赞点击事件
    public BindingCommand likeClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            //拿到position
//            int position = viewModel.observableList.indexOf(TrendItemViewModel.this);
//            ToastUtils.showShort("position：" + position+"点赞");
//            viewModel.uc.clickMore.setValue(position);
        }
    });
    //评论点击事件
    public BindingCommand commentClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            //拿到position
//            int position = viewModel.observableList.indexOf(TrendItemViewModel.this);
//            ToastUtils.showShort("position：" + position+"点赞");
//            viewModel.uc.clickMore.setValue(position);
        }
    });
    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
//            ToastUtils.showShort("我是头布局");
        }
    });


    public ProgramHeadViewModel(@NonNull BaseViewModel viewModel) {
        super(viewModel);
        for (int i = 0; i < 6; i++) {
//            HeadItemViewModel headItem = new HeadItemViewModel(viewModel, "Title" + i);
//            itemList.add(headItem);
        }
    }
}
