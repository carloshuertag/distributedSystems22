public class MatrixMxNMul {
    public static void main(String[] args) {
        int n = 6000, m = 5000, i, j, k, x;
        double[][] A = new double[n][m];
        double[][] B = new double[m][n];
        for (i = 0; i < n; i++) for (j = 0; j < m; j++) A[i][j] = (double)(3 * i + 2 * j);
        for (i = 0; i < m; i++) for (j = 0; j < n; j++) B[i][j] = (double)(2 * i - 3 * j); // Initialize B
        /*System.out.println("A = ");
        for (i = 0; i < n; i++) {
            for (j = 0; j < m; j++) System.out.print(A[i][j] + " ");
            System.out.println();
        }
        System.out.println("B = ");
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) System.out.print(B[i][j] + " ");
            System.out.println();
        }*/
        double[][] Bt = new double[n][m];
        for (i = 0; i < m; i++) for (j = 0; j < n; j++) {
            Bt[j][i] = B[i][j]; // Bt = B^t (transpose)
        }
        /*System.out.println("B^t = ");
        for (i = 0; i < n; i++) {
            for (j = 0; j < m; j++) System.out.print(Bt[i][j] + " ");
            System.out.println();
        }*/
        double[][] C = new double[n][n];
        for (i = 0; i < n; i++) for (j = 0; j < n; j++) for (k = 0; k < m; k++)
                    C[i][j] += A[i][k] * Bt[j][k]; // C = A * B
        /*System.out.println("C = ");
        for (i = 0; i < n; i++) {
            for (j = 0; j < m; j++) System.out.print(C[i][j] + " ");
            System.out.println();
        } */
        double checksum = 0.0d;
        for(i = 0; i < n; i++) for(j = 0; j < n; j++)
                checksum += C[i][j];
        System.out.println("Checksum = " + checksum);
    }
}
