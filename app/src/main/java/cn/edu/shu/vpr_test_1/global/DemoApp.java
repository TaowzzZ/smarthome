package cn.edu.shu.vpr_test_1.global;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import cn.edu.shu.vpr_test_1.entity.GroupHisList;
import cn.edu.shu.vpr_test_1.entity.User;

/**
 * Created by win8 on 2017/4/25.
 */

public class DemoApp extends Application{
    private static User mUser;
    private static GroupHisList mHisList ;
    public static final String HIS_FILE_NAME ="HistoryFile";
    @Override
    public void onCreate() {

        super.onCreate();

        // 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用“,”分隔。
        // 设置你申请的应用appid


        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID+"=59074d2b");

    }

    public static User getHostUser() {
        if (null == mUser) {
            mUser = new User();
        }
        return mUser;
    }
    public static void setHostUser(User user) {
        mUser = user;
    }

    public static GroupHisList getmHisList() {
        if (null == mHisList) {
            mHisList = new GroupHisList();
        }
        return mHisList;
    }

    public static void setmHisList(GroupHisList mHisList) {
        DemoApp.mHisList = mHisList;
    }
}

