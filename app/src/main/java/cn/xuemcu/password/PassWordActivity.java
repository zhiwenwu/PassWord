package cn.xuemcu.password;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 朱红晨 on 2017/5/28.
 */

public class PassWordActivity extends Activity implements AdapterView.OnItemClickListener {
    private ListView listView = null;
    private List<PassWord> passWordList = new ArrayList<>();
    private PassWordAdapter passWordAdapter = null;
    private MyDatabaseHelper myDatabaseHelper = null;
    private SQLiteDatabase sqLiteDatabase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // 进入该界面表明指纹或则密码验证通过
        Toast.makeText(this,"验证通过！",Toast.LENGTH_SHORT).show();

        listView = (ListView) findViewById(R.id.listPw);
        listView.setOnItemClickListener(this);

        myDatabaseHelper = new MyDatabaseHelper(this, "PassWord.db", null, 1);
        sqLiteDatabase = myDatabaseHelper.getWritableDatabase();

        PassWord passWord = new PassWord("HaHa");
        passWordList.add(passWord);

        this.passWordAdapter = new PassWordAdapter(PassWordActivity.this,R.layout.listview_password,passWordList);
        listView.setAdapter(passWordAdapter);
    }

    private void SQLInsertPassWord(String websiteName,String accounts,String passWord) {
        if(sqLiteDatabase == null)
            Toast.makeText(PassWordActivity.this,"sqLiteDatabase is null !!!",Toast.LENGTH_SHORT).show();
        else {
            ContentValues values = new ContentValues();
            values.put("websiteName", websiteName);
            values.put("accounts", accounts);
            values.put("passWord", passWord);
            sqLiteDatabase.insert("PassWord", null, values);
        }
    }

    public void addTo(View view) {
        Toast.makeText(PassWordActivity.this,"Add To",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PassWord pw = passWordList.get(position);
        Toast.makeText(PassWordActivity.this,pw.getWebsiteName(),Toast.LENGTH_SHORT).show();
    }
}
