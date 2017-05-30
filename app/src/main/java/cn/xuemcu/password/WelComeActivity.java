package cn.xuemcu.password;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by 朱红晨 on 2017/5/28.
 */

public class WelComeActivity extends Activity {
    private static final int GOTO_MAIN_ACTIVITY = 0;
    private static final int GOTO_MAIN_FigurePrint = 1;
    private FingerprintManager manager;
    private KeyguardManager mKeyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        manager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
        mKeyManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);

        if(isFinger() == false)
            mHandler.sendEmptyMessageDelayed(GOTO_MAIN_ACTIVITY, 3000);//3秒跳转
        else
            mHandler.sendEmptyMessageDelayed(GOTO_MAIN_FigurePrint, 3000);//3秒跳转
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GOTO_MAIN_ACTIVITY:
                    Intent intent = new Intent();
                    intent.setClass(WelComeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case GOTO_MAIN_FigurePrint:
                    intent = new Intent();
                    intent.setClass(WelComeActivity.this, FigurePrintActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    break;
            }
        };
    };

    public boolean isFinger() {
        //判断当前手机版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "没有指纹识别权限", Toast.LENGTH_SHORT).show();
                return false;
            }
            Log.e("CX", "有指纹权限");
            //判断硬件是否支持指纹识别
            if (!manager.isHardwareDetected()) {
                Toast.makeText(this, "没有指纹识别模块", Toast.LENGTH_SHORT).show();
                return false;
            }
            Log.e("CX", "有指纹模块");
            //判断 是否开启锁屏密码
            if (!mKeyManager.isKeyguardSecure()) {
                Toast.makeText(this, "没有开启锁屏密码", Toast.LENGTH_SHORT).show();
                return false;
            }
            Log.e("CX", "已开启锁屏密码");
            //判断是否有指纹录入
            if (!manager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "没有录入指纹", Toast.LENGTH_SHORT).show();
                return false;
            }
            Log.e("CX", "已录入指纹");
            return true;
        } else {
            return false;
        }
    }
}
