package com.maxus.tsp.common.util;

public class CRC2Util {

	public static String getCRC(String s) {
		int[] e = new int[s.length() / 2];

		for (int i = 0; i < s.length(); i += 2) {

			String t = s.substring(i, i + 2);

			e[i / 2] = Integer.parseInt(t, 16);
		}

		int crc = e[0];
		for (int i = 1; i < e.length; i++) {

			crc ^= (int) e[i];
		}

		return Integer.toHexString(crc);
	}

	/*
	 * public static int getCRC(byte data[]) { int crc = data[0]; for (int i =
	 * 1; i < data.length; i++) {
	 * 
	 * crc ^= (int )data[i] ; }
	 * 
	 * return crc; }
	 */

}
// xUint16 checksum = data[0];
// x_forloop_do(xUint16 i = 1, i < length, i++, checksum ^= data[i]);

//