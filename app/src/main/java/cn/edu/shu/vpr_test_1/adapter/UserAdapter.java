package cn.edu.shu.vpr_test_1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.edu.shu.vpr_test_1.R;


/**
 * Created by win8 on 2017/5/4.
 */

public class UserAdapter extends BaseAdapter{

    private List<String> mList;
    private Context mContext;

    public UserAdapter(List<String> list, Context context) {
        mList = list;
        mContext = context;
    }

    public void setArray(List<String> list) {
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {

        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String item = getItem(position);
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, null);
        TextView item_user = (TextView) view.findViewById(R.id.item_user);
        TextView item_user_number = (TextView) view.findViewById(R.id.item_user_number);
        item_user.setText(item);
        item_user_number.setText(position+1+"、");
        return view;
    }
}
