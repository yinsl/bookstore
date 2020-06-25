/*
 * package com.maxus.tsp.common;
 * 
 * import java.util.List; import java.util.Map; import java.util.Set; import
 * java.util.concurrent.TimeUnit;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.data.redis.core.HashOperations; import
 * org.springframework.data.redis.core.ListOperations; import
 * org.springframework.data.redis.core.SetOperations; import
 * org.springframework.data.redis.core.StringRedisTemplate; import
 * org.springframework.data.redis.core.ZSetOperations; import
 * org.springframework.stereotype.Component;
 * 
 * @Component public class RedisApi {
 * 
 * @Autowired private StringRedisTemplate stringRedisTemplate;
 * 
 *//**
	 * 
	 * @Title: setValue
	 * @Description: 向redis存储一个键值对
	 * @param: @param key
	 * @param: @param a
	 * @return: void
	 * @throws @author fogmk
	 * @Date 2017年8月3日 下午1:44:31
	 */
/*
 * public void setValue(String key, String value) {
 * stringRedisTemplate.opsForValue().set(key, value); }
 * 
 *//**
	 * 
	 * @param key
	 * @param value
	 * @param time     过期时间
	 * @param timeUnit 时间单位
	 * @return
	 */
/*
 * public void setValue(String key, String value, int time, TimeUnit timeUnit) {
 * setValue(key, value); stringRedisTemplate.expire(key, time, timeUnit); }
 * 
 * public String getValue(String key) { return
 * stringRedisTemplate.opsForValue().get(key); }
 * 
 * public void setSet(String key, String value) {
 * stringRedisTemplate.opsForSet().add(key, value); }
 * 
 * public Object getSet(String key) { return
 * stringRedisTemplate.opsForSet().randomMember(key); }
 * 
 * public void setHash(String key, String field, String value) {
 * stringRedisTemplate.opsForHash().put(key, field, value); }
 * 
 * public String getHash(String key, String field) { HashOperations<String,
 * String, String> operations = stringRedisTemplate.opsForHash(); if
 * (operations.hasKey(key, field)) { return String.valueOf(operations.get(key,
 * field)); } else { return null; } }
 * 
 * public Map<String, String> getHashAll(String key) { HashOperations<String,
 * String, String> operations = stringRedisTemplate.opsForHash(); return
 * operations.entries(key); }
 * 
 * public boolean setHashAll(String key, Map<String, String> dataMap) { if
 * (dataMap != null) { HashOperations<String, String, String> operations =
 * stringRedisTemplate.opsForHash(); operations.putAll(key, dataMap); return
 * true; } else { return false; } }
 * 
 * public boolean removeHash(String key, String field) { HashOperations<String,
 * String, String> operations = stringRedisTemplate.opsForHash(); long
 * deleteSize = operations.delete(key, field); if (deleteSize > 0) { return
 * true; } else { return false; } }
 * 
 * public void removeKey(String key) { stringRedisTemplate.delete(key); }
 * 
 * public void removeSet(String key, String member) { SetOperations<String,
 * String> operations = stringRedisTemplate.opsForSet(); operations.remove(key,
 * member); }
 * 
 * public boolean hasKey(String key) { return stringRedisTemplate.hasKey(key); }
 * 
 * public boolean hasKeyInSet(String key, String member) { return
 * stringRedisTemplate.opsForSet().isMember(key, member); }
 * 
 * public boolean hasKey(String key, String field) { return
 * stringRedisTemplate.opsForHash().hasKey(key, field); }
 * 
 * public boolean delete(String key) { stringRedisTemplate.delete(key); return
 * true; }
 * 
 * public boolean expireForKey(String key, int time, TimeUnit timeUnit) { return
 * stringRedisTemplate.expire(key, time, timeUnit); }
 * 
 *//**
	 *
	 * @Title: perSistKey
	 * @Description: 为指定的key去除过期时间，即永久保存
	 * @param: @param  key
	 * @param: @return
	 * @return: boolean
	 * @throws @author 张涛
	 * @Date 2018年2月6日 下午2:49:22
	 */
/*
 * public boolean perSistKey(String key) { return
 * stringRedisTemplate.persist(key); }
 * 
 *//**
	 * 可以通过模糊查询获取所有的redis的String类型,可以加前缀
	 * 
	 * @param pattern
	 * @return
	 *//*
		 * public Set<String> getAllStringKeys(String pattern) { return
		 * stringRedisTemplate.keys(pattern); }
		 * 
		 * public void rightPush(String listKey, String newStr) { ListOperations<String,
		 * String> list = stringRedisTemplate.opsForList(); list.rightPush(listKey,
		 * newStr); }
		 * 
		 * public void rightPushAll(String listKey, List<String> newStr) {
		 * ListOperations<String, String> list = stringRedisTemplate.opsForList();
		 * list.rightPushAll(listKey, newStr); }
		 * 
		 * public List<String> getRangeList(String key, int start, int end) {
		 * ListOperations<String, String> list = stringRedisTemplate.opsForList();
		 * return list.range(key, start, end); }
		 * 
		 * public boolean zset(String key, String value, double score) {
		 * ZSetOperations<String, String> zs = stringRedisTemplate.opsForZSet(); return
		 * zs.add(key, value, score); }
		 * 
		 * }
		 */