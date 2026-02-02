package xyz.openexonaut.servlet;

import java.io.*;

import javax.servlet.http.*;

import com.smartfoxserver.v2.*;
import com.smartfoxserver.v2.entities.match.*;

public class ExonautOnline extends HttpServlet {
    private int countForMode(String mode) {
        return SmartFoxServer.getInstance()
                .getAPIManager()
                .getSFSApi()
                .findRooms(
                        SmartFoxServer.getInstance()
                                .getZoneManager()
                                .getZoneByName("Exonaut")
                                .getRoomList(),
                        new MatchExpression("mode", StringMatch.EQUALS, mode),
                        0)
                .stream()
                .mapToInt(room -> room.getPlayersList().size())
                .sum();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter()
                .write(
                        String.format(
                                "{\"solo\":%d,\"team\":%d}",
                                countForMode("freeforall"), countForMode("team")));
    }
}
