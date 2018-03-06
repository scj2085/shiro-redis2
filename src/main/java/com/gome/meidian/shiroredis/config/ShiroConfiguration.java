package com.gome.meidian.shiroredis.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gome.meidian.shiroredis.shiro.CustomSessionListener;
import com.gome.meidian.shiroredis.shiro.CustomShiroCacheManager;
import com.gome.meidian.shiroredis.shiro.CustomShiroSessionDAO;
import com.gome.meidian.shiroredis.shiro.JedisManager;
import com.gome.meidian.shiroredis.shiro.JedisShiroCacheManager;
import com.gome.meidian.shiroredis.shiro.JedisShiroSessionRepository;
import com.gome.meidian.shiroredis.shiro.ShiroRealm;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * shiro 配置
 * @author shichangjian
 *
 */
@Configuration
public class ShiroConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ShiroConfiguration.class);

	/**
	 * 配置ShiroFilter
	 */
	@Bean
	public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {

		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		// 必须设置 SecurityManager
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		
		//拦截器
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<String,String>();
        filterChainDefinitionMap.put("/**", "anon");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}
	
	@Bean
	public ShiroRealm shiroRealm() {
		ShiroRealm shiroRealm = new ShiroRealm();
		shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
		return shiroRealm;
	}

	public HashedCredentialsMatcher hashedCredentialsMatcher(){
		HashedCredentialsMatcher hcm = new HashedCredentialsMatcher();
		hcm.setHashAlgorithmName(Sha256Hash.ALGORITHM_NAME);
		hcm.setHashIterations(1024);
		//hcm.setStoredCredentialsHexEncoded(true);
		return hcm;
	}
	
    
	/**
	 * 安全管理器
	 * @return
	 */
	@Bean
	public SecurityManager defaultWebSecurityManager(){
//		public SecurityManager defaultWebSecurityManager(CustomSessionListener customSessionListener){
		DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
		defaultWebSecurityManager.setRealm(shiroRealm());
		defaultWebSecurityManager.setSessionManager(defaultWebSessionManager());
		defaultWebSecurityManager.setRememberMeManager(rememberMeManager());
		defaultWebSecurityManager.setCacheManager(customShiroCacheManager());
		return defaultWebSecurityManager;
	}
	
	@Bean
	public CustomShiroCacheManager customShiroCacheManager(){
		CustomShiroCacheManager customShiroCacheManager = new CustomShiroCacheManager();
		customShiroCacheManager.setShiroCacheManager(jedisShiroCacheManager());
		return customShiroCacheManager;
	}
	
	@Bean
	public JedisShiroCacheManager jedisShiroCacheManager(){
		JedisShiroCacheManager jedisShiroCacheManager = new JedisShiroCacheManager();
		jedisShiroCacheManager.setJedisManager(jedisManager());
		return jedisShiroCacheManager;
	}
	@Bean
	public JedisManager jedisManager(){
		JedisManager jedisManager = new JedisManager();
		jedisManager.setJedisPool(jedisPool());
		return jedisManager;
	}
	
	@Bean
	public JedisPoolConfig jedisPoolConfig(){
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(100);   	//最大闲置
		jedisPoolConfig.setMinIdle(10);		//最小闲置
		jedisPoolConfig.setMaxTotal(500);	//最大连接数
		jedisPoolConfig.setMaxWaitMillis(5000);//最大等待
		jedisPoolConfig.setTestOnBorrow(true);	//可以获取
		return jedisPoolConfig;
	}
	
	@Bean
	public JedisPool jedisPool(){
		JedisPool jedisPool = new JedisPool(jedisPoolConfig(),"127.0.0.1",6379,0);
		return jedisPool;
	}
	
	/**
	 * 会话Session ID生成器,UUID 规则
	 * @return
	 */
	@Bean
	public JavaUuidSessionIdGenerator javaUuidSessionIdGenerator(){
		JavaUuidSessionIdGenerator javaUuidSessionIdGenerator = new JavaUuidSessionIdGenerator();
		return javaUuidSessionIdGenerator;
	}
	
    /**
     * cookie对象;
     * @return
     */
    public SimpleCookie rememberMeCookie(){
       //这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
       SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
       //<!-- 记住我cookie生效时间30天 ,单位秒;-->
       simpleCookie.setMaxAge(2592000);
       return simpleCookie;
    }

    /**
     * cookie管理对象;记住我功能
     * @return
     */
    public CookieRememberMeManager rememberMeManager(){
       CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
       cookieRememberMeManager.setCookie(rememberMeCookie());
       //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
       cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhmFLUs0KTA3Kprsdag=="));
       return cookieRememberMeManager;
    }
	
	@Bean
	public JedisShiroSessionRepository jedisShiroSessionRepository(){
		JedisShiroSessionRepository jedisShiroSessionRepository = new JedisShiroSessionRepository();
		jedisShiroSessionRepository.setJedisManager(jedisManager());
		return jedisShiroSessionRepository;
	}
	
	@Bean
	public CustomShiroSessionDAO customShiroSessionDAO(){
		CustomShiroSessionDAO customShiroSessionDAO = new CustomShiroSessionDAO();
		customShiroSessionDAO.setShiroSessionRepository(jedisShiroSessionRepository());
		customShiroSessionDAO.setSessionIdGenerator(javaUuidSessionIdGenerator());
		return customShiroSessionDAO;
	}
