import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class ServidorUDP {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;
    private static List<ClienteInfo> clientesConectados = new ArrayList<>();

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            System.out.println("Servidor en espera");

            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                if (packet.getData()[0] == '#') {
                    ServidorAyuda.handleFileMessage(socket, packet, clientesConectados);
                } else {
                    ServidorAyuda.handleClientMessage(socket, packet, clientesConectados);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
