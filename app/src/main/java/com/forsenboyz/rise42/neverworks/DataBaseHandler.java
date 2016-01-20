package com.forsenboyz.rise42.neverworks;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHandler {
    private DataBaseCreator baseCreator;
    private SQLiteDatabase db;

    private String currentUser;

    DataBaseHandler(Context context){
        baseCreator = new DataBaseCreator(context);
        db = baseCreator.getWritableDatabase();
        Log.d("MY_TAG","Base created "+baseCreator.getDatabaseName());
    }

    public void insertOutcome(String message){
        insertRow(currentUser,message);
    }

    public boolean isOpen(){
        return db.isOpen();
    }

    public long insertRow(String from, String message){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DataBaseCreator.SENDER_COLUMN,from);
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
        baseCreator.onUpgrade(db, DataBaseCreator.DATABASE_VERSION, Integer.valueOf(DataBaseCreator.DATABASE_VERSION + 1));
    }

    public void close(){
        baseCreator.close();
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        Log.d("MY_TAG","Database user: "+this.currentUser);
    }
}



class DataBaseCreator extends SQLiteOpenHelper {

    public static final String ID_COLUMN = "_id";
    public static final String SENDER_COLUMN = "sender";
    public static final String MESSAGE_COLUMN = "message";

    public static final String[] KEYS = {ID_COLUMN, SENDER_COLUMN,MESSAGE_COLUMN};

    public static final String DATABASE_NAME = "messageBase.db";
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_TABLE = "messages";

    DataBaseCreator(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "create table "+DATABASE_TABLE +" ("
                +ID_COLUMN+" integer primary key autoincrement, "
                + SENDER_COLUMN +" text, "
                +MESSAGE_COLUMN +" text);";
        db.execSQL(createTable);
        Log.d("MY_TAG","DataBase created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
        Log.d("MY_TAG","DataBase wasted");
        onCreate(db);
    }

}