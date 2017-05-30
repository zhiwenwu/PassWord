package cn.xuemcu.password;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by 朱红晨 on 2017/5/29.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static String CREATE_PASSWORD = "create table PassWord ("
            + "ID integer primary key autoincrement, "
            + "websiteName text, "
            + "accounts text, "
            + "passWord text)";
    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PASSWORD);
        Toast.makeText(mContext,"Create PassWord Succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
