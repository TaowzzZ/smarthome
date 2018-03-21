package cn.edu.shu.vpr_test_1.bean;

import java.util.List;

/**
 * Created by win8 on 2017/5/4.
 */

public class UserBean {
    /**
     * ssub : ipt
     * person : [{"user":"wangtao"},{"user":"wangguoping"}]
     * group_name : 123
     * sst : query
     * ret : 0
     * group_id : 2273305750
     */

    private String ssub;
    private String group_name;
    private String sst;
    private int ret;
    private String group_id;
    private List<PersonBean> person;

    public String getSsub() {
        return ssub;
    }

    public void setSsub(String ssub) {
        this.ssub = ssub;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getSst() {
        return sst;
    }

    public void setSst(String sst) {
        this.sst = sst;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public List<PersonBean> getPerson() {
        return person;
    }

    public void setPerson(List<PersonBean> person) {
        this.person = person;
    }

    public static class PersonBean {
        /**
         * user : wangtao
         */

        private String user;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
