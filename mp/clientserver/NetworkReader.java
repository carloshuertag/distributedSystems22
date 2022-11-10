public class NetworkReader {
    /**
     * Reads a message from the network.
     * @param dis the network input stream.
     * @param buffer the buffer to read the message into.
     * @param read the number of bytes read.
     * @param remaining the number of bytes remaining to be read.
     * @throws java.io.IOException if DataInputStream.read() throws an IOException.
     */
    static void read(java.io.DataInputStream dis,byte[] buffer,int read,int remaining) throws java.io.IOException {
            int count;
            while (remaining > 0) {
                count = dis.read(buffer,read,remaining);
                read += count;
                remaining -= count;
            }
        }
}
