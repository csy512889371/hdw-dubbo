package com.hdw.upms.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hdw.common.csrf.CsrfToken;
import com.hdw.upms.shiro.captcha.DreamCaptcha;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * @description 登录退出
 * @author TuMinglong
 * @date 2018年3月6日 上午9:55:46
 */

@Api(value = "登录接口类", tags = { "登录接口" })
@Controller
public class LoginController extends CommonController {
	@Autowired
	private DreamCaptcha dreamCaptcha;

	/**
	 * 图形验证码
	 */
	@ApiOperation(value = "图形验证码", notes = "图形验证码")
	@GetMapping("captcha.jpg")
	public void captcha(HttpServletRequest request, HttpServletResponse response) {
		dreamCaptcha.generate(request, response);
	}

	/**
	 * 首页
	 *
	 * @return
	 */
	@GetMapping("/")
	public String index() {
		return "redirect:/index";
	}

	/**
	 * 首页
	 *
	 * @param model
	 * @return
	 */
	@GetMapping("/index")
	public String index(Model model) {
		return "index";
	}

	/**
	 * GET 登录
	 * 
	 * @return {String}
	 */
	@ApiOperation(value = "GET 登录", notes = "GET 登录")

	@GetMapping("/login")
	@CsrfToken(create = true)
	public String login() {
		logger.info("GET请求登录");
		if (SecurityUtils.getSubject().isAuthenticated()) {
			return "redirect:/index";
		}
		return "login";
	}

	/**
	 * POST 登录 shiro 写法
	 *
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return {Object}
	 */
	@ApiOperation(value = "POST 登录 ", notes = "POST 登录 ")

	@PostMapping("/login")
	@CsrfToken(remove = true)
	@ResponseBody
	public Object loginPost(HttpServletRequest request, HttpServletResponse response, String username, String password,
			String captcha, @RequestParam(value = "rememberMe", defaultValue = "0") Integer rememberMe) {
		logger.info("POST请求登录");
		// 改为全部抛出异常，避免ajax csrf token被刷新
		if (StringUtils.isBlank(username)) {
			throw new RuntimeException("用户名不能为空");
		}
		if (StringUtils.isBlank(password)) {
			throw new RuntimeException("密码不能为空");
		}
		if (StringUtils.isBlank(captcha)) {
			throw new RuntimeException("验证码不能为空");
		}
		if (!dreamCaptcha.validate(request, response, captcha)) {
			throw new RuntimeException("验证码错误");
		}
		Subject user = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		// 设置记住密码
		token.setRememberMe(1 == rememberMe);
		try {
			user.login(token);
			return renderSuccess();
		} catch (UnknownAccountException e) {
			throw new RuntimeException("账号不存在！", e);
		} catch (DisabledAccountException e) {
			throw new RuntimeException("账号未启用！", e);
		} catch (IncorrectCredentialsException e) {
			throw new RuntimeException("密码错误！", e);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 未授权
	 * 
	 * @return {String}
	 */
	@ApiOperation(value = "未授权 ", notes = "未授权 ")

	@GetMapping("/unauth")
	public String unauth() {
		if (SecurityUtils.getSubject().isAuthenticated() == false) {
			return "redirect:/login";
		}
		return "unauth";
	}

	/**
	 * 退出
	 * 
	 * @return {Result}
	 */
	@ApiOperation(value = "退出 ", notes = "退出")

	@GetMapping("/logout")
	public String logout() {
		logger.info("登出");
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
		return "redirect:/index";
	}

}
