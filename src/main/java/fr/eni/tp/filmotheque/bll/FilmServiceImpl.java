package fr.eni.tp.filmotheque.bll;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import fr.eni.tp.filmotheque.bo.Avis;
import fr.eni.tp.filmotheque.bo.Film;
import fr.eni.tp.filmotheque.bo.Genre;
import fr.eni.tp.filmotheque.bo.Membre;
import fr.eni.tp.filmotheque.bo.Participant;
import fr.eni.tp.filmotheque.dal.AvisDAO;
import fr.eni.tp.filmotheque.dal.FilmDAO;
import fr.eni.tp.filmotheque.dal.GenreDAO;
import fr.eni.tp.filmotheque.dal.MembreDAO;
import fr.eni.tp.filmotheque.dal.ParticipantDAO;
import fr.eni.tp.filmotheque.exceptions.BusinessCode;
import fr.eni.tp.filmotheque.exceptions.BusinessException;

@Service
@Primary
public class FilmServiceImpl implements FilmService {
	// Injection des Repository
	private FilmDAO filmDAO;
	private GenreDAO genreDAO;
	private ParticipantDAO participantDAO;
	private AvisDAO avisDAO;
	private MembreDAO membreDAO;

	public FilmServiceImpl(FilmDAO filmDAO, GenreDAO genreDAO, ParticipantDAO participantDAO, AvisDAO avisDAO,
			MembreDAO membreDAO) {
		this.filmDAO = filmDAO;
		this.genreDAO = genreDAO;
		this.participantDAO = participantDAO;
		this.avisDAO = avisDAO;
		this.membreDAO = membreDAO;

	}

	@Override
	public List<Film> consulterFilms() {
		// Il faut remonter la liste des films
		List<Film> films = filmDAO.findAll();

		// Puis si cette liste n'est pas vide -- pour chaque film associé son genre et
		// son réalisateur
		if (films != null) {
			films.forEach(f -> {
				chargerGenreEtRealisateur1Film(f);
			});
		}
		return films;
	}

	@Override
	public Film consulterFilmParId(long id) {
		// Récupération d'un film par son identifiant
		Film f = filmDAO.read(id);

		if (f != null) {
			// Charger le genre et le réalisateur du film
			chargerGenreEtRealisateur1Film(f);
			// Charger la liste des acteurs
			List<Participant> acteurs = participantDAO.findActeurs(id);
			if (acteurs != null) {
				f.setActeurs(acteurs);
			}

			// Charger la liste des avis s'il y en a
			List<Avis> avis = avisDAO.findByFilm(id);
			if (avis != null) {
				// Association avec le membre
				avis.forEach(a -> {
					chargerMembre1Avis(a);
				});
				f.setAvis(avis);
			}
		}
		return f;
	}

	/**
	 * Méthode privée pour centraliser l'association entre un film et son genre et
	 * réalisateur
	 * 
	 * @param film
	 */
	private void chargerGenreEtRealisateur1Film(Film f) {
		Participant realisateur = participantDAO.read(f.getRealisateur().getId());
		f.setRealisateur(realisateur);
		Genre genre = genreDAO.read(f.getGenre().getId());
		f.setGenre(genre);
	}

	@Override
	public List<Genre> consulterGenres() {
		return genreDAO.findAll();
	}

	@Override
	public List<Participant> consulterParticipants() {
		return participantDAO.findAll();
	}

	@Override
	public Genre consulterGenreParId(long id) {
		return genreDAO.read(id);
	}

	@Override
	public Participant consulterParticipantParId(long id) {
		return participantDAO.read(id);
	}

	@Override
	public void creerFilm(Film film) {
		BusinessException be = new BusinessException();
		boolean isValid = true;
		isValid &= validerFilm(film, be);
		isValid &= validerTitre(film.getTitre(), be);
		isValid &= validerFilmUnique(film.getTitre(), be);
		isValid &= validerAnnee(film.getAnnee(), be);
		isValid &= validerGenre(film.getGenre(), be);
		isValid &= validerRealisateur(film.getRealisateur(), be);
		isValid &= validerActeurs(film.getActeurs(), be);
		isValid &= validerDuree(film.getDuree(), be);
		isValid &= validerSynopsis(film.getSynopsis(), be);
		if (isValid) {
			filmDAO.create(film);

			long idFilm = film.getId();
			film.getActeurs().forEach(p -> {
				participantDAO.createActeur(p.getId(), idFilm);
			});
		} else {
			throw be;
		}
	}

