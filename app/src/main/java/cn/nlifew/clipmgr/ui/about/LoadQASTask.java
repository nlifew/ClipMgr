package cn.nlifew.clipmgr.ui.about;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import androidx.lifecycle.MutableLiveData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class LoadQASTask extends AsyncTask<String, Void, List<QAS>> {
    private static final String TAG = "LoadQASTask";

    LoadQASTask(AssetManager am, MutableLiveData<Exception> err, MutableLiveData<List<QAS>> result) {
        mAm = am;
        mError = err;
        mResult = result;
    }

    private final AssetManager mAm;
    private final MutableLiveData<Exception> mError;
    private final MutableLiveData<List<QAS>> mResult;


    @Override
    protected List<QAS> doInBackground(String... strings) {
        List<QAS> list = new ArrayList<>();

        try (InputStream is = mAm.open(strings[0])) {
            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(is, "UTF-8");

            for (int ev = xml.getEventType(); ev != XmlPullParser.END_DOCUMENT; ev = xml.next()) {
                if (ev != XmlPullParser.START_TAG || ! "item".equals(xml.getName())) {
                    continue;
                }
                final String q = xml.getAttributeValue(null, "question");
                final String a = xml.nextText().trim();

                list.add(new QAS(q, a));
            }
            mResult.postValue(list);
        } catch (IOException| XmlPullParserException e) {
            mError.postValue(e);
            Log.e(TAG, "doInBackground: " + Arrays.toString(strings), e);
        }
        return null;
    }
}
