package com.skyblockin.voidsurvival.storage.database;

public class SQLiteTable {

    protected final SQLiteDatabase database;
    private final String tableName;
    private final String[] columns;

    public SQLiteTable(SQLiteDatabase database, String tableName, String... columns) {
        this.database = database;
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String[] getColumns() {
        return this.columns;
    }

}
