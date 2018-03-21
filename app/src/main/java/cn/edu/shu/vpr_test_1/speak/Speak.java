package cn.edu.shu.vpr_test_1.speak;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by win8 on 2017/5/6.
 */

public class Speak {
    public static final String TAG = "MainActivity";
    private SpeechSynthesizer mTts;
    static Context mContext;

    public Speak(Context context){
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        //初始化语音对象
        mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener);
        //参数初始化
        setParam();
    }



    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {


        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.i(TAG, "初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);

        // 设置在线合成发音人
        /*合成发音人列表
        1、 语言为中英文的发音人可以支持中英文的混合朗读。
        2、 英文发音人只能朗读英文，中文无法朗读。
        3、 汉语发音人只能朗读中文，遇到英文会以单个字母的方式进行朗读。
        4、 使用新引擎参数会获得更好的合成效果。

        发音人名称 属性 语言 参数名称 新引擎参数 备注
        小燕 青年女声 中英文（普通话） xiaoyan 默认
        小宇 青年男声 中英文（普通话） xiaoyu
        凯瑟琳 青年女声 英文 catherine
        亨利 青年男声 英文 henry
        玛丽 青年女声 英文 vimary
        小研 青年女声 中英文（普通话） vixy
        小琪 青年女声 中英文（普通话） vixq xiaoqi
        小峰 青年男声 中英文（普通话） vixf
        小梅 青年女声 中英文（粤语） vixm xiaomei
        小莉 青年女声 中英文（台湾普通话） vixl xiaolin
        小蓉 青年女声 汉语（四川话） vixr xiaorong
        小芸 青年女声 汉语（东北话） vixyun xiaoqian
        小坤 青年男声 汉语（河南话） vixk xiaokun
        小强 青年男声 汉语（湖南话） vixqa xiaoqiang
        小莹 青年女声 汉语（陕西话） vixying
        小新 童年男声 汉语（普通话） vixx xiaoxin
        楠楠 童年女声 汉语（普通话） vinn nannan
        老孙 老年男声 汉语（普通话） vils
        Mariane 法语 Mariane
        Allabent 俄语 Allabent
        Gabriela 西班牙语 Gabriela
        Abha 印地语 Abha
        XiaoYun 越南语 XiaoYun*/
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");
        //设置合成语速    在线0-100  本地0-200
        mTts.setParameter(SpeechConstant.SPEED, "60");
        //设置合成音调   0-100
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量    0-100
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        //设置云端
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //设置播放器音频流类型
        //mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    // 开始合成
    // 收到onCompleted 回调时，合成结束、生成合成音频
    // 合成的音频格式：只支持pcm格式
    public void startSpeak(final String text) {

        if (text != null) {
            int code = mTts.startSpeaking(text, mTtsListener);
            if (code != ErrorCode.SUCCESS) {
                if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                    //未安装则跳转到提示安装页面
                } else {
                    showToast(mContext, "语音合成失败,错误码: " + code);
                }
            }
        }


    }

    // 取消合成
    public void stopSpeak() {
        mTts.pauseSpeaking();
    }

    // 暂停播放
    public void pauseSpeak() {
        mTts.pauseSpeaking();
    }

    //继续播放
    public void resumeSpeak() {
        mTts.resumeSpeaking();
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            Log.i(TAG, "暂停播放");
        }

        //恢复播放回调接口
        @Override
        public void onSpeakResumed() {
            Log.i(TAG, "继续播放");
        }

        //缓冲进度回调
        //percent为缓冲进度0~100， beginPos为缓冲音频在文本中开始位置， endPos表示缓冲音频在文本中结束位置， info为附加信息。
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            //showToast(mContext, "缓冲进度为" + percent );
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            //showToast(mContext, "播放进度为" + percent );
        }

        //会话结束回调接口，没有错误时， error为null
        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.i(TAG, "播放完成");
            } else if (error != null) {
                Log.i(TAG, "合成错误:" + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //  if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //      String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //      Log.d(TAG, "session id =" + sid);
            //  }
        }
    };


    /**
     *   吐司工具
     */
    private static Toast toast;

    public static void showToast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);//如果不为空，则直接改变当前toast的文本
        }
        toast.show();
    }
}
