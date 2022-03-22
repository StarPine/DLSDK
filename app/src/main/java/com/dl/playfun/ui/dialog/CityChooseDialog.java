package com.dl.playfun.ui.dialog;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.widget.recyclerview.LineManagers;
import com.dl.playfun.R;
import com.dl.playfun.ui.dialog.adapter.CityChooseAdapter;

import java.util.List;

/**
 * 城市选择
 *
 * @author wulei
 */
public class CityChooseDialog extends BaseDialogFragment implements View.OnClickListener {

    private final List<ConfigItemEntity> citys;
    private View topView;
    private RecyclerView recyclerView;
    private Button btnConfirm;
    private View overlayView;
    private CityChooseAdapter adapter;
    private CityChooseDialogListener cityChooseDialogListener;

    public CityChooseDialog(List<ConfigItemEntity> citys) {
        this.citys = citys;
    }

    public CityChooseDialogListener getCityChooseDialogListener() {
        return cityChooseDialogListener;
    }

    public void setCityChooseDialogListener(CityChooseDialogListener cityChooseDialogListener) {
        this.cityChooseDialogListener = cityChooseDialogListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CityDialog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        topView = view.findViewById(R.id.top_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        overlayView = view.findViewById(R.id.rl_overlay);

        topView.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        overlayView.setOnClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.addItemDecoration(LineManagers.horizontal(1, 0, 0).create(recyclerView));
        adapter = new CityChooseAdapter(recyclerView);
        adapter.setData(this.citys);
        recyclerView.setAdapter(adapter);

        adapter.setCityChooseAdapterListener(itemEntity -> {
            if (cityChooseDialogListener != null) {
                cityChooseDialogListener.onItemClick(CityChooseDialog.this, itemEntity);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mWindow.setGravity(Gravity.TOP);
//        mWindow.setWindowAnimations(R.style.TopAnimation);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, mWidthAndHeight[1]);
        mWindow.setBackgroundDrawableResource(R.color.transparent);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_city_choose;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, mWidthAndHeight[1] / 2);
//        ImmersionBar.with(this)
//                .navigationBarWithKitkatEnable(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
//                .init();
    }

//    @Override
//    public void onDestroy() {
//        ImmersionBar.destroy(this);
//        super.onDestroy();
//    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            if (cityChooseDialogListener != null) {
                cityChooseDialogListener.onClickConfirm(this);
            }
        } else if (view.getId() == R.id.rl_overlay || view.getId() == R.id.top_view) {
            this.dismiss();
        }
    }

    public interface CityChooseDialogListener {
        void onClickConfirm(CityChooseDialog dialog);

        void onItemClick(CityChooseDialog dialog, ConfigItemEntity itemEntity);
    }
}
