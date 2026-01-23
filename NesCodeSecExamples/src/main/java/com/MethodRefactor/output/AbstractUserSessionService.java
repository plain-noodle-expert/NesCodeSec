<|editable_region_start|>
package hiconic.platform.reflex.security.processor;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.securityservice.api.DeletedSessionInfo;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.processing.securityservice.impl.Roles;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.provider.Hub;

public abstract class AbstractUserSessionService implements UserSessionService, LifecycleAware {

	static final Logger log = Logger.getLogger(AbstractUserSessionService.class);

	protected Function<UserSessionType, String> sessionIdProvider;
	protected String nodeId;

	protected UserSessionType defaultUserSessionType = UserSessionType.normal;
	protected TimeSpan defaultUserSessionMaxIdleTime;

	protected List<Hub<UserSession>> internalUserSessionHolders;

	public AbstractUserSessionService() {
		super();
	}

	protected abstract void deletePersistenceUserSession(PersistenceUserSession pUserSession);
	protected abstract void deletePersistenceUserSession(String sessionId);
	protected abstract void closePersistenceUserSession(String sessionId);
	protected abstract void closePersistenceUserSession(PersistenceUserSession userSession);

	protected abstract Maybe<PersistenceUserSession> findPersistenceUserSession(String sessionId);

	protected abstract Maybe<PersistenceUserSession> findPersistenceUserSessionByAcquirationKey(String acquirationKey);

	protected abstract PersistenceUserSession createPersistenceUserSession(User user, UserSessionType type, TimeSpan maxIdleTime, TimeSpan maxAge,
			Date fixedExpiryDate, String internetAddress, Map<String, String> properties, String acquirationKey,
			boolean blocksAuthenticationAfterLogout);

	@Required
	public void setSessionIdProvider(Function<UserSessionType, String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}

	@Required
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @param defaultUserSessionMaxIdleTime
	 *            Defaults to null - no max idle time limit.
	 */
	@Configurable
	public void setDefaultUserSessionMaxIdleTime(TimeSpan defaultUserSessionMaxIdleTime) {
		this.defaultUserSessionMaxIdleTime = defaultUserSessionMaxIdleTime;
	}

	/**
	 * @param defaultUserSessionType
	 *            Defaults to 'normal'.
	 */
	@Configurable
	public void setDefaultUserSessionType(UserSessionType defaultUserSessionType) {
		this.defaultUserSessionType = defaultUserSessionType;
	}

	@Configurable
	public void setInternalUserSessionHolders(List<Hub<UserSession>> internalUserSessionHolders) {
		this.internalUserSessionHolders = internalUserSessionHolders;
	}

	@Override
	public void postConstruct() {

		try {
			createInternalUserSessions();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to ensure the persistence of internal user sessions", e);
		}
	}
	
	private String encodeSet(Set<String> set) {
		StringBuilder builder = new StringBuilder();
		for (String el: set) {
			if (builder.length() != 0)
				builder.append(',');
			
			builder.append(URLEncoder.encode(el, StandardCharsets.UTF_8));
		}
		
		return builder.toString();
	}
	
	private Set<String> decodeSet(String encoded) {
		
		Set<String> set = new LinkedHashSet<>();
		StringTokenizer tokenizer = new StringTokenizer(encoded, ",");
		
		while (tokenizer.hasMoreTokens()) {
			String el = tokenizer.nextToken();
			set.add(URLDecoder.decode(el, StandardCharsets.UTF_8));
		}
		
		return set;
	}
	
	@Override
	public void preDestroy() {
		try {
			deleteInternalUserSessions();
		} catch (Exception e) {
			log.error(() -> "Failed to cleanup the internal user sessions", e);
		}
	}

	@Override
	public Maybe<UserSession> createUserSession(User user, UserSessionType type, TimeSpan maxIdleTime, TimeSpan maxAge, Date fixedExpiryDate,
			String internetAddress, Map<String, String> properties, String acquirationKey, boolean blocksAuthenticationAfterLogout) {
		if (user == null || user.getId() == null) {
			return Reasons.build(InvalidArgument.T).text("User and user id cannot be null").toMaybe();
		}

		log.debug(() -> "Creating a user session for user '" + user.getName() + "' connected from '" + internetAddress + "'");
		PersistenceUserSession pUserSession = createPersistenceUserSession(user, type, maxIdleTime, maxAge, fixedExpiryDate, internetAddress,
				properties, acquirationKey, blocksAuthenticationAfterLogout);
		return Maybe.complete(mapToUserSession(pUserSession));
	}

	@Override
	public Maybe<UserSession> findUserSession(String sessionId) {
		log.trace(() -> "Fetching user session '" + sessionId + "'");
		Maybe<PersistenceUserSession> pUserSessionMaybe = findPersistenceUserSession(sessionId);

		if (pUserSessionMaybe.isUnsatisfied()) {
			return Maybe.empty(pUserSessionMaybe.whyUnsatisfied());
		}

		PersistenceUserSession pUserSession = pUserSessionMaybe.get();

		UserSession userSession = mapToUserSession(pUserSession);
		log.trace(() -> "Found user session '" + sessionId + "'; Returning: " + userSession);

		return Maybe.complete(userSession);
	}

	@Override
	public Maybe<DeletedSessionInfo> deleteUserSession(String sessionId) {
		log.debug(() -> "Deleting user session '" + sessionId + "'");

		Maybe<PersistenceUserSession> pUserSessionMaybe = findPersistenceUserSession(sessionId);

		if (pUserSessionMaybe.isUnsatisfied()) {
			return pUserSessionMaybe.whyUnsatisfied().asMaybe();
		}

		PersistenceUserSession pUserSession = pUserSessionMaybe.get();

		String acquirationKey = pUserSession.getAcquirationKey();

		if (acquirationKey != null && pUserSession.getBlocksAuthenticationAfterLogout()) {
			closePersistenceUserSession(pUserSession);
		} else {
			deletePersistenceUserSession(pUserSession);
		}

		DeletedSessionInfo info = new DeletedSessionInfo() {
			UserSession userSession = null;

			@Override
			public UserSession userSession() {
				if (userSession == null)
					userSession = mapToUserSession(pUserSession);

				return userSession;
			}

			@Override
			public String acquirationKey() {
				return pUserSession.getAcquirationKey();
			}
		};

		return Maybe.complete(info);
	}

	
	public void deleteInternalUserSessionsAuthorized() {
		if (this.internalUserSessionHolders == null || this.internalUserSessionHolders.isEmpty()) {
			log.warn(() -> "Skipping internal user sessions cleanup; Internal user session holder list was not configured or is empty");
			return;
		}
		for (Hub<UserSession> userSessionHolder : this.internalUserSessionHolders) {
			deleteInternalUserSessionAuthorized(userSessionHolder);
		}
	}

	private void deleteInternalUserSessionAuthorized(Hub<UserSession> userSessionHolder) {
		UserSession userSession = userSessionHolder.get();
		if (userSession == null) {
			return;
		}
		deletePersistenceUserSession(userSession.getSessionId());
	}

}
<|editable_region_end|>
```
