package cn.nlifew.clipmgr.ui;


import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BaseFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    public interface LazyLoadSwitch {
        boolean willLazyLoad(BaseFragment fragment);
    }

    private boolean mLazyLoaded;
    private boolean mLazyLoadEnabled = true;
    private LazyLoadSwitch mLazyLoadSwitch;

    @Override
    public void onResume() {
        super.onResume();
        lazyLoadIfReady();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) lazyLoadIfReady();
    }

    @Override
    public void onDestroyView() {
        mLazyLoaded = false;
        super.onDestroyView();
    }

    public boolean isLazyLoadReady() {
        return mLazyLoadEnabled && ! mLazyLoaded
                && isResumed() && getUserVisibleHint()
                && (mLazyLoadSwitch == null || mLazyLoadSwitch.willLazyLoad(this));
    }

    public void lazyLoadIfReady() {
        if (isLazyLoadReady()) {
            mLazyLoaded = true;
            onLazyLoad();
        }
    }

    public void setLazyLoadEnabled(boolean b) {
        mLazyLoadEnabled = b;
    }

    public boolean isLazyLoadEnabled() {
        return mLazyLoadEnabled;
    }

    public void setLazyLoadSwitch(LazyLoadSwitch l) {
        mLazyLoadSwitch = l;
    }

    @CallSuper
    protected void onLazyLoad() {
        Log.d(TAG, "onLazyLoad: start");
    }
}
