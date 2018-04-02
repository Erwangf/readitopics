package utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import core.MyDocument;

/**
 * Classe pour manipuler des vecters
 * @author Antoine
 */	
public class Vector {

	public static double sim(double[] d1,double[] d2) {
		double cosin = 0;
		int taille = d1.length;
		if (taille == d2.length){
			for (int k = 0;k<taille;k++){
				cosin  += d1[k] * d2[k];					
			}		
		}
		return cosin/norme(d1)*norme(d2);
	}

	public static TreeMap<Double, Integer> norm_max(double[] d1,double[] d2,int j) {
		TreeMap<Double,Integer> ts = new TreeMap<>();
		for (int i =0;i<d1.length;i++){
			ts.put(-1*d1[i],i);
		}
		return ts;
	}
	public static double somme_produit(double[] d1, double[] d2){
		double out = 0;
		for (int k = 0;k<d1.length;k++){
			out+= d1[k] * d2[k];
		}
		return out;
	}
	public static double norme(double[] d1) {
		double n = 0;
		for (double aD1 : d1) {
			n += aD1 * aD1;
		}		
		n = Math.sqrt(n);
		return n;
	}


	public static double[] addi(double[] d1, double[] d2) {
		// TODO Auto-generated method stub
		double[] out = new double[d1.length];
		for(int i = 0;i<d1.length;i++){
			out[i] = d1[i]+d2[i];
		}
		return out;
	}

	public static double[] div(double[] d1,double j) {
		// TODO Auto-generated method stub
		double[] out = new double[d1.length];
		if (j!=0){
			for(int i = 0;i<d1.length;i++){
				out[i] = d1[i]/j;
			}
		}else{
			for(int i = 0;i<d1.length;i++){
				out[i] = 0;
			}
		}
		return out;
	}

	public static double som(double[] d1) {
		// TODO Auto-generated method stub
		double out = 0;
		for (double aD1 : d1) {
			out += aD1;
		}
		return out;
	}
	public double moyenne(double[] d1) {
		// TODO Auto-generated method stub
		double out = 0.0;
		double c = 0.0;
		for (double d : d1){
			out+=d;
			c++;
		}
		out = out/c;
		return out;
	}
	public static double[] normalise(double[] d) {
		// TODO Auto-generated method stub
		double norme = norme(d);
		double[] out = new double[d.length];
		int i = 0;
		for (double d_t : d){
			out[i] = (d_t / norme);
			i++;
		}
		return out;
	}
	public static void main(String[] args) {
		double[] a = new double[]{1,3,5,2};
		TreeMap<Double,Integer>ts = norm_max(a,a,0);
		for (Map.Entry<Double,Integer> entry : ts.entrySet()) {
			System.out.println(entry.getValue() +" "+ entry.getKey());
		}
	}
}
