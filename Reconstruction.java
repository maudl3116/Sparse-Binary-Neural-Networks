
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class Reconstruction{

public static void main(String[] args) throws IOException {

		int m = Integer.parseInt(args[0]);
		int NumImageAlteree = Integer.parseInt(args[1]);
		int Nefface = Integer.parseInt(args[2]);
		int Niter = Integer.parseInt(args[3]);
		
		
		int d = 8; 
		int cote = 4;
		int nb_cluster = 64;
		int taille_cluster = cote * cote;

		// POUR LE ROUGE

		// Récupération pour m images des valeurs des pixels pour une seule
		// couleur
		ReseauCifar will = new ReseauCifar(m);
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
		
		//Afficher l'image altérée
		
		int [] ImageAltereeTableau=new int [ImageAlteree.size()];
		for (int i=0; i<ImageAlteree.size(); i++){
			ImageAltereeTableau[i]=ImageAlteree.get(i);
		}
		
		AfficherImageReconstruite affichage0=new AfficherImageReconstruite("ImageAltérée",ImageAltereeTableau,DicoImagettes,TaillesCumulées,0);
		affichage0.setVisible(true);


		// A la fin des itérations : ImageReconstruite contient une valeur par cluster
		int[] ImageReconstruite = new int[nb_cluster];

		// Tableau temporaire remis à zéro lors d'une itération : ImageTemp
		// contient les scores 
		int[] ImageTemp = new int[taille];

		// Reconstruction

		for (int iter = 0; iter < Niter; iter++) {
			//minHeap = new PriorityQueue();

			// remise à zéro des scores 
			for (int i = 0; i < taille; i++) {
				ImageTemp[i] = 0;
			}
			
			// Mise à jour des scores
			for (int pixel = 0; pixel < taille; pixel++) {
				for (int allume = 0; allume < ImageAlteree.size(); allume++) {

					if (Reseau.get(pixel).contains(ImageAlteree.get(allume))) {

						ImageTemp[pixel] = ImageTemp[pixel] + 1;
					}
				}
			}
			
			
			
			/*
			for (int pixel = 0; pixel < taille; pixel++) {
				minHeap.offer(100000 - ImageTemp[pixel]);
			}

			int seuil = 0;

			for (int i = 0; i < nb_cluster; i++) {
				int temp = (Integer) minHeap.poll();
				seuil = 100000 - temp;
			}

			// Réinitialisation de l'image pour n'y stocker que les niveaux dont
			// le score est supérieur au seuil
			ImageAlteree = new ArrayList<Integer>();

			for (int i = 0; i < ImageTemp.length; i++) {
				if (ImageTemp[i] >= seuil) {

					ImageAlteree.add(i);
				}
			}*/
			
			ImageAlteree = new ArrayList<Integer>();
			for (int cluster=0; cluster<nb_cluster; cluster++){
				int Max=0;
				int Conflit=0;
				for (int index=0; index<TaillesCluster[cluster]; index++){
					if (ImageTemp[TaillesCumulées[cluster]+index]>Max) {Max=TaillesCumulées[cluster]+index;}
					if (ImageTemp[TaillesCumulées[cluster]+index]==Max && iter==Niter-1) {Conflit=1;}
				}
				if (Conflit==1){
					ImageAlteree.add(60000);
				}
				else{ImageAlteree.add(Max);
				}
			}
			
			
			
			ImageAlteree.trimToSize(); 
			//System.out.println("taille ImageAlteree"+ImageAlteree.size());
			
			//Afficher l'image à la ieme itération.
			
			int [] ImagettesIter=new int [ImageAlteree.size()];
			
			for (int i=0; i<ImageAlteree.size(); i++){
				ImagettesIter[i]=ImageAlteree.get(i);
			}
			
			AfficherImageReconstruite affichage_iter = new AfficherImageReconstruite("Voici l'image Reconstruite",
				ImagettesIter, DicoImagettes, TaillesCumulées,iter+1);
			affichage_iter.setVisible(true);
			
		}// fin for iter

		

		int[] ImagettesRouge = new int[nb_cluster];
		for (int i = 0; i < nb_cluster; i++) {
			ImagettesRouge[i] = ImageAlteree.get(i);
		}

		AfficherImageReconstruite affichage_final = new AfficherImageReconstruite("Voici l'image Reconstruite",
				ImagettesRouge, DicoImagettes, TaillesCumulées,Niter+1);
		affichage_final.setVisible(true);

	}
}
