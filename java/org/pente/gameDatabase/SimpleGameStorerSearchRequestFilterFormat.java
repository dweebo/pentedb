package org.pente.gameDatabase;

import java.util.*;
import java.text.*;
import java.net.*;
import java.io.*;

//import org.apache.log4j.*;

import org.pente.game.*;
import org.pente.filter.http.*;

/**
 * @author dweebo
 */
public class SimpleGameStorerSearchRequestFilterFormat implements ObjectFormat {

    private static final String     paramSeparator =        "&";

    public static final String      START_GAME_NUM_PARAM =  "start_game_num";
    public static final String      END_GAME_NUM_PARAM =    "end_game_num";
    public static final String      TOTAL_GAME_NUM_PARAM =  "total_game_num";
    public static final String      PLAYER_1_NAME_PARAM =   "player_1_name";
    public static final String      PLAYER_2_NAME_PARAM =   "player_2_name";
    public static final String      PLAYER_1_SEAT_PARAM =   "player_1_seat";
    public static final String      PLAYER_2_SEAT_PARAM =   "player_2_seat";
    public static final String      GAME_PARAM =            "game";
    public static final String      SITE_PARAM =            "site";
    public static final String      EVENT_PARAM =           "event";
    public static final String      ROUND_PARAM =           "round";
    public static final String      SECTION_PARAM =         "section";
    public static final String      AFTER_DATE_PARAM =      "after_date";
    public static final String      BEFORE_DATE_PARAM =     "before_date";
    public static final String      WINNER_PARAM =          "winner";

    public static final DateFormat  shortDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static final DateFormat  longDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private GameStorerSearchRequestFilterData convertObject(Object obj) {

        if (obj == null) {
            return null;
        }
        else if (!(obj instanceof GameStorerSearchRequestFilterData)) {
            throw new IllegalArgumentException("Object not GameStorerSearchRequestFilterData");
        }
        else {
            return (GameStorerSearchRequestFilterData) obj;
        }
    }

    public StringBuffer format(Object obj, StringBuffer buffer) {

        GameStorerSearchRequestFilterData filterData = convertObject(obj);

        return format(filterData, buffer, true);
    }

    public StringBuffer format(GameStorerSearchRequestFilterData filterData,
        StringBuffer buffer, boolean encode) {

        try {
            // add start game number
            buffer.append(START_GAME_NUM_PARAM);
            buffer.append("=");
            buffer.append(filterData.getStartGameNum());

            buffer.append(paramSeparator);

            // add end game number
            buffer.append(END_GAME_NUM_PARAM);
            buffer.append("=");
            buffer.append(filterData.getEndGameNum());

            buffer.append(paramSeparator);

            // add total game number (only valid for responses which is a little
            // weird since this is a REQUEST format, but request data gets included
            // with response data
            buffer.append(TOTAL_GAME_NUM_PARAM);
            buffer.append("=");
            buffer.append(filterData.getTotalGameNum());

            // add player 1 name if not null
            if (filterData.getPlayer1Name() != null) {
                buffer.append(paramSeparator);
                buffer.append(PLAYER_1_NAME_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        filterData.getPlayer1Name(), "UTF-8"));
                }
                else {
                    buffer.append(filterData.getPlayer1Name());
                }
            }
            // add player 1 seat
            buffer.append(paramSeparator);
            buffer.append(PLAYER_1_SEAT_PARAM);
            buffer.append("=");
            buffer.append(filterData.getPlayer1Seat());

