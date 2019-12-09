package cn.nlifew.clipmgr.ui.main.record;

import android.graphics.drawable.Drawable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.nlifew.clipmgr.bean.ActionRecord;

class ActionRecordWrapper {

    final ActionRecord mRecord;

    ActionRecordWrapper(ActionRecord r) {
        mRecord = r;
    }

    Drawable mIcon;
    String mTime;
}
