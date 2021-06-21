package dati;

import org.apache.flink.streaming.connectors.nifi.NiFiDataPacket;
import queries.Query2;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Query2POJO {

    protected String ship_id;
    protected Integer ship_type;
    protected String trip_id;
    protected Double lon;
    protected Double lat;
    protected Timestamp date_time;
    protected String ore;

    public static boolean filterByHours(Query2POJO dati){

        String[] ore_minuti = dati.getOre().split(":");
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ore_minuti[0]));
        cal1.set(Calendar.MINUTE, Integer.parseInt(ore_minuti[1]));

        //String limit = "00:00";
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt("00"));
        cal2.set(Calendar.MINUTE, Integer.parseInt("00"));

        //String limit = "11:59";
        Calendar cal3 = Calendar.getInstance();
        cal3.set(Calendar.HOUR_OF_DAY, Integer.parseInt("11"));
        cal3.set(Calendar.MINUTE, Integer.parseInt("59"));

        if(cal1.after(cal2) && cal1.before(cal3)){
            return true;
        }
        return false;
    }

    public static Query2POJO parseFromValue(NiFiDataPacket value) {

        Query2POJO dati = new Query2POJO();

        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        String file = new String(value.getContent(), Charset.defaultCharset());
        String fileWithoutFirstRow = file.substring(file.indexOf("\n")+1);
        String[] lines = fileWithoutFirstRow.split("\n"); // Splitting file by line

        for (String l : lines) {
            String[] temp = l.split(",");

            try {

                Date date = formatter.parse(temp[4]);
                cal.setTime(date);
                String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);

                dati.setOre(time); //ore
                dati.setShip_id(temp[0]); //ship_id
                dati.setShip_type(Integer.parseInt(temp[1])); //shiptype
                dati.setTrip_id(temp[5]); //trip_id
                dati.setLon(Double.parseDouble(temp[2])); //lon
                dati.setLat(Double.parseDouble(temp[3])); //lat
                dati.setDate_time(new Timestamp(formatter.parse(temp[4]).getTime()));//timestamp

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dati;
    }

    public Integer getShip_type() {
        return ship_type;
    }

    public void setShip_type(Integer ship_type) {
        this.ship_type = ship_type;
    }

    public String getShip_id() {
        return ship_id;
    }

    public void setShip_id(String ship_id) {
        this.ship_id = ship_id;
    }

    public String getTrip_id() {
        return trip_id;
    }

    public void setTrip_id(String trip_id) {
        this.trip_id = trip_id;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Timestamp getDate_time() {
        return date_time;
    }

    public void setDate_time(Timestamp date_time) {
        this.date_time = date_time;
    }

    public String getOre() {
        return ore;
    }

    public void setOre(String ore) {
        this.ore = ore;
    }
}