package fr.huautleroux.petitschevaux.affichage.console;

import java.io.IOException;
import java.util.HashMap;

import fr.huautleroux.petitschevaux.affichage.AffichageInterface;
import fr.huautleroux.petitschevaux.cases.CaseEchelle;
import fr.huautleroux.petitschevaux.cases.CaseEcurie;
import fr.huautleroux.petitschevaux.cases.abstracts.Case;
import fr.huautleroux.petitschevaux.core.GestionPartie;
import fr.huautleroux.petitschevaux.core.Partie;
import fr.huautleroux.petitschevaux.core.Plateau;
import fr.huautleroux.petitschevaux.entites.Pion;
import fr.huautleroux.petitschevaux.entites.abstracts.Joueur;
import fr.huautleroux.petitschevaux.enums.Couleur;
import fr.huautleroux.petitschevaux.enums.SauvegardeResultat;
import fr.huautleroux.petitschevaux.exceptions.ChargementSauvegardeException;
import fr.huautleroux.petitschevaux.exceptions.SauvegardeException;
import fr.huautleroux.petitschevaux.save.GestionSauvegarde;

public class IConsole implements AffichageInterface {

	private GestionSauvegarde gestionSauvegarde = new GestionSauvegarde();

	public void start() {
		GestionPartie gestionPartie;
		boolean nouvellePartie = true;

		try {
			gestionPartie = menuChargementSauvegarde();
			nouvellePartie = false;
		} catch (ChargementSauvegardeException e) {
			gestionPartie = new GestionPartie(this);
		}

		gestionPartie.demarrerPartie(nouvellePartie);
	}

	public void effacerAffichage() {
		String os = System.getProperty("os.name");

		try {
			if (os.contains("Windows"))
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			else
				System.out.print("\033[H\033[2J");
		} catch(IOException | InterruptedException e) {}
	}

	public void debutTour(int numeroTour) {
		System.out.println(CCouleurs.PURPLE_BRIGHT + "TOUR N°" + numeroTour + CCouleurs.RESET);
	}

	public int getNombreJoueurs() {
		int nbJoueur;

		do {
			System.out.print("Entrez le nombre de joueurs qui vont participer : ");
			nbJoueur = Saisie.asInt();
			System.out.println("");
		} while (nbJoueur > 4 || nbJoueur < 0);

		return nbJoueur;
	}

	public int getDeTruque(int de) {
		int deTruque;

		System.out.println("Dé original : " + de);

		do {
			System.out.print("Entrez la valeur du dé truquée : ");
			deTruque = Saisie.asInt();
			System.out.println("");
		} while (deTruque < 0 || deTruque > 999);

		return deTruque;
	}

	public SauvegardeResultat menuSauvegarde(Partie partie) throws SauvegardeException {
		System.out.println("Entrez le nom souhaité pour la sauvegarde");
		String nomSauvegarde = Saisie.asStringNoEmpty();
		nomSauvegarde = gestionSauvegarde.convertSaveName(nomSauvegarde);
		boolean overwrite = false;

		if (gestionSauvegarde.estSauvegardeValide(nomSauvegarde)) {
			System.out.print("Une sauvegarde existe avec ce nom, souhaitez-vous l'écraser ? O(ui) / N(on) : ");
			overwrite = Saisie.asBoolean();
			System.out.println("");
		}

		gestionSauvegarde.sauvegarderPartie(partie, nomSauvegarde, overwrite);
		System.out.println("La partie a été sauvegarde sur le slot " + nomSauvegarde);

		System.out.print("Souhaitez-vous quitter la partie en cours ? O(ui) / N(on) : ");
		boolean stopPartie = Saisie.asBoolean();
		System.out.println("");

		return stopPartie ? SauvegardeResultat.QUITTER : SauvegardeResultat.CONTINUER;
	}

	public GestionPartie menuChargementSauvegarde() throws ChargementSauvegardeException {
		if(gestionSauvegarde.getSauvegardes().isEmpty())
			throw new ChargementSauvegardeException("Aucune sauvegarde n'existe");

		System.out.print("Souhaitez-vous charger une sauvegarde ? O(ui) / N(on) : ");
		boolean chargerSauvegarde = Saisie.asBoolean();
		System.out.println("");

		if(!chargerSauvegarde)
			throw new ChargementSauvegardeException("Opération interrompue");

		System.out.println(" Liste des sauvegardes :");
		gestionSauvegarde.getSauvegardes().forEach(save -> System.out.println("    • " + save));

		String nomSauvegarde;

		do {
			System.out.print("Choisissez la sauvegarde à charger (Tappez stop pour annuler) : ");
			nomSauvegarde = Saisie.asStringNoEmpty();
			System.out.println("");
		} while(!gestionSauvegarde.estSauvegardeValide(nomSauvegarde) && !nomSauvegarde.equals("stop"));

		if(nomSauvegarde.equals("stop"))
			throw new ChargementSauvegardeException("Opération interrompue");

		nomSauvegarde = gestionSauvegarde.convertSaveName(nomSauvegarde);

		GestionPartie gererPartie = gestionSauvegarde.chargerPartie(this, nomSauvegarde);
		System.out.println("La partie " + nomSauvegarde + " a été chargée\n");
		return gererPartie;
	}

