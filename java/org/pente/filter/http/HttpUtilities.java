package org.pente.filter.http;

import java.util.*;

/**
 * @author dweebo
 */
public class HttpUtilities {

    private HttpUtilities() {
    }

    /** Loads http parameters into a hashtable
     *  @param paramString The parameter string
     *  @param params The hashtable to store the parameters in
     *  @exception Exception If the parameters cannot be loaded
     */
    public static void parseParams(String paramString, Hashtable params) throws Exception {

        StringTokenizer paramTokenizer = new StringTokenizer(paramString, "&");
        while (paramTokenizer.hasMoreElements()) {
            String oneParam = paramTokenizer.nextToken();

            StringTokenizer oneParamTokenizer = new StringTokenizer(oneParam, "=");
            String name = oneParamTokenizer.nextToken();
            String value = "";
            try {
                value = decode(oneParamTokenizer.nextToken()).trim();
            } catch(NoSuchElementException ex) {
            }

            // if multiple parameters share the same name, put them in a vector
            Object exists = params.get(name);
            if (exists != null) {
                if (exists instanceof Vector) {
                    Vector vec = (Vector) exists;
                    vec.addElement(value);
                }
                else {
                    Vector vec = new Vector();
                    vec.addElement(exists);
                    vec.addElement(value);
                    params.put(name, vec);
                }
            }
            else {
                params.put(name, value);
            }
        }
    }

    /** Decodes parameter values
     *  Taken from URLDecoder class in java1.2 for java1.1 compatibility
     *  @param s The string to decode
     *  @param String The decoded string
     *  @exception If the string cannot be decoded
     */
    public static String decode(String s) throws Exception {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        sb.append((char)Integer.parseInt(
                                        s.substring(i + 1, i + 3), 16));
                    }
                    catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    }
                    i += 2;
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        // Undo conversion to external encoding
        String result = sb.toString();
        byte[] inputBytes = result.getBytes("8859_1");
        return new String(inputBytes);
    }
}