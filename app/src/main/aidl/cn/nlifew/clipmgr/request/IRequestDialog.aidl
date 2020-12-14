// IRequestDialog.aidl
package cn.nlifew.clipmgr.request;

import cn.nlifew.clipmgr.request.RequestDialogParam;

interface IRequestDialog {
    void show(String packageName, in RequestDialogParam param);
}