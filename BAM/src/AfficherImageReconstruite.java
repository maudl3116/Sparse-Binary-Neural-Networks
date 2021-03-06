
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

import java.util.PriorityQueue;

public class AfficherImageReconstruite extends JFrame {

	private static final long serialVersionUID = 1L;

	// constantes afin de fixer la taille des imagettes (8*8 ou 4*4 par exemple)

	public int pixels;
	public int taille_cluster;
	public int d;
	public int nb_cluster;
	
	public int enr;
	

	public AfficherImageReconstruite(String titre, int[] ImagettesRouge,
			ArrayList<ArrayList<Integer>> DicoImagettes, int[] TaillesCumulées,int enr, int pixels) throws IOException {
		super(titre);
		this.pixels = pixels;
		this.taille_cluster = pixels * pixels;
		this.nb_cluster = 1024/taille_cluster;
		this.d = 32/pixels;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String img = createImage(ImagettesRouge, DicoImagettes, TaillesCumulées,enr);
		init(img);
	}

	

	private void init(String img) {
		JLabel label = new JLabel(new ImageIcon(img));
		this.add(label, BorderLayout.CENTER);
		this.pack();
	}



	private String createImage(int[] ImagettesRouge, ArrayList<ArrayList<Integer>> DicoImagettes,
			int[] TaillesCumulées,int enr) throws IOException {

		// Récupération des niveaux de rouge pour chaque cluster

		int[][] r = new int[32][32];

		// pour tout cluster (k*4+l)
		for (int k = 0; k < d; k++) {
			for (int l = 0; l < d; l++) {
				// récupérer les cote*cote pixels de l'imagette choisie
				for (int i = 0; i < pixels; i++) {
					for (int j = 0; j < pixels; j++) {
						// commencer la lecture du dictionnaire à curseur

						// il faut convertir
						// ImagettesRouge en numéro de cluster entre 0 et 16,
						// i.e. effectuer la transformation inverse à celle de
						// RecuperationImageCIFAR
						
						// Gérer le cas où il manque des informations sur les clusters, et où on a un conflit.
												
						if((k*d+l)>ImagettesRouge.length-1 || ImagettesRouge[k*d+l]==60000 ){
						r[k * pixels + i][l * pixels + j] = - 128;
						//System.out.println("error");
						}
						else{ 
							int curseur = (ImagettesRouge[k * d + l] - TaillesCumulées[k * d + l])
									* taille_cluster;

							//System.out.println("cluster: "+(k*d+l));
							r[k * pixels + i][l * pixels + j] = DicoImagettes.get(k * d + l).get(
									curseur + i * pixels + j);
							
							r[k * pixels + i][l * pixels + j] = r[k * pixels + i][l * pixels + j] - 128;
						}
					}
				}
			}
		}// fin for cluster k*d+l

		// Création de l'image avec recombinaison des niveaux de RGB

		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < 32; y++) {
			for (int x = 0; x < 32; x++) {
				int rgb = (r[y][x]) << 16;

				image.setRGB(x, y, rgb);
			}
		}
		// Ecriture de l'image dans un fichier
		File outputFile = new File("./cifar"+enr+".png");
		ImageIO.write(image, "png", outputFile);
		return "./cifar.png";

	}// fin Méthode createImage

	
}// fin classe AfficherImageReconstruite
