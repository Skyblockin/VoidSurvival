package com.skyblockin.voidsurvival.storage.database;

import java.sql.Connection;

@FunctionalInterface
public interface DatabaseFunction<T> {

    T execute(Connection connection) throws Exception;

}
