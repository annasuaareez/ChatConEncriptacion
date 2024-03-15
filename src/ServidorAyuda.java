import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServidorAyuda {

    // Método para manejar los mensajes de los clientes
    public static void handleClientMessage(DatagramSocket socket, DatagramPacket packet, List<ClienteInfo> clientesConectados) throws IOException {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        String message = new String(packet.getData(), 0, packet.getLength());

        if (message.equals("DESCONEXION")) {
            String nombreClienteDesconectado = obtenerNombreCliente(address, port, clientesConectados);
            eliminarCliente(address, port, clientesConectados);
            for (ClienteInfo cliente : clientesConectados) {
                enviarMensaje(socket, "¡" + nombreClienteDesconectado + " se ha desconectado del chat!", cliente.getAddress(), cliente.getPort());
            }
            // Imprimir en el servidor el mensaje de desconexión
            LocalDateTime horaConexion = LocalDateTime.now();
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");
            String horaConexionFormateada = horaConexion.format(formatoHora);
            System.out.println("Cliente " + nombreClienteDesconectado + " se ha desconectado del chat. - Hora de desconexión: " + horaConexionFormateada);
        } else {
            // Verificar si el cliente ya está registrado
            boolean isClientRegistered = false;
            for (ClienteInfo cliente : clientesConectados) {
                if (cliente.getAddress().equals(address) && cliente.getPort() == port) {
                    isClientRegistered = true;
                    break;
                }
            }

            if (!isClientRegistered) {
                // Obtener el nombre del cliente del mensaje
                String[] parts = message.split(":");
                String nombreCliente = (parts.length >= 2) ? parts[1] : "Cliente " + (clientesConectados.size() + 1);

                // Registrar un nuevo cliente con la hora de conexión
                LocalDateTime horaConexion = LocalDateTime.now();
                DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");
                String horaConexionFormateada = horaConexion.format(formatoHora);
                clientesConectados.add(new ClienteInfo(address, port, nombreCliente, horaConexionFormateada));
                System.out.println("Nuevo cliente registrado: " + nombreCliente + " - " + address.toString() + ":" + port + " - Hora de conexión: " + horaConexionFormateada);

                for (ClienteInfo cliente : clientesConectados) {
                    // Enviar mensaje de entrada al chat con el nombre del cliente y la hora de conexión
                    enviarMensaje(socket, "¡" + nombreCliente + " ha entrado al chat!", cliente.getAddress(), cliente.getPort());
                }
            } else {
                // Desencriptar el mensaje recibido antes de enviarlo a los demás clientes
                String mensajeDesencriptado = Encriptador.desencriptarMensaje(message);

                String nombreRemitente = obtenerNombreCliente(address, port, clientesConectados);

                LocalDateTime horaActual = LocalDateTime.now();
                DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");
                String horaEnvio = horaActual.format(formatoHora);

                String mensajeServidor = nombreRemitente + " (" + horaEnvio + "): " + message;

                System.out.println("Mensaje cifrado recibido de " + mensajeServidor);

                // Enviar el mensaje a todos los clientes conectados
                String mensajeConInfo = obtenerMensajeConInformacion(mensajeDesencriptado, obtenerNombreCliente(address, port, clientesConectados));
                for (ClienteInfo cliente : clientesConectados) {
                    enviarMensaje(socket, mensajeConInfo, cliente.getAddress(), cliente.getPort());
                }
            }
        }
    }

    // Método para manejar los archivos enviados por los clientes
    public static void handleFileMessage(DatagramSocket socket, DatagramPacket packet, List<ClienteInfo> clientesConectados) throws IOException {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        byte[] fileData = new byte[packet.getLength() - 1]; // Excluir el primer byte que indica que es un archivo
        System.arraycopy(packet.getData(), 1, fileData, 0, fileData.length);

        // Reenviar el archivo a todos los clientes conectados
        for (ClienteInfo cliente : clientesConectados) {
            enviarArchivo(socket, fileData, cliente.getAddress(), cliente.getPort());
        }
    }

    // Método para obtener el nombre del cliente dado su dirección y puerto
    private static String obtenerNombreCliente(InetAddress address, int port, List<ClienteInfo> clientesConectados) {
        for (ClienteInfo cliente : clientesConectados) {
            if (cliente.getAddress().equals(address) && cliente.getPort() == port) {
                return cliente.getNombre();
            }
        }
        return "Cliente Desconocido";
    }

    // Método para obtener un mensaje con información adicional (nombre del remitente y hora de envío)
    private static String obtenerMensajeConInformacion(String message, String nombreRemitente) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaEnvio = ahora.format(formato);
        return nombreRemitente + " (" + horaEnvio + "): " + message;
    }

    // Método para enviar un mensaje a un cliente específico
    public static void enviarMensaje(DatagramSocket socket, String message, InetAddress address, int port) throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
    }

    private static void eliminarCliente(InetAddress address, int port, List<ClienteInfo> clientesConectados) {
        clientesConectados.removeIf(cliente -> cliente.getAddress().equals(address) && cliente.getPort() == port);
    }

    // Método para enviar un archivo a un cliente específico
    public static void enviarArchivo(DatagramSocket socket, byte[] fileData, InetAddress address, int port) throws IOException {
        // Agregar el indicador de archivo al inicio del mensaje
        byte[] sendData = new byte[fileData.length + 1];
        sendData[0] = '#';
        System.arraycopy(fileData, 0, sendData, 1, fileData.length);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);
    }
}
