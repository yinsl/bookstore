/**
 * RedisConfig.java Create on 2017年8月3日
 * Copyright (c) 2017年8月3日 by 上汽集团商用车技术中心
 *
 * @author <a href="renhuaiyu@saicmotor.com">任怀宇</a>
 * @version 1.0
 */
package com.maxus.tsp.common.redis.allredis;


import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxus.tsp.platform.service.model.vo.GBLoginNo;
import com.maxus.tsp.platform.service.model.vo.ItRedisInfo;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;


/**
 * @ClassName: RedisConfig.java
 * @Description: 初始化redis相关配置
 * @author 任怀宇
 * @version V1.0
 * @Date 2017年8月3日 下午12:58:50
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    private final static Logger logger = LogManager.getLogger(RedisConfig.class);

    @Value("${spring.redis.ithost}")
    String itHostname;
    @Value("${spring.redis.itport}")
    int itPort;
    @Value("${spring.redis.itdatabase}")
    int itdatabase;
    @Value("${spring.redis.itpassword}")
    String itPassword;

    @Value("${spring.redis.host}")
    String hostname;
    @Value("${spring.redis.port}")
    int port;
    @Value("${spring.redis.database}")
    int database;
    @Value("${spring.redis.password}")
    String password;
    @Value("${spring.redis.timeout}")
    int timeout;
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-wait}")
    private long maxWait;


    @Autowired
    @Qualifier("itlettuceConnectionFactory")
    LettuceConnectionFactory itLettuceConnectionFactory;
    /**
     * Set the per-connection request queue size. The command invocation will lead to a RedisException if the queue size is exceeded.
     * Setting the requestQueueSize to a lower value will lead earlier to exceptions during overload or while the connection
     * is in a disconnected state. A higher value means hitting the boundary will take longer to occur,
     * but more requests will potentially be queued up and more heap space is used. Defaults to Integer.MAX_VALUE.
     * See DEFAULT_REQUEST_QUEUE_SIZE.
     */
    private final int requestQueueSize = 20000;
    @Bean
    @Primary
    LettuceConnectionFactory myLettuceConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(database);
        redisStandaloneConfiguration.setHostName(hostname);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
        ClientOptions options = ClientOptions.builder()
        		.disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
        		.build();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder = LettuceClientConfiguration.builder();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration,
                lettuceClientConfigurationBuilder.commandTimeout(Duration.ofSeconds(20)).clientOptions(options).build());
        return factory;
    }

    @Bean("itlettuceConnectionFactory")
    LettuceConnectionFactory itLettuceConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(itdatabase);
        redisStandaloneConfiguration.setHostName(itHostname);
        redisStandaloneConfiguration.setPort(itPort);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(itPassword));

        ClientOptions options = ClientOptions.builder().disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
        		.requestQueueSize(requestQueueSize).build();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder = LettuceClientConfiguration.builder();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration,
                lettuceClientConfigurationBuilder.commandTimeout(Duration.ofSeconds(20)).clientOptions(options).build());
        return factory;
    }




    @Bean(name = "redisKeyDatabase")
    public StringRedisTemplate redisTemplate(LettuceConnectionFactory myLettuceConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(myLettuceConnectionFactory);

        Jackson2JsonRedisSerializer<?> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        logger.info("redistemplate[redisKeyDatabase] connection info -- {}@{}:{}/{}",myLettuceConnectionFactory.getPassword(),
        		myLettuceConnectionFactory.getHostName(),
        		myLettuceConnectionFactory.getPort(),
        		myLettuceConnectionFactory.getDatabase());
        return template;
    }

    @Bean(name = "itredisDatabase")
    public RedisTemplate<String, ItRedisInfo> itRedisTemplate() {
        Jackson2JsonRedisSerializer<ItRedisInfo> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<ItRedisInfo>(ItRedisInfo.class);
        //不需要配置ObjectMapper，如果配置了，会将类信息存入redis，或根据redis中的类信息去解析
        RedisTemplate<String, ItRedisInfo> template = new RedisTemplate<String, ItRedisInfo>();
        template.setConnectionFactory(itLettuceConnectionFactory);
        //key仍旧为string类型序列化，反序列化
        template.setKeySerializer(template.getStringSerializer());
        //value按ItRedisInfo格式序列化，反序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        logger.info("redistemplate[itredisDatabase] connection info -- {}@{}:{}/{}",itLettuceConnectionFactory.getPassword(),
        		itLettuceConnectionFactory.getHostName(),
        		itLettuceConnectionFactory.getPort(),
        		itLettuceConnectionFactory.getDatabase());
        return template;
    }

    @Bean(name = "itredisStringDatabase")
    public StringRedisTemplate itredisStringDatabase() {
        StringRedisTemplate template = new StringRedisTemplate(itLettuceConnectionFactory);
        Jackson2JsonRedisSerializer<?> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        logger.info("redistemplate[itredisStringDatabase] connection info -- {}@{}:{}/{}",itLettuceConnectionFactory.getPassword(),
        		itLettuceConnectionFactory.getHostName(),
        		itLettuceConnectionFactory.getPort(),
        		itLettuceConnectionFactory.getDatabase());
        return template;
    }

    @Bean(name = "gbredisDatabase")
    public RedisTemplate<String, GBLoginNo> gbRedisTemplate() {
        Jackson2JsonRedisSerializer<GBLoginNo> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<GBLoginNo>(GBLoginNo.class);
        //不需要配置ObjectMapper，如果配置了，会将类信息存入redis，或根据redis中的类信息去解析
        RedisTemplate<String, GBLoginNo> template = new RedisTemplate<String, GBLoginNo>();
        template.setConnectionFactory(itLettuceConnectionFactory);
        //key仍旧为string类型序列化，反序列化
        template.setKeySerializer(template.getStringSerializer());
        //value按ItRedisInfo格式序列化，反序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        logger.info("redistemplate[gbredisDatabase] connection info -- {}@{}:{}/{}",itLettuceConnectionFactory.getPassword(),
        		itLettuceConnectionFactory.getHostName(),
        		itLettuceConnectionFactory.getPort(),
        		itLettuceConnectionFactory.getDatabase());
        return template;
    }

}
