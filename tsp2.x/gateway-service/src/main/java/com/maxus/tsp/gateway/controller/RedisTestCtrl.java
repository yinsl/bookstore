package com.maxus.tsp.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/redis")
public class RedisTestCtrl {

	@Autowired
	@Qualifier("redisKeyDatabase")
	private StringRedisTemplate redisTemplate;
	@Autowired
	@Qualifier("itredisStringDatabase")
	private StringRedisTemplate itredisTemplate;
	@GetMapping(value = "/get/{key}")
	public String get(@PathVariable String key) {
		return redisTemplate.opsForValue().get(key);
	}
	@GetMapping(value = "/set/{key}/{value}")
	public int set(@PathVariable String key,@PathVariable String value) {
		redisTemplate.opsForValue().set(key, value);
		return 1;
	}
	@GetMapping(value = "/getit/{key}")
	public String getit(@PathVariable String key) {
		return itredisTemplate.opsForValue().get(key);
	}
	@GetMapping(value = "/setit/{key}/{value}")
	public int setit(@PathVariable String key,@PathVariable String value) {
		itredisTemplate.opsForValue().set(key, value);
		return 1;
	}
}
