package cn.misection.supermario.controller;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import cn.misection.supermario.R;
import cn.misection.supermario.databinding.ActivityMainBinding;

/**
 * @author javaman
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    public void onBackPressed() {
        //响应返回按钮事件
        mBinding.gameView.setPause(true);
        new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("是否退出游戏")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        setContentView(new View(MainActivity.this));
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mBinding.gameView.setPause(false);
                    }
                })
                .setCancelable(false)
                .show();
//		super.onBackPressed();
    }
}
