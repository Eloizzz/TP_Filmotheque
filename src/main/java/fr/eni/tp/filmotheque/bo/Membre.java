package fr.eni.tp.filmotheque.bo;

public class Membre extends Personne {
	/**
	 * Numéro de sérialisation
	 */
	private static final long serialVersionUID = 1L;
	private String pseudo;
	private String motDePasse;
	private boolean admin;

	public Membre() {
	}

	public Membre(String nom, String prenom, String pseudo, String motDePasse) {
		super(nom, prenom);
		this.pseudo = pseudo;
		this.motDePasse = motDePasse;
	}

	public Membre(long id, String nom, String prenom, String pseudo, String motDePasse) {
		super(id, nom, prenom);
		this.pseudo = pseudo;
		this.motDePasse = motDePasse;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString());
		builder.append(" - Membre (pseudo=");
		builder.append(pseudo);
		builder.append(", admin=");
		builder.append(admin);
		builder.append(") ");
		return builder.toString();
	}

}
