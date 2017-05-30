package cn.xuemcu.password;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by 朱红晨 on 2017/5/28.
 */

public class PassWordActivity extends Activity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,View.OnLongClickListener,Runnable {
    private ListView listView = null;
    private List<PassWord> passWordList = new ArrayList<>();
    private PassWordAdapter passWordAdapter = null;
    private MyDatabaseHelper myDatabaseHelper = null;
    private SQLiteDatabase sqLiteDatabase = null;
    private PassWord onItemClickPassWord = null;
    private Button btnAddTo = null;
    private EditText editsousuo = null;
    private static final int UPDATE_ACTIVITY = 0;
    private DesUtils desUtils = null;
    private String sdDir = "";
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;
    private int dataExportOrImport = 0;
    private String souSuoIsChange = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // 进入该界面表明指纹或则密码验证通过
        Toast.makeText(this,"验证通过！",Toast.LENGTH_SHORT).show();

        listView = (ListView) findViewById(R.id.listPw);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        myDatabaseHelper = new MyDatabaseHelper(this, "PassWord.db", null, 1);
        sqLiteDatabase = myDatabaseHelper.getWritableDatabase();

        btnAddTo = (Button) findViewById(R.id.btnAddTo);
        btnAddTo.setOnLongClickListener(this);

        editsousuo = (EditText) findViewById(R.id.editsousuo);

        sdDir = Environment.getExternalStorageDirectory().toString();

