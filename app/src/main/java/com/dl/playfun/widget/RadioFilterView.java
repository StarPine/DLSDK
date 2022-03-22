package com.dl.playfun.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.entity.RadioTwoFilterItemEntity;
import com.dl.playfun.widget.dropdownfilterpop.DropDownFilterPopupWindow;
import com.dl.playfun.widget.dropdownfilterpop.DropDownTwoFilterPopupWindow;
import com.dl.playfun.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 筛选过滤器
 *
 * @author 石林
 */
public class RadioFilterView extends LinearLayout implements View.OnClickListener, DropDownFilterPopupWindow.OnItemClickListener, DropDownTwoFilterPopupWindow.OnTwoFItemClickListener, PopupWindow.OnDismissListener {

    private final View sexView;
    private final View regionView;

    private final CheckBox cbSex;
    private final CheckBox cbRegion;

    private final DropDownFilterPopupWindow sexPop;
    private final DropDownTwoFilterPopupWindow regionPop;

    private List<RadioFilterItemEntity> sexs;
    private List<RadioTwoFilterItemEntity> twoFilterItemEntities;
    private List<RadioTwoFilterItemEntity.CityBean> twoFilterItemEntitiesCity;
    private Integer sexsPosion = 0;
    private Integer regionsLeftPosion = 0;

    private RadioFilterListener radioFilterListener;

    public RadioFilterView(Context context) {
        this(context, null);
    }

    public RadioFilterView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioFilterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_radio_filter, this);
        sexView = findViewById(R.id.rl_sex);
        regionView = findViewById(R.id.rl_region);
        cbSex = findViewById(R.id.tv_sex);
        cbRegion = findViewById(R.id.tv_region);

        sexView.setOnClickListener(this);
        regionView.setOnClickListener(this);

        sexs = new ArrayList<>();
        twoFilterItemEntities = new ArrayList<>();
        twoFilterItemEntitiesCity = new ArrayList<>();

        sexPop = new DropDownFilterPopupWindow((Activity) context, sexs);
        sexPop.setOnItemClickListener(this);
        sexPop.setOnDismissListener(this);
        
        regionPop = new DropDownTwoFilterPopupWindow((Activity) context, twoFilterItemEntities);
        regionPop.setOnTwoFItemClickListener(this);
        regionPop.setOnDismissListener(this);
    }

    public RadioFilterListener getRadioFilterListener() {
        return radioFilterListener;
    }

    public void setRadioFilterListener(RadioFilterListener radioFilterListener) {
        this.radioFilterListener = radioFilterListener;
    }

    public void setFilterData(List<RadioFilterItemEntity> sexs, List<RadioTwoFilterItemEntity> regions) {
        this.sexs = sexs;
        this.twoFilterItemEntities = regions;
        if (sexs.size() > 0) {
            cbSex.setText(sexs.get(0).getName());
        }
        sexPop.setDatas(sexs);
        regionPop.setDatas(regions);
    }

    public void cityClick(int position) {
        regionsLeftPosion = position;
        RadioTwoFilterItemEntity name = twoFilterItemEntities.get(position);
        cbRegion.setText(name.getName());
    }

    public void sexClick(int position) {
        sexsPosion = position;
        RadioFilterItemEntity name = sexs.get(position);
        cbSex.setText(name.getName());
    }

    @Override
    public void onItemClick(DropDownFilterPopupWindow popupWindow, int position) {
        if (popupWindow == sexPop) {
            popupWindow.dismiss();
            sexsPosion = position;
            RadioFilterItemEntity name = sexs.get(position);
            cbSex.setText(name.getName());
            if (radioFilterListener != null) {
                radioFilterListener.onSexSelected(RadioFilterView.this, position, name);
            }
        }
    }

    @Override
    public void onItemClickLeft(DropDownTwoFilterPopupWindow popupWindow, int position) {
        if (popupWindow == regionPop) {
            regionsLeftPosion = position;
            twoFilterItemEntitiesCity.clear();
            twoFilterItemEntitiesCity.addAll(twoFilterItemEntities.get(position).getCity());
            if (position != 0){
                twoFilterItemEntitiesCity.add(0,new RadioTwoFilterItemEntity.CityBean(0,getResources().getString(R.string.playfun_text_all)));
            }
            regionPop.setRightDatas(twoFilterItemEntitiesCity);
        }
    }

    @Override
    public void onItemClickRight(DropDownTwoFilterPopupWindow popupWindow, int position) {
        if (popupWindow == regionPop) {
            popupWindow.dismiss();
            RadioTwoFilterItemEntity twoFilterItemEntity = twoFilterItemEntities.get(regionsLeftPosion);
            RadioTwoFilterItemEntity.CityBean twoFilterItemEntityCity = twoFilterItemEntitiesCity.get(position);
            cbRegion.setText(twoFilterItemEntityCity.getName());
            if (radioFilterListener != null) {
                radioFilterListener.onRegionSelected(RadioFilterView.this, position, twoFilterItemEntity.getId(),twoFilterItemEntityCity.getId());
            }
        }
    }

    @Override
    public void onDismiss() {
        cbSex.setChecked(false);
        cbRegion.setChecked(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rl_sex) {
            cbSex.setChecked(true);
            sexPop.setSelectedPosition(sexsPosion);
            sexPop.showAsDropDown(this);
            AppContext.instance().logEvent(AppsFlyerEvent.Gender_optional);
        } else if (view.getId() == R.id.rl_region) {
            cbRegion.setChecked(true);
            regionPop.setSelectedPosition(regionsLeftPosion);
            AppContext.instance().logEvent(AppsFlyerEvent.Region);
            regionPop.showAsDropDown(this);
        }
    }

    public interface RadioFilterListener {

        void onPublishTimeSelected(RadioFilterView radioFilterView, int position, RadioFilterItemEntity obj);

        void onSexSelected(RadioFilterView radioFilterView, int position, RadioFilterItemEntity obj);

        void onRegionSelected(RadioFilterView radioFilterView, int position, Integer gameId,Integer cityId);
    }

    public static class RadioFilterItemEntity<T> {
        private String name;
        private T data;

        public RadioFilterItemEntity(String name, T data) {
            this.name = name;
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
