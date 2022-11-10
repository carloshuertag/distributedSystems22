public class Cliente {
    public static void main (String[] args) {
        System.out.println("Cliente iniciado\n");
        java.net.Socket connection = null;
        for(;;)
            try {
                connection = new java.net.Socket("localhost",50000);
                System.out.println("Conexión establecida con el servidor.");
                try {
                    java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream());
                    java.io.DataInputStream dis = new java.io.DataInputStream(connection.getInputStream());
                    dos.writeInt(123);
                    dos.writeDouble(1234567890.1234567890);
                    dos.write("hola".getBytes());
                    byte[] buffer = new byte[4];
                    NetworkReader.read(dis, buffer,0,4);
                    System.out.println(new String(buffer,"UTF-8"));
                    java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(5 * Double.BYTES);
                    bb.putDouble(1.1);
                    bb.putDouble(1.2);
                    bb.putDouble(1.3);
                    bb.putDouble(1.4);
                    bb.putDouble(1.5);
                    byte[] bbuffer = bb.array();
                    dos.write(bbuffer);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Exception when closing socket: " + e.getMessage());
                } finally {
                    try {
                        connection.close();
                    } catch (Exception e) {
                        System.err.println("Exception when closing socket: " + e.getMessage());
                    }
                    finally {
                        System.out.println("\nCliente terminado.");
                    }
                    break;
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
            }
    }
}
