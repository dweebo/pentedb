package org.pente.gameDatabase.swing;

import java.io.*;
import javax.swing.*;

/**
 * @author dweebo
 */
public class MyProgressInputStream extends InputStream {

	private JProgressBar progress;
	private InputStream in;
	public MyProgressInputStream(JProgressBar progress, InputStream in) {
		this.progress = progress;
		this.in = in;
	}

	public int read() throws IOException {
		int r = in.read();
		updateProgress(1);
		return r;
	}
	private void updateProgress(int read) {
		if (read == -1) {
			progress.setValue(progress.getMaximum());
		}
		else {
			progress.setValue(progress.getValue() + read);
		}
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
		int l = in.read(b);
		updateProgress(l);
		return l;
	}
	public int read(byte[] b, int off, int len) throws IOException {
		int l = in.read(b, off, len);
		updateProgress(l);
		return l;
	}
	public synchronized void reset() throws IOException {
		in.reset();
	}
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
}
