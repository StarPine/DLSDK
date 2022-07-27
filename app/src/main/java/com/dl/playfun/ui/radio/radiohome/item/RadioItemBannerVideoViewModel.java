package com.dl.playfun.ui.radio.radiohome.item;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.dl.playfun.R;
import com.dl.playfun.entity.AdUserItemEntity;
import com.dl.playfun.ui.radio.radiohome.RadioViewModel;
import com.dl.playfun.ui.userdetail.detail.UserDetailFragment;
import com.dl.playfun.utils.StringUtil;
import com.tencent.qcloud.tuikit.tuichat.component.AudioPlayer;

import java.util.Objects;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * Author: 彭石林
 * Time: 2022/7/26 19:17
 * Description: This is RadioItemBannerVideoViewModel
 */
public class RadioItemBannerVideoViewModel extends MultiItemViewModel<RadioViewModel> {

    public ObservableField<AdUserItemEntity> adUserItemEntity = new ObservableField<>();
    public ObservableBoolean isPlaying = new ObservableBoolean(false);

    public RadioItemBannerVideoViewModel(@NonNull RadioViewModel viewModel,AdUserItemEntity adUserItemEntity) {
        super(viewModel);
        this.adUserItemEntity.set(adUserItemEntity);
    }
    //播放语音
    public BindingCommand  audioPlayClickCommand = new BindingCommand(() -> {
        isPlaying.set(true);
        if (AudioPlayer.getInstance().isPlaying()) {
            AudioPlayer.getInstance().stopPlay();
            isPlaying.set(false);
            return;
        }
        //全局音频播放单列
        AudioPlayer.getInstance().startPlay(StringUtil.getFullAudioUrl(Objects.requireNonNull(adUserItemEntity.get()).getSound()), (success, isOutTime) -> isPlaying.set(false));
        int position = viewModel.radioItemsAdUser.indexOf(this);
        if (position != - 1){
            viewModel.itemClickPlayAudio(position);
        }
    });

    //拨打视频
    public BindingCommand callVideoClickCommand = new BindingCommand(() -> viewModel.itemClickCallVideo(adUserItemEntity.get()));

    public Integer getSoundShow() {
        if (adUserItemEntity.get() != null && StringUtils.isEmpty(adUserItemEntity.get().getSound())) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }
}
