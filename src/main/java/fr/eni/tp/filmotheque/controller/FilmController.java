package fr.eni.tp.filmotheque.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import fr.eni.tp.filmotheque.bll.FilmService;
import fr.eni.tp.filmotheque.bo.Film;
import fr.eni.tp.filmotheque.bo.Genre;
import fr.eni.tp.filmotheque.bo.Membre;
import fr.eni.tp.filmotheque.bo.Participant;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/films")
@SessionAttributes({ "genresEnSession", "membreEnSession", "participantsEnSession" })
public class FilmController {

	private FilmService filmService;

	public FilmController(FilmService filmService) {
		this.filmService = filmService;
	}

	@GetMapping
	public String afficherFilms(Model model) {
		List<Film> films = filmService.consulterFilms();
		model.addAttribute("films", films);

		return "view-films";
	}

	@GetMapping("/detail")
	public String afficherUnFilm(@RequestParam(name = "id", required = true) long id, Model model) {
		if (id > 0) {
			Film film = filmService.consulterFilmParId(id);
			if (film != null) {
				// Ajout de l'instance dans le modèle
				model.addAttribute("film", film);
				return "view-film-form";
			} else {
				System.out.println("Film inconnu");
			}
		} else {
			System.out.println("Identifiant inconnu");
		}
		// Par défaut redirection vers l'url pour afficher les films
		return "redirect:/films";

	}

	/**
	 * Gestion de la liste des genres en session
	 * 
	 * Création d'une méthode qui charge les données dans le modèle
	 * avec @ModelAttribute
	 * 
	 * Injection de l'attribut en session avec @SessionAttributes
	 */
	@ModelAttribute("genresEnSession")
	public List<Genre> chargerGenres() {
		System.out.println("Chargement en Session - GENRES");
		return filmService.consulterGenres();
	}

	@ModelAttribute("participantsEnSession")
	public List<Participant> chargerParticipants() {
		System.out.println("Chargement en Session - PARTICIPANTS");
		return filmService.consulterParticipants();
	}

	@GetMapping("/creer")
	public String creerFilm(@ModelAttribute("membreEnSession") Membre mEnSession, Model model) {
		if (mEnSession != null && mEnSession.getId() >= 1) {
			model.addAttribute("film", new Film());
			return "view-film-form";
		}
		return "redirect:/films";
	}
	
	
	@PostMapping("/creer")
	public String creerFilm(@Valid @ModelAttribute("membreEnSession") Membre mEnSession,
			@Valid @ModelAttribute("film")Film film, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "view-film-form";
		} else {
			if (mEnSession != null && mEnSession.getId() >= 1) {
			System.out.println(film);
			filmService.creerFilm(film);			
		}
		return "redirect:/films";
		}
	}

}
