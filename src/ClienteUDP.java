import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClienteUDP {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            Scanner scanner = new Scanner(System.in);
            System.out.print("Ingrese su nombre: ");
            String nombreCliente = scanner.nextLine();
            ClienteAyuda.enviarMensaje(socket, "REGISTRO:" + nombreCliente, serverAddress, SERVER_PORT);
            Thread recibirMensajes = new Thread(() -> {
                try {
                    while (true) {
                        byte[] receiveBuffer = new byte[BUFFER_SIZE];
                        DatagramPacket packetRecibido = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(packetRecibido);

                        String mensajeRecibido = new String(packetRecibido.getData(), 0, packetRecibido.getLength());
                        System.out.println(mensajeRecibido);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
            recibirMensajes.start();
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("salir")) {
                    // Enviar mensaje de desconexión
                    ClienteAyuda.enviarMensaje(socket, "DESCONEXION", serverAddress, SERVER_PORT);
                    socket.close();
                } else if (input.equalsIgnoreCase("enviar archivo")) {
                    String filePath = input.substring("enviar archivo".length()).trim();
                    File file = new File(filePath);
                    if (file.exists()) {
                        ClienteAyuda.enviarArchivo(socket, file, serverAddress, SERVER_PORT);
                    } else {
                        System.out.println("El archivo especificado no existe.");
                    }
                } else {
                    // Encriptar el mensaje antes de enviarlo
                    String mensajeEncriptado = Encriptador.encriptarMensaje(input);
                    ClienteAyuda.enviarMensaje(socket, mensajeEncriptado, serverAddress, SERVER_PORT);
                }
            }
        } catch (IOException e) {
            System.out.println("Error en el cliente: " + e.getMessage());
        }
    }
}
