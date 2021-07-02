package queries.query2;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;


import java.text.SimpleDateFormat;
import java.util.*;

public class QueryWindowFunction extends ProcessWindowFunction<Tuple4<String, String, Integer, Integer>, String, String, TimeWindow> {

    SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");

    @Override
    public void process(String s, Context context, Iterable<Tuple4<String, String, Integer, Integer>> iterable, Collector<String> collector){

        List<Tuple3<String, String, Integer>> listAM = new ArrayList<>();
        List<Tuple3<String, String, Integer>> listPM = new ArrayList<>();

        iterable.iterator().forEachRemaining(t -> {

            listAM.add(new Tuple3<>(t.f0, t.f1, t.f2));
            listPM.add(new Tuple3<>(t.f0, t.f1, t.f3));

        });

        listAM.sort(Comparator.comparing(t -> t.f2)); // Sorting by frequency
        Collections.reverse(listAM); // Dec order

        listPM.sort(Comparator.comparing(t -> t.f2)); // Sorting by frequency
        Collections.reverse(listPM); // Dec order

        String out =  formatter.format(new Date(context.window().getStart())) + "," + listAM.get(0).f0; // time_stamp and sea

        out += ",slot_a";

        for(int i=0; i<3 && i<listAM.size(); i++) {
            out += "," + listAM.get(i).f1;
        }

        out += ",slot_p";

        for(int j=0; j<3 && j<listPM.size(); j++) {
            out += "," + listPM.get(j).f1;
        }

        collector.collect(out);

    }

}