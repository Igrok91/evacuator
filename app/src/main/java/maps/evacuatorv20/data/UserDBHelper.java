package maps.evacuatorv20.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Igor on 17.03.2017.
 */

public class UserDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = "my";
    /**
     * Имя файла базы данных
     */
    private static final String DATABASE_NAME = "user.db";

    /**
     * Версия базы данных. При изменении схемы увеличить на единицу
     */
    private static final int DATABASE_VERSION = 1;

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }




    @Override
    public void onCreate(SQLiteDatabase db) {
        // Строка для создания таблицы
        String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserContract.User.TABLE_NAME + " ("
                + UserContract.User._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserContract.User.COLUMN_NAME + " TEXT NOT NULL, "
                + UserContract.User.COLUMN_LAST_NAME + " TEXT NOT NULL, "
                + UserContract.User.COLUMN_MIDDLE_NAME + " TEXT, "
                + UserContract.User.COLUMN_CITY + " TEXT, "
                + UserContract.User.COLUMN_DRIVER + " INTEGER, "
                + UserContract.User.COLUMN_EMAIL + " TEXT NOT NULL, "
                + UserContract.User.COLUMN_TELEPHONE + " TEXT NOT NULL, "
                + UserContract.User.COLUMN_ORDERS_COUNT + " INTEGER DEFAULT 0, "
                + UserContract.User.COLUMN_RATING + " INTEGER  DEFAULT 0);";

        // Запускаем создание таблицы
        Log.d(LOG_TAG, "success");
        db.execSQL(SQL_CREATE_USER_TABLE);
        Log.d(LOG_TAG, "success");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + UserContract.User.TABLE_NAME );

        onCreate(db);
    }
}
