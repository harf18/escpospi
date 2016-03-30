package net.vibrac.escpospi.bus;

public class BusFactory {

    public static BusConnexion getBus(String busType){
        switch(busType.toUpperCase()){
            case "SERIAL":
                return new BusSerial();
        }
        return null;
    }
}
