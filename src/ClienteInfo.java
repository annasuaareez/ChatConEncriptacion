import java.net.InetAddress;

public class ClienteInfo {
    private InetAddress address;
    private int port;
    private String nombre;
    private String horaConexion;

    public ClienteInfo(InetAddress address, int port, String nombre, String horaConexion) {
        this.address = address;
        this.port = port;
        this.nombre = nombre;
        this.horaConexion = horaConexion;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return "ClienteInfo{" +
                "address=" + address +
                ", port=" + port +
                ", nombre='" + nombre + '\'' +
                ", horaConexion='" + horaConexion + '\'' +
                '}';
    }
}
