package com.virtue.socketlibrary;

public class ChangeTypeUtil {

	/**
	 * 将int值转成网络字节
	 * 
	 * @return 返回网络字节数组
	 */
	public static byte[] intToByteArray(int a) {
		return new byte[] { (byte) ((a >> 24) & 0xFF), (byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF),
				(byte) (a & 0xFF) };
	}

	/**
	 * 将byte[]转成int
	 * 
	 * @param b
	 * @return
	 */
	public static int byteArrayToInt(byte[] b) {
		// return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0]
		// & 0xFF) << 24;
		return (b[0] & 0xFF) << 24 | (b[1] & 0xFF) << 16 | (b[2] & 0xFF) << 8 | b[3] & 0xFF;

	}

	// 将指定byte数组以16进制的形式打印到控制台
	public static void printHexString(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			String ret = "";
			System.out.println(hex.toUpperCase() + " ");
		}

	}

//	public static byte[] charArrayToByteArray(char[] cChar){
//		byte[] byteData=Encoding.Default.GetBytes(cChar);
//		return byteData;
//	}
//
//	public static char[] byteArrayToCharArray(byte[] byteData){
//		char[] cChar=Encoding.ASCII.GetChars(byteData);
//		return cChar;
//	}

}
