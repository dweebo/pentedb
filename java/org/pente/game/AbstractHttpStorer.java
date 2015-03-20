package org.pente.game;

import java.io.*;
import java.net.*;
import java.util.*;

//import org.apache.log4j.*;

import org.pente.gameDatabase.*;
import org.pente.filter.http.*;

/** This class provides common methods needed by classes which plan on
 *  sending/receiving information to/from a http interface which provides access
 *  to the dsg database.  This simplifies the work that individual storers
 *  have to make in order to extend the storing capabilities to transfer over http.
 *
 *  @author dweebo
 */
public abstract class AbstractHttpStorer {

    /** Log4j logging category, private so that subclasses must define their own */
    //private static Category cat = Category.getInstance(AbstractHttpStorer.class.getName());

    /** The host where the http game server is running */
    protected String      host;

    /** The port number where the http game server is running */
    protected int         port;

    /** Any request string that is common before all game storer commands */
    protected String      context;

	protected String	  cookie;
	protected String 	  userName;
	protected String	  password;

    /** The game format used to load/store games */
    protected GameFormat  gameFormat;

    /** Create a new http game storer
     *  @param host The host to connect to
     *  @param port The port number to connect to
     *  @gameFormat The format to send/receive games
     */
    public AbstractHttpStorer(String host, int port, GameFormat gameFormat) {
        this(host, port, gameFormat, "", "", "");
    }

    /** Create a new http game storer
     *  @param host The host to connect to
     *  @param port The port number to connect to
     *  @gameFormat The format to send/receive games
     *  @startRequestStr If any additional path info is needed before commands
     */
    public AbstractHttpStorer(String host, int port, GameFormat gameFormat,
		String context, String userName, String password) {
        this.host = host;
        this.port = port;
        this.gameFormat = gameFormat;
        this.context = context;
		this.userName = userName;
		this.password = password;
    }
    public AbstractHttpStorer(String host, int port, String userName, String password) {
    	this.host = host;
    	this.port = port;
    	this.userName = userName;
    	this.password = password;
    	context = "";
    }

    /** Get a connection to the http server
     *  @return Socket A socket connection
     *  @exception Exception If the connection cannot be made
     */
    private Socket getConnection() throws Exception {
        return new Socket(host, port);
    }

    /** Creates the request to send to the http server
     *  @param params The parameters to send in a string
     *  @param request The request portion
     *  @return StringBuffer The full request
     */
    protected StringBuffer createHttpRequest(StringBuffer params, String request) {

		StringBuffer cookieBuffer = new StringBuffer(
			"Cookie: name2=").append(userName).append(", password2=").append(password);
		if (cookie != null) {
			cookieBuffer.append(", JSESSIONID=").append(cookie);
		}
		cookieBuffer.append(HttpConstants.END_LINE);

        StringBuffer requestBuffer = new StringBuffer("POST ").
                                     append(context + request).append(" HTTP/1.0").
                                     append(HttpConstants.END_LINE).

                                     // host is needed by servers with virtual hosts (eg. dsg.ebizhostingsolutions.com)
                                     append("Host: ").append(host).append(HttpConstants.END_LINE).

                                     // content-type is needed by tomcat
                                     append("Content-Type: application/x-www-form-urlencoded").append(HttpConstants.END_LINE).

                                     append(cookieBuffer).

                                     append(HttpConstants.CONTENT_LENGTH).append(": ").append(params.length()).
                                     append(HttpConstants.END_LINE).append(HttpConstants.END_LINE).
                                     append(params.toString());

        return requestBuffer;
    }

    /** Writes the request to the http server through a socket connection
     *  and reads the response into a StringBuffer which it returns.
     *  @param request The request to the server
     *  @return StringBuffer The response from the server
     *  @exception Exception If the request/response can't be sent/received
     */
    protected Socket getHttpResponseSocket(StringBuffer request) throws Exception {

        Socket socket = null;
        BufferedWriter out = null;


        try {
            // connect and get streams
            socket = getConnection();
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // send request
            char requestChars[] = new char[request.length()];
            request.getChars(0, request.length(), requestChars, 0);
            out.write(requestChars);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            if (socket != null) {
            	socket.close();
            	socket = null;
            }
            throw e;
        }

		//TODO set cookie for faster future searches

        return socket;
    }

    /** Writes the request to the http server through a socket connection
     *  and reads the response into a StringBuffer which it returns.
     *  @param request The request to the server
     *  @return StringBuffer The response from the server
     *  @exception Exception If the request/response can't be sent/received
     */
    protected StringBuffer sendHttpRequest(StringBuffer request) throws Exception {

        Socket socket = null;
        BufferedWriter out = null;
        BufferedReader in = null;
        StringBuffer response = null;

        try {
            // connect and get streams
            socket = getConnection();
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send request
            char requestChars[] = new char[request.length()];
            request.getChars(0, request.length(), requestChars, 0);
            out.write(requestChars);
            out.flush();

            response = new StringBuffer();
            // receive response
            while (true) {

                char responseChars[] = new char[1024];
                int length = in.read(responseChars);
                if (length == -1) {
                    break;
                }

                response.append(responseChars, 0, length);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

		//TODO set cookie for faster future searches

        return response;
    }

    /** This determines what the status code of the http response is from the
     *  full response.
     *  @param response The full http response
     *  @return int The http response status code
     *  @throws Exception If anything goes awry
     */
    protected int getHttpResponseCode(String response) throws Exception {

        StringTokenizer responseLineTokenizer = new StringTokenizer(response, HttpConstants.END_LINE);
        response = responseLineTokenizer.nextToken();

        StringTokenizer responseTokenizer = new StringTokenizer(response, " ");
        responseTokenizer.nextToken();
        String statusStr = responseTokenizer.nextToken();
        return Integer.parseInt(statusStr);
    }

    /** This strips any http headers from the http response.
     *  @param response The full http response
     *  @return StringBuffer The response minus the http headers
     *  @throws Exception If anything goes awry
     */
    protected StringBuffer getHttpResponse(StringBuffer responseBuf) throws Exception {

        StringBuffer response = new StringBuffer(responseBuf.toString());
        int endHeaders = response.toString().indexOf(HttpConstants.END_LINE + HttpConstants.END_LINE);

        // detect jsessionid in case it changes
        String headersStr = response.substring(0, endHeaders);
        String headers[] = headersStr.split("\r\n");
		for (int i = 1; i < headers.length; i++) {
			if (headers[i].toLowerCase().startsWith("set-cookie: jsessionid=")) {
        		int j = headers[i].indexOf(';');
        		if (j != -1) {
        			cookie = headers[i].substring(0, j);
        			System.out.println("sessionid="+cookie);
        		}
			}
		}

        response.replace(0, endHeaders + 4, "");
        return response;
    }

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
}