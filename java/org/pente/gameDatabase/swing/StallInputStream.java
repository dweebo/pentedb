package org.pente.gameDatabase.swing;

import java.io.*;

/**
 * @author dweebo
 */
public class StallInputStream extends InputStream {

	private InputStream in;
	public void setInputStream(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		return in.read();
	}
	public int available() throws IOException {
		if (in == null) return 0;
		else return in.available();
	}
	public void close() throws IOException {
		in.close();
	}
	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}
	public boolean markSupported() {
		return in.markSupported();
	}
	public int read(byte[] b) throws IOException {
		return in.read(b);
	}
	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}
	public synchronized void reset() throws IOException {
		in.reset();
	}
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
}
