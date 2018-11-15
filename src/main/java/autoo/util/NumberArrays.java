package autoo.util;

import java.util.Collection;

public final class NumberArrays {
    private NumberArrays() {
    }
    
    public static <T extends Number> long[] toLongArray(Collection<T> objs) {
        long[] result = new long[objs.size()];
        int i = 0;
        for (T n : objs) {
            result[i++] = n.longValue();
        }
        return result;
    }
    
    public static <T extends Number> int[] toIntArray(Collection<T> objs) {
        int[] result = new int[objs.size()];
        int i = 0;
        for (T n : objs) {
            result[i++] = n.intValue();
        }
        return result;
    }
    
    public static <T extends Number> short[] toShortArray(Collection<T> objs) {
        short[] result = new short[objs.size()];
        int i = 0;
        for (T n : objs) {
            result[i++] = n.shortValue();
        }
        return result;
    }
    
    public static <T extends Number> double[] toDoubleArray(Collection<T> objs) {
        double[] result = new double[objs.size()];
        int i = 0;
        for (T n : objs) {
            result[i++] = n.doubleValue();
        }
        return result;
    }

    public static <T extends Number> float[] toFloatArray(Collection<T> objs) {
        float[] result = new float[objs.size()];
        int i = 0;
        for (T n : objs) {
            result[i++] = n.floatValue();
        }
        return result;
    }


}
