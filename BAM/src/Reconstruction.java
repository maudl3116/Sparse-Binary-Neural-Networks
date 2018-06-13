
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




public class Reconstruction{
	

public static void main(String[] args) throws IOException {

		int m = Integer.parseInt(args[0]); // number of images we want to store
		int NumImageAlteree = Integer.parseInt(args[1]);
		int Nefface = Integer.parseInt(args[2]);
		int Niter = Integer.parseInt(args[3]);
		int gamma = 1;
		int pixels = 2;
		int taille_cluster = pixels*pixels;
		int nb_cluster = 1024/(taille_cluster);
		int[] TaillesCumulées = new int[nb_cluster];
		
		
		
		// POUR LE ROUGE

		// Récupération pour m images des valeurs des pixels pour une seule
		// couleur
		ReseauCifar will = new ReseauCifar(m, pixels);
		will.GenererReseauWillshaw();
		ArrayList<ArrayList<Integer>> Reseau = will.getReseau();

		// Recuperer taille des images (variable selon le nombre d'imagettes
		// dans le "dictionnaire"

		int taille = will.getn();
		int l_cluster = taille/nb_cluster;
		
    
		// Recuperer tailles des clusters
		for(int i=1; i<nb_cluster;i++){
			TaillesCumulées[i]=TaillesCumulées[i-1]+l_cluster;
		}
		
		// Recuperer tailles des clusters
		
		/*int[] TaillesCluster = will.getTaillesCluster();
		int[] TaillesCumulées = will.getTaillesCumulées();*/

		// Recupere le dictionnaire des Imagettes

		ArrayList<ArrayList<Integer>> DicoImagettes = will.getImagettes();

		// On altère une des m images apprises : NumImageAlteree
		int[][] Images = will.getImages();

		ArrayList<Integer> ImageAlteree = new ArrayList<Integer>();
		
		// affichage image de depart
		ImageAlteree = new ArrayList<Integer>();
		for (int i = 0; i < nb_cluster; i++) {
			ImageAlteree.add(Images[NumImageAlteree][i]);
			
		}
		
		int [] ImageAltereeTableau=new int [ImageAlteree.size()];
		for (int i=0; i<ImageAlteree.size(); i++){
			ImageAltereeTableau[i]=ImageAlteree.get(i);
			System.out.println("value image is: "+ImageAltereeTableau[i]);
		}
		
		AfficherImageReconstruite affichageinit=new AfficherImageReconstruite("ImageAltérée",ImageAltereeTableau,DicoImagettes,TaillesCumulées,55, pixels);
		affichageinit.setVisible(true);
		// fin affichage

		// On efface Nefface niveaux de clusters
		ImageAlteree = new ArrayList<Integer>();
		for (int i = 0; i < nb_cluster - Nefface; i++) {
			ImageAlteree.add(Images[NumImageAlteree][i]);
			
		}
		
		int[] Scores = new int[taille];
				
		ImageAltereeTableau=new int [ImageAlteree.size()];
		for (int i=0; i<ImageAlteree.size(); i++){
			ImageAltereeTableau[i]=ImageAlteree.get(i);
			System.out.println("value image is: "+ImageAltereeTableau[i]);
		}
		
		AfficherImageReconstruite affichage0=new AfficherImageReconstruite("ImageAltérée",ImageAltereeTableau,DicoImagettes,TaillesCumulées,0, pixels);
		affichage0.setVisible(true);

		// with the SUM of MAX rule, we need to initialize missing clusters with all ones
		// 1. find the size of the last cluster
		
		// 2. add the unknown
		/*for(int i=taille-l_cluster*Nefface; i<taille; i++){
			ImageAlteree.add(i);
		}*/

		int[] ImageReconstruite = new int[nb_cluster];
		
		// Reconstruction
		System.out.println("Reconstruction starts");
		long start = System.currentTimeMillis();
		// dynamic rule (SOS)
		
		
		boolean[]already_activated=new boolean[nb_cluster];
		
		for (int iter=0; iter<Niter; iter++){
			
			System.out.println("iteration number: "+iter);
			Scores = new int[taille];
			// score
			
			// SUM OF SUM
			int[] conflict = new int[nb_cluster];
			if(iter==0){
				int[] maxi = new int[nb_cluster];
				for (int pixel=0; pixel<Scores.length; pixel++) {
					// memory
					if(ImageAlteree.contains(pixel)) Scores[pixel]=gamma;
					// field
					for (int e:Reseau.get(pixel)){
						
						if(ImageAlteree.contains(e)){
							Scores[pixel]=Scores[pixel]+1;
						}
					}
					if(Scores[pixel]>maxi[pixel/l_cluster]){
						maxi[pixel/l_cluster]=Scores[pixel];
					}
				}
				
				// activation
				ImageAlteree = new ArrayList<Integer>();
				for (int pixel=0; pixel<Scores.length; pixel++) {
					if(Scores[pixel]==nb_cluster-Nefface){
						ImageAlteree.add(pixel);
						conflict[pixel/l_cluster]+=1;
					}
				}
				ImageAlteree.trimToSize();
				
				
			}
			
			
			
			
			// SUM OF MAX
			else{
				for (int pixel=0; pixel<Scores.length; pixel++) {
					if(conflict[pixel/l_cluster]>1 || conflict[pixel/l_cluster]==0){
						
						// memory
						if(ImageAlteree.contains(pixel)) Scores[pixel]=gamma;
						// field
						already_activated=new boolean[nb_cluster];
						
						for (int e:Reseau.get(pixel)){
							
							if(!already_activated[e/l_cluster] && ImageAlteree.contains(e)){
								Scores[pixel]=Scores[pixel]+1;
								already_activated[e/l_cluster]=true;
							}
						}
						
					
					}
					else if(conflict[pixel/l_cluster]==1){
						Scores[pixel]=gamma+nb_cluster-1;
					}
					

					
				}
				
				conflict = new int[nb_cluster];
				
				
				ImageAlteree = new ArrayList<Integer>();
				int count=0;
				for(int e=0; e<taille; e++){
					if(Scores[e]==gamma+nb_cluster-1){
						count+=1;
						ImageAlteree.add(e);
						conflict[e/l_cluster]+=1;
					}
				}
				System.out.println("count"+count);
				ImageAlteree.trimToSize(); 
			}
			
			//Afficher l'image à la ieme itération. 
			
			int [] ImagettesIter=new int [nb_cluster];
			
			
			already_activated = new boolean[nb_cluster];
			int index = 0;
			for(int e:ImageAlteree){
				if (!already_activated[e/l_cluster]){
					ImagettesIter[index]=e;
					index+=1;
					already_activated[e/l_cluster]=true;
				}
			}
			
			AfficherImageReconstruite affichage_iter = new AfficherImageReconstruite("Voici l'image Reconstruite",
				ImagettesIter, DicoImagettes, TaillesCumulées,iter+1, pixels);
			System.out.println(iter);
			affichage_iter.setVisible(true);
			
		}// fin for iter

		long end = System.currentTimeMillis();
		
		long duration = end-start;
		System.out.println("the duration of the reconstruction is: "+duration+"ms");

		int[] ImagettesRouge = new int[nb_cluster];
		
		already_activated = new boolean[nb_cluster];
		int index = 0;
		for(int e:ImageAlteree){
			if (!already_activated[e/l_cluster]){
				ImagettesRouge[index]=e;
				index+=1;
				already_activated[e/l_cluster]=true;
			}
		}

		AfficherImageReconstruite affichage_final = new AfficherImageReconstruite("Voici l'image Reconstruite",
				ImagettesRouge, DicoImagettes, TaillesCumulées,Niter+1, pixels);
		affichage_final.setVisible(true);
		System.out.println("last");
		
		
		// tell whether the result is correct
		for(int i=0; i<nb_cluster; i++){
			if (ImagettesRouge[i]!=Images[NumImageAlteree][i]){
				System.out.println("FALSE");
				break;
			}
		}
		
		
	}

/*PriorityQueue minHeap = new PriorityQueue();
for (int pixel = 0; pixel < taille; pixel++) {
	minHeap.offer(100000 - ImageTemp[pixel]);
}
int seuil = 0;

for (int i = 0; i < nb_cluster; i++) {
	int temp = (Integer) minHeap.poll();
	seuil = 100000 - temp;
}

for (int i = 0; i < ImageTemp.length; i++) {
	if (ImageTemp[i] >= seuil) {
		ImageAlteree.add(i);
	}
}*/

}
