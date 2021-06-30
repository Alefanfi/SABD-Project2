package queries.query3;

import flatmap.FlatMapRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.nifi.NiFiDataPacket;
import org.apache.flink.streaming.connectors.nifi.NiFiSource;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import pojo.Record;
import sink.MyRedisMapper;

import java.text.SimpleDateFormat;
import java.time.Duration;


public class Query3 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment streamExecEnv = StreamExecutionEnvironment
                .getExecutionEnvironment();

        // Nifi source
        SiteToSiteClientConfig clientConfig = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("dataset")
                .requestBatchCount(5)
                .buildConfig();

        SourceFunction<NiFiDataPacket> nifiSource = new NiFiSource(clientConfig);

        // Redis sink
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("redis").setPort(6379).build();


        KeyedStream<Record, String> trip_data = streamExecEnv
                .addSource(nifiSource)
                .flatMap(new FlatMapRecord(new SimpleDateFormat("yy-MM-dd HH:mm")))
                .returns(Record.class)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Record>forBoundedOutOfOrderness(Duration.ofDays(1))
                                .withTimestampAssigner((record, timestamp) -> record.getTs().getTime()) // Assigning timestamps
                )
                .keyBy(Record::getTrip); // Grouping by trip_id

        trip_data
                .window(TumblingEventTimeWindows.of(Time.hours(1))) // 1 hour window
                .aggregate(new QueryAggregateFunction()) // (time_stamp, trip_id, score)
                .windowAll(TumblingEventTimeWindows.of(Time.hours(1))) // 1 hour window
                .process(new QueryAllWindowFunction()) // Returns a string with (time_stamp, id_1 ,score_1, ... , id_5, score_5)
                .addSink(new RedisSink<>(conf, new MyRedisMapper("query3_1h"))); // Add sink


        trip_data
                .window(TumblingEventTimeWindows.of(Time.hours(2))) // 1 hour window
                .aggregate(new QueryAggregateFunction()) // (time_stamp, trip_id, score)
                .windowAll(TumblingEventTimeWindows.of(Time.hours(2))) // 1 hour window
                .process(new QueryAllWindowFunction()) // Returns a string with (time_stamp, id_1 ,score_1, ... , id_5, score_5)
                .addSink(new RedisSink<>(conf, new MyRedisMapper("query3_2h"))); // Add sink


        streamExecEnv.execute("Query 3");
    }
}
