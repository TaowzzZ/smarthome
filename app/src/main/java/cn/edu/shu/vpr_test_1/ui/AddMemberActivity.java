package cn.edu.shu.vpr_test_1.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.cloud.util.VerifierUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.shu.vpr_test_1.R;
import cn.edu.shu.vpr_test_1.global.DemoApp;
import cn.edu.shu.vpr_test_1.util.ErrorDesc;
import cn.edu.shu.vpr_test_1.util.FuncUtil;

/**
 * Created by win8 on 2017/5/7.
 */

public class AddMemberActivity extends Activity implements View.OnClickListener {
    private static final String TAG = AddMemberActivity.class.getSimpleName();

    // 密码类型
    // 默认为数字密码
    private int mPwdType = 3;
    // 数字密码类型为3，其他类型暂未开放
    private static final int PWD_TYPE_NUM = 3;

    // 会话类型
    private int mSST = 0;
    // 注册
    private static final int SST_ENROLL = 0;
    // 验证
    private static final int SST_VERIFY = 1;

    // 模型操作类型
    private int mModelCmd;
    // 查询模型
    private static final int MODEL_QUE = 0;
    // 删除模型
    private static final int MODEL_DEL = 1;

    // 用户id，唯一标识
    private String authid;
    // 身份验证对象
    private IdentityVerifier mIdVerifier;
    // 数字声纹密码
    private String mNumPwd = "";
    // 数字声纹密码段，默认有5段
    private String[] mNumPwdSegs;
    // 用于验证的数字密码
    private String mVerifyNumPwd = "";
    //创建的组号
    public String group_id = "2271479337";

    // UI控件
    private TextView mResultEditText;
    private EditText mAuthIdEditText;
    private RadioGroup mSstTypeGroup;
    private Button mPressToTalkButton;
    private AlertDialog mTextPwdSelectDialog;
    private Toast mToast;
    private Toolbar mToolbar_add;

    // 是否可以录音
    private boolean mCanStartRecord = false;
    // 是否可以录音
    private boolean isStartWork = false;
    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;
    // 进度对话框
    private ProgressDialog mProDialog;

    /**
     * 下载密码监听器
     */
    private IdentityListener mDownloadPwdListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            mProDialog.dismiss();
            // 下载密码时，恢复按住说话触摸
            mPressToTalkButton.setClickable(true);

