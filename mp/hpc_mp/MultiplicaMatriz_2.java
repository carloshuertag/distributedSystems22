public class MultiplicaMatriz_2 {
    public static void main(String args[]){
        int N = 12, i, j, k;
        float[][] A = new float[N][N];
        float[][] B = new float[N][N];
        float[][] C = new float[N][N];
        float x, checksum = 0;
        for (i = 0; i < N; i++) for (j = 0; j < N; j++) {
                A[i][j] = i + 3 * j;
                B[i][j] = 2 * i - j;
                C[i][j] = 0; // Initialization
            }
        for (i = 0; i < N; i++) for (j = 0; j < i; j++) {
                x = B[i][j];
                B[i][j] = B[j][i];
                B[j][i] = x; // B = B^t (transpose)
            }
        for (i = 0; i < N; i++) for (j = 0; j < N; j++) for (k = 0; k < N; k++)
                    C[i][j] += A[i][k] * B[j][k]; // C = A * B
        System.out.println("A = ");
        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) System.out.print(A[i][j] + " ");
            System.out.println();
        }
        System.out.println("B = ");
        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) System.out.print(B[j][i] + " ");
            System.out.println(); // Print B^t^t = B
        }
        System.out.println("C = ");
        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                checksum += C[i][j];
                System.out.print(C[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Checksum = " + checksum);
    }
}