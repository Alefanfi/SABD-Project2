package queries.query3;


import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampWindowFunction extends ProcessWindowFunction<Tuple2<String, Double>, Tuple3<String, String, Double>, String, TimeWindow> {

    @Override
    public void process(String s, Context context, Iterable<Tuple2<String, Double>> iterable, Collector<Tuple3<String, String, Double>> collector) {

        final SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH");
        final String date = formatter.format(new Date(context.window().getStart()));

        iterable.iterator().forEachRemaining( t -> collector.collect(new Tuple3<>(date, t.f0, t.f1))); // (timestamp, trip_id, score)
    }
}
