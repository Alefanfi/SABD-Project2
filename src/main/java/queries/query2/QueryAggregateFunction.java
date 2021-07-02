package queries.query2;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import common.Record;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QueryAggregateFunction implements AggregateFunction<Record, Count, Tuple4<String, String, Integer, Integer>> {
    @Override
    public Count createAccumulator() {
        return new Count();
    }

    @Override
    public Count add(Record record, Count count) {

        if(count.sea == null || count.cell == null){
            // New window
            count.sea = record.getSeaType();
            count.cell = record.getCell();
        }

        // Checks if the trip has already been considered
        if(!count.trips.contains(record.getTrip())){

            // Get hour from record timestamp
            Calendar cal = Calendar.getInstance();
            cal.setTime(record.getTs());
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            if(hour < 12){
                count.countAM++; // slot am
            }else {
                count.countPM++; // slot pm
            }

            count.trips.add(record.getTrip()); // Adds the trip
        }

        return count;
    }

    @Override
    public Tuple4<String, String, Integer, Integer> getResult(Count count) {
        return new Tuple4<>(count.sea.name(), count.cell, count.countAM, count.countPM); // (sea_type, cell_id, count_am, count_pm)
    }

    @Override
    public Count merge(Count count, Count count2) {
        count.countAM += count2.countAM;
        count.countPM += count2.countPM;
        return count;
    }
}

class Count{

    List<String> trips = new ArrayList<>();

    Record.Seatype sea = null;
    String cell = null;
    int countAM=0;
    int countPM=0;
}
