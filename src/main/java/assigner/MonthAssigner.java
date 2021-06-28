package assigner;

import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.WindowStagger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class MonthAssigner extends TumblingEventTimeWindows{


    public MonthAssigner() {
        super(1,0, WindowStagger.ALIGNED);
    }

    @Override
    public Collection<TimeWindow> assignWindows(Object o, long timestamp, WindowAssignerContext context){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timestamp));

        long start = calendar.getTimeInMillis();

        calendar.add(Calendar.MONTH,1);

        long end = calendar.getTimeInMillis();

        return Collections.singletonList(new TimeWindow(start, end));

    }
}
