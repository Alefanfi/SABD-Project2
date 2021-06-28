package queries.query1;

import assigner.MonthAssigner;
import flatmap.FlatMapRecord;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import pojo.Record;

import java.time.Duration;
import java.util.HashMap;

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

        // Nifi sink
        SiteToSiteClientConfig clientConfig2 = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("results")
                .requestBatchCount(5)
                .buildConfig();

        SinkFunction<String> nifiSink = new NiFiSink<>(clientConfig2, (NiFiDataPacketBuilder<String>) (s, ctx) -> new StandardNiFiDataPacket(s.getBytes(), new HashMap<>()));



        KeyedStream<Record, String> cell_data = streamExecEnv
                .addSource(nifiSource) // Add nifi source
                .flatMap( new FlatMapRecord()) // Generate new record with (ship_id, ship_type, cell_id, ts, trip_id)
                .returns(Record.class)
                .filter((FilterFunction<Record>) record -> record.getTypeSea().compareTo("Occidentale") == 0) // Keeping only records of Mar Mediterraneo Occidentale
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Record>forBoundedOutOfOrderness(Duration.ofDays(1))
                        .withTimestampAssigner((record, timestamp) -> record.getTs().getTime()) // Assigning timestamp
                )
                .keyBy(Record::getCell); // Grouping by cell id


        cell_data
                .window(TumblingEventTimeWindows.of(Time.days(7))) // Window with 7 days size
                .process(new QueryWindowFunction())
                .addSink(nifiSink);

        cell_data
                .window(new MonthAssigner()) // Window with 1 month size
                .process(new QueryWindowFunction())
                .addSink(nifiSink);


        streamExecEnv.execute("Query 1");
    }

}
