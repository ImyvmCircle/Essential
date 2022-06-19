package com.imyvm.essential.interfaces;

import com.imyvm.essential.control.TeleportRequest;

public interface PlayerEntityMixinInterface {
    void updateAwayFromKeyboard(boolean awayFromKeyboard);

    boolean isAwayFromKeyboard();

    void updateActivity();

    TeleportRequest getRequestAsSender();

    void setRequestAsSender(TeleportRequest requestAsSender);

    TeleportRequest getRequestAsReceiver();

    void setRequestAsReceiver(TeleportRequest requestAsReceiver);
}
