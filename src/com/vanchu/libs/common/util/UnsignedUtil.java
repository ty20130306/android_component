package com.vanchu.libs.common.util;

public class UnsignedUtil {

	public static long toUnsignedInt(int value) {
		return value & 0x0FFFFFFFF;
	}
	
	public static int toUnsignedShort(short value) {
		return value & 0x0FFFF;
	}
	
	public static int toUnsignedByte(byte value) {
		return value & 0x0FF;
	}
}
