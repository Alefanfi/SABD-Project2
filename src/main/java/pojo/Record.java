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

    private String id;      // ship id
    private Shiptype type;  // ship type
    private String cell;    // cell id
    private Date ts;      // timestamp
    private String trip;    // trip id
    private String ore;     //ore

    private String typeSea; //tipo di mare

    private double lon;
    private double lat;

    private long timestampDate;

    private Tuple2<String, String> info;

    public Record(String id, int type, double lon, double lat, Date ts, String trip) {

        this.id = id;
        this.type = this.defineShipType(type);
        this.cell = this.defineCellId(lon, lat);
        this.ts = ts;
        this.trip = trip;
        this.typeSea = defineSea(lon,lat);

    }

    public Record(String ore, String id, int type, double lon, double lat, Date ts, String trip) {

        this.ore = ore;
        this.id = id;
        this.type = this.defineShipType(type);
        this.lon = lon;
        this.lat = lat;
        this.cell = this.defineCellId(lon, lat);
        this.ts = ts;
        this.trip = trip;
        this.typeSea = this.defineSea(lon, lat);
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

        return id + "," + type.name() + "," + cell + "," + ts + "," + trip + "," + typeSea;

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

    /*public static boolean filterByHours(Record record, String limit1, String limit2){

        String[] ore_minuti = record.getOre().split(":");
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ore_minuti[0]));
        cal1.set(Calendar.MINUTE, Integer.parseInt(ore_minuti[1]));

        String[] ore = limit1.split(":");
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ore[0]));
        cal2.set(Calendar.MINUTE, Integer.parseInt(ore[1]));

        String[] ore2 = limit2.split(":");
        Calendar cal3 = Calendar.getInstance();
        cal3.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ore2[0]));
        cal3.set(Calendar.MINUTE, Integer.parseInt(ore2[1]));

        if(cal1.after(cal2) && cal1.before(cal3)){
            return true;
        }
        return false;
    }*/

    public static String valueToString(Record record){
        return record.getOre() + "," + record.getId() + "," + record.getType() + "," + record.getTrip() + "," + record.getTs() + "," +
                record.getTimestampDate() + "," + record.getCell() + "," + record.getLon() + "," + record.getLat() +  "-----------------" + record.getTypeSea();
    }

    public int getHourInDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ts);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }


    //Getters and Setters ----------------------------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Shiptype getType() {
        return type;
    }

    public void setType(Shiptype type) {
        this.type = type;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) { this.ts = ts; }

    public String getTrip() { return trip; }

    public void setTrip(String trip) { this.trip = trip; }

    public String getOre() { return ore; }

    public void setOre(String ore) { this.ore = ore; }

    public String getTypeSea() { return typeSea; }

    public void setTypeSea(String typeSea) { this.typeSea = typeSea; }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public long getTimestampDate() { return timestampDate; }

    public void setTimestampDate(long timestampDate) { this.timestampDate = timestampDate; }

    public Tuple2<String, String> getInfo() {
        return info;
    }

    public void setInfo(Tuple2<String, String> info) {
        this.info = info;
    }

}
