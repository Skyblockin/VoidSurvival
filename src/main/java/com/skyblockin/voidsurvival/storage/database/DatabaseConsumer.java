package com.skyblockin.voidsurvival.storage.database;

public interface DatabaseConsumer<T> {

    void consume(T object) throws Exception;

}
