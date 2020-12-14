package cn.nlifew.clipmgr.ui.request;

import android.content.DialogInterface;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public interface OnRequestFinishListener {
    int RESULT_UNKNOWN  =   0;
    int RESULT_CANCEL   =   1;
    int RESULT_POSITIVE =   1 << 1;
    int RESULT_NEGATIVE =   1 << 2;
    int RESULT_REMEMBER =   1 << 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RESULT_UNKNOWN, RESULT_CANCEL, RESULT_POSITIVE,
            RESULT_NEGATIVE, RESULT_REMEMBER})
    @interface flag {  };

    void onRequestFinish(@flag int result);
}
