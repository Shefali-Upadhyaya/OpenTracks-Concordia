package de.dennisguse.opentracks.sensors;

public class UintUtils {

    public static final int UINT16_MAX = 0xFFFF;
    public static final long UINT32_MAX = 0xFFFFFFFFL;

    private UintUtils() {
    }

    /**
     * Computes a - b for UINT with overflow (b < a).
     *
     * @return diff
     */
    public static long diff(long a, long b, final long UINT_MAX) {
        if (a < 0 || b < 0) {
            throw new ValueOutOfRangeException("a or b cannot be less than zero.");
        }
        if (a > UINT_MAX || b > UINT_MAX) {
            throw new ValueOutOfRangeException("a or b are outside of the allowed range.");
        }

        if (a >= b) {
            return a - b;
        }

        return (UINT_MAX - b) + a;
    }
}
class ValueOutOfRangeException extends RuntimeException{
    public ValueOutOfRangeException(String message){
        super(message);
    }
}