            switch (mPwdType) {
                case PWD_TYPE_NUM:
                    StringBuffer numberString = new StringBuffer();
                    try {
                        JSONObject object = new JSONObject(result.getResultString());
                        if (!object.has("num_pwd")) {
                            mNumPwd = null;
                            return;
                        }

                        JSONArray pwdArray = object.optJSONArray("num_pwd");
                        numberString.append(pwdArray.get(0));
                        for (int i = 1; i < pwdArray.length(); i++) {
                            numberString.append("-" + pwdArray.get(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mNumPwd = numberString.toString();
                    mNumPwdSegs = mNumPwd.split("-");

                    mResultEditText.setText("您的注册密码：\n" + mNumPwd + "\n请长按“按住说话”按钮进行注册\n");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            mProDialog.dismiss();
            // 下载密码时，恢复按住说话触摸
            mPressToTalkButton.setClickable(true);
            mResultEditText.setText("密码下载失败！" + error.getPlainDescription(true));
        }
    };

    /**
     * 声纹注册监听器
     */
    private IdentityListener mEnrollListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            JSONObject jsonResult = null;
            try {
                jsonResult = new JSONObject(result.getResultString());
                int ret = jsonResult.getInt("ret");

                if (ErrorCode.SUCCESS == ret) {

                    final int suc = Integer.parseInt(jsonResult.optString("suc"));
                    final int rgn = Integer.parseInt(jsonResult.optString("rgn"));

                    if (suc == rgn) {
                        joinGroup(authid);
                        mResultEditText.setText("注册成功");
                        mCanStartRecord = false;
                        isStartWork = false;
                        mPcmRecorder.stopRecord(true);


                    } else {
                        int nowTimes = suc + 1;
                        int leftTimes = 5 - nowTimes;

                        StringBuffer strBuffer = new StringBuffer();
                        strBuffer.append("请长按“按住说话”按钮！\n");
                        strBuffer.append("请读出：" + mNumPwdSegs[nowTimes - 1] + "\n");
                        strBuffer.append("训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
                        mResultEditText.setText(strBuffer.toString());
                    }

                } else {
                    showTip(new SpeechError(ret).getPlainDescription(true));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {
            if (SpeechEvent.EVENT_VOLUME == eventType) {
                showTip("音量：" + arg1);
            } else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
                showTip("录音结束");
            }

        }

        @Override
        public void onError(SpeechError error) {
            isStartWork = false;

            StringBuffer errorResult = new StringBuffer();
            errorResult.append("注册失败！\n");
            errorResult.append("错误信息：" + error.getPlainDescription(true) + "\n");
            errorResult.append("请长按“按住说话”重新注册!");
            mResultEditText.setText(errorResult.toString());
        }

    };

    /**
     * 声纹验证监听器
     */
    private IdentityListener mVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, "verify:" + result.getResultString());

            try {
                JSONObject object = new JSONObject(result.getResultString());
                String decision = object.getString("decision");

                if ("accepted".equalsIgnoreCase(decision)) {
                    mResultEditText.setText("验证通过");
                } else {
                    mResultEditText.setText("验证失败");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isStartWork = false;
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_VOLUME == eventType) {
                showTip("音量：" + arg1);
            } else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
                showTip("录音结束");
            }
        }

        @Override
        public void onError(SpeechError error) {
            isStartWork = false;
            mCanStartRecord = false;

            StringBuffer errorResult = new StringBuffer();
            errorResult.append("验证失败！\n");
            errorResult.append("错误信息：" + error.getPlainDescription(true) + "\n");
            errorResult.append("请长按“按住说话”重新验证!");
            mResultEditText.setText(errorResult.toString());
        }
    };

    /**
     * 声纹模型操作监听器
     */
    private IdentityListener mModelListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, "model operation:" + result.getResultString());

            mProDialog.dismiss();

            JSONObject jsonResult = null;
            int ret = ErrorCode.SUCCESS;
            try {
                jsonResult = new JSONObject(result.getResultString());
                ret = jsonResult.getInt("ret");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (mModelCmd) {
                case MODEL_QUE:
                    if (!TextUtils.isEmpty(mAuthIdEditText.getText())) {
                        authid = mAuthIdEditText.getText().toString() ;
                    } else{
                        showTip("用户名不能为空！");
                        return;
                    }

                    if (ErrorCode.SUCCESS == ret) {
                        showTip("模型存在");
                    } else {
                        showTip("模型不存在");
                    }
                    break;
                case MODEL_DEL:

                    if (!TextUtils.isEmpty(mAuthIdEditText.getText())) {
                        authid = mAuthIdEditText.getText().toString() ;
                    } else{
                        showTip("用户名不能为空！");
                        return;
                    }

                    if (ErrorCode.SUCCESS == ret) {
                        showTip("模型已删除");
                    } else {
                        showTip("模型删除失败");
                    }
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            mProDialog.dismiss();
            showTip(error.getPlainDescription(true));
        }
    };

