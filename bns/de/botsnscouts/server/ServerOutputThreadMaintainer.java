package de.botsnscouts.server;

interface ServerOutputThreadMaintainer{
    void deleteOutput(ServerAusgabeThread it, String reason);
    int getOutputTimeout();
}
