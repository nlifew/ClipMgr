package cn.nlifew.clipmgr.request;

import android.content.DialogInterface;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import cn.nlifew.clipmgr.request.IRequestFinish;

public abstract class OnRequestFinishListener extends IRequestFinish.Stub {
    public static final int RESULT_UNKNOWN  =   0;
    public static final int RESULT_CANCEL   =   1;
    public static final int RESULT_POSITIVE =   1 << 1;
    public static final int RESULT_NEGATIVE =   1 << 2;
    public static final int RESULT_REMEMBER =   1 << 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RESULT_UNKNOWN, RESULT_CANCEL, RESULT_POSITIVE,
            RESULT_NEGATIVE, RESULT_REMEMBER})
    public @interface flag {  };
}
