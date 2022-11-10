import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.InetSocketAddress;

/*
 * @author: @huerta2502
 */

public class MulticastChat extends Thread{

    final static String GRPIPADDR = "239.10.10.10";
    final static String CHARSET = "UTF-8";
    final static String PROMPT = "Mensaje: ";
    final static int PORT = 10000;

    static Object obj = new Object();
    static boolean prompt;

    /**
     * Sends a message to the multicast group.
     * @param buffer The message to send in bytes.
     * @param ipAddr The multicast group IP address.
     * @param port The multicast group port.
     * @throws IOException if an error occurs while sending the message.
     */
    static void sendMulticastMessage(byte[] buffer, String ipAddr, int port) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.send(new DatagramPacket(buffer, buffer.length,InetAddress.getByName(ipAddr), port));
        datagramSocket.close();
    }

    /**
     * Receives a message from the multicast group.
     * @param mSocket The multicast socket.
     * @param msgLength The message length.
     * @return The message received in bytes.
     * @throws IOException if an error occurs while receiving the message.
     */
    static byte[] receiveMulticastMessage(MulticastSocket mSocket, int msgLength) throws IOException {
        byte[] buffer = new byte[msgLength];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        mSocket.receive(datagramPacket);
        return datagramPacket.getData();
    }

    /**
     * Prints all the messages received from the multicast group.
     */
    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(GRPIPADDR);
            InetSocketAddress sa = new InetSocketAddress(group, PORT);
            NetworkInterface ni = NetworkInterface.getByName("em1");
            socket.joinGroup(sa, ni);
            for(;;) {
                byte[] dataReceived = receiveMulticastMessage(socket, 256);
                String received = new String(dataReceived, 0, dataReceived.length, CHARSET);
                synchronized(obj) {
                    if (prompt) System.out.print("\033[2K");
                    for(int i = 0; i < PROMPT.length(); i++) System.out.print("\b");
                    System.out.println(received);
                } 
                synchronized (obj) {
                    if (prompt) System.out.print(PROMPT);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Reads the user input and sends it to the multicast group.
     * Usage: && java MulticastChat <username>
     * @param args {@code args[0]} is the user name.
     */
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack","true");
        if (args.length != 1) {
            System.out.println("Usage: java MulticastChat <username>");
            return;
        }
        String name = args[0];
        new MulticastChat().start();
        for(;;) {
            synchronized(obj) {
                prompt = true;
            }
            System.out.print(PROMPT);
            String message = System.console().readLine();
            if(message.equals("salir")) System.exit(0);
            synchronized(obj) {
                prompt = false;
            }
            String sending = name + " :- " + message;
            byte[] data = new byte[256];
            int i;
            for(i = 0; i < sending.length(); i++){
                data[i] = (byte)sending.charAt(i);
            }
            for(; i < 256; data[i++] = 0);
            try {
                sendMulticastMessage(sending.getBytes(CHARSET), GRPIPADDR, PORT);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
