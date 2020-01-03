package cn.nlifew.clipmgr.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.ui.main.MainActivity;

public class EmptyActivity extends BaseActivity {
    private static final String TAG = "EmptyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        findViewById(R.id.activity_main_btn1)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton1((Button) v);
                    }
                });

        findViewById(R.id.activity_main_btn2)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton2((Button) v);
                    }
                });

    }

    private void onClickButton1(Button btn) {
        Log.d(TAG, "onClickButton1: " + EmptyActivity.class.getClassLoader());
        try {
            Class.forName("cn.nlifew.clipmgr.RiruCall");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "onClickButton1: ", e);
        }
    }

    private void onClickButton2(Button btn) {
        Log.d(TAG, "onClickButton2: start");
    }
}
