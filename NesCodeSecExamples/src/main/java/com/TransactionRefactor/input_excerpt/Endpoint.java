package de.tud.plt.r43ples.webservice;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import de.tud.plt.r43ples.core.HeaderInformation;
import de.tud.plt.r43ples.core.R43plesCoreInterface;
import de.tud.plt.r43ples.core.R43plesCoreSingleton;
import de.tud.plt.r43ples.existentobjects.InitialCommit;
import de.tud.plt.r43ples.existentobjects.MergeCommit;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.*;
import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NoWriterForLangException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.mergingUI.ui.MergingControl;


@Path("sparql")
public class Endpoint {


	@Context
	private UriInfo uriInfo;
	@Context
	private Request request;

	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);


	static final MediaType TEXT_TURTLE_TYPE = new MediaType("text", "turtle");
	static final MediaType APPLICATION_RDF_XML_TYPE = new MediaType("application", "rdf+xml");
	static final MediaType APPLICATION_SPARQL_RESULTS_XML_TYPE = new MediaType("application", "sparql-results+xml");


	/**map for client and mergingControlMap
	 * for each client there is a mergingControlMap**/
	protected static HashMap<String, HashMap<String, MergingControl>> clientMap = new HashMap<String, HashMap<String, MergingControl>>();


	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml"})
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public final Response sparqlPOST(
			@FormParam("format") final String formatQuery,
			@FormParam("query") @DefaultValue("") final String sparqlQuery,
			@HeaderParam("r43ples-revisiongraph") @DefaultValue("") final String revision_information,
			@FormParam("query_rewriting") @DefaultValue("") final String query_rewriting) throws InternalErrorException {
		try {
			String format = getFormat(formatQuery);
			logger.info("SPARQL POST query (format: "+format+", query: "+sparqlQuery +")" + revision_information);
			return sparql(format, sparqlQuery, revision_information, query_rewriting);
		} catch (Exception e) {
			return Response.serverError().status(Response.Status.NOT_ACCEPTABLE).build();
		}
	}

	private String getFormat(final String formatQuery) throws Exception {
		if (formatQuery == null){
			List<Variant> reqVariants = Variant.mediaTypes(MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE,
					MediaType.APPLICATION_JSON_TYPE, TEXT_TURTLE_TYPE, APPLICATION_RDF_XML_TYPE, APPLICATION_SPARQL_RESULTS_XML_TYPE).build();
			Variant bestVariant = request.selectVariant(reqVariants);
	        if (bestVariant == null) {
	        	throw new Exception("Requested datatype not available");
	        }
        	MediaType reqMediaType = bestVariant.getMediaType();
        	return reqMediaType.toString();
		}
		else {
			return formatQuery;
		}
	}

	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	@Consumes("application/sparql-query")
	public final Response sparqlPOSTdirectly(
			@HeaderParam("r43ples-revisiongraph") @DefaultValue("") final String revision_information,
			final String sparqlQuery) throws InternalErrorException {
		List<Variant> reqVariants = Variant.mediaTypes(MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE,
				MediaType.APPLICATION_JSON_TYPE, TEXT_TURTLE_TYPE, APPLICATION_RDF_XML_TYPE, APPLICATION_SPARQL_RESULTS_XML_TYPE).build();
		Variant bestVariant = request.selectVariant(reqVariants);
        if (bestVariant == null) {
            return Response.serverError().status(Response.Status.NOT_ACCEPTABLE).build();
        }
    	MediaType reqMediaType = bestVariant.getMediaType();
    	String format = reqMediaType.toString();
		logger.info("SPARQL POST query directly ");
		return sparql(reqMediaType.toString(), sparqlQuery, revision_information, false);
	}

	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response sparqlGET(
			@QueryParam("format") final String formatQuery,
			@QueryParam("query") @DefaultValue("") final String sparqlQuery,
			@HeaderParam("r43ples-revisiongraph") @DefaultValue("") final String revision_information,
			@QueryParam("query_rewriting") @DefaultValue("") final String query_rewriting) throws InternalErrorException {
		String format;
		try {
			format = getFormat(formatQuery);
		} catch (Exception e) {
			return Response.serverError().status(Response.Status.NOT_ACCEPTABLE).build();
		}

		logger.info("SPARQL GET query ");
		return sparql(format, sparqlQuery, revision_information, query_rewriting);
	}

	private final Response sparql(final String format, final String sparqlQuery, final String revision_information, final boolean query_rewriting) throws InternalErrorException {
		if ("".equals(sparqlQuery)) {
			if (format.contains(MediaType.TEXT_HTML)) {
				return getHTMLResponse();
			} else {
				return getServiceDescriptionResponse(format);
			}
		} else {
			return getSparqlResponse(format, sparqlQuery, revision_information, query_rewriting);
		}
	}

	public final Response sparql(final String format, final String sparqlQuery, final boolean query_rewriting) throws InternalErrorException {
		return sparql(format, sparqlQuery, null, query_rewriting);
	}

	private Response sparql(final String format, final String sparqlQuery, final String revision_information, final String query_rewriting) throws InternalErrorException {
		String option = query_rewriting.toLowerCase();
		if (option.equals("on") || option.equals("true") || option.equals("new"))
			return sparql(format, sparqlQuery, revision_information, true);
		else
			return sparql(format, sparqlQuery, revision_information, false);
	}

	public final Response sparql(final String format, final String sparqlQuery) throws InternalErrorException {
		return sparql(format, sparqlQuery, null, false);
	}

}