package com.dl.playfun.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.ui.MainContainerActivity;
import com.dl.playfun.viewmodel.BaseViewModel;
import com.dl.playfun.widget.dialog.loading.DialogLoading;
import com.dl.playfun.widget.dialog.loading.DialogProgress;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.gyf.immersionbar.ImmersionBar;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragmentDelegate;
import me.yokeyword.fragmentation.SupportHelper;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public abstract class BaseFragment<V extends ViewDataBinding, VM extends BaseViewModel> extends me.goldze.mvvmhabit.base.BaseFragment<V, VM> implements ISupportFragment {

    final SupportFragmentDelegate mDelegate = new SupportFragmentDelegate(this);
    protected AppCompatActivity mActivity;
    protected FragmentActivity _mActivity;

    private DialogLoading dialogLoading;
    private DialogProgress dialogProgress;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusView = view.findViewById(R.id.status_bar_view);
        if (statusView != null) {
            ImmersionBar.setStatusBarView(this, statusView);
        }
        if (viewModel != null) {
            viewModel.onViewCreated();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (AppCompatActivity) context;
    }

    @Override
    protected void registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack();
        viewModel.getMuc().showHudEvent.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                showHud(s);
            }
        });
        viewModel.getMuc().showProgressHudEvent.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> map) {
                String title = (String) map.get("title");
                int progress = (int) map.get("progress");
                showProgressHud(title, progress);
            }
        });
        viewModel.getMuc().dismissHudEvent.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void v) {
                dismissHud();
            }
        });
        viewModel.getMuc().hideKeyboardEvent.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                KeyboardUtils.hideSoftInput(mActivity);
            }
        });
        viewModel.getMuc().startFragmentEvent.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> params) {
                String canonicalName = (String) params.get(BaseViewModel.ParameterField.FRAGMENT_NAME);
                Bundle bundle = (Bundle) params.get(BaseViewModel.ParameterField.BUNDLE);
                startContainerActivity(canonicalName, bundle);
            }
        });
        viewModel.getMuc().startWithPopFragmentEvent.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> params) {
                try {
                    String canonicalName = (String) params.get(BaseViewModel.ParameterField.FRAGMENT_NAME);
                    Bundle bundle = (Bundle) params.get(BaseViewModel.ParameterField.BUNDLE);
                    if (canonicalName == null || "".equals(canonicalName)) {
                        throw new IllegalArgumentException("can not find page fragmentName");
                    }
                    Class<?> fragmentClass = Class.forName(canonicalName);
                    Fragment fragment = (Fragment) fragmentClass.newInstance();
                    if (bundle != null) {
                        fragment.setArguments(bundle);
                    }

                    Fragment parentFragment = getRootFragment();
                    if (parentFragment != null) {
                        BaseFragment baseFragment = (BaseFragment) parentFragment;
                        baseFragment.startWithPop((ISupportFragment) fragment);
                    } else {
                        startWithPop((ISupportFragment) fragment);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        viewModel.getMuc().startWithPopToFragmentEvent.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> params) {
                String targetFragmentName = (String) params.get(BaseViewModel.ParameterField.FRAGMENT_NAME);
                String toFragmentName = (String) params.get(BaseViewModel.ParameterField.TO_FRAGMENT_NAME);
                Boolean include = (Boolean) params.get(BaseViewModel.ParameterField.INCLUDE_TARGET_FRAGMENT);
                try {
                    if (targetFragmentName == null || "".equals(targetFragmentName)) {
                        throw new IllegalArgumentException("can not find page targetFragmentName");
                    }
                    if (toFragmentName == null || "".equals(toFragmentName)) {
                        throw new IllegalArgumentException("can not find page toFragmentName");
                    }
                    Class<?> toFragmentClass = Class.forName(toFragmentName);
                    Fragment fragment = (Fragment) toFragmentClass.newInstance();

                    Class<?> targetFragmentClass = Class.forName(targetFragmentName);

                    Fragment parentFragment = getRootFragment();
                    if (parentFragment != null) {
                        BaseFragment baseFragment = (BaseFragment) parentFragment;
                        baseFragment.startWithPopTo((ISupportFragment) fragment, targetFragmentClass, include != null && include);
                    } else {
                        startWithPopTo((ISupportFragment) fragment, targetFragmentClass, include != null && include);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        viewModel.getMuc().popFragmentEvent.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void aVoid) {
                Fragment parentFragment = getRootFragment();
                if (parentFragment != null) {
                    BaseFragment baseFragment = (BaseFragment) parentFragment;
                    baseFragment.pop();
                } else {
                    pop();
                }
            }
        });
        viewModel.getMuc().popToFragmentEvent.observe(this, new Observer<Map<String, Object>>() {
            @Override
            public void onChanged(Map<String, Object> params) {
                String targetFragmentName = (String) params.get(BaseViewModel.ParameterField.FRAGMENT_NAME);
                Boolean include = (Boolean) params.get(BaseViewModel.ParameterField.INCLUDE_TARGET_FRAGMENT);
                try {
                    if (targetFragmentName == null || "".equals(targetFragmentName)) {
                        throw new IllegalArgumentException("can not find page targetFragmentName");
                    }
                    Class<?> targetFragmentClass = Class.forName(targetFragmentName);

                    Fragment parentFragment = getRootFragment();
                    if (parentFragment != null) {
                        BaseFragment baseFragment = (BaseFragment) parentFragment;
                        baseFragment.popTo(targetFragmentClass, include != null && include);
                    } else {
                        popTo(targetFragmentClass, include != null && include);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        viewModel.getMuc().popAllToFragmentEvent.observe(this, this::popAllTo);
    }

    private void showHud(String title) {
        if (dialogProgress != null && dialogProgress.isShowing()) {
            dialogProgress.dismiss();
        }

        if (dialogLoading == null) {
            dialogLoading = new DialogLoading(this.getContext());
        }
        dialogLoading.show();
    }

    public void showHud() {
        showHud("");
    }

    public void showProgressHud(String title, int progress) {
        if (dialogLoading != null && dialogLoading.isShowing()) {
            dialogLoading.dismiss();
        }
        if (dialogProgress == null) {
            dialogProgress = new DialogProgress(this.getContext());
        }
        dialogProgress.setProgress(progress);
        if (!dialogProgress.isShowing()) {
            dialogProgress.show();
        }
    }

    public void dismissHud() {
        try {
            post(()->{
                if (dialogLoading != null && dialogLoading.isShowing()) {
                    dialogLoading.dismiss();
                }
                if (dialogProgress != null && dialogProgress.isShowing()) {
                    dialogProgress.dismiss();
                }
            });
        }catch (Exception ignored){

        }
    }

    @Override
    public SupportFragmentDelegate getSupportDelegate() {
        return mDelegate;
    }

    /**
     * Perform some extra transactions.
     * ???????????????????????????Tag?????????SharedElement???????????????????????????Fragment
     */
    @Override
    public ExtraTransaction extraTransaction() {
        return mDelegate.extraTransaction();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate.onAttach(activity);
        _mActivity = mDelegate.getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate(savedInstanceState);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return mDelegate.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDelegate.onSaveInstanceState(outState);
    }


//    void onSupportVisible();


//    void onSupportInvisible();

    @Override
    public void onResume() {
        super.onResume();
        mDelegate.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDelegate.onPause();
    }

    @Override
    public void onDestroyView() {
        mDelegate.onDestroyView();
        super.onDestroyView();
    }

    public void dismissDestroyHud() {
        if (dialogLoading != null) {
            if(dialogLoading.isShowing()){
                dialogLoading.dismiss();
            }
            dialogLoading = null;
        }
        if (dialogProgress != null) {
            if(dialogProgress.isShowing()){
                dialogProgress.dismiss();
            }
            dialogProgress = null;
        }
    }

    @Override
    public void onDestroy() {
        dismissDestroyHud();
        mDelegate.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        try {
            mDelegate.onHiddenChanged(hidden);
        }catch (Exception ignored) {

        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mDelegate.setUserVisibleHint(isVisibleToUser);
    }

    protected boolean isUmengReportPage() {
        return true;
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     * <p>
     * The runnable will be run after all the previous action has been run.
     * <p>
     * ?????????????????????????????? ?????????Action
     *
     * @deprecated Use {@link #post(Runnable)} instead.
     */
    @Deprecated
    @Override
    public void enqueueAction(Runnable runnable) {
        mDelegate.enqueueAction(runnable);
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     * <p>
     * The runnable will be run after all the previous action has been run.
     * <p>
     * ?????????????????????????????? ?????????Action
     */
    @Override
    public void post(Runnable runnable) {
        mDelegate.post(runnable);
    }

    /**
     * Called when the enter-animation end.
     * ???????????? ?????????,??????
     */
    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        mDelegate.onEnterAnimationEnd(savedInstanceState);
        viewModel.onEnterAnimationEnd();
    }

    /**
     * Lazy initial???Called when fragment is first called.
     * <p>
     * ???????????? ????????? ??? ViewPager???????????????  ?????????????????????
     */
    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        mDelegate.onLazyInitView(savedInstanceState);
        viewModel.onLazyInitView();
    }

    /**
     * Called when the fragment is visible.
     * ???Fragment????????????????????????
     * <p>
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    @Override
    public void onSupportVisible() {
        mDelegate.onSupportVisible();
//        if (isUmengReportPage()) {
//            MobclickAgent.onPageStart(this.getClass().getSimpleName());
//        }
    }

    /**
     * Called when the fragment is invivible.
     * ???Fragment???????????????????????????
     * <p>
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    @Override
    public void onSupportInvisible() {
        mDelegate.onSupportInvisible();
//        if (isUmengReportPage()) {
//            MobclickAgent.onPageEnd(this.getClass().getSimpleName());
//        }
    }

    /**
     * Return true if the fragment has been supportVisible.
     */
    @Override
    final public boolean isSupportVisible() {
        return mDelegate.isSupportVisible();
    }

    /**
     * Set fragment animation with a higher priority than the ISupportActivity
     * ????????????Fragmemt??????,???????????????SupportActivity??????
     */
    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return mDelegate.onCreateFragmentAnimator();
    }

    /**
     * ??????????????????????????? copy
     *
     * @return FragmentAnimator
     */
    @Override
    public FragmentAnimator getFragmentAnimator() {
        return mDelegate.getFragmentAnimator();
    }

    /**
     * ??????Fragment??????????????????
     */
    @Override
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        mDelegate.setFragmentAnimator(fragmentAnimator);
    }

    /**
     * ??????????????????,?????????SupportActivity???onBackPressed()??????????????????
     *
     * @return false?????????????????????, true?????????????????????
     */
    @Override
    public boolean onBackPressedSupport() {
        return mDelegate.onBackPressedSupport();
    }

    /**
     * ?????? {@link Activity#setResult(int, Intent)}
     * <p>
     * Similar to {@link Activity#setResult(int, Intent)}
     *
     * @see #startForResult(ISupportFragment, int)
     */
    @Override
    public void setFragmentResult(int resultCode, Bundle bundle) {
        mDelegate.setFragmentResult(resultCode, bundle);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        mDelegate.onFragmentResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewBundle(Bundle args) {
        mDelegate.onNewBundle(args);
    }

    /**
     * ??????NewBundle,?????????????????????SingleTask/SingleTop???
     *
     * @see #start(ISupportFragment, int)
     */
    @Override
    public void putNewBundle(Bundle newBundle) {
        mDelegate.putNewBundle(newBundle);
    }


    /****************************************?????????????????????(Optional methods)******************************************************/
    // ?????????Support?????????????????????????????????


    /**
     * @Desc TODO(?????????????????????Fragment?????????1???????????????)
     * @author ?????????
     * @parame
     * @return
     * @Date 2022/10/19
     */
    public void popAllTo(@NonNull ISupportFragment toFragment){
        FragmentManager fragmentManager = mDelegate.getActivity().getSupportFragmentManager();
        List<Fragment> listFragment = fragmentManager.getFragments();
        if(!listFragment.isEmpty()){
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for(Fragment childFragment : listFragment){
                fragmentTransaction.remove(childFragment);
            }
            fragmentTransaction.commit();
        }
        if(mActivity != null){
            if (mActivity instanceof MainContainerActivity){
                ((MainContainerActivity) mActivity).popAllTo(toFragment);
            }
        }
    }

    /**
     * ???????????????
     */
    protected void hideSoftInput() {
        mDelegate.hideSoftInput();
    }

    /**
     * ???????????????,??????????????????,??????onPause????????????????????????
     */
    protected void showSoftInput(final View view) {
        mDelegate.showSoftInput(view);
    }

    /**
     * ?????????Fragment, ???Activity???????????????Fragment ??? Fragment??????????????????Fragment
     *
     * @param containerId ??????id
     * @param toFragment  ??????Fragment
     */
    public void loadRootFragment(int containerId, ISupportFragment toFragment) {
        mDelegate.loadRootFragment(containerId, toFragment);
    }

    public void loadRootFragment(int containerId, ISupportFragment toFragment, boolean addToBackStack, boolean allowAnim) {
        mDelegate.loadRootFragment(containerId, toFragment, addToBackStack, allowAnim);
    }

    /**
     * ?????????????????????Fragment,??????Wechat, QQ???????????????
     */
    public void loadMultipleRootFragment(int containerId, int showPosition, ISupportFragment... toFragments) {
        mDelegate.loadMultipleRootFragment(containerId, showPosition, toFragments);
    }

    /**
     * show??????Fragment,hide??????????????????Fragment
     * ??????????????????????????????????????????????????????Fragment,(????????????loadMultipleRootFragment()?????????Fragment)
     * <p>
     * ????????????????????????{@link #showHideFragment(ISupportFragment, ISupportFragment)}
     *
     * @param showFragment ??????show???Fragment
     */
    public void showHideFragment(ISupportFragment showFragment) {
        mDelegate.showHideFragment(showFragment);
    }

    /**
     * show??????Fragment,hide??????Fragment ; ???????????????????????????????????? ??????tab?????????
     */
    public void showHideFragment(ISupportFragment showFragment, ISupportFragment hideFragment) {
        mDelegate.showHideFragment(showFragment, hideFragment);
    }

    public void start(ISupportFragment toFragment) {
        mDelegate.start(toFragment);
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    public void start(final ISupportFragment toFragment, @LaunchMode int launchMode) {
        mDelegate.start(toFragment, launchMode);
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    public void startForResult(ISupportFragment toFragment, int requestCode) {
        mDelegate.startForResult(toFragment, requestCode);
    }

    /**
     * Start the target Fragment and pop itself
     */
    public void startWithPop(ISupportFragment toFragment) {
        mDelegate.startWithPop(toFragment);
    }

    /**
     * @see #popTo(Class, boolean)
     * +
     * @see #start(ISupportFragment)
     */
    public void startWithPopTo(ISupportFragment toFragment, Class<?> targetFragmentClass, boolean includeTargetFragment) {
        mDelegate.startWithPopTo(toFragment, targetFragmentClass, includeTargetFragment);
    }

    public void replaceFragment(ISupportFragment toFragment, boolean addToBackStack) {
        mDelegate.replaceFragment(toFragment, addToBackStack);
    }

    public void pop() {
        mDelegate.pop();
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     * <p>
     * ???????????????fragment
     *
     * @param targetFragmentClass   ??????fragment
     * @param includeTargetFragment ???????????????fragment
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
        mDelegate.popTo(targetFragmentClass, includeTargetFragment);
    }

    /**
     * ???????????????fragment??????
     */
    public <T extends ISupportFragment> T findChildFragment(Class<T> fragmentClass) {
        return SupportHelper.findFragment(getChildFragmentManager(), fragmentClass);
    }


    /**
     * ??????????????????
     *
     * @param canonicalName ????????? : Fragment.class.getCanonicalName()
     */
    @Override
    public void startContainerActivity(String canonicalName) {
        startContainerActivity(canonicalName, null);
    }

    /**
     * ??????????????????
     *
     * @param canonicalName ????????? : Fragment.class.getCanonicalName()
     * @param bundle        ????????????????????????
     */
    @Override
    public void startContainerActivity(String canonicalName, Bundle bundle) {
        try {
            if (canonicalName == null || "".equals(canonicalName)) {
                throw new IllegalArgumentException("can not find page fragmentName");
            }
            Class<?> fragmentClass = Class.forName(canonicalName);
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            if (bundle != null) {
                fragment.setArguments(bundle);
            }
            Fragment parentFragment = getRootFragment();
            if (parentFragment != null) {
                BaseFragment baseFragment = (BaseFragment) parentFragment;
                baseFragment.start((ISupportFragment) fragment);
            } else {
                start((ISupportFragment) fragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     *
     * @param fragmentName ????????? : Fragment.class.getCanonicalName()
     */
    public void startFragment(String fragmentName) {
        startFragment(fragmentName, null);
    }

    /**
     * ??????????????????
     *
     * @param fragmentName ????????? : Fragment.class.getCanonicalName()
     * @param bundle       ????????????????????????
     */
    public void startFragment(String fragmentName, Bundle bundle) {
        try {
            if (fragmentName == null || "".equals(fragmentName)) {
                throw new IllegalArgumentException("can not find page fragmentName");
            }
            Class<?> fragmentClass = Class.forName(fragmentName);
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            if (bundle != null) {
                fragment.setArguments(bundle);
            }

            Fragment parentFragment = getRootFragment();
            if (parentFragment != null) {
                BaseFragment baseFragment = (BaseFragment) parentFragment;
                baseFragment.start((ISupportFragment) fragment);
            } else {
                start((ISupportFragment) fragment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Fragment getRootFragment() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment == null) {
            return this;
        }
        boolean b = true;
        do {
            Fragment p = parentFragment.getParentFragment();
            if (p != null) {
                parentFragment = p;
            } else {
                b = false;
            }
        } while (b);
        return parentFragment;
    }

    protected void startOutWebBrowser(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivity(intent);
        }
//        startActivity(Intent.createChooser(intent, "??????????????????"));
    }

    protected String getStringByResId(@StringRes int resId) {
        return StringUtils.getString(resId);
    }
}