    /**
     * 录音机监听器
     */
    private PcmRecorder.PcmRecordListener mPcmRecordListener = new PcmRecorder.PcmRecordListener() {

        @Override
        public void onRecordStarted(boolean success) {
        }

        @Override
        public void onRecordReleased() {
        }

        @Override
        public void onRecordBuffer(byte[] data, int offset, int length) {
            StringBuffer params = new StringBuffer();

            switch (mSST) {
                case SST_ENROLL:
                    params.append("rgn=5,");
                    params.append("ptxt=" + mNumPwd + ",");
                    params.append("pwdt=" + mPwdType + ",");
                    mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
                    break;
                case SST_VERIFY:
                    params.append("ptxt=" + mVerifyNumPwd + ",");
                    params.append("pwdt=" + mPwdType + ",");
                    mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(SpeechError e) {
        }
    };

    /**
     * 按压监听器
     */
    private View.OnTouchListener mPressTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if( null == mIdVerifier ){
                // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
                return false;
            }


            if (!TextUtils.isEmpty(mAuthIdEditText.getText())) {
                authid = mAuthIdEditText.getText().toString() ;
            } else{
                showTip("用户名不能为空！");
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isStartWork) {
                        // 根据业务类型调用服务
                        if (mSST == SST_ENROLL) {
                            if (null == mNumPwdSegs) {
                                // 启动录音机时密码为空，中断此次操作，下载密码
                                downloadPwd();
                                break;
                            }
                            vocalEnroll();
                        } else if (mSST == SST_VERIFY) {
                            vocalVerify();
                        } else {
                            showTip("请先选择相应业务！");
                            break;
                        }
                        isStartWork = true;
                        mCanStartRecord = true;
                    }
                    if (mCanStartRecord) {
                        try {
                            mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
                            mPcmRecorder.startRecording(mPcmRecordListener);
                        } catch (SpeechError e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();

                    mIdVerifier.stopWrite("ivp");
                    if (null != mPcmRecorder) {

                        mPcmRecorder.stopRecord(true);
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmember);

        initUI();

        mIdVerifier = IdentityVerifier.createVerifier(AddMemberActivity.this, new InitListener() {

            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });

        mToolbar_add= (Toolbar) findViewById(R.id.toolBar_add);
        mToolbar_add.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AddMemberActivity.this,ManageActivity.class) ;
                startActivity(intent);
                finish();

            }
        });
    }

    @SuppressLint("ShowToast")
    private void initUI() {
        mResultEditText = (TextView) findViewById(R.id.vocal_edt_result);
        mAuthIdEditText = (EditText) findViewById(R.id.et_name);

        mPressToTalkButton = (Button) findViewById(R.id.btn_vocal_press_to_talk);
        mPressToTalkButton.setOnTouchListener(mPressTouchListener);

        Button query = (Button) findViewById(R.id.btn_vocal_query);
        Button delete = (Button) findViewById(R.id.btn_vocal_delete);

        query.setOnClickListener(AddMemberActivity.this);
        delete.setOnClickListener(AddMemberActivity.this);

        mProDialog = new ProgressDialog(AddMemberActivity.this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍候");
        // cancel进度框时，取消正在进行的操作
        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        // 密码选择RadioGroup初始化
        mSstTypeGroup = (RadioGroup) findViewById(R.id.vocal_radioGroup1);
        mSstTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if( null == mIdVerifier ){
                    // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                    showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
                    return;
                }

                // 取消之前操作
                if (mIdVerifier.isWorking()) {
                    mIdVerifier.cancel();
                }
                cancelOperation();

                switch (checkedId) {
                    case R.id.vocal_radioVerify:
                        // 设置会话类型为验证
                        mSST = SST_VERIFY;
                        mVerifyNumPwd = VerifierUtil.generateNumberPassword(8);

                        StringBuffer strBuffer = new StringBuffer();
                        strBuffer.append("您的验证密码：" + mVerifyNumPwd + "\n");
                        strBuffer.append("请长按“按住说话”按钮进行验证！\n");
                        mResultEditText.setText(strBuffer.toString());
                        break;
                    case R.id.vocal_radioEnroll:
                        // 设置会话类型为验证
                        mSST = SST_ENROLL;

                        if (null == mNumPwdSegs) {
                            // 首次注册密码为空时，调用下载密码
                            downloadPwd();
                        } else {
                            mResultEditText.setText("请长按“按住说话”按钮进行注册\n");
                        }
                        break;
                    default:
                        break;
                }
            }
        });


        mToast = Toast.makeText(AddMemberActivity.this, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onClick(View v) {
        if( null == mIdVerifier ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
            return;
        }

        // 取消先前操作
        cancelOperation();

        switch (v.getId()) {
            case R.id.btn_vocal_query:
                // 执行查询模型
                mModelCmd = MODEL_QUE;
                executeModelCommand("query");
                break;
            case R.id.btn_vocal_delete:
                // 执行删除模型
                mModelCmd = MODEL_DEL;
                executeModelCommand("delete");
                break;
            default:
                break;
        }
    }

    private void downloadPwd() {
        // 获取密码之前先终止之前的操作
        mIdVerifier.cancel();
        mNumPwd = null;
        // 下载密码时，按住说话触摸无效
        mPressToTalkButton.setClickable(false);

        mProDialog.setMessage("下载中...");
        mProDialog.show();

        // 设置下载密码参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");

        // 子业务执行参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();
        // 设置模型操作的密码类型
        params.append("pwdt=" + mPwdType + ",");
        // 执行密码下载操作
        mIdVerifier.execute("ivp", "download", params.toString(), mDownloadPwdListener);
    }

    private void vocalEnroll() {
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("请长按“按住说话”按钮！\n");
        strBuffer.append("请读出：" + mNumPwdSegs[0] + "\n");
        strBuffer.append("训练 第" + 1 + "遍，剩余4遍\n");
        mResultEditText.setText(strBuffer.toString());

        // 设置声纹注册参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "enroll");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mEnrollListener);
    }

    private void vocalVerify() {
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("您的验证密码：" + mVerifyNumPwd + "\n");
        strBuffer.append("请长按“按住说话”按钮进行验证！\n");
        mResultEditText.setText(strBuffer.toString());
        // 设置声纹验证参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
        // 验证模式，单一验证模式：sin
        mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
        // 用户的唯一标识，在声纹业务获取注册、验证、查询和删除模型时都要填写，不能为空
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mVerifyListener);
    }

    private void cancelOperation() {
        isStartWork = false;
        mIdVerifier.cancel();

        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
    }

    private void executeModelCommand(String cmd) {
        if ("query".equals(cmd)) {
            mProDialog.setMessage("查询中...");
        } else if ("delete".equals(cmd)) {
            mProDialog.setMessage("删除中...");
        }
        mProDialog.show();
        // 设置声纹模型参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);

        // 子业务执行参数，若无可以传空字符传
        StringBuffer params3 = new StringBuffer();
        // 设置模型操作的密码类型
        params3.append("pwdt=" + mPwdType + ",");
        // 执行模型操作
        mIdVerifier.execute("ivp", cmd, params3.toString(), mModelListener);
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

    private void joinGroup(String name) {

        if (TextUtils.isEmpty(group_id)) {
            showTip("groupId数据丢失！");
            return;
        }
        startProgress("正在加入组...");

        // sst=add，auth_id=eqhe，group_id=123456，scope=person
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, name);
        // 设置模型参数，若无可以传空字符传
        StringBuffer params2 = new StringBuffer();
        params2.append("auth_id=" + name);
        params2.append(",scope=person");
        params2.append(",group_id=" + group_id);
        // 执行模型操作
        mIdVerifier.execute("ipt", "add", params2.toString(), mAddListener);
    }

    /**
     * 加入组监听器
     */
    private IdentityListener mAddListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());
            try {
                JSONObject resObj = new JSONObject(result.getResultString());
                // 保存到用户信息中，用来显示用户加人的组
                DemoApp.getHostUser().getGroup_Hashlist()
                        .put(resObj.getString("group_id")
                                , resObj.getString("group_name") + "(" + resObj.getString("group_id") + ")");
                FuncUtil.saveObject(AddMemberActivity.this, DemoApp.getHostUser(),
                        DemoApp.getHostUser().getUsername());

                // 保存到历史记录中
                DemoApp.getmHisList().addHisItem(resObj.getString("group_id"),
                        resObj.getString("group_name") + "(" + resObj.getString("group_id") + ")");
                FuncUtil.saveObject(AddMemberActivity.this, DemoApp.getmHisList(), DemoApp.HIS_FILE_NAME);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            showTip("加入组成功");
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

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        if (null != mTextPwdSelectDialog) {
            mTextPwdSelectDialog.dismiss();
        }

        setResult(RESULT_OK);
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
        }
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
}
