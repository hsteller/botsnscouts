package de.botsnscouts.server;

/** The admin-functions on the entity that maintains the
    list of active ServerRobotThreads / ServerOutputThreads 
*/
public interface ThreadMaintainer{
    /** Should give a reference, as any non-alive threads
	will be removed by an Iterator.remove() */
    java.util.Vector /* of ServerAusgabeThread */ getActiveOutputs();
    void deleteOutput(ServerAusgabeThread victim, String reason);
    void addOutput(ServerAusgabeThread n);
    int allocateColor(int myPreferredColor, String name);
    void addRobotThread(ServerRoboterThread n);
    MOKListener getMOKListener();
    OKListener getOKListener();
    ServerRobotThreadMaintainer getRobThreadMaintainer();
    ServerOutputThreadMaintainer getOutputThreadMaintainer();
    InfoRequestAnswerer getInfoRequestAnswerer();
    de.botsnscouts.start.StartServer getStartServer();
    int getSignUpTimeout();
    int getMaxPlayers();
}
