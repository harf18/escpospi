package net.vibrac.escpospi.bus;

public interface BusConnexion {

    void open(String address, int baudRate);

    void open();

    void write(byte[] command);

    void close();
}
