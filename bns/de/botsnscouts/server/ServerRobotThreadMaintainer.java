package de.botsnscouts.server;

/** What someone who keeps track of SRTs must provide */
interface ServerRobotThreadMaintainer{
    void deleteRob(ServerRoboterThread it, String reason);
    void reEntry(ServerRoboterThread it);
    int getTurnTimeout();
    void sendMsg(String id, String[] args);
}
