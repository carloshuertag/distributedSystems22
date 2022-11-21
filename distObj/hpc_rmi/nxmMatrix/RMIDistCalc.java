public interface RMIDistCalc extends java.rmi.Remote{
    public double[][] matrixMult(double[][] A, double[][] B, int n, int m) throws java.rmi.RemoteException;
}
