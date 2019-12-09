package cn.nlifew.clipmgr.ui.main.record;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.nlifew.clipmgr.app.ThisApp;
import cn.nlifew.clipmgr.bean.ActionRecord;
import cn.nlifew.clipmgr.settings.Settings;

class LoadActionRecordTask extends AsyncTask<Void, Void, List<ActionRecordWrapper>> {
    private static final String TAG = "LoadActionRecordTask";

    private static final Date sDate = new Date();
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.CHINA
    );

    LoadActionRecordTask(ActionRecordAdapter adapter) {
        mAdapter = new WeakReference<>(adapter);
        mPm = adapter.mContext.getPackageManager();
    }

    private final WeakReference<ActionRecordAdapter> mAdapter;
    private final PackageManager mPm;

    @Override
    protected List<ActionRecordWrapper> doInBackground(Void... voids) {
        List<ActionRecord> records = LitePal
                .order("time desc")
                .find(ActionRecord.class);

        boolean showSystemApp = Settings
                .getInstance(ThisApp.getContext())
                .isShowSystemApp();

        List<ActionRecordWrapper> wrappers = new ArrayList<>(records.size());

        for (ActionRecord r : records) {
            ApplicationInfo info;

            try {
                info = mPm.getApplicationInfo(r.getPkg(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "doInBackground: " + r.getPkg(), e);
                r.delete();
                continue;
            }

            // 过滤掉不喜欢的 app (

            if (! showSystemApp && (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue;
            }

            ActionRecordWrapper wrapper = new ActionRecordWrapper(r);
            wrappers.add(wrapper);

            sDate.setTime(r.getTime());
            wrapper.mTime = sDateFormat.format(sDate);
            wrapper.mIcon = info.loadIcon(mPm);
        }
        return wrappers;
    }

    @Override
    protected void onPostExecute(List<ActionRecordWrapper> actionRecords) {
        final ActionRecordAdapter adapter = mAdapter.get();
        if (adapter == null) {
            Log.w(TAG, "onPostExecute: Adapter has been recycled");
            return;
        }
        adapter.updateDataSet(actionRecords);
        adapter.stopLoading();
    }
}
