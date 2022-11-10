public class Servidor {
    public static void main (String[] args) {
        System.out.println("Servidor iniciado\n");
        java.net.ServerSocket endpoint = null;
        try {
            endpoint = new java.net.ServerSocket(50000);
            java.net.Socket connection = endpoint.accept();
            java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream());
            java.io.DataInputStream dis = new java.io.DataInputStream(connection.getInputStream());
            System.out.println(dis.readInt());
            System.out.println(dis.readDouble());
            byte[] buffer = new byte[4];
            NetworkReader.read(dis, buffer,0,4);
            System.out.println(new String(buffer,"UTF-8"));
            dos.write("HOLA".getBytes());
            byte[] bbuffer = new byte[5 * Double.BYTES];
            NetworkReader.read(dis,bbuffer,0,5 * Double.BYTES);
            java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(bbuffer);
            java.util.stream.IntStream.range(0,5).forEach(i -> System.out.println(b.getDouble()));
            connection.close();
            try{
                connection.close();
            } catch (Exception e) {
                System.err.println("Exception when closing socket: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("\nServidor ha finalizado la conexión.");
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
            }
            finally {
                System.out.println("\nServidor terminado.");
            }
        }
    }
}
