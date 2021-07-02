package queries.query3;

import org.apache.flink.api.common.functions.AggregateFunction;
import pojo.Record;;
import scala.Tuple3;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QueryAggregateFunction implements AggregateFunction<Record, QueryAccumulator, Tuple3<String, String, Double>> {


    @Override
    public QueryAccumulator createAccumulator() {
        return new QueryAccumulator();
    }

    @Override
    public QueryAccumulator add(Record record, QueryAccumulator accum) {

        if(accum.trip_id == null) {
            // First couple (lon,lat) found !
            accum.lon = record.getLon();
            accum.lat = record.getLat();
            accum.trip_id = record.getTrip();
            accum.maxTs = record.getTs();
        }
        else if(record.getTs().after(accum.maxTs)){
            // Compute the distance
            double lon = record.getLon() - accum.lon;
            double lat = record.getLat() - accum.lat;

            accum.score = Math.sqrt(lon*lon + lat*lat);
            accum.maxTs = record.getTs();

        }

        return accum;
    }

    @Override
    public Tuple3<String, String, Double> getResult(QueryAccumulator accum) {

        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:00:00");
        return new Tuple3<>(formatter.format(accum.maxTs),accum.trip_id, accum.score); // (time_stamp, trip_id, score)

    }

    @Override
    public QueryAccumulator merge(QueryAccumulator acc1, QueryAccumulator acc2) {
        if(acc1.maxTs.after(acc2.maxTs)){
            return acc1;
        }
        else{
            return acc2;
        }
    }
}

class QueryAccumulator {

    String trip_id = null;
    double lon = 0;
    double lat = 0;
    double score = 0;
    Date maxTs = null;
}