package edu.illinois.cs.cogcomp.descartes.util;

/**
 * @author vivek
 * Statistics of one variable.
 * 
 * Mean, standard deviation.
 * 
 *
 */
public class OneVariableStats
{
	double sigmax;
	int num;
	double sigmax2;
	
	public void reset()
	{
		sigmax = 0;
		num =0;
		sigmax2= 0;
	}
	
	public OneVariableStats()
	{
		reset();
	}
	
	public void add(double d)
	{
		num++;
		sigmax += d;
		sigmax2 += (d*d);
	}
	
	
	public double mean()
	{
		return (sigmax)/num;
	}
	
	public double std()
	{
		double m = mean();
		return Math.sqrt(sigmax2/num -  m*m);
	}
	
	
	
	
}
