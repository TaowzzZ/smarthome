package cn.edu.shu.vpr_test_1.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import cn.edu.shu.vpr_test_1.R;
import cn.edu.shu.vpr_test_1.adapter.UserAdapter;
import cn.edu.shu.vpr_test_1.bean.UserBean;
import cn.edu.shu.vpr_test_1.global.DemoApp;
import cn.edu.shu.vpr_test_1.util.ErrorDesc;
import cn.edu.shu.vpr_test_1.util.FuncUtil;

/**
 * Created by win8 on 2017/5/7.
 */

public class ManageActivity extends Activity implements View.OnClickListener {
    private final static String TAG = ManageActivity.class.getSimpleName();
    // 身份验证对象
    private IdentityVerifier mIdVerifier;
    //创建的组号
    public String group_id = "2271479337";


    private UserAdapter mUserAdapter;
    private List<String> username;
    private Toast mToast;
    private ProgressDialog mProDialog;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        username = new ArrayList<>();
        mIdVerifier = IdentityVerifier.createVerifier(this, null);
        // 画面初期化
        initLayout();
        // 先查询群成员
        queryGroup();

        // 绑定XML中的ListView，作为Item的容器
        mList = (ListView) findViewById(R.id.lv_my_user);
        // 去除行与行之间的黑线：
        //mList.setDivider(null);
        // 添加并且显示
        mUserAdapter = new UserAdapter(username, ManageActivity.this);
        mList.setAdapter(mUserAdapter);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isDelete(username.get(position));
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        queryGroup();
    }

    /**
     * 画面初期化。
     */
    @SuppressLint("ShowToast")
    private void initLayout() {

        mProDialog = new ProgressDialog(this);
        // 等待框设置为不可取消
        mProDialog.setCancelable(true);
        mProDialog.setCanceledOnTouchOutside(false);
        mProDialog.setTitle("请稍候");

        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // cancel进度框时,取消正在进行的操作
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        findViewById(R.id.bt_add).setOnClickListener(this);   //添加新成员


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar_manage);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    @Override
    public void onClick(View v) {
        if (null == mIdVerifier) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

        switch (v.getId()) {
            case R.id.bt_add:
                /**
                 * 点击添加按钮对应的响应事件
                 */
                Intent intent = new Intent(ManageActivity.this, AddMemberActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 开启进度条
     */
    private void startProgress(String msg) {
        mProDialog.setMessage(msg);
        mProDialog.show();
        //((RelativeLayout)findViewById(R.id.group_manager_layout)).setEnabled(false);
    }

    /**
     * 关闭进度条
     */
    private void stopProgress() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
    }


    /**
     * 查询指定组中成员
     */
    private void queryGroup() {
        startProgress("正在刷新组成员...");

        // sst=add，auth_id=eqhe，group_id=123456，scope=person
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, DemoApp.getHostUser().getUsername());
        // 设置模型参数，若无可以传空字符传
        StringBuffer params2 = new StringBuffer();
        params2.append("scope=group");
        params2.append(",group_id=" + group_id);
        // 执行模型操作
        mIdVerifier.execute("ipt", "query", params2.toString(), mQueryListener);
    }


    // 删除组监听器
    private void deleteGroup(String str) {

        startProgress("正在删除...");

        // sst=add，auth_id=eqhe，group_id=123456，scope=person
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, str);

        // 设置模型参数，若无可以传空字符传
        StringBuffer params2 = new StringBuffer();

        // 删除组中指定auth_id用户
        params2.append("scope=person");
        params2.append(",auth_id="+str);

        params2.append(",group_id=" + group_id);
        // 执行模型操作
        mIdVerifier.execute("ipt", "delete", params2.toString(), mDeleteListener);
    }

    // 查询组成员监听器
    private IdentityListener mQueryListener = new IdentityListener() {
        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.i(TAG, result.getResultString());
            Gson gson = new Gson();

            UserBean userBean = gson.fromJson(result.getResultString(), UserBean.class);
            List<UserBean.PersonBean> person = userBean.getPerson();
            if(username.size()>0)
                username.clear();

            for (UserBean.PersonBean personBean : person) {
                username.add(personBean.getUser());
            }

            mUserAdapter.notifyDataSetChanged();

            showTip("查询组成员成功");
            stopProgress();
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }

        @Override
        public void onError(SpeechError error) {
            Log.d(TAG, error.getPlainDescription(true));
            showTip(ErrorDesc.getDesc(error) + ":" + error.getErrorCode());
            stopProgress();
        }
    };

    private void isDelete(final String str) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ManageActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage("确认删除成员"+str+"?");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteGroup(str);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }


    // 删除成员监听器
    private IdentityListener mDeleteListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());
            try {
                JSONObject resObj = new JSONObject(result.getResultString());
                int ret = resObj.getInt("ret");
                if (0 != ret) {
                    onError(new SpeechError(ret));
                    return;
                } else {
                    if (result.getResultString().contains("user")) {
                        String user = resObj.getString("user");
                        showTip("删除组成员" + user + "成功");
                    } else {
                        showTip("删除组成功");
                        // 保存到用户信息中，用来显示用户加人的组
                        DemoApp.getHostUser().getGroup_Hashlist()
                                .remove(resObj.getString("group_id"));
                        FuncUtil.saveObject(ManageActivity.this, DemoApp.getHostUser(),
                                DemoApp.getHostUser().getUsername());

                        // 保存到历史记录中
                        DemoApp.getmHisList().removeHisItem(resObj.getString("group_id"));
                        FuncUtil.saveObject(ManageActivity.this, DemoApp.getmHisList(), DemoApp.HIS_FILE_NAME);

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            queryGroup();
            mUserAdapter.setArray(username);
            mUserAdapter.notifyDataSetChanged();
            stopProgress();
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }

        @Override
        public void onError(SpeechError error) {
            Log.d(TAG, error.getPlainDescription(true));
            showTip(ErrorDesc.getDesc(error) + ":" + error.getErrorCode());
            stopProgress();
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }


}

