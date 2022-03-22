package com.dl.playfun.ui.radio.programlist;

import static com.dl.playfun.app.AppConfig.FEMALE;
import static com.dl.playfun.app.AppConfig.MALE;
import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_TYPE;
import static com.dl.playfun.ui.userdetail.report.ReportUserFragment.ARG_REPORT_USER_ID;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.app.Injection;
import com.dl.playfun.databinding.FragmentProgramListBinding;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.helper.DialogHelper;
import com.dl.playfun.ui.base.BaseRefreshToolbarFragment;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleFragment;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleFragment;
import com.dl.playfun.ui.userdetail.report.ReportUserFragment;
import com.dl.playfun.utils.ImmersionBarUtils;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.utils.StringUtil;
import com.dl.playfun.widget.AppBarStateChangeListener;
import com.dl.playfun.widget.RadioFilterView;
import com.dl.playfun.widget.dialog.MVDialog;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.utils.ToastUtils;
import me.jessyan.autosize.internal.CustomAdapt;

/**
 * 节目列表
 *
 * @author wulei
 */
public class ProgramListFragment extends BaseRefreshToolbarFragment<FragmentProgramListBinding, ProgramListViewModel> implements CustomAdapt, RadioFilterView.RadioFilterListener {
    public static final String ARG_ID = "arg_id";
    public static final String ARG_THEME_ID = "arg_theme_id";
    public static final String ARG_THEME_NAME = "arg_theme_name";
    public static final String ARG_KEY_WORD = "arg_key_word";

    private Integer themeIds;
    private Integer themeId;
    private String themeName;
    private String keyword;
    private Context mContext;
    private EasyPopup mCirclePop;

    private final int toolbarHeight = -1;
    private final boolean toolbarUp = false;
    private String getHeadImg = null;


    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        ImmersionBarUtils.setupStatusBar(this, true, true);
    }

    public static Bundle getStartBundle(int id, int themeId, String themeName, String keyword) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_ID, id);
        bundle.putInt(ARG_THEME_ID, themeId);
        bundle.putString(ARG_THEME_NAME, themeName);
        bundle.putString(ARG_KEY_WORD, keyword);
        return bundle;
    }

    @Override
    public void initParam() {
        super.initParam();
        themeIds = getArguments().getInt(ARG_ID);
        themeId = getArguments().getInt(ARG_THEME_ID);
        themeName = getArguments().getString(ARG_THEME_NAME);
        keyword = getArguments().getString(ARG_KEY_WORD);
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_program_list;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ProgramListViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        ProgramListViewModel viewModel = ViewModelProviders.of(this, factory).get(ProgramListViewModel.class);
        viewModel.themeId = this.themeId;
        viewModel.themeName = this.themeName;
        viewModel.keyword = this.keyword;
        return viewModel;
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        mContext = this.getContext();
        //加载主题详情
        viewModel.getThemeDetail(themeIds);
        //加载主题详情背景图片
        viewModel.uc.loadThemeDetailSrc.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                String url_conver = viewModel.themeItemEntity.get().cover;
                if (!StringUtil.isEmpty(url_conver)) {
//                    Glide.with(mContext)
//                            .asBitmap()
//                            .load(StringUtil.getFullImageUrl(url_conver))
//                            .error(R.drawable.radio_program_list_content)
//                            .placeholder(R.drawable.radio_program_list_content)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(new SimpleTarget<Bitmap>() {
//                                @Override
//                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//                                    Drawable drawable = new BitmapDrawable(resource);
//                                    binding.themeDetailSrc.setBackground(drawable);
//                                }
//
//                            });
                    Glide.with(mContext).load(StringUtil.getFullImageUrl(url_conver))
                            .error(R.drawable.radio_program_list_content)
                            .placeholder(R.drawable.radio_program_list_content)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.themeDetailSrc);
                }
                String topToolIcon = viewModel.themeItemEntity.get().topToolIcon;
                if (!StringUtil.isEmpty(topToolIcon)) {
                    getHeadImg = topToolIcon;
                    Glide.with(mContext).load(StringUtil.getFullImageUrl(topToolIcon))
                            .error(R.drawable.radio_program_list_title)
                            .placeholder(R.drawable.radio_program_list_title)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.programTitle1);
                } else {
                        binding.programTitle1.setImageResource(R.drawable.radio_program_list_title);
                }

            }
        });
        viewModel.uc.clickMore.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                Integer position = Integer.valueOf(((Map<String, String>) o).get("position"));
                Integer broadcastId = Integer.valueOf(((Map<String, String>) o).get("broadcastId"));
                mCirclePop = EasyPopup.create()
                        .setContentView(ProgramListFragment.this.getContext(), R.layout.more_item)
