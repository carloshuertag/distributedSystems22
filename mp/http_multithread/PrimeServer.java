import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PrimeServer extends Thread {
    private Socket connection;

    public PrimeServer(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            System.out.println("Cliente "+connection.getRemoteSocketAddress().toString()+" conectado");
            DataInputStream input = new DataInputStream(connection.getInputStream());
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            long n = input.readLong();
            long l = input.readLong();
            long r = input.readLong();
            boolean isPrime = true;
            for(long i = l; i <= r; i++)
                if(n % i == 0 && n != i) {
                    isPrime = false;
                    break;
                }
            output.writeUTF((isPrime) ? "NO DIVIDE" : "DIVIDE");
            System.out.println("En el rango "+l+"-"+r+(isPrime?" no":" sí")+" se encontró un divisor");
            Thread.sleep(100);
        } catch (Exception e) {
            System.out.println("Se produjo una excepción en un hilo de conexión del servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                System.out.println("Se produjo una excepción al cerrar la conexión en un hilo de conexión de servidor: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("Hilo de conexión del servidor finalizado");
            }
        }
    }

    public static void main(String[] args) {
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
            if(port < 1024 || port > 65535) throw new Exception();
        } catch (Exception e) {
            System.err.println("Se produjo una excepción al leer el puerto del servidor: " + e.getMessage());
            System.out.println("Uso del programa: java PrimeServer <puerto>");
            e.printStackTrace();
            System.exit(1);
        }
        ServerSocket endpoint = null;
        try {
            endpoint = new ServerSocket(port);
            System.out.println("Servidor iniciado\n");
            for (;;) {
                Socket connection = endpoint.accept();
                PrimeServer server = new PrimeServer(connection);
                server.start();
            }            
        } catch (Exception e) {
            System.out.println("Se produjo una excepción en el servidor: "+ e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                endpoint.close();
            } catch (Exception e) {
                System.out.println("Se produjo una excepción al cerrar la conexión: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                System.out.println("\nServidor terminado.");
            }
        }
    }
}