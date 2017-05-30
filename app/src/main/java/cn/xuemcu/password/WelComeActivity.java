package cn.xuemcu.password;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 朱红晨 on 2017/5/28.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class WelComeActivity extends Activity {
    private static final int GOTO_MAIN_ACTIVITY = 0;
    private static final int GOTO_MAIN_FigurePrint = 1;
    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0;

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
                    //intent = new Intent();
                    //intent.setClass(WelComeActivity.this, FigurePrintActivity.class);
                    //startActivity(intent);
                    //finish();
                    AlertDialog.Builder builder = new AlertDialog.Builder(WelComeActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    View view = LayoutInflater.from(WelComeActivity.this).inflate(R.layout.layout_fingerprint,null);
                    initView(view);
                    builder.setView(view);
                    builder.setCancelable(false);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Window window = dialog.getWindow();
                    window.setGravity(Gravity.TOP);
                    startListening(null);
                    break;
                default:
                    break;
            }
        };
    };

    TextView[] tv = new TextView[5];
    private int postion = 0;
    private void initView(View view) {
        postion = 0;
        tv[0] = (TextView) view.findViewById(R.id.tv_1);
        tv[1] = (TextView) view.findViewById(R.id.tv_2);
        tv[2] = (TextView) view.findViewById(R.id.tv_3);
        tv[3] = (TextView) view.findViewById(R.id.tv_4);
        tv[4] = (TextView) view.findViewById(R.id.tv_5);
        handler.sendEmptyMessageDelayed(0,100);
    }

    private Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                int i = postion % 5;
                if (i == 0){
                    tv[4].setBackground(null);
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                else{
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    tv[i-1].setBackground(null);
                }
                postion++;
                handler.sendEmptyMessageDelayed(0,100);
            }
        }
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

    /**
     * 如果识别失败次数过多,则转入输入解锁密码界面，
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showAuthenticationScreen() {
        Intent intent = mKeyManager.createConfirmDeviceCredentialIntent("finger", "测试指纹识别");
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    /**该对象提供了取消操作的能力。创建该对象也很简单，使用 new CancellationSignal() 就可以了。**/
    CancellationSignal mCancellationSignal = new CancellationSignal();

    /**回调方法**/
    FingerprintManager.AuthenticationCallback mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            // 验证出错回调 指纹传感器会关闭一段时间,在下次调用authenticate时,会出现禁用期(时间依厂商不同30,1分都有)
            Toast.makeText(WelComeActivity.this, errString, Toast.LENGTH_SHORT).show();
            showAuthenticationScreen();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            // 验证帮助回调
            Toast.makeText(WelComeActivity.this, helpString, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {  //验证成功
            Toast.makeText(WelComeActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setClass(WelComeActivity.this, PassWordActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onAuthenticationFailed() {
            // 验证失败  指纹验证失败后,指纹传感器不会立即关闭指纹验证,系统会提供5次重试的机会,即调用5次onAuthenticationFailed后,才会调用onAuthenticationError
            Toast.makeText(WelComeActivity.this, "指纹识别失败", Toast.LENGTH_SHORT).show();
        }
    };

    /**如果支持一系列的条件，可以认证回调，参数是加密对象**/
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        //判断是否添加指纹识别权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "没有指纹识别权限", Toast.LENGTH_SHORT).show();
            return;
        }
        /**参数分别是:防止第三方恶意攻击的包装类,CancellationSignal对象,flags,回调对象,handle**/
        manager.authenticate(cryptoObject, mCancellationSignal, 0, mSelfCancelled, null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(WelComeActivity.this, PassWordActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
                System.exit(0);
            }
        }
    }
}
