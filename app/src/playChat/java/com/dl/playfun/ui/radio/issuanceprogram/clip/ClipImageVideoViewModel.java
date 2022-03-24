package com.dl.playfun.ui.radio.issuanceprogram.clip;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableList;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.entity.CustonImgVideoEntity;
import com.dl.playfun.viewmodel.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

/**
 * Author: 彭石林
 * Time: 2021/10/11 19:01
 * Description: This is clipImageVideoViewModel
 */
public class ClipImageVideoViewModel extends BaseViewModel<AppRepository> {

    //图片、视频
    public BindingRecyclerViewAdapter<ClipItemViewModel> objAdapter = new BindingRecyclerViewAdapter<>();
    public ItemBinding<ClipItemViewModel> objItemBinding = ItemBinding.of(BR.viewModel, R.layout.item_clip_img_video_grid);
    public ObservableList<ClipItemViewModel> objItems = new ObservableArrayList<>();
    public ObservableField<String> durationTxt = new ObservableField<>();
    //上一个选中的内容
    private int old_position = -1;

    public UIChangeObservable ucClip = new UIChangeObservable();
    public class UIChangeObservable {
        public SingleLiveEvent<CustonImgVideoEntity> upMediaSource = new SingleLiveEvent<>();
        public SingleLiveEvent<Void> CliPhoneSub = new SingleLiveEvent<>();
    }

    public ClipImageVideoViewModel(@NonNull @NotNull Application application, AppRepository model) {
        super(application, model);
    }
    //确定裁剪
    public BindingCommand btnSubscribe = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            ucClip.CliPhoneSub.call();
        }
    });

    public void itemClickSelectEntity(@NonNull CustonImgVideoEntity custonImgVideoEntity,int position){
//        for(ClipItemViewModel clipItemViewModel : objItems){
//            if(clipItemViewModel.equals(objItems.get(position))){
//                clipItemViewModel.checked.set(true);
//            }else{
//                clipItemViewModel.checked.set(false);
//            }
//        }
//        objAdapter.notifyDataSetChanged();
        objAdapter.getAdapterItem(position).checked.set(true);
        if(old_position!=-1){
            if(old_position==position){
                old_position = position;
                return;
            }
            objAdapter.getAdapterItem(old_position).checked.set(false);
            old_position = position;
        }else{
            old_position = position;
        }
        ucClip.upMediaSource.setValue(custonImgVideoEntity);
    }
}
