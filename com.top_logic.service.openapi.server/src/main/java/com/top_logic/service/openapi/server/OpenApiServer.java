/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyInitializer;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfigs;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.InfoObject;
import com.top_logic.service.openapi.common.document.SchemaObject;
import com.top_logic.service.openapi.common.document.TagObject;
import com.top_logic.service.openapi.server.HandlerForPath.CompareByParts;
import com.top_logic.service.openapi.server.authentication.AlwaysAuthenticated;
import com.top_logic.service.openapi.server.authentication.AuthenticateVisitor;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.NeverAuthenticated;
import com.top_logic.service.openapi.server.authentication.ServerSecret;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.impl.ComputationFailure;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilder;
import com.top_logic.service.openapi.server.layout.OpenAPIExporter;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.InvalidValueException;
import com.top_logic.service.openapi.server.parameter.ParameterReference;
import com.top_logic.service.openapi.server.parameter.ReferencedParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter;
import com.top_logic.util.Resources;

/**
 * Service providing a configurable REST service.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("OpenAPI server")
public class OpenApiServer extends ConfiguredManagedClass<OpenApiServer.Config<?>> {

	/**
	 * Configuration options for {@link OpenApiServer}.
	 */
	@DisplayOrder({
		Config.BASE_PATH,
		Config.INFORMATION,
		Config.GLOBAL_SCHEMAS,
		Config.GLOBAL_PARAMETERS,
		Config.AUTHENTICATIONS,
		Config.TAGS,
		Config.PATHS,
		Config.SECRETS
	})
	@Label("OpenAPI server configuration")
	public interface Config<I extends OpenApiServer> extends ConfiguredManagedClass.Config<I>, AuthenticationConfigs {
		
		/** @see #getBaseURL() */
		String BASE_PATH = "base-url";

		/** @see #getPaths() */
		String PATHS = "paths";

		/** @see #getSecrets() */
		String SECRETS = "secrets";

		/** @see #getAuthentications() */
		String AUTHENTICATIONS = "authentications";

		/** @see #getInformation() */
		String INFORMATION = "information";

		/** @see #getGlobalSchemas() */
		String GLOBAL_SCHEMAS = "global-schemas";

		/** @see #getGlobalParameters() */
		String GLOBAL_PARAMETERS = "global-parameters";

		/** @see #getTags() */
		String TAGS = "tags";

		/**
		 * The base URL under which the <i>OpenAPI</i> server can be accessed by clients.
		 * 
		 * @implSpec The actual value is the path relative to the application path.
		 */
		@Mandatory
		@ControlProvider(DisplayFullPathTemplate.class)
		String getBaseURL();

		/**
		 * Paths that trigger service methods.
		 */
		@Name(PATHS)
		@Constraint(DisjointPathes.class)
		@CollapseEntries
		List<PathItem> getPaths();

		/**
		 * Informations about the <i>OpenAPI</i> server.
		 */
		@Name(INFORMATION)
		@ItemDefault
		@NonNullable
		Information getInformation();

		/**
		 * Setter for {@link #getInformation()}.
		 */
		void setInformation(Information value);

		/**
		 * {@link AuthenticationConfig}s that can be used to authenticate requests in
		 * {@link #getPaths()}.
		 */
		@Override
		Map<String, AuthenticationConfig> getAuthentications();

		/**
		 * Configuration of the secrets that a client must use to access this server.
		 */
		@Name(SECRETS)
		List<ServerSecret> getSecrets();

		/**
		 * Global schemas that can be used within the <i>OpenAPI</i> server.
		 */
		@Key(SchemaObject.NAME_ATTRIBUTE)
		@Name(GLOBAL_SCHEMAS)
		@EntryTag("schema")
		@DisplayMinimized
		Map<String, SchemaObject> getGlobalSchemas();

		/**
		 * Definition of {@link RequestParameter} that can be used in each {@link OperationByMethod}
		 * and in each {@link PathItem}. In the concrete {@link OperationByMethod} or
		 * {@link PathItem} a {@link ParameterReference} with the same name must be configured.
		 */
		@Name(GLOBAL_PARAMETERS)
		@Key(ReferencedParameter.REFERENCE_NAME)
		@DisplayMinimized
		Map<String, ReferencedParameter> getGlobalParameters();

		/**
		 * A list of tags used by the specification with additional metadata. Each tag name in the
		 * list MUST be unique.
		 */
		@Name(TAGS)
		@Key(TagObject.NAME_ATTRIBUTE)
		@DisplayMinimized
		List<TagObject> getTags();

		/**
		 * Path to the JSP displaying the <i>OpenAPI</i> specification file.
		 */
		@StringDefault("/jsp/openapi/server/displayAPISpec.jsp")
		@Hidden
		String getDisplayAPISpecJSP();

		/**
		 * Checks that the pathes are disjoint.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public class DisjointPathes extends ValueConstraint<List<PathItem>> {

			private static final Pattern PARAMETER_PATTERN = Pattern.compile(PathItem.VARIABLE_PATTERN);

			/**
			 * Creates a new {@link DisjointPathes}.
			 */
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public DisjointPathes() {
				super((Class) List.class);
			}

			@Override
			protected void checkValue(PropertyModel<List<PathItem>> propertyModel) {
				List<PathItem> items = CollectionUtil.nonNull(propertyModel.getValue());
				switch (items.size()) {
					case 0:
					case 1:
						break;
					default:
						Map<String, PathItem> error = new HashMap<>();
						for (PathItem item : items) {
							String path = item.getCompletePath();
							String unifiedCompletePath = unifiedCompletePath(path);
							PathItem clash = error.put(unifiedCompletePath, item);
							if (clash != null) {
								propertyModel.setProblemDescription(
									I18NConstants.CLASHING_PATH_PARAMETERS__PATH1__PATH2__UNIFIED_COMPLETE_PATH
										.fill(item.getPath(), clash.getPath(), unifiedCompletePath));
							}
						}
				}
			}

			private String unifiedCompletePath(String path) {
				Matcher matcher = PARAMETER_PATTERN.matcher(path);
				if (!matcher.find()) {
					return path;
				}
				int i = 1;
				StringBuilder sb = new StringBuilder();
				do {
					matcher.appendReplacement(sb, "<var" + i++ + ">");
				} while (matcher.find());
				matcher.appendTail(sb);
				return sb.toString();
			}

		}

	}

	/**
	 * Part of an {@link OpenApiServer} configuration.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface OpenAPIServerPart extends ConfigurationItem {

		/** @see #getServerConfiguration() */
		String SERVER_CONFIGURATION = "server-configuration";

		/**
		 * The server configuration containing this configuration object.
		 */
		@Name(SERVER_CONFIGURATION)
		@Hidden
		@Abstract
		OpenApiServer.Config<? extends OpenApiServer> getServerConfiguration();

	}

	/**
	 * {@link InfoObject} holding additional globally used schemas.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Information extends InfoObject {

		@Override
		@ValueInitializer(Initializer.class)
		String getTitle();

		@Override
		@ValueInitializer(Initializer.class)
		String getVersion();

		/**
		 * {@link PropertyInitializer} for {@link Information} objects.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public class Initializer implements PropertyInitializer {

			@Override
			public Object getInitialValue(PropertyDescriptor property) {
				switch (property.getPropertyName()) {
					case Information.TITLE:
						return Resources.getInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE);
					case Information.VERSION:
						return "1.0";
					default:
						return null;
				}
			}

		}

	}

	/**
	 * Parameter for the {@link Config#getDisplayAPISpecJSP() JSP} displaying the API specification.
	 * The parameter will contain contain the URL (relative to the application server) to access the
	 * <i>OpenAPI</i> specification file.
	 */
	@CalledFromJSP
	public static final String API_URL_PARAM = "api-url";

	private Map<HttpMethod, List<HandlerForPath>> _handlers = Collections.emptyMap();

	/** Path to access the content of the current OpenAPI specification file. */
	private String _apiFilePath = "";

	/** Path of the index file which displays the OpenAPI specification file using Swagger API. */
	private String _indexFilePath = "";

	/**
	 * Content of the OpenAPI specification file indexed by the used server name: The used server
	 * depend on the determination of the host, where the application is hosted.
	 */
	private volatile Map<String, byte[]> _apiFileContent = Collections.emptyMap();

	/**
	 * Creates a {@link OpenApiServer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OpenApiServer(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		
		Config<?> config = getConfig();
		_handlers = buildHandlers(config.getPaths());
		_apiFilePath = "/" + new OpenAPIExporter(config).getExportFileName();
		URLPathBuilder builder = URLPathBuilder.newEmptyBuilder();
		builder.appendRaw(config.getDisplayAPISpecJSP());
		builder.appendParameter(API_URL_PARAM, config.getBaseURL() + _apiFilePath);
		_indexFilePath = builder.getURL();
	}

	private Map<HttpMethod, List<HandlerForPath>> buildHandlers(List<PathItem> paths) {
		Map<HttpMethod, List<HandlerForPath>> handlerMap = new HashMap<>();
		Map<String, ConcreteRequestParameter<?>> globalParameters =
			createGlobalParameters(getConfig().getGlobalParameters());
		for (PathItem pathItem : paths) {
			List<ConcreteRequestParameter<?>> pathItemParams =
				resolveParameters(pathItem.getParameters(), globalParameters);

			for (Entry<HttpMethod, OperationByMethod> entry : pathItem.getOperations().entrySet()) {
				HttpMethod method = entry.getKey();
				OperationByMethod operation = entry.getValue();

				List<ConcreteRequestParameter<?>> allParameters = new ArrayList<>(pathItemParams);
				allParameters.addAll(resolveParameters(operation.getParameters(), globalParameters));
				ServiceMethodBuilder implBuilder = TypedConfigUtil.createInstance(operation.getImplementation());

				List<String> parameterNames = allParameters.stream()
					.map(ConcreteRequestParameter::getScriptParameterNames)
					.flatMap(List::stream)
					.collect(Collectors.toList());

				String path = pathItem.getCompletePath();
				Authenticator authenticator = authenticator(operation);
				PathHandler pathHandler = new PathHandler(path, allParameters,
					implBuilder.build(path, parameterNames), authenticator);

				List<HandlerForPath> handlers = handlerMap.get(method);
				if (handlers == null) {
					handlers = new ArrayList<>();
					handlerMap.put(method, handlers);
				}

				handlers.add(new HandlerForPath(pathHandler));
			}
		}
		handlerMap.values().forEach(this::sortHandlers);
		return handlerMap;
	}

	/**
	 * Builds a map from the name of a {@link ReferencedParameter} to its definition.
	 */
	private Map<String, ConcreteRequestParameter<?>> createGlobalParameters(
			Map<String, ReferencedParameter> globalParameters) {
		Map<String, ConcreteRequestParameter<?>> result = new HashMap<>();
		for (Entry<String, ReferencedParameter> globalParam : globalParameters.entrySet()) {
			result.put(globalParam.getKey(),
				TypedConfigUtil.createInstance(globalParam.getValue().getParameterDefinition()));
		}
		return result;
	}

	private List<ConcreteRequestParameter<?>> resolveParameters(
			List<RequestParameter.Config<? extends RequestParameter<?>>> parameter,
			Map<String, ConcreteRequestParameter<?>> globalParameters) {
		Stream<ConcreteRequestParameter<?>> concreteParamsStream = parameter.stream()
			.map(paramConfig -> {
			if (paramConfig instanceof ParameterReference.Config) {
				return globalParameters.get(paramConfig.getName());
			} else {
				return TypedConfigUtil.createInstance(paramConfig);
			}
			})
			.map(ConcreteRequestParameter.class::cast);
		/* Note: Assigned to local variable because JavaC recognises an compilation failure. */
		return concreteParamsStream.collect(Collectors.toList());
	}

	private void sortHandlers(List<HandlerForPath> handlers) {
		Collections.sort(handlers, CompareByParts.INSTANCE);
	}

	private Authenticator authenticator(OperationByMethod operation) {
		Authenticator authenticator = AlwaysAuthenticated.INSTANCE;
		for (String authentication : operation.getAuthentication()) {
			Authenticator localAuthenticator;
			try {
				localAuthenticator = getConfig()
					.getAuthentications()
					.get(authentication)
					.visit(AuthenticateVisitor.INSTANCE, this);
			} catch (RuntimeException ex) {
				String path = operation.getEnclosingPathItem().getPath();
				String method = operation.getMethod().name();
				Logger.error("Unable to create authenticator for path '" + path + "' and method '" + method + "'.", ex,
					OpenApiServer.class);
				localAuthenticator =
					new NeverAuthenticated(
						I18NConstants.CREATING_AUTHENTICATOR_FAILED__PATH__METHOD.fill(path, method));
			}
			authenticator = authenticator.andThen(localAuthenticator);
		}
		return authenticator;
	}

	@Override
	protected void shutDown() {
		_handlers = Collections.emptyMap();
	}

	/**
	 * The {@link OpenApiServer} service instance.
	 */
	public static OpenApiServer getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Entry for handling requests.
	 */
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			req.getRequestDispatcher(_indexFilePath).forward(req, resp);
			return;
		}
		if (_apiFilePath.equals(pathInfo)) {
			deliverAPISpec(req, resp);
			return;
		}
		Pair<PathHandler, Map<String, String>> handler = findHandler(req.getMethod(), pathInfo);
		if (handler == null) {
			sendNotFound(resp, "No handler registered for path '" + pathInfo + "'.");
			return;
		}

		process(handler.getFirst(), handler.getSecond(), req, resp);
	}

	private void deliverAPISpec(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		byte[] openAPISpec;
		if (DefaultDisplayContext.hasDisplayContext(req)) {
			openAPISpec = getOpenAPISpec(DefaultDisplayContext.getDisplayContext(req));
		} else {
			DefaultDisplayContext dc = DefaultDisplayContext.setupDisplayContext(req.getServletContext(), req, resp);
			try {
				openAPISpec = getOpenAPISpec(dc);
			} finally {
				DefaultDisplayContext.teardownDisplayContext(req);
			}
		}

		resp.setContentLength(openAPISpec.length);
		resp.setCharacterEncoding(JsonUtilities.DEFAULT_JSON_ENCODING);
		resp.setContentType(JsonUtilities.JSON_CONTENT_TYPE);
		resp.getOutputStream().write(openAPISpec);
	}

	private byte[] getOpenAPISpec(DisplayContext context) throws IOException {
		Config<?> config = getConfig();
		String url = DisplayFullPathTemplate.createFullBaseURL(context, config.getBaseURL());
		byte[] content = _apiFileContent.get(url);
		if (content == null) {
			content = createSpecFileContent(context, config);
			synchronized (this) {
				switch (_apiFileContent.size()) {
					case 0: {
						_apiFileContent = Collections.singletonMap(url, content);
						break;
					}
					case 1: {
						Map<String, byte[]> tmp = new ConcurrentHashMap<>(_apiFileContent);
						tmp.put(url, content);
						_apiFileContent = tmp;
						break;
					}
					default: {
						_apiFileContent.put(url, content);
						break;
					}
				}
			}
		}
		return content;
	}

	private byte[] createSpecFileContent(DisplayContext context, Config<?> config) throws IOException {
		OpenAPIExporter exporter = new OpenAPIExporter(config);
		exporter.createDocument(context);
		ByteArrayStream out = new ByteArrayStream();
		exporter.deliverTo(out);
		return out.toByteArray();
	}

	private void process(PathHandler handler, Map<String, String> parameters, HttpServletRequest req,
			HttpServletResponse resp)
			throws IOException {
		try {
			handler.handleRequest(req, resp, parameters);
		} catch (InvalidValueException ex) {
			String message = "Invalid API request to '" + handler.getPath() + "': " + ex.getMessage();
			Logger.info(message, ex, OpenApiServer.class);

			responsePlainText(resp, HttpServletResponse.SC_BAD_REQUEST, message);
		} catch (ComputationFailure ex) {
			String message = "API request to '" + handler.getPath() + "' failed: " + messages(ex);
			Logger.info(message, OpenApiServer.class);

			responsePlainText(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		} catch (AuthenticationFailure ex) {
			ex.enhanceResponse(resp, handler.getPath());
		} catch (Throwable ex) {
			String message = "API request to '" + handler.getPath() + "' failed: " + messages(ex);
			Logger.error(message, ex, OpenApiServer.class);

			responsePlainText(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}
	}

	private void responsePlainText(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("text/plain");
		response.setCharacterEncoding(StringServices.UTF8);
		response.getWriter().write(message);
	}

	private static String messages(Throwable ex) {
		StringBuilder buffer = new StringBuilder();
		appendMessages(buffer, new HashSet<>(), ex);
		return buffer.toString();
	}

	private static void appendMessages(StringBuilder buffer, Set<String> seenMessages, Throwable ex) {
		String message = getMessage(ex);
		if (seenMessages.add(message)) {
			appendMessage(buffer, message);
		}

		Throwable cause = ex.getCause();
		if (cause != null) {
			appendMessages(buffer, seenMessages, cause);
		}
	}

	private static String getMessage(Throwable ex) {
		String message = ex.getMessage();
		if (message == null) {
			message = ex.getClass().getName();
		}
		return message;
	}

	private static void appendMessage(StringBuilder buffer, String message) {
		int length = buffer.length();
		if (length > 0) {
			if (buffer.charAt(length - 1) == '.') {
				buffer.setLength(length - 1);
			}
			buffer.append(": ");
		}
		buffer.append(message);
	}

	private Pair<PathHandler, Map<String, String>> findHandler(String methodName, String pathInfo) {
		HttpMethod method;
		try {
			method = HttpMethod.valueOf(methodName.toUpperCase());
		} catch (RuntimeException ex) {
			method = HttpMethod.POST;
		}
		return findHandlerForMethod(method, pathInfo);
	}

	private Pair<PathHandler, Map<String, String>> findHandlerForMethod(HttpMethod method, String pathInfo) {
		List<HandlerForPath> pathValues = _handlers.get(method);
		if (pathValues != null) {
			for (HandlerForPath pathValue : pathValues) {
				Map<String, String> variableValues = pathValue.matches(pathInfo);
				if (variableValues != null) {
					return TupleFactory.pair(pathValue.getHandler(), variableValues);
				}
			}
		}

		// If no handler is configured, use ANY as default.
		return method == null ? null : findHandlerForMethod(null, pathInfo);
	}

	private void sendNotFound(HttpServletResponse resp, String message) throws IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND, message);
	}

	/**
	 * Singleton reference to {@link OpenApiServer} service.
	 */
	public static final class Module extends TypedRuntimeModule<OpenApiServer> {
		/**
		 * Singleton {@link OpenApiServer.Module} instance.
		 */
		public static final OpenApiServer.Module INSTANCE = new OpenApiServer.Module();
	
		private Module() {
			// Singleton constructor.
		}
	
		@Override
		public Class<OpenApiServer> getImplementation() {
			return OpenApiServer.class;
		}
	}
}
