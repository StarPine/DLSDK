package com.dl.playfun.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Outline;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.dl.playfun.data.AppRepository;
import com.dl.playfun.data.source.http.observer.BaseObserver;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.entity.GiftBagAdapterEntity;
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
    //背包列表
    private RelativeLayout bag_page_layout;
    private RecyclerView bag_list_page;
    private GiftBagCardDetailAdapter giftBagCardDetailAdapter;
    private LinearLayout indicatorLayout;
    private ImageView bag_empty_img;
    //水晶列表
    private RelativeLayout crystal_page_layout;
    private GiftBagRcvAdapter crystalGifAdapter;
    private RecyclerView crystalGifListPage;
    private LinearLayout crystalIndicatorLayout;
    //赠送按钮
    private Button btnSubmit;
    private Button crystalBtnSubmit;

    private LinearLayout checkGiftLineLayout;

    private LinearLayout checkCrystalLineLayout;

    private GiftBagEntity.GiftEntity checkGiftItemEntity;
    private GiftBagEntity.GiftEntity checkCrystalItemEntity;

    private GiftNumberSelectorAdapter.GiftNumberSelectorViewHolder checkGiftNumber;
    private GiftNumberSelectorAdapter.GiftNumberSelectorViewHolder checkCrystalGIftNumber;

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

    public GiftBagDialog(Context context, boolean isDarkShow, int balance_value, int prop_type) {
        super(context);
        this.mContext = context;
        this.isDark = isDarkShow;
        this.maleBalance = balance_value;
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

        tab_bag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab_bag.setTextColor(ColorUtils.getColor(R.color.purple1));
                tab_gift.setTextColor(ColorUtils.getColor(R.color.empty_list_hint));
                tab_crystal.setTextColor(ColorUtils.getColor(R.color.empty_list_hint));
                gift_page_layout.setVisibility(View.GONE);
                bag_page_layout.setVisibility(View.VISIBLE);
                crystal_page_layout.setVisibility(View.GONE);
                balance_diamond.setVisibility(View.VISIBLE);
                balance_crystal.setVisibility(View.GONE);
                balance_value.setVisibility(View.VISIBLE);
                crystal_balance_value.setVisibility(View.GONE);
            }
        });
        tab_gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tab_bag.setTextColor(ColorUtils.getColor(R.color.empty_list_hint));
                tab_gift.setTextColor(ColorUtils.getColor(R.color.purple1));
                tab_crystal.setTextColor(ColorUtils.getColor(R.color.empty_list_hint));
                gift_page_layout.setVisibility(View.VISIBLE);
                bag_page_layout.setVisibility(View.GONE);
                crystal_page_layout.setVisibility(View.GONE);
                balance_diamond.setVisibility(View.VISIBLE);
                balance_crystal.setVisibility(View.GONE);
                balance_value.setVisibility(View.VISIBLE);
                crystal_balance_value.setVisibility(View.GONE);
            }
        });
        tab_crystal.setOnClickListener((v) -> {
            tab_bag.setTextColor(ColorUtils.getColor(R.color.empty_list_hint));
            tab_gift.setTextColor(ColorUtils.getColor(R.color.empty_list_hint));
            tab_crystal.setTextColor(ColorUtils.getColor(R.color.purple1));
            gift_page_layout.setVisibility(View.GONE);
            bag_page_layout.setVisibility(View.GONE);
            crystal_page_layout.setVisibility(View.VISIBLE);
            balance_diamond.setVisibility(View.GONE);
            balance_crystal.setVisibility(View.VISIBLE);
            balance_value.setVisibility(View.GONE);
            crystal_balance_value.setVisibility(View.VISIBLE);

        });

        List<Integer> numbers = Arrays.asList(new Integer[]{1, 10, 38, 66, 188, 520, 1314, 3344});
        GiftNumberSelectorAdapter adapter = new GiftNumberSelectorAdapter();
        adapter.setGiftNumbers(numbers);
        adapter.setListener((holder) -> {
            Context context = holder.root.getContext();
            if (checkGiftNumber != null) {
                checkGiftNumber.root.setBackground(null);
                checkGiftNumber.item.setTextColor(ContextCompat.getColor(context, R.color.color_text_333333));
            }
            sendGiftNumber = Integer.valueOf(holder.item.getText().toString());
            checkGiftNumber = holder;
            holder.root.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_bg_gift_number_selector_item));
            holder.item.setTextColor(ContextCompat.getColor(context, R.color.color_bg_gift_number_selector_item));
        });
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        gift_check_number.setLayoutManager(manager);
        gift_check_number.setAdapter(adapter);

        ViewOutlineProvider vop = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), ConvertUtils.dp2px(100));
            }
        };

        gift_check_number.setOutlineProvider(vop);
        gift_check_number.setClipToOutline(true);

        adapter = new GiftNumberSelectorAdapter();
        adapter.setGiftNumbers(numbers);
        adapter.setListener((holder -> {
            Context context = holder.root.getContext();
            if (checkCrystalGIftNumber != null) {
                checkCrystalGIftNumber.root.setBackground(null);
                checkCrystalGIftNumber.item.setTextColor(ContextCompat.getColor(context, R.color.color_text_333333));
            }
            sendCrystalGiftNumber = Integer.valueOf(holder.item.getText().toString());
            checkCrystalGIftNumber = holder;
            holder.root.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_bg_gift_number_selector_item));
            holder.item.setTextColor(ContextCompat.getColor(context, R.color.color_bg_gift_number_selector_item));
        }));
        manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        crystal_check_number.setLayoutManager(manager);
        crystal_check_number.setAdapter(adapter);
        crystal_check_number.setOutlineProvider(vop);
        crystal_check_number.setClipToOutline(true);

        if (isDark) {
            gift_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_9897B3));
            crystal_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_9897B3));
            gift_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector_night));
            crystal_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector_night));
        } else {
            gift_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_333333));
            crystal_number_text.setTextColor(ContextCompat.getColor(mContext, R.color.color_text_333333));
            gift_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector));
            crystal_check_number.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_bg_gift_number_selector));
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        giftListPage.setLayoutManager(linearLayoutManager);
        new LinearSnapHelper().attachToRecyclerView(giftListPage);

        giftListPage.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                        indicatorLayout.getChildAt(firstPosition).setBackground(mContext.getResources().getDrawable(R.drawable.picture_num_oval));
                                    } else {
                                        indicatorLayout.getChildAt(i).setBackground(mContext.getResources().getDrawable(R.drawable.picture_oval_focus_not));
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
        LinearLayoutManager CrystalLinearLayoutManager = new LinearLayoutManager(rootView.getContext());
        CrystalLinearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        crystalGifListPage.setLayoutManager(CrystalLinearLayoutManager);
        new LinearSnapHelper().attachToRecyclerView(crystalGifListPage);

        crystalGifListPage.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE: //滚动停止
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        int firstPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                        if (firstPosition == lastPosition) {
                            int childCount = crystalIndicatorLayout.getChildCount();
                            if (childCount > 0) {
                                for (int i = 0; i < childCount; i++) {
                                    if (i == firstPosition) {
                                        crystalIndicatorLayout.getChildAt(firstPosition).setBackground(mContext.getResources().getDrawable(R.drawable.picture_num_oval));
                                    } else {
                                        crystalIndicatorLayout.getChildAt(i).setBackground(mContext.getResources().getDrawable(R.drawable.picture_oval_focus_not));
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
                            btn_stored.setBackground(mContext.getDrawable(R.drawable.gift_red_border_backdrop));
                            btn_stored.setTextColor(ColorUtils.getColor(R.color.red));
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
                        List<GiftBagEntity.DiamondGiftEntity> listGifEntity = giftBagEntity.getGift();
                        int gifSize = listGifEntity.size();
                        List<GiftBagEntity.DiamondGiftEntity> $listData = new ArrayList<>();
                        List<GiftBagAdapterEntity> listGiftAdapter = new ArrayList<>();
                        if (!listGifEntity.isEmpty()) listGifEntity.get(0).setFirst(true);
                        if (gifSize / 10 > 0) {
                            int cnt = 0;
                            int idx = 1;
                            for (int i = 0; i < gifSize; i++) {
                                if (idx / 10 > 0) {
                                    $listData.add(listGifEntity.get(i));
                                    listGiftAdapter.add(cnt, new GiftBagAdapterEntity(cnt, $listData));
                                    $listData = new ArrayList<>();
                                    cnt++;
                                    idx = 1;
                                } else {
                                    idx++;
                                    $listData.add(listGifEntity.get(i));
                                    if (i == (gifSize - 1)) {
                                        listGiftAdapter.add(cnt, new GiftBagAdapterEntity(cnt, $listData));
                                    }
                                }
                            }
                        } else {
                            $listData.addAll(listGifEntity);
                            if ($listData.size() > 0) {
                                GiftBagAdapterEntity giftBagAdapterEntity = new GiftBagAdapterEntity(0, $listData);
                                listGiftAdapter.add(giftBagAdapterEntity);
                            }
                        }
                        giftBagRcvAdapter = new GiftBagRcvAdapter(giftListPage, listGiftAdapter, R.layout.dialog_gift_bag_item_detail, isDark);
                        giftListPage.setAdapter(giftBagRcvAdapter);
                        if (listGiftAdapter.size() > 0) {
                            int listGiftSize = listGiftAdapter.size();
                            for (int i = 0; i < listGiftSize; i++) {
                                View view = new View(mContext);
                                if (i == 0) {
                                    view.setBackground(mContext.getResources().getDrawable(R.drawable.picture_num_oval));
                                } else {
                                    view.setBackground(mContext.getResources().getDrawable(R.drawable.picture_oval_focus_not));
                                }
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(6), dp2px(6));
                                params.topMargin = dp2px(5);
                                params.leftMargin = dp2px(5);
                                view.setLayoutParams(params);
                                indicatorLayout.addView(view);
                                Log.d("GIFT_DIALOG", "ADD");
                            }
                        }
                        giftBagRcvAdapter.setOnClickListener((position, itemEntity, detail_layout, rcvPosition) -> {
                            if (checkGiftItemEntity != null) {
                                if (checkGiftItemEntity.getId().intValue() == itemEntity.getId().intValue()) {
                                    return;
                                } else {
                                    checkGiftItemEntity = itemEntity;
                                    if (checkGiftLineLayout != null)
                                        checkGiftLineLayout.setBackgroundDrawable(null);
                                    detail_layout.setBackground(mContext.getDrawable(R.drawable.purple_gift_checked));
                                    checkGiftLineLayout = detail_layout;
                                }
                            } else {
                                checkGiftItemEntity = itemEntity;
                                checkGiftLineLayout = detail_layout;
                                detail_layout.setBackground(mContext.getDrawable(R.drawable.purple_gift_checked));
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
                        List<GiftBagEntity.CrystalGiftEntity> listCrystalEntity = giftBagEntity.getCrystal();
                        int crystalSize = listCrystalEntity.size();
                        List<GiftBagEntity.CrystalGiftEntity> $crystalSizeListData = new ArrayList<>();
                        List<GiftBagAdapterEntity> listCrystalAdapter = new ArrayList<>();
                        if (!listCrystalEntity.isEmpty()) listCrystalEntity.get(0).setFirst(true);
                        if (crystalSize / 10 > 0) {
                            int cnt = 0;
                            int idx = 1;
                            for (int i = 0; i < crystalSize; i++) {
                                if (idx / 10 > 0) {
                                    $crystalSizeListData.add(listCrystalEntity.get(i));
                                    listCrystalAdapter.add(cnt, new GiftBagAdapterEntity(cnt, $crystalSizeListData));
                                    $crystalSizeListData = new ArrayList<>();
                                    cnt++;
                                    idx = 1;
                                } else {
                                    idx++;
                                    $crystalSizeListData.add(listCrystalEntity.get(i));
                                    if (i == (crystalSize - 1)) {
                                        listCrystalAdapter.add(cnt, new GiftBagAdapterEntity(cnt, $crystalSizeListData));
                                    }
                                }
                            }
                        } else {
                            $crystalSizeListData.addAll(listCrystalEntity);
                            if ($crystalSizeListData.size() > 0) {
                                GiftBagAdapterEntity giftBagAdapterEntity = new GiftBagAdapterEntity(0, $crystalSizeListData);
                                listCrystalAdapter.add(giftBagAdapterEntity);
                            }
                        }
                        crystalGifAdapter = new GiftBagRcvAdapter(crystalGifListPage, listCrystalAdapter, R.layout.dialog_crystal_gift_bag_item_detail, isDark);
                        crystalGifListPage.setAdapter(crystalGifAdapter);
                        if (listCrystalAdapter.size() > 0) {
                            int listGiftSize = listCrystalAdapter.size();
                            if (listGiftSize < 10) crystalIndicatorLayout.setVisibility(View.GONE);
                            for (int i = 0; i < listGiftSize; i++) {
                                View view = new View(mContext);
                                if (i == 0) {
                                    view.setBackground(mContext.getResources().getDrawable(R.drawable.picture_num_oval));
                                } else {
                                    view.setBackground(mContext.getResources().getDrawable(R.drawable.picture_oval_focus_not));
                                }
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(6), dp2px(6));
                                params.topMargin = dp2px(5);
                                params.leftMargin = dp2px(5);
                                view.setLayoutParams(params);
                                crystalIndicatorLayout.addView(view);
                            }
                        }
                        crystalGifAdapter.setOnClickListener((position, itemEntity, detail_layout, rcvPosition) -> {
                            if (checkGiftItemEntity != null) {
                                if (checkGiftItemEntity.getId().intValue() != itemEntity.getId().intValue()) {
                                    checkCrystalItemEntity = itemEntity;
                                    if (checkCrystalLineLayout != null)
                                        checkCrystalLineLayout.setBackgroundDrawable(null);
                                    detail_layout.setBackground(mContext.getDrawable(R.drawable.purple_gift_checked));
                                    checkCrystalLineLayout = detail_layout;
                                }
                            } else {
                                checkCrystalItemEntity = itemEntity;
                                checkCrystalLineLayout = detail_layout;
                                detail_layout.setBackground(mContext.getDrawable(R.drawable.purple_gift_checked));
                            }

                        });
                    }
                });
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
        void sendGiftClick(Dialog dialog, int number, GiftBagEntity.GiftEntity giftEntity);

        void rechargeStored(Dialog dialog);
    }

    public void setCardOnClickListener(CardOnClickListener cardOnClickListener) {

    }

    public interface CardOnClickListener {
        void onClick(Dialog dialog, int type);
    }
}
