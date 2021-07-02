package queries.query2;

import common.MonthAssigner;
import common.FlatMapRecord;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import common.Record;
import common.MyRedisMapper;

import java.text.SimpleDateFormat;
import java.time.Duration;

public class Query2 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment streamExecEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Nifi Source
        SiteToSiteClientConfig clientConfig = new SiteToSiteClient
                .Builder()
                .url("http://nifi:8080/nifi")
                .portName("dataset")
                .requestBatchCount(5)
                .buildConfig();

        SourceFunction<NiFiDataPacket> nifiSource = new NiFiSource(clientConfig);

        // Redis sink
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("redis").setPort(6379).build();


        KeyedStream<Record, String> sea_data = streamExecEnv
                .addSource(nifiSource) // Add source
                .flatMap(new FlatMapRecord(new SimpleDateFormat("yy-MM-dd HH"))) // Generate new record with (ship_id, ship_type, cell_id, ts, trip_id, sea_type)
                .returns(Record.class)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Record>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                                .withTimestampAssigner((record, timestamp) -> record.getTs().getTime()) // Assigning timestamps
                )
                .keyBy(Record::getCell); // Grouping by cell_id


        sea_data
                .window(TumblingEventTimeWindows.of(Time.days(7))) // 7 day window
                .aggregate(new QueryAggregateFunction()) // Calculates frequency for each cell both in am and pm slots , returns (sea_type, cell_id, count_am, count_pm)
                .keyBy(t -> t.f0) // Grouping by sea_type
                .window(TumblingEventTimeWindows.of(Time.days(7))) // 7 days window
                .process(new QueryWindowFunction())
                .addSink(new RedisSink<>(conf, new MyRedisMapper("query2_week"))); // Add sink

        sea_data
                .window(new MonthAssigner()) // 1 month window
                .aggregate(new QueryAggregateFunction()) // Calculates frequency for each cell both in am and pm slots , returns (sea_type, cell_id, count_am, count_pm)
                .keyBy(t -> t.f0) // Grouping by sea_type
                .window(new MonthAssigner()) // 1 month window
                .process(new QueryWindowFunction())
                .addSink(new RedisSink<>(conf, new MyRedisMapper("query2_month"))); // Add sink


        streamExecEnv.execute("Query 2");
    }
}