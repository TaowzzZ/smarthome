package cn.edu.shu.vpr_test_1.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 实体类，保存用户信息
 * Created by win8 on 2017/4/25.
 */

public class User implements Serializable{
    private static final long serialVersionUID = 1L;
    // 用户名
    private String username = "default";
    // 是否已注册
    private boolean isEnrolled;
    // 是否已登录
    private boolean isLogined;
    // 声纹标准分
    private double voice_score;
    // 已加入的组列表id-info
    private HashMap<String, String> group_list = new HashMap<String, String>();
    // 已加入的组信息列表
    private ArrayList<String> group_full_list = new ArrayList<String>();
    public User() {

    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public void setEnrolled(boolean enrolled) {
        this.isEnrolled = enrolled;
    }

    public boolean isEnrolled() {
        return this.isEnrolled;
    }

    public boolean isLogined() {
        return this.isLogined;
    }

    public void setLogined(boolean logined) {
        this.isLogined = logined;
    }

    public double getVoiceScore() {
        return voice_score;
    }

    public void setVoiceScore(double voice_score) {
        this.voice_score = voice_score;
    }

    public void setGroup_list(HashMap<String, String> group_list) {
        this.group_list = group_list;
    }

    public HashMap<String, String> getGroup_Hashlist() {
        if(group_list == null){
            group_list = new HashMap<String, String>();
        }
        return group_list;
    }

    public ArrayList<String> getGroup_list() {
        if(null == group_list) {
            group_list = new HashMap<String, String>();
        }

        group_full_list = new ArrayList<String>();
        Iterator iterator = group_list.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String val = (String) entry.getValue();
            group_full_list.add(val);
        }
        return group_full_list;
    }
}
