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

        Date date = new Date(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long start = calendar.getTimeInMillis();

        calendar.add(Calendar.MONTH,1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        long end = calendar.getTimeInMillis();

        return Collections.singletonList(new TimeWindow(start, end));

    }
}