	@Override
	public String consulterTitreFilm(long id) {
		return filmDAO.findTitre(id);
	}

	@Override
	public void publierAvis(Avis avis, long idFilm) {
		BusinessException be = new BusinessException();
		boolean isValid = true;
		isValid &= validerAvis(avis, be);
		isValid &= validerNote(avis.getNote(), be);
		isValid &= validerCommentaire(avis.getCommentaire(), be);
		isValid &= validerMembre(avis.getMembre(), be);
		isValid &= validerIdFilm(idFilm, be);
		isValid &= validerMembreAvisFilm(idFilm, avis.getMembre().getId(), be);
		
		if (isValid) {
			try {
				avisDAO.create(avis, idFilm);
			} catch (DataAccessException e) {
				be.add(BusinessCode.BLL_AVIS_CREER_ERREUR);
				throw be;
			}
		} else {
			throw be;
		}
	}

	

	@Override
	public List<Avis> consulterAvis(long idFilm) {
		List<Avis> avis = avisDAO.findByFilm(idFilm);
		if (avis != null) {
			// Association avec le membre
			avis.forEach(a -> {
				chargerMembre1Avis(a);
			});
		}
		return avis;
	}

	/**
	 * Méthode privée pour centraliser l'association entre un avis et son membre
	 * 
	 * @param Avis
	 */
	private void chargerMembre1Avis(Avis a) {
		Membre membre = membreDAO.read(a.getMembre().getId());
		a.setMembre(membre);
	}

	private boolean validerFilm(Film f, BusinessException be) {
		if (f == null) {
			be.add(BusinessCode.VALIDATION_FILM_NULL);
			return false;
		}
		return true;
	}

	private boolean validerTitre(String titre, BusinessException be) {
		if (titre == null || titre.isBlank()) {
			be.add(BusinessCode.VALIDATION_FILM_TITRE_BLANK);
			return false;
		}
		if (titre.length() > 250) {
			be.add(BusinessCode.VALIDATION_FILM_TITRE_LENGTH);
			return false;
		}
		return true;
	}

	private boolean validerGenre(Genre genre, BusinessException be) {
		if (genre == null) {
			be.add(BusinessCode.VALIDATION_FILM_GENRE_NULL);
			return false;
		}
		if (genre.getId() <= 0) {
			be.add(BusinessCode.VALIDATION_FILM_GENRE_ID_INCONNU);
			return false;
		}
		try {
			Genre genreEnBase = genreDAO.read(genre.getId());
			if (genreEnBase == null) {
				be.add(BusinessCode.VALIDATION_FILM_GENRE_ID_INCONNU);
				return false;
			}
		} catch (DataAccessException e) {
			be.add(BusinessCode.VALIDATION_FILM_GENRE_ID_INCONNU);
			return false;
		}
		return true;
	}

	private boolean validerRealisateur(Participant participant, BusinessException be) {
		if (participant == null) {
			be.add(BusinessCode.VALIDATION_FILM_REALISATEUR_NULL);
			return false;
		}
		if (participant.getId() <= 0) {
			be.add(BusinessCode.VALIDATION_FILM_REALISATEUR_ID_INCONNU);
			return false;
		}
		try {
			Participant participantEnBase = participantDAO.read(participant.getId());
			if (participantEnBase == null) {
				be.add(BusinessCode.VALIDATION_FILM_ACTEUR_ID_INCONNU);
				return false;
			}
		} catch (DataAccessException e) {
			be.add(BusinessCode.VALIDATION_FILM_ACTEUR_ID_INCONNU);
			return false;
		}
		return true;
	}

	private boolean validerActeurs(List<Participant> acteurs, BusinessException be) {
		if (acteurs == null || acteurs.isEmpty()) {
			return true;
		}
		for (Participant participant : acteurs) {
			if (participant.getId() <= 0) {
				be.add(BusinessCode.VALIDATION_FILM_ACTEUR_ID_INCONNU);
				return false;
			} else {
				try {
					Participant participantEnBase = participantDAO.read(participant.getId());
					if (participantEnBase == null) {
						be.add(BusinessCode.VALIDATION_FILM_REALISATEUR_ID_INCONNU);
						return false;
					}
				} catch (DataAccessException e) {
					be.add(BusinessCode.VALIDATION_FILM_REALISATEUR_ID_INCONNU);
					return false;
				}
			}
		}
		return true;
	}

