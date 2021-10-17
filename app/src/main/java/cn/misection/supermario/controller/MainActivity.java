package cn.misection.supermario.controller;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import cn.misection.supermario.ui.view.GameView;

/**
 * @author javaman
 */
public class MainActivity extends AppCompatActivity {

    private GameView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new GameView(this);
        setContentView(view);

    }

    @Override
    public void onBackPressed() {
        //响应返回按钮事件
        view.setPause(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("是否退出游戏")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setContentView(new View(MainActivity.this));
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        view.setPause(false);
                    }
                })
                .setCancelable(false)
                .show();
//		super.onBackPressed();
    }
}
