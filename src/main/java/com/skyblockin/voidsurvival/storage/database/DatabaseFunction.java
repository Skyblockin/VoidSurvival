package com.skyblockin.voidsurvival.storage.database;

@FunctionalInterface
public interface DatabaseFunction<T, R> {

    R execute(T object) throws Exception;

}
