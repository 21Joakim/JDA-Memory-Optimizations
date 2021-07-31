package com.jockie.jda.memory.utility.data;

public class DataConvertUtility {
	
	private DataConvertUtility() {}
	
	/* Not entirely sure if this is the correct way to do it, but it works */
	public static long[] hexToLong(char[] hexChars) {
		/* 64 (bits in long)/4 (bits for each hex) = 16 */
		long[] longs = new long[(int) Math.ceil(hexChars.length/16)];
		
		for(int i = 0; i < hexChars.length; i++) {
			long offset = i % 16L;
			int current = (int) i/16;
			
			long hex = Character.digit(hexChars[i], 16);
			longs[current] |= (hex << (offset * 4L));
		}
		
		return longs;
	}
	
	/* Not entirely sure if this is the correct way to do it, but it works */
	public static char[] longToHex(long[] longs) {
		/* 64 (bits in long)/4 (bits for each hex) = 16 */
		char[] hexChars = new char[longs.length * 16];
		
		for(int i = 0; i < longs.length * 16; i++) {
			long offset = i % 16L;
			int current = (int) i/16;
			
			long hex = (longs[current] >> (offset * 4L) & 0x0FL);
			hexChars[i] = Character.forDigit((int) hex, 16);
		}
		
		return hexChars;
	}
}