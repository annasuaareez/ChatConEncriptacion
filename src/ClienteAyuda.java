import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClienteAyuda {

    private static final int BUFFER_SIZE = 1024;

    // Método para enviar un mensaje a un servidor UDP
    public static void enviarMensaje(DatagramSocket socket, String message, InetAddress address, int port) throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
    }

    public static void enviarArchivo(DatagramSocket socket, File file, InetAddress address, int port) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] fileData = outputStream.toByteArray();
            byte[] sendData = new byte[fileData.length + 1];
            sendData[0] = '#'; // Indicador de archivo
            System.arraycopy(fileData, 0, sendData, 1, fileData.length);

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);
        } catch (IOException e) {
            System.out.println("Error al enviar el archivo: " + e.getMessage());
        }
    }
}
