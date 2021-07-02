package pojo;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.nifi.NiFiDataPacket;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Record implements Serializable {

    public enum Shiptype { MILITARY, PASSENGER, CARGO, OTHER }

    private String id;          // ship id
    private Shiptype shipType;  // ship type
    private double lon;         // longitude
    private double lat;         // latitude
    private String cell;        // cell id
    private Date ts;            // timestamp
    private String trip;        // trip id
    private String seaType;     // sea type


    private String ore;     //ore
    private long timestampDate;
    private Tuple2<String, String> info; //cell_id, typeSea

    public Record(String id, int type, double lon, double lat, Date ts, String trip) {

        this.id = id;
        this.shipType = this.defineShipType(type);
        this.lon = lon;
        this.lat = lat;
        this.cell = this.defineCellId(lon, lat);
        this.ts = ts;
        this.trip = trip;
        this.seaType = defineSea(lon,lat);
        this.ore = this.getHoursByDate(ts);
        this.info = new Tuple2<>(this.defineCellId(lon, lat), this.defineSea(lon, lat));

    }

    public Record(String ore, String id, int type, double lon, double lat, Date ts, String trip) {

        this.ore = ore;
        this.id = id;
        this.shipType = this.defineShipType(type);
        this.cell = this.defineCellId(lon, lat);
        this.ts = ts;
        this.trip = trip;
        this.seaType = this.defineSea(lon, lat);
        this.timestampDate = ts.getTime();
        this.info = new Tuple2<>(this.defineCellId(lon, lat), this.defineSea(lon, lat));
    }

    public Record(){}

    /*
    Returns the ship's type:
            MILITARY = 35
            PASSENGER = 60-69
            CARGO = 70-79
            OTHER
     */
    private Shiptype defineShipType(int type){

        if(type == 35){
            return Shiptype.MILITARY;
        }
        else if(type >= 60 && type <= 69){
            return Shiptype.PASSENGER;
        }
        else if(type >= 70 && type <= 79){
            return Shiptype.CARGO;
        }
        else{
            return Shiptype.OTHER;
        }
    }

    // Returns the cell's id calculated using longitude and latitude
    private String defineCellId(double lon, double lat){

        double minlat = 32.0;
        double maxlat = 45.0;
        double minlon = -6.0;
        double maxlon = 37.0;

        double latbin = (maxlat - minlat)/10;
        double lonbin = (maxlon - minlon)/40;

        char cell_x = (char)('A' + ((int) Math.ceil((lat - minlat)/latbin)) -1);
        int cell_y = (int) Math.ceil((lon - minlon)/lonbin);

        return "" + cell_x + cell_y;
    }

    // Returns the sea type, either western or eastern mediterranean
    private String defineSea(double lon, double lat){

        double latCapoFeto = 37.660618;
        double lonCapoFeto = 12.521133;

        double latCapoBon = 37.083287;
        double lonCapoBon = 11.038891;

        if((lat>latCapoBon && lon<lonCapoBon) || (lon<lonCapoFeto && lat<latCapoBon) ||
                (lon>lonCapoBon && lat<43.168196 && lat>latCapoBon && lon<16.177403) || (lat<latCapoFeto && lon<lonCapoFeto)){

            return "Occidentale";

        }else{

            return "Orientale";
        }

    }

    // Returns a string with all the record's info
    public String toString(){

        return id + "," + shipType.name() + "," + cell + "," + ts + "," + trip + "," + seaType;

    }

    private String getHoursByDate(Date ts){

        Calendar cal = Calendar.getInstance();

        cal.setTime(ts);
        String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);

        return time;
    }

    public static Record parseFromValue(NiFiDataPacket value) {

        Record record = null;

        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        String file = new String(value.getContent(), Charset.defaultCharset());
        String fileWithoutFirstRow = file.substring(file.indexOf("\n")+1);
        String[] lines = fileWithoutFirstRow.split("\n"); // Splitting file by line

        for (String l : lines) {
            String[] temp = l.split(",");

            double lon = Double.parseDouble(temp[2]);
            double lat = Double.parseDouble(temp[3]);

            try {

                Date date = formatter.parse(temp[4]);
                cal.setTime(date);
                String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);

                record = new Record(time, temp[0], Integer.parseInt(temp[1]), lon, lat, date, temp[5]);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return record;
    }

    public static String valueToString(Record record){
        return record.getOre() + "," + record.getId() + "," + record.getShipType() + "," + record.getTrip() + "," + record.getTs() + "," +
                record.getTimestampDate() + "," + record.getCell() + "," + record.getLon() + "," + record.getLat() +  "-----------------" + record.getSeaType();
    }

    //Getters and Setters ----------------------------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public Shiptype getShipType() {
        return shipType;
    }

    public String getCell() {
        return cell;
    }

    public Date getTs() {
        return ts;
    }

    public String getTrip() { return trip; }

    public String getOre() { return ore; }

    public String getSeaType() { return seaType; }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public long getTimestampDate() { return timestampDate; }

    public Tuple2<String, String> getInfo() {
        return info;
    }

}
