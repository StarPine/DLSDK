package com.dl.playfun.data;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.dl.playfun.data.source.HttpDataSource;
import com.dl.playfun.data.source.LocalDataSource;
import com.dl.playfun.data.source.http.response.BaseDataResponse;
import com.dl.playfun.data.source.http.response.BaseListDataResponse;
import com.dl.playfun.data.source.http.response.BaseResponse;
import com.dl.playfun.entity.AccostEntity;
import com.dl.playfun.entity.AddressEntity;
import com.dl.playfun.entity.AlbumPhotoEntity;
import com.dl.playfun.entity.AllConfigEntity;
import com.dl.playfun.entity.ApplyMessageEntity;
import com.dl.playfun.entity.AuthLoginUserEntity;
import com.dl.playfun.entity.BaseUserBeanEntity;
import com.dl.playfun.entity.BlackEntity;
import com.dl.playfun.entity.BonusGoodsEntity;
import com.dl.playfun.entity.BoradCastMessageEntity;
import com.dl.playfun.entity.BroadcastEntity;
import com.dl.playfun.entity.BroadcastListEntity;
import com.dl.playfun.entity.BrowseNumberEntity;
import com.dl.playfun.entity.BubbleEntity;
import com.dl.playfun.entity.CallingInfoEntity;
import com.dl.playfun.entity.CallingInviteInfo;
import com.dl.playfun.entity.CashWalletEntity;
import com.dl.playfun.entity.ChatDetailCoinEntity;
import com.dl.playfun.entity.ChatRedPackageEntity;
import com.dl.playfun.entity.CoinExchangeBoxInfo;
import com.dl.playfun.entity.CoinWalletEntity;
import com.dl.playfun.entity.CommentMessageEntity;
import com.dl.playfun.entity.CompareFaceEntity;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.CreateOrderEntity;
import com.dl.playfun.entity.EjectEntity;
import com.dl.playfun.entity.EjectSignInEntity;
import com.dl.playfun.entity.EvaluateEntity;
import com.dl.playfun.entity.EvaluateMessageEntity;
import com.dl.playfun.entity.EvaluateObjEntity;
import com.dl.playfun.entity.ExchangeEntity;
import com.dl.playfun.entity.ExchangeIntegraOuterEntity;
import com.dl.playfun.entity.FaceVerifyResultEntity;
import com.dl.playfun.entity.FaceVerifyTokenEntity;
import com.dl.playfun.entity.GameCoinBuy;
import com.dl.playfun.entity.GameCoinWalletEntity;
import com.dl.playfun.entity.GameConfigEntity;
import com.dl.playfun.entity.GamePhotoAlbumEntity;
import com.dl.playfun.entity.GiftBagEntity;
import com.dl.playfun.entity.GiveMessageEntity;
import com.dl.playfun.entity.GoldDetailEntity;
import com.dl.playfun.entity.GoodsEntity;
import com.dl.playfun.entity.GoogleNearPoiBean;
import com.dl.playfun.entity.GooglePoiBean;
import com.dl.playfun.entity.ImSigEntity;
import com.dl.playfun.entity.IsChatEntity;
import com.dl.playfun.entity.LocalGooglePayCache;
import com.dl.playfun.entity.MessageGroupEntity;
import com.dl.playfun.entity.MessageRuleEntity;
import com.dl.playfun.entity.NewsEntity;
import com.dl.playfun.entity.OccupationConfigItemEntity;
import com.dl.playfun.entity.ParkItemEntity;
import com.dl.playfun.entity.PhotoAlbumEntity;
import com.dl.playfun.entity.PriceConfigEntity;
import com.dl.playfun.entity.PrivacyEntity;
import com.dl.playfun.entity.ProfitMessageEntity;
import com.dl.playfun.entity.PushSettingEntity;
import com.dl.playfun.entity.RadioTwoFilterItemEntity;
import com.dl.playfun.entity.SignMessageEntity;
import com.dl.playfun.entity.SoundEntity;
import com.dl.playfun.entity.StatusEntity;
import com.dl.playfun.entity.SwiftMessageEntity;
import com.dl.playfun.entity.SwitchesEntity;
import com.dl.playfun.entity.SystemConfigEntity;
import com.dl.playfun.entity.SystemConfigTaskEntity;
import com.dl.playfun.entity.SystemMessageEntity;
import com.dl.playfun.entity.TagEntity;
import com.dl.playfun.entity.TaskAdEntity;
import com.dl.playfun.entity.TaskConfigEntity;
import com.dl.playfun.entity.TaskConfigItemEntity;
import com.dl.playfun.entity.TaskRewardReceiveEntity;
import com.dl.playfun.entity.TokenEntity;
import com.dl.playfun.entity.TopicalListEntity;
import com.dl.playfun.entity.TraceEntity;
import com.dl.playfun.entity.UnReadMessageNumEntity;
import com.dl.playfun.entity.UnlockSocialAccountConfigEntity;
import com.dl.playfun.entity.UserCoinItemEntity;
import com.dl.playfun.entity.UserConnMicStatusEntity;
import com.dl.playfun.entity.UserDataEntity;
import com.dl.playfun.entity.UserDetailEntity;
import com.dl.playfun.entity.UserInfoEntity;
import com.dl.playfun.entity.UserProfitPageEntity;
import com.dl.playfun.entity.UserRemarkEntity;
import com.dl.playfun.entity.VersionEntity;
import com.dl.playfun.entity.VipPackageItemEntity;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import me.goldze.mvvmhabit.base.BaseModel;
import okhttp3.RequestBody;

/**
 * MVVM的Model层，统一模块的数据仓库，包含网络数据和本地数据（一个应用可以有多个Repositor）
 *
 * @author goldze
 * @date 2019/3/26
 */
public class AppRepository extends BaseModel implements HttpDataSource, LocalDataSource {
    private volatile static AppRepository INSTANCE = null;
    private final HttpDataSource mHttpDataSource;

    private final LocalDataSource mLocalDataSource;

