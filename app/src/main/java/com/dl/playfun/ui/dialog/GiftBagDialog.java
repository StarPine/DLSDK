package com.dl.playfun.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.manager.ConfigManager;
import com.dl.playfun.ui.base.BaseDialog;
import com.dl.playfun.ui.dialog.adapter.GiftBagCardDetailAdapter;
import com.dl.playfun.ui.dialog.adapter.GiftBagRcvAdapter;
import com.dl.playfun.ui.dialog.adapter.GiftNumberSelectorAdapter;
import com.dl.playfun.widget.dialog.MessageDetailDialog;
import com.dl.playfun.R;
import com.zyyoona7.popup.EasyPopup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.goldze.mvvmhabit.utils.ConvertUtils;
import me.goldze.mvvmhabit.utils.RxUtils;

/**
 * Author: 彭石林
 * Time: 2021/12/7 12:06
 * Description: This is GiftBagDialog
 */
public class GiftBagDialog extends BaseDialog {
    private final Context mContext;
    private View rootView;
    //recyclerView礼物列表
    private RelativeLayout gift_page_layout;
    private GiftBagRcvAdapter giftBagRcvAdapter;
    private RecyclerView giftListPage;
    private LinearLayout indicatorLayout;
    //背包列表
    private RelativeLayout bag_page_layout;
    private RecyclerView bag_list_page;
    private GiftBagCardDetailAdapter giftBagCardDetailAdapter;
    private ImageView bag_empty_img;
    //水晶列表
    private RelativeLayout crystal_page_layout;
    private GiftBagRcvAdapter crystalGifAdapter;
    private RecyclerView crystalGifListPage;
    private LinearLayout crystalIndicatorLayout;
    //赠送按钮
    private Button btnSubmit;
    private Button crystalBtnSubmit;

    private GiftBagEntity.GiftEntity checkGiftItemEntity;
    private GiftBagEntity.GiftEntity checkCrystalItemEntity;

    private RecyclerView gift_check_number;
    private RecyclerView crystal_check_number;

    private TextView gift_number_text;
    private TextView crystal_number_text;

    private EasyPopup mCirclePop;//pupop弹窗

    private Integer sendGiftNumber = 1;//发送数量
    private Integer sendCrystalGiftNumber = 1;

    private TextView btn_stored;//储值按钮

    private ImageView balance_diamond;
    private ImageView balance_crystal;

    private TextView balance_value;//余额
    private TextView crystal_balance_value;

    private TextView tab_gift;//礼物tab
    private TextView tab_bag;//背包tab
    private TextView tab_crystal;//水晶tab

    private GiftOnClickListener giftOnClickListener;//点击事件回调
    private CardOnClickListener cardOnClickListener;//卡片点击事件回调
    //深色
    private final boolean isDark;

    private int maleBalance = 0;

    public AppRepository appRepository;
    //道具卡类型 1啪啪卡 2聊天卡 3语音卡 4視頻卡
    private int propType = 0;

    /**
     * 赠礼弹窗
     *
     * @param context 上下文
     * @param isDarkShow 是否暗黑模式
     * @param prop_type 道具卡类型
     */
    public GiftBagDialog(Context context, boolean isDarkShow, int prop_type) {
        super(context);
        this.mContext = context;
        this.isDark = isDarkShow;
        this.propType = prop_type;
        initView();
    }

    public void setBalanceValue(int balanceValue) {
        balance_value.post(() -> {
            balance_value.setText((maleBalance - balanceValue) + "");
            maleBalance = maleBalance - balanceValue;
        });
    }

