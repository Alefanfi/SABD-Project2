package queries.query2;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class KeyBinder
        extends ProcessWindowFunction<Tuple2<Integer,Integer>, Tuple2<String, Tuple2<Integer,Integer>>, String, TimeWindow> {

    @Override
    public void process(String key, Context context, Iterable<Tuple2<Integer, Integer>> counter, Collector<Tuple2<String, Tuple2<Integer, Integer>>> out) {
        Tuple2<Integer, Integer> count = counter.iterator().next();
        out.collect(new Tuple2<>(key, count));
    }
}
