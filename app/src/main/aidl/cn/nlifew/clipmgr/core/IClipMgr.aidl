// IClipMgr.aidl
package cn.nlifew.clipmgr.core;

import android.content.ClipData;

interface IClipMgr {
    void setPrimaryClip(String pkg, in ClipData clip);
}
