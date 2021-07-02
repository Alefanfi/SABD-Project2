package queries.query1;

import org.apache.flink.api.common.functions.AggregateFunction;
import common.Record;

public class QueryAggregateFunction implements AggregateFunction<Record, QueryAccumulator, String> {

    @Override
    public QueryAccumulator createAccumulator() {
        return new QueryAccumulator();
    }

    @Override
    public QueryAccumulator add(Record record, QueryAccumulator accum) {

        // Setting cell id
        if(accum.cell == null){
            accum.cell = record.getCell();
        }

        // Check ship's type
        if(record.getShipType().compareTo(Record.Shiptype.MILITARY) == 0){
            accum.ship_military++;
        }
        if(record.getShipType().compareTo(Record.Shiptype.PASSENGER) == 0){
            accum.ship_passenger++;
        }
        if(record.getShipType().compareTo(Record.Shiptype.CARGO) == 0){
            accum.ship_cargo++;
        }
        else{
            accum.ship_other++;
        }

        return accum;
    }

    @Override
    public String getResult(QueryAccumulator accum) {

        // Returns a string with mean number of ships of each type
        return  "," + accum.cell
                + ",ship_t35," + String.format("%.3f", accum.ship_military/7.0)
                + ",ship_t60-69," + String.format("%.3f", accum.ship_passenger/7.0)
                + ",ship_t70-79," + String.format("%.3f", accum.ship_cargo/7.0)
                + ",ship_to," + String.format("%.3f", accum.ship_other/7.0);

    }

    @Override
    public QueryAccumulator merge(QueryAccumulator accum1, QueryAccumulator accum2) {

        accum1.ship_military += accum2.ship_military;
        accum1.ship_passenger += accum2.ship_passenger;
        accum1.ship_cargo += accum2.ship_cargo;
        accum1.ship_other += accum2.ship_other;

        return accum1;
    }
}

class QueryAccumulator {

    int ship_military = 0;
    int ship_passenger = 0;
    int ship_cargo = 0;
    int ship_other = 0;

    String cell = null;

}