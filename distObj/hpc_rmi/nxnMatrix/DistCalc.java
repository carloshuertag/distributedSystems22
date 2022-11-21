import java.rmi.RemoteException;

public class DistCalc extends java.rmi.server.UnicastRemoteObject implements RMIDistCalc {
    public DistCalc() throws RemoteException {
        super();
    }

    @Override
    public int[][] multiplica_matrices(int[][] A,int[][] B,int N) throws RemoteException{
        int[][] C = new int[N/2][N/2];
        for (int i = 0; i < N/2; i++)
            for (int j = 0; j < N/2; j++) {
                C[i][j] = 0;
                for (int k = 0; k < N; k++)
                    C[i][j] += A[i][k] * B[j][k];
            }
        return C;
    }
}
