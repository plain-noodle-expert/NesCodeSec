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

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins);

		return "admin";
	}

	@RequestMapping("/admin")
	public String admin(Model model, Principal p) {
		model.addAttribute("admins", admins