package de.botsnscouts.server;

/** Used by a Waitable to notify an interested party
    that a Message has been acknowledged */
interface MOKListener{
    void notifyDone(Waitable w);
}
