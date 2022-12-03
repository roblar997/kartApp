package com.example.kartapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.kartapp.database.models.Severdighet;

import java.util.ArrayList;
import java.util.List;

public class DbHandlerSeverdighet extends SQLiteOpenHelper {

    static String KEY_LAT = "lat";
    static String KEY_LNG = "lng";
    static String KEY_GATEADRESSE = "gateadresse";
    static String KEY_BESKRIVELSE = "beskrivelse";
    static String TABLE_SEVERDIGHETER = "Severdighet";
//

    static int DATABASE_VERSION = 3;static String DATABASE_NAME = "s349967";
    public DbHandlerSeverdighet(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public int oppdaterSeverdighet(SQLiteDatabase db, Severdighet severdighet) {
        ContentValues values = new ContentValues();
        values.put(KEY_LAT, severdighet.getLat());
        values.put(KEY_LNG, severdighet.getLng());
        values.put(KEY_BESKRIVELSE, severdighet.getBeskrivelse());
        values.put(KEY_GATEADRESSE, severdighet.getGateadresse());
        int endret = db.update(TABLE_SEVERDIGHETER , values, " ROUND("+KEY_LAT + ",12) = " + String.format("%.12f",severdighet.getLat()) + " AND ROUND(" + KEY_LNG + ",12) = " + String.format("%.12f",severdighet.getLng()) ,
                null);

        return endret;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String LAG_TABELL = "CREATE TABLE IF NOT EXISTS " + TABLE_SEVERDIGHETER+ "(" + KEY_LAT +" DOUBLE," + KEY_LNG + " DOUBLE," + KEY_GATEADRESSE + " TEXT," + KEY_BESKRIVELSE + " TEXT,"
                + "PRIMARY KEY (" + KEY_LAT + "," + KEY_LNG + "))";

        Log.d("SQL", LAG_TABELL);
        db.execSQL(LAG_TABELL);
    }
    public void slettSeverdighet(SQLiteDatabase db, Double inn_lat, Double inn_lng) {

        db.delete(TABLE_SEVERDIGHETER , " ROUND("+KEY_LAT + ",12) = " + String.format("%.12f",inn_lat) + " AND ROUND(" + KEY_LNG + ",12) = " + String.format("%.12f",inn_lng) ,
               null);


    }


    public List<Severdighet> finnAlleSeverdigheter(SQLiteDatabase db) {

        List<Severdighet> severdighetList = new ArrayList<Severdighet>();
        String selectQuery = "SELECT * FROM " + TABLE_SEVERDIGHETER;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Severdighet severdighet = new Severdighet();
                severdighet.setLat(cursor.getDouble(0));
                severdighet.setLng(cursor.getDouble(1));
                severdighet.setGateadresse(cursor.getString(2));
                severdighet.setBeskrivelse(cursor.getString(3));
                severdighetList.add(severdighet);}
                      while (cursor.moveToNext());
                cursor.close();
             }
              return severdighetList;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEVERDIGHETER );

    }

    public void leggTilSeverdighet(SQLiteDatabase db, Severdighet severdighet) {
        ContentValues values = new ContentValues();
        values.put(KEY_LAT, severdighet.getLat());
        values.put(KEY_LNG,  severdighet.getLng());
        values.put(KEY_GATEADRESSE, severdighet.getGateadresse());
        values.put(KEY_BESKRIVELSE, severdighet.getBeskrivelse());
        db.insert(TABLE_SEVERDIGHETER , null, values);
    }
    @Override
    public void close() {
        super.close();

    }
}