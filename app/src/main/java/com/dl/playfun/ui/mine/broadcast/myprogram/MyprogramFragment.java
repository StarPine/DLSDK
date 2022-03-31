package com.dl.playfun.ui.mine.broadcast.myprogram;

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

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.ThemeItemEntity;
import com.dl.playfun.helper.DialogHelper;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseRefreshFragment;
import com.dl.playfun.utils.PictureSelectorUtil;
import com.dl.playfun.widget.dialog.MVDialog;
import com.google.gson.reflect.TypeToken;
import com.dl.playfun.BR;
import com.dl.playfun.R;
import com.dl.playfun.databinding.FragmentMyProgramBinding;
import com.dl.playfun.ui.dialog.ProgramSubjectChooseDialog;
import com.dl.playfun.ui.program.searchprogramsite.SearchProgramSiteFragment;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * @author litchi
 */
public class MyprogramFragment extends BaseRefreshFragment<FragmentMyProgramBinding, MyprogramViewModel> {

    private EasyPopup mCirclePop;

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_my_program;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public MyprogramViewModel initViewModel() {
        AppViewModelFactory factory = AppViewModelFactory.getInstance(mActivity.getApplication());
        return ViewModelProviders.of(this, factory).get(MyprogramViewModel.class);
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
    public void initData() {
        super.initData();
        binding.rcyTrend.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        if(GSYVideoManager.isFullState(MyprogramFragment.this.mActivity)) {
                            return;
                        }
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        try {
                            GSYVideoManager.releaseAllVideos();
                        } catch (Exception e) {

                        }
                        viewModel.adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            GSYVideoManager.releaseAllVideos();
        } catch (Exception e) {

        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.uc.clickMore.observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                mCirclePop = EasyPopup.create()
                        .setContentView(MyprogramFragment.this.getContext(), R.layout.more_item)
//                        .setAnimationStyle(R.style.RightPopAnim)
                        //是否允许点击PopupWindow之外的地方消失
                        .setFocusAndOutsideEnable(true)
                        .setDimValue(0)
                        .setWidth(350)
                        .apply();

                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rcyTrend.getLayoutManager();
                final View child = layoutManager.findViewByPosition((Integer) o);
                if (child != null) {
                    mCirclePop.showAtAnchorView(child.findViewById(R.id.iv_more), YGravity.BELOW, XGravity.ALIGN_RIGHT, 0, 0);
                }
                TextView stop = mCirclePop.findViewById(R.id.tv_stop);
                stop.setText(viewModel.observableList.get((Integer) o).topicalListEntityObservableField.get().getBroadcast().getIsComment() == 0 ? getString(R.string.playfun_fragment_issuance_program_no_comment) : getString(R.string.playfun_open_comment));
                TextView tvDetele = mCirclePop.findViewById(R.id.tv_detele);
                tvDetele.setText(getString(R.string.playfun_delete_program));
                stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.setComment((Integer) o);
                        mCirclePop.dismiss();
                    }
                });
                mCirclePop.findViewById(R.id.tv_detele).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MVDialog.getInstance(MyprogramFragment.this.getContext())
                                .setContent(getString(R.string.playfun_confirm_delete_program))
                                .chooseType(MVDialog.TypeEnum.CENTER)
                                .setConfirmOnlick(new MVDialog.ConfirmOnclick() {
                                    @Override
                                    public void confirm(MVDialog dialog) {
                                        viewModel.deleteTopical((Integer) o);
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
                    ToastUtils.showShort(R.string.playfun_already);
                }
            }
        });
        viewModel.uc.clickComment.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                if (viewModel.isVip || (viewModel.sex == 0 && viewModel.certification == 1)) {
                    MVDialog.getInstance(MyprogramFragment.this.getContext())
                            .seCommentConfirm((dialog, comment) -> {
                                if (StringUtils.isEmpty(comment)) {
                                    ToastUtils.showShort(R.string.playfun_warn_input_comment);
                                    return;
                                }
                                dialog.dismiss();
                                String id = ((Map<String, String>) o).get("id");
                                String toUserId = ((Map<String, String>) o).get("toUseriD");
                                String toUserName = ((Map<String, String>) o).get("toUserName");
                                viewModel.topicalComment(Integer.valueOf(id), comment, toUserId != null ? Integer.valueOf(toUserId) : null, toUserName);
                            })
                            .chooseType(MVDialog.TypeEnum.BOTTOMCOMMENT)
                            .show();
                } else {
                    DialogHelper.showNotVipCommentDialog(MyprogramFragment.this);
                }
            }
        });

        viewModel.uc.clickSignUp.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                MVDialog.getInstance(MyprogramFragment.this.getContext())
                        .setContent(getString(R.string.playfun_end_porgram))
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
        viewModel.uc.clickImage.observe(this, new Observer() {
            @Override
            public void onChanged(Object o) {
                Integer position = Integer.valueOf(((Map<String, String>) o).get("position"));
                String listStr = ((Map<String, String>) o).get("images");
                List<String> images = GsonUtils.fromJson(listStr, new TypeToken<List<String>>() {
                }.getType());
                PictureSelectorUtil.previewImage(MyprogramFragment.this.getContext(), images, position);
            }
        });
        viewModel.uc.clickPublish.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                List<ThemeItemEntity> themes = new ArrayList<>();
                for (ConfigItemEntity configItemEntity : ConfigManager.getInstance().getAppRepository().readThemeConfig()) {
                    ThemeItemEntity themeItemEntity = new ThemeItemEntity();
                    themeItemEntity.setIcon(configItemEntity.getIcon());
                    themeItemEntity.setId(configItemEntity.getId());
                    themeItemEntity.setTitle(configItemEntity.getName());
                    themes.add(themeItemEntity);
                }
                ProgramSubjectChooseDialog dialog = new ProgramSubjectChooseDialog(themes);
                dialog.show(getChildFragmentManager(), ProgramSubjectChooseDialog.class.getCanonicalName());
                dialog.setProgramSubjectChooseDialogListener((dialog1, itemEntity) -> {
                    dialog1.dismiss();
                    Bundle bundle = SearchProgramSiteFragment.getStartBundle(itemEntity);
                    viewModel.start(SearchProgramSiteFragment.class.getCanonicalName(), bundle);
                });
            }
        });
    }

}
