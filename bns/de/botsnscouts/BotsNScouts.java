package de.botsnscouts;
import org.apache.log4j.*;


public class BotsNScouts{
    public static final Category CAT = Category.getInstance( BotsNScouts.class );

    public static void main(String[] args) throws Throwable {
	try {
        	PropertyConfigurator.configure(BotsNScouts.class.getResource("conf/log4j.conf"));
	        CAT.debug("Starting app");
		CAT.debug("User.dir: "+System.getProperty("user.dir"));
		de.botsnscouts.start.Start.main(args);
	} catch( Throwable t ) {
		CAT.fatal("Exception:", t);
		throw t;
	}

    }
}
