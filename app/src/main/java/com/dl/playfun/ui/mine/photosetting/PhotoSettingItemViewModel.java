package com.dl.playfun.ui.mine.photosetting;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dl.playfun.entity.AlbumPhotoEntity;

import me.goldze.mvvmhabit.base.ItemViewModel;

/**
 * @author wulei
 */
public class PhotoSettingItemViewModel extends ItemViewModel<PhotoSettingViewModel> {
    public ObservableField<AlbumPhotoEntity> itemEntity = new ObservableField<>();
    public ObservableField<Integer> playStatus = new ObservableField<>(0);

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

}
