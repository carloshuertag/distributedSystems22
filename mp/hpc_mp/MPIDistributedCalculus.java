import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class MPIDistributedCalculus extends Thread {

    String host, portStr;
    float[][] m1, m2;
    int size, m1Start, m1End;
    float[][] result;

    /**
     * Create a new thread to calculate a portion of the result matrix.
     * @param host host to connect to
     * @param portStr port to connect to
     * @param m1 matrix 1
     * @param m2 matrix 2
     * @param size size of the matrices
     * @param m1Start start of the first matrix
     * @param m1End end of the first matrix
     */
    public MPIDistributedCalculus(String host, String portStr, float[][] m1, float[][] m2,
                                    int size, int m1Start, int m1End) {
        this.host = host;
        this.portStr = portStr;
        this.m1 = m1;
        this.m2 = m2;
        this.size = size;
        this.m1Start = m1Start;
        this.m1End = m1End;
    }

    static class NetworkReader {

        /**
         * Reads a message from the network.
         * @param dis the network input stream.
         * @param buffer the buffer to read the message into.
         * @param read the number of bytes read.
         * @param remaining the number of bytes remaining to be read.
         * @throws java.io.IOException if DataInputStream.read() throws an IOException.
         */
        static void read(java.io.DataInputStream dis,byte[] buffer,int read,int remaining)
            throws java.io.IOException {
                int count;
                while (remaining > 0) {
                    count = dis.read(buffer,read,remaining);
                    read += count;
                    remaining -= count;
                }
            }
    }

    static class ServerNode extends Thread {

        private Socket connection;

        /**
         * Creates a new ServerNode.
         * @param connection the socket to use for the connection.
         */
        public ServerNode(Socket connection) {
            this.connection = connection;
        }

        /**
         * Reads a message from the network.
         */
        @Override
        public void run() {
            System.out.println("Client connection thread init.");
            try {
                DataInputStream dis = new DataInputStream(connection.getInputStream());
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                int size = dis.readInt(), m1Bytes = (size >> 1) * size * Float.BYTES, i, j, k;
                byte[] buffer = new byte[m1Bytes];
                NetworkReader.read(dis, buffer, 0, m1Bytes);
                ByteBuffer bb = ByteBuffer.wrap(buffer);
                float[][] m1 = new float[size >> 1][size];
                for(i = 0; i < size >> 1; i++) for(j = 0; j < size; j++) m1[i][j] = bb.getFloat();
                System.out.println("M1 received.");
                bb.clear();
                int m2Bytes = size * size * Float.BYTES;
                buffer = new byte[m2Bytes];
                NetworkReader.read(dis, buffer, 0, m2Bytes);
                bb = ByteBuffer.wrap(buffer);
                float[][] m2 = new float[size][size];
                for(i = 0; i < size; i++) for(j = 0; j < size; j++) m2[i][j] = bb.getFloat();
                System.out.println("M2 received.");
                bb.clear();
                float result[][] = new float[size >> 1][size];
                for(i = 0; i < size >> 1; i++) for(j = 0; j < size; j++) {
                        result[i][j] = 0;
                        for(k = 0; k < size; k++) result[i][j] += m1[i][k] * m2[j][k];
                    }
                System.out.println("Result calculated.");
                bb = ByteBuffer.allocate(m1Bytes);
                for(i = 0; i < size >> 1; i++) for(j = 0; j < size; j++) bb.putFloat(result[i][j]);
                dos.write(bb.array());
                bb.clear();
                System.out.println("Result sent.");
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
                    System.out.println("Client connection thread now ended.");
                }
            }
        }

        /**
         * Starts the server.
         * @param port the port to listen on.
         */
        static void server(int port) {
            ServerSocket endpoint = null;
            try {
                SSLServerSocketFactory server = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                endpoint = server.createServerSocket(port);
                for (;;) {
                    Socket connection = endpoint.accept();
                    ServerNode serverNode = new ServerNode(connection);
                    serverNode.start();
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
                    System.out.println("Server down.");
                }
            }
        }
    }

    /**
     *  Sends m1[m1Start] to m1[m1End] and m2 to host:portStr and gets the result.
     */
    @Override
    public void run() {
        result = new float[size >> 1][size];
        System.out.println("Connecting to " + host + ":" + portStr);
        for(;;)
            try {
                int port = Integer.parseInt(portStr);
                SSLSocketFactory client = (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket connection = client.createSocket(host, port);
                try {
                    int i, j;
                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                    DataInputStream dis = new DataInputStream(connection.getInputStream());
                    int m1Bytes = (size >> 1) * size * Float.BYTES;
                    ByteBuffer bb = ByteBuffer.allocate(m1Bytes);
                    dos.writeInt(size); // Send n
                    for(i = m1Start; i < m1End; i++) for(j = 0; j < size; j++) bb.putFloat(m1[i][j]);
                    dos.write(bb.array()); // Send A[0], A[1], ..., A[n/2 - 1]
                    bb.clear();
                    bb = java.nio.ByteBuffer.allocate(size * size * Float.BYTES);
                    for(i = 0; i < size; i++) for(j = 0; j < size; j++) bb.putFloat(m2[i][j]);
                    dos.write(bb.array()); // Send B
                    bb.clear();
                    byte[] buffer = new byte[m1Bytes];
                    NetworkReader.read(dis, buffer, 0, m1Bytes);
                    bb = ByteBuffer.wrap(buffer);
                    for(i = 0; i < size >> 1; i++) for(j = 0; j < size; j++) result[i][j] = bb.getFloat();
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
                System.out.println("Error when connecting to server. Trying again in 1 second.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    System.err.println("Exception when sleeping: " + e1.getMessage());
                }
            } catch (java.lang.SecurityException | NullPointerException | IllegalArgumentException e) {
                System.err.println("Exception at creating stream socket: " + e.getMessage());
                break;
            } finally {
                System.out.println("Connection ended.");
            }
    }

    /**
     *  Usage: java -Djavax.net.ssl.trustStore=xxxxx.jks -Djavax.net.ssl.trustStorePassword=*****
     *         MPIDistributedCalculus <node> <n> ?<node 1 ipaddr> ?<node 1 port> ?<node 2 ipaddr> ?<node 2 port>
     *  e.g. java -Djavax.net.ssl.trustStore=client_keystore.jks -Djavax.net.ssl.trustStorePassword=SSLjava
     *       MPIDistributedCalculus 0 <n> <node 1 ipaddr> <node 1 port> <node 2 ipaddr> <node 2 port>
     *  e.g. java -Djavax.net.ssl.keyStore=server_keystore.jks -Djavax.net.ssl.keyStorePassword=SSLjava
     *       MPIDistributedCalculus 1 <port>
     *  e.g. java -Djavax.net.ssl.keyStore=server_keystore.jks -Djavax.net.ssl.keyStorePassword=SSLjava
     *       MPIDistributedCalculus 2 <port>
     * @param args 0: node, 1: matrix size / port, ?2: node 1 ipaddr, ?3: node 1 port, ?4: node 2 ipaddr, ?5: node 2 port
     */
    public static void main(String args[])  {
        short node = 0;
        try {
            node = Short.parseShort(args[0]);
            switch(node) {
                case 0:
                    short n = Short.parseShort(args[1]);
                    short i, j;
                    float[][] A = new float[n][n];
                    float[][] B = new float[n][n];
                    float x;
                    for (i = 0; i < n; i++) for (j = 0; j < n; j++) {
                            A[i][j] = i + 3 * j; // Initialize A
                            B[i][j] = 2 * i - j; // Initialize B
                        }
                    System.out.println("Init A & B");
                    for (i = 0; i < n; i++) for (j = 0; j < i; j++) {
                            x = B[i][j];
                            B[i][j] = B[j][i];
                            B[j][i] = x; // B = B^t (transpose)
                        }
                    System.out.println("Transpose B");
                    MPIDistributedCalculus distCalc1 = new MPIDistributedCalculus(args[2], args[3], A, B, n, 0, n >> 1);
                    MPIDistributedCalculus distCalc2 = new MPIDistributedCalculus(args[4], args[5], A, B, n, n >> 1, n);
                    System.out.println("Send A[0] to A[n/2 - 1] and B to node 1");
                    distCalc1.start();
                    System.out.println("Send A[n/2] to A[n - 1] and B to node 2");
                    distCalc2.start();
                    try {
                        distCalc1.join(); //C1 = distributedCalculus(args[2], args[3], A, B, n, 0, n >> 1);
                        System.out.println("Receive C[0] to C[n/2 - 1] from node 1");
                        distCalc2.join(); //C2 = distributedCalculus(args[4], args[5], A, B, n, n >> 1, n);
                        System.out.println("Receive C[n/2] to C[n - 1] from node 2");
                        float[][] C1 = distCalc1.result;
                        float[][] C2 = distCalc2.result;
                        float checksum = 0;
                        for (i = 0; i < n >> 1; i++) for (j = 0; j < n; j++) checksum += C1[i][j];
                        for (i = 0; i < n >> 1; i++) for (j = 0; j < n; j++) checksum += C2[i][j];
                        System.out.println("Checksum: " + checksum);
                        if(n <= 12) {
                            System.out.println("A = ");
                            for (i = 0; i < n; i++) {
                                for (j = 0; j < n; j++) System.out.print(A[i][j] + " ");
                                System.out.println();
                            }
                            System.out.println("B = ");
                            for (i = 0; i < n; i++) {
                                for (j = 0; j < n; j++) System.out.print(B[j][i] + " ");
                                System.out.println(); // Print B^t^t = B
                            }
                            System.out.println("C = ");
                            for (i = 0; i < n >> 1; i++) {
                                for (j = 0; j < n; j++) System.out.print(C1[i][j] + " ");
                                System.out.println();
                            }
                            for (i = 0; i < n >> 1; i++) {
                                for (j = 0; j < n; j++) System.out.print(C2[i][j] + " ");
                                System.out.println();
                            }
                        }
                    } catch (InterruptedException e) {
                        System.err.println("Exception when joining threads: " + e.getMessage());
                    }
                    break;
                case 1: case 2:
                    int port;
                    try {
                        port =  Integer.parseInt(args[1]);
                        ServerNode.server(port);
                    } catch (Exception e) {
                        System.err.println("Usage: java -Djavax.net.ssl.trustStore=client_keystore.jks -Djavax.net.ssl.trustStorePassword=SSLjava MPIClient <node> <port>");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Node not supported");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Usage: java -Djavax.net.ssl.trustStore=client_keystore.jks -Djavax.net.ssl.trustStorePassword=SSLjava MPIClient <node> <n/port> ?<node 1 ipaddr> ?<node 1 port> ?<node 2 ipaddr> ?<node 2 port>");
            System.exit(1);
        }
    }
}
