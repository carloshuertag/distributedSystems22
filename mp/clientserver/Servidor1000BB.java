public class Servidor1000BB {
    static long startTime, totalTime;
    public static void main(String[] args) {
        java.net.ServerSocket endpoint = null;
        System.out.println("Servidor 1000BB iniciado\n");
        try {
            endpoint = new java.net.ServerSocket(50000);
            java.net.Socket connection = endpoint.accept();
            java.io.DataInputStream dis = new java.io.DataInputStream(connection.getInputStream());
            byte[] buffer = new byte[1000 * Double.BYTES];
            startTime = System.currentTimeMillis();
            NetworkReader.read(dis,buffer,0,1000 * Double.BYTES);
            totalTime = System.currentTimeMillis() - startTime;
            java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(buffer);
            java.util.stream.IntStream.range(0,1000).forEach(i -> System.out.println(b.getDouble()));
            System.out.println("Tiempo de recepción: " + totalTime + "ms");
            try{
                connection.close();
            } catch (Exception e) {
                System.err.println("Exception when closing socket: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("\nServidor 1000BB ha finalizado la conexión.");
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
                System.out.println("\nServidor 1000BB terminado.");
            }
        }
    }
}
