package pojo;

import java.io.Serializable;
import java.util.Date;

public class Record implements Serializable {

    public enum Shiptype { MILITARY, PASSENGER, CARGO, OTHER }

    private String id;      // ship id
    private Shiptype type;  // ship type
    private String cell;    // cell id
    private Date ts;      // timestamp
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

        return "da_fare";
    }

    // Returns a string with all the record's info
    public String toString(){

        return id + "," + type.name() + "," + cell + "," + ts + "," + trip;

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

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public String getTrip() {
        return trip;
    }

    public void setTrip(String trip) {
        this.trip = trip;
    }
}
