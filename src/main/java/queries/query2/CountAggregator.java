package queries.query2;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import pojo.Record;

public class CountAggregator implements AggregateFunction<Record, Count, Tuple2<Integer, Integer>> {
    @Override
    public Count createAccumulator() {
        return new Count();
    }

    @Override
    public Count add(Record record, Count count) {
        count.trip = record.getTrip();

        String[] ore = record.getOre().split(":");

        if(Integer.parseInt(ore[0]) < 12){
            count.countAM++;
        }else if (Integer.parseInt(ore[0]) > 11){
            count.countPM++;
        }
        return count;
    }

    @Override
    public Tuple2<Integer, Integer> getResult(Count count) {
        return new Tuple2<>(count.countAM, count.countPM);
    }

    @Override
    public Count merge(Count count, Count count2) {
        count.countAM += count2.countAM;
        count.countPM += count2.countPM;
        return count;
    }
}
