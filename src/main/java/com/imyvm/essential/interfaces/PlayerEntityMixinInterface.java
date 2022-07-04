package com.imyvm.essential.interfaces;

public interface PlayerEntityMixinInterface {
    void imyvm$updateAwayFromKeyboard(boolean awayFromKeyboard);

    boolean imyvm$isAwayFromKeyboard();

    void imyvm$updateActivity();

    void imyvm$lazyTick();
}
