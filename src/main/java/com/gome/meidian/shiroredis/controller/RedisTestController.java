package com.gome.meidian.shiroredis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

@RestController
public class RedisTestController {

//	@Autowired
//	private JedisCluster jedisCluster;
	
	public static void main(String[] args) {  
//	    Jedis jedis = new Jedis("127.0.0.1", 6379, 1000);  
//	    jedis.set("redis", "redis value");  
//	    String value = jedis.get("redis");  
//	    System.err.println("redis=" + value); 
	    
	    Jedis jedis = null;
	    JedisPool jedisPool = null;
	    try {
	    	jedisPool = new JedisPool(new JedisPoolConfig(),"127.0.0.1",6379,0);
	        jedis = jedisPool.getResource();
	        jedis.set("key", "value");
	    }catch (JedisException e) {
	        if(jedis != null){
	        	jedisPool.returnBrokenResource(jedis);
	        }
	        throw e;
	    }finally{
	        if(jedis != null){
	        	jedisPool.returnResource(jedis);
	        }
	    }
	}
	
	
}
