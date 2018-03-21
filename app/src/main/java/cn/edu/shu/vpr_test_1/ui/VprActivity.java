package cn.edu.shu.vpr_test_1.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
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
import cn.edu.shu.vpr_test_1.util.FuncUtil;

/**
 * Created by win8 on 2017/5/6.
 */

public class VprActivity extends Activity implements View.OnClickListener{
    private static final String TAG = VprActivity.class.getSimpleName();

    // 密码类型
    // 默认为数字密码
    private int mPwdType = 3;
    // 用于鉴别的数字密码
    private String mIdentifyNumPwd = "";

    // 用户输入的组ID
    private final String mGroupId = "2271479337";
    // 身份鉴别对象
    private IdentityVerifier mIdVerifier;

    // UI控件
    private TextView mResultTextView;
    private AlertDialog mTextPwdSelectDialog;
    private Toast mToast;
    private Toolbar mToolbar;
    JSONArray candidates;

    // 是否已经开始业务
    private boolean mIsWorking = false;
    // 是否可以鉴别
    private boolean mCanIdentify = false;
    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;
    // 进度对话框
    private ProgressDialog mProDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vprecognition);

        mIdVerifier = IdentityVerifier.createVerifier(VprActivity.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });

        initUI();


        mToolbar = (Toolbar) findViewById(R.id.toolBar_vpr);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 声纹鉴别监听器
     */
    private IdentityListener mSearchListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            dismissProDialog();
            mIsWorking = false;

            handleResult(result);
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
            mCanIdentify = false;
            dismissProDialog();
            mIsWorking = false;
            showTip(error.getPlainDescription(true));
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
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(!mIsWorking) {
                        vocalSearch();

                        try {
                            mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
                            mPcmRecorder.startRecording(mPcmRecordListener);
                        } catch (SpeechError e) {
                            e.printStackTrace();
                        }

                        mIsWorking = true;
                        mCanIdentify = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    v.performClick();
                    if(mCanIdentify){
                        showProDialog("鉴别中...");
                    }
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
            // 子业务执行参数，若无可以传空字符传
            params.append("ptxt=" + mIdentifyNumPwd + ",");
            params.append("pwdt=" + mPwdType + ",");
            params.append(",group_id=" + mGroupId +",topc=3");
            mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
        }

        @Override
        public void onError(SpeechError e) {
            dismissProDialog();
            mCanIdentify = false;
        }
    };


    private void handleResult(IdentityResult result) {
        if (null == result) {
            return;
        }
        try {
            String resultStr = result.getResultString();
            JSONObject resultJson = new JSONObject(resultStr);
            if(ErrorCode.SUCCESS == resultJson.getInt("ret"))
            {
                // 保存到历史记录中
                DemoApp.getmHisList().addHisItem(resultJson.getString("group_id"),
                        resultJson.getString("group_name") + "(" + resultJson.getString("group_id") + ")");
                FuncUtil.saveObject(VprActivity.this, DemoApp.getmHisList(), DemoApp.HIS_FILE_NAME);

                try {
                    JSONObject obj = new JSONObject(resultStr);
                    // 组名称

                    JSONObject ifv_result = obj.getJSONObject("ifv_result");
                    candidates = ifv_result.getJSONArray("candidates");

                    // 获取其中排名第一的对象
                    JSONObject first = candidates.getJSONObject(0);
                    // 获取用户名
                    String firstName = first.optString("user");
                    // 获取用户分数
                    Double firstScore = first.optDouble("score");
                    Log.i("==============","+++++++++++++++++++++++执行到这里了吗"+firstScore);
                    if(firstScore >85){
                        showTip(firstName+"，您好！您已通过验证");
                        //Log.i("==============","+++++++++++++++++++++++执行到这里了吗333333");
                        // 权限标志用Intent返回
                        Intent intent = new Intent();
                        // 把返回数据存入Intent
                        intent.putExtra("data_return","pass");
                        // 设置返回数据
                        setResult(RESULT_OK,intent);
                        // 关闭此活动
                        finish();
                    } else{
                        showTip("抱歉，您没有通过验证！");
                        //Log.i("==============","+++++++++++++++++++++++执行到这里了吗2222");
                        // 权限标志用Intent返回
                        Intent intent = new Intent();
                        // 把返回数据存入Intent
                        intent.putExtra("data_return","unpass");
                        // 设置返回数据
                        setResult(RESULT_OK,intent);
                        // 关闭此活动
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                showTip("鉴别失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {

        mResultTextView = (TextView) findViewById(R.id.tv_dialog);

        // 按住说话键
        (findViewById(R.id.bt_press_talk)).setOnTouchListener(mPressTouchListener);

        mProDialog = new ProgressDialog(VprActivity.this);
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

        mToast = Toast.makeText(VprActivity.this, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);

        mIdentifyNumPwd = VerifierUtil.generateNumberPassword(8);
        StringBuilder strBufSearch = new StringBuilder();
        strBufSearch.append("您的鉴别密码：" + mIdentifyNumPwd +"\n");
        strBufSearch.append("请长按“按住说话”按钮进行鉴别！\n");
        mResultTextView.setText(strBufSearch.toString());
    }

    private void vocalSearch() {
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
        // 设置组ID
        mIdVerifier.setParameter("group_id", mGroupId);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mSearchListener);
    }

    @Override
    public void finish() {
        super.finish();
        if (null != mTextPwdSelectDialog) {
            mTextPwdSelectDialog.dismiss();
        }
        setResult(RESULT_OK);
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

    private void dismissProDialog() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
    }

    private void showProDialog(String msg) {
        if (mProDialog != null) {
            mProDialog.setMessage(msg);
            mProDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
    }

}
