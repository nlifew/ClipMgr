package cn.nlifew.clipmgr.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.litepal.LitePal;

import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.bean.PackageRule;
import cn.nlifew.clipmgr.core.ClipMgr;
import cn.nlifew.clipmgr.core.IClipMgr;
import cn.nlifew.clipmgr.util.PackageUtils;
import cn.nlifew.clipmgr.util.ToastUtils;

public class ClipMgrService extends Service {
    public ClipMgrService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ClipMgr(this);
    }

}
