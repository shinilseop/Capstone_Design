package kr.ac.kongju.witlab.kket_controller_automode.database;

import android.provider.BaseColumns;

/**
 * Created by WitLab on 2018-05-04.
 */

public final class QueryRepository {
    public static final String TABLENAME_CONTROL_PACKET = "control_packet";
    public static final String TABLENAME_LOGS = "logs";
    public static final String COL_IDX = "idx";
    public static final String COL_M0 = "m0";
    public static final String COL_M1 = "m1";
    public static final String COL_M2 = "m2";
    public static final String COL_M3 = "m3";
    public static final String COL_DATETIME = "datetime";

    public static final class Create implements BaseColumns {
        public static final String QUERY_CREATE_TABLE_CONTROL_PACKET =
                "CREATE TABLE IF NOT EXISTS " + TABLENAME_CONTROL_PACKET + " (" +
                        COL_IDX + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        COL_M0 + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_M1 + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_M2 + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_M3 + " INTEGER NOT NULL DEFAULT 0" +
                        ");";

        public static final String QUERY_CREATE_TABLE_LOGS =
                "CREATE TABLE IF NOT EXISTS " + TABLENAME_LOGS + " (" +
                        COL_DATETIME + " TEXT PRIMARY KEY, " +
                        COL_IDX + " INTEGER NOT NULL," +
                        COL_M0 + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_M1 + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_M2 + " INTEGER NOT NULL DEFAULT 0, " +
                        COL_M3 + " INTEGER NOT NULL DEFAULT 0" +
                ");";
    }

    public static final class Drop implements BaseColumns {
        public static final String QUERY_DROP_TABLE_CTRLPKT =
                "DROP TABLE IF EXISTS " + TABLENAME_CONTROL_PACKET;

        public static final String QUERY_DROP_TABLE_LOGS =
                "DROP TABLE IF EXISTS " + TABLENAME_LOGS;
    }

    public static final class Select implements BaseColumns {
        public static final String QUERY_HEAD_SELECT_PACKET =
                "SELECT * FROM " + TABLENAME_CONTROL_PACKET +
                " WHERE " + COL_IDX + " = ";

        public static final String QUERY_SELECT_LASTLOG =
                "SELECT * FROM " + TABLENAME_LOGS + " ORDER BY DATETIME DESC LIMIT 1;";

        public static final String QUERY_DEBUG =
                "SELECT * FROM " + TABLENAME_CONTROL_PACKET + ";";
    }
}
