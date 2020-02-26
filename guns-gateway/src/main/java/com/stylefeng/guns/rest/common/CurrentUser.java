package com.stylefeng.guns.rest.common;

public class CurrentUser {

    //线程绑定的存储空间
    private static final InheritableThreadLocal<String> THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void saveUserId(String userId){
        THREAD_LOCAL.set(userId);
    }

    public static String getCurrentUser(){
        return THREAD_LOCAL.get();
    }

//    //将用户信息放入存储空间
//    public static void saveUserInfo(UserInfoModel userInfoModel){
//        THREAD_LOCAL.set(userInfoModel);
//    }
//    //将用户信息取出
//    public static UserInfoModel getCurrentUser(){
//        return THREAD_LOCAL.get();
//    }

}
