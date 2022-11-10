import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HTTPServer extends Thread {

    static int primeServerPorts[] = {0,0,0,0};

    static class Client extends Thread{

        static boolean isPrime = true;
        static Object lock = new Object();

        String host;
        int port;
        long n, l, r;

        /**
         * Client constructor
         * @param host Host of the prime server
         * @param port Port of the prime server
         * @param n Number to check
         * @param l Left limit
         * @param r Right limit
         */
        Client(String host, int port, long n, long l, long r){
            this.host = host;
            this.port = port;
            this.n = n;
            this.l = l;
            this.r = r;
        }

        @Override
        public void run() {
            System.out.println("Cliente iniciado");
            Socket connection = null;
            for(;;)
                try {
                    connection = new Socket(host,port);
                    System.out.println("Conexión establecida con el servidor.");
                    try {
                        java.io.DataInputStream dis = new java.io.DataInputStream(connection.getInputStream());
                        java.io.DataOutputStream dos = new java.io.DataOutputStream(connection.getOutputStream());
                        dos.writeLong(n);
                        dos.writeLong(l);
                        dos.writeLong(r);
                        String result = dis.readUTF();
                        synchronized (lock) {
                            if(result.equals("DIVIDE")) isPrime = false;
                        }
                        System.out.println("En el rango 2-"+r+" el número "+n+(isPrime?" ":" no")+" es primo");
                    } catch (Exception e) {
                        System.out.println("Se produjo una excepción al leer el resultado del servidor: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        try {
                            connection.close();
                        } catch (Exception e) {
                            System.out.println("Se produjo una excepción al cerrar la conexión con el servidor: " + e.getMessage());
                            e.printStackTrace();
                        }
                        finally {
                            System.out.println("Cliente terminado.");
                        }
                        break;
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Error al conectar con el servidor. Intentando de nuevo en 1 segundo.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        System.err.println("Exception when sleeping: " + e1.getMessage());
                        e1.printStackTrace();
                    }
                } catch (java.lang.SecurityException | NullPointerException | IllegalArgumentException e) {
                    System.err.println("Exception at creating stream socket: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
        }
    }

    public static short PORT = 80;
    public static String HOST = "localhost";
    private Socket connection;

    public HTTPServer(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            System.out.println("Cliente "+connection.getRemoteSocketAddress().toString()+" conectado");
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter output = new PrintWriter(connection.getOutputStream(), false);
            String requestHeader = input.readLine();
            if (requestHeader.startsWith("GET")) {
                StringTokenizer getTokenizer = new StringTokenizer(requestHeader, "?");
                String path = getTokenizer.nextToken().substring(4);
                if(path.startsWith("/primo")){
                    if(getTokenizer.hasMoreTokens()){
                        String params = getTokenizer.nextToken();
                        params = params.substring(0, params.indexOf(" "));
                        String answer;
                        StringTokenizer paramsTokenizer = new StringTokenizer(params, "&");
                        long n = 0;
                        try {
                            n = Long.parseLong(paramsTokenizer.nextToken().substring("numero".length() + 1));
                            if(n < 2) throw new Exception();
                            long halfn = (n >> 1) + 1, r = 1, l;
                            Client[] clients = new Client[4];
                            Client.isPrime = true;
                            for(int i = 1; i <= 4; i++){
                                l = (r != 0) ? r + 1L : 2L;
                                r = (halfn * i) >> 2;
                                clients[i - 1] = new Client("localhost", primeServerPorts[i-1], n, l, r);
                                clients[i - 1].start();
                            }
                            for(int i = 0; i < 4; clients[i++].join());
                            System.out.println("Para el número "+n+" el resultado es: ");
                            System.out.println(Client.isPrime?"ES PRIMO\n":"NO ES PRIMO\n");
                            answer = (Client.isPrime) ? "ES PRIMO" : "NO ES PRIMO";
                        } catch (Exception e) {
                            answer = "ENTRADA INVÁLIDA";
                        }
                        StringBuilder htmlBuilder = new StringBuilder();
                        htmlBuilder.append("<html><head><title>HTTP Server</title></head><body>");
                        htmlBuilder.append("<p>").append(answer).append("</p>");
                        htmlBuilder.append("</body></html>");
                        String response = htmlBuilder.toString();
                        output.println("HTTP/1.1 200 OK");
                        output.println("Server: ServidorHTTP.java");
                        output.println("Date: " + new Date());
                        output.println("Content-type: text/html; charset=utf-8");
                        output.println("Content-length: " + response.length());
                        output.println();
                        output.flush();
                        output.println(response);
                        output.flush();
                    } else HTTPClientErrorResponse(output, 400);
                } else HTTPClientErrorResponse(output, 404);
            } else { // Pending HEAD Method
                HTTPClientErrorResponse(output, 405);
            }
            System.out.println(requestHeader);
            for (;;) {
                requestHeader = input.readLine();
                System.out.println(requestHeader);
                if (requestHeader.equals("")) break;
            }
        } catch (Exception e) {
            System.out.println("Se produjo una excepción en un hilo de conexión del servidor HTTP: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                System.out.println("Se produjo una excepción al cerrar la conexión en un hilo de conexión de servidor HTTP: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("Hilo de conexión del servidor HTTP finalizado");
            }
        }
    }

    private void HTTPClientErrorResponse(PrintWriter output, int code){
        String firstLine = "HTTP/1.1 ";
        switch(code){
            case 404:
                firstLine += "404 Not Found";
                break;
            case 405:
                firstLine += "405 Method Not Allowed";
                break;
            default:
                firstLine += "400 Bad Request";
                break;
        }
        output.println(firstLine);
        output.println("Server: ServidorHTTP.java");
        output.println("Date: " + new Date());
        output.println("Content-type: text/html; charset=utf-8");
        output.println("Content-length: 0");
        output.println();
        output.flush();
    }

    public static void main(String[] args) {
        try {
            primeServerPorts[0] = Integer.parseInt(args[0]);
            primeServerPorts[1] = Integer.parseInt(args[1]);
            primeServerPorts[2] = Integer.parseInt(args[2]);
            primeServerPorts[3] = Integer.parseInt(args[3]);
            for(int i = 0; i < 4; i++)
                if(primeServerPorts[i] < 1024 || primeServerPorts[i] > 65535) throw new Exception();
        } catch (Exception e) {
            System.out.println("Se produjo una excepción al leer algún puerto de los servidores: " + e.getMessage());
            System.out.println("Uso del programa: java HTTPServer <puerto1> <puerto2> <puerto3> <puerto4>");
            e.printStackTrace();
            System.exit(1);
        }
        ServerSocket endpoint = null;
        try {
            endpoint = new ServerSocket(PORT);
            System.out.println("Servidor HTTP iniciado");
            for (;;) {
                Socket connection = endpoint.accept();
                HTTPServer server = new HTTPServer(connection);
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
                System.out.println("Servidor HTTP terminado.");
            }
        }
    }
}