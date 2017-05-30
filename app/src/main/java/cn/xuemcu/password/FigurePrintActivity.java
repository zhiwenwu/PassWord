package cn.xuemcu.password;

import android.app.Activity;
import android.os.Bundle;

import com.ant.liao.GifView;

/**
 * Created by 朱红晨 on 2017/5/30.
 */

public class FigurePrintActivity extends Activity {
    private GifView gifView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_figureprint);

        gifView = (GifView) findViewById(R.id.gifView);
        gifView.setGifImage(R.drawable.zhiwen);
    }
}
