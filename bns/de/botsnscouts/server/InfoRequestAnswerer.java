package de.botsnscouts.server;

/** Can answer the various InfoRequests a client might have */
interface InfoRequestAnswerer{
    int getFieldSizeX();
    int getFieldSizeY();
    String getFieldString();
    de.botsnscouts.util.Ort[] getFlags();
    String[] getNames();
    de.botsnscouts.util.Ort getRobPos(String name);
    de.botsnscouts.util.Roboter getRobStatus(String name);
    boolean gameRunning();
    String[] getStanding();
    de.botsnscouts.util.Status[] getEvalStatus();
    String[] getNamesByColor();
    de.botsnscouts.util.StatsList getStats();
}
