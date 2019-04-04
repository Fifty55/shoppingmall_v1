package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Created by yuchi on 3/31/19.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0) {
            return ServerResponse.createByErrorMessage("No username");
        }
        //TODO: MD5 password encoder
        String encodePassword = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,encodePassword);
        if(user == null) {
            return ServerResponse.createByErrorMessage("Password incorrect");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Login successfully",user);

    }

    public ServerResponse<String> register(User user) {
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /*
    Check the user exit or not
    if exit return false;
     */
    public ServerResponse<String> checkValid (String str, String type){

        if(StringUtils.isNoneBlank(type)) {
            if(Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0) {
                    return ServerResponse.createByErrorMessage("Username has been used");
                }
            }
            if(Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0) {
                    return ServerResponse.createByErrorMessage("Email has been used");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("Param error");
        }
        return ServerResponse.createBySuccessMessage("The str can be used");
    }

    public ServerResponse<String> checkExist (String str, String type){
        // TODO: BUILD THE EXIST
        if(StringUtils.isNoneBlank(type)) {
            if(Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if(resultCount == 0) {
                    return ServerResponse.createByErrorMessage("No username");
                }
            }
            if(Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if(resultCount == 0) {
                    return ServerResponse.createByErrorMessage("No email name");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("Param error");
        }
        return ServerResponse.createBySuccessMessage("Find user");
    }

    public ServerResponse<String> selectQuestion (String username){
        ServerResponse<String> validResponse = this.checkExist(username,Const.USERNAME);
        if(!validResponse.isSuccess()) {
            return ServerResponse.createBySuccessMessage("No user");
        }
        String question = userMapper.selectQuestionByUserName(username);
        if(StringUtils.isNoneBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("No security questions");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int findAnswer = userMapper.checkAnswer(username,question,answer);
        if(findAnswer > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("Answer is not right");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String token) {
        if(StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("Need token");
        }
        ServerResponse<String> validResponse = this.checkExist(username,Const.USERNAME);
        if(!validResponse.isSuccess()) {
            return ServerResponse.createBySuccessMessage("No user");
        }

        String cacheToken = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("Token expire or non-Vaild");
        }
        if(StringUtils.equals(cacheToken,token)) {
            String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5PasswordNew);
            if(rowCount == 1) {
                return ServerResponse.createBySuccessMessage("password change");
            }else {
                System.out.print("wrong: " + rowCount);
            }
        }else {
            return ServerResponse.createByErrorMessage("Token is wrong and get new token");
        }
        return ServerResponse.createByErrorMessage("Fail to change");
    }

    public ServerResponse<String> resetPassword (String passwordOld, String passwordNew, User user) {
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0) {
            return ServerResponse.createByErrorMessage("Password is not right. Cannot change to new password");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount == 1) {
            return ServerResponse.createBySuccessMessage("Password update successfully");
        }
        return ServerResponse.createByErrorMessage("Same password or Fail to update");
    }

    public ServerResponse<User> updateInformation(User user) {
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("update user profile successfully",updateUser);
        }
        return ServerResponse.createByErrorMessage("Fail to update user profile");
    }

    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null) {
            return ServerResponse.createByErrorMessage("Cannot find current User");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    public boolean checkAdminRole(User user) {
        if(user != null || user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return true;
        }
        return false;
    }


}