    private AppRepository(@NonNull HttpDataSource httpDataSource,
                          @NonNull LocalDataSource localDataSource) {
        this.mHttpDataSource = httpDataSource;
        this.mLocalDataSource = localDataSource;
    }

    public static AppRepository getInstance(HttpDataSource httpDataSource,
                                            LocalDataSource localDataSource) {
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppRepository(httpDataSource, localDataSource);
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<BaseDataResponse<ChatDetailCoinEntity>> getTotalCoins(Integer dismissRoom) {
        return mHttpDataSource.getTotalCoins(dismissRoom);
    }

    @Override
    public Observable<BaseResponse> GamePaySuccessNotify(String packageName, String orderNumber, List<String> productId, String token, int type, Integer event, String serverId, String roleId) {
        return mHttpDataSource.GamePaySuccessNotify(packageName, orderNumber, productId, token, type, event, serverId, roleId);
    }

    @Override
    public Observable<BaseDataResponse<GamePhotoAlbumEntity>> getGamePhotoAlbumList(String serverId, String roleId) {
        return mHttpDataSource.getGamePhotoAlbumList(serverId, roleId);
    }

    @Override
    public Observable<BaseResponse> setGameState(int gameState) {
        return mHttpDataSource.setGameState(gameState);
    }

    @Override
    public Observable<BaseResponse> commitRoleInfo(RequestBody requestBody) {
        return mHttpDataSource.commitRoleInfo(requestBody);
    }

    @Override
    public Observable<BaseResponse> upUserSex(Integer sex) {
        return mHttpDataSource.upUserSex(sex);
    }

    @Override
    public Observable<BaseDataResponse<CreateOrderEntity>> createChatDetailOrder(Integer id, Integer type, Integer payType, Integer toUserId, Integer channel) {
        return mHttpDataSource.createChatDetailOrder(id, type, payType, toUserId, channel);
    }

    @Override
    public Observable<BaseDataResponse<PriceConfigEntity.Current>> getMaleRefundMsg(Integer toUserId, Integer type) {
        return mHttpDataSource.getMaleRefundMsg(toUserId, type);
    }

    @Override
    public Observable<BaseDataResponse> getTips(Integer toUserId, Integer type, String isShow) {
        return mHttpDataSource.getTips(toUserId, type, isShow);
    }

    @Override
    public Observable<BaseResponse> addIMCollect(Integer userId, Integer type) {
        return mHttpDataSource.addIMCollect(userId, type);
    }

    @Override
    public Observable<BaseDataResponse<SwitchesEntity>> getListSwitches() {
        return mHttpDataSource.getListSwitches();
    }

    @Override
    public Observable<BaseDataResponse<Map<String, Integer>>> verifyGoddessTips(Integer toUserId) {
        return mHttpDataSource.verifyGoddessTips(toUserId);
    }

    @Override
    public Observable<BaseDataResponse<PriceConfigEntity>> getPriceConfig(Integer to_user_id) {
        return mHttpDataSource.getPriceConfig(to_user_id);
    }

    @Override
    public Observable<BaseDataResponse<CallingInfoEntity.SayHiList>> getSayHiList(Integer page, Integer perPage) {
        return mHttpDataSource.getSayHiList(page, perPage);
    }

    @Override
    public Observable<BaseDataResponse<CallingInfoEntity>> getCallingInfo(Integer roomId, Integer callingType, Integer fromUserId, Integer toUserId, Integer currentUserId) {
        return mHttpDataSource.getCallingInfo(roomId, callingType, fromUserId, toUserId, currentUserId);
    }

    @Override
    public Observable<BaseDataResponse<CallingInviteInfo>> callingInviteInfo(Integer callingType, Integer fromUserId, Integer toUserId, Integer currentUserId) {
        return mHttpDataSource.callingInviteInfo(callingType, fromUserId, toUserId, currentUserId);
    }

    @Override
    public Observable<BaseDataResponse<UnlockSocialAccountConfigEntity>> getUnlockSocialAccountConfig() {
        return mHttpDataSource.getUnlockSocialAccountConfig();
    }

    @Override
    public Observable<BaseResponse> updateSocialLevel(Integer socialLevel) {
        return mHttpDataSource.updateSocialLevel(socialLevel);
    }

    @Override
    public Observable<BaseResponse> sendUserGift(Integer gift_id, Integer to_user_id, Integer amount,Integer type) {
        return mHttpDataSource.sendUserGift(gift_id, to_user_id, amount,type);
    }

    @Override
    public Observable<BaseDataResponse<GiftBagEntity>> getBagGiftInfo() {
        return mHttpDataSource.getBagGiftInfo();
    }

    @Override
    public Observable<BaseDataResponse<UserProfitPageEntity>> getUserProfitPageInfo(Long currentUserId, Integer page, Integer perPage) {
        return mHttpDataSource.getUserProfitPageInfo(currentUserId, page, perPage);
    }

    @Override
    public Observable<BaseDataResponse<CoinWalletEntity>> getUserAccount() {
        return mHttpDataSource.getUserAccount();
    }

    @Override
    public Observable<BaseDataResponse<GameCoinWalletEntity>> getUserAccountPageInfo() {
        return mHttpDataSource.getUserAccountPageInfo();
    }

    @Override
    public Observable<BaseDataResponse<BubbleEntity>> getBubbleEntity() {
        return mHttpDataSource.getBubbleEntity();
    }

    @Override
    public Observable<BaseDataResponse<AccostEntity>> getAccostList(Integer page) {
        return mHttpDataSource.getAccostList(page);
    }

    @Override
    public Observable<BaseResponse> putAccostList(List<Integer> userIds) {
        return mHttpDataSource.putAccostList(userIds);
    }

    @Override
    public Observable<BaseResponse> putAccostFirst(Integer userId) {
        return mHttpDataSource.putAccostFirst(userId);
    }

    @Override
    public Observable<BaseDataResponse<BroadcastListEntity>> getBroadcastHome(Integer sex, Integer city_id, Integer game_id, Integer is_online, Integer is_collect, Integer type, Integer page) {
        return mHttpDataSource.getBroadcastHome(sex, city_id, game_id, is_online, is_collect, type, page);
    }

    @Override
    public Observable<BaseDataResponse<List<MessageRuleEntity>>> getMessageRule() {
        return mHttpDataSource.getMessageRule();
    }

    @Override
    public Observable<BaseDataResponse> getSensitiveWords() {
        return mHttpDataSource.getSensitiveWords();
    }

    @Override
    public Observable<BaseDataResponse<PhotoAlbumEntity>> getPhotoAlbum(Integer user_id) {
        return mHttpDataSource.getPhotoAlbum(user_id);
    }

    @Override
    public Observable<BaseResponse> removeUserSound() {
        return mHttpDataSource.removeUserSound();
    }

    @Override
    public Observable<BaseDataResponse> putUserSound(String paht,Integer sound_time) {
        return mHttpDataSource.putUserSound(paht,sound_time);
    }

    @Override
    public Observable<BaseListDataResponse<SoundEntity>> getUserSound(Integer page) {
        return mHttpDataSource.getUserSound(page);
    }

    @Override
    public Observable<BaseResponse> topicalCreateMood(String describe, String start_date, List<String> images, Integer is_comment, Integer is_hide, Double longitude, Double latitude, String video, Integer news_type) {
        return mHttpDataSource.topicalCreateMood(describe, start_date, images, is_comment, is_hide, longitude, latitude, video, news_type);
    }

    @Override
    public Observable<BaseListDataResponse<BroadcastEntity>> broadcastAll(Integer page) {
        return mHttpDataSource.broadcastAll(page);
    }

    @Override
    public Observable<BaseDataResponse<List<GoodsEntity>>> pointsGoodList() {
        return mHttpDataSource.pointsGoodList();
    }


    @Override
    public Observable<BaseResponse> pushGreet(Integer type) {
        return mHttpDataSource.pushGreet(type);
    }

    @Override
    public Observable<BaseDataResponse<Map<String, String>>> isBlacklist(String userId) {
        return mHttpDataSource.isBlacklist(userId);
    }

    @Override
    public Observable<BaseListDataResponse<TaskAdEntity>> rechargeVipList() {
        return mHttpDataSource.rechargeVipList();
    }

    @Override
    public Observable<BaseDataResponse<Map<String, String>>> isOnlineUser(String userId) {
        return mHttpDataSource.isOnlineUser(userId);
    }

    @Override
    public Observable<BaseDataResponse<BrowseNumberEntity>> newsBrowseNumber() {
        return mHttpDataSource.newsBrowseNumber();
    }

    @Override
    public Observable<BaseListDataResponse<TraceEntity>> toBrowse(Integer page) {
        return mHttpDataSource.toBrowse(page);
    }

    @Override
    public Observable<BaseListDataResponse<TraceEntity>> collectFans(Integer page) {
        return mHttpDataSource.collectFans(page);
    }

    @Override
    public Observable<BaseListDataResponse<TraceEntity>> collect(Integer page) {
        return mHttpDataSource.collect(page);
    }

    @Override
    public Observable<BaseResponse> reportUserLocation(String latitude, String longitude) {
        return mHttpDataSource.reportUserLocation(latitude, longitude);
    }

    @Override
    public Observable<BaseResponse> repoetLocalGoogleOrder(Map<String, Object> map) {
        return mHttpDataSource.repoetLocalGoogleOrder(map);
    }

    @Override
    public Observable<BaseDataResponse<CreateOrderEntity>> createOrderUserDetail(Integer id, Integer type, Integer payType, Integer number) {
        return mHttpDataSource.createOrderUserDetail(id, type, payType, number);
    }

    @Override
    public Observable<BaseDataResponse<Map<String, String>>> freeSevenDay(Integer pay_type, Integer goods_type) {
        return mHttpDataSource.freeSevenDay(pay_type, goods_type);
    }

    @Override
    public Observable<BaseDataResponse<TagEntity>> tag(String to_user_id) {
        return mHttpDataSource.tag(to_user_id);
    }

    @Override
    public Observable<BaseResponse> userInvite(String code, Integer type, String channel) {
        return mHttpDataSource.userInvite(code, type, channel);
    }

    @Override
    public Observable<BaseResponse> isBindCity(Integer city_id) {
        return mHttpDataSource.isBindCity(city_id);
    }

    @Override
    public Observable<BaseResponse> regUser(String nickname, String avatar, String birthday, Integer sex, String channel) {
        return mHttpDataSource.regUser(nickname, avatar, birthday, sex, channel);
    }

    @Override
    public Observable<BaseResponse> coordinate(Double latitude, Double longitude, String county_name, String province_name) {
        return mHttpDataSource.coordinate(latitude, longitude, county_name, province_name);
    }

    @Override
    public Observable<BaseDataResponse<SwiftMessageEntity>> getSwiftMessage(Integer page) {
        return mHttpDataSource.getSwiftMessage(page);
    }

    @Override
    public Observable<BaseResponse> bindAccount(String id, String type) {
        return mHttpDataSource.bindAccount(id, type);
    }

    @Override
    public Observable<BaseDataResponse<TokenEntity>> v2Login(String phone, String code, String device_code) {
        return mHttpDataSource.v2Login(phone, code, device_code);
    }

    @Override
    public Observable<BaseDataResponse<Map<String, String>>> imagFaceUpload(String imgUrl) {
        return mHttpDataSource.imagFaceUpload(imgUrl);
    }

    @Override
    public Observable<BaseDataResponse<VersionEntity>> detectionVersion(String client) {
        return mHttpDataSource.detectionVersion(client);
    }

    @Override
    public Observable<BaseDataResponse<GoogleNearPoiBean>> nearSearchPlace(RequestBody requestBody) {
        return mHttpDataSource.nearSearchPlace(requestBody);
    }

    @Override
    public Observable<BaseDataResponse<GooglePoiBean>> textSearchPlace(RequestBody requestBody) {
        return mHttpDataSource.textSearchPlace(requestBody);
    }

    @Override
    public void putKeyValue(String key, String value) {
        mLocalDataSource.putKeyValue(key, value);
    }

    @Override
    public String readKeyValue(String key) {
        return mLocalDataSource.readKeyValue(key);
    }

    @Override
    public Integer readSwitches(String key) {
        return mLocalDataSource.readSwitches(key);
    }

    @Override
    public void putSwitches(String key, Integer value) {
        mLocalDataSource.putSwitches(key, value);
    }

    @Override
    public Boolean readVerifyGoddessTipsUser(String key) {
        return mLocalDataSource.readVerifyGoddessTipsUser(key);
    }

    @Override
    public void putVerifyGoddessTipsUser(String key, String value) {
        mLocalDataSource.putVerifyGoddessTipsUser(key, value);
    }

    @Override
    public Boolean readChatPushStatus() {
        return mLocalDataSource.readChatPushStatus();
    }

    @Override
    public void saveChatPushStatus(int value) {
        mLocalDataSource.saveChatPushStatus(value);
    }

    @Override
    public void saveVersion(String code) {
        mLocalDataSource.saveVersion(code);
    }

    @Override
    public Boolean readVersion() {
        return mLocalDataSource.readVersion();
    }

    @Override
    public void clearChannelAF() {
        mLocalDataSource.clearChannelAF();
    }

    @Override
    public String readChannelAF() {
        return mLocalDataSource.readChannelAF();
    }

    @Override
    public void saveChannelAF(String channel) {
        mLocalDataSource.saveChannelAF(channel);
    }

    @Override
    public SystemConfigTaskEntity readSystemConfigTask() {
        return mLocalDataSource.readSystemConfigTask();
    }

    @Override
    public void saveSystemConfigTask(SystemConfigTaskEntity entity) {
        mLocalDataSource.saveSystemConfigTask(entity);
    }

    @Override
    public Map<String, String> redMessageTagUser() {
        return mLocalDataSource.redMessageTagUser();
    }

    @Override
    public void saveMessageTagUser(Map map) {
        mLocalDataSource.saveMessageTagUser(map);
    }

    @Override
    public void clearOneLinkCode() {
        mLocalDataSource.clearOneLinkCode();
    }

    @Override
    public Map<String, String> readOneLinkCode() {
        return mLocalDataSource.readOneLinkCode();
    }

    @Override
    public void saveOneLinkCode(String linkCode) {
        mLocalDataSource.saveOneLinkCode(linkCode);
    }

    @Override
    public void clearGooglePayCache() {
        mLocalDataSource.clearGooglePayCache();
    }

    @Override
    public LocalGooglePayCache readGooglePlay() {
        return mLocalDataSource.readGooglePlay();
    }

    @Override
    public void saveGooglePlay(LocalGooglePayCache localGooglePayCache) {
        mLocalDataSource.saveGooglePlay(localGooglePayCache);
    }

    @Override
    public void saveLoginInfo(TokenEntity tokenEntity) {
        mLocalDataSource.saveLoginInfo(tokenEntity);
    }

    @Override
    public TokenEntity readLoginInfo() {
        return mLocalDataSource.readLoginInfo();
    }

    @Override
    public void logout() {
        mLocalDataSource.logout();
    }

    @Override
    public void saveUserData(UserDataEntity userDataEntity) {
        mLocalDataSource.saveUserData(userDataEntity);
    }

    @Override
    public UserDataEntity readUserData() {
        return mLocalDataSource.readUserData();
    }

    @Override
    public void saveIsVerifyFace(Boolean isVerifyFace) {
        mLocalDataSource.saveIsVerifyFace(isVerifyFace);
    }

    @Override
    public Boolean readIsVerifyFace() {
        return mLocalDataSource.readIsVerifyFace();
    }

    @Override
    public void saveIsNewUser (Boolean isNewUser) {
        mLocalDataSource.saveIsNewUser(isNewUser);
    }

    @Override
    public Boolean readIsNewUser() {
        return mLocalDataSource.readIsNewUser();
    }

    @Override
    public void saveNeedVerifyFace(boolean needVerifyFace) {
        mLocalDataSource.saveNeedVerifyFace(needVerifyFace);
    }

    @Override
    public boolean readNeedVerifyFace() {
        return mLocalDataSource.readNeedVerifyFace();
    }

    @Override
    public void saveSystemConfig(SystemConfigEntity config) {
        mLocalDataSource.saveSystemConfig(config);
    }

     @Override
    public void saveGameConfig(List<GameConfigEntity> configs) {
        mLocalDataSource.saveGameConfig(configs);
    }

    @Override
    public List<GameConfigEntity> readGameConfig() {
        return mLocalDataSource.readGameConfig();
    }


    @Override
    public SystemConfigEntity readSystemConfig() {
        return mLocalDataSource.readSystemConfig();
    }

    @Override
    public void saveProgramTimeConfig(List<ConfigItemEntity> configs) {
        mLocalDataSource.saveProgramTimeConfig(configs);
    }

    @Override
    public List<ConfigItemEntity> readProgramTimeConfig() {
        return mLocalDataSource.readProgramTimeConfig();
    }

    @Override
    public void saveHeightConfig(List<ConfigItemEntity> configs) {
        mLocalDataSource.saveHeightConfig(configs);
    }

    @Override
    public List<ConfigItemEntity> readHeightConfig() {
        return mLocalDataSource.readHeightConfig();
    }

    @Override
    public void saveWeightConfig(List<ConfigItemEntity> configs) {
        mLocalDataSource.saveWeightConfig(configs);
    }

    @Override
    public List<ConfigItemEntity> readWeightConfig() {
        return mLocalDataSource.readWeightConfig();
    }

    @Override
    public void saveSensitiveWords(List<String> configs) {
        mLocalDataSource.saveSensitiveWords(configs);
    }

    @Override
    public List<String> readSensitiveWords() {
        return mLocalDataSource.readSensitiveWords();
    }

    @Override
    public void saveReportReasonConfig(List<ConfigItemEntity> configs) {
        mLocalDataSource.saveReportReasonConfig(configs);
    }

    @Override
    public List<ConfigItemEntity> readReportReasonConfig() {
        return mLocalDataSource.readReportReasonConfig();
    }

    @Override
    public void saveFemaleEvaluateConfig(List<EvaluateObjEntity> configs) {
        mLocalDataSource.saveFemaleEvaluateConfig(configs);
    }

    @Override
    public void saveMaleEvaluateConfig(List<EvaluateObjEntity> configs) {
        mLocalDataSource.saveMaleEvaluateConfig(configs);
    }

    @Override
    public List<EvaluateObjEntity> readFemaleEvaluateConfig() {
        return mLocalDataSource.readFemaleEvaluateConfig();
    }

    @Override
    public List<EvaluateObjEntity> readMaleEvaluateConfig() {
        return mLocalDataSource.readMaleEvaluateConfig();
    }

    @Override
    public List<EvaluateObjEntity> readEvaluateConfig() {
        return mLocalDataSource.readEvaluateConfig();
    }

    @Override
    public void saveHopeObjectConfig(List<ConfigItemEntity> configs) {
        mLocalDataSource.saveHopeObjectConfig(configs);
    }

    @Override
    public List<ConfigItemEntity> readHopeObjectConfig() {
        return mLocalDataSource.readHopeObjectConfig();
    }

    @Override
    public void saveOccupationConfig(List<OccupationConfigItemEntity> configs) {
        mLocalDataSource.saveOccupationConfig(configs);
    }

    @Override
    public List<OccupationConfigItemEntity> readOccupationConfig() {
        return mLocalDataSource.readOccupationConfig();
    }

    @Override
    public void saveThemeConfig(List<ConfigItemEntity> configs) {
        mLocalDataSource.saveThemeConfig(configs);
    }

    @Override
    public List<ConfigItemEntity> readThemeConfig() {
        return mLocalDataSource.readThemeConfig();
    }

    @Override
    public void saveCityConfig(List<ConfigItemEntity> configs) {
        mLocalDataSource.saveCityConfig(configs);
    }

    @Override
    public List<ConfigItemEntity> readCityConfig() {
        return mLocalDataSource.readCityConfig();
    }

    @Override
    public void saveChatCustomMessageStatus(String msgId, int status) {
        mLocalDataSource.saveChatCustomMessageStatus(msgId, status);
    }

    @Override
    public int readCahtCustomMessageStatus(String msgId) {
        return mLocalDataSource.readCahtCustomMessageStatus(msgId);
    }

    @Override
    public void saveChatMessageIsSound(Boolean isSound) {
        mLocalDataSource.saveChatMessageIsSound(isSound);
    }

    @Override
    public Boolean readChatMessageIsSound() {
        return mLocalDataSource.readChatMessageIsSound();
    }

    @Override
    public void saveChatMessageIsShake(Boolean isShake) {
        mLocalDataSource.saveChatMessageIsShake(isShake);
    }

    @Override
    public Boolean readChatMessageIsShake() {
        return mLocalDataSource.readChatMessageIsShake();
    }

    @Override
    public void saveDefaultHomePageConfig(String pageName) {
        mLocalDataSource.saveDefaultHomePageConfig(pageName);
    }

    @Override
    public String readDefaultHomePageConfig() {
        return mLocalDataSource.readDefaultHomePageConfig();
    }

    @Override
    public void saveIsFrist(Boolean isFrist) {
        mLocalDataSource.saveIsFrist(isFrist);
    }

    @Override
    public Boolean readIsFrist() {
        return mLocalDataSource.readIsFrist();
    }

    @Override
    public void savePassword(String password) {
        mLocalDataSource.savePassword(password);
    }

    @Override
    public String readPassword() {
        return mLocalDataSource.readPassword();
    }


    //---------------------- HTTP --------------------


    @Override
    public Observable<BaseResponse> verifyCodePost(String phone) {
        return mHttpDataSource.verifyCodePost(phone);
    }

    @Override
    public Observable<BaseDataResponse<TokenEntity>> register(String phone, String password, String code) {
        return mHttpDataSource.register(phone, password, code);
    }

    @Override
    public Observable<BaseResponse> acceptUseAgreement() {
        return mHttpDataSource.acceptUseAgreement();
    }

    @Override
    public Observable<BaseDataResponse<TokenEntity>> login(String phone, String password) {
        return mHttpDataSource.login(phone, password);
    }

    @Override
    public Observable<BaseDataResponse<AuthLoginUserEntity>> authLoginPost(String id, String type) {
        return mHttpDataSource.authLoginPost(id, type);
    }

    @Override
    public Observable<BaseListDataResponse<ParkItemEntity>> homeList(Integer cityId, Integer type, Integer isOnline, Integer sex, String searchName, Double longitude, Double latitude, Integer page) {
        return mHttpDataSource.homeList(cityId, type, isOnline, sex, searchName, longitude, latitude, page);
    }

    @Override
    public Observable<BaseDataResponse<UserInfoEntity>> getUserInfo() {
        return mHttpDataSource.getUserInfo();
    }

    @Override
    public Observable<BaseDataResponse<List<RadioTwoFilterItemEntity>>> getGameCity() {
        return mHttpDataSource.getGameCity();
    }


    @Override
    public Observable<BaseDataResponse<UserDataEntity>> getUserData() {
        return mHttpDataSource.getUserData();
    }

    @Override
    public Observable<BaseResponse> userRemark(Integer user_id, String nickname, String remark) {
        return mHttpDataSource.userRemark(user_id, nickname, remark);
    }

    @Override
    public Observable<BaseDataResponse<UserRemarkEntity>> getUserRemark(Integer userId) {
        return mHttpDataSource.getUserRemark(userId);
    }

    @Override
    public Observable<BaseResponse> updateAvatar(String avatar) {
        return mHttpDataSource.updateAvatar(avatar);
    }

    @Override
    public Observable<BaseResponse> updateUserData(String nickname, List<Integer> permanent_city_ids, String birthday, String occupation, List<Integer> program_ids, List<Integer> hope_object_ids, String facebook, String insgram,Integer accountType, Integer is_weixin_show, Integer height, Integer weight, String desc) {
        return mHttpDataSource.updateUserData(nickname, permanent_city_ids, birthday, occupation, program_ids, hope_object_ids, facebook, insgram,accountType, is_weixin_show, height, weight, desc);
    }

    @Override
    public Observable<BaseDataResponse<UserDetailEntity>> userMain(Integer id, Double longitude, Double latitude) {
        return mHttpDataSource.userMain(id, longitude, latitude);
    }

    @Override
    public Observable<BaseResponse> topicalCreate(Integer theme_id, String describe, String address, List<Integer> hope_object, String start_date, Integer end_time, List<String> images, Integer is_comment, Integer is_hide, String address_name, Integer city_id, Double longitude, Double latitude, String video) {
        return mHttpDataSource.topicalCreate(theme_id, describe, address, hope_object, start_date, end_time, images, is_comment, is_hide, address_name, city_id, longitude, latitude, video);
    }

    @Override
    public Observable<BaseResponse> singUp(Integer id, String img) {
        return mHttpDataSource.singUp(id, img);
    }

    @Override
    public Observable<BaseListDataResponse<BlackEntity>> getBlackList(Integer page) {
        return mHttpDataSource.getBlackList(page);
    }

    @Override
    public Observable<BaseResponse> addBlack(Integer user_id) {
        return mHttpDataSource.addBlack(user_id);
    }

    @Override
    public Observable<BaseResponse> deleteBlack(Integer id) {
        return mHttpDataSource.deleteBlack(id);
    }

    @Override
    public Observable<BaseResponse> deleteTopical(Integer id) {
        return mHttpDataSource.deleteTopical(id);
    }

    @Override
    public Observable<BaseListDataResponse<ParkItemEntity>> getCollectList(int page, Double latitude, Double longitude) {
        return mHttpDataSource.getCollectList(page, latitude, longitude);
    }

    @Override
    public Observable<BaseResponse> addCollect(Integer userId) {
        return mHttpDataSource.addCollect(userId);
    }

    @Override
    public Observable<BaseResponse> deleteCollect(Integer userId) {
        return mHttpDataSource.deleteCollect(userId);
    }

    @Override
    public Observable<BaseResponse> newsCreate(String content, List<String> images, Integer is_comment, Integer is_hide) {
        return mHttpDataSource.newsCreate(content, images, is_comment, is_hide);
    }

    @Override
    public Observable<BaseDataResponse<NewsEntity>> newsDetail(Integer id) {
        return mHttpDataSource.newsDetail(id);
    }

    @Override
    public Observable<BaseResponse> deleteNews(Integer id) {
        return mHttpDataSource.deleteNews(id);
    }

    @Override
    public Observable<BaseListDataResponse<BroadcastEntity>> broadcast(Integer type, Integer theme_id, Integer is_online, Integer city_id, Integer sex, Integer page) {
        return mHttpDataSource.broadcast(type, theme_id, is_online, city_id, sex, page);
    }

    @Override
    public Observable<BaseListDataResponse<NewsEntity>> getNewsList(Integer user_id, Integer page) {
        return mHttpDataSource.getNewsList(user_id, page);
    }


    @Override
    public Observable<BaseListDataResponse<TopicalListEntity>> getTopicalList(Integer userId, Integer page) {
        return mHttpDataSource.getTopicalList(userId, page);
    }

    @Override
    public Observable<BaseResponse> report(Integer id, String type, String reasonId, List<String> images, String desc) {
        return mHttpDataSource.report(id, type, reasonId, images, desc);
    }

    @Override
    public Observable<BaseResponse> topicalComment(Integer id, String content, Integer to_user_id) {
        return mHttpDataSource.topicalComment(id, content, to_user_id);
    }

    @Override
    public Observable<BaseResponse> newsComment(Integer id, String content, Integer to_user_id) {
        return mHttpDataSource.newsComment(id, content, to_user_id);
    }

    @Override
    public Observable<BaseDataResponse<StatusEntity>> evaluateStatus(Integer userId) {
        return mHttpDataSource.evaluateStatus(userId);
    }

    @Override
    public Observable<BaseResponse> evaluateCreate(Integer userId, Integer tagId, String img) {
        return mHttpDataSource.evaluateCreate(userId, tagId, img);
    }

    @Override
    public Observable<BaseDataResponse<List<EvaluateEntity>>> evaluate(Integer userId) {
        return mHttpDataSource.evaluate(userId);
    }

    @Override
    public Observable<BaseResponse> newsGive(Integer id) {
        return mHttpDataSource.newsGive(id);
    }

    @Override
    public Observable<BaseDataResponse<IsChatEntity>> isChat(Integer userId) {
        return mHttpDataSource.isChat(userId);
    }

    @Override
    public Observable<BaseResponse> useVipChat(Integer userId, Integer type) {
        return mHttpDataSource.useVipChat(userId, type);
    }

    @Override
    public Observable<BaseResponse> imgeReadLog(Integer image_id) {
        return mHttpDataSource.imgeReadLog(image_id);
    }

    @Override
    public Observable<BaseResponse> password(String original_password, String new_password) {
        return mHttpDataSource.password(original_password, new_password);
    }

    @Override
    public Observable<BaseDataResponse<AllConfigEntity>> getAllConfig() {
        return mHttpDataSource.getAllConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getProgramTimeConfig() {
        return mHttpDataSource.getProgramTimeConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getHeightConfig() {
        return mHttpDataSource.getHeightConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getWeightConfig() {
        return mHttpDataSource.getWeightConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getReportReasonConfig() {
        return mHttpDataSource.getReportReasonConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getEvaluateConfig() {
        return mHttpDataSource.getEvaluateConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getHopeObjectConfig() {
        return mHttpDataSource.getHopeObjectConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<OccupationConfigItemEntity>>> getOccupationConfig() {
        return mHttpDataSource.getOccupationConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getThemeConfig() {
        return mHttpDataSource.getThemeConfig();
    }

    @Override
    public Observable<BaseDataResponse<List<ConfigItemEntity>>> getCityConfig() {
        return mHttpDataSource.getCityConfig();
    }

    @Override
    public Observable<BaseResponse> userVerify(Integer user_id, String img) {
        return mHttpDataSource.userVerify(user_id, img);
    }

    @Override
    public Observable<BaseDataResponse<List<GoodsEntity>>> goods() {
        return mHttpDataSource.goods();
    }

    @Override
    public Observable<BaseDataResponse<List<VipPackageItemEntity>>> vipPackages() {
        return mHttpDataSource.vipPackages();
    }

    @Override
    public Observable<BaseDataResponse> saveVerifyFaceImage(String imgPath) {
        return mHttpDataSource.saveVerifyFaceImage(imgPath);
    }

    @Override
    public Observable<BaseResponse> cashOut(float money) {
        return mHttpDataSource.cashOut(money);
    }

    @Override
    public Observable<BaseResponse> sendCcode(String phone) {
        return mHttpDataSource.sendCcode(phone);
    }

    @Override
    public Observable<BaseListDataResponse<AlbumPhotoEntity>> albumImage(Integer userId, Integer type) {
        return mHttpDataSource.albumImage(userId, type);
    }

    @Override
    public Observable<BaseResponse> albumInsert(Integer fileType, String src, Integer isBurn, String videoImage) {
        return mHttpDataSource.albumInsert(fileType, src, isBurn, videoImage);
    }

    @Override
    public Observable<BaseDataResponse<List<AlbumPhotoEntity>>> delAlbumImage(Integer id) {
        return mHttpDataSource.delAlbumImage(id);
    }

    @Override
    public Observable<BaseResponse> setBurnAlbumImage(Integer imgId, Boolean state) {
        return mHttpDataSource.setBurnAlbumImage(imgId, state);
    }

    @Override
    public Observable<BaseResponse> setRedPackageAlbumImage(Integer imgId, Boolean state) {
        return mHttpDataSource.setRedPackageAlbumImage(imgId, state);
    }

    @Override
    public Observable<BaseResponse> setRedPackageAlbumVideo(Integer videoId, Boolean state) {
        return mHttpDataSource.setRedPackageAlbumVideo(videoId, state);
    }

    @Override
    public Observable<BaseDataResponse<FaceVerifyTokenEntity>> faceVerifyToken() {
        return mHttpDataSource.faceVerifyToken();
    }

    @Override
    public Observable<BaseDataResponse<CompareFaceEntity>> compareFaces(String img) {
        return mHttpDataSource.compareFaces(img);
    }

    @Override
    public Observable<BaseDataResponse<FaceVerifyResultEntity>> faceVerifyResult(String bizId) {
        return mHttpDataSource.faceVerifyResult(bizId);
    }

    @Override
    public Observable<BaseDataResponse<StatusEntity>> faceIsCertification() {
        return mHttpDataSource.faceIsCertification();
    }

    @Override
    public Observable<BaseDataResponse<PrivacyEntity>> getPrivacy() {
        return mHttpDataSource.getPrivacy();
    }

    @Override
    public Observable<BaseResponse> setPrivacy(PrivacyEntity privacyEntity) {
        return mHttpDataSource.setPrivacy(privacyEntity);
    }

    @Override
    public Observable<BaseResponse> updatePhone(String phone, int code, String password) {
        return mHttpDataSource.updatePhone(phone, code, password);
    }

    @Override
    public Observable<BaseResponse> applyGoddess(List<String> images) {
        return mHttpDataSource.applyGoddess(images);
    }

    @Override
    public Observable<BaseDataResponse<StatusEntity>> applyGoddessResult() {
        return mHttpDataSource.applyGoddessResult();
    }

    @Override
    public Observable<BaseResponse> resetPassword(String phone, int code, String password) {
        return mHttpDataSource.resetPassword(phone, code, password);
    }

    @Override
    public Observable<BaseResponse> setSex(int sex) {
        return mHttpDataSource.setSex(sex);
    }

    @Override
    public Observable<BaseDataResponse<CashWalletEntity>> cashWallet() {
        return mHttpDataSource.cashWallet();
    }

    @Override
    public Observable<BaseDataResponse<CoinWalletEntity>> coinWallet() {
        return mHttpDataSource.coinWallet();
    }

    @Override
    public Observable<BaseResponse> setWithdrawAccount(String realName, String account) {
        return mHttpDataSource.setWithdrawAccount(realName, account);
    }

    @Override
    public Observable<BaseDataResponse<ImSigEntity>> getImSig() {
        return mHttpDataSource.getImSig();
    }

    @Override
    public Observable<BaseResponse> setAlbumPrivacy(Integer type, Integer money) {
        return mHttpDataSource.setAlbumPrivacy(type, money);
    }

    @Override
    public Observable<BaseResponse> setComment(Integer id, Integer isComment) {
        return mHttpDataSource.setComment(id, isComment);
    }

    @Override
    public Observable<BaseListDataResponse<ApplyMessageEntity>> getMessageApply(Integer page) {
        return mHttpDataSource.getMessageApply(page);
    }

    @Override
    public Observable<BaseListDataResponse<BoradCastMessageEntity>> getMessageBoradcast(Integer page) {
        return mHttpDataSource.getMessageBoradcast(page);
    }

    @Override
    public Observable<BaseListDataResponse<CommentMessageEntity>> getMessageComment(Integer page) {
        return mHttpDataSource.getMessageComment(page);
    }

    @Override
    public Observable<BaseListDataResponse<EvaluateMessageEntity>> getMessageEvaluate(Integer page) {
        return mHttpDataSource.getMessageEvaluate(page);
    }

    @Override
    public Observable<BaseListDataResponse<GiveMessageEntity>> getMessageGive(Integer page) {
        return mHttpDataSource.getMessageGive(page);
    }

    @Override
    public Observable<BaseListDataResponse<SignMessageEntity>> getMessageSign(Integer page) {
        return mHttpDataSource.getMessageSign(page);
    }

    @Override
    public Observable<BaseListDataResponse<SystemMessageEntity>> getMessageSystem(Integer page) {
        return mHttpDataSource.getMessageSystem(page);
    }

    @Override
    public Observable<BaseListDataResponse<ProfitMessageEntity>> getMessageProfit(Integer page) {
        return mHttpDataSource.getMessageProfit(page);
    }

    @Override
    public Observable<BaseResponse> evaluateAppeal(Integer messageId, Integer tagId) {
        return mHttpDataSource.evaluateAppeal(messageId, tagId);
    }

    @Override
    public Observable<BaseDataResponse<List<MessageGroupEntity>>> getMessageList() {
        return mHttpDataSource.getMessageList();
    }

    @Override
    public Observable<BaseResponse> deleteMessage(String type, Integer id) {
        return mHttpDataSource.deleteMessage(type, id);
    }

    @Override
    public Observable<BaseDataResponse<PushSettingEntity>> getPushSetting() {
        return mHttpDataSource.getPushSetting();
    }

    @Override
    public Observable<BaseResponse> savePushSetting(PushSettingEntity pushSettingEntity) {
        return mHttpDataSource.savePushSetting(pushSettingEntity);
    }

    @Override
    public Observable<BaseResponse> TopicalGive(Integer id) {
        return mHttpDataSource.TopicalGive(id);
    }

    @Override
    public Observable<BaseResponse> TopicalFinish(Integer id) {
        return mHttpDataSource.TopicalFinish(id);
    }

    @Override
    public Observable<BaseResponse> burnReset() {
        return mHttpDataSource.burnReset();
    }

    @Override
    public Observable<BaseDataResponse<CreateOrderEntity>> createOrder(Integer id, Integer type, Integer payType, Integer toUserId) {
        return mHttpDataSource.createOrder(id, type, payType, toUserId);
    }

    @Override
    public Observable<BaseResponse> coinPayOrder(String orderNumber) {
        return mHttpDataSource.coinPayOrder(orderNumber);
    }

    @Override
    public Observable<BaseListDataResponse<BaseUserBeanEntity>> getNewsGiveList(Integer id, Integer page) {
        return mHttpDataSource.getNewsGiveList(id, page);
    }

    @Override
    public Observable<BaseListDataResponse<BaseUserBeanEntity>> getTopicalGiveList(Integer id, Integer page) {
        return mHttpDataSource.getTopicalGiveList(id, page);
    }

    @Override
    public Observable<BaseDataResponse<TopicalListEntity>> topicalDetail(Integer id) {
        return mHttpDataSource.topicalDetail(id);
    }

    @Override
    public Observable<BaseResponse> checkTopical() {
        return mHttpDataSource.checkTopical();
    }

    @Override
    public Observable<BaseResponse> signUpReport(Integer id) {
        return mHttpDataSource.signUpReport(id);
    }

    @Override
    public Observable<BaseDataResponse<ChatRedPackageEntity>> sendCoinRedPackage(Integer userId, Integer money, String desc) {
        return mHttpDataSource.sendCoinRedPackage(userId, money, desc);
    }

    @Override
    public Observable<BaseDataResponse<ChatRedPackageEntity>> getCoinRedPackage(int id) {
        return mHttpDataSource.getCoinRedPackage(id);
    }

    @Override
    public Observable<BaseResponse> receiveCoinRedPackage(int id) {
        return mHttpDataSource.receiveCoinRedPackage(id);
    }

    @Override
    public Observable<BaseListDataResponse<UserCoinItemEntity>> userCoinEarnings(int page) {
        return mHttpDataSource.userCoinEarnings(page);
    }

    @Override
    public Observable<BaseDataResponse<UserConnMicStatusEntity>> userIsConnMic(int userId) {
        return mHttpDataSource.userIsConnMic(userId);
    }

    @Override
    public Observable<BaseResponse> paySuccessNotify(String packageName, String orderNumber, List<String> productId, String token, int type, Integer event) {
        return mHttpDataSource.paySuccessNotify(packageName, orderNumber, productId, token, type, event);
    }

    @Override
    public Observable<BaseResponse> pushDeviceToken(String deviceId, String version_number) {
        return mHttpDataSource.pushDeviceToken(deviceId, version_number);
    }

    @Override
    public Observable<BaseResponse> replyApplyAlubm(int applyId, boolean status) {
        return mHttpDataSource.replyApplyAlubm(applyId, status);
    }

    @Override
    public Observable<BaseResponse> checkApplyAlbumPhoto(int applyId) {
        return mHttpDataSource.checkApplyAlbumPhoto(applyId);
    }

    @Override
    public Observable<BaseDataResponse<StatusEntity>> publishCheck(int type) {
        return mHttpDataSource.publishCheck(type);
    }

    @Override
    public Observable<BaseDataResponse<UnReadMessageNumEntity>> getUnreadMessageNum() {
        return mHttpDataSource.getUnreadMessageNum();
    }

    @Override
    public Observable<BaseDataResponse<CoinExchangeBoxInfo>> getCoinExchangeBoxInfo() {
        return mHttpDataSource.getCoinExchangeBoxInfo();
    }

    @Override
    public Observable<BaseResponse> exchangeCoins(int id) {
        return mHttpDataSource.exchangeCoins(id);
    }

    @Override
    public Observable<BaseDataResponse<List<GameCoinBuy>>> buyGameCoins() {
        return mHttpDataSource.buyGameCoins();
    }
}
