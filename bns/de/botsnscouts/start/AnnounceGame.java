package de.botsnscouts.start;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import nanoxml.XMLElement;

import org.apache.log4j.Category;

import de.botsnscouts.util.Conf;
import de.botsnscouts.util.InvalidInputException;

/**  Announce the new game at a meta server.
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */

class AnnounceGame {

    private int portNo;
    private String serverName;
    private boolean doAnnounce = false;

    private static final Category CAT = Category.getInstance(AnnounceGame.class);

    public AnnounceGame() {
        //Get Default from Conf
        serverName = Conf.getDefaultMetaServer();
        portNo = Conf.getDefaultMetaServerPort();
    }

    void setAnnounce(boolean doIt) {
        doAnnounce = doIt;
    }

    /**
     * Announce the game at the meta server, known via serverName and portNo.
     * @throws UnableToAnnounceGameException It might not be possible to announce the game
     *         because we cannot connect the meta server (wrong ip or port, meta server down
     *         or no internet connection.
     * @throws YouAreNotReachable Thrown when the meta server found out that it will
     *         not be possible for clients to connect to our game server, e.g. because
     *         we are behind a firewall.
     */
    void announceGame(GameOptions gameOptions)
            throws UnableToAnnounceGameException, YouAreNotReachable {
        if (doAnnounce) {
            CAT.info("Going to announce game at meta server "+getServerString());

            try {
                Socket s = new Socket( serverName, portNo);
                Reader reader = new InputStreamReader(s.getInputStream());
                Writer writer = new OutputStreamWriter(s.getOutputStream());
                writer.write(gameOptions.toXML().toString());
                writer.flush();
                XMLElement answer = new XMLElement();
                answer.parseFromReader(reader);
                if (!answer.getName().equals("announced")) {
                    if (answer.getAttribute("reason").equals("noBNSServerAccessible"))
                        throw new YouAreNotReachable();
                    else throw new UnableToAnnounceGameException();
                }
            } catch (IOException ex) {
                CAT.warn(ex);
                throw new UnableToAnnounceGameException(ex.getMessage());
            }
        }
    }

    String getServerString() {
        return serverName+":"+portNo;
    }

    /**
     *  Trying to set a new server name and port from a string.
     */
    void parse(String s) throws InvalidInputException {
        CAT.debug("Parsing "+s);
        try {
            for (int i=0; i < s.length(); i++) {
                if (s.charAt(i)==':') {
                    //ok, serverName is before, portNo after ':'
                    portNo = Integer.parseInt(s.substring(i+1, s.length()));
                    serverName = s.substring(0, i);
                    Conf.setProperty("meta.server", serverName);
                    Conf.setProperty("meta.port", ""+portNo);
                    Conf.saveProperties();
                    break;
                }
                if (!Character.isLetterOrDigit(s.charAt(i)) && s.charAt(i)!='.') {
                    // Cannot be an ip address!
                    throw new InvalidInputException("No IP address");
                }
            }
        } catch (RuntimeException ex) {
            CAT.info("Invalid input", ex);
            throw new InvalidInputException();
        }
    }

    public boolean willBeAnnounced() {
        return doAnnounce;
    }


}
