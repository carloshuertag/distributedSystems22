import java.io.File;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ssl.SSLSocketFactory;

public class SecureFilesClient extends Thread {

    public static int PORT = 50500;

    private String fileName;

    public SecureFilesClient(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void run() {
        System.out.println("Enviando archivo " + fileName + "...");
        Socket connection = null;
        for(;;)
            try {
                SSLSocketFactory client = (SSLSocketFactory) SSLSocketFactory.getDefault();
                connection = client.createSocket("localhost", PORT);
                System.out.println("Conexión establecida con el servidor.");
                try {
                    java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream());
                    java.io.DataInputStream dis = new java.io.DataInputStream(connection.getInputStream());
                    File file = new File(fileName);
                    if(file.length() > Integer.MAX_VALUE) throw new Exception("El archivo es demasiado grande.");
                    dos.writeUTF(fileName.substring(fileName.lastIndexOf(File.separator) + 1));
                    int remaining = (int)file.length();
                    dos.writeInt(remaining);
                    java.io.FileInputStream fis = new java.io.FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while (remaining > 0) {
                        read = fis.read(buffer, 0, Math.min(buffer.length, remaining));
                        dos.write(buffer, 0, read);
                        remaining -= read;
                    }
                    fis.close();
                    String message = dis.readUTF();
                    if(!message.equals("OK")) throw new Exception("El servidor no pudo recibir el archivo.");
                    System.out.println("Archivo "+fileName+" enviado.");
                } catch (Exception e) {
                    System.err.println("Exception thrown at connection thread: " + e.getMessage());
                } finally {
                    try {
                        connection.close();
                    } catch (Exception e) {
                        System.err.println("Exception when closing socket: " + e.getMessage());
                    } finally {
                        break;
                    }
                }
            } catch (java.io.IOException e) {
                System.out.println("Error al conectar con el servidor. Intentando de nuevo en 1 segundo.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    System.err.println("Exception when sleeping: " + e1.getMessage());
                }
            } catch (java.lang.SecurityException | NullPointerException | IllegalArgumentException e) {
                System.err.println("Exception at creating stream socket: " + e.getMessage());
                break;
            } finally {
                System.out.println("\nHilo de conexión finalizado.");
            }
    }

    /**
     *  Usage:
     *  java -Djavax.net.ssl.trustStore=client_keystore.jks -Djavax.net.ssl.trustStorePassword=SSLjava SecureFilesClient <filename> <filename> ...
     *  e.g.
     *  java -Djavax.net.ssl.trustStore=client_keystore.jks -Djavax.net.ssl.trustStorePassword=SSLjava SecureFilesClient server_certificate.pem Tarea2ArchivosSeguros Ejemplo.pdf Open SSL.txt
     * @param args command line arguments, filename to be sent.
     */
    public static void main(String args[]){
        //System.setProperty("javax.net.ssl.trustStore","client_keystore.jks");
        //System.setProperty("javax.net.ssl.trustStorePassword","SSLjava");
        ArrayList <String> fileNames = new ArrayList<>();
        String fileName = "";
        File file = null;
        for(int i = 0; i < args.length; i++){
            fileName += args[i];
            file = new File(fileName);
            if(file.exists()) {
                fileNames.add(fileName);
                fileName = "";
            } else fileName += " ";
        }
        if(fileNames.isEmpty()) {
            System.out.println("No se encontró ningun archivo.");
            System.exit(0);
        } else fileNames.forEach(f -> {
            SecureFilesClient client = new SecureFilesClient(f);
            client.start();
        });
    }

}
