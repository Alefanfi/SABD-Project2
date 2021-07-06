package queries.query3;

import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class QueryAllWindowFunction extends ProcessAllWindowFunction<Tuple3<String, String, Double>, String, TimeWindow> {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");

    @Override
    public void process(Context context, Iterable<Tuple3<String, String, Double>> iterable, Collector<String> collector) {

        int i;
        List<Tuple3<String, String, Double>> scores = IteratorUtils.toList(iterable.iterator());
        scores.sort(Comparator.comparing(t -> t.f2)); // Sorting by score
        Collections.reverse(scores); // Dec order

        String out =  formatter.format(new Date(context.window().getStart())); // time_stamp

        for(i=0; i<5 && i<scores.size(); i++) {

            Tuple3<String, String, Double> t = scores.get(i);

            out += "," + t.f1 + "," + String.format("%.3f", t.f2); // trip_id, score
        }

        // Number of fields should be the same
        while(i<5){
            out += ",null,0.000";
            i++;
        }

        collector.collect(out);
    }
}
