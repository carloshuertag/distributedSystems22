import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;

public class SecureFilesServer extends Thread {

    public static int PORT = 50500;

    private Socket connection;

    public SecureFilesServer(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        System.out.println("Hilo de conexión con el cliente iniciado.");
        try {
            java.io.DataInputStream dis = new java.io.DataInputStream(connection.getInputStream());
            String fileName = dis.readUTF();
            int fileSize = dis.readInt();
            String message = "OK";            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
            byte[] buffer = new byte[1024];
            int read = 0;
            int remaining = fileSize;
            while (remaining > 0) {
                read = dis.read(buffer, 0, Math.min(buffer.length, remaining));
                fos.write(buffer, 0, read);
                remaining -= read;
            }
            fos.close();
            java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream());
            if(new java.io.File(fileName).length() != fileSize) message = "No se recibió el archivo completo.";
            dos.writeUTF(message);
        } catch (Exception e) {
            System.err.println("Exception thrown at connection thread: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                System.err.println("Exception when closing socket: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("Hilo de conexión con el cliente terminado.");
            }
        }
    }

    /**
     *  Usage:
     *  java -Djavax.net.ssl.keyStore=server_keystore.jks -Djavax.net.ssl.keyStorePassword=SSLjava SecureFilesServer
     * @param args command line arguments, not used.
     */
    public static void main(String args[]){
        //System.setProperty("javax.net.ssl.trustStore","server_keystore.jks");
        //System.setProperty("javax.net.ssl.trustStorePassword","SSLjava");
        System.out.println("Servidor iniciado\n");
        ServerSocket endpoint = null;
        try {
            SSLServerSocketFactory server = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            endpoint = server.createServerSocket(PORT);
            for (;;) {
                Socket connection = endpoint.accept();
                SecureFilesServer filesServer = new SecureFilesServer(connection);
                filesServer.start();
            } 
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                endpoint.close();
            } catch (Exception e) {
                System.err.println("Exception when closing socket: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("\nServidor terminado.");
            }
        }
    }

}
