
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class RecuperationImageCIFAR {

	public int nb_images;
	public int[] TaillesCluster;
	public int[] TaillesCumulées;
    public int[][] RGB = new int[32][32];

	public int pixels;
	public int d;
	public int taille_cluster;
	public int nb_cluster;
    public int q=16;

    public int l_cluster;
    
	public int[][] Images;
	private FileInputStream fis;
	public ArrayList<ArrayList<Integer>> imagettes;

	public byte[]tab;

	public RecuperationImageCIFAR(int m, int pixels) throws IOException {
		this.pixels = pixels;
		this.taille_cluster = pixels * pixels;
		this.nb_cluster = 1024/taille_cluster;
		this.d = 32/pixels;
		this.nb_images = m;
		this.l_cluster= nb_images;//(int) Math.pow(256/q,taille_cluster);
		Images = new int[nb_images][nb_cluster];
		LireFichierBinaire("./data_batch_1.bin");
		createImages();
	}

	//extract image from tab, and store in a 32*32 array
	public void extract(int image){
		for (int k = 0; k < 32; k++) {
			for (int l = 0; l < 32; l++) {
				RGB[k][l] = tab[32 * k + l + 1 + 3073 * image + 0 * 1024];  // 3073 -> skips two color channels
			}
		}
	}
	
	public int findImage(int toFind){
		
		extract(toFind);
		int image = 0 ;
		boolean flag = false;
		
		long start = System.currentTimeMillis();
		
		while(image<nb_images){
		    flag= false;
			for (int k = 0; k < 32; k++) {
				for (int l = 0; l < 32; l++) {
					if (RGB[k][l]!=tab[32 * k + l + 1 + 3073 * image + 0 * 1024]){
						flag=true;
						break;
					}
				}
				if (flag) break;
			}
			if (!flag){
				
				return image;
			}
			else{
				image+=1;
			}
		}
		
		return -1;
		
	}

	// reduces the number of grey levels (quantification)
	public void quantification(){
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				int temp = (RGB[i][j] + 128)/this.q; 
				RGB[i][j] = this.q*temp;
				//System.out.println(RGB[i][j]);
			}
		}
	}
	
	// changes the representation of a cluster (from 2D array to 1D array)
	public int[] flatten(int k, int l){
		int[] RGBrow = new int[taille_cluster];
		for (int i = 0; i < pixels; i++) {
			for (int j = 0; j < pixels; j++) {
				RGBrow[i * pixels + j] = RGB[k * pixels + i][l * pixels + j];
			}
		}
		return RGBrow;
	}
	
	public ArrayList<Integer>array_2_list(int[] RGBrow){
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int i=0; i<RGBrow.length;i++){
			array.add(RGBrow[i]);
		}
		return array;
	}
	
	
	private int[][] createImages() throws IOException {

		imagettes = new ArrayList<ArrayList<Integer>>(nb_cluster);

		this.TaillesCluster = new int[nb_cluster];

		for (int u = 0; u < nb_images; u++) { // for all images

			// 1----create a 32*32 array representing the image
			extract(u);
			
			// 2----apply quantification to the image
			quantification();

			// 3----Create a dictionary of clusters, and change the representation of the dataset
			
			//go through all clusters (row:k // column:l) k*d+l is the cluster number
	
			for (int k = 0; k < d; k++) {
				for (int l = 0; l < d; l++) {
					
					// --b-- flatten the cluster
					int[] RGBrow = flatten(k,l);
					
					int curseurimagette = 0; 
					
					// if we are working with the first image
					if (u == 0) {
						imagettes.add(array_2_list(RGBrow)); // the dictionary of clusters
						Images[u][k * d + l] = 0;  // Images, contains the new representation of the dataset
						
					}
					// otherwise we have to determine whether we have seen a new cluster
					else {

						int curseurimagettes = 0; // curseurimagettes* taille_cluster points to the first pixel of a cluster in the dictionary
						
						boolean flag_spotted = false;
						
						while (curseurimagettes * taille_cluster < imagettes.get(k * d + l).size()) { 
							
							int curseur = 0; // goes through the pixels of the cluster
							
							// have we already registered this cluster?
							while (RGBrow[curseur] == imagettes.get(k * d + l).get(
									curseurimagettes * taille_cluster + curseur)) {
								curseur++;
								
								if (curseur == taille_cluster){
									flag_spotted = true;
									break;
								}
							}
							if (flag_spotted){
								//System.out.println("Image: "+u);
								//System.out.println("cluster: "+(k*d+l));
								break;
							}
							// keep comparing with the following clusters
							else{
								curseurimagettes++;
							}
						}
						
						// find the new representation of the image 

						if (!flag_spotted) {
							
							Images[u][k * d + l] = curseurimagettes;
							imagettes.get(k * d + l).addAll(array_2_list(RGBrow));
							
						} else {
							
							Images[u][k * d + l] = curseurimagettes;
						}
					}
				}// end for k 
			}// end for l
		} // end for u

		
		// Obtain a binary representation

		// --a--register how many different clusters have been found for each cluster

		/*for (int cluster = 0; cluster < nb_cluster; cluster++) {
			TaillesCluster[cluster] = imagettes.get(cluster).size();
			TaillesCluster[cluster] = TaillesCluster[cluster] / taille_cluster; // tells you how many different clusters there are
			//System.out.println("cluster "+cluster+": "+TaillesCluster[cluster]);
		}*/

		/*TaillesCumulées = new int[nb_cluster];
		TaillesCumulées[0] = 0;
		for (int i = 0; i < nb_cluster - 1; i++) {
			TaillesCumulées[i + 1] = TaillesCumulées[i] + TaillesCluster[i];
		}*/

		// --b--ajouter à chaque ligne le cumul précédent
		for (int cluster = 1; cluster < nb_cluster; cluster++) {
			for (int i = 0; i < nb_images; i++) {
				Images[i][cluster] = Images[i][cluster] + cluster*l_cluster;
			}
		}
		
		// --c--we have now a new representation for the dataset
		return Images;
	
	}// end method

	public int[][] getImages() {
		return Images;
	}

	public int[] getTaillesCluster() {
		return TaillesCluster;
	}

	public int[] getTaillesCumulées() {
		return TaillesCumulées;
	}

	public ArrayList<ArrayList<Integer>> getImagettes() {
		System.out.println(nb_images + " " + imagettes.get(2).size()); 
		return imagettes;

	}
	
	public int getl() {
		return l_cluster;

	}

	public void LireFichierBinaire(String nomFichier) throws IOException {

		File fichier = new File(nomFichier);
		byte[] tab = new byte[(int) fichier.length()];
		fis = new FileInputStream(fichier);
		fis.read(tab, 0, (int) fichier.length());
		this.tab = tab;
	}

}
