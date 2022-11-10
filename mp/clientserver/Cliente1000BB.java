public class Cliente1000BB {
    static long startTime, totalTime;
    public static void main(String[] array) {
        System.out.println("Cliente 1000BB iniciado\n");
        java.net.Socket connection = null;
        try {
            connection = new java.net.Socket("localhost",50000);
            java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream());
            java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(1000 * Double.BYTES);
            for(int i = 0; i < 1000; i++) bb.putDouble(DoubleArray.array[i]);
            byte[] buffer = bb.array();
            startTime = System.currentTimeMillis();
            dos.write(buffer);
            totalTime = System.currentTimeMillis() - startTime;
            System.out.println("Tiempo de envio: " + totalTime + "ms");
            Thread.sleep(1000);
            connection.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                System.err.println("Exception when closing socket: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("\nCliente 1000BB terminado.");
            }
        }
    }
}
