package queries;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.flink.util.Collector;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import pojo.Record;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

public class Query1 {

    public static void main(String[] args) throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment streamExecEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        // Taking data from nifi
        SiteToSiteClientConfig clientConfig = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("dataset")
                .requestBatchCount(5)
                .buildConfig();

        SourceFunction<NiFiDataPacket> nifiSource = new NiFiSource(clientConfig);

        DataStream<Record> prova = streamExecEnv
                .addSource(nifiSource) // Add nifi source
                .flatMap( (NiFiDataPacket value, Collector<Record> out) -> {

                    String file = new String(value.getContent(), Charset.defaultCharset());
                    String[] lines = file.split("\n"); // Splitting file by line

                    Arrays.stream(lines).forEach( line -> {

                        String[] elems = line.split(","); // Split line by ','

                        Record r = null;

                        try {
                            r = new Record(elems[0], Integer.parseInt(elems[1]), Double.parseDouble(elems[2]), Double.parseDouble(elems[3]), formatter.parse(elems[4]), elems[5]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        out.collect(r); // each record has : ship_id, ship_type, cell_id, ts, trip_id
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
