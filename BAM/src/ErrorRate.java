
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;



public class ErrorRate{
	
	public static ReseauCifar will;
	public static ArrayList<ArrayList<Integer>> Reseau;
	
	public ErrorRate(int m, int pixels) throws IOException{
		will = new ReseauCifar(m, pixels);
		will.GenererReseauWillshaw();
		Reseau = will.getReseau();
		

	}

public static void main(String[] args) throws IOException {
	int m = Integer.parseInt(args[0]); 
	int Nefface = Integer.parseInt(args[1]);
	int Niter = Integer.parseInt(args[2]);
	int pixels = 4;

	ErrorRate test = new ErrorRate(m, pixels);


		
		// pick randomly 100 images
		Random rand = new Random();
		int NumImageAlteree = 0;
		for (int i=0; i<100; i++){
			NumImageAlteree = rand.nextInt(m);
			String[] args2 = {Integer.toString(m),Integer.toString(NumImageAlteree),Integer.toString(Nefface),Integer.toString(Niter)};
			Reconstruction2.main(args2);
			System.out.println("ground truth: "+NumImageAlteree);
			long start = System.nanoTime();
			will.recup.findImage(5999);
			long end = System.nanoTime();
			System.out.println("traditional took:"+(end-start));
			
		}
		
		
	
}
}