package queries.query1;

import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.flink.util.Collector;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import pojo.Record;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Query1 {

    public static void main(String[] args) throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");

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
                .flatMap( (NiFiDataPacket value, Collector<Record> out) -> {

                    String file = new String(value.getContent(), Charset.defaultCharset());
                    file = file.substring(file.indexOf("\n")+1); // Remove header

                    String[] lines = file.split("\n"); // Splitting file by line

                    Arrays.stream(lines).forEach( line -> {

                        String[] elems = line.split(","); // Split line by ','

                        Date date = null;
                        try {
                            date = formatter.parse(elems[4]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Record r = new Record(elems[0], Integer.parseInt(elems[1]), Double.parseDouble(elems[2]), Double.parseDouble(elems[3]), date, elems[5]);

                        out.collect(r); // each record has : (ship_id, ship_type, cell_id, ts, trip_id)
                    });

                })
                .returns(Record.class)
                .filter((FilterFunction<Record>) record -> record.getTypeSea().compareTo("Occidentale") == 0) // Keeping only records of Mar Mediterraneo Occidentale
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Record>forBoundedOutOfOrderness(Duration.ofDays(1))
                        .withTimestampAssigner((record, timestamp) -> record.getTs().getTime()) // Assigning timestamp
                )
                .keyBy(Record::getCell); // Grouping by cell id


        DataStreamSink<String> week_data = cell_data
                .window(TumblingEventTimeWindows.of(Time.days(7))) // Window with 7 days size
                .process(new QueryWindowFunction())
                .addSink(nifiSink);


        DataStreamSink<String> month_data = cell_data
                .window(TumblingEventTimeWindows.of(Time.days(30))) // Window with 1 month size
                .process(new QueryWindowFunction())
                .addSink(nifiSink);


        streamExecEnv.execute("Query 1");
    }

}
