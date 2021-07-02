package common;

import java.io.Serializable;
import java.util.Date;

public class Record implements Serializable {

    public enum Shiptype { MILITARY, PASSENGER, CARGO, OTHER }
    public enum Seatype { WEST, EAST }

    private final String id;          // ship id
    private final Shiptype shipType;  // ship type
    private final double lon;         // longitude
    private final double lat;         // latitude
    private final String cell;        // cell id
    private final Date ts;            // timestamp
    private final String trip;        // trip id
    private final Seatype seaType;     // sea type


    public Record(String id, int type, double lon, double lat, Date ts, String trip) {

        this.id = id;
        this.shipType = this.defineShipType(type);
        this.lon = lon;
        this.lat = lat;
        this.cell = this.defineCellId(lon, lat);
        this.ts = ts;
        this.trip = trip;
        this.seaType = this.defineSea(lon,lat);

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
    private Seatype defineSea(double lon, double lat){

        double latCapoFeto = 37.660618;
        double lonCapoFeto = 12.521133;

        double latCapoBon = 37.083287;
        double lonCapoBon = 11.038891;

        if((lat>latCapoBon && lon<lonCapoBon) || (lon<lonCapoFeto && lat<latCapoBon) ||
                (lon>lonCapoBon && lat<43.168196 && lat>latCapoBon && lon<16.177403) || (lat<latCapoFeto && lon<lonCapoFeto)){

            return Seatype.WEST;

        }else{

            return Seatype.EAST;
        }

    }

    // Returns a string with all the record's info
    public String toString(){

        return id + "," + shipType.name() + "," + lon + "," + lat + "," + cell + "," + ts + "," + trip + "," + seaType;
    }


    // Getters and Setters ---------------------------------------------------------------------------------------------

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

    public String getTrip() {
        return trip;
    }

    public Seatype getSeaType() {
        return seaType;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    // -----------------------------------------------------------------------------------------------------------------

}
