package com.antitheft.alarm.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.UUID;

public class DetailItem implements Parcelable {

    public UUID writeCharacter;
    public UUID readCharacter;
    public UUID service;

    public DetailItem() {

    }

    public DetailItem(UUID writeCharacter, UUID readCharacter, UUID service) {
        this.writeCharacter = writeCharacter;
        this.readCharacter = readCharacter;
        this.service = service;
    }

    public UUID getWriteCharacter() {
        return writeCharacter;
    }

    public void setWriteCharacter(UUID writeCharacter) {
        this.writeCharacter = writeCharacter;
    }

    public UUID getReadCharacter() {
        return readCharacter;
    }

    public void setReadCharacter(UUID readCharacter) {
        this.readCharacter = readCharacter;
    }

    public UUID getService() {
        return service;
    }

    public void setService(UUID service) {
        this.service = service;
    }

    public boolean isEmpty() {
        return service == null && (writeCharacter == null || readCharacter == null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.writeCharacter);
        dest.writeSerializable(this.readCharacter);
        dest.writeSerializable(this.service);
    }

    protected DetailItem(Parcel in) {
        this.writeCharacter = (UUID) in.readSerializable();
        this.readCharacter = (UUID) in.readSerializable();
        this.service = (UUID) in.readSerializable();
    }

    public static final Creator<DetailItem> CREATOR = new Creator<DetailItem>() {
        @Override
        public DetailItem createFromParcel(Parcel source) {
            return new DetailItem(source);
        }

        @Override
        public DetailItem[] newArray(int size) {
            return new DetailItem[size];
        }
    };

    @Override
    public String toString() {
        return "DetailItem{" +
                "writeCharacter=" + writeCharacter +
                ", readCharacter=" + readCharacter +
                ", service=" + service +
                '}';
    }
}