	private boolean validerFilmUnique(String titre, BusinessException be) {
		try {
			boolean titreExiste = filmDAO.findTitre(titre);
			if (titreExiste) {
				be.add(BusinessCode.VALIDATION_FILM_UNIQUE);
				return false;
			}
		} catch (DataAccessException e) {
			be.add(BusinessCode.VALIDATION_FILM_UNIQUE);
			return false;
		}
		return true;
	}

	private boolean validerAnnee(int annee, BusinessException be) {
		if (annee < 1900) {
			be.add(BusinessCode.VALIDATION_FILM_ANNEE);
			return false;
		}
		return true;
	}

	private boolean validerDuree(int duree, BusinessException be) {
		if (duree < 1) {
			be.add(BusinessCode.VALIDATION_FILM_DUREE);
			return false;
		}
		return true;
	}

	private boolean validerSynopsis(String synopsis, BusinessException be) {
		if (synopsis == null || synopsis.isBlank()) {
			be.add(BusinessCode.VALIDATION_FILM_SYNOPSIS_BLANK);
			return false;
		}
		if (synopsis.length() < 20 || synopsis.length() > 250) {
			be.add(BusinessCode.VALIDATION_FILM_SYNOPSIS_LENGTH);
			return false;
		}
		return true;
	}
	
	private boolean validerMembreAvisFilm(long idFilm, long idMembre, BusinessException be) {
		try {
			int count = avisDAO.countAvis(idFilm, idMembre);
			if (count > 0) {
				be.add(BusinessCode.VALIDATION_AVIS_UNIQUE);
				return false;
			}
		} catch (DataAccessException e) {
			be.add(BusinessCode.VALIDATION_AVIS_UNIQUE);
			return false;
		}
		return true;
	}

	private boolean validerIdFilm(long idFilm, BusinessException be) {
		if (idFilm < 0) {
			be.add(BusinessCode.VALIDATION_FILM_ID_INCONNU);
			return false;
		}
		try {
			String titre = filmDAO.findTitre(idFilm);
			if (titre == null) {
				be.add(BusinessCode.VALIDATION_FILM_INCONNU);
				return false;
			}
		} catch (DataAccessException e) {
			be.add(BusinessCode.VALIDATION_FILM_INCONNU);
			return false;
		}
		return true;
	}

	private boolean validerMembre(Membre membre, BusinessException be) {
		if (membre == null) {
			be.add(BusinessCode.VALIDATION_AVIS_MEMBRE_NULL);
			return false;
		}
		if (membre.getId() <= 0) {
			be.add(BusinessCode.VALIDATION_AVIS_MEMBRE_ID_INCONNU);
			return false;
		}
		try {
			Membre membreEnBase = membreDAO.read(membre.getId());
			if (membreEnBase == null) {
				be.add(BusinessCode.VALIDATION_AVIS_MEMBRE_INCONNU);
				return false;
			}
			
			if (!membreEnBase.equals(membre)) {
				be.add(BusinessCode.VALIDATION_AVIS_MEMBRE_INCONNU);
				return false;
			}
		} catch (DataAccessException e) {
			be.add(BusinessCode.VALIDATION_AVIS_MEMBRE_INCONNU);
			return false;
		}
		return true;
	}

	private boolean validerCommentaire(String commentaire, BusinessException be) {
		if (commentaire == null || commentaire.isBlank()) {
			be.add(BusinessCode.VALIDATION_AVIS_COMMENTAIRE_BLANK);
			return false;
		}
		if (commentaire.length() < 1 || commentaire.length() > 250) {
			be.add(BusinessCode.VALIDATION_AVIS_COMMENTAIRE_LENGTH);
			return false;
		}
		return true;
	}

	private boolean validerNote(int note, BusinessException be) {
		if (note < 0 || note > 5) {
			be.add(BusinessCode.VALIDATION_AVIS_NOTE);
			return false;
		}
		return true;
	}

	private boolean validerAvis(Avis a, BusinessException be) {
		if (a == null) {
			be.add(BusinessCode.VALIDATION_AVIS_NULL);
			return false;
		}
		return true;
	}
}
