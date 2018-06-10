import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class VecteursSIFTheap {

	public static void main(String[] args) {
		//ENTREE
		
		int nombreCompo1 = Integer.parseInt(args[0]);
		// vecteur dont on cherche le plus proche voisin
		int numVecteur = Integer.parseInt(args[1]);
		
		
		int[][] VecteursBinaire = new int[10000][512];
		double[][] Vecteurs = new double[10000][512];
		File f = new File("testMaud");
		FileReader fr;
		BufferedReader br;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);

			String str = " ";
			
			int ligne = 0;
			while ((str = br.readLine()) != null) {

				
				ArrayList<Double> tempList = new ArrayList<Double>();
				

				String[] tab = str.split(" ");

				int compteur = 0;
				int curseur = 0;
				for (String s : tab) {

					int i;
					Double d;
					d = Double.parseDouble(s);
					tempList.add(d);
					
					Vecteurs[ligne][curseur] = d;
					curseur++;
				}

				// seuil variable tel qu'on mette 5 valeurs à 1 pour chaque
				// vecteur
				PriorityQueue minHeap = new PriorityQueue();
				minHeap = new PriorityQueue();
				for (int j = 0; j < 512; j++) {

					minHeap.offer(-tempList.get(j));

				}

				double seuil = 0;

				for (int k = 0; k < nombreCompo1; k++) {

					double temp = (Double) minHeap.poll();

					seuil = -temp;

				}

				for (int j = 0; j < 512; j++) {

					if (tempList.get(j) >= seuil) {

						// tempListBinaire.add(1);
						compteur++;
						VecteursBinaire[ligne][j] = 1;

					} else
						VecteursBinaire[ligne][j] = 0;
					// tempListBinaire.add(0);

				}

				ligne++;
				// System.out.println("get="+tempList.size()+" "+"compteur="+compteur);

			}
			fr.close();
			br.close();
		} catch (IOException e1) {
		}

		// calcul distance de Hamming

		

		

		// vecteur qui gardera les distances

		int[] distanceBinaire = new int[10000];
		double[] distance = new double[10000];

		// distanceBinaire

		int MIN = 0;
		int MIN2 = 0;

		for (int vect = 0; vect < 10000; vect++) {
			int sommeBin = 0;
			double somme = 0;
			for (int ind = 0; ind < 512; ind++) {
				// (xi0-xj)
				int temp = VecteursBinaire[numVecteur][ind] - VecteursBinaire[vect][ind];
				double temp2 = Vecteurs[numVecteur][ind] - Vecteurs[vect][ind];
				// (xi0-xj)²
				temp = temp * temp;
				temp2 = temp2 * temp2;
				// (xi0-xj)²+(xi0-xj+1)²+...
				sommeBin = sommeBin + temp;
				somme = somme + temp2;

			}

			// d(xi0,x(vect))
			distanceBinaire[vect] = sommeBin;
			distance[vect] = somme;
			// min potentiel
			if (vect == 0)
				MIN = 0;

			else {
				if (distanceBinaire[vect] < distanceBinaire[MIN])
					MIN = vect;
				
			}

			if (vect == 0)
				MIN2 = 0;

			else {
				if (distance[vect] < distance[MIN2])
					MIN2 = vect;
			}

		}

		System.out.println("MINbinaire=" + MIN + " " + "MIN=" + MIN2);

	}
}

