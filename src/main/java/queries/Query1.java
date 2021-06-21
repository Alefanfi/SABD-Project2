package queries;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.flink.util.Collector;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import scala.Tuple2;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

public class Query1 {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streamExecEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Taking dataset from nifi
        SiteToSiteClientConfig clientConfig = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("dataset")
                .requestBatchCount(5)
                .buildConfig();

        SourceFunction<NiFiDataPacket> nifiSource = new NiFiSource(clientConfig);

        DataStream<Tuple2<Tuple2<String, String>, Integer>> prova = streamExecEnv
                .addSource(nifiSource)
                .flatMap( (NiFiDataPacket value, Collector<Tuple2<Tuple2<String, String>, Integer>> out) -> {

                    String file = new String(value.getContent(), Charset.defaultCharset());
                    String[] lines = file.split("\n"); // Splitting file by line

                    Arrays.stream(lines).forEach( line -> {

                        String[] elems = line.split(",");

                        String aiuto = "help";

                        Tuple2<Tuple2<String, String>, Integer> tuple = new Tuple2<>(new Tuple2<>(elems[7], aiuto), Integer.parseInt(elems[1])); // (( ts, aiuto), ship_type)

                        out.collect(tuple);
                    });

                });


        SiteToSiteClientConfig clientConfig2 = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("results")
                .requestBatchCount(5)
                .buildConfig();

        SinkFunction<String> nifiSink = new NiFiSink<>(clientConfig2, (NiFiDataPacketBuilder<String>) (s, ctx) -> new StandardNiFiDataPacket(s.getBytes(), new HashMap<>()));

        //prova.addSink(nifiSink);

        streamExecEnv.execute("Query 1");
    }
}
