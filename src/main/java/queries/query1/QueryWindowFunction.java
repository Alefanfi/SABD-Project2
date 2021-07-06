package queries.query1;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QueryWindowFunction extends ProcessWindowFunction<String, String, String, TimeWindow> {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

    @Override
    public void process(String s, Context context, Iterable<String> iterable, Collector<String> collector) {

        String ts = formatter.format(new Date(context.window().getStart())); // Get timestamp from window

        iterable.iterator().forEachRemaining(elem -> {

            collector.collect(ts + elem); // Adding timestamp

        });

    }
}
