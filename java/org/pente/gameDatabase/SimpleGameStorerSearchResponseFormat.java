package org.pente.gameDatabase;

import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;

import org.pente.game.*;
import org.pente.filter.http.*;

/**
 * @author dweebo
 */
public class SimpleGameStorerSearchResponseFormat implements GameStorerSearchResponseFormat {

    private static final String     paramSeparator =        "&";
    private static final String     moveDelimiter =         ",";

    private static final String     REQUEST_PARAM =         "request";
    private static final String     MOVE_RESULTS_PARAM =    "move_results";
    private static final String     GAME_PARAM =            "game";
    private static final String     GAME_ID = "game_id";

    public String getContentType() {
        return HttpConstants.CONTENT_TYPE_TEXT;
    }

    private GameStorerSearchResponseData convertObject(Object obj) {

        if (obj == null) {
            return null;
        }
        else if (!(obj instanceof GameStorerSearchResponseData)) {
            throw new IllegalArgumentException("Object not GameStorerSearchResponseData");
        }
        else {
            return (GameStorerSearchResponseData) obj;
        }
    }

    public StringBuffer format(Object data, StringBuffer buffer) {

        try {
            GameStorerSearchResponseData responseData = convertObject(data);

            formatRequestData(responseData.getGameStorerSearchRequestData(), buffer);
            buffer.append(paramSeparator);
            buffer.append("rotation=" + responseData.getRotation());
            buffer.append(paramSeparator);
            formatMoveResults(responseData, buffer, true);

            if (!responseData.getGames().isEmpty()) {
                buffer.append(paramSeparator);
                formatGames(responseData, buffer, true);
            }
        } catch (UnsupportedEncodingException e) {
        }

        return buffer;
    }

    public StringBuffer formatRequestData(
        GameStorerSearchRequestData data, StringBuffer buffer)
        throws UnsupportedEncodingException {

        SimpleGameStorerSearchRequestFormat requestFormat = new SimpleGameStorerSearchRequestFormat();
        StringBuffer buf = new StringBuffer();
        requestFormat.format(data, buf);

        buffer.append(REQUEST_PARAM);
        buffer.append("=");
        buffer.append(URLEncoder.encode(buf.toString(), "UTF-8"));

        return buffer;
    }

    public StringBuffer formatMoveResults(
        GameStorerSearchResponseData data, StringBuffer buffer, boolean encode)
        throws UnsupportedEncodingException {

        if (encode) {
            buffer.append(MOVE_RESULTS_PARAM);
            buffer.append("=");
        }

        StringBuffer resultsBuf = new StringBuffer();
        Vector searchResultsVector = data.searchResponseMoveData();
        Enumeration searchResults = searchResultsVector.elements();
        while (searchResults.hasMoreElements()) {
            GameStorerSearchResponseMoveData moveData = (GameStorerSearchResponseMoveData) searchResults.nextElement();

            resultsBuf.append(PGNGameFormat.formatCoordinates(moveData.getMove()));
            resultsBuf.append(moveDelimiter);
            resultsBuf.append(moveData.getGames());
            resultsBuf.append(moveDelimiter);
            resultsBuf.append(moveData.getWins());
            resultsBuf.append(moveDelimiter);
        }

        if (encode) {
            buffer.append(URLEncoder.encode(resultsBuf.toString(), "UTF-8"));
        }
        else {
            buffer.append(resultsBuf.toString());
        }

        return buffer;
    }

    public StringBuffer formatGames(GameStorerSearchResponseData data,
		StringBuffer buffer, boolean encode)
		throws UnsupportedEncodingException {

        PGNGameFormat gameFormat = new PGNGameFormat();
        Enumeration games = data.getGames().elements();

        int gameCount = 0;
        while (games.hasMoreElements()) {
            GameData gameData = (GameData) games.nextElement();

            if (gameCount++ > 0) {
                buffer.append(paramSeparator);
            }

            buffer.append(GAME_ID);
            buffer.append("=");
            buffer.append(gameData.getGameID());

            buffer.append(paramSeparator);

            buffer.append(GAME_PARAM);
            buffer.append("=");
            StringBuffer tmp = new StringBuffer();
			gameFormat.format(gameData, tmp);
			if (encode) {
				buffer.append(URLEncoder.encode(tmp.toString(), "UTF-8"));
			}
			else {
				buffer.append(tmp);
			}
        }
        return buffer;
    }

    public Object parse(Object data, StringBuffer buffer) throws ParseException {

        GameStorerSearchResponseData responseData = convertObject(data);

        if (responseData == null) {
            responseData = new SimpleGameStorerSearchResponseData();
        }

        Hashtable params = new Hashtable();

        try {
            HttpUtilities.parseParams(buffer.toString(), params);
        } catch (Exception ex) {
            throw new ParseException("ParseException parsing params", 0);
        }

        // parse request data
        GameStorerSearchRequestData requestData = new SimpleGameStorerSearchRequestData();
        SimpleGameStorerSearchRequestFormat requestFormat = new SimpleGameStorerSearchRequestFormat();
        String requestStr = (String) params.get(REQUEST_PARAM);
        StringBuffer requestBuf = new StringBuffer(requestStr);
        requestData = (GameStorerSearchRequestData) requestFormat.parse(requestData, requestBuf);
        responseData.setGameStorerSearchRequestData(requestData);

        String rotationStr = null;
        try {
            rotationStr = (String) params.get("rotation");
            if (rotationStr != null) {
            	int rotation = Integer.parseInt(rotationStr);
            	responseData.setRotation(rotation);
            }
        } catch (Exception ex) {
            throw new ParseException("ParseException on rotation", 0);
        }

        // parse move results
        String moveResults = null;
        try {
            moveResults = (String) params.get(MOVE_RESULTS_PARAM);
            if (moveResults != null) {
                moveResults = URLDecoder.decode(moveResults, "UTF-8");
            }
        } catch (Exception ex) {
            throw new ParseException("ParseException using URLDecoder on moveResults", 0);
        }
        if (moveResults.length() > 0) {
            StringTokenizer moveTokenizer = new StringTokenizer(moveResults, moveDelimiter);
            while (moveTokenizer.hasMoreTokens()) {
                // this will fail if number of delimiters % 3 != 0
                GameStorerSearchResponseMoveData moveData = new SimpleGameStorerSearchResponseMoveData();
                moveData.setMove(PGNGameFormat.parseCoordinates(moveTokenizer.nextToken()));
                moveData.setGames(Integer.parseInt(moveTokenizer.nextToken()));
                moveData.setWins(Integer.parseInt(moveTokenizer.nextToken()));

                responseData.addSearchResponseMoveData(moveData);
            }
        }

        Object games = params.get(GAME_PARAM);
        Object gids = params.get(GAME_ID);
        if (games != null) {
            GameFormat gameFormat = new PGNGameFormat();
            if (games instanceof Vector) {
                Vector gamesVec = (Vector) games;
                Vector gameIDsVec = (Vector) gids;
                for (int i = 0; i < gamesVec.size(); i++) {
                    GameData gameData = new DefaultGameData();
                    gameData = (GameData) gameFormat.parse(gameData, new StringBuffer((String) gamesVec.elementAt(i)));
                    gameData.setGameID(Long.parseLong((String) gameIDsVec.elementAt(i)));
                    responseData.addGame(gameData);
                }
            }
            else if (games instanceof String) {
                GameData gameData = new DefaultGameData();
                gameData = (GameData) gameFormat.parse(gameData, new StringBuffer((String) games));
                gameData.setGameID(Long.parseLong((String) gids));
                responseData.addGame(gameData);
            }
        }

        return responseData;
    }
}