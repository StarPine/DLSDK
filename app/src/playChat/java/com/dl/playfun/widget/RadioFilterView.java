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

import com.dl.playfun.R;
import com.dl.playfun.app.AppContext;
import com.dl.playfun.app.AppsFlyerEvent;
import com.dl.playfun.widget.dropdownfilterpop.DropDownFilterPopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * 筛选过滤器
 *
 * @author 石林
 */
public class RadioFilterView extends LinearLayout implements View.OnClickListener, DropDownFilterPopupWindow.OnItemClickListener, PopupWindow.OnDismissListener {

    private final View publishTimeView;
    private final View sexView;
    private final View regionView;

    private final CheckBox cbPublishTime;
    private final CheckBox cbSex;
    private final CheckBox cbRegion;

    private final DropDownFilterPopupWindow timePop;
    private final DropDownFilterPopupWindow sexPop;
    private final DropDownFilterPopupWindow regionPop;

    private List<RadioFilterItemEntity> times;
    private List<RadioFilterItemEntity> sexs;
    private List<RadioFilterItemEntity> regions;
    private Integer timesPosion = 0;
    private Integer sexsPosion = 0;
    private Integer regionsPosion = 0;

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
        publishTimeView = findViewById(R.id.rl_publish_time);
        sexView = findViewById(R.id.rl_sex);
        regionView = findViewById(R.id.rl_region);
        cbPublishTime = findViewById(R.id.tv_publish_time);
        cbSex = findViewById(R.id.tv_sex);
        cbRegion = findViewById(R.id.tv_region);

        publishTimeView.setOnClickListener(this);
        sexView.setOnClickListener(this);
        regionView.setOnClickListener(this);

        times = new ArrayList<>();
        sexs = new ArrayList<>();
        regions = new ArrayList<>();

        timePop = new DropDownFilterPopupWindow((Activity) context, times);
        timePop.setOnItemClickListener(this);
        timePop.setOnDismissListener(this);
        sexPop = new DropDownFilterPopupWindow((Activity) context, sexs);
        sexPop.setOnItemClickListener(this);
        sexPop.setOnDismissListener(this);
        regionPop = new DropDownFilterPopupWindow((Activity) context, regions);
        regionPop.setOnItemClickListener(this);
        regionPop.setOnDismissListener(this);
    }

    public RadioFilterListener getRadioFilterListener() {
        return radioFilterListener;
    }

    public void setRadioFilterListener(RadioFilterListener radioFilterListener) {
        this.radioFilterListener = radioFilterListener;
    }

    public void setFilterData(List<RadioFilterItemEntity> times, List<RadioFilterItemEntity> sexs, List<RadioFilterItemEntity> regions) {
        this.times = times;
        this.sexs = sexs;
        this.regions = regions;
        if(sexs.size() > 0){
            cbSex.setText(sexs.get(0).getName());
        }
        timePop.setDatas(times);
        sexPop.setDatas(sexs);
        regionPop.setDatas(regions);
    }

    public void timesClick(int position){
        timesPosion = position;
        RadioFilterItemEntity name = times.get(position);
        cbPublishTime.setText(name.getName());
    }

    public void cityClick(int position){
        regionsPosion = position;
        RadioFilterItemEntity name = regions.get(position);
        cbRegion.setText(name.getName());
    }

    public void sexClick(int position) {
        sexsPosion = position;
        RadioFilterItemEntity name = sexs.get(position);
        cbSex.setText(name.getName());
    }

    @Override
    public void onItemClick(DropDownFilterPopupWindow popupWindow, int position) {
        if (popupWindow == timePop) {
            popupWindow.dismiss();
            timesPosion = position;
            RadioFilterItemEntity name = times.get(position);
            cbPublishTime.setText(name.getName());
            if (radioFilterListener != null) {
                radioFilterListener.onPublishTimeSelected(RadioFilterView.this, position, name);
            }
        } else if (popupWindow == sexPop) {
            popupWindow.dismiss();
            sexsPosion = position;
            RadioFilterItemEntity name = sexs.get(position);
            cbSex.setText(name.getName());
            if (radioFilterListener != null) {
                radioFilterListener.onSexSelected(RadioFilterView.this, position, name);
            }
        } else if (popupWindow == regionPop) {
            popupWindow.dismiss();
            regionsPosion = position;
            RadioFilterItemEntity name = regions.get(position);
            cbRegion.setText(name.getName());
            if (radioFilterListener != null) {
                radioFilterListener.onRegionSelected(RadioFilterView.this, position, name);
            }
        }
    }

    @Override
    public void onDismiss() {
        cbPublishTime.setChecked(false);
        cbSex.setChecked(false);
        cbRegion.setChecked(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rl_publish_time) {
            cbPublishTime.setChecked(true);
            timePop.setSelectedPosition(timesPosion);
            timePop.showAsDropDown(this);
            AppContext.instance().logEvent(AppsFlyerEvent.Time_optional);
        } else if (view.getId() == R.id.rl_sex) {
            cbSex.setChecked(true);
            sexPop.setSelectedPosition(sexsPosion);
            sexPop.showAsDropDown(this);
            AppContext.instance().logEvent(AppsFlyerEvent.Gender_optional);
        } else if (view.getId() == R.id.rl_region) {
            cbRegion.setChecked(true);
            regionPop.setSelectedPosition(regionsPosion);
            AppContext.instance().logEvent(AppsFlyerEvent.Region);
            regionPop.showAsDropDown(this);
        }
    }

    public interface RadioFilterListener {

        void onPublishTimeSelected(RadioFilterView radioFilterView, int position, RadioFilterItemEntity obj);

        void onSexSelected(RadioFilterView radioFilterView, int position, RadioFilterItemEntity obj);

        void onRegionSelected(RadioFilterView radioFilterView, int position, RadioFilterItemEntity obj);
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
