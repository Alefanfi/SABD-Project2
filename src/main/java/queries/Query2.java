package queries;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.flink.util.Collector;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class Query2 {

    private static final Logger log = Logger.getLogger(Query2.class.getName());

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment streamExecEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        SiteToSiteClientConfig clientConfig = new SiteToSiteClient
                .Builder()
                .url("http://nifi:8080/nifi")
                .portName("dataset")
                .requestBatchCount(5)
                .buildConfig();

        SiteToSiteClientConfig clientConfig2 = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("results")
                .requestBatchCount(5)
                .buildConfig();

        SourceFunction<NiFiDataPacket> nifiSource = new NiFiSource(clientConfig);
        DataStream<NiFiDataPacket> nifi = streamExecEnv.addSource(nifiSource);

        DataStream<String> dataStream = nifi
                .flatMap( (NiFiDataPacket value, Collector<String> out) -> {

                    String file = new String(value.getContent(), Charset.defaultCharset());
                    Arrays.stream(file.split("\n")).forEach(out::collect);  // Splitting file by line

            })
                .returns(Types.STRING);

        DataStreamSink<Tuple2<Tuple3<String, Integer, String>, Tuple3<Long, Long, String>>> tupleDataStream = dataStream
                .flatMap(
                        (String s, Collector<Tuple2<Tuple3<String, Integer, String>, Tuple3<Long, Long, String>>> out) -> {

                            String[] temp = s.split(",");

                            Tuple2<Tuple3<String, Integer, String>, Tuple3<Long, Long, String>> tupla = new Tuple2<>(
                            new Tuple3<>(temp[0], Integer.parseInt(temp[1]), temp[10]), //ship_id, shiptype, trip_id
                            new Tuple3<>(Long.valueOf(temp[3]), Long.valueOf(temp[4]), temp[7])); //lon, lat, timestamp

                })
                .returns(Types.TUPLE(Types.TUPLE(Types.STRING, Types.INT, Types.STRING), Types.TUPLE(Types.LONG, Types.LONG, Types.STRING)))
                .print();

        /*DataStream<Tuple2<Tuple3<String, Integer, String>, Tuple3<Long, Long, String>>> tupleDataStream = dataStream
                .map((MapFunction<String, Tuple2<Tuple3<String, Integer, String>, Tuple3<Long, Long, String>>>) s -> {

                    String[] temp = s.split(",");

                    return new Tuple2<>(
                            new Tuple3<>(temp[0], Integer.parseInt(temp[1]), temp[10]), //ship_id, shiptype, trip_id
                            new Tuple3<>(Long.valueOf(temp[3]), Long.valueOf(temp[4]), temp[7]) //lon, lat, timestamp
                    );
                });*/


        SinkFunction<String> nifiSink = new NiFiSink<>(
                clientConfig2, (NiFiDataPacketBuilder<String>) (s, ctx) -> new StandardNiFiDataPacket(s.getBytes(), new HashMap<>()));

        dataStream.addSink(nifiSink);

        streamExecEnv.execute();
    }
}
