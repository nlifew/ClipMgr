package cn.nlifew.clipmgr.ui.about;

import android.app.Application;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class QASModel extends AndroidViewModel {

    public QASModel(@NonNull Application application) {
        super(application);
    }

    private final MutableLiveData<Exception> mError = new MutableLiveData<>(null);
    private final MutableLiveData<List<QAS>> mList = new MutableLiveData<>(null);

    LiveData<Exception> error() { return mError; }
    LiveData<List<QAS>> list() { return mList; }

    void loadData(String name) {
        AssetManager am = getApplication().getAssets();
        new LoadQASTask(am, mError, mList).execute(name);
    }
}
