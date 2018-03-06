package com.gome.meidian.shiroredis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//@Component
//@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
	
	private int maxIdle; 
	private int maxTotal; 
	private long maxWaitMillis; 
	private boolean testOnBorrow; 

}