            // add player 2 name if not null
            if (filterData.getPlayer2Name() != null) {
                buffer.append(paramSeparator);
                buffer.append(PLAYER_2_NAME_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        filterData.getPlayer2Name(), "UTF-8"));
                }
                else {
                    buffer.append(filterData.getPlayer2Name());
                }
            }
            // add player 2 seat
            buffer.append(paramSeparator);
            buffer.append(PLAYER_2_SEAT_PARAM);
            buffer.append("=");
            buffer.append(filterData.getPlayer2Seat());

            buffer.append(paramSeparator);
            buffer.append(GAME_PARAM);
            buffer.append("=");
            buffer.append(GridStateFactory.getGameName(filterData.getGame()));

            // add site name if not null
            if (filterData.getSite() != null) {
                buffer.append(paramSeparator);
                buffer.append(SITE_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        filterData.getSite(), "UTF-8"));
                }
                else {
                    buffer.append(filterData.getSite());
                }
            }

            // add event name if not null
            if (filterData.getEvent() != null) {
                buffer.append(paramSeparator);
                buffer.append(EVENT_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        filterData.getEvent(), "UTF-8"));
                }
                else {
                    buffer.append(filterData.getEvent());
                }
            }

            // add round name if not null
            if (filterData.getRound() != null) {
                buffer.append(paramSeparator);
                buffer.append(ROUND_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        filterData.getRound(), "UTF-8"));
                }
                else {
                    buffer.append(filterData.getRound());
                }
            }

            // add section name if not null
            if (filterData.getSection() != null) {
                buffer.append(paramSeparator);
                buffer.append(SECTION_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        filterData.getSection(), "UTF-8"));
                }
                else {
                    buffer.append(filterData.getSection());
                }
            }

            // add after date if not null
            if (filterData.getAfterDate() != null) {
                buffer.append(paramSeparator);
                buffer.append(AFTER_DATE_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        longDateFormat.format(filterData.getAfterDate()), "UTF-8"));
                }
                else {
                    buffer.append(longDateFormat.format(filterData.getAfterDate()));
                }
            }

            // add after date if not null
            if (filterData.getBeforeDate() != null) {
                buffer.append(paramSeparator);
                buffer.append(BEFORE_DATE_PARAM);
                buffer.append("=");
                if (encode) {
                    buffer.append(URLEncoder.encode(
                        longDateFormat.format(filterData.getBeforeDate()), "UTF-8"));
                }
                else {
                    buffer.append(longDateFormat.format(filterData.getBeforeDate()));
                }
            }

            if (filterData.getWinner() != GameData.UNKNOWN) {
                buffer.append(paramSeparator);
                buffer.append(WINNER_PARAM);
                buffer.append("=");
                buffer.append(filterData.getWinner());
            }

        } catch (UnsupportedEncodingException e) {
        }

        return buffer;
    }

    public Object parse(Object obj, StringBuffer buffer) throws ParseException {

        GameStorerSearchRequestFilterData filterData = convertObject(obj);
        Hashtable params = new Hashtable();

        try {
            HttpUtilities.parseParams(buffer.toString(), params);

            String startGameStr = (String) params.get(START_GAME_NUM_PARAM);
            String endGameStr = (String) params.get(END_GAME_NUM_PARAM);
            String totalGameStr = (String) params.get(TOTAL_GAME_NUM_PARAM);

            if (startGameStr != null) {
                filterData.setStartGameNum(Integer.parseInt(startGameStr));
            }
            if (endGameStr != null) {
                filterData.setEndGameNum(Integer.parseInt(endGameStr));
            }
            if (totalGameStr != null) {
                filterData.setTotalGameNum(Integer.parseInt(totalGameStr));
            }


            String player1Name = (String) params.get(PLAYER_1_NAME_PARAM);
            String player2Name = (String) params.get(PLAYER_2_NAME_PARAM);

            if (player1Name != null && player1Name.trim().length() > 0) {
                filterData.setPlayer1Name(URLDecoder.decode(
                    player1Name, "UTF-8"));
            }
            if (player2Name != null && player2Name.trim().length() > 0) {
                filterData.setPlayer2Name(URLDecoder.decode(
                    player2Name, "UTF-8"));
            }
            String p1SeatStr = (String) params.get(PLAYER_1_SEAT_PARAM);
            String p2SeatStr = (String) params.get(PLAYER_2_SEAT_PARAM);
            if (p1SeatStr != null && p1SeatStr.trim().length() > 0) {
            	int player1Seat = Integer.parseInt(p1SeatStr);
            	filterData.setPlayer1Seat(player1Seat);
            }
            if (p2SeatStr != null && p2SeatStr.trim().length() > 0) {
            	int player2Seat = Integer.parseInt(p2SeatStr);
            	filterData.setPlayer2Seat(player2Seat);
            }

            String gameStr = (String) params.get(GAME_PARAM);
            String site = (String) params.get(SITE_PARAM);
            String event = (String) params.get(EVENT_PARAM);
            String round = (String) params.get(ROUND_PARAM);
            String section = (String) params.get(SECTION_PARAM);

            if (gameStr == null) {
                throw new Exception("Game can't be null");
            }
            filterData.setGame(GridStateFactory.getGameId(gameStr));

            if (site != null) {
                filterData.setSite(URLDecoder.decode(
                    site, "UTF-8"));
            }
            if (event != null) {
                filterData.setEvent(URLDecoder.decode(
                    event, "UTF-8"));
            }
            if (round != null) {
                filterData.setRound(URLDecoder.decode(
                    round, "UTF-8"));
            }
            if (section != null) {
                filterData.setSection(URLDecoder.decode(
                    section, "UTF-8"));
            }


            String afterDateStr = (String) params.get(AFTER_DATE_PARAM);
            String beforeDateStr = (String) params.get(BEFORE_DATE_PARAM);

            if (afterDateStr != null) {
                if (afterDateStr.length() < 11) {
                    filterData.setAfterDate(shortDateFormat.parse(afterDateStr));
                }
                else {
                    filterData.setAfterDate(longDateFormat.parse(afterDateStr));
                }
            }
            if (beforeDateStr != null) {
                if (beforeDateStr.length() < 11) {
                    filterData.setBeforeDate(shortDateFormat.parse(beforeDateStr));
                }
                else {
                    filterData.setBeforeDate(longDateFormat.parse(beforeDateStr));
                }
            }

            String winnerStr = (String) params.get(WINNER_PARAM);
            if (winnerStr != null) {
                filterData.setWinner(Integer.parseInt(winnerStr));
            }
            else {
                filterData.setWinner(0);
            }

        } catch (Exception ex) {
            //log4j.error("ParseException ", ex);
            throw new ParseException("ParseException using URLDecoder", 0);
        }

        return filterData;
    }
}