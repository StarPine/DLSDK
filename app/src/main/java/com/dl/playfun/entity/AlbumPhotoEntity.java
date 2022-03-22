package com.dl.playfun.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.dl.playfun.BR;

/**
 * @author wulei
 */
public class AlbumPhotoEntity extends BaseObservable implements Parcelable {
    public static final Creator<AlbumPhotoEntity> CREATOR = new Creator<AlbumPhotoEntity>() {
        @Override
        public AlbumPhotoEntity createFromParcel(Parcel source) {
            return new AlbumPhotoEntity(source);
        }

        @Override
        public AlbumPhotoEntity[] newArray(int size) {
            return new AlbumPhotoEntity[size];
        }
    };
    private Integer id;
    private String src;
    private int type;
    @SerializedName("is_burn")
    private int isBurn;
    @SerializedName("is_red_package")
    private int isRedPackage;
    private float money;
    @SerializedName("created_at")
    private String createAt;
    @SerializedName("burn_status")
    private int burnStatus;
    @SerializedName("verification_type")
    private int verificationType;
    @SerializedName("is_pay")
    private int isPay;
    @Expose(serialize = false, deserialize = false)
    private String msgId;

    public AlbumPhotoEntity() {
    }

    protected AlbumPhotoEntity(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.src = in.readString();
        this.type = in.readInt();
        this.isBurn = in.readInt();
        this.isRedPackage = in.readInt();
        this.money = (Float) in.readValue(Float.class.getClassLoader());
        this.createAt = in.readString();
        this.burnStatus = (Integer) in.readValue(Integer.class.getClassLoader());
        this.verificationType = (Integer) in.readValue(Integer.class.getClassLoader());
        this.isPay = (Integer) in.readValue(Integer.class.getClassLoader());
        this.msgId = in.readString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Bindable
    public int getIsBurn() {
        return isBurn;
    }

    public void setIsBurn(int isBurn) {
        this.isBurn = isBurn;
        notifyPropertyChanged(BR.isBurn);
    }

    @Bindable
    public int getIsRedPackage() {
        return isRedPackage;
    }

    public void setIsRedPackage(int isRedPackage) {
        this.isRedPackage = isRedPackage;
        notifyPropertyChanged(BR.isRedPackage);
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    @Bindable
    public int getBurnStatus() {
        return burnStatus;
    }

    public void setBurnStatus(int burnStatus) {
        this.burnStatus = burnStatus;
        notifyPropertyChanged(BR.burnStatus);
    }

    public int getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(int verificationType) {
        this.verificationType = verificationType;
    }

    @Bindable
    public int getIsPay() {
        return isPay;
    }

    public void setIsPay(int isPay) {
        this.isPay = isPay;
        notifyPropertyChanged(BR.isPay);
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(Float money) {
        this.money = money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.src);
        dest.writeInt(this.type);
        dest.writeInt(this.isBurn);
        dest.writeInt(this.isRedPackage);
        dest.writeValue(this.money);
        dest.writeString(this.createAt);
        dest.writeValue(this.burnStatus);
        dest.writeValue(this.verificationType);
        dest.writeValue(this.isPay);
        dest.writeString(this.msgId);
    }
}
