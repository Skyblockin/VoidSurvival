package com.skyblockin.voidsurvival.storage.database;

import java.sql.Connection;

public interface DatabaseConsumer {

    void consume(Connection connection) throws Exception;

}
