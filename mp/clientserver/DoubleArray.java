public class DoubleArray {
    /**
     * The array of 1000 doubles [1.0, 2.0, 3.0, ..., 1000.0].
     */
    public static double[] array;
    static {
        array = new double[1000];
        for (int i = 0; i < 1000; i++) {
            array[i] = i + 1.0d;
        }
    }
}
