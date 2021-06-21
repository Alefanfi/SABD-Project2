package dati;

import java.io.Serializable;

public class Record implements Serializable {

    public enum Shiptype { MILITARY, PASSENGER, CARGO, OTHER }

    private String ship_id;
    private Shiptype ship_type;
    private String cell_id;
    private String ts;
    private String trip_id;

    public Record(String ship_id, int ship_type, double lon, double lat, String ts, String trip_id) {

        this.ship_id = ship_id;
        this.ship_type = this.define_ship_type(ship_type);
        this.cell_id = this.define_cell_id(lon, lat);
        this.ts = ts;
        this.trip_id = trip_id;

    }

    /*
    Returns the ship's type:
            MILITARY = 35
            PASSENGER = 60-69
            CARGO = 70-79
            OTHER
     */
    private Shiptype define_ship_type(int ship_type){

        if(ship_type == 35){
            return Shiptype.MILITARY;
        }
        else if(ship_type >= 60 && ship_type <= 69){
            return Shiptype.PASSENGER;
        }
        else if(ship_type >= 70 && ship_type <= 79){
            return Shiptype.CARGO;
        }
        else{
            return Shiptype.OTHER;
        }
    }

    private String define_cell_id(double lon, double lat){

        return "da_fare";
    }



}
