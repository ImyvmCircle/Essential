package com.imyvm.essential.interfaces;

import com.imyvm.essential.control.TeleportRequest;

public interface PlayerEntityMixinInterface {
    void imyvm$updateAwayFromKeyboard(boolean awayFromKeyboard);

    boolean imyvm$isAwayFromKeyboard();

    void imyvm$updateActivity();

    TeleportRequest imyvm$getRequestAsSender();

    void imyvm$setRequestAsSender(TeleportRequest requestAsSender);

    TeleportRequest imyvm$getRequestAsReceiver();

    void imyvm$setRequestAsReceiver(TeleportRequest requestAsReceiver);
}
