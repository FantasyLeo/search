package edu.index.core;

/**
 * 字节缓存
 * @author fantasy
 *
 */
public class ByteBuffer {
	private byte[] buf;
	private int length;
	private int step;

	/**
	 * 
	 * @param cap 初始字节数
	 */
	public ByteBuffer(int cap) {
		buf = new byte[cap];
		length = 0;
		step = 10240;
	}

	/**
	 * 增加字节
	 * @param arr
	 * @param offset
	 * @param len
	 */
	public void add(byte[] arr,int offset ,int len) {

		if(buf.length < this.length + len) {
			byte[] temp = buf;
			buf = new byte[this.length + len + step];
			System.arraycopy(temp, 0, buf, 0, this.length );
		}

		System.arraycopy(arr, offset, buf, this.length, len);
		this.length = this.length + len;
	}

	public byte[] arrayByte() {
		return this.buf;
	}
}
