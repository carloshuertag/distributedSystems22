import java.rmi.Naming;

public class RMIClient {

    public static void main(String[] args) {
        int N = 12, i, j, k, x;
        int[][] A = new int[N][N];
        int[][] B = new int[N][N];
        int[][] C = new int[N][N];
        for (i = 0; i < N; i++) for (j = 0; j < N; j++) {
            A[i][j] = i + 3 * j; // Initialize A
            B[i][j] = 2 * i - j; // Initialize B
        }
        for (i = 0; i < N; i++) for (j = 0; j < i; j++) {
            x = B[i][j];
            B[i][j] = B[j][i];
            B[j][i] = x; // B = B^t (transpose)
        }
        int[][] A1 = Calc.separa_matriz(A,0,N);
        int[][] A2 = Calc.separa_matriz(A,N/2,N);
        int[][] B1 = Calc.separa_matriz(B,0,N);
        int[][] B2 = Calc.separa_matriz(B,N/2,N);
        String url = "rmi://localhost/distCalc";
        try {
            RMIDistCalc rmiDistCalc = (RMIDistCalc) Naming.lookup(url);
            int[][] C1 = rmiDistCalc.multiplica_matrices(A1,B1,N);
            int[][] C2 = rmiDistCalc.multiplica_matrices(A1,B2,N);
            int[][] C3 = rmiDistCalc.multiplica_matrices(A2,B1,N);
            int[][] C4 = rmiDistCalc.multiplica_matrices(A2,B2,N);
            Calc.acomoda_matriz(C,C1,0,0,N);
            Calc.acomoda_matriz(C,C2,0,N/2,N);
            Calc.acomoda_matriz(C,C3,N/2,0,N);
            Calc.acomoda_matriz(C,C4,N/2,N/2,N);
            System.out.println("A = ");
            for (i = 0; i < N; i++) {
                for (j = 0; j < N; j++) System.out.print(A[i][j] + " ");
                System.out.println();
            }
            System.out.println("B = ");
            for (i = 0; i < N; i++) for (j = 0; j < i; j++) {
                x = B[i][j];
                B[i][j] = B[j][i];
                B[j][i] = x; // B = B^t^t
            }
            for (i = 0; i < N; i++) {
                for (j = 0; j < N; j++) System.out.print(B[i][j] + " ");
                System.out.println(); // B
            }
            System.out.println("C = ");
            long checksum = 0L;
            for (i = 0; i < N; i++) {
                for (j = 0; j < N; j++) {
                    checksum += C[i][j];
                    System.out.print(C[i][j] + " ");
                }
                System.out.println();
            }
            System.out.println("Checksum = " + checksum);
        } catch (java.rmi.NotBoundException | java.net.MalformedURLException | java.rmi.RemoteException e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }
}
