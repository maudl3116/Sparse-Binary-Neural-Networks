
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
		int gamma = 0;
		int pixels = 4;
		int taille_cluster = pixels*pixels;
		int nb_cluster = 1024/(taille_cluster);

		// POUR LE ROUGE

		// Récupération pour m images des valeurs des pixels pour une seule
		// couleur
		ReseauCifar will = new ReseauCifar(m, pixels);
		will.GenererReseauWillshaw();
		ArrayList<ArrayList<Integer>> Reseau = will.getReseau();

		// Recuperer taille des images (variable selon le nombre d'imagettes
		// dans le "dictionnaire"

		int taille = will.getn();

		// Recuperer tailles des clusters

		int[] TaillesCluster = will.getTaillesCluster();
		int[] TaillesCumulées = will.getTaillesCumulées();

		// Recupere le dictionnaire des Imagettes

		ArrayList<ArrayList<Integer>> DicoImagettes = will.getImagettes();

		// On altère une des m images apprises : NumImageAlteree
		int[][] Images = will.getImages();

		ArrayList<Integer> ImageAlteree = new ArrayList<Integer>();
		

		// On efface Nefface niveaux de clusters
		for (int i = 0; i < nb_cluster - Nefface; i++) {
			ImageAlteree.add(Images[NumImageAlteree][i]);
			
		}
		
		int[] Scores = new int[taille];
				
		int [] ImageAltereeTableau=new int [ImageAlteree.size()];
		for (int i=0; i<ImageAlteree.size(); i++){
			ImageAltereeTableau[i]=ImageAlteree.get(i);
		}
		
		AfficherImageReconstruite affichage0=new AfficherImageReconstruite("ImageAltérée",ImageAltereeTableau,DicoImagettes,TaillesCumulées,0, pixels);
		affichage0.setVisible(true);


		int[] ImageReconstruite = new int[nb_cluster];
		PriorityQueue<Integer> minHeap = new PriorityQueue();
		
		// Reconstruction
		System.out.println("Reconstruction starts");
		long start = System.currentTimeMillis();
		// dynamic rule (SOS)
		
		Scores = new int[taille];
		int max = 0;
		for (int pixel=0; pixel<Scores.length; pixel++) {
			// memory
			if(ImageAlteree.contains(pixel)) Scores[pixel]=gamma;
			// field
			for (int e:Reseau.get(pixel)){
				if(ImageAlteree.contains(e)) Scores[pixel]= Scores[pixel]+1;		
			}
			if (Scores[pixel]>=max)max=Scores[pixel];
		}
		
		// PHASE 1
		
		// activation rule: GWTO: find the maximum score in the whole network, and keep the neurons that have this score
		
		ImageAlteree = new ArrayList<Integer>();
		ArrayList<Integer> ActiveFanals = new ArrayList<Integer>();
		for (int i=0; i<Scores.length; i++){
			if (Scores[i]==max){
			
				ImageAlteree.add(i);
				ActiveFanals.add(i);
			}
		}
		ImageAlteree.trimToSize(); 

		ActiveFanals.trimToSize(); 
		System.out.println("The number of active fanals in phase 1 is: "+ ActiveFanals.size());

		// dynamic rule
		Scores = new int[taille];

		for (int pixel=0; pixel<Scores.length; pixel++) {
			//System.out.println("pixel: "+pixel);
			// memory
			if(ImageAlteree.contains(pixel)) Scores[pixel]=gamma;
			// field
			for (int e:Reseau.get(pixel)){
				if(ImageAlteree.contains(e)) Scores[pixel]= Scores[pixel]+1;		
			}
			if (Scores[pixel]>=max)max=Scores[pixel];
		}
				
		// PHASE 2
		minHeap = new PriorityQueue<Integer>();
		int seuil_kicks_out;
		
		for (int iter=0; iter<Niter; iter++){
			System.out.println("iteration number: "+iter);
			seuil_kicks_out = 0;
			// activation rule: Kicks out
			ImageAlteree = new ArrayList<Integer>();
			System.out.println("number of active fanals: "+ActiveFanals.size());
			for (int e:ActiveFanals) {
				minHeap.offer(Scores[e]);
			}

			seuil_kicks_out = (Integer) minHeap.poll();
			//System.out.println("seuil: "+seuil_kicks_out);
			

			for(int e:ActiveFanals){
				if(Scores[e]>=seuil_kicks_out){
					ImageAlteree.add(e);
				}
			}
			ActiveFanals = new ArrayList<Integer>();
			for (int e:ImageAlteree){
				ActiveFanals.add(e);
			}
			
			ImageAlteree.trimToSize();
			ActiveFanals.trimToSize();
				
			
			// dynamic rule
			Scores = new int[taille];
			
			for (int pixel=0; pixel<Scores.length; pixel++) {
				// memory
				if(ImageAlteree.contains(pixel)) Scores[pixel]=gamma;
				// field
				for (int e:Reseau.get(pixel)){
					if(ImageAlteree.contains(e)) Scores[pixel]= Scores[pixel]+1;		
				}
				if (Scores[pixel]>=max)max=Scores[pixel];
			}
			
			//Afficher l'image à la ieme itération. 
			
			int [] ImagettesIter=new int [nb_cluster];
			
			//what if we have several candidates?
			int missing = TaillesCluster[0];
			
			for(int i=1; i<TaillesCumulées.length; i++){
				int count=0;
				missing=missing+TaillesCluster[i];
				for(int e:ImageAlteree){
					
					if (e<TaillesCumulées[i] && e>=TaillesCumulées[i-1]){
						ImagettesIter[i-1]=e;
						count+=1;
					}
				}
				if(count>1){
					System.out.println("conflict");
					ImagettesIter[i-1]=60000;
				}
				
			}
			
			int count=0;
			System.out.println("missing "+missing);
			for(int e:ImageAlteree){
				
				if (e<missing && e>=TaillesCumulées[TaillesCumulées.length-1]){
					ImagettesIter[ImagettesIter.length-1]=e;
					count+=1;
				}
			}
			if(count>1){
				System.out.println("conflict last");
				ImagettesIter[ImagettesIter.length-1]=60000;
			}
			
			/*for(int i=0; i<ImagettesIter.length; i++){
				System.out.println("result: "+ImagettesIter[i]);
			}*/
			
			
			AfficherImageReconstruite affichage_iter = new AfficherImageReconstruite("Voici l'image Reconstruite",
				ImagettesIter, DicoImagettes, TaillesCumulées,iter+1, pixels);
			System.out.println(iter);
			affichage_iter.setVisible(true);
			
		}// fin for iter

		long end = System.currentTimeMillis();
		
		long duration = end-start;
		System.out.println("the duration of the reconstruction is: "+duration+"ms");

		int[] ImagettesRouge = new int[nb_cluster];
		
		int missing = TaillesCluster[0];
		
		for(int i=1; i<TaillesCumulées.length; i++){
			int count=0;
			missing=missing+TaillesCluster[i];
			for(int e:ImageAlteree){
				
				if (e<TaillesCumulées[i] && e>=TaillesCumulées[i-1]){
					ImagettesRouge[i-1]=e;
					count+=1;
				}
			}
			if(count>1){
				System.out.println("conflict");
				ImagettesRouge[i-1]=60000;
			}
			
		}
		
		int count=0;
		for(int e:ImageAlteree){
			
			if (e<missing && e>=TaillesCumulées[TaillesCumulées.length-1]){
				ImagettesRouge[ImagettesRouge.length-1]=e;
				count+=1;
			}
		}
		if(count>1){
			System.out.println("conflict");
			ImagettesRouge[ImagettesRouge.length-1]=60000;
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
