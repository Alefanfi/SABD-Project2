package common;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Gauge;

public class MetricsMapper extends RichMapFunction<String, String> {

    private transient double throughput = 0;
    private transient double latency = 0;

    private transient long counter = 0;
    private transient double start;


    @Override
    public void open(Configuration config) {
        getRuntimeContext().getMetricGroup()
                .gauge("throughput", (Gauge<Double>) () -> this.throughput);

        getRuntimeContext().getMetricGroup()
                .gauge("latency", (Gauge<Double>) () -> this.latency);

        this.start = System.currentTimeMillis();

    }

    @Override
    public String map(String value) {

        this.counter++;

        // Compute throughput and latency
        double elapsed_millis = System.currentTimeMillis() - this.start;
        double elapsed_sec = elapsed_millis/1000;

        this.throughput = this.counter/elapsed_sec;
        this.latency = elapsed_millis/this.counter;

        return value;
    }
}
