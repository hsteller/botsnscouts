package de.spline.rr;

/**
* @author Holger Dreher, Mohammad Al-Saad, Alexander R.
* @version 1.0
*/
import java.util.*;

public class ThreadBesitzer extends Thread
{
	public Vector Threads;
	public ThreadBesitzer(Vector Threads)
	{
		this.Threads=Threads;
	}
	
	public void run()
	{
		if(Threads!=null)
		{
			for(int i=0;i<Threads.size();i++)
			{
				((Thread)Threads.elementAt(i)).start();
			}	
			for(int i=0;i<Threads.size();i++)
			{	
				try
				{
					((Thread)Threads.elementAt(i)).join();
				}
				catch(InterruptedException e)
				{
				}	
			}
		}
	}
}
