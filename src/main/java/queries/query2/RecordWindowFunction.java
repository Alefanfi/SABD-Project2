package queries.query2;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


public class RecordWindowFunction extends ProcessAllWindowFunction<Tuple2<String, Tuple2<Integer,Integer>>, String, TimeWindow> {
    @Override
    public void process(Context context, Iterable<Tuple2<String, Tuple2<Integer, Integer>>> iterable, Collector<String> collector) throws Exception {
        List<Tuple2<String, Integer>> listAM = new ArrayList<>();
        List<Tuple2<String, Integer>> listPM = new ArrayList<>();

        for(Tuple2<String, Tuple2<Integer,Integer>> t : iterable){

            listAM.add(new Tuple2<>(t.f0, t.f1.f0));
            listPM.add(new Tuple2<>(t.f0,t.f1.f1));

        }

        listAM.sort((a, b) -> b.f1 - a.f1);
        listPM.sort((a, b) -> b.f1 - a.f1);

        LocalDateTime startDate = LocalDateTime.ofEpochSecond(
                context.window().getStart() / 1000, 0, ZoneOffset.UTC);
        StringBuilder result = new StringBuilder(String.valueOf(startDate));

        int sizeAM = listAM.size();
        int sizePM = listPM.size();

        for (int i = 0; i < 3 && i < sizeAM; i++){
            if(i==0)
                result.append(", "+" AM: ").append(listAM.get(i).f0);
            else
                result.append(", ").append(listAM.get(i).f0);
        }

        for (int i = 0; i < 3 && i < sizePM; i++){
            if(i==0)
                result.append("; PM: ").append(listPM.get(i).f0);
            else
                result.append(", ").append(listPM.get(i).f0);
        }

        collector.collect(result.toString());
    }

}

/*public class RecordWindowFunction extends ProcessAllWindowFunction<Tuple2<Tuple2<String, String>, Tuple2<Integer,Integer>>, String, TimeWindow>{

    @Override
    public void process(Context context, Iterable<Tuple2<Tuple2<String, String>, Tuple2<Integer, Integer>>> iterable, Collector<String> collector) throws Exception {
        List<Tuple2<Tuple2<String, String>, Integer>> listAM = new ArrayList<>();
        List<Tuple2<Tuple2<String, String>, Integer>> listPM = new ArrayList<>();

        for(Tuple2<Tuple2<String, String>, Tuple2<Integer,Integer>> t : iterable){

            listAM.add(new Tuple2<>(new Tuple2<>(t.f0.f0, t.f0.f1), t.f1.f0));
            listPM.add(new Tuple2<>(new Tuple2<>(t.f0.f0, t.f0.f1),t.f1.f1));

        }

        listAM.sort((a, b) -> b.f1 - a.f1);
        listPM.sort((a, b) -> b.f1 - a.f1);

        LocalDateTime startDate = LocalDateTime.ofEpochSecond(
                context.window().getStart() / 1000, 0, ZoneOffset.UTC);
        StringBuilder result = new StringBuilder(String.valueOf(startDate));

        int sizeAM = listAM.size();
        int sizePM = listPM.size();

        for (int i = 0; i < 3 && i < sizeAM; i++){
            if(i==0)
                result.append(", "+" AM: ").append(listAM.get(i).f0);
            else
                result.append(", ").append(listAM.get(i).f0);
        }

        for (int i = 0; i < 3 && i < sizePM; i++){
            if(i==0)
                result.append("; PM: ").append(listPM.get(i).f0);
            else
                result.append(", ").append(listPM.get(i).f0);
        }

        collector.collect(result.toString());
    }
}*/