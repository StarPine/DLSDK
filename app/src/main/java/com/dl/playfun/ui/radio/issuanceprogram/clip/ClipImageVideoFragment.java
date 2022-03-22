package com.dl.playfun.ui.radio.issuanceprogram.clip;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.aliyun.common.utils.DensityUtil;
import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.ThreadUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.base.AlivcSvideoEditParam;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.utils.VideoInfoUtils;
import com.aliyun.svideo.base.widget.HorizontalListView;
import com.aliyun.svideo.base.widget.SizeChangedNotifier;
import com.aliyun.svideo.base.widget.VideoSliceSeekBar;
import com.aliyun.svideo.base.widget.VideoTrimFrameLayout;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.crop.VideoTrimAdapter;
import com.aliyun.svideo.crop.bean.AlivcCropOutputParam;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.struct.common.CropKey;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.crop.AliyunICrop;
import com.aliyun.svideosdk.crop.CropCallback;
import com.aliyun.svideosdk.crop.CropParam;
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator;
import com.aliyun.svideosdk.player.AliyunISVideoPlayer;
import com.aliyun.svideosdk.player.PlayerCallback;
import com.aliyun.svideosdk.player.impl.AliyunSVideoPlayerCreator;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.dl.playfun.app.AppConfig;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.entity.CustonImgVideoEntity;
import com.dl.playfun.event.SelectMediaSourcesEvent;
import com.dl.playfun.ui.base.BaseFragment;
import com.dl.playfun.utils.ApiUitl;
import com.dl.playfun.utils.TimeUtils;
import com.dl.playfun.widget.dialog.DensityUtils;
import com.duanqu.transcode.NativeParser;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentClipImageVideoBinding;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import me.goldze.mvvmhabit.bus.RxBus;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * Author: 彭石林
 * Time: 2021/10/11 19:01
 * Description: This is clipImageVideoFragment
 */
