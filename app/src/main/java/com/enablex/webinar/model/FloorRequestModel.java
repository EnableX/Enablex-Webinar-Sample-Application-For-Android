package com.enablex.webinar.model;

import java.io.Serializable;

public class FloorRequestModel implements Serializable {

    String clientId;
    String name;
    boolean isRequestAccepted;
    boolean isRequestRejected;
    boolean isRequestReleased;
    private boolean isMute;
    private boolean isUnMute;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public boolean isUnMute() {
        return isUnMute;
    }

    public void setUnMute(boolean unMute) {
        isUnMute = unMute;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }

    public boolean isRequestAccepted() {
        return isRequestAccepted;
    }

    public void setRequestAccepted(boolean requestAccepted) {
        isRequestAccepted = requestAccepted;
    }

    public boolean isRequestRejected() {
        return isRequestRejected;
    }

    public void setRequestRejected(boolean requestRejected) {
        isRequestRejected = requestRejected;
    }

    public boolean isRequestReleased() {
        return isRequestReleased;
    }

    public void setRequestReleased(boolean requestReleased) {
        isRequestReleased = requestReleased;
    }
}
