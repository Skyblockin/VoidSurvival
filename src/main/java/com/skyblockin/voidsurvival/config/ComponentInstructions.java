package com.skyblockin.voidsurvival.config;

public interface ComponentInstructions<T> {

    T apply(T value);

}
