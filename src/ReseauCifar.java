
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class ReseauCifar {

	public int m; // Number of images in memory
	public int[][] Images; // tableau de taille m * nb_cluster
	public ArrayList<ArrayList<Integer>> ReseauWillshaw;
    public RecuperationImageCIFAR recup;
	// paramètres pour l'effacement et la reconstruction
	public int Niter;
	public int NumImageAlteree;
	public int Nefface;

	// paramètres concernant le découpage de l'image
	
	// nombre de pièces, et aussi nombre de composantes non nulles pour une image
	public int pixels; 
	public int nb_cluster;
	
	// nombre de possibilités trouvées pour chaque pièce
	public int[] TaillesCluster; 
	
	// utile pour représenter l'image
	public int[] TaillesCumulées;  
	
	// Dictionnaire (i.e. ce qui a été trouvé pour chaque partie)
	public ArrayList<ArrayList<Integer>> Imagettes;
	
	// Taille des images apprises version 1,0 ( taille=SUM[taille(clusteur(i))] )
	public int n; 
	
	
	public ReseauCifar(int m, int pixels) throws IOException { 
		
		this.m = m;
		this.pixels = pixels;
		this.nb_cluster=1024/(pixels*pixels);
		// Recuperation de m images formatées 
		recup = new RecuperationImageCIFAR(m, pixels);
		this.Images = recup.getImages();

		// récupération des tailles des clusters
		TaillesCluster = recup.getTaillesCluster();
		TaillesCumulées = recup.getTaillesCumulées();
		
		// Calcul de n, car avec les imagettes n dépend des m images récupérées
		int taille = 0;

		for (int cluster = 0; cluster < nb_cluster; cluster++) {
			taille = taille + TaillesCluster[cluster];

		}

		this.n = taille;

		// récupération des imagettes enregistrées (i.e. contient les niveaux de gris pour chaque imagette)
		Imagettes = recup.getImagettes();

		// instancier le Reseau
		this.ReseauWillshaw = new ArrayList<ArrayList<Integer>>(n);

		for (int i = 0; i < n; i++) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			ReseauWillshaw.add(temp);
		}
		System.out.println("taille = " + ReseauWillshaw.size());

	}// fin constructeur



	public void GenererReseauWillshaw() {
		int nb_connections = 0;
		for (int k = 0; k < m; k++) // for all products
		{
		
			for (int i = 0; i < nb_cluster; i++) // for all clusters
			{
				for (int j = 0; j < nb_cluster; j++) // Pour tout j
				{
					if (!ReseauWillshaw.get(Images[k][i]).contains(Images[k][j])) {
						ReseauWillshaw.get(Images[k][i]).add(Images[k][j]);
						nb_connections++;
					}
				}
			}
		}
		System.out.println("Number of connections in the network: " +nb_connections);
	}

	public int[][] getImages() {
		return Images;
	}

	public int[] getTaillesCluster() {
		return TaillesCluster;
	}

	public int[] getTaillesCumulées() {
		return TaillesCumulées;
	}

	public ArrayList<ArrayList<Integer>> getReseau() {
		return ReseauWillshaw;

	}

	public int getn() {
		return n;
	}

	public ArrayList<ArrayList<Integer>> getImagettes() {
		return Imagettes;
	}
}
