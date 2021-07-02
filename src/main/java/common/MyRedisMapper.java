package common;

import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

public class MyRedisMapper implements RedisMapper<String> {

    private final String key; // List's key

    public MyRedisMapper(String key){
        this.key = key;
    }

    @Override
    public RedisCommandDescription getCommandDescription() {
        return new RedisCommandDescription(RedisCommand.LPUSH, null); // Use LPUSH to add new value on top of the list
    }

    @Override
    public String getKeyFromData(String data) {
        return this.key;
    }

    @Override
    public String getValueFromData(String data) {
        return data;
    }
}