//                        .setAnimationStyle(R.style.RightPopAnim)
                        //是否允许点击PopupWindow之外的地方消失
                        .setFocusAndOutsideEnable(true)
                        .setDimValue(0)
                        .setWidth(350)
                        .apply();


                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rcvProgram.getLayoutManager();
                final View child = layoutManager.findViewByPosition(position);
                if (child != null) {
                    mCirclePop.showAtAnchorView(child.findViewById(R.id.iv_more), YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 0);
                }
                TextView stop = mCirclePop.findViewById(R.id.tv_stop);
                boolean isSelf = false;
                if (viewModel.userId == viewModel.observableList.get(position).topicalListEntityObservableField.get().getUserId()) {
                    stop.setText(viewModel.observableList.get(position).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? getString(R.string.fragment_issuance_program_no_comment) : getString(R.string.open_comment));
                    TextView tvDetele = mCirclePop.findViewById(R.id.tv_detele);
                    tvDetele.setText(getString(R.string.delete_program));
                    isSelf = true;
                } else {
                    mCirclePop.findViewById(R.id.tv_detele).setVisibility(View.GONE);
                    stop.setText(getString(R.string.report_user_title));
                    isSelf = false;
                }

                boolean finalIsSelf = isSelf;
                stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (finalIsSelf) {
                            viewModel.setComment(position);
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString(ARG_REPORT_TYPE, "broadcast");
                            bundle.putInt(ARG_REPORT_USER_ID, broadcastId);
                            startContainerActivity(ReportUserFragment.class.getCanonicalName(), bundle);
                        }
                        mCirclePop.dismiss();
                    }
                });
                mCirclePop.findViewById(R.id.tv_detele).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MVDialog.getInstance(ProgramListFragment.this.getContext())
                                .setContent(getString(R.string.confirm_delete_program))
                                .chooseType(MVDialog.TypeEnum.CENTER)
                                .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                    @Override
                                    public void confirm(MVDialog dialog) {
                                        viewModel.deleteTopical(position);
                                        dialog.dismiss();
                                    }
                                })
                                .chooseType(MVDialog.TypeEnum.CENTER)
                                .show();
                        mCirclePop.dismiss();
                    }
                });

            }
        });

        viewModel.uc.clickLike.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                if (viewModel.observableList.get((Integer) o).topicalListEntityObservableField.get().getIsGive() == 0) {
                    viewModel.topicalGive((Integer) o);
                } else {
                    ToastUtils.showShort(R.string.already);
                }
            }
        });
        viewModel.uc.clickComment.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                if (viewModel.isVip || (viewModel.sex == 0 && viewModel.certification == 1)) {
                    MVDialog.getInstance(ProgramListFragment.this.getContext())
                            .seCommentConfirm(new MVDialog.ConfirmComment() {
                                @Override
                                public void clickListItem(Dialog dialog, String comment) {
                                    if (StringUtils.isEmpty(comment)) {
                                        ToastUtils.showShort(R.string.warn_input_comment);
                                        return;
                                    }
                                    dialog.dismiss();
                                    String id = ((Map<String, String>) o).get("id");
                                    String toUserId = ((Map<String, String>) o).get("toUseriD");
                                    String toUserName = ((Map<String, String>) o).get("toUserName");
                                    viewModel.topicalComment(Integer.valueOf(id), comment, toUserId != null ? Integer.valueOf(toUserId) : null, toUserName);
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.BOTTOMCOMMENT)
                            .show();
                } else {
                    DialogHelper.showNotVipCommentDialog(ProgramListFragment.this);
                }

            }
        });

        viewModel.uc.clickSignUp.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(ProgramListFragment.this.getContext())
                        .setContent(getString(R.string.end_porgram))
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                viewModel.topicalFinish((Integer) o);
                                dialog.dismiss();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });

        viewModel.uc.clickCheck.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                viewModel.initUserDate();
                if (viewModel.certification == 1) {
                    MVDialog.getInstance(ProgramListFragment.this.getContext())
                            .setTitle(getString(R.string.report_send_photo_titile))
                            .setConfirmText(getString(R.string.dialog_set_withdraw_account_confirm))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    chooseAvatar((Integer) o);
                                    dialog.dismiss();
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .show();
                } else {
                    MVDialog.getInstance(ProgramListFragment.this.getContext())
                            .setTitle(getString(R.string.authentication_free_sign_up))
                            .setConfirmText(getString(R.string.mine_once_certification))
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                @Override
                                public void confirm(MVDialog dialog) {
                                    if (viewModel.sex == MALE) {
                                        viewModel.start(CertificationMaleFragment.class.getCanonicalName());
                                        return;
                                    } else if (viewModel.sex == FEMALE) {
                                        viewModel.start(CertificationFemaleFragment.class.getCanonicalName());
                                        return;
                                    }
                                    com.blankj.utilcode.util.ToastUtils.showShort(R.string.sex_unknown);
                                    dialog.dismiss();
                                }
                            })
                            .chooseType(MVDialog.TypeEnum.CENTER)
                            .show();
                }
            }
        });
        viewModel.uc.signUpSucceed.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(ProgramListFragment.this.getContext())
                        .setContent(getString(R.string.sign_up_after_call_you))
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .setConfirmText(getString(R.string.roger))
                        .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                            @Override
                            public void confirm(MVDialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .chooseType(MVDialog.TypeEnum.CENTER)
                        .show();
            }
        });

        viewModel.uc.clickImage.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                Integer position = Integer.valueOf(((Map<String, String>) o).get("position"));
                String listStr = ((Map<String, String>) o).get("images");
                List<String> images = GsonUtils.fromJson(listStr, new TypeToken<List<String>>() {
                }.getType());
                PictureSelectorUtil.previewImage(ProgramListFragment.this.getContext(), images, position);
            }
        });

    }


    private void chooseAvatar(int position) {
        PictureSelectorUtil.selectImageAndCrop(mActivity, true, 1, 1, new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {
                viewModel.imagUpload(result.get(0).getCutPath(), position);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        setTitleBarTitle(themeName);

        List<RadioFilterView.RadioFilterItemEntity> times = new ArrayList<>();
        times.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.issuance_time), 1));
        times.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.activity_time), 2));
