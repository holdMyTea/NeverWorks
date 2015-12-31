package com.forsenboyz.rise42.neverworks;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHandler {
    private DataBaseCreator baseCreator;
    private SQLiteDatabase db;

    DataBaseHandler(Context context){
        baseCreator = new DataBaseCreator(context);
        db = baseCreator.getWritableDatabase();
        ClientActivity.log("Base created "+baseCreator.getDatabaseName());
    }

    public long insertIncome(String message){
        return insertRow(true,message);
    }

    public long insertOutcome(String message){
        return insertRow(false,message);
    }

    public boolean isOpen(){
        return db.isOpen();
    }

    public long insertRow(boolean income, String message){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DataBaseCreator.INCOME_COLUMN,income);
        contentValues.put(DataBaseCreator.MESSAGE_COLUMN, message);

        return db.insert(DataBaseCreator.DATABASE_TABLE, null, contentValues);
    }

    public Cursor getRow(long rowID){
        String script = DataBaseCreator.ID_COLUMN+"="+rowID;
        Cursor c = db.query(true,DataBaseCreator.DATABASE_TABLE,DataBaseCreator.KEYS,script,null,null,null,null,null);
        if(c!=null){
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getAllRows(){
        Cursor c = db.query(true,DataBaseCreator.DATABASE_TABLE,DataBaseCreator.KEYS,null,null,null,null,null,null);
        c.moveToFirst();
        return c;
    }

    public void dropTable(){
        baseCreator.onUpgrade(db,DataBaseCreator.DATABASE_VERSION,Integer.valueOf(DataBaseCreator.DATABASE_VERSION + 1));
    }

    public void close(){
        baseCreator.close();
    }
}



class DataBaseCreator extends SQLiteOpenHelper {

    public static final String ID_COLUMN = "_id";
    public static final String INCOME_COLUMN = "income";
    public static final String MESSAGE_COLUMN = "message";

    public static final String[] KEYS = {ID_COLUMN,INCOME_COLUMN,MESSAGE_COLUMN};

    public static final String DATABASE_NAME = "messageBase.db";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_TABLE = "messages";

    DataBaseCreator(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "create table "+DATABASE_TABLE +" ("
                +ID_COLUMN+" integer primary key autoincrement, "
                +INCOME_COLUMN+" integer, "+MESSAGE_COLUMN +" text);";
        db.execSQL(createTable);
        ClientActivity.log("DataBase created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
        ClientActivity.log("DataBase wasted");
        onCreate(db);
    }

}