package cn.edu.shu.vpr_test_1.bean;

/**
 * Created by win8 on 2017/5/3.
 */

public class KeyWord {
    private String str;

    public String getPlacekeyWord(String str){
        if(str.contains("客厅")){
            return "客厅";
        }
        if(str.contains("房间")||str.contains("卧室")){
            return "房间";
        }
        if(str.contains("卫生间")||str.contains("厕所")){
            return "卫生间";
        }
        if(str.contains("厨房")){
            return "厨房";
        }
        return null;
    }
    public String getObjectKeyWord(String str){
        if(str.contains("灯")){
            return "灯";
        }
        if(str.contains("风扇")){
            return "风扇";
        }
        if(str.contains("窗帘")){
            return "窗帘";
        }
        if(str.contains("空调")){
            return "空调";
        }
        if(str.contains("门")){
            return "门";
        }
        return null;
    }
    public String getActionKeyWord(String str){
        if(str.contains("打开")){
            return "打开";
        }
        if(str.contains("关掉")||str.contains("关闭")||str.contains("关上")){
            return "关闭";
        }
        return null;
    }


}
