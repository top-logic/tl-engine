/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.treexf.TreeMaterializer.Factory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.config.MethodResolver;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.client.authentication.ClientSecret;
import com.top_logic.service.openapi.client.authentication.NoSecurityEnhancement;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancerVisitor;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.conf.ParameterDefinition;
import com.top_logic.service.openapi.client.registry.conf.ResponseForStatusCodes;
import com.top_logic.service.openapi.client.registry.impl.CallHandler;
import com.top_logic.service.openapi.client.registry.impl.MethodDefinitionDocumentation;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilder;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;
import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;
import com.top_logic.service.openapi.client.registry.impl.response.DispatchingResponseHandler;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseChecker;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandler;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandlerFactory;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfigs;

/**
 * Service allowing to configure client end-points for external APIs that can be called through
 * TL-Script functions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceExtensionPoint(SearchBuilder.Module.class)
public class ServiceMethodRegistry extends ConfiguredManagedClass<ServiceMethodRegistry.Config<?>>
		implements MethodResolver {

	private Map<String, ServiceMethodBuilder> _builders;

	private HttpContext _requestContext;

	/**
	 * Configuration options for {@link ServiceMethodRegistry}.
	 */
	public interface Config<I extends ServiceMethodRegistry>
			extends ConfiguredManagedClass.Config<I>, AuthenticationConfigs {

		/** @see #getMethodDefinitions() */
		String METHOD_DEFINITIONS = "method-definitions";

		/** @see #getAuthentications() */
		String AUTHENTICATIONS = "authentications";

		/** @see #getSecrets() */
		String SECRETS = "secrets";

		/**
		 * Definitions for user-defined TL-Script functions invoking external APIs.
		 */
		@Name(METHOD_DEFINITIONS)
		@Key(MethodDefinition.NAME_ATTRIBUTE)
		Map<String, MethodDefinition> getMethodDefinitions();

		/**
		 * {@link AuthenticationConfig}s that can be used to authenticate requests in
		 * {@link #getMethodDefinitions()}.
		 */
		@Override
		Map<String, AuthenticationConfig> getAuthentications();

		/**
		 * Configuration of the secrets that can be used to deliver to the Open API server.
		 */
		@Key(ClientSecret.DOMAIN)
		@Name(SECRETS)
		Map<String, ClientSecret> getSecrets();

	}

	/**
	 * Super class for contents of a {@link ServiceMethodRegistry.Config}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface ServiceRegistryPart extends ConfigPart {

		/*** @see #getServiceRegistry() */
		String SERVICE_REGISTRY = "service-registry";

		/**
		 * Configuration of the {@link ServiceMethodRegistry} this is a part from.
		 */
		@Container
		@Name(SERVICE_REGISTRY)
		@Hidden
		ServiceMethodRegistry.Config<?> getServiceRegistry();

	}

	/**
	 * Creates a {@link ServiceMethodRegistry} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ServiceMethodRegistry(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();

		HashMap<String, ServiceMethodBuilder> builders = new HashMap<>();
		for (Entry<String, MethodDefinition> method : getConfig().getMethodDefinitions().entrySet()) {
			builders.put(method.getKey(), createBuilder(method.getValue()));
		}
		_builders = builders;
		_builders.values().forEach(ServiceMethodBuilder::init);

		_requestContext = createContext();
	}

	@Override
	protected void shutDown() {
		_builders = null;
		_requestContext = null;
		super.shutDown();
	}

	/**
	 * Creates the {@link HttpContext} that is used to execute the actual request.
	 */
	protected HttpContext createContext() {
		return new BasicHttpContext();
	}

	private ServiceMethodBuilder createBuilder(MethodDefinition method) {
		String methodName = method.getName();
		String baseUrl = method.getBaseUrl();
		List<ParameterDefinition> parameters = method.getParameters();
		List<CallBuilderFactory> serviceArguments = TypedConfigUtil.createInstanceList(method.getCallBuilders());

		List<String> parameterNames =
			parameters.stream().map(ParameterDefinition::getName).collect(Collectors.toList());
		Map<String, Integer> parameterIndex = MapUtil.createIndexMap(parameterNames);

		MethodSpec methodSpec = new MethodSpec() {
			@Override
			public List<String> getParameterNames() {
				return parameterNames;
			}

			@Override
			public int getParameterIndex(String name) {
				Integer result = parameterIndex.get(name);
				if (result == null) {
					Logger.error("Undefined parameter '" + name + "' in method '" + methodName + "'.",
						ServiceMethodRegistry.class);
					return 0;
				}
				return result;
			}
		};

		List<CallBuilder> modifiers =
			serviceArguments.stream().map(sa -> sa.createRequestModifier(methodSpec)).collect(Collectors.toList());

		int parameterCount = parameters.size();
		int lastMandatoryIndex = -1;
		QueryExecutor[] defaultValues = new QueryExecutor[parameterCount];
		for (int n = 0; n < parameterCount; n++) {
			ParameterDefinition parameter = parameters.get(n);
			defaultValues[n] = QueryExecutor.compileOptional(parameter.getDefaultValue());
			if (parameter.isRequired()) {
				lastMandatoryIndex = n;
			}
		}

		int minArgs = lastMandatoryIndex + 1;

		String httpMethod = method.getHttpMethod().name();

		SecurityEnhancer enhancer = securityEnhancer(method);

		CallHandler handler = new CallHandler() {

			private ResponseHandler _responseHandler;

			@Override
			public Object execute(Object self, Object[] arguments) throws Exception {
				Object[] allArguments;
				if (arguments.length < parameterCount) {
					allArguments = new Object[parameterCount];
					System.arraycopy(arguments, 0, allArguments, 0, arguments.length);

					// Fill default values.
					int firstDefault = arguments.length;
					for (int n = firstDefault; n < parameterCount; n++) {
						QueryExecutor defaultValue = defaultValues[n];
						allArguments[n] = defaultValue == null ? null : defaultValue.execute(allArguments);
					}
				} else {
					allArguments = arguments;
				}

				Call call = Call.newInstance(allArguments);
				UriBuilder urlBuilder = new UriBuilder(baseUrl);
				for (CallBuilder modifier : modifiers) {
					modifier.buildUrl(urlBuilder, call);
				}
				enhancer.enhanceUrl(urlBuilder);
				URI uri = urlBuilder.build();

				Logger.debug("Calling external API: " + uri.toString(), ServiceMethodRegistry.class);

				BasicClassicHttpRequest request = new BasicClassicHttpRequest(httpMethod, uri);
				for (CallBuilder modifier : modifiers) {
					modifier.buildRequest(request, call);
				}

				try (final CloseableHttpClient httpclient = enhancer.enhanceClient(HttpClients.custom()).build()) {
					enhancer.enhanceRequest(httpclient, request);
					try (CloseableHttpResponse response = httpclient.execute(request, _requestContext)) {
						return _responseHandler.handle(method, call, response);
					}
				}
			}

			@Override
			public void init() {
				ResponseHandler defaultResponseHandler = createResponseHandler(method.getResponseHandler(), methodSpec);
				defaultResponseHandler = new ResponseChecker(defaultResponseHandler);
				List<ResponseForStatusCodes> optionalResponses = method.getOptionalResponseHandlers();

				ResponseHandler responseHandler;
				if (optionalResponses.isEmpty()) {
					responseHandler = defaultResponseHandler;
				} else {
					DispatchingResponseHandler dispatchingHandler =
						new DispatchingResponseHandler(defaultResponseHandler);
					for (ResponseForStatusCodes responseByCode : optionalResponses) {
						for (String statusCode : responseByCode.getStatusCodes()) {
							dispatchingHandler.registerHandler(statusCode,
								createResponseHandler(responseByCode.getResponseHandler(), methodSpec));
						}
					}
					responseHandler = dispatchingHandler;
				}
				_responseHandler = responseHandler;
			}
		};

		return new ServiceMethodBuilder(methodName, minArgs, handler);
	}

	private static ResponseHandler createResponseHandler(
			PolymorphicConfiguration<? extends ResponseHandlerFactory> factory,
			MethodSpec methodSpec) {
		return TypedConfigUtil.createInstance(factory).create(methodSpec);
	}

	private SecurityEnhancer securityEnhancer(MethodDefinition method) {
		SecurityEnhancer enhancer = NoSecurityEnhancement.INSTANCE;
		for (String authenticationName : method.getAuthentication()) {
			AuthenticationConfig authentication = getConfig().getAuthentications().get(authenticationName);
			enhancer = enhancer.andThen(authentication.visit(SecurityEnhancerVisitor.INSTANCE, this));
		}
		return enhancer;
	}

	@Override
	public Factory getFactory(Object type) {
		return _builders.get(type);
	}

	@Override
	public Collection<String> getMethodNames() {
		return Collections.unmodifiableCollection(_builders.keySet());
	}

	@Override
	public MethodBuilder<?> getMethodBuilder(String functionName) {
		return _builders.get(functionName);
	}

	@Override
	public Optional<String> getDocumentation(DisplayContext context, String functionName) {
		MethodDefinition methodDefinition = getConfig().getMethodDefinitions().get(functionName);
		if (methodDefinition == null) {
			return Optional.empty();
		}
		StringWriter sw = new StringWriter();
		try (TagWriter out = new TagWriter(sw)) {
			new MethodDefinitionDocumentation(methodDefinition).write(context, out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return Optional.of(sw.toString());
	}

	/**
	 * Plug-in to the {@link SearchBuilder} allowing to inject TL-Script function that call external
	 * APIs.
	 * 
	 * <p>
	 * This class is registered in the {@link ServiceMethodBuilder} configuration.
	 * </p>
	 */
	public static final class ServiceMethodResolver implements MethodResolver {

		/**
		 * Singleton {@link ServiceMethodRegistry.ServiceMethodResolver} instance.
		 */
		@CalledByReflection
		public static final ServiceMethodResolver INSTANCE = new ServiceMethodResolver();

		private ServiceMethodResolver() {
			// Singleton constructor.
		}

		@Override
		public Collection<String> getMethodNames() {
			if (!isActive()) {
				return Collections.emptyList();
			}
			return getInstance().getMethodNames();
		}

		@Override
		public Factory getFactory(Object type) {
			if (!isActive()) {
				return null;
			}
			return getInstance().getFactory(type);
		}

		@Override
		public MethodBuilder<?> getMethodBuilder(String functionName) {
			if (!isActive()) {
				return null;
			}
			return getInstance().getMethodBuilder(functionName);
		}

		@Override
		public Optional<String> getDocumentation(DisplayContext context, String functionName) {
			if (!isActive()) {
				return null;
			}
			return getInstance().getDocumentation(context, functionName);
		}

	}

	/**
	 * Singleton instance of the {@link ServiceMethodRegistry} service.
	 */
	public static ServiceMethodRegistry getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Whether the {@link ServiceMethodRegistry} service is active.
	 */
	public static boolean isActive() {
		return Module.INSTANCE.isActive();
	}

	/**
	 * Singleton reference for {@link ServiceMethodRegistry}.
	 */
	public static class Module extends TypedRuntimeModule<ServiceMethodRegistry> {

		/**
		 * Singleton {@link ServiceMethodRegistry.Module} instance.
		 */
		public static final ServiceMethodRegistry.Module INSTANCE = new ServiceMethodRegistry.Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ServiceMethodRegistry> getImplementation() {
			return ServiceMethodRegistry.class;
		}

	}

}
