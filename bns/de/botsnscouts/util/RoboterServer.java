package de.spline.rr;

/**
 * Roboter-Objekt, wie es vom Server verwendet wird
 */
public class RoboterServer extends Roboter
{
    public Karte[] zugeteilteKarten = new Karte[9];
    public KommServerRoboter Komm;
    public boolean naechsteRundeDeaktiviert;


    public RoboterServer( RoboterServer r) {
	super(r);
	
	for(int i = 0 ; i < 5; i ++)
	    if(gesperrteRegister[i]!=null)
		r.gesperrteRegister[i] = new Karte(this.gesperrteRegister[i].getprio(), this.gesperrteRegister[i].getaktion());
	    else
		r.gesperrteRegister[i]=null;
            naechsteRundeDeaktiviert = false;

    }
    
    public RoboterServer() {
	super("NoName");
    }

    public RoboterServer(String robName)
	{
            super(robName);
            naechsteRundeDeaktiviert = false;
	}
	
    public String toString() {
	String s=super.toString();
						
	s+="\n\t\tzugeteilteKarten: ";
	for(int i =0; i<9; i++)
	    if (zugeteilteKarten[i]!=null)
		s+=" ["+zugeteilteKarten[i].getprio()+"|"+zugeteilteKarten[i].getaktion()+"]";
	    else
		s+="#";
	
	s+="\n";
	return s;
    }
    
    public void zeige_Roboter()
        {
            Global.debug(this,this.toString());
        }
	
}
