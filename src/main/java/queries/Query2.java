package queries;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.nifi.*;
import org.apache.flink.util.Collector;
import org.apache.nifi.remote.client.SiteToSiteClient;
import org.apache.nifi.remote.client.SiteToSiteClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class Query2 {

    private static final Logger LOG = LoggerFactory.getLogger(Query2.class);

    public static void main(String[] args) throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment streamExecEnv = StreamExecutionEnvironment.getExecutionEnvironment();

        SiteToSiteClientConfig clientConfig = new SiteToSiteClient
                .Builder()
                .url("http://nifi:8080/nifi")
                .portName("dataset")
                .requestBatchCount(5)
                .buildConfig();

        SourceFunction<NiFiDataPacket> nifiSource = new NiFiSource(clientConfig);

        /*DataStreamSink<Query2POJO> originalStream = streamExecEnv
                .addSource(nifiSource)
                .map(value -> Query2POJO.parseFromValue(value))
                .filter(Query2POJO.filterByHours)
                .print();*/

        DataStream<Tuple3<String, Tuple3<String, Integer, String>, Tuple3<Double, Double, Timestamp>>> dataStream = streamExecEnv
                .addSource(nifiSource)
                .flatMap(
                        (NiFiDataPacket value, Collector<Tuple3<String, Tuple3<String, Integer, String>, Tuple3<Double, Double, Timestamp>>> out) -> {

                            String file = new String(value.getContent(), Charset.defaultCharset());
                            String fileWithoutFirstRow = file.substring(file.indexOf("\n")+1);
                            String[] lines = fileWithoutFirstRow.split("\n"); // Splitting file by line
                            Arrays.stream(lines).forEach(l -> {

                                String[] temp = l.split(",");

                                Tuple3<String, Tuple3<String, Integer, String>, Tuple3<Double, Double, Timestamp>> tupla = null;

                                try {
                                    Date date = formatter.parse(temp[4]);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
                                    tupla = new Tuple3<>(
                                            time,
                                            new Tuple3<>(temp[0], Integer.parseInt(temp[1]), temp[5]), //ship_id, shiptype, trip_id
                                            new Tuple3<>(Double.parseDouble(temp[2]), Double.parseDouble(temp[3]), new Timestamp(formatter.parse(temp[4]).getTime()))); //lon, lat, timestamp, ore_minuti
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                out.collect(tupla);

                            });
                        })

                .returns(Types.TUPLE(Types.STRING, Types.TUPLE(Types.STRING, Types.INT, Types.STRING), Types.TUPLE(Types.DOUBLE, Types.DOUBLE, Types.SQL_TIMESTAMP)));

        DataStreamSink<Tuple3<String, Tuple3<String, Integer, String>, Tuple3<Double, Double, Timestamp>>> datastream1 =
                dataStream.filter((FilterFunction<Tuple3<String, Tuple3<String, Integer, String>, Tuple3<Double, Double, Timestamp>>>) dati -> {

                    String[] ore_minuti = dati.f0.split(":");
                    Calendar cal1 = Calendar.getInstance();
                    cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ore_minuti[0]));
                    cal1.set(Calendar.MINUTE, Integer.parseInt(ore_minuti[1]));

                    //String limit = "00:00";
                    Calendar cal2 = Calendar.getInstance();
                    cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt("00"));
                    cal2.set(Calendar.MINUTE, Integer.parseInt("00"));

                    //String limit = "11:59";
                    Calendar cal3 = Calendar.getInstance();
                    cal3.set(Calendar.HOUR_OF_DAY, Integer.parseInt("11"));
                    cal3.set(Calendar.MINUTE, Integer.parseInt("59"));

                    if(cal1.after(cal2) && cal1.before(cal3)){
                        return true;
                    }else{
                      return false;
                    }
                })
                .print();


        SiteToSiteClientConfig clientConfig2 = new SiteToSiteClient.Builder()
                .url("http://nifi:8080/nifi")
                .portName("results")
                .requestBatchCount(5)
                .buildConfig();

        SinkFunction<String> nifiSink = new NiFiSink<>(
                clientConfig2, (NiFiDataPacketBuilder<String>) (s, ctx) -> new StandardNiFiDataPacket(s.getBytes(), new HashMap<>()));

        //dataStream.addSink(nifiSink);

        streamExecEnv.execute();
    }
}
