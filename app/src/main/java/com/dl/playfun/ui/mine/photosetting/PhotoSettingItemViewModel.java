package com.dl.playfun.ui.mine.photosetting;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.dl.playfun.entity.AlbumPhotoEntity;

import me.goldze.mvvmhabit.base.ItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * @author wulei
 */
public class PhotoSettingItemViewModel extends ItemViewModel<PhotoSettingViewModel> {
    public ObservableField<AlbumPhotoEntity> itemEntity = new ObservableField<>();
    public ObservableField<Integer> playStatus = new ObservableField<>(0);

    public ObservableInt photoCoverShow = new ObservableInt(-1);

    public PhotoSettingItemViewModel(@NonNull PhotoSettingViewModel viewModel, AlbumPhotoEntity itemEntity) {
        super(viewModel);
        this.itemEntity.set(itemEntity);
    }

    public Integer getPosition() {
        int position = 0;
        if (viewModel instanceof PhotoSettingViewModel) {
            position = viewModel.items.indexOf(PhotoSettingItemViewModel.this);
        }
        return position;
    }
    //点击展示弹窗--封面详情
    public BindingCommand itemClickHint = new BindingCommand(()->{
        if (viewModel instanceof PhotoSettingViewModel) {
            viewModel.uc.clickPhotoCoverAlert.call();
        }
    });
    //点击上传为封面
    public BindingCommand itemClickPhotoCover = new BindingCommand(() -> {
        AlbumPhotoEntity albumPhotoEntity = itemEntity.get();
        //不为null  并且是本人
        if(albumPhotoEntity!=null && albumPhotoEntity.getVerificationType()==1){
            if(albumPhotoEntity.getIsCallCover()!=null && albumPhotoEntity.getIsCallCover()==0){
                int position = -1;
                if (viewModel instanceof PhotoSettingViewModel) {
                    position = viewModel.items.indexOf(PhotoSettingItemViewModel.this);
                }
                if(position!=-1){
                    viewModel.photoCallCover(albumPhotoEntity.getId());
                }
            }
        }
    });

    //根据是否已上传封面状态来判断显示
    public boolean getShowPhotoCover(int state){
        AlbumPhotoEntity albumPhotoEntity = itemEntity.get();
        //不为null  并且是本人
        if(albumPhotoEntity!=null && albumPhotoEntity.getVerificationType()==1){
            return state == 0;
        }
        return false;
    }

}
