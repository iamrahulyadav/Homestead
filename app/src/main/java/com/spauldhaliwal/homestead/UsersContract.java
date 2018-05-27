package com.spauldhaliwal.homestead;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.spauldhaliwal.homestead.AppProvider.CONTENT_AUTHORITY;
import static com.spauldhaliwal.homestead.AppProvider.CONTENT_AUTHORITY_URI;

/**
 * Created by pauldhaliwal on 2018-03-26.
 */

public class UsersContract {

    static final String ROOT_NODE = "Users";
    static final String JOBS_NODE = "jobs";

    static final String UID = "uid";
    static final String TOKEN_ID = "tokenId";
    static final String NAME = "name";
    static final String EMAIL = "email";
    static final String PROFILE_IMAGE = "profileImage";
    static final String HOMESTEAD_ID = "homesteadId";
    static final String NOTIFICATIONS = "notifications";

    /**
     * The uri's to access the tasks table
     */

    public static final Uri CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, ROOT_NODE);

    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "." + ROOT_NODE;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "." + ROOT_NODE;


    static Uri buildJobUri(long jobId) {
        return ContentUris.withAppendedId(CONTENT_URI, jobId);
    }

    static long getJobId(Uri uri) {
        return ContentUris.parseId(uri);
    }


}
