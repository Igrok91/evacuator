package maps.evacuatorv20.data;

import android.provider.BaseColumns;

/**
 * Created by Igor on 17.03.2017.
 */

public final class UserContract {
    private UserContract() {

    }

    public static final class User implements BaseColumns{
        public final static String TABLE_NAME = "guests";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "first_name";
        public final static String COLUMN_LAST_NAME = "last_name";
        public final static String COLUMN_MIDDLE_NAME = "middle_name";
        public final static String COLUMN_EMAIL = "email";
        public final static String COLUMN_CITY = "city";
        public final static String COLUMN_TELEPHONE = "telephone_number";
        public final static String COLUMN_RATING = "rating";
        public final static String COLUMN_DRIVER = "driver";
        public final static String COLUMN_ORDERS_COUNT = "orders_count";
    }
}
