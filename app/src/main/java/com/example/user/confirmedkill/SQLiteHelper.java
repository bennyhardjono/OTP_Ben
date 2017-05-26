package com.example.user.confirmedkill;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by User on 12/18/2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Password.db";

    public static final String TABLE = "Client_VDZ";
    public static final String c_id = "app_id";
    public static final String c_password = "password";


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

            String CREATE_TABLE = "CREATE TABLE " + TABLE + "("
                    + c_id + " INTEGER PRIMARY KEY, "
                    + c_password + " TEXT " + ")";
            sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE);
        //Cursor res = sqLiteDatabase.rawQuery("Select * from "+ TABLE, null);
        //if(!(res.getCount()>0)){
        //    onCreate(sqLiteDatabase);
        //}
    }


    public boolean insertID(String app_id)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(c_id, app_id);
        long result = sqLiteDatabase.insert(TABLE, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getID()
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("Select app_id from "+ TABLE, null);
        return res;
    }

    public boolean insertPassword(String password, String id)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(c_password, password);
        long result = sqLiteDatabase.update(TABLE, contentValues, "app_id="+ id, null);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getPassword()
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("Select password from "+ TABLE, null);
        return res;
    }
}
