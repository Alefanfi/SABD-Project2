package queries.query2;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import pojo.Record;

import java.time.Duration;
import java.util.HashMap;

public class Query2 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment streamExecEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        SiteToSiteClientConfig clientConfig = new SiteToSiteClient
                .Builder()
                .url("http://nifi:8080/nifi")
                .portName("dataset")
                .requestBatchCount(5)
                .buildConfig();

        SourceFunction<NiFiDataPacket> nifiSource = new NiFiSource(clientConfig);

        SiteToSiteClientConfig clientConfig2 = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("results")
                .requestBatchCount(5)
                .buildConfig();

        SinkFunction<String> nifiSink = new NiFiSink<>(
                clientConfig2, (NiFiDataPacketBuilder<String>) (s, ctx) -> new StandardNiFiDataPacket(s.getBytes(), new HashMap<>()));

        KeyedStream<Record, Tuple2<String, String>> stream = streamExecEnv
                .addSource(nifiSource)
                .map(Record::parseFromValue)
                .returns(Record.class)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Record>forBoundedOutOfOrderness(Duration.ofSeconds(1))
                                .withTimestampAssigner((record, timestamp) -> record.getTs().getTime())
                )
                .keyBy(Record::getInfo); //Grouping by cell id, sea type

        DataStreamSink<String> weekStream = stream
                .window(TumblingEventTimeWindows.of(Time.days(7)))
                .aggregate(new CountAggregator(), new KeyBinder())
                .windowAll(TumblingEventTimeWindows.of(Time.days(7)))
                .process(new RecordWindowFunction())
                .print();

        streamExecEnv.execute("Query 2");
    }
}

class Count{
    public String trip;
    public Integer countAM=0;
    public Integer countPM=0;
}
