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

/**
 * @author goldze
 * @date 2019/3/26
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
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(HomeMainViewModel.class)) {
            return (T) new HomeMainViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(EditProfileViewModel.class)) {
            return (T) new EditProfileViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(MineViewModel.class)) {
            return (T) new MineViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(PushSettingViewModel.class)) {
            return (T) new PushSettingViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(MessageMainViewModel.class)) {
            return (T) new MessageMainViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(BlacklistViewModel.class)) {
            return (T) new BlacklistViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(SettingViewModel.class)) {
            return (T) new SettingViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(PrivacySettingViewModel.class)) {
            return (T) new PrivacySettingViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ChangePasswordViewModel.class)) {
            return (T) new ChangePasswordViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ResetPasswordViewModel.class)) {
            return (T) new ResetPasswordViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(CertificationMaleViewModel.class)) {
            return (T) new CertificationMaleViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(UploadPhotoViewModel.class)) {
            return (T) new UploadPhotoViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(FaceRecognitionViewModel.class)) {
            return (T) new FaceRecognitionViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(RadioViewModel.class)) {
            return (T) new RadioViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(BroadcastViewModel.class)) {
            return (T) new BroadcastViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(MyTrendsViewModel.class)) {
            return (T) new MyTrendsViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(LikeListViewModel.class)) {
            return (T) new LikeListViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(UserDetailViewModel.class)) {
            return (T) new UserDetailViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ReportUserViewModel.class)) {
            return (T) new ReportUserViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(UserDynamicViewModel.class)) {
            return (T) new UserDynamicViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(MyPhotoAlbumViewModel.class)) {
            return (T) new MyPhotoAlbumViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(WalletViewModel.class)) {
            return (T) new WalletViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(CashViewModel.class)) {
            return (T) new CashViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(CoinViewModel.class)) {
            return (T) new CoinViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(HomeListViewModel.class)) {
            return (T) new HomeListViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(FaceVerifySuccessViewModel.class)) {
            return (T) new FaceVerifySuccessViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(GoddessCertificationViewModel.class)) {
            return (T) new GoddessCertificationViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(CertificationFemaleViewModel.class)) {
            return (T) new CertificationFemaleViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(SearchViewModel.class)) {
            return (T) new SearchViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ChatDetailViewModel.class)) {
            return (T) new ChatDetailViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(IssuanceProgramViewModel.class)) {
            return (T) new IssuanceProgramViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ApplyMessageViewModel.class)) {
            return (T) new ApplyMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(SystemMessageGroupViewModel.class)) {
            return (T) new SystemMessageGroupViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(BroadcastMessageViewModel.class)) {
            return (T) new BroadcastMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(CommentMessageViewModel.class)) {
            return (T) new CommentMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(EvaluateMessageViewModel.class)) {
            return (T) new EvaluateMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(GiveMessageViewModel.class)) {
            return (T) new GiveMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(SystemMessageViewModel.class)) {
            return (T) new SystemMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ProfitMessageViewModel.class)) {
            return (T) new ProfitMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(VipSubscribeViewModel.class)) {
            return (T) new VipSubscribeViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(TrendDetailViewModel.class)) {
            return (T) new TrendDetailViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(PhotoSettingViewModel.class)) {
            return (T) new PhotoSettingViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(SetRedPackagePhotoViewModel.class)) {
            return (T) new SetRedPackagePhotoViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(SetRedPackageVideoViewModel.class)) {
            return (T) new SetRedPackageVideoViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(TheirPhotoAlbumViewModel.class)) {
            return (T) new TheirPhotoAlbumViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(LocationMapsViewModel.class)) {
            return (T) new LocationMapsViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(PhotoBrowseViewModel.class)) {
            return (T) new PhotoBrowseViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(GiveListViewModel.class)) {
            return (T) new GiveListViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(UpdateFaceViewModel.class)) {
            return (T) new UpdateFaceViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(UpdateFaceSuccessViewModel.class)) {
            return (T) new UpdateFaceSuccessViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(WebDetailViewModel.class)) {
            return (T) new WebDetailViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(SendCoinRedPackageViewModel.class)) {
            return (T) new SendCoinRedPackageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(CoinRedPackageDetailViewModel.class)) {
            return (T) new CoinRedPackageDetailViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ScreenLockViewModel.class)) {
            return (T) new ScreenLockViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(VipPrivilegeViewModel.class)) {
            return (T) new VipPrivilegeViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(PhotoReviewViewModel.class)) {
            return (T) new PhotoReviewViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(InvitationCodeViewModel.class)) {
            return (T) new InvitationCodeViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(InviteWebDetailViewModel.class)) {
            return (T) new InviteWebDetailViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(RankListViewModel.class)) {
            return (T) new RankListViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ChatMessageViewModel.class)) {
            return (T) new ChatMessageViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(TraceViewModel.class)) {
            return (T) new TraceViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(TraceListViewModel.class)) {
            return (T) new TraceListViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(TraeManViewModel.class)) {
            return (T) new TraeManViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(MyAllBroadcastViewModel.class)){
            return (T) new MyAllBroadcastViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(ClipImageVideoViewModel.class)){
            return (T) new ClipImageVideoViewModel(mApplication, mRepository);
        }else if (modelClass.isAssignableFrom(TapeAudioViewModel.class)){
            return (T) new TapeAudioViewModel(mApplication, mRepository);
        }else if (modelClass.isAssignableFrom(MeSettingViewModel.class)) {
            return (T) new MeSettingViewModel(mApplication, mRepository);
        }else if (modelClass.isAssignableFrom(FukubuViewModel.class)) {
            return (T) new FukubuViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(TwDollarMoneyViewModel.class)) {
            return (T) new TwDollarMoneyViewModel(mApplication, mRepository);
        }else if (modelClass.isAssignableFrom(AudioCallingViewModel2.class)) {
            return (T) new AudioCallingViewModel2(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(AudioCallChatingViewModel.class)) {
            return (T) new AudioCallChatingViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(VideoCallViewModel.class)) {
            return (T) new VideoCallViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(PerfectProfileViewModel.class)){
            return (T) new PerfectProfileViewModel(mApplication, mRepository);
        } else if (modelClass.isAssignableFrom(CommunityAccountModel.class)){
            return (T) new CommunityAccountModel(mApplication, mRepository);
        }else if (modelClass.isAssignableFrom(NotepadViewModel.class)){
            return (T) new NotepadViewModel(mApplication, mRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
