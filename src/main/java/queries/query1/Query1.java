package queries.query1;

import assigner.MonthAssigner;
import flatmap.FlatMapRecord;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import pojo.Record;
import sink.MyRedisMapper;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Query1 {

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


        KeyedStream<Record, String> cell_data = streamExecEnv
                .addSource(nifiSource) // Add source
                .flatMap( new FlatMapRecord(new SimpleDateFormat("yy-MM-dd"))) // Generate new record with (ship_id, ship_type, cell_id, ts, trip_id)
                .returns(Record.class)
                .filter((FilterFunction<Record>) record -> record.getSeaType().compareTo("Occidentale") == 0) // Keeping only records of Western Mediterranean
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Record>forBoundedOutOfOrderness(Duration.ofDays(1))
                        .withTimestampAssigner((record, timestamp) -> record.getTs().getTime()) // Assigning timestamps
                )
                .keyBy(Record::getCell); // Grouping by cell id


        cell_data
                .window(TumblingEventTimeWindows.of(Time.days(7))) // 7 days window
                .aggregate(new QueryAggregateFunction(), new QueryWindowFunction())
                .addSink(new RedisSink<>(conf, new MyRedisMapper("query1_week"))); // Add sink

        cell_data
                .window(new MonthAssigner()) // 1 month window
                .aggregate(new QueryAggregateFunction(), new QueryWindowFunction())
                .addSink(new RedisSink<>(conf, new MyRedisMapper("query1_month"))); // Add sink

        // execute program
        JobExecutionResult result = streamExecEnv.execute("Query 1");;
        System.out.println("The job took " + result.getNetRuntime(TimeUnit.SECONDS) + " to execute");
    }

}
