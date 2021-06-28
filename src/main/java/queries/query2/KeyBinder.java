package queries.query2;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class KeyBinder
        extends ProcessWindowFunction<Tuple2<Integer,Integer>, Tuple2<Tuple2<String, String>, Tuple2<Integer,Integer>>, Tuple2<String, String>, TimeWindow> {

    @Override
    public void process(Tuple2<String, String> key, Context context, Iterable<Tuple2<Integer, Integer>> iterable, Collector<Tuple2<Tuple2<String, String>, Tuple2<Integer, Integer>>> collector) throws Exception {
        Tuple2<Integer, Integer> count = iterable.iterator().next();
        collector.collect(new Tuple2<>(key, count));
    }
}