public class ClipImageVideoFragment extends BaseFragment<FragmentClipImageVideoBinding, ClipImageVideoViewModel> implements CustomAdapt,TextureView.SurfaceTextureListener, HorizontalListView.OnScrollCallBack, SizeChangedNotifier.Listener,
        MediaPlayer.OnVideoSizeChangedListener, VideoTrimFrameLayout.OnVideoScrollCallBack {

    private CustonImgVideoEntity $custonEntity;
    private RelativeLayout.LayoutParams $layoutParams = null;
    /**
    * @Desc TODO(阿里云SDK)
    * @Date 2021/10/14
    */
    public static final VideoDisplayMode SCALE_CROP = VideoDisplayMode.SCALE;
    public static final VideoDisplayMode SCALE_FILL = VideoDisplayMode.FILL;
    public static final String TAG = ClipImageVideoFragment.class.getSimpleName();
    public static final int REQUEST_CODE_EDITOR_VIDEO_CROP = 1;
    public static final int REQUEST_CODE_CROP_VIDEO_CROP = 2;

    private static final int PLAY_VIDEO = 1000;
    private static final int PAUSE_VIDEO = 1001;
    private static final int END_VIDEO = 1003;


    private int playState = END_VIDEO;

    private AliyunICrop crop;

    private VideoTrimFrameLayout frame;
    private TextureView textureview;
    private Surface mSurface;

    /**
     * sdk提供的播放器，支持非关键帧的实时预览
     */
    private AliyunISVideoPlayer mPlayer;



    private String mInputVideoPath;
    private String outputPath;
    private long duration;
    private int resolutionMode;
    private final int ratioMode =1;
    private VideoQuality quality = VideoQuality.HD;
    private final VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    private final int frameRate = 30;
    private int gop;

    private int screenWidth;
    private int frameWidth;
    private int frameHeight;
    private int mScrollX;
    private int mScrollY;
    private int videoWidth;
    private int videoHeight;
    private int cropDuration = 2000;

    private VideoDisplayMode cropMode = VideoDisplayMode.SCALE;
    private AliyunIThumbnailFetcher mThumbnailFetcher;


    private long mStartTime;
    private long mEndTime;

    private final static int MAX_DURATION = Integer.MAX_VALUE;
    private VideoTrimAdapter adapter;

    private final Handler playHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case PAUSE_VIDEO:
                    pauseVideo();
                    break;
                case PLAY_VIDEO:
                    if (mPlayer != null) {
                        long currentPlayPos = mPlayer.getCurrentPosition() / 1000;
                        Log.d(TAG, "currentPlayPos:" + currentPlayPos);
                        if (currentPlayPos < mEndTime) {
                            binding.aliyunSeekBar.showFrameProgress(true);
                            binding.aliyunSeekBar.setFrameProgress(currentPlayPos / (float) duration);
                            playHandler.sendEmptyMessageDelayed(PLAY_VIDEO, 100);
                        } else {
                            playVideo();
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private boolean isPause = false;
    private boolean isCropping = false;
    /**
     * 每次修改裁剪结束位置时需要重新播放视频
     */
    private boolean needPlayStart = false;
    private boolean isUseGPU = false;
    /**
     * @Desc TODO(阿里云SDK)
     * @Date 2021/10/14
     */
    /**
     * 原比例
     */
    public static final int RATIO_ORIGINAL = 3;
    private VideoDisplayMode mOriginalMode;

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ClipImageVideoViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        ClipImageVideoViewModel clipImageVideoViewModel = ViewModelProviders.of(this, factory).get(ClipImageVideoViewModel.class);
        return clipImageVideoViewModel;
    }

    @Override
    public void initParam() {
        super.initParam();
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
         return R.layout.fragment_clip_image_video;
    }

    @Override
    public void initData() {
        super.initData();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        crop = AliyunCropCreator.createCropInstance(getContext());
        crop.setCropCallback(new CropCallbacks());
        initAliYun();
        initSurface();
        $layoutParams = (RelativeLayout.LayoutParams) binding.cropImageView.getLayoutParams();
        final Uri contentUri = MediaStore.Files.getContentUri("external");
        final String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        final String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? OR "+ MediaStore.Files.FileColumns.MEDIA_TYPE +"=?)" + " AND " + MediaStore.MediaColumns.SIZE + ">0";

        ContentResolver contentResolver = getContext().getContentResolver();
        String[] projections = new String[]{MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.SIZE, MediaStore.Video.Media.DURATION};

        boolean defaultCheck = true;
        Cursor cursor = null;
            cursor = contentResolver.query(contentUri, projections, selection, new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)}, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {

            int pathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            int mimeTypeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
            int durationIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION);
            int widthIdx = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
            int heightIdx = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);

            do {
                long size = cursor.getLong(sizeIndex);
                // 图片大小不得小于 1 KB
                if (size < 1024) {
                    continue;
                }

                String type = cursor.getString(mimeTypeIndex);
                String path = cursor.getString(pathIndex);
                if (TextUtils.isEmpty(path) || TextUtils.isEmpty(type)) {
                    continue;
                }

                File file = new File(path);
                if (!file.exists() || !file.isFile()) {
                    continue;
                }
                CustonImgVideoEntity custonImgVideoEntity = new CustonImgVideoEntity();
                custonImgVideoEntity.setPath(path);
                //图片
                if(type.equals("image/jpeg") && (path.endsWith("png") || path.endsWith("jpg") || path.endsWith("jpeg"))){
                    custonImgVideoEntity.setMediaType(1);
                }else if(type.equals("video/mp4") && path.endsWith("mp4")){
                    long duration = cursor.getLong(durationIndex);
                    // 视频时长不得小于 3 秒
                    if (duration < 3000) {
                        continue;
                    }
                    // 视频大小不得小于 10 KB
                    if (size < 1024 * 10) {
                        continue;
                    }
                    custonImgVideoEntity.setMediaType(2);
                    custonImgVideoEntity.setDuration(duration);
                    custonImgVideoEntity.setSize(size);
                }else{
                    continue;
                }

                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    // 获取目录名作为专辑名称
                    int width = cursor.getInt(widthIdx);
                    int height = cursor.getInt(heightIdx);
                    custonImgVideoEntity.setWidth(width);
                    custonImgVideoEntity.setHeight(height);
                    ClipItemViewModel clipItemViewModel = new ClipItemViewModel(viewModel,custonImgVideoEntity);
                    if(type.equals("image/jpeg")){
                        if(defaultCheck){
                            defaultCheck = false;
                            upMediaPath(path);
                        }
                    }
                    viewModel.objItems.add(clipItemViewModel);
                }

            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.ucClip.CliPhoneSub.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                if($custonEntity!=null){
                    if($custonEntity.getMediaType()==1){
                        binding.cropImageView.setDrawingCacheEnabled(true);
                        Bitmap bitmap = ImageUtils.view2Bitmap(binding.cropImageView);
                        binding.cropImageView.setDrawingCacheEnabled(false);
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        Bitmap newBitmap = ImageUtils.clip(bitmap, 0, height > 1080 ? 1080 : height, width > 1080 ? 1080 : width, height > 1080 ? 1080 : height);
                        ApiUitl.saveBitmap(getContext(), newBitmap, AppConfig.VERSION_NAME + ApiUitl.getDateTimeFileName() + ".jpg", new ApiUitl.CallBackUploadFileNameCallback() {
                            @Override
                            public void success(String fileName) {
                                ToastUtils.showShort("保存成功");
                                RxBus.getDefault().post(new SelectMediaSourcesEvent(fileName, 1));
                                pop();
                            }
                        });

                    }else{
                        int mAction = 0;
                        if (mScrollX != 0 || mScrollY != 0 || !cropMode.equals(mOriginalMode)) {
                            //需要裁剪画面时或者切换裁剪模式时，走真裁剪
                            mAction = CropKey.ACTION_TRANSCODE;
                        }
                        switch (mAction) {
                            case CropKey.ACTION_TRANSCODE:
                                startCrop();
                                break;
                            case CropKey.ACTION_SELECT_TIME:
                                long duration = mEndTime - mStartTime;
                                AlivcCropOutputParam cropOutputParam = new AlivcCropOutputParam();
                                //由于只是选择时间，所以文件路径和输入路径保持一致
                                cropOutputParam.setOutputPath(mInputVideoPath);
                                cropOutputParam.setDuration(duration);
                                cropOutputParam.setStartTime(mStartTime);
                                onCropComplete(cropOutputParam);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });
        viewModel.ucClip.upMediaSource.observe(this, new Observer<CustonImgVideoEntity>() {
            @Override
            public void onChanged(CustonImgVideoEntity custonImgVideoEntity) {
                $custonEntity = custonImgVideoEntity;
                if(custonImgVideoEntity.getMediaType()==2){
                    //视频
                    binding.cropImageView.setVisibility(View.GONE);
                    binding.trimRoot.setVisibility(View.VISIBLE);
                    //清楚所有帧适配
                    adapter.removeAllItems();
                    getData(custonImgVideoEntity.getPath());
//                    AliyunSnapVideoParam mCropParam = new AliyunSnapVideoParam.Builder()
//                            .setFrameRate(30)//帧率默认30
//                            .setGop(250) //关键帧间隔
//                            .setFilterList(new String[]{})
//                            .setCropMode(VideoDisplayMode.SCALE)
//                            .setVideoQuality(VideoQuality.HD)
//                            .setVideoCodec(VideoCodecs.H264_HARDWARE)
//                            .setResolutionMode(0)
//                            .setRatioMode(1)
//                            .setCropMode(VideoDisplayMode.FILL)
//                            .setNeedRecord(false)
//                            .setMinVideoDuration(2000)
//                            .setMaxVideoDuration(60 * 1000 * 1000)
//                            .setMinCropDuration(3000)
//                            .setSortMode(AliyunSnapVideoParam.SORT_MODE_MERGE)
//                            .build();
                }else{
                    binding.trimRoot.setVisibility(View.GONE);
                    binding.cropImageView.setVisibility(View.VISIBLE);
                    upMediaPath(custonImgVideoEntity.getPath());
                }

            }
        });
    }

    public void upMediaPath(String path){
        Glide.with(getContext())
                .asBitmap()
                .load(path)
                .into(new ImageViewTarget<Bitmap>(binding.cropImageView) {
                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }

                    @Override
                    protected void setResource(@Nullable Bitmap resource) {
                        if (resource != null) {
                                // 普通图片
                            int imgWidth = resource.getWidth();
                            int ImgHeight = resource.getHeight();
                            $layoutParams.width = imgWidth;
                            $layoutParams.height = ImgHeight;
                            binding.cropImageView.setLayoutParams($layoutParams);
                            binding.cropImageView.setImageBitmap(resource);
                        }
                    }
                });
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }

    private void getData(String VideoPath) {
        mInputVideoPath = VideoPath;
        try {
            duration = crop.getVideoDuration(mInputVideoPath) / 1000;
        } catch (Exception e) {
            ToastUtil.showToast(getContext(), R.string.alivc_crop_video_tip_crop_failed);
        }
        //获取精确的视频时间
        resolutionMode = 0;
        cropMode = VideoDisplayMode.SCALE;
        mOriginalMode = cropMode;
        quality = VideoQuality.HD;
        gop = 3001;
        cropDuration = 3000;
        isUseGPU = true;

        resizeFrame();
        initAliYunView();
        playVideo();
        binding.aliyunSeekBar.setSliceBlocked(true);
    }

    private void initAliYun(){
        //进度条
        binding.aliyunSeekBar.setSeekBarChangeListener(mSeekBarListener);
        //帧图片
        adapter = new VideoTrimAdapter(getContext(), new ArrayList<SoftReference<Bitmap>>());
        binding.aliyunVideoTailorImageList.setAdapter(adapter);
        binding.aliyunVideoTailorImageList.setOnScrollCallBack(this);
        //设置进度条布局高
        setListViewHeight();
    }
    private void initAliYunView() {
        int mOutStrokeWidth = DensityUtil.dip2px(getContext(), 5);
        int minDiff = (int) (cropDuration / (float) duration * 100) + 1;
        binding.aliyunSeekBar.setProgressMinDiff(minDiff > 100 ? 100 : minDiff);
        viewModel.durationTxt.set((float) duration / 1000 + "");
        binding.aliyunCropProgressBg.setVisibility(View.GONE);
        binding.aliyunCropProgress.setOutRadius(DensityUtil.dip2px(getContext(), 40) / 2 - mOutStrokeWidth / 2);
        binding.aliyunCropProgress.setOffset(mOutStrokeWidth / 2, mOutStrokeWidth / 2);
        binding.aliyunCropProgress.setOutStrokeWidth(mOutStrokeWidth);

        requestThumbItemTime();
    }

    private void setListViewHeight() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.aliyunVideoTailorImageList.getLayoutParams();
        layoutParams.height = (screenWidth - DensityUtils.dip2px(getContext(), 40)) / 10;
        binding.aliyunVideoTailorImageList.setLayoutParams(layoutParams);
        binding.aliyunSeekBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, layoutParams.height));
    }

    public void initSurface() {
        frame = binding.aliyunVideoSurfaceLayout;
        frame.setOnSizeChangedListener(this);
        frame.setOnScrollCallBack(this);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) frame.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenWidth;
        frame.setLayoutParams(layoutParams);
        textureview = binding.aliyunVideoTextureview;
