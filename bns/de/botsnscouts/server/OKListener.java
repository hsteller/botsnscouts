package de.botsnscouts.server;

/** Used by a Waitable to notify an interested party
    that something has been received (e.g., a register
    programming). */
interface OKListener{
    void notifyDone(Waitable w);
}
