public class Servidor1000rD {
    static long startTime, totalTime;
    static double[] array = new double[1000];
    public static void main(String[] args) {
        java.net.ServerSocket endpoint = null;
        System.out.println("Servidor 1000rD iniciado\n");
        try {
            endpoint = new java.net.ServerSocket(50000);
            java.net.Socket connection = endpoint.accept();
            java.io.DataInputStream dis = new java.io.DataInputStream(connection.getInputStream());
            startTime = System.currentTimeMillis();
            for(int i = 0; i < 1000; i++) array[i] = dis.readDouble();
            totalTime = System.currentTimeMillis() - startTime;
            for(int i = 0; i < 1000; i++) System.out.println(array[i]);
            System.out.println("Tiempo de recepción: " + totalTime + "ms");
            try{
                connection.close();
            } catch (Exception e) {
                System.out.println("Exception when closing socket: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("\nServidor 1000rD ha finalizado la conexión.");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                endpoint.close();
            } catch (Exception e) {
                System.out.println("Exception when closing socket: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("\nServidor 1000rD terminado.");
            }
        }
    }
}
