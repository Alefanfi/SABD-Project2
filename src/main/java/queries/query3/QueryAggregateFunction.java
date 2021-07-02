package queries.query3;

import org.apache.flink.api.common.functions.AggregateFunction;
import common.Record;
import scala.Tuple3;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryAggregateFunction implements AggregateFunction<Record, QueryAccumulator, Tuple3<String, String, Double>> {

    /* HashMap of trips:
        key = trip_id
        value = (start_lon, start_lat, start_date)
     */
    private final HashMap<String, Tuple3<Double, Double, Date>> trip_list = new HashMap<>();

    @Override
    public QueryAccumulator createAccumulator() {
        return new QueryAccumulator();
    }

    // Computes the distance between the given points
    public double computeScore(double old_lon, double old_lat, double new_lon, double new_lat){

        double lon = new_lon - old_lon;
        double lat = new_lat - old_lat;

        return Math.sqrt(lon*lon + lat*lat);

    }

    // Removes old trips by comparing the estimated arrival with the given date
    public void cleanUpTripList(Date date){

        Iterator iter = trip_list.entrySet().iterator();

        while(iter.hasNext()){
            Map.Entry<String, Tuple3<Double, Double, Date>> trip = (Map.Entry)iter.next();

            if(trip.getValue()._3().before(date)){
                // Trip has already ended
                iter.remove();
            }
        }
    }

    @Override
    public QueryAccumulator add(Record record, QueryAccumulator accum) {

        if(accum.trip_id == null) {
            // New window
            String trip_id = record.getTrip();
            accum.trip_id = trip_id;
            accum.maxTs = record.getTs();

            Tuple3<Double, Double, Date> trip = this.trip_list.get(trip_id);

            cleanUpTripList(record.getTs()); // Remove finished trips

            if(trip == null){
                // First couple (lon,lat) found !!!
                SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH");
                Date trip_end = null;

                try {
                    trip_end = formatter.parse(trip_id.split("_")[1].split(" - ")[1]); // Find end date from trip id
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                this.trip_list.put(trip_id, new Tuple3<>(record.getLon(), record.getLat(), trip_end)); // Add trip to list

            }
            else{
                // Get initial (lon,lat)
                accum.lon = trip._1();
                accum.lat = trip._2();

                accum.score = computeScore(accum.lon, accum.lat, record.getLon(), record.getLat());
            }

        }
        else if(record.getTs().after(accum.maxTs)){
            // Compute the distance only if timestamp > maxTs
            accum.score = computeScore(accum.lon, accum.lat, record.getLon(), record.getLat());
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