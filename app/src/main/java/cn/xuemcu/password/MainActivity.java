package cn.xuemcu.password;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity implements Runnable {
    private static final int MainActivity_TOAST = 0;
    private static final int MainActivity_PASS = 1;
    private boolean threadFlag = true;
    private EditText editText = null;
    private Message message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.edtext);
        message = new Message();
        threadFlag = true;
        new Thread(this).start();
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MainActivity_PASS:
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, PassWordActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case MainActivity_TOAST:
                    Toast.makeText(MainActivity.this,msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    public void run() {
        while(this.threadFlag) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SimpleDateFormat sDateFormat = new SimpleDateFormat("HHmm");
            String date = sDateFormat.format(new java.util.Date());

            if(editText.getText().toString().equals(date)) {
                this.threadFlag = false;

                message.what = MainActivity_PASS;
                message.obj = "密码正确";
                this.mHandler.sendMessage(this.message);
            }
        }
    }
}
