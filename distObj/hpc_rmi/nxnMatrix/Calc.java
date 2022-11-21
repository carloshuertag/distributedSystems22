public class Calc{

    static int[][] separa_matriz(int[][] A,int inicio,int N) {
        int[][] M = new int[N/2][N];
        for (int i = 0; i < N/2; i++)
            for (int j = 0; j < N; j++)
                M[i][j] = A[i + inicio][j];
        return M;
    }

    static void acomoda_matriz(int[][] C,int[][] A,int renglon,int columna,int N) {
        for (int i = 0; i < N/2; i++)
            for (int j = 0; j < N/2; j++)
                C[i+renglon][j+columna] = A[i][j];
    }
}
