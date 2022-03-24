package com.dl.playfun.ui.mine.task.record.list;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.dl.playfun.R;
import com.dl.playfun.entity.ExchangeEntity;

import org.jetbrains.annotations.NotNull;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.StringUtils;

/**
 * Author: 彭石林
 * Time: 2021/8/12 14:14
 * Description: This is ExchangeRecordItemViewModel
 */
public class ExchangeRecordItemViewModel extends MultiItemViewModel<ExchangeRecordListViewModel> {

    public ObservableField<ExchangeEntity> itemEntity = new ObservableField<ExchangeEntity>();
    public ObservableField<Integer> gold = new ObservableField<>(0);
    public ObservableField<Boolean> agree = new ObservableField<Boolean>(false);
    public ObservableField<Boolean> isDetailHidden = new ObservableField<>(true);
    public ObservableField<String> isDetailText = new ObservableField<>();

    public ObservableField<Drawable> itemDrawable = new ObservableField<>();
    public BindingCommand clickDetail = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (isDetailHidden.get().booleanValue()) {
                isDetailHidden.set(false);
                itemDrawable.set(ResourceUtils.getDrawable(R.drawable.ic_address_icon_up));
                int type = itemEntity.get().getType().intValue();
                if (type == 1) {
                    if (StringUtils.isEmpty(isDetailText.get())) {
                        //int position = viewModel.observableList.indexOf(ExchangeRecordItemViewModel.this);
                        ExchangeEntity entity = itemEntity.get();
                        if (entity != null) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(entity.getContacts() + "  " + entity.getPhone() + "\n")
                                    .append(entity.getCity() + "  " + entity.getAre() + "\n")
                                    .append(entity.getAddress());
                            isDetailText.set(stringBuilder.toString());
                        } else {
                            ToastUtils.showShort(R.string.server_exception);
                        }

                        //viewModel.getAddress(itemEntity.get().getAddressId(), position);
                    }
                } else if (type == 2) {
                    isDetailText.set(itemEntity.get().getContent());
                }
            } else {
                isDetailHidden.set(true);
                itemDrawable.set(ResourceUtils.getDrawable(R.drawable.ic_address_icon_down));
            }
        }
    });
    //选择回调如果全部选择了。回调全选按钮更变状态
    public BindingCommand isSelectedAll = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            viewModel.isSelectedAll();
        }
    });
    public BindingCommand ToSubVip = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            viewModel.uc.toSubVipPlay.setValue(3);
        }
    });

    public ExchangeRecordItemViewModel(@NonNull @NotNull ExchangeRecordListViewModel viewModel, ExchangeEntity exchangeEntity, int gold) {
        super(viewModel);
        this.itemEntity.set(exchangeEntity);
        this.gold.set(gold);
        this.itemDrawable.set(ResourceUtils.getDrawable(R.drawable.ic_address_icon_down));
    }

    public String getDateTime() {
        return String.format(StringUtils.getString(R.string.task_exchange_record_fragment_item1), itemEntity.get().getCreatedAt());
    }

    public String getExchangeText() {
        int type = itemEntity.get().getType().intValue();
        switch (type) {
            case 1:
                return StringUtils.getString(R.string.task_exchange_record_gold_1);
            case 2:
                return StringUtils.getString(R.string.task_exchange_record_gold_2);
        }
        return "";
    }

    public int ExchangeTypeShow() {
        int goldType = gold.get().intValue();
        int type = itemEntity.get().getType().intValue();
        if (goldType == 1) {
            if (type == 1) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        } else if (goldType == 2) {
            if (type == 3) {
                return View.GONE;
            }
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public int SubVipShow() {
        int goldType = gold.get().intValue();
        int type = itemEntity.get().getType().intValue();
        if (goldType == 1) {
            if (type == 3) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        } else {
            return View.GONE;
        }
    }

    public int SubYVipShow() {
        int goldType = gold.get().intValue();
        int type = itemEntity.get().getType().intValue();
        if (goldType == 2) {
            if (type == 3) {
                return View.VISIBLE;
            } else {
                return View.GONE;
            }
        } else {
            return View.GONE;
        }
    }
}