        try {
            desUtils = new DesUtils("clovex");//自定义密钥
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            displayListView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    private void displayListView() throws Exception {
        if(sqLiteDatabase == null)
            Toast.makeText(PassWordActivity.this,"sqLiteDatabase is null !!!",Toast.LENGTH_SHORT).show();
        else {
            Cursor cursor = null;
            if(editsousuo.getText().toString().equals(""))
                cursor = sqLiteDatabase.query("PassWord",null,null,null,null,null,null);
            else {
                cursor = sqLiteDatabase.rawQuery(
                        "select * from PassWord where websiteName like ?"
                        , new String[]{"%"+editsousuo.getText().toString()+"%"});
            }
            passWordList.clear();
            if(cursor.moveToFirst()) {
                do {
                    PassWord passWord = new PassWord(cursor.getString(cursor.getColumnIndex("websiteName")),
                            cursor.getString(cursor.getColumnIndex("accounts")),
                            desUtils.decrypt(cursor.getString(cursor.getColumnIndex("passWord"))));
                    passWordList.add(passWord);
                } while(cursor.moveToNext());
            }
            cursor.close();

            this.passWordAdapter = new PassWordAdapter(PassWordActivity.this,R.layout.listview_password,passWordList);
            listView.setAdapter(passWordAdapter);
        }
    }

    private void SQLDeletePassWord(PassWord passWord) {
        if(sqLiteDatabase == null)
            Toast.makeText(PassWordActivity.this,"sqLiteDatabase is null !!!",Toast.LENGTH_SHORT).show();
        else {
            sqLiteDatabase.execSQL("delete from PassWord where websiteName = ? and accounts = ?",
                    new String[]{passWord.getWebsiteName(), passWord.getAccounts()});
            Toast.makeText(PassWordActivity.this,"删除数据！",Toast.LENGTH_SHORT).show();
        }
    }
    private void SQLChangePassWord(PassWord pw,String websiteName,String accounts,String passWord) throws Exception {
        if(sqLiteDatabase == null)
            Toast.makeText(PassWordActivity.this,"sqLiteDatabase is null !!!",Toast.LENGTH_SHORT).show();
        else {
            if(pw.getWebsiteName().equals(websiteName) && pw.getAccounts().equals(accounts)) {
                Toast.makeText(PassWordActivity.this,"修改密码！",Toast.LENGTH_SHORT).show();
                Cursor cursor = sqLiteDatabase.rawQuery(
                        "select * from PassWord where websiteName = ? and accounts = ?"
                        , new String[]{pw.getWebsiteName(), pw.getAccounts()});
                if (cursor.moveToFirst()) {
                    do {
                        sqLiteDatabase.execSQL("update PassWord set passWord = ? where websiteName = ? and accounts = ?",
                                new String[]{desUtils.encrypt(passWord),websiteName,accounts});
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } else {
                Toast.makeText(PassWordActivity.this,"删除后新建！",Toast.LENGTH_SHORT).show();
                sqLiteDatabase.execSQL("delete from PassWord where websiteName = ? and accounts = ?",
                        new String[]{pw.getWebsiteName(), pw.getAccounts()});
                SQLInsertPassWord(websiteName, accounts, passWord);
            }
        }
    }

    private void SQLInsertPassWord(String websiteName,String accounts,String passWord) throws Exception {
        if(sqLiteDatabase == null)
            Toast.makeText(PassWordActivity.this,"sqLiteDatabase is null !!!",Toast.LENGTH_SHORT).show();
        else {

            int isHave = 0;
            Cursor cursor = sqLiteDatabase.rawQuery(
                "select * from PassWord where websiteName = ? and accounts = ?"
                    , new String[]{websiteName, accounts});
            if(cursor.moveToFirst()) {
                do {
                    isHave++;
                } while(cursor.moveToNext());
            }
            cursor.close();

            if(isHave == 0) {
                ContentValues values = new ContentValues();
                values.put("websiteName", websiteName);
                values.put("accounts", accounts);
                values.put("passWord", desUtils.encrypt(passWord));
                sqLiteDatabase.insert("PassWord", null, values);
            } else {
                Toast.makeText(PassWordActivity.this,"该数据以存在！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addTo(View view) {
        //Toast.makeText(PassWordActivity.this,"Add To",Toast.LENGTH_SHORT).show();
        final CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setTitle("添加");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //设置你的操作事项
                if(!builder.getWebsiteName().equals("")) {
                    if(!builder.getAccounts().equals("")) {
                        if(!builder.getPassWord().equals("")) {
                            try {
                                SQLInsertPassWord(builder.getWebsiteName(),builder.getAccounts(),builder.getPassWord());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                displayListView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        } else {
                            Toast.makeText(PassWordActivity.this, "请输入密码！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PassWordActivity.this, "请输入帐号！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PassWordActivity.this, "请输入网站名称！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击事件
        onItemClickPassWord = passWordList.get(position);

        final CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setTitle("查看");
        builder.setWebsiteName(onItemClickPassWord.getWebsiteName());
        builder.setAccounts(onItemClickPassWord.getAccounts());
        builder.setPassWord(onItemClickPassWord.getPassWord());
        builder.setPositiveButton("修改", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //设置你的操作事项
                if(!builder.getWebsiteName().equals("")) {
                    if(!builder.getAccounts().equals("")) {
                        if(!builder.getPassWord().equals("")) {
                            try {
                                SQLChangePassWord(onItemClickPassWord,builder.getWebsiteName(),builder.getAccounts(),builder.getPassWord());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                displayListView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        } else {
                            Toast.makeText(PassWordActivity.this, "请输入密码！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PassWordActivity.this, "请输入帐号！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PassWordActivity.this, "请输入网站名称！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("删除", new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(PassWordActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                normalDialog.setTitle("提示");
                normalDialog.setMessage("确定删除该数据吗？");
                normalDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                            SQLDeletePassWord(onItemClickPassWord);
                            try {
                                displayListView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                normalDialog.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                        }
                    });
                // 显示
                normalDialog.show();

                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //长按事件
        PassWord pw = passWordList.get(position);

        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.btnAddTo) {
            final String[] items = { "导入数据","导出数据","关于软件"};
            AlertDialog.Builder listDialog = new AlertDialog.Builder(PassWordActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

            listDialog.setTitle("请选择功能");
            listDialog.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // which 下标从0开始
                    // ...To-do
                    if(which == 0) {
                        dataExportOrImport = 1;
                        if(checkPermission() == false) {
                            return;
                        } else {
                            dataImport();
                        }

                    } else if(which == 1) {
                        dataExportOrImport = 2;
                        if(checkPermission() == false) {
                            return;
                        } else {
                            dataExport();
                        }

                    } else if(which == 2) {
                        final AlertDialog.Builder normalDialog =
                                new AlertDialog.Builder(PassWordActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                        normalDialog.setTitle("关于软件");
                        normalDialog.setMessage("作者: 朱红晨 & 吴致文\n版本: V1.0.0\n时间: 2017.05.30");
                        normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //...To-do
                                }
                            });
                        // 显示
                        normalDialog.show();
                    }
                }
            });
            listDialog.show();
        }
        return true;
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_ACTIVITY:
                    try {
                        displayListView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                default:
                    break;
            }
        };
    };

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(100);
                if(!editsousuo.getText().toString().equals(souSuoIsChange)) {
                    mHandler.sendEmptyMessage(UPDATE_ACTIVITY);
                }
                souSuoIsChange = editsousuo.getText().toString();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    private boolean checkPermission() {
        /**
         * 第 1 步: 检查是否有相应的权限
         */
        boolean isAllGranted = checkPermissionAllGranted(
            new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }
        );
        // 如果这2个权限全都拥有, 则直接执行备份代码
        if (isAllGranted) {
            //有权限
            return true;
        } else {
            //无权限
            /**
             * 第 2 步: 请求权限
             */
            // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
            ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                },
                MY_PERMISSION_REQUEST_CODE
            );
            return false;
        }
    }

    /**
     * 第 3 步: 申请权限结果返回处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了
                if(dataExportOrImport == 1)
                    dataImport();
                else if(dataExportOrImport == 2)
                    dataExport();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                Toast.makeText(PassWordActivity.this, "需要权限来读写数据!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dataExport() {
        //数据导出 /PassWord/PassWord.cx
        //Toast.makeText(PassWordActivity.this, sdDir+"/PassWord/", Toast.LENGTH_SHORT).show();

        File file = new File(sdDir+"/PassWord");
        if (!file.exists())
        {
            file.mkdirs();
            Toast.makeText(PassWordActivity.this, "已创建文件夹!", Toast.LENGTH_SHORT).show();
        }

        file = new File(sdDir+"/PassWord/PassWord.cx");
        if(!file.exists()){
            //文件不存在
        } else {
            //文件存在
            if(file.isFile())
                file.delete();
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
        String date = sDateFormat.format(new java.util.Date());

        String fileName = sdDir+"/PassWord/PassWord"+"-"+date+".cx";
        //Toast.makeText(PassWordActivity.this, fileName, Toast.LENGTH_SHORT).show();

        file = new File(fileName);
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
            }
        }

        FileWriter fw = null;
        BufferedWriter bw = null;
        FileWriter fw1 = null;
        BufferedWriter bw1 = null;
        Cursor cursor;
        try {
            fw = new FileWriter(fileName, true);
            // 创建FileWriter对象，用来写入字符流
            bw = new BufferedWriter(fw); // 将缓冲对文件的输出
            fw1 = new FileWriter(sdDir+"/PassWord/PassWord.cx", true);
            // 创建FileWriter对象，用来写入字符流
            bw1 = new BufferedWriter(fw1); // 将缓冲对文件的输出

            cursor = sqLiteDatabase.query("PassWord",null,null,null,null,null,null);

            if(cursor.moveToFirst()) {
                do {
                    String pwData = cursor.getString(cursor.getColumnIndex("websiteName")) + "#"
                        + cursor.getString(cursor.getColumnIndex("accounts"))+ "#"
                        + cursor.getString(cursor.getColumnIndex("passWord"));
                    bw.write(pwData+"\r\n"); // 写入文件
                    //bw.newLine();
                    bw.flush(); // 刷新该流的缓冲
                    bw1.write(pwData+"\r\n"); // 写入文件
                    //bw1.newLine();
                    bw1.flush(); // 刷新该流的缓冲
                } while(cursor.moveToNext());
            }
            cursor.close();
            bw.close();
            fw.close();
            bw1.close();
            fw1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(PassWordActivity.this, "数据导出成功!", Toast.LENGTH_SHORT).show();
    }

    private void dataImport() {
        //数据导入
        File file = new File(sdDir+"/PassWord/PassWord.cx");
        if(!file.exists()){
            //文件不存在
            Toast.makeText(PassWordActivity.this, "文件不存在:"+sdDir+"/PassWord/PassWord.cx", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            int Have = 0,noHave = 0;
            while((lineTxt = bufferedReader.readLine()) != null){
                String[] text = lineTxt.split("#");
                Log.e("CX", lineTxt);
                Log.e("CX", text[0]);
                Log.e("CX", text[1]);
                Log.e("CX", text[2]);
                if(text.length == 3) {
                    int isHave = 0;
                    Cursor cursor = sqLiteDatabase.rawQuery(
                            "select * from PassWord where websiteName = ? and accounts = ?"
                            , new String[]{text[0], text[1]});
                    if(cursor.moveToFirst()) {
                        do {
                            isHave++;
                        } while(cursor.moveToNext());
                    }
                    cursor.close();

                    if(isHave == 0) {
                        ContentValues values = new ContentValues();
                        values.put("websiteName", text[0]);
                        values.put("accounts", text[1]);
                        values.put("passWord", text[2]);
                        sqLiteDatabase.insert("PassWord", null, values);
                        noHave++;
                    } else {
                        Have++;
                    }
                }
            }
            read.close();

            Toast.makeText(PassWordActivity.this, "已导入"+Integer.toString(Have+noHave)+
                    "条数据，其中" + Integer.toString(Have) +
                    "条已存在！", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Toast.makeText(PassWordActivity.this, "数据导入成功!", Toast.LENGTH_SHORT).show();
        try {
            displayListView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
