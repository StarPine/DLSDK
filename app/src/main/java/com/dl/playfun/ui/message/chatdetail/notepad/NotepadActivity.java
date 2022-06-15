package com.dl.playfun.ui.message.chatdetail.notepad;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.dl.playfun.R;
import com.dl.playfun.app.AppViewModelFactory;
import com.dl.playfun.databinding.ActivityNotepadBinding;
import com.dl.playfun.kl.viewmodel.AudioCallingViewModel2;
import com.dl.playfun.widget.BasicToolbar;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.tatarka.bindingcollectionadapter2.BR;

/**
 * @Name： PlayFun_Google
 * @Description：
 * @Author： liaosf
 * @Date： 2022/6/15 17:23
 * 修改备注：
 */
public class NotepadActivity extends BaseActivity<ActivityNotepadBinding, NotepadViewModel> implements BasicToolbar.ToolbarListener{

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_notepad;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public NotepadViewModel initViewModel() {
        //使用自定义的ViewModelFactory来创建ViewModel，如果不重写该方法，则默认会调用LoginViewModel(@NonNull Application application)构造方法
        AppViewModelFactory factory = AppViewModelFactory.getInstance(getApplication());
        return ViewModelProviders.of(this, factory).get(NotepadViewModel.class);
    }

    @Override
    public void initParam() {
        super.initParam();

    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        binding.basicToolbar.setToolbarListener(this);
        binding.editNotepad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvWordCount.setText(String.format(getString(R.string.notepad_word_count_format),binding.editNotepad.getText().toString().length()+""));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onBackClick(BasicToolbar toolbar) {
        this.finish();
    }
}
