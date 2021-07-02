package queries.query3;

import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple3;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class QueryAllWindowFunction extends ProcessAllWindowFunction<Tuple3<String, String, Double>, String, TimeWindow> {

    SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

    @Override
    public void process(Context context, Iterable<Tuple3<String, String, Double>> iterable, Collector<String> collector) {

        List<Tuple3<String, String, Double>> scores = IteratorUtils.toList(iterable.iterator());
        scores.sort(Comparator.comparing(Tuple3::_3)); // Sorting by score
        Collections.reverse(scores); // Dec order

        String out =  formatter.format(new Date(context.window().getStart())); // time_stamp

        for(int i=0; i<5 && i<scores.size(); i++) {

            Tuple3<String, String, Double> t = scores.get(i);

            out += "," + t._2() + "," + String.format("%.3f", t._3()); // trip_id, score
        }

        collector.collect(out);
    }
}
