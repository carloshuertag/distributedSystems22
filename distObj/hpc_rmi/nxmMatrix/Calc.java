public class Calc {

    /**
     * Transpose matrix
     * @param M matrix
     * @return transpose matrix
     */
    static double[][] Transpose(double[][] M){
        int n = M.length, m = M[0].length;
        double[][] MT = new double[m][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) MT[j][i] = M[i][j];
        return MT;
    }

    /**
     * Group matrix into shards
     * @param M matrix
     * @param offs offset
     * @param cols number of columns
     * @param shardSize shard size
     * @return shard of matrix M from offs to offs + shardSize
     */
    static double[][] Group(double[][] M, int offs, int cols, int shardSize) {
        double[][] Shard = new double[shardSize][cols];
        for (int i = 0; i < shardSize; i++)
            for (int j = 0; j < cols; j++)
                Shard[i][j] = M[i + offs][j];
        return Shard;
    }

    /**
     * Multiply matrix nxm by matrix mxn given its transpose matrix row by row
     * @param A matrix nxm
     * @param B transpose matrix mxn
     * @param n number of rows of A
     * @param m number of columns of A
     * @return result matrix AxB nxn
     */
    static double[][] matrixMult(double[][] A, double[][] B, int n, int m) {
        double[][] C = new double[n][n];
        int i, j, k;
        for (i = 0; i < n; i++) for (j = 0; j < n; j++){
            C[i][j] = 0;
            for (k = 0; k < m; k++)
                    C[i][j] += A[i][k] * B[j][k]; // C = A * B
        }
        return C;
    }

    /**
     * Regroups matrix from shards
     * @param C matrix to be regrouped
     * @param AB shard matrix
     * @param row start row in C
     * @param col start column in C
     * @param shardSize shard size
     */
    static void Regroup(double[][] C, double[][] AB, int row, int col, int shardSize) {
        int r;
        for (int i = 0; i < shardSize; i++){
            r = i + row;
            for (int j = 0; j < shardSize; j++)
                C[r][j + col] = AB[i][j];
        }
    }
}
