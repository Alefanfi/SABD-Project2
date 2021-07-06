package queries.query2;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampWindowFunction extends ProcessWindowFunction<Tuple4<String, String, Integer, Integer>, Tuple5<String, String, String, Integer, Integer>, String, TimeWindow> {

    @Override
    public void process(String s, Context context, Iterable<Tuple4<String, String, Integer, Integer>> iterable, Collector<Tuple5<String, String, String, Integer, Integer>> collector) {

        final SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
        final String date = formatter.format(new Date(context.window().getStart()));

        iterable.iterator().forEachRemaining( t -> collector.collect(new Tuple5<>(date, t.f0, t.f1, t.f2, t.f3))); // (timestamp, sea_type, cell_id, count_am, count_pm)

    }
}