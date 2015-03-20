package org.pente.gameDatabase;

import org.pente.filter.http.*;
import org.pente.game.*;

/**
 * @author dweebo
 */
public class HttpGameStorerSearcher extends AbstractHttpStorer implements GameStorerSearcher {

    private static ObjectFormat requestFormat = new HttpObjectFormat(new SimpleGameStorerSearchRequestFormat());
    private static GameStorerSearchResponseFormat responseFormat = new SimpleGameStorerSearchResponseFormat();

    public HttpGameStorerSearcher(String host, int port, GameFormat gameFormat) {
        super(host, port, gameFormat);
    }
    public HttpGameStorerSearcher(String host, int port, GameFormat gameFormat,
		String context, String userName, String password) {
        super(host, port, gameFormat, context, userName, password);
        //System.out.println("password = " + password);
    }

    public void search(GameStorerSearchRequestData data, GameStorerSearchResponseData responseData) throws Exception {

        // set the response format to simple, it doesn't matter since calling client
        // won't need to know how the data was formatted
        data.setGameStorerSearchResponseFormat(SimpleGameStorerSearchResponseFormat.class.getName());

        // format the request into a buffer
        StringBuffer paramsBuffer = new StringBuffer();
        requestFormat.format(data, paramsBuffer);

        // create the http request, send/receive
        StringBuffer requestBuffer = createHttpRequest(paramsBuffer, "/search");
        StringBuffer responseBuffer = sendHttpRequest(requestBuffer);

        int status = getHttpResponseCode(responseBuffer.toString());
        if (status != HttpConstants.STATUS_OK) {
            throw new HttpGameStorerException(status + " - " + responseBuffer.toString());
        }
        else {
            // strip headers
            responseBuffer = getHttpResponse(responseBuffer);

            // parse results
            responseFormat.parse(responseData, responseBuffer);
        }
    }
}