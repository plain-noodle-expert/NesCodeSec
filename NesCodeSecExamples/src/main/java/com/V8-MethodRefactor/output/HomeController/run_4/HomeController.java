<|editable_region_start|>
package org.mitre.web;

import java.security.Principal;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;

import org.mitre.openid.connect.client.OIDCAuthenticationFilter;
import org.mitre.openid.connect.client.SubjectIssuerGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.util.Date;
import org.joda.time.DateTime;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	// [START trace_setup_java_custom_span]
  	private static final Tracer tracer = Tracing.getTracer();
	
	

	// filter reference so we can get class names and things like that.
	@Autowired
	private OIDCAuthenticationFilter filter;

	@Resource(name = "namedAdmins")
	private Set<SubjectIssuerGrantedAuthority> admins;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model, Principal p) {

		model.addAttribute("issuerServiceClass", filter.getIssuerService().getClass().getSimpleName());
		model.addAttribute("serverConfigurationServiceClass", filter.getServerConfigurationService().getClass().getSimpleName());
		model.addAttribute("clientConfigurationServiceClass", filter.getClientConfigurationService().getClass().getSimpleName());
		model.addAttribute("authRequestOptionsServiceClass", filter.getAuthRequestOptionsService().getClass().getSimpleName());
		model.addAttribute("authRequestUriBuilderClass", filter.getAuthRequestUrlBuilder().getClass().getSimpleName());

		model.addAttribute("admins", admins);

		return "home";
	}

	@RequestMapping("/user")
	@PreAuthorize("hasRole('ROLE_USER')")
	public String user(Principal p) {
		return "user";
	}

	@RequestMapping("/open")
	public String open(Principal p) {
		return "open";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/isadmin")
	public String isAdmin(Principal p) {
		for (SubjectIssuerGrantedAuthority admin : admins) {
			if (admin.getSubject().equals(p.getName())) {
				return "true";
			}
		}
		return "false";
	}

	public String login(Principal p) {
		return "login";
	}
	
	
	
	
	public static void doWork() {
    	// Create a child Span of the current Span.
    		try (Scope ss = tracer.spanBuilder("MyChildWorkSpan").startScopedSpan()) {
      			doInitialWork();
      			tracer.getCurrentSpan().addAnnotation("Finished initial work");
      			doFinalWork();
    		}
  	}

  	private static void doInitialWork() {
    	// ...
    		tracer.getCurrentSpan().addAnnotation("Doing initial work");
    	// ...
  	}

  	private static void doFinalWork() {
    	// ...
    		tracer.getCurrentSpan().addAnnotation("Hello world!");
    	// ...
  	}
  	// [END trace_setup_java_custom_span]
	
	// [START trace_setup_java_full_sampling]
  	public static void doWorkFullSampled() {
    		try (Scope ss =
       			tracer
            			.spanBuilder("MyChildWorkSpan")
            			.setSampler(Samplers.alwaysSample())
            			.startScopedSpan()) {
      			doInitialWork();
      			tracer.getCurrentSpan().addAnnotation("Finished initial work");
      			doFinalWork();
    		}
  	}
  	// [END trace_setup_java_full_sampling]

  	// [START trace_setup_java_create_and_register]
  	public static void createAndRegister() throws IOException {
    		StackdriverTraceExporter.createAndRegister(StackdriverTraceConfiguration.builder().build());
  	}
  	// [END trace_setup_java_create_and_register]

  	// [START trace_setup_java_create_and_register_with_token]
  	public static void createAndRegisterWithToken(String accessToken) throws IOException {
    		Date expirationTime = DateTime.now().plusSeconds(60).toDate();

    		GoogleCredentials credentials =
        		GoogleCredentials.create(new AccessToken(accessToken, expirationTime));
    		StackdriverTraceExporter.createAndRegister(
        		StackdriverTraceConfiguration.builder()
            			.setProjectId("MyStackdriverProjectId")
            			.setCredentials(credentials)
           	 		.build());
  	}
  	// [END trace_setup_java_create_and_register_with_token]

  	// [START trace_setup_java_register_exporter]
  	public static void createAndRegisterGoogleCloudPlatform(String projectId) throws IOException {
    		StackdriverTraceExporter.createAndRegister(
        		StackdriverTraceConfiguration.builder().setProjectId(projectId).build());
  	}
  	// [END trace_setup_java_register_exporter]
}
<|editable_region_end|>
```
