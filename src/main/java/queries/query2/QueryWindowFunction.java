package queries.query2;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;


import java.text.SimpleDateFormat;
import java.util.*;

public class QueryWindowFunction extends ProcessWindowFunction<Tuple5<String, String, String, Integer, Integer>, String, String, TimeWindow> {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

    @Override
    public void process(String s, Context context, Iterable<Tuple5<String, String, String, Integer, Integer>> iterable, Collector<String> collector){

        int i;
        List<Tuple3<String, String, Integer>> listAM = new ArrayList<>();
        List<Tuple3<String, String, Integer>> listPM = new ArrayList<>();

        iterable.iterator().forEachRemaining(t -> {

            listAM.add(new Tuple3<>(t.f1, t.f2, t.f3));
            listPM.add(new Tuple3<>(t.f1, t.f2, t.f4));

        });

        listAM.sort(Comparator.comparing(t -> t.f2)); // Sorting by frequency
        Collections.reverse(listAM); // Dec order

        listPM.sort(Comparator.comparing(t -> t.f2)); // Sorting by frequency
        Collections.reverse(listPM); // Dec order

        String out =  formatter.format(new Date(context.window().getStart())) + "," + listAM.get(0).f0; // time_stamp and sea

        out += ",slot_a";

        for(i=0; i<3 && i<listAM.size(); i++) {
            out += "," + listAM.get(i).f1;
        }

        // Number of fields should be the same
        while(i<3){
            out += ",null";
            i++;
        }

        out += ",slot_p";

        for(i=0; i<3 && i<listPM.size(); i++) {
            out += "," + listPM.get(i).f1;
        }

        // Number of fields should be the same
        while(i<3){
            out += ",null";
            i++;
        }

        collector.collect(out);

    }

}