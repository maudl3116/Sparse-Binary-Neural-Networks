
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

//Le but est de lire dans le fichier binaire les images, et de les mettre dans le format qui nous intéresse.

public class RecuperationImageCIFAR {

	public int[][] RGB = new int[32][32];
	public int m;
	public int[] TaillesCluster;
	public int[] TaillesCumulées;

	// constantes afin de fixer la taille des imagettes (8*8 ou 4*4 par exemple)
	public int d = 8; // nombre de cluster par ligne
	public int cote = 4;
	public int nb_cluster = 64;
	public int taille_cluster = cote * cote;

	public int[][] Images; // une ligne contient numéro de l'imagette présente pour chaque clusters
	private FileInputStream fis;
	public ArrayList<ArrayList<Integer>> imagettes; // notre dictionnaire d'imagettes

	// CONSTRUCTEUR

	public RecuperationImageCIFAR(int m) throws IOException {

		this.m = m;
		Images = new int[m][nb_cluster];
		createImage(LireFichierBinaire("./data_batch_1.bin"));
	}

	// METHODE DE CLASSE

	private int[][] createImage(byte[] tab) throws IOException {

		// 0----création d'une liste de listes qui contient toutes les imagettes
		// possibles
		// ligne : toutes les imagettes différentes rencontrées pour un cluster
		// colonne : tous les clusters 

		imagettes = new ArrayList<ArrayList<Integer>>(nb_cluster);

		
		// 0bis----création d'un tableau qui garde en mémoire les numéros des
		// imagettes pour tous les cluster, pour toute image.

		this.TaillesCluster = new int[nb_cluster];

		for (int u = 0; u < m; u++) { // pour chaque image apprise

			// 1----recupération niveaux de gris dans RGB
			for (int k = 0; k < 32; k++) {
				for (int l = 0; l < 32; l++) {
					RGB[k][l] = tab[32 * k + l + 1 + 3073 * u + 0 * 1024];
				}
			}

			// 2----quantification
			for (int i = 0; i < 32; i++) {
				for (int j = 0; j < 32; j++) {
					int temp = (int) ((RGB[i][j] + 128) / taille_cluster);
					RGB[i][j] = (temp * taille_cluster); 
				}
			}

			// 3----Compter le nombre d'imagettes différentes pour chaque
			// cluster ET les stocker dans un tableau (pour la reconstruction)

			// --a--parcourir les clusters (k-ligne // l-colonne) k*d+l est le
			// numéro du cluster
			for (int k = 0; k < d; k++) {
				for (int l = 0; l < d; l++) {
					// --b--mettre les éléments de RGB qui correspondent au
					// cluster fixé (k,l) en ligne
					int[] RGBligne = new int[taille_cluster];

					for (int i = 0; i < cote; i++) {
						for (int j = 0; j < cote; j++) {
							RGBligne[i * cote + j] = RGB[k * cote + i][l * cote + j];
						}
					}
					// --c--pour un cluster fixé (k,l) ET une image (u) à
					// apprendre : comparer son cluster aux imagettes déjà
					// enregistrées pour ce cluster

					// si on apprend pour la première fois une image (u=0) alors
					// l'imagette est automatiquement ajoutée
					
					int curseurimagette = 0; //curseur qui se positionne sur une imagette 
					if (u == 0) {
						ArrayList a = new ArrayList<Integer>();
						
						for (int i = 0; i < taille_cluster; i++) {
							a.add(RGBligne[i]);
						}
						
						imagettes.add(a);
						
						Images[u][k * d + l] = 0; // on retient les imagettes pour l'image u=0
					}

					else {

						int curseurimagettes = 0; // positionne au début d'une imagette
						while (curseurimagettes * taille_cluster < imagettes.get(k * d + l).size()) {
							int curseur = 0; // curseur qui parcourt les pixels de l'imagette 
							
							//tant qu'on ne trouve pas de différence on compare
							while (RGBligne[curseur] == imagettes.get(k * d + l).get(
									curseur + curseurimagettes * taille_cluster)) {
								curseur++;
								// si on arrive au bout de l'imagette, cela signifie qu'on l'a trouvée à l'identique, donc il faut sortir
								if (curseur == taille_cluster)
									break;
							}
							// l'imagette a été trouvée à l'identique donc on sort 
							if (curseur == taille_cluster)
								break;
							// l'imagette est potentiellement nouvelle, donc on passe à la comparaison avec l'imagette suivante
							else
								curseurimagettes++;
						}
						
						// enregistrement dans le dictionnaire "imagettes" et dans "Images"

						if (curseurimagettes * taille_cluster == imagettes.get(k * d + l).size()) {
							Images[u][k * d + l] = curseurimagettes;
							for (int i = 0; i < taille_cluster; i++) {
								imagettes.get(k * d + l).add(RGBligne[i]);
							}
						} else {
							Images[u][k * d + l] = curseurimagettes;
						}

					}// fin else u!=0

				}// fin for k
			}// fin for l

		} // fin u qui parcourt toutes les images

		
		// 4---Pour le lot d'images à apprendre, on a recensé toutes les
		// imagettes présentes (dans au moins une image) pour chaque cluster
		// ----on a par la meme occasion représenté toute image apprise sous
		// forme de numéro d'imagettes

		
		// 5---La représentation n'est pas encore la bonne, car les numéros
		// contenus dans Images ne permettent pas de l'écrire comme vecteur
		// contenant des 0 et des 1.
		// ----Pour ce faire il faut savoir combien de possibilités ont été
		// rencontrées pour chaque cluster

		// --a--Chercher les max dans chaque cluster, donc chaque ligne de
		// imagettes

		

		for (int cluster = 0; cluster < nb_cluster; cluster++) {
			TaillesCluster[cluster] = imagettes.get(cluster).size();
			TaillesCluster[cluster] = TaillesCluster[cluster] / taille_cluster;
		}

		TaillesCumulées = new int[nb_cluster];
		TaillesCumulées[0] = 0;
		for (int i = 0; i < nb_cluster - 1; i++) {
			TaillesCumulées[i + 1] = TaillesCumulées[i] + TaillesCluster[i];
		}

		// --b--ajouter à chaque ligne le cumul précédent
		for (int cluster = 1; cluster < nb_cluster; cluster++) {

			for (int i = 0; i < m; i++) {
				Images[i][cluster] = Images[i][cluster] + TaillesCumulées[cluster];
			}
		}
		
		// --c--Les images sont pretes à etre mémorisées dans le réseau
		return Images;
	
	}// fin create

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
		System.out.println(m + " " + imagettes.get(2).size()); 

		return imagettes;

	}

	public byte[] LireFichierBinaire(String nomFichier) throws IOException {

		File fichier = new File(nomFichier);
		byte[] tab = new byte[(int) fichier.length()];
		fis = new FileInputStream(fichier);
		fis.read(tab, 0, (int) fichier.length());
		return tab;
	}

}
