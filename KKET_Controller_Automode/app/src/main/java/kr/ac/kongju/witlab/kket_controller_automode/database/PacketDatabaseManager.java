package kr.ac.kongju.witlab.kket_controller_automode.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by WitLab on 2018-05-04.
 */

public class PacketDatabaseManager {
    private static final String DATABASE_NAME = "PacketDB(SQLite).db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    private Context mContext;

    private final int valueInterval = 16;

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x ", b));
        return sb.toString();
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
//            sqLiteDatabase.execSQL(QueryRepository.Drop.QUERY_DROP_TABLE_CTRLPKT);
//            sqLiteDatabase.execSQL(QueryRepository.Drop.QUERY_DROP_TABLE_LOGS);
//            sqLiteDatabase.execSQL(QueryRepository.Create.QUERY_CREATE_TABLE_CONTROL_PACKET);
            sqLiteDatabase.execSQL(QueryRepository.Create.QUERY_CREATE_TABLE_CONTROL_PACKET);
            sqLiteDatabase.execSQL(QueryRepository.Create.QUERY_CREATE_TABLE_LOGS);
            generatePacketTable(sqLiteDatabase);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            onCreate(sqLiteDatabase);
        }
    }

    public PacketDatabaseManager(Context context) {
        this.mContext = context;
    }

    public PacketDatabaseManager open() throws SQLException {
        dbHelper = new DatabaseHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void create() {
        dbHelper.onCreate(db);
    }

    public void close() {
        // 강제 db 삭제
        db.execSQL(QueryRepository.Drop.QUERY_DROP_TABLE_CTRLPKT);

        db.close();
    }

    public String selectLastLogToString() {
        Cursor cursor = db.rawQuery(QueryRepository.Select.QUERY_SELECT_LASTLOG, null);
        StringBuilder sb = new StringBuilder();
        if (cursor.moveToFirst()) {
            sb.append(cursor.getString(cursor.getColumnIndex(QueryRepository.COL_DATETIME)));
            sb.append(", ");
            sb.append(cursor.getInt(cursor.getColumnIndex(QueryRepository.COL_IDX)));
            sb.append(", ");
            sb.append(cursor.getInt(cursor.getColumnIndex(QueryRepository.COL_M0)));
            sb.append(", ");
            sb.append(cursor.getInt(cursor.getColumnIndex(QueryRepository.COL_M1)));
            sb.append(", ");
            sb.append(cursor.getInt(cursor.getColumnIndex(QueryRepository.COL_M2)));
            sb.append(", ");
            sb.append(cursor.getInt(cursor.getColumnIndex(QueryRepository.COL_M3)));
        }
        return sb.toString();
    }

    public byte[] getPacket(int idx) {
        byte[] retpacket = {(byte)0x02, (byte)0x11, (byte)0xFF, 0, 0, 0, 0, (byte)0x03};
        Cursor cursor = db.rawQuery(QueryRepository.Select.QUERY_HEAD_SELECT_PACKET + idx + ";", null);
        while (cursor.moveToNext()) {
            Log.d("getPacket()", "moveToNext() == true, idx = "+idx);
            for (int i = 2; i < retpacket.length; i++) {
                retpacket[i] = (byte) cursor.getInt(i - 1);
                Log.d("getPacket()","packet i = "+i+", "+retpacket[i]);
            }
            Log.d("getPacket()", PacketDatabaseManager.byteArrayToHex(retpacket));
        }

        insertLog(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), idx, retpacket);
        cursor.close();

        return retpacket;
    }

    private void insertLog(String datetime, int idx, byte[] packet) {
        ContentValues values = new ContentValues();
        values.put(QueryRepository.COL_DATETIME, datetime);
        values.put(QueryRepository.COL_IDX, idx);
        values.put(QueryRepository.COL_M0, packet[2]);
        values.put(QueryRepository.COL_M1, packet[3]);
        values.put(QueryRepository.COL_M2, packet[4]);
        values.put(QueryRepository.COL_M3, packet[5]);
        db.insert(QueryRepository.TABLENAME_LOGS, null, values);
    }

    private void generatePacketTable(SQLiteDatabase db) {
        Cursor c;
        c = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE name = \'" + QueryRepository.TABLENAME_CONTROL_PACKET + "\'", null);
        Log.d("generatePacketTable()", " data count "+c.getCount());
        if (c.getCount() > 2) {  // if exist
            Log.d("generatePacketTable()", "there is table! don\'t have to insert packet tables.");
            return;
        }

        for (int i = 0; i <= 256; i += valueInterval)
            for (int j = 0; j <= 256; j += valueInterval)
                for (int k = 0; k <= 256; k += valueInterval)
                    for (int l = 0; l <= 256; l += valueInterval) {
                        if (i + j + k + l > 256)
                            continue;
                        else if (i + j + k + l == 256) {
                            if (l > 0)
                                l -= 1;
                            else if (k > 0)
                                k -= 1;
                            else if (j > 0)
                                j -= 1;
                            else
                                i -= 1;
                        }
                        insert(db, i, j, k, l);
                        Log.d("generatePacketTable()", "insert " + i + ", " + j + ", " + k + ", " + l);
                    }
    }

    private void insert(SQLiteDatabase db, int m0, int m1, int m2, int m3) {
        ContentValues values = new ContentValues();
        values.put(QueryRepository.COL_M0, m0);
        values.put(QueryRepository.COL_M1, m1);
        values.put(QueryRepository.COL_M2, m2);
        values.put(QueryRepository.COL_M3, m3);
        db.insert(QueryRepository.TABLENAME_CONTROL_PACKET, null, values);
    }

    public String selectDebug(String query) {
        Cursor cursor = db.rawQuery(query, null);
        StringBuilder sb = new StringBuilder();
        Log.d("selectDebug()", "cursor.getCount() = " + cursor.getCount());
        while (cursor.moveToNext()) {
            if (cursor.getColumnCount() == 5) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    sb.append(cursor.getColumnName(i));
                    sb.append(": ");
                    sb.append(cursor.getInt(i));
                    sb.append(", ");
                }
            } else {
                sb.append(cursor.getColumnName(0));
                sb.append(": ");
                sb.append(cursor.getString(0));
                sb.append(", ");
                for (int i = 1; i < cursor.getColumnCount(); i++) {
                    sb.append(cursor.getColumnName(i));
                    sb.append(": ");
                    sb.append(cursor.getInt(i));
                    sb.append(", ");
                }
            }
            sb.append("\n");
        }
        cursor.close();

        if(sb.toString().length() == 0)
            sb.append("<no results>");
        return sb.toString();
    }
}
