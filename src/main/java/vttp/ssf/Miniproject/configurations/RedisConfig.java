package vttp.ssf.Miniproject.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.logging.Logger;

@Configuration
public class RedisConfig {

    private final Logger log = Logger.getLogger(RedisConfig.class.getName());

    // Get all the redis configuration into the class (use dependency injection)
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.database}")
    private int redisDatabase;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean("redisObjectTemplate")
    public RedisTemplate<String, Object> createRedisTemplateObject() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        // Sets the database - select 0
        config.setDatabase(redisDatabase);
        // Set the username and password if they are set
        if (!redisUsername.trim().equals("")) {
            log.info("Setting Redis username and password");
            config.setUsername(redisUsername);
            config.setPassword(redisPassword);
        }

        // Create a connection to the database
        JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();

        //Create a factory to connect to Redis
        JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
        jedisFac.afterPropertiesSet();

        // Create the RedisTemplate
        RedisTemplate<String, Object> redisTemplate= new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisFac);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

    @Bean("redisStringTemplate")
    public RedisTemplate<String, String> createRedisTemplate() {

        // Create a database configuration
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        // Sets the database - select 0
        config.setDatabase(redisDatabase);
        // Set the username and password if they are set
        if (!redisUsername.trim().equals("")) {
            log.info("Setting Redis username and password");
            config.setUsername(redisUsername);
            config.setPassword(redisPassword);
        }

        // Create a connection to the database
        JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();

        // Create a factory to connect to Redis
        JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
        jedisFac.afterPropertiesSet();

        // Create the RedisTemplate
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisFac);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
