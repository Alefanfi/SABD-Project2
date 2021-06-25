package pojo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Record implements Serializable {

    public enum Shiptype { MILITARY, PASSENGER, CARGO, OTHER }

    private String id;      // ship id
    private Shiptype type;  // ship type
    private String cell;    // cell id
    private Date ts;        // timestamp
    private String trip;    // trip id


    public Record(String id, int type, double lon, double lat, Date ts, String trip) {

        this.id = id;
        this.type = this.defineShipType(type);
        this.cell = this.defineCellId(lon, lat);
        this.ts = ts;
        this.trip = trip;

    }

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

    // Returns a string with all the record's info
    public String toString(){

        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        return id + "," + type.name() + "," + cell + "," + formatter.format(ts) + "," + trip;

    }

// Getters and Setters -------------------------------------------------------------------------------------------------

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

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public String getTrip() {
        return trip;
    }

    public void setTrip(String trip) {
        this.trip = trip;
    }

// ---------------------------------------------------------------------------------------------------------------------

}
