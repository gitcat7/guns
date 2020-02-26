package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.api.user.vo.UserInfoModel;
import com.stylefeng.guns.api.user.vo.UserModel;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Reference(interfaceClass = UserAPI.class, check = false)
    private UserAPI userAPI;

    @PostMapping("registry")
    public ResponseVO registry(UserModel userModel){
        if(null == userModel.getUsername() || userModel.getUsername().trim().length() == 0){
            return ResponseVO.serviceFail("用户名不能为空");
        }
        if(null == userModel.getPassword() || userModel.getPassword().trim().length() == 0){
            return ResponseVO.serviceFail("密码不能为空");
        }
        boolean isSuccess = userAPI.register(userModel);
        if(isSuccess){
            return ResponseVO.success("注册成功");
        }
        return ResponseVO.serviceFail("注册失败");
    }

    @GetMapping("check")
    public ResponseVO check(String username){
        if(null != username && username.trim().length() > 0){
            boolean notExists = userAPI.checkUsername(username);
            if(notExists){
                return ResponseVO.success("用户名不存在");
            }
            return ResponseVO.serviceFail("用户名存在");
        }
        return ResponseVO.serviceFail("用户名不能为空");
    }

    @GetMapping("logout")
    public ResponseVO logout(){
        /*
        应用:
            1.前端存储JWT[7天]:JWT的刷新
            2.服务器端会存储活动用户信息[30分钟]
            3.JWT里的userId为key,查找活跃用户
        退出:
            1.前端删除调JWt
            2.后端服务器删除活跃用户缓存
        现状:
            1.前端删除掉JWT
         */
        return ResponseVO.success("用户退出成功");
    }

    @GetMapping("getUserInfo")
    public ResponseVO getUserInfo(){
        //获取当前登录用户
        String userId = CurrentUser.getCurrentUser();
        if(null != userId && userId.trim().length() > 0){
            //将用户传入后端进行查询
            int uuid = Integer.parseInt(userId);
            UserInfoModel userInfo = userAPI.getUserInfo(uuid);
            if(null != userInfo){
                return ResponseVO.success(userInfo);
            }
            return ResponseVO.serviceFail("用户信息查询失败");
        }
        return ResponseVO.success("用户未登录");
    }

    @PostMapping("updateUserInfo")
    public ResponseVO updateUserInfo(UserInfoModel userInfoModel){
        //获取当前登录用户
        String userId = CurrentUser.getCurrentUser();
        if(null != userId && userId.trim().length() > 0){
            //将用户传入后端进行查询
            int uuid = Integer.parseInt(userId);
            //判断当前登陆人员的ID与修改的结果ID是否一致
            if(uuid != userInfoModel.getUuid()){
                return ResponseVO.serviceFail("请修改您个人的信息");
            }
            UserInfoModel userInfo = userAPI.updateUserInfo(userInfoModel);
            if(null != userInfo){
                return ResponseVO.success(userInfo);
            }
            return ResponseVO.serviceFail("用户信息修改失败");
        }
        return ResponseVO.success("用户未登录");
    }
}
