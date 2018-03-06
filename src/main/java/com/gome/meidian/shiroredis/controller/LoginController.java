package com.gome.meidian.shiroredis.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gome.meidian.shiroredis.shiro.MeiproSession;
import com.gome.meidian.shiroredis.shiro.UserVo;
import com.gome.meidian.shiroredis.utils.Constants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RestController
public class LoginController {
	
   	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


//	@RequestMapping(value = "/login", method = RequestMethod.POST)
//	public String login(@RequestParam String username, @RequestParam String password,
//			@RequestParam boolean remember) {
//		if (username == null || password == null) {
//			logger.error("/login error username or password  null");
//			return "username is null";
//
//		}
//
//		try {
//			login(username, password, null, remember);
//		} catch (AuthenticationException e) {
//			if (e instanceof UnknownAccountException) {
//				// 账户不存在
//				logger.error("login error", e);
//			} else if (e instanceof IncorrectCredentialsException) {
//				// 密码不正确
//				logger.error("login error", e);
//			}
//		}
//
//		return "ok";
//	}
	
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(@RequestParam String username, @RequestParam String password,
			@RequestParam boolean remember) {
		if (username == null || password == null) {
			logger.error("/login error username or password  null");
			return "username is null";

		}
		MeiproSession session = new MeiproSession();
		try {
			session.login(username, password, null, remember);
			session.getSession().setTimeout(Constants.SESSION_EXPIRE_TIME);
		} catch (AuthenticationException e) {
			if (e instanceof UnknownAccountException) {
				// 账户不存在
				logger.error("login error", e);
				return "账户不存在";
			} else if (e instanceof IncorrectCredentialsException) {
				// 密码不正确
				logger.error("login error", e);
				return "密码不正确";
			}
			return "error";
		}
		logger.warn("userId:{} login", session.getUserInfo().getId());

		return "ok";
	}
	
	
	@RequestMapping(value = "/test", method=RequestMethod.GET)
	public String test(){
		Subject subject = SecurityUtils.getSubject();
		Session session = subject.getSession();
		Object object = subject.getPrincipal();
		UserVo userVo = (UserVo)object;
		return userVo.getAccountName();
	}
    public void login(String username, String password, String host, boolean remberMe) {
        UsernamePasswordToken upToken = new UsernamePasswordToken();
        upToken.setUsername(username);
        upToken.setPassword(password.toCharArray());
        upToken.setRememberMe(remberMe);
        upToken.setHost(host);
        Subject subject = SecurityUtils.getSubject();
        subject.login(upToken);
    }
}
