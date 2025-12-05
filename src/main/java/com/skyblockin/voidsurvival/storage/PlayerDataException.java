package com.skyblockin.voidsurvival.storage;

public class PlayerDataException extends Exception {

    public PlayerDataException(String errorMessage) {
        super(errorMessage);
    }

    public PlayerDataException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

}
