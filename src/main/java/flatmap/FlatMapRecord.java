package flatmap;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.connectors.nifi.NiFiDataPacket;
import org.apache.flink.util.Collector;
import pojo.Record;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class FlatMapRecord implements FlatMapFunction<NiFiDataPacket, Record> {

    private SimpleDateFormat formatter;

    public FlatMapRecord(SimpleDateFormat formatter){
        this.formatter = formatter;
    }

    @Override
    public void flatMap(NiFiDataPacket value, Collector<Record> out) {

        String file = new String(value.getContent(), Charset.defaultCharset());
        file = file.substring(file.indexOf("\n")+1); // Remove header

        String[] lines = file.split("\n"); // Splitting file by line

        Arrays.stream(lines).forEach(line -> {

            String[] elems = line.split(","); // Split line by ','

            Date date = null;
            try {
                date = formatter.parse(elems[4]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Record r = new Record(elems[0], Integer.parseInt(elems[1]), Double.parseDouble(elems[2]), Double.parseDouble(elems[3]), date, elems[5]);

            out.collect(r); // each record has : (ship_id, ship_type, cell_id, ts, trip_id)

        });
    }
}
