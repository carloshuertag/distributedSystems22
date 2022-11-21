import java.rmi.RemoteException;

public class DistCalc extends java.rmi.server.UnicastRemoteObject implements RMIDistCalc {

    /**
     * Creates a new instance of DistCalc
     * @throws RemoteException if there is a problem with the remote object
     */
    public DistCalc() throws RemoteException {
        super();
    }

    /**
     * Multiply matrix nxm by matrix mxn given its transpose matrix row by row
     * @param A matrix nxm
     * @param B transpose matrix mxn
     * @param n number of rows of A
     * @param m number of columns of A
     * @return result matrix AxB nxn
     * @throws RemoteException if there is a problem with the remote object
     */
    @Override
    public double[][] matrixMult(double[][] A, double[][] B, int n, int m) throws RemoteException {
        double[][] C = new double[n][n];
        int i, j, k;
        for (i = 0; i < n; i++) for (j = 0; j < n; j++){
            C[i][j] = 0;
            for (k = 0; k < m; k++)
                    C[i][j] += A[i][k] * B[j][k]; // C = A * B
        }
        return C;
    }
    
}
