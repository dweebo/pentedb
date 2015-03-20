package org.pente.gameDatabase.swing;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.Component;

import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;

import org.pente.filter.http.HttpConstants;
import org.pente.game.AbstractHttpStorer;

/**
 * @author dweebo
 */
public class PlunkHttpLoader extends AbstractHttpStorer {

	public PlunkHttpLoader(String host, int port, String userName, String password) {
    	super(host, port, userName, password);
    }

	public int remoteLogin(StringBuffer encryptedPasswordBuf, StringBuffer sessionIdBuf) throws Exception {

    	Socket s = null;
    	int r = 500;
    	try  {
    		StringBuffer requestBuffer = createHttpRequest(new StringBuffer(), "/remotelogin");

    		s = getHttpResponseSocket(requestBuffer);
            InputStream in = s.getInputStream();
	        int status = getStatus(in);
	        r = status;
            String cookie = getHeader("set-cookie: password2=", in);
            String sessionId = getHeader("set-cookie: jsessionid=", in);
	        if (status == HttpConstants.STATUS_OK && cookie != null) {
	        	//System.out.println("cookie="+cookie);
	        	int i = cookie.indexOf(';');
	        	if (i != -1) {
	        		encryptedPasswordBuf.append(cookie.substring(0, i));
	        	}
	        	if (sessionId != null) {
	        		i = sessionId.indexOf(';');
	        		if (i != -1) {
	        			sessionIdBuf.append(sessionId.substring(0, i));
	        		}
	        	}
	        }
	        else {
	        	StringBuffer resp = new StringBuffer();
	        	getHttpResponse(resp);
	        	System.err.println("Bad login response: " + resp);
	        }

    	} catch (Exception e) {
    		if (s != null) {
    			s.close();
    		}
    		throw e;
    	}

    	return r;
	}

	public Vector loadVenueData(Component parent) throws Exception {

		headers = null;
		Socket s = null;
		ProgressMonitor progress = null;
		Vector venues = null;

		try
		{
			StallInputStream stallInput = new StallInputStream();

			ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(
					parent, "Loading filter data from pente.org", stallInput);
			progress = pmis.getProgressMonitor();
            progress.setNote("Connecting to pente.org...");
			progress.setMillisToDecideToPopup(1);
            progress.setMillisToPopup(1);
            progress.setMaximum(15000);

			StringBuffer paramsBuffer = new StringBuffer();
	        StringBuffer requestBuffer = createHttpRequest(paramsBuffer, "/venues");

	        int len = 0;

        	s = getHttpResponseSocket(requestBuffer);
        	progress.setProgress(2000);
            progress.setNote("Downloading from pente.org...");

            // read past the http headers to the data
            InputStream in = s.getInputStream();
            String contentLength = getHeader("content-length:", in);
            if (contentLength != null) {
            	len = Integer.parseInt(contentLength);
            }
//            for (int i = 0; i < len; i++) {
//            	int b = in.read();
//            	if (b == -1) {
//            		System.out.println("error");
//            	}
//            }

        	progress.setMaximum(len);
        	stallInput.setInputStream(in);

        	ObjectInputStream objectIn = new ObjectInputStream(pmis);
			venues = (Vector) objectIn.readObject();

        } catch (Exception e) {
        	if (s != null) {
        		s.close();
            	s = null;
        	}
			if (!(e instanceof InterruptedException) && !(progress == null || !progress.isCanceled())) {
				throw e;
			}
        }

        return venues;
	}

	private int getStatus(InputStream in) throws IOException {
        StringBuffer lineBuf = new StringBuffer();
        int l=0;
        while (true) {
        	char c = (char) in.read();
        	lineBuf.append(c);
        	if (l == 0 && c == '\r') l++;
        	else if (l == 1 && c == '\n') break;
        	else l = 0;
        }
	    StringTokenizer responseTokenizer = new StringTokenizer(lineBuf.toString(), " ");
	    responseTokenizer.nextToken();
	    String statusStr = responseTokenizer.nextToken();
	    return Integer.parseInt(statusStr);
	}

	private String headers[] = null;
	private String getHeader(String headerName, InputStream in) throws IOException {

        if (headers == null) {
            int l=0;
	        StringBuffer headerBuf = new StringBuffer();
	        while (true) {
	        	char c = (char) in.read();
	        	headerBuf.append(c);
	        	if (l == 0 && c == '\r') l++;
	        	else if (l == 1 && c == '\n') l++;
	        	else if (l == 2 && c == '\r') l++;
	        	else if (l == 3 && c == '\n') break;
	        	else l = 0;
	        }
	        headers = headerBuf.toString().split("\r\n");
        }

    	for (int i = 1; i < headers.length; i++) {

    		if (headers[i].toLowerCase().startsWith(headerName)) {
    			return headers[i].substring(headerName.length(),
    				headers[i].length()).trim();
    		}
    	}
    	return null;
	}
}
