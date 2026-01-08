<|current_file_content|>
package guestbook;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.FragmentsRendering;

/**
 * A controller to handle web requests to manage {@link GuestbookEntry}s
 *
 * @author Paul Henke
 * @author Oliver Drotbohm
 */
@Controller
class GuestbookController {

	private final GuestbookRepository guestbook;

	GuestbookController(GuestbookRepository guestbook) {

		Assert.notNull(guestbook, "Guestbook must not be null!");
		this.guestbook = guestbook;
	}

	@GetMapping(path = "/")
	String index() {
		return "redirect:/guestbook";
	}

	@GetMapping(path = "/guestbook")
	String guestBook(Model model, @ModelAttribute(binding = false) GuestbookForm form) {

		model.addAttribute("entries", guestbook.findAll());
		model.addAttribute("form", form);

		return "guestbook";
	}

	@PostMapping(path = "/guestbook")
	String addEntry(@Valid @ModelAttribute("form") GuestbookForm form, Errors errors, Model model) {

		if (errors.hasErrors()) {
			return guestBook(model, form);
		}

		guestbook.save(form.toNewEntry());

		return "redirect:/guestbook";
	}
}
<|/current_file_content|>


<|recently_viewed_code_snippets|>
<|recently_viewed_code_snippet|>
package guestbook;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

/**
 * A repository to manage {@link GuestbookEntry} instances. The methods are dynamically implemented by Spring Data JPA.
 *
 * @author Oliver Drotbohm
 * @see <a href="https://en.wikipedia.org/wiki/Domain-driven_design#Building_blocks">(Wikipedia)
 *      Domain-driven-design</a>
 * @see <a href="https://spring.io/projects/spring-data-jpa">Spring data jpa</a>
 */
interface GuestbookRepository extends CrudRepository<GuestbookEntry, Long> {

	/**
	 * Returns all {@link GuestbookEntry}s created by the user with the given name, sorted by the given sort criteria.
	 *
	 * @param name the name to search for
	 * @param sort the given sorting criteria
	 * @return all {@link GuestbookEntry}s matching the query
	 */
	Streamable<GuestbookEntry> findByName(String name, Sort sort);
}
<|/recently_viewed_code_snippet|>

<|recently_viewed_code_snippet|>
package guestbook;

import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The core class to bootstrap our application. It triggers Spring Boot's auto-configuration, component scanning and
 * configuration properties scanning using the {@link SpringBootApplication} convenience annotation. At the same time,
 * this class acts as configuration class to configure additional components (see {@link #init(GuestbookRepository)})
 * that the Spring container will take into account when bootstrapping.
 *
 * @author Paul Henke
 * @author Oliver Drotbohm
 */
@SpringBootApplication
public class Application {

	/**
	 * The main application method, bootstraps the Spring container.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Some initializing code to populate our database with some {@link GuestbookEntry}s. Beans of type
	 * {@link CommandLineRunner} will be executed on application startup which makes them a convenient way to run
	 * initialization code.
	 */
	@Bean
	CommandLineRunner init(GuestbookRepository guestbook) {

		return args -> {

			Stream.of( //
					new GuestbookEntry("H4xx0r", "first!!!"), //
					new GuestbookEntry("Arni", "Hasta la vista, baby"), //
					new GuestbookEntry("Duke Nukem",
							"It's time to kick ass and chew bubble gum. And I'm all out of gum."), //
					new GuestbookEntry("Gump1337",
							"Mama always said life was like a box of chocolates. You never know what you're gonna get.")) //
					.forEach(guestbook::save);
		};
	}

	/**
	 * This class customizes the web and web security configuration through callback methods provided by the
	 * {@link WebMvcConfigurer} interface.
	 */
	@Configuration
	static class SecurityConfiguration implements WebMvcConfigurer {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry)
		 */
		@Override
		public void addViewControllers(ViewControllerRegistry registry) {

			// Route requests to /login to the login view (a default one provided by Spring Security)
			registry.addViewController("/login").setViewName("login");
		}

		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

			// Allow all requests on the URI level, configure form login.
			http.csrf(it -> it.disable())
					.authorizeHttpRequests(it -> it.anyRequest().permitAll())
					.formLogin(it -> {})
					.logout(it -> it.logoutSuccessUrl("/").clearAuthentication(true));

			return http.build();
		}
	}
}
<|/recently_viewed_code_snippet|>
<|/recently_viewed_code_snippets|>
