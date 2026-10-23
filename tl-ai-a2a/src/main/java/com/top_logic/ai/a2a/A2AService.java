/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.top_logic.ai.a2a.client.RemoteAgents;
import com.top_logic.ai.a2a.protocol.ArtifactRef;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.Sections;
import com.top_logic.ai.a2a.server.A2ARequestHandler;
import com.top_logic.ai.a2a.server.AgentRunBackend;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentications;
import com.top_logic.tl.ai.core.access.EndpointAuthentication;
import com.top_logic.tl.ai.core.access.EndpointSecret;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.observe.ObserverQueue;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * Connects this application's agents to other agents over the A2A protocol v1.0: exposes configured
 * agents ({@code /a2a/*}, {@link A2AServlet}) and lets local agents call configured remote agents
 * (tool factory {@code a2a}, {@code A2AClientTools}).
 *
 * <p>
 * Deny by default: an agent is reachable only if it is listed under &lt;agents&gt;, and only with a
 * credential one of the &lt;authentications&gt; accepts - the schemes of the MCP endpoint, personal
 * access tokens among them. A run acts as the account the credential names.
 * </p>
 */
@ServiceDependencies({
	AgentExecutionStore.Module.class,
	ToolRegistryService.Module.class,
	ServletContextService.Module.class
})
public class A2AService extends ConfiguredManagedClass<A2AService.Config<?>> {

	/**
	 * Configuration of {@link A2AService}.
	 */
	public interface Config<I extends A2AService> extends ConfiguredManagedClass.Config<I>, ServerAuthentications {

		/**
		 * The name of this application in artifact references and in the metadata it sends. Empty: the
		 * servlet context path without its slash.
		 */
		@Name("app")
		@Nullable
		String getApp();

		/**
		 * The public base URL of the application, e.g. {@code https://example.com/app}, for the agent
		 * cards. Empty: taken from the request that fetches the card.
		 */
		@Name("public-url")
		@Nullable
		String getPublicUrl();

		/**
		 * The longest time a blocking {@code SendMessage} waits for its task, and the longest time a
		 * local {@code askAgent} waits for a remote agent.
		 */
		@Name("max-blocking-seconds")
		@IntDefault(1500)
		int getMaxBlockingSeconds();

		/**
		 * How a calling agent proves who it is: personal access tokens
		 * (&lt;personal-access-token-authentication&gt;), an API key mapped to an account, OAuth
		 * bearer tokens, HTTP basic. Empty: every call is refused.
		 */
		@Override
		@DisplayMinimized
		Map<String, ServerAuthentication.Config<?>> getAuthentications();

		/**
		 * The secrets the {@link #getAuthentications() authentications} check against.
		 */
		@Name("secrets")
		@DisplayMinimized
		List<EndpointSecret> getSecrets();

		/**
		 * The agents this application exposes; the first one also answers
		 * {@code /.well-known/agent-card.json}.
		 */
		@Name("agents")
		@Key(ExposedAgentConfig.PATH)
		List<ExposedAgentConfig> getAgents();

		/**
		 * The remote A2A agents local agents may call.
		 */
		@Name("remote-agents")
		@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
		List<RemoteAgentConfig> getRemoteAgents();

		/**
		 * Hooks that decide the subject of an incoming conversation, asked in order.
		 */
		@Name("subject-resolvers")
		@EntryTag("resolver")
		List<PolymorphicConfiguration<? extends A2ASubjectResolver>> getSubjectResolvers();

		/**
		 * Hooks that publish this application's artifacts by reference, asked in order.
		 */
		@Name("artifact-sources")
		@EntryTag("source")
		List<PolymorphicConfiguration<? extends A2AArtifactSource>> getArtifactSources();

		/**
		 * Hooks that add artifacts to a completed task.
		 */
		@Name("artifact-contributors")
		@EntryTag("contributor")
		List<PolymorphicConfiguration<? extends A2AArtifactContributor>> getArtifactContributors();

		/**
		 * Hooks that handle the structured data parts of incoming messages, asked in order.
		 */
		@Name("data-handlers")
		@EntryTag("handler")
		List<PolymorphicConfiguration<? extends A2ADataHandler>> getDataHandlers();

		/**
		 * Hooks that learn of the A2A exchanges in both directions. None by default; one that passes
		 * them on outside the application must authenticate that itself - see {@link A2AObserver}.
		 */
		@Name("observers")
		@EntryTag("observer")
		List<PolymorphicConfiguration<? extends A2AObserver>> getObservers();
	}

	/**
	 * An agent this application exposes.
	 */
	public interface ExposedAgentConfig extends ConfigurationItem {

		/** @see #getPath() */
		String PATH = "path";

		/**
		 * The agent's name in URLs: {@code /a2a/{path}}.
		 */
		@Name(PATH)
		@Mandatory
		String getPath();

		/**
		 * The tl-ai agent that serves it.
		 */
		@Name("agent")
		@Mandatory
		String getAgent();

		/**
		 * The name on the agent card. Empty: the path.
		 */
		@Name("display-name")
		@Nullable
		String getDisplayName();

		/**
		 * What the agent does, for the agent card.
		 */
		@Name("description")
		@Nullable
		String getDescription();

		/**
		 * Whether a run started over A2A may use tools that change the application. They then run
		 * without an approval prompt. Off: those tools are not offered to the run.
		 */
		@Name("allow-writes")
		boolean getAllowWrites();

		/**
		 * The model the runs use. Empty: the agent's own model or the application default.
		 */
		@Name("model")
		@Nullable
		String getModel();

		/**
		 * The skills on the agent card.
		 */
		@Name("skills")
		@Key(SkillConfig.ID)
		List<SkillConfig> getSkills();
	}

	/**
	 * A skill on an agent card.
	 */
	public interface SkillConfig extends ConfigurationItem {

		/** @see #getId() */
		String ID = "id";

		/** The skill id. */
		@Name(ID)
		@Mandatory
		String getId();

		/** The skill name. Empty: the id. */
		@Name("name")
		@Nullable
		String getName();

		/** What the skill does. */
		@Name("description")
		@Nullable
		String getDescription();

		/** Keywords. */
		@Name("tags")
		@Format(CommaSeparatedStrings.class)
		List<String> getTags();
	}

	/**
	 * A remote A2A agent.
	 */
	public interface RemoteAgentConfig extends NamedConfigMandatory {

		/**
		 * The URL of the remote agent card.
		 */
		@Name("card-url")
		@Mandatory
		String getCardUrl();

		/** Configuration property name for {@link #getCredentials()}. */
		String CREDENTIALS = "credentials";

		/**
		 * How the calls to the remote agent are authenticated.
		 *
		 * <p>
		 * Typically a personal access token of the remote application, sent as bearer token and
		 * read from an environment variable or a file:
		 * </p>
		 *
		 * <pre>
		 * &lt;remote-agent name="other" card-url="https://example.com/other/a2a/agent/agent-card.json"&gt;
		 *   &lt;credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer"&gt;
		 *     &lt;secret class="com.top_logic.basic.credential.EnvironmentSecretSource" name="OTHER_A2A_TOKEN"/&gt;
		 *   &lt;/credentials&gt;
		 * &lt;/remote-agent&gt;
		 * </pre>
		 *
		 * <p>
		 * If not set, the calls are sent without credentials. The agent card is always fetched
		 * without credentials. A call whose credentials are rejected (HTTP 401) is repeated once
		 * with fresh credentials.
		 * </p>
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();
	}

	/**
	 * URI of the agent-card extension that names this application's artifact endpoint and the
	 * {@code app} its references carry.
	 */
	public static final String ARTIFACTS_EXTENSION = "https://top-logic.com/a2a/extensions/artifacts/v1";

	private static volatile A2AService _instance;

	private final List<A2ASubjectResolver> _subjectResolvers;

	private final List<A2AArtifactContributor> _artifactContributors;

	private final List<A2AArtifactSource> _artifactSources;

	private final List<A2ADataHandler> _dataHandlers;

	private final List<A2AObserver> _observers;

	private ObserverQueue _observerQueue;

	private Consumer<A2AExchange> _observer;

	private final ContextBook _contexts = new ContextBook();

	private Authenticator _authenticator;

	private AgentRunBackend _backend;

	private A2ARequestHandler _handler;

	private final RemoteAgents _remoteAgents;

	/**
	 * Creates an {@link A2AService} from configuration.
	 */
	public A2AService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_subjectResolvers = instances(context, config.getSubjectResolvers());
		_artifactContributors = instances(context, config.getArtifactContributors());
		_artifactSources = instances(context, config.getArtifactSources());
		_dataHandlers = instances(context, config.getDataHandlers());
		_observers = instances(context, config.getObservers());
		_remoteAgents = new RemoteAgents(context, config.getRemoteAgents());
	}

	private static <T> List<T> instances(InstantiationContext context,
			List<? extends PolymorphicConfiguration<? extends T>> configs) {
		List<T> result = new ArrayList<>();
		for (PolymorphicConfiguration<? extends T> config : configs) {
			result.add(context.getInstance(config));
		}
		return result;
	}

	/**
	 * The running service, or <code>null</code>.
	 */
	public static A2AService getInstance() {
		return _instance;
	}

	@Override
	protected void startUp() {
		super.startUp();
		Config<?> config = getConfig();
		_authenticator =
			EndpointAuthentication.build("/a2a/*", config.getAuthentications(), config.getSecrets());
		if (!_observers.isEmpty()) {
			ObserverQueue queue = new ObserverQueue("a2a-observers");
			_observerQueue = queue;
			_observer = exchange -> queue.deliver(_observers, observer -> observer.onExchange(exchange));
		}
		_backend = new AgentRunBackend(this);
		_handler = new A2ARequestHandler(_backend, path -> exposedAgent(path) != null,
			config.getMaxBlockingSeconds() * 1000L, observer());
		_instance = this;
		Logger.info("A2A service started: agents " + config.getAgents().stream().map(ExposedAgentConfig::getPath).toList()
			+ ", remote agents " + _remoteAgents.names() + ".", A2AService.class);
	}

	@Override
	protected void shutDown() {
		_instance = null;
		if (_backend != null) {
			_backend.shutDown();
		}
		if (_observerQueue != null) {
			_observerQueue.close();
		}
		super.shutDown();
	}

	/** The subject resolvers, in order. */
	public List<A2ASubjectResolver> subjectResolvers() {
		return _subjectResolvers;
	}

	/** The artifact contributors, in order. */
	public List<A2AArtifactContributor> artifactContributors() {
		return _artifactContributors;
	}

	/** The data handlers, in order. */
	public List<A2ADataHandler> dataHandlers() {
		return _dataHandlers;
	}

	/**
	 * What hands an exchange to the {@link A2AObserver}s on their own thread, or <code>null</code>
	 * when none is configured.
	 */
	public Consumer<A2AExchange> observer() {
		return _observer;
	}

	/**
	 * Tells the observers that an artifact of {@code ref} was read.
	 *
	 * @param agent
	 *        The local agent that read it, or <code>null</code> when a remote client read it.
	 * @param peer
	 *        The remote side: the reading client, or the application the artifact belongs to.
	 */
	public void observeFetch(A2AExchange.Direction direction, String agent, String peer, ArtifactRef ref,
			String section, String runId) {
		if (_observer != null) {
			_observer.accept(new A2AExchange(Instant.now(), direction, A2AExchange.Kind.ARTIFACT_FETCHED, agent, peer,
				null, ref.taskId(), null, runId, Json.map("ref", ref.toString(), "section", section)));
		}
	}

	/** Context ids of local conversations and remembered subjects. */
	public ContextBook contexts() {
		return _contexts;
	}

	/** The JSON-RPC handler. */
	public A2ARequestHandler handler() {
		return _handler;
	}

	/** The remote agents. */
	public RemoteAgents remoteAgents() {
		return _remoteAgents;
	}

	/** What checks the credential of a caller. */
	public Authenticator authenticator() {
		return _authenticator;
	}

	/**
	 * The name of this application in references and outgoing metadata.
	 */
	public String appName() {
		String app = getConfig().getApp();
		if (app != null && !app.isBlank()) {
			return app.trim();
		}
		String path = ServletContextService.getInstance().getServletContext().getContextPath();
		return path.length() > 1 ? path.substring(1) : "app";
	}

	/**
	 * The exposed agent with the given path, or <code>null</code>.
	 */
	public ExposedAgentConfig exposedAgent(String path) {
		for (ExposedAgentConfig agent : getConfig().getAgents()) {
			if (agent.getPath().equals(path)) {
				return agent;
			}
		}
		return null;
	}

	/**
	 * The display name of an exposed agent.
	 */
	public static String displayName(ExposedAgentConfig agent) {
		String name = agent.getDisplayName();
		return name == null || name.isBlank() ? agent.getPath() : name.trim();
	}

	/**
	 * The display name of a local agent: the one it is exposed under, else its name.
	 */
	public String localDisplayName(String agentName) {
		for (ExposedAgentConfig exposed : getConfig().getAgents()) {
			if (exposed.getAgent().equals(agentName)) {
				return displayName(exposed);
			}
		}
		return agentName == null ? appName() : agentName;
	}

	/**
	 * The artifact a reference of this application names: an artifact of an A2A task, or what an
	 * {@link A2AArtifactSource} publishes. Called in the {@code TLContext} of the reading account.
	 *
	 * @param account
	 *        The login name of the reading account.
	 * @return The artifact, or <code>null</code> if the reference is unknown or names another
	 *         application.
	 */
	public A2AArtifactSource.Artifact resolveArtifact(ArtifactRef ref, String account) {
		if (!ref.app().equals(appName())) {
			return null;
		}
		if (ArtifactRef.TASK_KIND.equals(ref.kind())) {
			return _backend.taskArtifact(ref.id(), account);
		}
		return ask(_artifactSources, source -> source.resolve(ref));
	}

	/**
	 * Asks application hooks in order: the first non-<code>null</code> answer wins. A hook that throws
	 * is logged and skipped.
	 */
	public static <H, R> R ask(List<H> hooks, Function<H, R> question) {
		for (H hook : hooks) {
			try {
				R answer = question.apply(hook);
				if (answer != null) {
					return answer;
				}
			} catch (RuntimeException ex) {
				Logger.error("A2A hook " + hook.getClass().getName() + " failed: " + ex.getMessage(), ex, A2AService.class);
			}
		}
		return null;
	}

	/**
	 * The answer of {@code GET /a2a-artifacts} for an artifact and an optional section:
	 * <code>{"ref", "name", "mediaType", "version", "sections", "section", "content"}</code>, or
	 * <code>{"error", "sections"}</code>.
	 *
	 * @param artifact
	 *        The artifact, or <code>null</code> if the reference is unknown.
	 */
	public static Map<String, Object> artifactResponse(A2AArtifactSource.Artifact artifact, ArtifactRef ref,
			String section) {
		if (artifact == null) {
			return Json.map("error", "Unknown artifact: " + ref + ".");
		}
		List<String> sections = artifact.sections() != null ? artifact.sections() : Sections.list(artifact.content());
		Object content = Sections.extract(artifact.content(), section);
		if (content == null) {
			return Json.map("error", "Artifact " + ref + " has no section '" + section + "'.", "sections", sections);
		}
		return Json.map("ref", ref.toString(), "name", artifact.name(), "mediaType", artifact.mediaType(),
			"version", artifact.version(), "sections", sections,
			"section", section == null || section.isBlank() ? null : section, "content", content);
	}

	/**
	 * The agent card of an exposed agent.
	 *
	 * @param baseUrl
	 *        The application's base URL as the client reached it; the configured {@code public-url}
	 *        wins.
	 */
	public Map<String, Object> agentCard(ExposedAgentConfig agent, String baseUrl) {
		String base = getConfig().getPublicUrl();
		base = (base == null || base.isBlank() ? baseUrl : base.trim()).replaceAll("/+$", "");
		List<Object> skills = new ArrayList<>();
		for (SkillConfig skill : agent.getSkills()) {
			skills.add(Json.map("id", skill.getId(),
				"name", skill.getName() == null || skill.getName().isBlank() ? skill.getId() : skill.getName(),
				"description", skill.getDescription() == null ? "" : skill.getDescription(),
				"tags", new ArrayList<>(skill.getTags())));
		}
		String description = agent.getDescription();
		return Json.map(
			"name", displayName(agent),
			"description", description == null || description.isBlank()
				? "TopLogic agent " + displayName(agent) + "." : description,
			"supportedInterfaces", List.of(Json.map("url", base + "/a2a/" + agent.getPath(),
				"protocolBinding", "JSONRPC", "protocolVersion", "1.0")),
			"provider", Json.map("url", base, "organization", appName()),
			"version", "1.0",
			"capabilities", Json.map("streaming", Boolean.TRUE, "pushNotifications", Boolean.FALSE,
				"extensions", List.of(Json.map("uri", ARTIFACTS_EXTENSION,
					"description", "Artifacts by reference: GET <url>?ref=tl-artifact://<app>/<kind>/<id>&section=..."
						+ " with the same credential as the JSON-RPC calls.",
					"params", Json.map("app", appName(), "url", base + "/a2a-artifacts")))),
			"securitySchemes", Json.map("bearer", Json.map("httpAuthSecurityScheme", Json.map("scheme", "Bearer"))),
			"securityRequirements", List.of(Json.map("schemes", Json.map("bearer", Json.map("list", List.of())))),
			"defaultInputModes", List.of("text/plain", "application/json"),
			"defaultOutputModes", List.of("text/plain", "application/json"),
			"skills", skills);
	}

	/**
	 * Module of {@link A2AService}.
	 */
	public static final class Module extends TypedRuntimeModule<A2AService> {

		/** The singleton. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<A2AService> getImplementation() {
			return A2AService.class;
		}
	}
}
