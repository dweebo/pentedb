package org.pente.game;

import java.net.*;
import java.util.*;
import java.text.*;
import java.io.*;

import org.pente.filter.http.HttpUtilities;

/** Interface to to wrap another format into an http safe format that
 *  can be transferred without having to know anything about the actual format
 *  as long as it implements the ObjectFormat interface.
 *
 *  @author dweebo
 */
public class HttpObjectFormat implements ObjectFormat {

    /** http param name for the value that holds the name of the base format */
    public static final String FORMAT_NAME = "format_name";

    /** http param name for the value that holds the data */
    public static final String FORMAT_DATA = "format_data";

    /** The underlying format class to wrap */
    private ObjectFormat        baseFormat;

    /** Use this constructor if you don't need to call parse(), just format().
     *  @param baseFormat The ObjectFormat to wrap in format()
     */
    public HttpObjectFormat(ObjectFormat baseFormat) {
        this.baseFormat = baseFormat;
    }

    /** Format the data into a format that can be transferred via http
     *  @param data The data object to format
     *  @param buffer The buffer to place the formatted data into
     *  @return StringBuffer Same as passed in
     */
    public StringBuffer format(Object data, StringBuffer buffer) {

        formatName(buffer);
        buffer.append("&");
        formatData(data, buffer);

        return buffer;
    }

    /** Format the name of the base format class
     *  @param buffer The buffer to place the formatted data into
     *  @return StringBuffer Same as passed in
     */
    public StringBuffer formatName(StringBuffer buffer) {

        buffer.append(FORMAT_NAME);
        buffer.append("=");
        buffer.append(baseFormat.getClass().getName());

        return buffer;
    }

    /** Format the data
     *  @param data The data object to format
     *  @param buffer The buffer to place the formatted data into
     *  @return StringBuffer Same as passed in
     */
    public StringBuffer formatData(Object data, StringBuffer buffer) {

        buffer.append(FORMAT_DATA);
        buffer.append("=");
        StringBuffer baseBuffer = new StringBuffer();
        baseBuffer = baseFormat.format(data, baseBuffer);
        try {
            buffer.append(URLEncoder.encode(baseBuffer.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }

        return buffer;
    }

	public Object parse(Object obj, StringBuffer buffer) throws ParseException {
		return null;
	}
}