    public void show() {
        //设置背景透明,去四个角
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(rootView);
        //设置宽度充满屏幕
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        rootView = inflater.inflate(R.layout.dialog_gift_bag, null);
        if (isDark) {
            LinearLayout container = rootView.findViewById(R.id.container);
            container.setBackground(mContext.getDrawable(R.drawable.dialog_gift_bag_backdrop2));
        }
        gift_page_layout = rootView.findViewById(R.id.gift_page_layout);//礼物layout
        bag_page_layout = rootView.findViewById(R.id.bag_page_layout);//背包layout
        crystal_page_layout = rootView.findViewById(R.id.crystal_page_layout);//水晶layout
        tab_gift = rootView.findViewById(R.id.tab_gift);
        tab_bag = rootView.findViewById(R.id.tab_bag);
        tab_crystal = rootView.findViewById(R.id.tab_crystal);
        giftListPage = rootView.findViewById(R.id.gift_list_page);
        crystalGifListPage = rootView.findViewById(R.id.crystal_list_page);
        btnSubmit = rootView.findViewById(R.id.btn_submit);
        crystalBtnSubmit = rootView.findViewById(R.id.crystal_btn_submit);
        indicatorLayout = rootView.findViewById(R.id.indicator_layout);
        crystalIndicatorLayout = rootView.findViewById(R.id.crystal_indicator_layout);
        gift_check_number = rootView.findViewById(R.id.gift_number_list);
        crystal_check_number = rootView.findViewById(R.id.crystal_number_list);
        gift_number_text = rootView.findViewById(R.id.gift_number_text);
        crystal_number_text = rootView.findViewById(R.id.crystal_number_text);

        balance_diamond = rootView.findViewById(R.id.iv_balance_diamond);
        balance_crystal = rootView.findViewById(R.id.iv_balance_crystal);

        btn_stored = rootView.findViewById(R.id.btn_stored);
        balance_value = rootView.findViewById(R.id.balance_value);
        crystal_balance_value = rootView.findViewById(R.id.crystal_balance_value);
        bag_list_page = rootView.findViewById(R.id.bag_list_page);

        bag_empty_img = rootView.findViewById(R.id.bag_empty_img);

        tab_bag.setOnClickListener(v -> {
            refreshTabColor(tab_bag);
            gift_page_layout.setVisibility(View.GONE);
            bag_page_layout.setVisibility(View.VISIBLE);
            crystal_page_layout.setVisibility(View.GONE);
            balance_diamond.setVisibility(View.VISIBLE);
            balance_crystal.setVisibility(View.GONE);
            balance_value.setVisibility(View.VISIBLE);
            crystal_balance_value.setVisibility(View.GONE);
        });
        tab_gift.setOnClickListener(v -> {
            refreshTabColor(tab_gift);
            gift_page_layout.setVisibility(View.VISIBLE);
            bag_page_layout.setVisibility(View.GONE);
            crystal_page_layout.setVisibility(View.GONE);
            balance_diamond.setVisibility(View.VISIBLE);
            balance_crystal.setVisibility(View.GONE);
            balance_value.setVisibility(View.VISIBLE);
            crystal_balance_value.setVisibility(View.GONE);
        });
        tab_crystal.setOnClickListener((v) -> {
            refreshTabColor(tab_crystal);
            gift_page_layout.setVisibility(View.GONE);
            bag_page_layout.setVisibility(View.GONE);
            crystal_page_layout.setVisibility(View.VISIBLE);
            balance_diamond.setVisibility(View.GONE);
            balance_crystal.setVisibility(View.VISIBLE);
            balance_value.setVisibility(View.GONE);
            crystal_balance_value.setVisibility(View.VISIBLE);

        });

        initSelectorView(gift_check_number, n -> sendGiftNumber = n);
        initSelectorView(crystal_check_number, n -> sendCrystalGiftNumber = n);

        if (isDark) {
            gift_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_9897B3));
            crystal_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_9897B3));
            gift_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector_night));
            crystal_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector_night));
        } else {
            gift_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_bfbfbf));
            crystal_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_bfbfbf));
            gift_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector));
            crystal_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector));
        }
        refreshTabColor(tab_gift);

        initGiftView(giftListPage, indicatorLayout);
        initGiftView(crystalGifListPage, crystalIndicatorLayout);
        //赠送按钮点击
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkGiftItemEntity == null) {
                    return;
                }
                if (giftOnClickListener != null) {
                    giftOnClickListener.sendGiftClick(GiftBagDialog.this, sendGiftNumber, checkGiftItemEntity);
                }
            }
        });
        crystalBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCrystalItemEntity == null) {
                    return;
                }
                if (giftOnClickListener != null) {
                    giftOnClickListener.sendGiftClick(GiftBagDialog.this, sendCrystalGiftNumber, checkCrystalItemEntity);
                }
            }
        });
        //充值按钮点击
        btn_stored.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (giftOnClickListener != null) {
                    giftOnClickListener.rechargeStored(GiftBagDialog.this);
                }
            }
        });
        initData();
    }

    private void refreshTabColor(TextView select) {
        TextView[] tabs = new TextView[] { tab_gift, tab_bag, tab_crystal };
        for (TextView tab: tabs) {
            if (tab == select) {
                tab.setTextColor(ColorUtils.getColor(R.color.purple1));
            } else {
                if (isDark) tab.setTextColor(ColorUtils.getColor(android.R.color.white));
                else tab.setTextColor(ColorUtils.getColor(R.color.color_text_333333));
            }
        }
    }

    private void initGiftView(RecyclerView view, LinearLayout indicatorLayout) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        view.setLayoutManager(linearLayoutManager);
        new PagerSnapHelper().attachToRecyclerView(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE: //滚动停止
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                        if (firstPosition == lastPosition) {
                            int childCount = indicatorLayout.getChildCount();
                            if (childCount > 0) {
                                for (int i = 0; i < childCount; i++) {
                                    if (i == firstPosition) {
                                        indicatorLayout.getChildAt(firstPosition).setBackground(ContextCompat.getDrawable(mContext, R.drawable.picture_num_oval));
                                    } else {
                                        indicatorLayout.getChildAt(i).setBackground(ContextCompat.getDrawable(mContext, R.drawable.picture_oval_focus_not));
                                    }
                                }
                            }
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING: //手指拖动
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING: //惯性滚动
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initSelectorView(RecyclerView view, GiftNumberSelectorAdapter.ItemOnClickListener listener) {
        List<Integer> numbers = Arrays.asList(1, 10, 38, 66, 188, 520, 1314, 3344);
        GiftNumberSelectorAdapter adapter = new GiftNumberSelectorAdapter(mContext, numbers, isDark, listener);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        view.setLayoutManager(manager);
        view.setAdapter(adapter);

        ViewOutlineProvider vop = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), ConvertUtils.dp2px(15));
            }
        };

        view.setOutlineProvider(vop);
        view.setClipToOutline(true);
    }

    private void initData() {
        appRepository = ConfigManager.getInstance().getAppRepository();
        appRepository.getBagGiftInfo()
                .doOnSubscribe(this)
                .compose(RxUtils.schedulersTransformer())
                .compose(RxUtils.exceptionTransformer())
                .subscribe(new BaseObserver<BaseDataResponse<GiftBagEntity>>() {
                    @Override
                    public void onSuccess(BaseDataResponse<GiftBagEntity> response) {
                        GiftBagEntity giftBagEntity = response.getData();
                        Integer isFirst = giftBagEntity.getIsFirst();
                        if (isFirst != null && isFirst == 1) {
                            btn_stored.setText(R.string.playfun_gift_bag_text1);
                        }
                        int totalCoin = giftBagEntity.getTotalCoin().intValue();
                        balance_value.setText(String.valueOf(totalCoin >= 0 ? totalCoin : 0));
                        double totalProfit = giftBagEntity.getTotalProfit();
                        String formatProfit;
                        if (totalProfit < 0) formatProfit = "0.00";
                        else if (totalProfit > 999999.99) formatProfit = "999999.99+";
                        else {
                            formatProfit = String.format(Locale.getDefault(), "%.2f", totalProfit);
                        }
                        crystal_balance_value.setText(formatProfit);
                        //礼物列表实现
                        List<GiftBagEntity.DiamondGiftEntity> giftEntities = giftBagEntity.getGift();
                        giftBagRcvAdapter = initGiftData(giftEntities, R.layout.dialog_gift_bag_item_detail, indicatorLayout);
                        giftListPage.setAdapter(giftBagRcvAdapter);
                        giftBagRcvAdapter.setOnClickListener(itemEntity -> {
                            if (checkGiftItemEntity == null || checkGiftItemEntity.getId().intValue() != itemEntity.getId().intValue()) {
                                checkGiftItemEntity = itemEntity;
                            }
                        });
                        //礼物列表实现
                        //背包实现
                        List<GiftBagEntity.propEntity> propEntity = giftBagEntity.getProp();
                        if (propEntity != null && propEntity.size() > 0) {
                            bag_empty_img.setVisibility(View.GONE);
                            bag_list_page.setVisibility(View.VISIBLE);
                            GridLayoutManager layoutManage = new GridLayoutManager(mContext, 5);
                            bag_list_page.setLayoutManager(layoutManage);
                            giftBagCardDetailAdapter = new GiftBagCardDetailAdapter(bag_list_page, propEntity, isDark, propType);
                            bag_list_page.setAdapter(giftBagCardDetailAdapter);
                            giftBagCardDetailAdapter.setOnClickListener(new GiftBagCardDetailAdapter.OnClickDetailListener() {
                                @Override
                                public void clickDetailCheck(int position, GiftBagEntity.propEntity itemEntity, LinearLayout detail_layout) {
                                    MessageDetailDialog.BgaCardDialog(mContext, itemEntity.getPropType(), itemEntity.getName(), itemEntity.getDesc())
                                            .show();
                                }
                            });
                        } else {
                            bag_list_page.setVisibility(View.GONE);
                            bag_empty_img.setVisibility(View.VISIBLE);
                        }
                        //背包实现
                        //水晶礼物实现
                        List<GiftBagEntity.CrystalGiftEntity> crystalEntities = giftBagEntity.getCrystal();
                        crystalGifAdapter = initGiftData(crystalEntities, R.layout.dialog_crystal_gift_bag_item_detail, crystalIndicatorLayout);
                        crystalGifListPage.setAdapter(crystalGifAdapter);
                        crystalGifAdapter.setOnClickListener(itemEntity -> {
                            if (checkCrystalItemEntity == null) {
                                checkCrystalItemEntity = itemEntity;
                            } else {
                                Integer oldItemId = checkCrystalItemEntity.getId();
                                Integer newItemId = itemEntity.getId();
                                if (oldItemId != null && newItemId != null && newItemId.intValue() != oldItemId.intValue()) {
                                    checkCrystalItemEntity = itemEntity;
                                }
                            }
                        });
                    }
                });
    }

    private GiftBagRcvAdapter initGiftData(List<? extends GiftBagEntity.GiftEntity> entities, int layoutRes, LinearLayout indicatorLayout) {
        int crystalSize = entities.size();
        List<GiftBagEntity.GiftEntity> adapterItemList = new ArrayList<>();
        List<List<GiftBagEntity.GiftEntity>> adapterList = new ArrayList<>();
        if (!entities.isEmpty()) entities.get(0).setFirst(true);

        for (int i = 0; i < crystalSize; i++) {
            adapterItemList.add(entities.get(i));
            if (adapterItemList.size() == 10 || i == crystalSize - 1) {
                adapterList.add(adapterItemList);
                adapterItemList = new ArrayList<>();
            }
        }
        int listGiftSize = adapterList.size();
        for (int i = 0; i < listGiftSize; i++) {
            View view = new View(mContext);
            if (i == 0) {
                view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.picture_num_oval));
            } else {
                view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.picture_oval_focus_not));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(6), dp2px(6));
            params.topMargin = dp2px(5);
            params.leftMargin = dp2px(5);
            view.setLayoutParams(params);
            indicatorLayout.addView(view);
        }
        return new GiftBagRcvAdapter(mContext, adapterList, layoutRes, isDark);
    }

    /***
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     * @param dpValue
     * @return
     */
    public int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setGiftOnClickListener(GiftOnClickListener clickListener) {
        this.giftOnClickListener = clickListener;
    }

    public interface GiftOnClickListener {

        /**
         * 赠礼回调
         *
         * @param dialog 弹窗本身
         * @param number 礼物数量
         * @param giftEntity 所选礼物
         */
        void sendGiftClick(Dialog dialog, int number, GiftBagEntity.GiftEntity giftEntity);

        /** 钻石充值 */
        void rechargeStored(Dialog dialog);
    }

    public void setCardOnClickListener(CardOnClickListener cardOnClickListener) {

    }

    public interface CardOnClickListener {
        void onClick(Dialog dialog, int type);
    }
}