//	
//	/**
//	 * 会话验证调度器 
//	 * @return
//	 */
//	@Bean
//	public ExecutorServiceSessionValidationScheduler executorServiceSessionValidationScheduler(CustomSessionListener customSessionListener){
//		ExecutorServiceSessionValidationScheduler executorServiceSessionValidationScheduler = new ExecutorServiceSessionValidationScheduler();
//		executorServiceSessionValidationScheduler.setInterval(18000000);
////		executorServiceSessionValidationScheduler.setSessionManager(defaultWebSessionManager(customSessionListener));
//		return executorServiceSessionValidationScheduler;
//	}
//	
	@Bean
	public DefaultWebSessionManager defaultWebSessionManager(){
//		public DefaultWebSessionManager defaultWebSessionManager(CustomSessionListener customSessionListener){
		DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
		defaultWebSessionManager.setSessionValidationInterval(1800000);	//相隔多久检查一次session的有效性
		defaultWebSessionManager.setGlobalSessionTimeout(1800000);		//session 有效时间为半小时 （毫秒单位）
		defaultWebSessionManager.setSessionDAO(customShiroSessionDAO());
		
		List<SessionListener> customSessionListeners = new ArrayList<SessionListener>();
		customSessionListeners.add(customSessionListener());
		defaultWebSessionManager.setSessionListeners(customSessionListeners);
		
//		defaultWebSessionManager.setSessionValidationScheduler(executorServiceSessionValidationScheduler(customSessionListener));
//		defaultWebSessionManager.setSessionValidationSchedulerEnabled(true);
//		defaultWebSessionManager.setSessionIdCookie(rememberMeManager());
		return defaultWebSessionManager;
	}
	
	@Bean
	public CustomSessionListener customSessionListener(){
		CustomSessionListener customSessionListener = new CustomSessionListener();
		customSessionListener.setShiroSessionRepository(jedisShiroSessionRepository());
		return customSessionListener;
	}
//	
//
//	
//	/**
//	 * 相当于调用SecurityUtils.setSecurityManager(securityManager)
//	 * @return
//	 */
//	@Bean
//	public MethodInvokingFactoryBean methodInvokingFactoryBean(CustomSessionListener customSessionListener){
//		MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
//		methodInvokingFactoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
//		methodInvokingFactoryBean.setArguments(defaultWebSecurityManager(customSessionListener));
//		return methodInvokingFactoryBean;
//	}
	
}