package com.bytedance.AndroidFinal;

import android.provider.BaseColumns;

public class CommentContract {

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CommentEntry.TABLE_NAME + " (" +
                    CommentEntry._ID + " INTEGER PRIMARY KEY," +
                    CommentEntry.COLUMN_NAME_VIEDOID + " TEXT," +
                    CommentEntry.COLUMN_NAME_TIME + " TEXT," +
                    CommentEntry.COLUMN_NAME_USER + " TEXT," +
                    CommentEntry.COLUMN_NAME_CONTENT + " TEXT)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + CommentEntry.TABLE_NAME;

    private CommentContract() {

    }

    public static class CommentEntry implements BaseColumns {

        public static final String TABLE_NAME = "comment";

        public static final String COLUMN_NAME_VIEDOID = "video_id";

        public static final String COLUMN_NAME_CONTENT = "content";

        public static final String COLUMN_NAME_TIME = "time";

        public static final String COLUMN_NAME_USER = "user";
    }
}