//        times.add(new RadioFilterView.RadioFilterItemEntity<>("最近距离", 3));

        List<RadioFilterView.RadioFilterItemEntity> sexs = new ArrayList<>();
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.any_gender), null));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.just_look_lady), 0));
        sexs.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.just_look_man), 1));

        List<ConfigItemEntity> citys = Injection.provideDemoRepository().readCityConfig();
        List<RadioFilterView.RadioFilterItemEntity> regions = new ArrayList<>();
        regions.add(new RadioFilterView.RadioFilterItemEntity<>(getString(R.string.any_area), null));
        for (ConfigItemEntity city : citys) {
            regions.add(new RadioFilterView.RadioFilterItemEntity<>(city.getName(), city));
        }
        binding.titleText.setText(themeName);
        binding.radioFilterView.setFilterData(times, sexs, regions);
        binding.radioFilterView.setRadioFilterListener(this);
        //通过CollapsingToolbarLayout修改字体颜色
        binding.collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);//设置还没收缩时状态下字体颜色
        binding.collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);//设置收缩后Toolbar上字体的颜色
        //给页面设置工具栏
        if (binding.collapsingToolbarLayout != null) {
            //设置隐藏图片时候ToolBar的颜色
            binding.collapsingToolbarLayout.setContentScrimColor(ColorUtils.getColor(R.color.white));
        }
        binding.appbarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                if (state == State.EXPANDED) {
                    //展开状态
                    binding.titleText.setVisibility(View.INVISIBLE);
                } else if (state == State.COLLAPSED) {
                    //折叠状态
                    binding.titleText.setVisibility(View.VISIBLE);
                } else {
                    //中间状态
//                    Toast.makeText(getActivity(),"中间状态",Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rcvProgram.setOnScrollListener(new RecyclerView.OnScrollListener() {
            final boolean scrollState = false;
            public int firstVisibleItem, lastVisibleItem, visibleCount;
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                switch (newState) {
//                    case RecyclerView.SCROLL_STATE_IDLE: //滚动停止
//                        scrollState = false;
//                        autoPlayVideo(recyclerView);
//                        break;
//                    case RecyclerView.SCROLL_STATE_DRAGGING: //手指拖动
//                        scrollState = true;
//                        break;
//                    case RecyclerView.SCROLL_STATE_SETTLING: //惯性滚动
//                        scrollState = true;
//                        break;
//                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                visibleCount = lastVisibleItem - firstVisibleItem;

                //大于0说明有播放
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    //当前播放的位置
                    int position = GSYVideoManager.instance().getPlayPosition();
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().getPlayTag().equals("SampleCoverVideoPlayer")
                            && (position < firstVisibleItem || position > lastVisibleItem)) {
                        if(GSYVideoManager.isFullState(ProgramListFragment.this.mActivity)) {
                            return;
                        }
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        GSYVideoManager.releaseAllVideos();
                        viewModel.adapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }
    @Override
    public void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }
    }

    /**
     * @return void
     * @Desc TODO(页面再次进入)
     * @author 彭石林
     * @parame [hidden]
     * @Date 2021/8/4
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
        }else {
            try {
                GSYVideoManager.releaseAllVideos();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onPublishTimeSelected(RadioFilterView radioFilterView, int position, RadioFilterView.RadioFilterItemEntity obj) {
        viewModel.setType((Integer) obj.getData());
    }

    @Override
    public void onSexSelected(RadioFilterView radioFilterView, int position, RadioFilterView.RadioFilterItemEntity obj) {
        if (obj.getData() == null) {
            viewModel.setSexId(null);
        } else {
            viewModel.setSexId((Integer) obj.getData());
        }
    }

    @Override
    public void onRegionSelected(RadioFilterView radioFilterView, int position, RadioFilterView.RadioFilterItemEntity obj) {
        if (obj.getData() == null) {
            viewModel.setCityId(null);
        } else {
            viewModel.setCityId(((ConfigItemEntity) obj.getData()).getId());
        }
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 360;
    }
}
