package common;

import org.apache.flink.api.common.functions.FilterFunction;

public class FilterRecord implements FilterFunction<Record> {

    @Override
    public boolean filter(Record record) {

        double lat = record.getLat();
        double lon = record.getLon();

        return lat >= 32.0 && lat <= 45.0 && lon >= -6.0 && lon <= 37.0;
    }
}
