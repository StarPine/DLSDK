package com.dl.playfun.ui.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dl.playfun.R;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.ui.dialog.adapter.CityChooseAdapter;
import com.dl.playfun.widget.recyclerview.LineManagers;

import java.util.List;

/**
 * Author: 彭石林
 * Time: 2022/7/25 18:16
 * Description: This is CityChooseDialog
 */
public class CityChooseDialog extends BaseDialogFragment implements View.OnClickListener {

    private final List<ConfigItemEntity> citys;
    private RecyclerView recyclerView;
    private TextView btnConfirm;
    private TextView btnCancel;
    private ConfigItemEntity currentChooseEntity;
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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomDialog);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recycler_view);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.addItemDecoration(LineManagers.horizontal(1, 0, 0).create(recyclerView));
        adapter = new CityChooseAdapter(recyclerView);
        adapter.setData(this.citys);
        recyclerView.setAdapter(adapter);

        adapter.setCityChooseAdapterListener((itemEntity,position) -> {
            currentChooseEntity = itemEntity;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mWindow.setGravity(Gravity.BOTTOM);
//        mWindow.setWindowAnimations(R.style.TopAnimation);
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWindow.setBackgroundDrawableResource(R.color.transparent);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_city_choose;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            if (cityChooseDialogListener != null) {
                cityChooseDialogListener.onItemClick(this,currentChooseEntity);
            }
        } else if (view.getId() == R.id.btn_cancel) {
            this.dismiss();
        }
    }

    public interface CityChooseDialogListener {
        void onItemClick(CityChooseDialog dialog, ConfigItemEntity itemEntity);
    }
}
