package com.ss.testserial;

public class Startwrite {
	static {
		System.loadLibrary("TestSerial");
	}

	public native int uartInit();
	public native int uartDestroy();
	
	// 24路电路板
	public native int openGrid(int cardID, int doorID, int[] retInfo);
	
	public native int getBoardAddress(int[] retInfo);

	public native int getDoorState(int boardID, int LockID, int[] retInfo);
	
	public native int getProtocalID(int[] retInfo);

	


}
