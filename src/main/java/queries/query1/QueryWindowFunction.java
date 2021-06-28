package queries.query1;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import pojo.Record;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QueryWindowFunction extends ProcessWindowFunction<Record, String, String, TimeWindow> {

    SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");

    @Override
    public void process(String s, Context context, Iterable<Record> iterable, Collector<String> collector) throws Exception {

        int ship_military = 0;
        int ship_passenger = 0;
        int ship_cargo = 0;
        int ship_other = 0;

        Date ts = new Date();

        String out;

        for (Record r : iterable){

            if(ts.after(r.getTs())){
                ts = r.getTs();
            }

            if(r.getType().compareTo(Record.Shiptype.MILITARY) == 0){
                ship_military++;
            }
            if(r.getType().compareTo(Record.Shiptype.PASSENGER) == 0){
                ship_passenger++;
            }
            if(r.getType().compareTo(Record.Shiptype.CARGO) == 0){
                ship_cargo++;
            }
            else{
                ship_other++;
            }
        }

        out = formatter.format(ts) + "," + s + ",ship_t35," + ship_military/7.0 + ",ship_t60-69," + ship_passenger/7.0 + ",ship_t70-79," + ship_cargo/7.0 + ",ship_to," + ship_other/7.0;

        collector.collect(out);

    }
}
