package com.proto.utils;

/**
 * Computation of int hash codes for fields of different types
 * @author wfrancis
 */
public class HashCodeUtils {
	
	private HashCodeUtils(){
		//ENSURES USE AS A LIBRARY
	}

	/**
	 * The value to use as the first "previousHashCode" in a hashCode method.
	 */
	public static final int SEED = 17;
	private static final int OFFSET = 37;
	
	/**
	 * @return b ? 0 : 1;
	 */
	public static int getHashCode(int previousHashCode, boolean b){
		int raw = b ? 0 : 1;
		return OFFSET * previousHashCode + raw;
	}

	/**
	 * @return (int)b;
	 */
	public static int getHashCode(int previousHashCode, byte b){
		int raw = b;
		return OFFSET * previousHashCode + raw;
	}

	/**
	 * @return (int)c;
	 */
	public static int getHashCode(int previousHashCode, char c){
		int raw = c;
		return OFFSET * previousHashCode + raw;
	}

	/**
	 * @return (int)s;
	 */
	public static int getHashCode(int previousHashCode, short s){
		int raw = s;
		return OFFSET * previousHashCode + raw;
	}

	/**
	 * @return i;
	 */
	public static int getHashCode(int previousHashCode, int i){
		int raw = i;
		return OFFSET * previousHashCode + raw;
	}

	/**
	 * @return (int)(l^(l>>>32));
	 */
	public static int getHashCode(int previousHashCode, long l){
		int raw = (int)(l^(l>>>32));
		return OFFSET * previousHashCode + raw;
	}

	/**
	 * @return Float.floatToIntBits(f);
	 */
	public static int getHashCode(int previousHashCode, float f){
		int raw = Float.floatToIntBits(f);
		return OFFSET * previousHashCode + raw;
	}

	/**
	 * @return getHashCode(Double.doubleToLongBits(d));
	 */
	public static int getHashCode(int previousHashCode, double d){
		return getHashCode(previousHashCode, Double.doubleToLongBits(d));
	}

	/**
	 * NOTE: If the field is an object refecrence and the class's equals method
	 * compares the field by recursively invoking equals, then use this method.
	 * Otherwise, please read Effective Java page 38.
	 * 
	 * @return o == null ? 0 : o.hashCode();
	 */
	public static int getHashCode(int previousHashCode, Object o){
		int raw = o == null ? 0 : o.hashCode();
		return OFFSET * previousHashCode + raw;
	}
	
	
	//TODO: replace all of these with <T> versions of the same thing using T... arguments??
	
	
	
	public static int getHashCode(int previousHashCode, Object[] o) {
		if (o == null) {
			return OFFSET * previousHashCode + 0;
		}

		int code = previousHashCode;
		for (int i = 0; i < o.length; i++) {
			code = getHashCode(code, o[i]);
		}
		
		return code;
	}
	
	public static int getHashCode(int previousHashCode, int[] o) {
		if (o == null) {
			return OFFSET * previousHashCode + 0;
		}

		int code = previousHashCode;
		for (int i = 0; i < o.length; i++) {
			code = getHashCode(code, o[i]);
		}
		
		return code;
	}
	
	public static int getHashCode(int previousHashCode, long[] o) {
		if (o == null) {
			return OFFSET * previousHashCode + 0;
		}

		int code = previousHashCode;
		for (int i = 0; i < o.length; i++) {
			code = getHashCode(code, o[i]);
		}
		
		return code;
	}
	
}
