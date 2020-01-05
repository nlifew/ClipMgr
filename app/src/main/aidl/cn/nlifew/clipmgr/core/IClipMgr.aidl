// IClipMgr.aidl
package cn.nlifew.clipmgr.core;

import android.content.ClipData;


interface IClipMgr {

    void saveActionRecord(String pkg, in ClipData clip, int action);

    int getPackageRule(String pkg);

    void setPackageRule(String pkg, int rule);

    boolean isRadicalMode();
}
