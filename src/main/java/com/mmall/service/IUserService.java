package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by yuchi on 3/31/19.
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid (String str, String type);
    ServerResponse<String> checkExist (String str, String type);
    ServerResponse<String> selectQuestion (String username);
    ServerResponse<String> checkAnswer(String username, String question, String answer);
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String token);
    ServerResponse<String> resetPassword (String passwordOld, String passwordNew, User user);
    ServerResponse<User> updateInformation(User user);
    ServerResponse<User> getInformation(Integer userId);
    boolean checkAdminRole(User user);
}
