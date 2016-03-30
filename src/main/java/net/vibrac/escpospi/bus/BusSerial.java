package net.vibrac.escpospi.bus;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class BusSerial implements BusConnexion {

    private final Serial printer = SerialFactory.createInstance();

    @Override
    public void open(){
        this.open(Serial.DEFAULT_COM_PORT, 9600);
    }

    @Override
    public void open(String address, int baudRate){
        printer.open(address, baudRate);
    }

    @Override
    public void write(byte[] command){
        for (byte b : command){
            printer.write(b);
        }
    }

    @Override
    public void close(){
        printer.close();
    }
}