//        resizeFrame();
        textureview.setSurfaceTextureListener(this);
    }

    private void resizeFrame() {
        NativeParser parser = new NativeParser();
        parser.init(mInputVideoPath);
        try {
            videoWidth = Integer.parseInt(parser.getValue(NativeParser.VIDEO_WIDTH));
            videoHeight = Integer.parseInt(parser.getValue(NativeParser.VIDEO_HEIGHT));

        } catch (NumberFormatException ex) {
            Log.e(TAG, ex.getMessage());
            return;
        } finally {
            parser.release();
            parser.dispose();
        }

        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }

        frameWidth = screenWidth;
        frameHeight = screenWidth;
        if (cropMode == SCALE_CROP) {
            scaleCrop(videoWidth, videoHeight);
        } else if (cropMode == SCALE_FILL) {
            scaleFill(videoWidth, videoHeight);
        }

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mPlayer == null) {
            mSurface = new Surface(surface);
            mPlayer = AliyunSVideoPlayerCreator.createPlayer();
            mPlayer.init(getContext());

            mPlayer.setPlayerCallback(new PlayerCallback() {
                @Override
                public void onPlayComplete() {
                }

                @Override
                public void onDataSize(int dataWidth, int dataHeight) {
                    if (dataWidth == 0 || dataHeight == 0) {
                        return;
                    }
                    frameWidth = frame.getWidth();
                    frameHeight = frame.getHeight();
                    videoWidth = dataWidth;
                    videoHeight = dataHeight;
                    if (crop != null && mEndTime == 0) {
                        try {
                            mEndTime = (long) (crop.getVideoDuration(mInputVideoPath) * 1.0f / 1000);
                        } catch (Exception e) {
                            ToastUtil.showToast(getContext(), R.string.alivc_crop_video_tip_error);
                        }
                    }
                    scaleCrop(dataWidth, dataHeight);
                    mPlayer.setDisplaySize(textureview.getLayoutParams().width, textureview.getLayoutParams().height);
                    playVideo();
                }

                @Override
                public void onError(int i) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(getContext(), getString(R.string.alivc_crop_video_tip_error));
                        }
                    });

                }
            });
            mPlayer.setDisplay(mSurface);
            mPlayer.setSource(mInputVideoPath);

        } else {
            mPlayer.stop();
            mPlayer.setSource(mInputVideoPath);
            mPlayer.play();
        }
        Log.i(TAG, "onSurfaceTextureAvailable");
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mPlayer.setDisplaySize(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            playState = END_VIDEO;
            mPlayer = null;
            mSurface.release();
            mSurface = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private final VideoSliceSeekBar.SeekBarChangeListener mSeekBarListener = new VideoSliceSeekBar.SeekBarChangeListener() {
        @Override
        public void seekBarValueChanged(float leftThumb, float rightThumb, int whitchSide) {
            long seekPos = 0;
            if (whitchSide == 0) {
                seekPos = (long) (duration * leftThumb / 100);
                mStartTime = seekPos;
            } else if (whitchSide == 1) {
                seekPos = (long) (duration * rightThumb / 100);
                mEndTime = seekPos;
            }
            viewModel.durationTxt.set((float) (mEndTime - mStartTime) / 1000 + "");
            if (mPlayer != null) {
                mPlayer.seek((int) seekPos);
            }
            Log.e(TAG, "mStartTime" + mStartTime);
        }

        @Override
        public void onSeekStart() {
            pauseVideo();
        }

        @Override
        public void onSeekEnd() {
            needPlayStart = true;
            if (playState == PAUSE_VIDEO) {
                playVideo();
            }
        }
    };

    private void resetScroll() {
        mScrollX = 0;
        mScrollY = 0;
    }

    @Override
    public void onScrollDistance(Long count, int distanceX) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPause) {
            playVideo();
        }
        if (isCropping && mPlayer != null) {
            long currentPosition = mPlayer.getCurrentPosition() / 1000;
            mPlayer.draw(currentPosition);
        }
    }

    @Override
    public void onPause() {
        if (playState == PLAY_VIDEO) {
            pauseVideo();
        }
        isPause = true;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (crop != null) {
            crop.dispose();
            crop = null;
        }
        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }
    }

    private void scaleFill(int videoWidth, int videoHeight) {
        if (videoWidth == 0 || videoHeight == 0) {
            Log.e(TAG, "error , videoSize width = 0 or height = 0");
            return;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureview.getLayoutParams();
        int s = Math.min(videoWidth, videoHeight);
        int b = Math.max(videoWidth, videoHeight);
        float videoRatio = (float) b / s;
        float ratio = 1f;
        if (videoWidth > videoHeight) {
            layoutParams.width = frameWidth;
            layoutParams.height = frameWidth * videoHeight / videoWidth;
        } else {
            if (videoRatio >= ratio) {
                layoutParams.height = frameHeight;
                layoutParams.width = frameHeight * videoWidth / videoHeight;
            } else {
                layoutParams.width = frameWidth;
                layoutParams.height = frameWidth * videoHeight / videoWidth;
            }
        }
        layoutParams.setMargins(0, 0, 0, 0);
        textureview.setLayoutParams(layoutParams);
        cropMode = SCALE_FILL;
        resetScroll();
    }

    private void scaleCrop(int videoWidth, int videoHeight) {
        if (videoWidth == 0 || videoHeight == 0) {
            Log.e(TAG, "error , videoSize width = 0 or height = 0");
            return;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureview.getLayoutParams();
        int s = Math.min(videoWidth, videoHeight);
        int b = Math.max(videoWidth, videoHeight);
        float videoRatio = (float) b / s;
        float ratio = 1f;
        if (ratioMode == AlivcSvideoEditParam.RATIO_MODE_ORIGINAL) {
            //原比例显示逻辑和填充模式一致
            if (videoWidth > videoHeight) {
                layoutParams.width = frameWidth;
                layoutParams.height = frameWidth * videoHeight / videoWidth;
            } else {
                if (videoRatio >= ratio) {
                    layoutParams.height = frameHeight;
                    layoutParams.width = frameHeight * videoWidth / videoHeight;
                } else {
                    layoutParams.width = frameWidth;
                    layoutParams.height = frameWidth * videoHeight / videoWidth;
                }
            }
        } else {
            if (videoWidth > videoHeight) {
                layoutParams.height = frameHeight;
                layoutParams.width = frameHeight * videoWidth / videoHeight;
            } else {
                if (videoRatio >= ratio) {
                    layoutParams.width = frameWidth;
                    layoutParams.height = frameWidth * videoHeight / videoWidth;
                } else {
                    layoutParams.height = frameHeight;
                    layoutParams.width = frameHeight * videoWidth / videoHeight;

                }
            }

        }

        layoutParams.setMargins(0, 0, 0, 0);
        textureview.setLayoutParams(layoutParams);
        cropMode = SCALE_CROP;
        resetScroll();
    }


    private void scanFile() {
        MediaScannerConnection.scanFile(getContext(),
                new String[] {outputPath}, new String[] {"video/mp4"}, null);
    }

    private void playVideo() {
        if (isCropping) {
            //裁剪过程中点击无效
            return;
        }
        if (mPlayer == null) {
            return;
        }
        mPlayer.seek((int) mStartTime);
        mPlayer.resume();
        playState = PLAY_VIDEO;
        long videoPos = mStartTime;
        playHandler.sendEmptyMessage(PLAY_VIDEO);
        //重新播放之后修改为false，防止暂停、播放的时候重新开始播放
        needPlayStart = false;
    }

    private void pauseVideo() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.pause();
        playState = PAUSE_VIDEO;
        playHandler.removeMessages(PLAY_VIDEO);
        binding.aliyunSeekBar.showFrameProgress(false);
        binding.aliyunSeekBar.invalidate();
    }

    private void resumeVideo() {
        if (mPlayer == null) {
            return;
        }
        if (needPlayStart) {
            playVideo();
            needPlayStart = false;
            return;
        }
        mPlayer.resume();
        playState = PLAY_VIDEO;
        playHandler.sendEmptyMessage(PLAY_VIDEO);
    }

    public void onBackPressed() {
        if (isCropping) {
            crop.cancel();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        frameWidth = frame.getWidth();
        frameHeight = frame.getHeight();
        videoWidth = width;
        videoHeight = height;
        mStartTime = 0;
        if (crop != null) {
            try {
                mEndTime = (long) (crop.getVideoDuration(mInputVideoPath) * 1.0f / 1000);
            } catch (Exception e) {
                ToastUtil.showToast(getContext(), R.string.alivc_crop_video_tip_crop_failed);
            }
        } else {
            mEndTime = Integer.MAX_VALUE;
        }
        if (cropMode == SCALE_CROP) {
            scaleCrop(width, height);
        } else if (cropMode == SCALE_FILL) {
            scaleFill(width, height);
        }

    }

    @Override
    public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {

    }

    @Override
    public void onVideoScroll(float distanceX, float distanceY) {
        if (isCropping) {
            //裁剪中无法操作
            return;
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textureview.getLayoutParams();
        int width = lp.width;
        int height = lp.height;

        if (width > frameWidth || height > frameHeight) {
            int maxHorizontalScroll = width - frameWidth;
            int maxVerticalScroll = height - frameHeight;
            if (maxHorizontalScroll > 0) {
                maxHorizontalScroll = maxHorizontalScroll / 2;
                mScrollX += distanceX;
                if (mScrollX > maxHorizontalScroll) {
                    mScrollX = maxHorizontalScroll;
                }
                if (mScrollX < -maxHorizontalScroll) {
                    mScrollX = -maxHorizontalScroll;
                }
            }
            if (maxVerticalScroll > 0) {
                maxVerticalScroll = maxVerticalScroll / 2;
                mScrollY += distanceY;
                if (mScrollY > maxVerticalScroll) {
                    mScrollY = maxVerticalScroll;
                }
                if (mScrollY < -maxVerticalScroll) {
                    mScrollY = -maxVerticalScroll;
                }
            }
            lp.setMargins(0, 0, mScrollX, mScrollY);
        }

        textureview.setLayoutParams(lp);
    }

    @Override
    public void onVideoSingleTapUp() {
        if (isCropping) {
            //裁剪过程中点击无效
            return;
        }
        if (playState == END_VIDEO) {
            playVideo();
        } else if (playState == PLAY_VIDEO) {
            pauseVideo();
        } else if (playState == PAUSE_VIDEO) {
            resumeVideo();
        }
    }


    /**
     * 裁剪结束
     */
    private void onCropComplete(AlivcCropOutputParam outputParam) {
        RxBus.getDefault().post(new SelectMediaSourcesEvent(outputParam.getOutputPath(),2));
        pop();
        //裁剪结束
//        Intent intent = getIntent();
//        intent.putExtra(AlivcCropOutputParam.RESULT_KEY_OUTPUT_PARAM, outputParam);
//        setResult(Activity.RESULT_OK, intent);
    }
    //开始剪辑
    private void startCrop() {

        if (!PermissionUtils.checkPermissionsGroup(getContext(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE})) {
            ToastUtils.showShort(PermissionUtils.NO_PERMISSION_TIP[4]);
            return;
        }


        if (frameWidth == 0 || frameHeight == 0) {
            ToastUtil.showToast(getContext(), R.string.alivc_crop_video_tip_crop_failed);
            isCropping = false;
            return;
        }
        if (isCropping) {
            return;
        }
        //开始裁剪时，暂停视频的播放,提高裁剪效率
        pauseVideo();
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textureview.getLayoutParams();
        int posX;
        int posY;
        int outputWidth = 0;
        int outputHeight = 0;
        int cropWidth;
        int cropHeight;
        outputPath = Constants.SDCardConstants.getDir(getContext()) + TimeUtils.getDateTimeFromMillisecond(System.currentTimeMillis()) + Constants.SDCardConstants.CROP_SUFFIX;
        float videoRatio = (float) videoHeight / videoWidth;
        float outputRatio = 1f;
        switch (ratioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                outputRatio = 1f;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                outputRatio = (float) 4 / 3;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                outputRatio = (float) 16 / 9;
                break;
            case RATIO_ORIGINAL:
                outputRatio = videoRatio;
                break;
            default:
                outputRatio = (float) 16 / 9;
                break;
        }
        if (videoRatio > outputRatio) {
            posX = 0;
            posY = ((lp.height - frameHeight) / 2 + mScrollY) * videoWidth / frameWidth;
            while (posY % 4 != 0) {
                posY++;
            }
            switch (resolutionMode) {
                case AliyunSnapVideoParam.RESOLUTION_360P:
                    outputWidth = 360;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_480P:
                    outputWidth = 480;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_540P:
                    outputWidth = 540;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_720P:
                    outputWidth = 720;
                    break;
                default:
                    outputWidth = 720;
                    break;
            }
            cropWidth = videoWidth;
            cropHeight = 0;
            switch (ratioMode) {
                case AliyunSnapVideoParam.RATIO_MODE_1_1:
                    cropHeight = videoWidth;
                    outputHeight = outputWidth;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_3_4:
                    cropHeight = videoWidth * 4 / 3;
                    outputHeight = outputWidth * 4 / 3;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_9_16:
                    cropHeight = videoWidth * 16 / 9;
                    outputHeight = outputWidth * 16 / 9;
                    break;
                default:
                    break;
            }
        } else if (videoRatio < outputRatio) {

            posX = ((lp.width - frameWidth) / 2 + mScrollX) * videoHeight / frameHeight;
            posY = 0;
            while (posX % 4 != 0) {
                posX++;
            }
            switch (resolutionMode) {
                case AliyunSnapVideoParam.RESOLUTION_360P:
                    outputWidth = 360;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_480P:
                    outputWidth = 480;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_540P:
                    outputWidth = 540;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_720P:
                    outputWidth = 720;
                    break;
                default:
                    outputWidth = 720;
                    break;
            }
            cropHeight = videoHeight;
            switch (ratioMode) {
                case AliyunSnapVideoParam.RATIO_MODE_1_1:
                    cropWidth = videoHeight;
                    outputHeight = outputWidth;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_3_4:
                    cropWidth = videoHeight * 3 / 4;
                    outputHeight = outputWidth * 4 / 3;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_9_16:
                    cropWidth = videoHeight * 9 / 16;
                    outputHeight = outputWidth * 16 / 9;
                    break;
                case RATIO_ORIGINAL:
                    cropWidth = (int) (videoHeight / videoRatio);
                    outputHeight = (int) (outputWidth * videoRatio);
                    break;
                default:
                    cropWidth = videoHeight * 9 / 16;
                    outputHeight = outputWidth * 16 / 9;
                    break;
            }
        } else {
            // 原比例或videoRatio = outputRatio执行else

            posX = 0;
            posY = 0;

            switch (resolutionMode) {
                case AliyunSnapVideoParam.RESOLUTION_360P:
                    outputWidth = 360;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_480P:
                    outputWidth = 480;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_540P:
                    outputWidth = 540;
                    break;
                case AliyunSnapVideoParam.RESOLUTION_720P:
                    outputWidth = 720;
                    break;
                default:
                    outputWidth = 720;
                    break;
            }
            cropHeight = videoHeight;
            cropWidth = videoHeight;
            outputHeight = outputWidth;
        }

        CropParam cropParam = new CropParam();
        cropParam.setOutputPath(outputPath);
        cropParam.setInputPath(mInputVideoPath);
        cropParam.setOutputWidth(outputWidth);
        cropParam.setOutputHeight(outputHeight);
        Rect cropRect = new Rect(posX, posY, posX + cropWidth, posY + cropHeight);
        cropParam.setCropRect(cropRect);
        cropParam.setStartTime(mStartTime * 1000);
        cropParam.setEndTime(mEndTime * 1000);
        cropParam.setScaleMode(cropMode);
        cropParam.setFrameRate(frameRate);
        cropParam.setGop(gop);
        cropParam.setQuality(quality);
        cropParam.setVideoCodec(mVideoCodec);
        cropParam.setFillColor(Color.BLACK);
        cropParam.setCrf(0);

        binding.aliyunCropProgressBg.setVisibility(View.VISIBLE);
        cropParam.setUseGPU(isUseGPU);
        crop.setCropParam(cropParam);



        int ret = crop.startCrop();
        if (ret < 0) {
            ToastUtil.showToast(getContext(), getString(R.string.alivc_crop_video_tip_crop_failed) + "  " + ret);
            return;
        }
        startCropTimestamp = System.currentTimeMillis();
        Log.d("CROP_COST", "start : " + startCropTimestamp);
        isCropping = true;
        binding.aliyunSeekBar.setSliceBlocked(true);


    }

    long startCropTimestamp;

    private void deleteFile() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                FileUtils.deleteFile(outputPath);
                return null;
            }
        } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 获取每个item取帧的时间值
     **/
    private void requestThumbItemTime() {
        int itemWidth = screenWidth / 10;

        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.addVideoSource(mInputVideoPath, 0, Integer.MAX_VALUE, 0);
        mThumbnailFetcher.setParameters(itemWidth, itemWidth, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 10);

        long duration = mThumbnailFetcher.getTotalDuration();
        long itemTime = duration / 10;
        for (int i = 1; i <= 10; i++) {
            requestFetchThumbnail(itemTime, i, 10);
        }

    }

    /**
     * 获取缩略图
     *
     * @param interval 取帧平均间隔
     * @param position 第几张
     * @param count    总共的张数
     */
    private void requestFetchThumbnail(final long interval, final int position, final int count) {
        long[] times = {(position - 1) * interval + interval / 2};

        Log.d(TAG, "requestThumbnailImage() times :" + times[0] + " ,position = " + position);
        mThumbnailFetcher.requestThumbnailImage(times, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
            private int vecIndex = 1;
            @Override
            public void onThumbnailReady(Bitmap frameBitmap, long l) {
                if (frameBitmap != null && !frameBitmap.isRecycled()) {
                    Log.i(TAG, "onThumbnailReady  put: " + position + " ,l = " + l / 1000);
                    SoftReference<Bitmap> bitmapSoftReference = new SoftReference<Bitmap>(frameBitmap);
                    adapter.add(bitmapSoftReference);
                } else {
                    if (position == 0) {
                        vecIndex = 1;
                    } else if (position == count + 1) {
                        vecIndex = -1;
                    }
                    int np = position + vecIndex;
                    Log.i(TAG, "requestThumbnailImage  failure: thisPosition = " + position + "newPosition = " + np);
                    requestFetchThumbnail(interval, np, count);
                }
            }

            @Override
            public void onError(int errorCode) {
                Log.w(TAG, "requestThumbnailImage error msg: " + errorCode);
            }
        });
    }
    public class CropCallbacks implements CropCallback{

        @Override
        public void onProgress(int percent) {
            binding.aliyunCropProgress.setProgress(percent);
        }

        @Override
        public void onError(int code) {
            Log.d(TAG, "crop failed : " + code);
            playHandler.post(new Runnable() {
                @Override
                public void run() {
                    binding.aliyunCropProgressBg.setVisibility(View.GONE);
                    binding.aliyunSeekBar.setSliceBlocked(false);
                    switch (code) {
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                            ToastUtil.showToast(getContext(), com.aliyun.svideo.crop.R.string.alivc_crop_video_tip_not_supported_video);
                            break;
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                            ToastUtil.showToast(getContext(), com.aliyun.svideo.crop.R.string.alivc_crop_video_tip_not_supported_audio);
                            break;
                        default:
                            ToastUtil.showToast(getContext(), com.aliyun.svideo.crop.R.string.alivc_crop_video_tip_crop_failed);
                            break;
                    }
//                progressDialog.dismiss();
                    //setResult(Activity.RESULT_CANCELED, getIntent());
                }
            });
            isCropping = false;
        }

        @Override
        public void onComplete(long duration) {
            long time = System.currentTimeMillis();
            Log.d(TAG, "completed : " + (time - startCropTimestamp));
            playHandler.post(new Runnable() {
                @Override
                public void run() {
                    binding.aliyunCropProgress.setVisibility(View.GONE);
                    binding.aliyunCropProgressBg.setVisibility(View.GONE);
                    binding.aliyunSeekBar.setSliceBlocked(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //适配android Q
                        com.aliyun.svideo.common.utils.ThreadUtils.runOnSubThread(new Runnable() {
                            @Override
                            public void run() {
                                UriUtils.saveVideoToMediaStore(getContext(), outputPath);
                            }
                        });
                    } else {
                        scanFile();
                    }
                    long duration = mEndTime - mStartTime;
                    AlivcCropOutputParam cropOutputParam = new AlivcCropOutputParam();
                    cropOutputParam.setOutputPath(outputPath);
                    cropOutputParam.setDuration(duration);
                    onCropComplete(cropOutputParam);
                }
            });
            isCropping = false;
            VideoInfoUtils.printVideoInfo(outputPath);
        }

        @Override
        public void onCancelComplete() {
            //取消完成
            playHandler.post(new Runnable() {
                @Override
                public void run() {
                    binding.aliyunCropProgressBg.setVisibility(View.GONE);
                    binding.aliyunSeekBar.setSliceBlocked(false);
                }
            });
            deleteFile();
            //setResult(Activity.RESULT_CANCELED);
            //finish();
            isCropping = false;
        }
    }
}
