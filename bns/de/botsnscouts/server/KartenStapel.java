package de.spline.rr;

import java.util.Random;
import java.util.*;



/**
 * KartenStapel enthält die Karten die im Spiel benutzt werden.
 * Die Art der Karten ist festgelegt (kann nicht ohne Neukompilieren geändert werden).
 * Beim Initialisieren wird ein gemischter Stapel erzeugt. Von ihm kann man Karten mit
 * der Hilfe der Metode gibKarte() ziehen.
 * @author Alexander, Holger, Mohammad
 * @version 1.0 
*/
public class KartenStapel
{
	   //die Menge der ganzen Karten
	   private static Karte[] KartenMenge = {
									 new Karte(49,"M1"),
									 new Karte(50,"M1"),
									 new Karte(51,"M1"),
									 new Karte(52,"M1"),
									 new Karte(53,"M1"),
									 new Karte(54,"M1"),
									 new Karte(55,"M1"),
									 new Karte(56,"M1"),
									 new Karte(57,"M1"),
									 new Karte(58,"M1"),
									 new Karte(59,"M1"),
									 new Karte(60,"M1"),
									 new Karte(61,"M1"),
									 new Karte(62,"M1"),
									 new Karte(63,"M1"),
									 new Karte(64,"M1"),
									 new Karte(65,"M1"),
									 new Karte(66,"M1"),

									 new Karte(67,"M2"),
									 new Karte(68,"M2"),
									 new Karte(69,"M2"),
									 new Karte(70,"M2"),
									 new Karte(71,"M2"),
									 new Karte(72,"M2"),
									 new Karte(73,"M2"),
									 new Karte(74,"M2"),
									 new Karte(75,"M2"),
									 new Karte(76,"M2"),
									 new Karte(77,"M2"),
									 new Karte(78,"M2"),

									 new Karte(79,"M3"),
									 new Karte(80,"M3"),
									 new Karte(81,"M3"),
									 new Karte(82,"M3"),
									 new Karte(83,"M3"),
									 new Karte(84,"M3"),

									 new Karte(43,"BU"),
									 new Karte(44,"BU"),
									 new Karte(45,"BU"),
									 new Karte(46,"BU"),
									 new Karte(47,"BU"),
									 new Karte(48,"BU"),

									 new Karte(7,"RL"),
									 new Karte(9,"RL"),
									 new Karte(11,"RL"),
									 new Karte(13,"RL"),
									 new Karte(15,"RL"),
									 new Karte(17,"RL"),
									 new Karte(19,"RL"),
									 new Karte(21,"RL"),
									 new Karte(23,"RL"),
									 new Karte(25,"RL"),
									 new Karte(27,"RL"),
									 new Karte(29,"RL"),
									 new Karte(31,"RL"),
									 new Karte(33,"RL"),
									 new Karte(35,"RL"),
									 new Karte(37,"RL"),
									 new Karte(39,"RL"),
									 new Karte(41,"RL"),

									 new Karte(8,"RR"),
									 new Karte(10,"RR"),
									 new Karte(12,"RR"),
									 new Karte(14,"RR"),
									 new Karte(16,"RR"),
									 new Karte(18,"RR"),
									 new Karte(20,"RR"),
									 new Karte(22,"RR"),
									 new Karte(24,"RR"),
									 new Karte(26,"RR"),
									 new Karte(28,"RR"),
									 new Karte(30,"RR"),
									 new Karte(32,"RR"),
									 new Karte(34,"RR"),
									 new Karte(36,"RR"),
									 new Karte(38,"RR"),
									 new Karte(40,"RR"),
									 new Karte(42,"RR"),

									 new Karte(1,"UT"),
									 new Karte(2,"UT"),
									 new Karte(3,"UT"),
									 new Karte(4,"UT"),
									 new Karte(5,"UT"),
									 new Karte(6,"UT")
							};

	// ein einziges Attribut
	private Karte[] stapel;
	private int schonVerteilteKarten;
  
	/**
	 * Dem Konstruktor werden die gesperten karten uebergeben.
	 * Die gesperten Karten werden im Stapel nicht mitgemischt	
	    * @param gespert Ein Array mit den gesperten Karten
	    */

/*	public KartenStapel(Karte[] gespert)
	{ 
		schonVerteilteKarten=0;
		int AnzahlderFreienKarten = 84-gespert.length;
		stapel=new Karte[AnzahlderFreienKarten];
		int zInStapel=0;
		// stapel wird mit den noch nicht gesperten Karten gefühlt
			 for(int z=0;z<84;z++)
		{
			if (!istKarteInArray(gespert,KartenMenge[z]))
			{
				stapel[zInStapel]=new Karte(0,"");
				stapel[zInStapel].copy(KartenMenge[z]);
				zInStapel++;	
			}
		}

		mische();
	}
*/

	public KartenStapel(Vector gespert)
	{ 
		schonVerteilteKarten=0;
		int AnzahlderFreienKarten = 84-gespert.size();
		stapel=new Karte[AnzahlderFreienKarten];
		int zInStapel=0;
		// stapel wird mit den noch nicht gesperten Karten gefühlt
		for(int z=0;z<84;z++)
		{
			if (!istKarteInArray(gespert,KartenMenge[z]))
			{
				stapel[zInStapel]=new Karte(0,"");
				stapel[zInStapel].copy(KartenMenge[z]);
				zInStapel++;	
			}
		}

		mische();
	}


	/**
	    * Erzeugt neuen Stapel mit 84 Karten. 
	    */
	public KartenStapel()
	{ 
		schonVerteilteKarten=0;
		int AnzahlderFreienKarten = 84;
		stapel=new Karte[AnzahlderFreienKarten];
			 for(int z=0;z<84;z++)
		{
				stapel[z]=new Karte(0,"");
				stapel[z].copy(KartenMenge[z]);
		}
		mische();
	}

	/**
	* Die Metode gibt eine Karte zurück, wenn der Stapel nicht leer ist, sonst - null.
	* @return eine Karte aus dem Stapel
	*/ 
	public Karte gibKarte()
	{
		
		if (schonVerteilteKarten==stapel.length)		
			return null;
		else
		{
			schonVerteilteKarten++;
			return	stapel[schonVerteilteKarten-1];
		}
	}

	private void mische()
	{
		Random rand=new Random();
		int stapL=stapel.length;
		int schonGemischt=0;
		Karte k=new Karte(0,"");
		while(schonGemischt<(stapL-1))
		{
			int r=rand.nextInt()%(stapL-1-schonGemischt);
			if(r<0) r=(-r);
			k.copy(stapel[stapL-1-schonGemischt]);
			stapel[stapL-1-schonGemischt].copy(stapel[r]);
			stapel[r].copy(k);
			schonGemischt++;
		}
	}
	
	// TRUE <=> k ist in Arr vorhanden
	private boolean istKarteInArray(Vector v, Karte k)
	{
		if (v==null) return false;
		int vl=v.size();
		for(int i=0;i<vl;i++){
			Karte von_v=(Karte)v.elementAt(i);
			if ((von_v.getprio()==k.getprio())&&(von_v.getaktion()==k.getaktion()))
			{
				return true;
			}
		}
		return false;
	}

}	 
   
