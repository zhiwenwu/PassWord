package cn.xuemcu.password;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 朱红晨 on 2017/5/29.
 */

public class PassWordAdapter extends ArrayAdapter<PassWord> {
    private int resourceId;
    public PassWordAdapter(Context context, int resource, List<PassWord> objects) {
        super(context, resource, objects);
        this.resourceId = resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        PassWord passWord = getItem(position);
        View view;
        if(convertView == null)
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        else
            view = convertView;
        TextView textView = (TextView) view.findViewById(R.id.tvWebsiteName);
        textView.setText(passWord.getWebsiteName());

        textView = (TextView) view.findViewById(R.id.tvAccounts);
        textView.setText("帐号:"+passWord.getAccounts());
        return view;
    }
}
