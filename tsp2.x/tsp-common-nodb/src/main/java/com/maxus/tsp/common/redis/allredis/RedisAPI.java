/**        
 * SIRedisAPI.java Create on 2017年8月3日
 * Copyright (c) 2017年8月3日 by 上汽集团商用车技术中心      
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>      
 * @version 1.0  
 */
package com.maxus.tsp.common.redis.allredis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.maxus.tsp.platform.service.model.vo.GBLoginNo;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;

@Component
public class RedisAPI {

	@Autowired
	@Qualifier("redisKeyDatabase")
	private StringRedisTemplate redisTemplate;
	
	@Autowired
	@Qualifier("itredisDatabase")
	private RedisTemplate<String, ItRedisInfo> itRedisTemplate;

	@Autowired
	@Qualifier("itredisStringDatabase")
	private RedisTemplate<String, String> itRedisStringTemplate;
	
	@Autowired
	@Qualifier("gbredisDatabase")
	private RedisTemplate<String, GBLoginNo> gbRedisTemplate;
	
	// redis是否可用
	private boolean valid = true;

	/**
	 * 
	 * @Title: isValid
	 * @Description: 确认redis是否可用了，调用其它方法出现异常，则代表redis不可用了
	 * @param: @return
	 * @return: boolean the valid
	 * @throws @author
	 *             fogmk
	 * @Date 2018年2月23日 下午3:08:42
	 */
	// @Scheduled(fixedDelay = 3000)
	public void validateRedis() {
		try {
			// 原来不可用，看看是否可以变成可用了
			if (!valid) {
				redisTemplate.opsForValue().set("REDIS_STATUS_TEST", "REDIS_STATUS_TEST", 5, TimeUnit.SECONDS);
				valid = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @Title: setValue
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @param
	 *             a
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月3日 下午1:44:31
	 */
	public Long incrementExpire(String key, long value, long expireMicroseconnds) {
		long res = redisTemplate.opsForValue().increment(key, value);
		redisTemplate.expire(key, expireMicroseconnds, TimeUnit.MICROSECONDS);
		return res;
	}
	
	/**
	 * 
	 * @Title: setValue
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @param
	 *             a
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月3日 下午1:44:31
	 */
	public Long increment(String key, long value) {
		return redisTemplate.opsForValue().increment(key, value);
	}

	/**
	 * 
	 * @Title: isValid
	 * @Description: 当前redis是否有效
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             fogmk
	 * @Date 2018年2月27日 下午1:58:16
	 */
	// public boolean isValid() {
	// 	//valid; 蜘蛛智行暂时不用
	// 	return true;
	// }

	/**
	 * 
	 * @Title: setValue
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @param
	 *             a
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月3日 下午1:44:31
	 */
	public boolean setValue(String key, String value) {
		try {
			ValueOperations<String, String> operations = redisTemplate.opsForValue();
			operations.set(key, value);

			boolean exists = redisTemplate.hasKey(key);
//			if (exists) {
//				System.out.println("exists is true");
//			} else {
//				System.out.println("exists is false");
//			}
			return exists;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @param time
	 *            过期时间
	 * @param timeUnit
	 *            时间单位
	 * @return
	 */
	public boolean setValue(String key, String value, int time, TimeUnit timeUnit) {
		try {
			//boolean b = setValue(key, value);
			//redisTemplate.expire(key, time, timeUnit);
			//使用redis提供的原子操作，设置value的同时也设置了过期时间，
			//1、减少redis操作次数
			//2、防止redis故障，导致设置value成功，但是过期时间没有设置成功
			redisTemplate.opsForValue().set(key, value, time, timeUnit);
			return true;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}
	/**
	 * @method  setValueWithEspireTime
	 * @description  存储String类型数据，并设置过期时间
	 * @param key
	 * @param value
	 * @param time
	 *            过期时间
	 * @param timeUnit
	 *            时间单位
	 * @return      
	 * @author      zhuna
	 * @date        2019/2/15 14:34
	 */
	public boolean setValueWithEspireTime(String key, String value, int time, TimeUnit timeUnit){
		boolean result = true;
		try {
			//redisTemplate.set(key, value, "NX", "EX", time);
			redisTemplate.opsForValue().set(key, value, time, timeUnit);
		}catch (Exception ex){
			result = false;
			throw ex;
		}
		return result;
	}
	/**
	 *
	 * @Title: expireForKey
	 * @Description: 为指定的key设置过期时间
	 * @param: @param
	 *             key
	 * @param: @param
	 *             time
	 * @param: @param
	 *             timeUnit
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             张涛
	 * @Date 2018年2月6日 下午2:47:54
	 */
	public boolean expireForKey(String key, int time, TimeUnit timeUnit) {
		boolean result = false;
		try {
			result = redisTemplate.expire(key, time, timeUnit);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
		return result;
	}

	/**
	 *
	 * @Title: perSistKey
	 * @Description: 为指定的key去除过期时间，即永久保存
	 * @param: @param
	 *             key
	 * @param: @return
	 * @return: boolean
	 * @throws @author
	 *             张涛
	 * @Date 2018年2月6日 下午2:49:22
	 */
	public boolean perSistKey(String key) {
		boolean result = false;
		try {
			result = redisTemplate.persist(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
		return result;
	}

	/**
	 *
	 * @Title: getSet
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @param
	 *             a
	 * @return: void
	 * @throws @author
	 *             赵伟阳
	 * @Date 2017年8月15日 下午1:39:30
	 */
	public String getValue(String key) {

		try {
			ValueOperations<String, String> operations = redisTemplate.opsForValue();
			return operations.get(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}
	
	public ItRedisInfo getItValue(String key) {
		try {
			ValueOperations<String, ItRedisInfo> operations = itRedisTemplate.opsForValue();
			return operations.get(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}
	
	public GBLoginNo getGbValue(String key) {
		try {
			ValueOperations<String, GBLoginNo> operations = gbRedisTemplate.opsForValue();
			return operations.get(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	public void setItValue(String key, ItRedisInfo itRedisInfo) {
		try {
			ValueOperations<String, ItRedisInfo> operations = itRedisTemplate.opsForValue();
			operations.set(key, itRedisInfo);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}
	
	public void setGbValue(String key, GBLoginNo gBLoginNo) {
		try {
			ValueOperations<String, GBLoginNo> operations = gbRedisTemplate.opsForValue();
			operations.set(key, gBLoginNo);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}
	


	/**
	 * 
	 * @Title: setSet
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @param
	 *             a
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月3日 下午1:44:31
	 */
	public boolean setSet(String key, String value) {
		try {
			SetOperations<String, String> operations = redisTemplate.opsForSet();
			operations.add(key, value);

			boolean exists = redisTemplate.hasKey(key);
//			if (exists) {
//				System.out.println("exists is true");
//			} else {
//				System.out.println("exists is false");
//			}
			return exists;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 *
	 * @Title: getSet
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @param
	 *             a
	 * @return: void
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月3日 下午1:44:31
	 */
	public Object getSet(String key) {
		try {
			SetOperations<String, String> operations = redisTemplate.opsForSet();

			return operations.randomMember(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: setHash
	 * @Description: 设置hash数据
	 * @param: @param
	 *             key
	 * @param: @param
	 *             field
	 * @param: @param
	 *             value
	 * @return: boolean
	 * @throws @author
	 *             余佶
	 * @Date 2017年8月11日 下午1:44:31
	 */
	public boolean setHash(String key, String field, String value) {
		try {
			HashOperations<String, String, String> operations = redisTemplate.opsForHash();
			operations.put(key, field, value);

			boolean exists = operations.hasKey(key, field);
//			if (exists) {
//				System.out.println("exists is true");
//			} else {
//				System.out.println("exists is false");
//			}
			return exists;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: getHash
	 * @Description: 从hash数据格式中获取值
	 * @param: @param
	 *             key
	 * @param: @param
	 *             field
	 * @return: Object
	 * @throws @author
	 *             余佶
	 * @Date 2017年8月11日 下午1:44:31
	 */
	public String getHash(String key, String field) {
		try {
			HashOperations<String, String, String> operations = redisTemplate.opsForHash();
			if (operations.hasKey(key, field))
				return String.valueOf(operations.get(key, field));
			else
				return null;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: getHashAll
	 * @Description: 从hash数据格式中获取所有值
	 * @param: @param
	 *             key
	 * @return: Object
	 * @throws @author
	 *             余佶
	 * @Date 2017年8月11日 下午1:44:31
	 */
	public Map<String, String> getHashAll(String key) {
		try {
			HashOperations<String, String, String> operations = redisTemplate.opsForHash();
			return operations.entries(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: setHashAll
	 * @Description: 批量插入hash数据
	 * @param: @param
	 *             key
	 * @param: @param
	 *             Map
	 * @return: Object
	 * @throws @author
	 *             余佶
	 * @Date 2017年8月11日 下午1:44:31
	 */
	public boolean setHashAll(String key, Map<String, String> dataMap) {
		try {
			if (dataMap != null) {
				HashOperations<String, String, String> operations = redisTemplate.opsForHash();
				operations.putAll(key, dataMap);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: removeHash
	 * @Description: 从hash数据格式中获取值
	 * @param: @param
	 *             key
	 * @param: @param
	 *             field
	 * @return: Object
	 * @throws @author
	 *             余佶
	 * @Date 2017年8月11日 下午1:44:31
	 */
	public boolean removeHash(String key, String field) {
		try {
			HashOperations<String, String, String> operations = redisTemplate.opsForHash();
			long deleteSize = operations.delete(key, field);
			if (deleteSize > 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	public boolean removeKey(String key) {
		try {
			// HashOperations<String, String,String> operations =
			// redisTemplate.opsForHash();
			redisTemplate.delete(key);
			return true;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: removeHash
	 * @Description: 从hash数据格式中获取值
	 * @param: @param
	 *             key
	 * @param: @param
	 *             field
	 * @return: Object
	 * @throws @author
	 *             余佶
	 * @Date 2017年8月11日 下午1:44:31
	 */
	public boolean removeSet(String key, String member) {
		try {
			SetOperations<String, String> operations = redisTemplate.opsForSet();
			long deleteSize = operations.remove(key, member);
			if (deleteSize > 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: hasKey
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @return
	 * @return: Object
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月3日 下午1:54:18
	 */
	public boolean hasKey(String key) {
		try {
			return redisTemplate.hasKey(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	public boolean ithasKey(String key) {
		try {
			return itRedisTemplate.hasKey(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: hasKey
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @return
	 * @return: Object
	 * @throws @author
	 *             fogmk
	 * @Date 2017年8月3日 下午1:54:18
	 */
	public boolean hasKeyInSet(String key, String member) {
		try {
			return redisTemplate.opsForSet().isMember(key, member);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 
	 * @Title: hasKey
	 * @Description:  
	 * @param: @param
	 *             key
	 * @param: @return
	 * @return: Object
	 * @throws @author
	 *             余佶
	 * @Date 2017年8月3日 下午1:54:18
	 */
	public boolean hasKey(String key, String field) {
			return redisTemplate.opsForHash().hasKey(key, field);
	}

	/**
	 * 删除key并且判断是否成功
	 * 
	 * @param key
	 * @return
	 */
	public boolean delete(String key) {

		try {
			redisTemplate.delete(key);

			boolean b = hasKey(key);

			if (b)

				return false;

			return true;
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * 可以通过模糊查询获取所有的redis的String类型,可以加前缀
	 * 
	 * @param pattern
	 * @return
	 */
	public Set<String> getAllStringKeys(String pattern) {
		Set<String> keys = null;
		try {
			keys = redisTemplate.keys(pattern);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
		return keys;
	}

    public boolean rightPush(String listKey, String newStr) {
        long result = 0;
        try {
            ListOperations<String, String> list = redisTemplate.opsForList();
            result = list.rightPush(listKey, newStr);
        } catch (Exception e) {
            valid = false;
            throw e;
        }
        return result > 0;
    }
    public boolean rightPushAll(String listKey, List<String> newStr) {
        long result = 0;
        try {
            ListOperations<String, String> list = redisTemplate.opsForList();
            list.rightPushAll(listKey, newStr);
        } catch (Exception e) {
            valid = false;
            throw e;
        }
        return result > 0;
    }

    public List<String> getRangeList(String key, int start, int end) {
        List<String> dataList = null;
        try {
            ListOperations<String, String> list = redisTemplate.opsForList();
            //Long count = list.size(key);
            dataList = list.range(key, start, end);
        } catch (Exception e) {
            valid = false;
            throw e;
        }
        return dataList;
    }

    public boolean zset(String key,String value, double score) {
        boolean result = false;
        try {
            ZSetOperations<String, String> zs = redisTemplate.opsForZSet();
            result = zs.add(key, value, score);
        } catch (Exception e) {
            valid = false;
            throw e;
        }
        return result;
    }

	public String getItValueString(String key) {
		try {
			ValueOperations<String, String> operations = itRedisStringTemplate.opsForValue();
			return operations.get(key);
		} catch (Exception e) {
			valid = false;
			throw e;
		}
	}
}
