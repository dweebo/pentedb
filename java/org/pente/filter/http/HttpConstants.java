package org.pente.filter.http;

/** Useful constants used by HTTP
 *  @version 0.2 02/12/2001
 *  @author dweebo
 */
public class HttpConstants {

    /** Non-instantiable constructor */
    private HttpConstants() {
    }

    /** Protocol header */
    public static final String 	HTTP = 				"http://";
    /** End line string */
    public static final String 	END_LINE = 			"\r\n";

    /** The default http web server port */
    public static final int     HTTP_PORT =         80;

    public static final int    STATUS_OK =          200;
    public static final int    STATUS_BAD_REQUEST = 400;
    public static final int    STATUS_NOT_FOUND =   404;
    public static final int    STATUS_SERVER_ERROR =500;

    /** http header specifying a cookie line */
    public static final String  GET_COOKIE =        "Cookie";
    /** http header specifying the browser to store a cookie */
    public static final String  SET_COOKIE =        "Set-Cookie";
    /** http header specifying the length of the request or response */
    public static final String  CONTENT_LENGTH =    "content-length";

    public static final String  CONTENT_LOCATION =  "content-location";

    public static final String  CONTENT_TYPE =      "content-type";

    public static final String  CONTENT_TYPE_TEXT = "text/plain";

    public static final String  CONTENT_TYPE_HTML = "text/html";

    public static final String  CONTENT_TYPE_GIF =  "image/gif";

    public static final String  CONTENT_TYPE_JS =   "application/x-javascript";
}