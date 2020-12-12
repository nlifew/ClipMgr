package cn.nlifew.clipmgr.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.nlifew.clipmgr.R;
import cn.nlifew.clipmgr.ui.main.MainActivity;
import cn.nlifew.clipmgr.ui.request.AlertDialogLayout;
import cn.nlifew.clipmgr.ui.request.RequestDialog;
import cn.nlifew.clipmgr.util.DisplayUtils;

public class EmptyActivity extends BaseActivity {
    private static final String TAG = "EmptyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        findViewById(R.id.activity_main_btn1)
                .setOnClickListener(v -> {
                    AlertDialogLayout layout = new AlertDialogLayout(this);
                    layout.setTitle(R.string.app_name);
                    layout.setIcon(R.mipmap.ic_launcher_round);
                    layout.setMessage("\n尝试修改剪贴板为：test");
                    layout.setPositive("允许");
                    layout.setNegative("拒绝");
                    layout.setRemember("记住我的选择");

//                    AlertDialog d = new AlertDialog.Builder(this)
////                            .setView(layout)
//                            .create();
//                    d.show();

                    Dialog dialog = new Dialog(this, android.R.style
                            .Theme_Material_Light_Dialog_NoActionBar_MinWidth);
                    dialog.setContentView(layout, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    dialog.show();
                });
        findViewById(R.id.activity_main_btn2)
                .setOnClickListener(v -> {
                    new RequestDialog.Builder(this)
                            .setTitle(R.string.app_name)
                            .setMessage("\n尝试修改剪贴板为：test")
                            .setPositive("允许")
                            .setNegative("拒绝")
                            .setRemember("记住我的选择")
                            .setIcon(R.mipmap.ic_launcher_round)
                            .setCancelable(true)
                            .setCallback(result -> Log.d(TAG, "onCreate: " + result))
                            .create()
                            .show();
                });
    }
}