	public void tirageAuSort(Couleur couleur, String nomJoueur, Runnable callback) {
		System.out.println(couleur.getTextCouleurIC() + "Tirage aléatoire : ");
		System.out.println("C'est " + nomJoueur + " qui commence en premier !" + CCouleurs.RESET);
		System.out.println("\nAppuyer sur Entrer pour continuer");

		attendreToucheEntrer(() -> callback.run());
	}

	public void attendreToucheEntrer(Runnable callback) {
		Saisie.asString();
		callback.run();
	}

	public void simpleMessage(String msg, String color) {
		if (color != null)
			System.out.println(color + msg + CCouleurs.RESET);
		else
			System.out.println(msg);
	}

	public void finDePartie(int numeroTour, Joueur joueurGagnant) {
		System.out.println(CCouleurs.PURPLE_BRIGHT + "FIN DE PARTIE\n\n" + CCouleurs.RESET);
		System.out.println(joueurGagnant.getCouleur().getTextCouleurIC() + joueurGagnant + " gagne la partie en " + numeroTour + " tours\n\n" + CCouleurs.RESET);
		System.out.println("\nAppuyer sur Entrer pour relancer une partie");

		attendreToucheEntrer(() -> start());
	}

	public HashMap<String, Couleur> getInitialisationJoueurs(int nbJoueurHumain) {
		HashMap<String, Couleur> joueurs = new HashMap<String, Couleur>();
		System.out.println("Couleur : J(aune) / B(leu) / V(ert) / R(ouge)");

		for (int i = 0; i < nbJoueurHumain; i++) {
			System.out.println("Nouveau Joueur");

			String pseudo;

			do {
				System.out.print("  Entrez votre pseudo : ");
				pseudo = Saisie.asStringNoEmpty();
			} while (joueurs.containsKey(pseudo));

			Couleur couleur;

			do {
				System.out.print("  Entrez la couleur que vous souhaitez : ");
				couleur = Saisie.asCouleur(this);
			} while (joueurs.containsValue(couleur));

			System.out.println("");
			joueurs.put(pseudo, couleur);
		}

		return joueurs;
	}

	public void miseAJourAffichage(Plateau plateau) {
		Couleur couleurCourant = plateau.getPartie().getJoueurCourant().getCouleur();

		HashMap<Case, String> alias = new HashMap<Case, String>();
		HashMap<Couleur, Integer> aliasCompteur = new HashMap<Couleur, Integer>();
		for (Couleur c : Couleur.values())
			aliasCompteur.put(c, 0);


		for (int y = 0; y < 15; y++) {
			for (int x = 0; x < 15; x++) {
				Case caseCible = plateau.getCaseParCordonnee(x, y);

				if (caseCible == null) {
					System.out.print("  ");
					continue;
				}

				if (caseCible instanceof CaseEcurie) {
					int numeroCheval = 0;
					numeroCheval += x <= 3 ? x%2 : x%11;
					int yTemp = (y <= 3 ? y%2 : y%11);
					numeroCheval += yTemp;

					if (yTemp != 0)
						numeroCheval++;

					Pion p = null;

					for (Pion pGet : caseCible.getChevaux())
						if (pGet.getId() == numeroCheval)
							p = pGet;

					if (p != null)
						if (p.getCouleur().equals(couleurCourant))
							System.out.print(p.getId() + 1);
						else
							System.out.print(p.getCouleur().name().charAt(0));
					else
						System.out.print(".");
				} else {
					String numeroCases = "";

					for (Pion p : caseCible.getChevaux()) {
						if (numeroCases.isEmpty()) {
							if (p.getCouleur().equals(couleurCourant))
								numeroCases = "" + (p.getId() + 1);
							else
								numeroCases = "" + p.getCouleur().name().charAt(0);
						}
						else {
							String lettreInfos;

							if (alias.containsKey(caseCible)) {
								lettreInfos = alias.get(caseCible);
								lettreInfos += ", " + (p.getId() + 1);
							} else {
								int compteur = aliasCompteur.get(p.getCouleur());
								int lettreNumero = 'C' + (3 * p.getCouleur().ordinal()) + compteur;

								if (lettreNumero > 'I') // Pour ne pas avoir le J, qui peut faire croire que c'est un pion Jaune
									lettreNumero++;

								String lettre = "" + (char) (lettreNumero);
								lettreInfos = lettre + " - " + p.getCouleur() + " : " + (caseCible.getChevaux().get(0).getId() + 1) + ", " + (p.getId() + 1);
								numeroCases = lettre;
								aliasCompteur.put(p.getCouleur(), compteur + 1);
							}

							alias.put(caseCible, lettreInfos);
						}
					}

					if (!numeroCases.isEmpty())
						System.out.print(numeroCases);
					else if (caseCible instanceof CaseEchelle)
						System.out.print(((CaseEchelle) caseCible).getSymbol());				
					else
						System.out.print("*");
				}

				System.out.print(" ");
			}

			System.out.println(" ");
		}

		if (!alias.isEmpty()) {
			System.out.println(" ");

			System.out.println("Vos pions :");
			for (String value : alias.values())
				if (value.toUpperCase().contains(couleurCourant.name()))
					System.out.println("  " + value);

			System.out.println("\nAutres pions :");

			for (String value : alias.values())
				if (!value.toUpperCase().contains(couleurCourant.name()))
					System.out.println("  " + value);
		}

		System.out.println("");
	}
}
