public class RMIClient extends Thread {

    final static int SHARDS = 6, THREADS = 3;

    static int n, m, shardSize;
    static double[][][] AShards, BtShards;
    static double[][][][] CShards;

    int start, end;
    RMIDistCalc remoteDistCalc;

    /**
     * Create a new thread to calculate a shard of the result matrix
     * @param start start index of the shard
     * @param end end index of the shard
     * @param remoteDistCalc remote object with the methods to be called
     */
    public RMIClient(int start, int end, RMIDistCalc remoteDistCalc) {
        this.start = start;
        this.end = end;
        this.remoteDistCalc = remoteDistCalc;
    }

    /**
     * Calculate a shard of the result matrix
     */
    @Override
    public void run() {
        int i, j;
        if(remoteDistCalc != null) {
            try {
                for (i = start; i < end; i++) for(j = 0; j < SHARDS; j++)
                        CShards[i][j] = remoteDistCalc.matrixMult(AShards[i], BtShards[j], shardSize, m);
            } catch (java.rmi.RemoteException e) {
                e.printStackTrace();
            }
        } else
            for(i = start; i < end; i++) for(j = 0; j < SHARDS; j++)
                    CShards[i][j] = Calc.matrixMult(AShards[i], BtShards[j], shardSize, m);
    }

    /**
     * Multiply matrix nxm by matrix mxn using RMI in a distributed environment
     * @param args the command line arguments
     * Usage: java RMIClient <n> <m> <host1> <host2>
     */
    public static void main(String[] args) {
        try {
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
            int i, j;
            double[][] A = new double[n][m], B = new double[m][n];
            for (i = 0; i < n; i++) for (j = 0; j < m; j++) A[i][j] = 3 * i + 2 * j;
            for (i = 0; i < m; i++) for (j = 0; j < n; j++) B[i][j] = 2 * i - 3 * j;
            boolean display;
            if((display = n == 6 && m == 5)){
                printMatrix(A, "A");
                printMatrix(B, "B");
            }
            double[][] Bt = Calc.Transpose(B);
            shardSize = n / SHARDS;
            AShards = new double[SHARDS][shardSize][m];
            BtShards = new double[SHARDS][shardSize][m];
            for(i = 0; i < SHARDS; i++) {
                AShards[i] = Calc.Group(A, i * shardSize, m, shardSize);
                BtShards[i] = Calc.Group(Bt, i * shardSize, m, shardSize);
            }
            CShards = new double[SHARDS][SHARDS][shardSize][shardSize];
            String[] urls = {"rmi://"+args[2]+"/distCalc", "rmi://"+args[3]+"/distCalc"};
            try {
                RMIDistCalc[] remoteDistCalc = new RMIDistCalc[2];
                for(i = 0; i < urls.length; i++) remoteDistCalc[i] = (RMIDistCalc) java.rmi.Naming.lookup(urls[i]);
                RMIClient[] clients = new RMIClient[3];
                int threadRows = SHARDS / THREADS;
                for(i = 0; i < clients.length; i++) {
                    if(i == 0) clients[0] = new RMIClient(0, threadRows, null);
                    else clients[i] = new RMIClient(i * threadRows, (i + 1) * threadRows, remoteDistCalc[i - 1]);
                }
                for (i = 0; i < clients.length; i++) clients[i].start();
                try {
                    for (i = 0; i < clients.length; i++) clients[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double[][] C = new double[n][n];
                for(i = 0; i < SHARDS; i++) for(j = 0; j < SHARDS; j++)
                        Calc.Regroup(C, CShards[i][j], i * shardSize, j * shardSize, shardSize);
                double checksum = 0.0d;
                for(i = 0; i < n; i++) for(j = 0; j < n; j++)
                        checksum += C[i][j];
                if (display) printMatrix(C, "C");
                System.out.println("Checksum = " + checksum);
            } catch (java.rmi.RemoteException e) {
                System.err.println("Exception: " + e.getMessage());
            } catch (java.rmi.NotBoundException | java.net.MalformedURLException e) {
                System.err.println("Exception: " + e.getMessage());
                System.err.println("Make sure that the server is running with distCalc name bound.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Usage: java RMIClient <n> <m> <host1> <host2>");
            System.exit(1);
        }
    }

    /**
     * Prints a matrix
     * @param M matrix to be printed
     * @param name name of the matrix
     */
    static void printMatrix(double[][] M, String name) {
        int n = M.length, m = M[0].length;
        System.out.println(name + " = ");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) System.out.print(M[i][j] + "\t");
            System.out.println();
        }
    }
}
