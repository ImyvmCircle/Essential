package com.imyvm.essential.interfaces;

public interface PlayerEntityMixinInterface {
    void updateAwayFromKeyboard(boolean awayFromKeyboard);

    boolean isAwayFromKeyboard();

    void updateActivity();
}
