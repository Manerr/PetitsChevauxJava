package fr.huautleroux.petitschevaux;
import fr.huautleroux.petitschevaux.core.Partie;
import fr.huautleroux.petitschevaux.utils.SaveManager;

public class PetitsChevaux {

	private Partie partie;
	private SaveManager saveManager;

	public PetitsChevaux() {
		this.saveManager = new SaveManager("saves");
		this.partie = new Partie();
		partie.initialiserJeu();

		testSaveLoadPartie("test");
	}

	private void testSaveLoadPartie(String saveName) {
		System.out.println("Sauvegarde sous le nom : " + saveName);
		saveManager.sauvegarderPartie(partie, saveName);
		
		System.out.println("Chargement de : " + saveName);
		Partie partieTest = saveManager.chargerPartie(saveName);
		
		System.out.println(" Joueurs :");
		partieTest.getJoueurs().forEach(get -> System.out.println("    " + get.getNom() + " " + get.getCouleur()));
		
		System.out.println(" Liste des sauvegardes :");
		saveManager.getSauvegardes().forEach(save -> System.out.println("    " + save));
	}

	public static void main(String[] args) {
		new PetitsChevaux();
	}

}
