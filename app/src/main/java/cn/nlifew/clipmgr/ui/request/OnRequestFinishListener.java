package cn.nlifew.clipmgr.ui.request;

public abstract class OnRequestFinishListener extends IRequestFinish.Stub {

    public static final int RESULT_UNKNOWN  =   0;
    public static final int RESULT_CANCEL   =   1;
    public static final int RESULT_POSITIVE =   1 << 1;
    public static final int RESULT_NEGATIVE =   1 << 2;
    public static final int RESULT_REMEMBER =   1 << 3;
}
