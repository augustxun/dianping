package com.dp.utils;


import com.dp.model.vo.UserVO;

public class UserHolder {
    private static final ThreadLocal<UserVO> tl = new ThreadLocal<>();

    public static void setUser(UserVO user){
        tl.set(user);
    }

    public static UserVO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
