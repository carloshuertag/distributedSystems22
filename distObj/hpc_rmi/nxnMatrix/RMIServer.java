import java.rmi.Naming;

public class RMIServer {
    public static void main(String[] args) {
        String url = "rmi://localhost/distCalc";
        try {
            DistCalc distCalc = new DistCalc();
            Naming.rebind(url, distCalc); // bind the object to the url in the registry
        } catch (java.rmi.RemoteException | java.net.MalformedURLException e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }
}
