package de.botsnscouts;
import org.apache.log4j.*;


public class BotsNScouts{
    public static Category CAT = Category.getInstance( BotsNScouts.class );


    public static void main(String[] args){
        PropertyConfigurator.configure(BotsNScouts.class.getResource("conf/log4j.conf"));
        CAT.debug("Starting app");
	de.botsnscouts.start.Start.main(args);
        CAT.debug("Ending app");
    }
}
