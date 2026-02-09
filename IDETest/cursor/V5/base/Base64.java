public class Base64
{
    public static final boolean ENCODE = true;
    public static final boolean DECODE = false;
    private static final int B = 76;
    private static final byte F = 61;
    private static final byte G = 10;
    private static final byte[] E;
    private static final byte[] C;
    private static final byte H = -9;
    private static final byte A = -5;
    private static final byte D = -1;
    
    private Base64() {
    }
    
    private static byte[] C(final byte[] array) {
        return B(array, 3);
    }
    
    private static byte[] B(final byte[] array, final int n) {
        final byte[] array2 = new byte[4];
        B(array, 0, n, array2, 0);
        return array2;
    }
    
    private static byte[] B(final byte[] array, final int n, final int n2, final byte[] array2, final int n3) {
        final int n4 = ((n2 > 0) ? (array[n] << 24 >>> 8) : 0) | ((n2 > 1) ? (array[n + 1] << 24 >>> 16) : 0) | ((n2 > 2) ? (array[n + 2] << 24 >>> 24) : 0);
        switch (n2) {
            case 3: {
                array2[n3] = Base64.E[n4 >>> 18];
                array2[n3 + 1] = Base64.E[n4 >>> 12 & 0x3F];
                array2[n3 + 2] = Base64.E[n4 >>> 6 & 0x3F];
                array2[n3 + 3] = Base64.E[n4 & 0x3F];
                return array2;
            }
            case 2: {
                array2[n3] = Base64.E[n4 >>> 18];
                array2[n3 + 1] = Base64.E[n4 >>> 12 & 0x3F];
                array2[n3 + 2] = Base64.E[n4 >>> 6 & 0x3F];
                array2[n3 + 3] = 61;
                return array2;
            }
            case 1: {
                array2[n3] = Base64.E[n4 >>> 18];
                array2[n3 + 1] = Base64.E[n4 >>> 12 & 0x3F];
                array2[n3 + 3] = (array2[n3 + 2] = 61);
                return array2;
            }
            default: {
                return array2;
            }
        }
    }
    public static boolean writeFile(final byte[] array, final String pathname, final boolean b) {
        return writeFile(array, 0, array.length, new File(pathname), b);
    }
    
    public static boolean writeFile(final byte[] array, final File file, final boolean b) {
        return writeFile(array, 0, array.length, file, b);
    }
    
    public static boolean writeFile(final byte[] array, final int n, final int n2, final File file, final boolean b) {
        OutputStream outputStream = null;
        boolean b2 = false;
        try {
            outputStream = new OutputStream(new BufferedOutputStream(new FileOutputStream(file)), b);
            outputStream.write(array, n, n2);
            b2 = true;
        }
        catch (IOException ex) {
            b2 = false;
        }
        finally {
            try {
                outputStream.close();
            }
            catch (Exception ex2) {}
        }
        return b2;
    }
    
    
    public static Object decodeToObject(final String s) {
        final byte[] decode = decode(s);
        java.io.InputStream in = null;
        ObjectInputStream objectInputStream = null;
        try {
            in = new java.io.InputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(file)));
            objectInputStream = new ObjectInputStream(in);
            return objectInputStream.readObject();
        }
        catch (Exception ex) {
            return null;
        }
        finally {
            try {
                objectInputStream.close();
            }
            catch (Exception ex2) {}
        }
        return null;
    }
    
}