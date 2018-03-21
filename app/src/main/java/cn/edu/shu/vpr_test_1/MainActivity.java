package cn.edu.shu.vpr_test_1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import java.util.ArrayList;
import cn.edu.shu.vpr_test_1.bean.KeyWord;
import cn.edu.shu.vpr_test_1.bean.VoiceBean;
import cn.edu.shu.vpr_test_1.speak.Speak;
import cn.edu.shu.vpr_test_1.ui.ManageActivity;
import cn.edu.shu.vpr_test_1.ui.VprActivity;

public class MainActivity extends AppCompatActivity {

    private StringBuffer mBuffer;
    private TextView tv_show;
    private TextView tv_place;
    private TextView tv_object;
    private TextView tv_action;


    private String place;
    private String object;
    private String action;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_show = (TextView) findViewById(R.id.tv_translate_text);
        tv_place = (TextView) findViewById(R.id.tv_place);
        tv_object = (TextView) findViewById(R.id.tv_object);
        tv_action = (TextView) findViewById(R.id.tv_action);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 完全退出
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_forward:
                Log.i("haha", "toolbar_search");
                Intent intent = new Intent(MainActivity.this,ManageActivity.class) ;
                startActivity(intent);
                return true;

        }
        return true;
    }

    public void startVoice(View view) {
        // 1.创建RecognizerDialog
        RecognizerDialog mDialog = new RecognizerDialog(MainActivity.this,null);

        // 2.设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT,"mandarin");

        mBuffer = new StringBuffer();
        // 3.设置回调接口
        mDialog.setListener(mRecognizerDialogListener);
        // 4.显示dialog,接收语音输入

        mDialog.show();
    }


    private Speak mSpeak;
    RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener(){
        // 听写结果回调接口（返回JSon格式结果）
        // 一般情况下通过onResults接口多次返回结果，完整的识别内容是多次结果的累加
        // isLast等于true时会话结束
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {

            String result = results.getResultString(); // 语音听写的结果

            String resultString = ProcessData(result); // 解析Json

            mBuffer.append(resultString); //让mbuffer不停地累加

            //话是否已经讲完了
            if(isLast){
                String finalResult = mBuffer.toString();
                tv_show.setText(finalResult);


                /**
                 * 关键词提取，用来做语义识别
                 */
                KeyWord keyword = new KeyWord();

                // 显示地点关键词
                String place = keyword.getPlacekeyWord(finalResult);
                tv_place.setText(place);
                // 显示操作对象关键词
                String object = keyword.getObjectKeyWord(finalResult);
                tv_object.setText(object);
                // 显示动作关键词
                String action = keyword.getActionKeyWord(finalResult);
                tv_action.setText(action);

                // 获取命令结果，并处理
                String resultBuffer = getResult(place,object,action);
                mSpeak = new Speak(MainActivity.this);
                mSpeak.startSpeak(resultBuffer);

            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };


    private String ProcessData(String result) {
        Gson gson = new Gson();
        VoiceBean voiceBean = gson.fromJson(result,VoiceBean.class);

        StringBuffer sb = new StringBuffer();

        ArrayList<VoiceBean.WsBean> ws = voiceBean.ws;
        for(VoiceBean.WsBean wsBean : ws){
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        return sb.toString();
    }

    public String getResult(final String pla,final String obj,final String act){
        String buffer;
        place = pla;object = obj;action = act;
        if(pla != null && obj != null && act != null){
            if(needPermission(pla,obj)) {

                buffer = "您需要通过声纹认证获取此指令权限";

                // 关联两个活动,获取另个活动返回的权限值
                Intent intent = new Intent(MainActivity.this,VprActivity.class);
                startActivityForResult(intent, 1);

            }else{
                buffer = "已经为您" + act +  pla + "的" + obj;
            }
        }else{
            buffer = "抱歉，我没听懂您的指令，请重试";
        }
        return buffer;
    }

    private boolean needPermission(String pla, String obj) {
        if(pla == "客厅" && obj == "门"){
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String mPermissionFlag="unpass";
        //Log.i("==============", "+++++++++++++++++++++++执行到这里了吗" + data);
        //得到验证结果
        switch (requestCode) {
            case 1:
                if (data == null) {
                    Log.e("data is null", "data is null");
                } else {
                    mPermissionFlag = data.getStringExtra("data_return");
                    Log.i("result======>>>", mPermissionFlag);
                }
                //Log.i("==============", "+++++++++++++++++++++++执行到这里了吗");
                //已经获取了mPermissionFlag（权限标志）
        if(mPermissionFlag.equals("pass")){
            mSpeak.startSpeak("您已通过认证，已经为您" + action + place + "的" + object);
            //Log.i("==============", "+++++++++++++++++++++++dulema");
        } else{
            mSpeak.startSpeak("抱歉，你尚未获取执行此操作的权限");
        }
        }
    }
}
