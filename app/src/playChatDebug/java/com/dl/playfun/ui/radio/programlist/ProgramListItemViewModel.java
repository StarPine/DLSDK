package com.dl.playfun.ui.radio.programlist;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;

import com.dl.playfun.ui.message.applymessage.ApplyMessageFragment;
import com.dl.playfun.ui.message.broadcastmessage.BroadcastMessageFragment;
import com.dl.playfun.ui.message.commentmessage.CommentMessageFragment;
import com.dl.playfun.ui.message.evaluatemessage.EvaluateMessageFragment;
import com.dl.playfun.ui.message.givemessage.GiveMessageFragment;
import com.dl.playfun.ui.message.profitmessage.ProfitMessageFragment;
import com.dl.playfun.ui.message.signmessage.SignMessageFragment;
import com.dl.playfun.ui.message.systemmessage.SystemMessageFragment;

import me.goldze.mvvmhabit.base.MultiItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

/**
 * @author wulei
 */
public class ProgramListItemViewModel extends MultiItemViewModel<ProgramListViewModel> {

    private final String mold;
    public ObservableField<Integer> unReadNumber = new ObservableField<>();
    public ObservableField<Integer> icon = new ObservableField<>();
    public ObservableField<String> title = new ObservableField<>();
    public ObservableField<String> subTitle = new ObservableField<>();
    public ObservableField<String> time = new ObservableField<>();
    //条目的点击事件
    public BindingCommand itemClick = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if ("system".equals(mold)) {
                viewModel.start(SystemMessageFragment.class.getCanonicalName());
            } else if ("apply".equals(mold)) {
                viewModel.start(ApplyMessageFragment.class.getCanonicalName());
            } else if ("broadcast".equals(mold)) {
                viewModel.start(BroadcastMessageFragment.class.getCanonicalName());
            } else if ("comment".equals(mold)) {
                viewModel.start(CommentMessageFragment.class.getCanonicalName());
            } else if ("sign".equals(mold)) {
                viewModel.start(SignMessageFragment.class.getCanonicalName());
            } else if ("give".equals(mold)) {
                viewModel.start(GiveMessageFragment.class.getCanonicalName());
            } else if ("evaluate".equals(mold)) {
                viewModel.start(EvaluateMessageFragment.class.getCanonicalName());
            } else if ("profit".equals(mold)) {
                viewModel.start(ProfitMessageFragment.class.getCanonicalName());
            }
        }
    });

    public ProgramListItemViewModel(@NonNull ProgramListViewModel viewModel, String mold, Integer unReadNumber, Integer iconRes, String title, String subTitle, String time) {
        super(viewModel);
        this.mold = mold;
        this.unReadNumber.set(unReadNumber);
        this.icon.set(iconRes);
        this.title.set(title);
        this.subTitle.set(subTitle);
        this.time.set(time);
    }

    @BindingAdapter({"icon_src"})
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

}
