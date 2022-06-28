package com.dl.playfun.app;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dl.playfun.data.AppRepository;
import com.dl.playfun.kl.viewmodel.AudioCallChatingViewModel;
import com.dl.playfun.kl.viewmodel.AudioCallingViewModel2;
import com.dl.playfun.kl.viewmodel.VideoCallViewModel;
import com.dl.playfun.ui.certification.certificationfemale.CertificationFemaleViewModel;
import com.dl.playfun.ui.certification.certificationmale.CertificationMaleViewModel;
import com.dl.playfun.ui.certification.facerecognition.FaceRecognitionViewModel;
import com.dl.playfun.ui.certification.goddesscertification.GoddessCertificationViewModel;
import com.dl.playfun.ui.certification.updateface.UpdateFaceViewModel;
import com.dl.playfun.ui.certification.updatefacesuccess.UpdateFaceSuccessViewModel;
import com.dl.playfun.ui.certification.uploadphoto.UploadPhotoViewModel;
import com.dl.playfun.ui.certification.verifysuccess.FaceVerifySuccessViewModel;
import com.dl.playfun.ui.home.HomeMainViewModel;
import com.dl.playfun.ui.home.homelist.HomeListViewModel;
import com.dl.playfun.ui.home.search.SearchViewModel;
import com.dl.playfun.ui.main.MainViewModel;
import com.dl.playfun.ui.message.MessageMainViewModel;
import com.dl.playfun.ui.message.applymessage.ApplyMessageViewModel;
import com.dl.playfun.ui.message.broadcastmessage.BroadcastMessageViewModel;
import com.dl.playfun.ui.message.chatdetail.ChatDetailViewModel;
import com.dl.playfun.ui.message.chatdetail.notepad.NotepadViewModel;
import com.dl.playfun.ui.message.chatmessage.ChatMessageViewModel;
import com.dl.playfun.ui.message.coinredpackagedetail.CoinRedPackageDetailViewModel;
import com.dl.playfun.ui.message.commentmessage.CommentMessageViewModel;
import com.dl.playfun.ui.message.evaluatemessage.EvaluateMessageViewModel;
import com.dl.playfun.ui.message.givemessage.GiveMessageViewModel;
import com.dl.playfun.ui.message.photoreview.PhotoReviewViewModel;
import com.dl.playfun.ui.message.profitmessage.ProfitMessageViewModel;
import com.dl.playfun.ui.message.pushsetting.PushSettingViewModel;
import com.dl.playfun.ui.message.sendcoinredpackage.SendCoinRedPackageViewModel;
import com.dl.playfun.ui.message.systemmessage.SystemMessageViewModel;
import com.dl.playfun.ui.message.systemmessagegroup.SystemMessageGroupViewModel;
import com.dl.playfun.ui.mine.MineViewModel;
import com.dl.playfun.ui.mine.account.CommunityAccountModel;
import com.dl.playfun.ui.mine.audio.TapeAudioViewModel;
import com.dl.playfun.ui.mine.blacklist.BlacklistViewModel;
import com.dl.playfun.ui.mine.broadcast.BroadcastViewModel;
import com.dl.playfun.ui.mine.broadcast.myall.MyAllBroadcastViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.MyTrendsViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.givelist.GiveListViewModel;
import com.dl.playfun.ui.mine.broadcast.mytrends.trenddetail.TrendDetailViewModel;
import com.dl.playfun.ui.mine.changepassword.ChangePasswordViewModel;
import com.dl.playfun.ui.mine.creenlock.ScreenLockViewModel;
import com.dl.playfun.ui.mine.invitationcode.InvitationCodeViewModel;
import com.dl.playfun.ui.mine.invitewebdetail.InviteWebDetailViewModel;
import com.dl.playfun.ui.mine.likelist.LikeListViewModel;
import com.dl.playfun.ui.mine.myphotoalbum.MyPhotoAlbumViewModel;
import com.dl.playfun.ui.mine.photosetting.PhotoSettingViewModel;
import com.dl.playfun.ui.mine.privacysetting.PrivacySettingViewModel;
import com.dl.playfun.ui.mine.profile.EditProfileViewModel;
import com.dl.playfun.ui.mine.profile.PerfectProfileViewModel;
import com.dl.playfun.ui.mine.resetpassword.ResetPasswordViewModel;
import com.dl.playfun.ui.mine.setredpackagephoto.SetRedPackagePhotoViewModel;
import com.dl.playfun.ui.mine.setredpackagevideo.SetRedPackageVideoViewModel;
import com.dl.playfun.ui.mine.setting.MeSettingViewModel;
import com.dl.playfun.ui.mine.setting.SettingViewModel;
import com.dl.playfun.ui.mine.trace.TraceViewModel;
import com.dl.playfun.ui.mine.trace.list.TraceListViewModel;
import com.dl.playfun.ui.mine.trace.man.TraeManViewModel;
import com.dl.playfun.ui.mine.vipprivilege.VipPrivilegeViewModel;
import com.dl.playfun.ui.mine.vipsubscribe.VipSubscribeViewModel;
import com.dl.playfun.ui.mine.wallet.WalletViewModel;
import com.dl.playfun.ui.mine.wallet.cash.CashViewModel;
import com.dl.playfun.ui.mine.wallet.coin.CoinViewModel;
import com.dl.playfun.ui.mine.wallet.girl.TwDollarMoneyViewModel;
import com.dl.playfun.ui.mine.webdetail.WebDetailViewModel;
import com.dl.playfun.ui.mine.webview.FukubuViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.IssuanceProgramViewModel;
import com.dl.playfun.ui.radio.issuanceprogram.clip.ClipImageVideoViewModel;
import com.dl.playfun.ui.radio.radiohome.RadioViewModel;
import com.dl.playfun.ui.ranklisk.ranklist.RankListViewModel;
import com.dl.playfun.ui.userdetail.detail.UserDetailViewModel;
import com.dl.playfun.ui.userdetail.locationmaps.LocationMapsViewModel;
import com.dl.playfun.ui.userdetail.photobrowse.PhotoBrowseViewModel;
import com.dl.playfun.ui.userdetail.report.ReportUserViewModel;
import com.dl.playfun.ui.userdetail.theirphotoalbum.TheirPhotoAlbumViewModel;
import com.dl.playfun.ui.userdetail.userdynamic.UserDynamicViewModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author 彭石林
 * @date 2022/6/26
 */
public class AppViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    @SuppressLint("StaticFieldLeak")
    private static volatile AppViewModelFactory INSTANCE;
    private final Application mApplication;
    private final AppRepository mRepository;

    private AppViewModelFactory(Application application, AppRepository repository) {
        this.mApplication = application;
        this.mRepository = repository;
    }

    public static AppViewModelFactory getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (AppViewModelFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppViewModelFactory(application, Injection.provideDemoRepository());
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //反射动态实例化ViewModel
        try {
            String className = modelClass.getCanonicalName();
            Class<?> classViewModel = Class.forName(className);
            Constructor<?> cons = classViewModel.getConstructor(Application.class, AppRepository.class);
            ViewModel viewModel = (ViewModel) cons.newInstance(mApplication, mRepository);
            return (T) viewModel;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
