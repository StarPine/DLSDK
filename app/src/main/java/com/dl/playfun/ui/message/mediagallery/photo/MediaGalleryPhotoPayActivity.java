package com.dl.playfun.ui.message.mediagallery.photo;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.ActivityMediaGalleryPhotoBinding;
import com.dl.playfun.ui.base.BaseActivity;
import com.dl.playfun.utils.ImmersionBarUtils;

/**
 * Author: 彭石林
 * Time: 2022/9/14 11:20
 * Description: This is MediaGalleryPhotoPayActivity
 */
public class MediaGalleryPhotoPayActivity extends BaseActivity<ActivityMediaGalleryPhotoBinding,MediaGalleryPhotoPayViewModel> {
    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_media_gallery_photo;
    }

    @Override
    public int initVariableId() {
        return BR.photoViewModel;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImmersionBarUtils.setupStatusBar(this, false, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }

    @Override
    public void initParam() {
        super.initParam();
    }

    @Override
    public MediaGalleryPhotoPayViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(MediaGalleryPhotoPayViewModel.class);
    }


}
