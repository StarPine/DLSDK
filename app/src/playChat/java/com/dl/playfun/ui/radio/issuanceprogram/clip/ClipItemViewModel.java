package com.dl.playfun.ui.radio.issuanceprogram.clip;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.CustonImgVideoEntity;
import com.dl.playfun.utils.ImageUtils;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.base.ItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * Author: 彭石林
 * Time: 2021/10/12 11:49
 * Description: This is ClipItemViewModel
 */
public class ClipItemViewModel extends ItemViewModel<ClipImageVideoViewModel> {

    public ObservableBoolean checked = new ObservableBoolean(false);
    public ObservableField<CustonImgVideoEntity> itemEntity = new ObservableField<>();

    public ClipItemViewModel(@NonNull @NotNull ClipImageVideoViewModel viewModel,CustonImgVideoEntity custonImgVideoEntity) {
        super(viewModel);
        itemEntity.set(custonImgVideoEntity);
    }

    public BindingCommand clickPostion = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            int position = viewModel.objItems.indexOf(ClipItemViewModel.this);
            viewModel.itemClickSelectEntity(itemEntity.get(),position);
        }
    });
    //获取视频时间
    public String getVideoTime(){
        if(itemEntity.get().getMediaType()==2){
            return ImageUtils.conversionTime((int)itemEntity.get().getDuration());
        }else{
            return null;
        }

    }
}
