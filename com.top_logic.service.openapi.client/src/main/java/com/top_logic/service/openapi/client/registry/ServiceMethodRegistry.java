/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpException;
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
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.config.MethodResolver;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelPartRef;
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
			public String getMethodName() {
				return methodName;
			}

			@Override
			public List<String> getParameterNames() {
				return parameterNames;
			}

			@Override
			public int getParameterIndex(String name) {
				Integer result = parameterIndex.get(name);
				if (result == null) {
					Logger.error("Undefined parameter '" + name + "' in method '" + getMethodName() + "'.",
						ServiceMethodRegistry.class);
					return 0;
				}
				return result;
			}
		};

		List<CallBuilder> modifiers =
			serviceArguments.stream().map(sa -> sa.createRequestModifier(methodSpec)).collect(Collectors.toList());

		String httpMethod = method.getHttpMethod().name();

		SecurityEnhancer enhancer = securityEnhancer(method);

		CallHandler handler = new CallHandler() {

			private ResponseHandler _responseHandler;

			@Override
			public Object execute(Object self, Object[] arguments) throws Exception {
				Call call = Call.newInstance(adaptValuesToExpectedType(arguments));
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
					return httpclient.execute(request, _requestContext, response -> {
						try {
							return _responseHandler.handle(method, call, response);
						} catch (RuntimeException ex) {
							throw ex;
						} catch (IOException | HttpException ex) {
							throw ex;
						} catch (Exception ex) {
							throw new IOException("Problem processing response.", ex);
						}
					});
				}
			}

			/**
			 * The arguments are given by a TL-Script function. TL-Script always works with
			 * {@link Double} as {@link Number} implementation. Therefore such a value must be
			 * adapted to have expected parameter type.
			 */
			private Object[] adaptValuesToExpectedType(Object[] arguments) {
				if (parameters.size() != arguments.length) {
					Logger.error("Expected " + parameters.size() + " arguments but got " + arguments.length,
						ServiceMethodRegistry.class);
					return arguments;
				}
				Object[] transformedArgs = arguments;
				for (int i = 0; i < arguments.length; i++) {
					Object arg = arguments[i];
					if (arg == null) {
						continue;
					}
					ParameterDefinition param = parameters.get(i);
					Object transformed;
					if (arg instanceof Set) {
						transformed = transformCollection((Set<?>) arg, param, LinkedHashSet::new);
					} else if (arg instanceof List) {
						transformed = transformCollection((List<?>) arg, param, ArrayList::new);
					} else if (arg.getClass().isArray()) {
						transformed = transformArray((Object[]) arg, param);
					} else {
						transformed = transformArgument(arg, param);
					}
					if (transformed == arg) {
						// Actually no change
						continue;
					}
					if (transformedArgs == arguments) {
						// No change until now. Create copy.
						transformedArgs = Arrays.copyOf(arguments, arguments.length);
					}
					transformedArgs[i] = arg;
				}
				return transformedArgs;
			}
			
			private Object transformArray(Object[] arg, ParameterDefinition param) {
				if (!param.isMultiple()) {
					return arg;
				}
				Object[] copy = Arrays.copyOf(arg, Array.getLength(arg));
				if (!transformInline(copy, param)) {
					return arg;
				}
				return copy;
			}

			private Object transformCollection(Collection<?> arg, ParameterDefinition param,
					Supplier<Collection<Object>> factory) {
				if (!param.isMultiple()) {
					return arg;
				}
				Object[] content = arg.toArray();
				if (!transformInline(content, param)) {
					return arg;
				}
				Collection<Object> result = factory.get();
				Collections.addAll(result, content);
				return result;
			}

			private boolean transformInline(Object[] args, ParameterDefinition param) {
				boolean anyChange = false;
				for (int i = 0; i < args.length; i++) {
					Object arg = args[i];
					Object transformed = transformArgument(arg, param);
					if (arg == transformed) {
						// Actually no transformation.
						continue;
					}
					anyChange = true;
					args[i] = transformed;
				}
				return anyChange;
			}

			private Object transformArgument(Object arg, ParameterDefinition param) {
				if (!(arg instanceof Number)) {
					return arg;
				}
				TLModelPartRef paramType = param.getType();
				if (paramType == null) {
					return arg;
				}
				switch (paramType.qualifiedName()) {
					case TypeSpec.DOUBLE_TYPE: {
						if (!(arg instanceof Double)) {
							arg = Double.valueOf(((Number) arg).doubleValue());
						}
						break;
					}
					case TypeSpec.FLOAT_TYPE: {
						if (!(arg instanceof Float)) {
							arg = Float.valueOf(((Number) arg).floatValue());
						}
						break;
					}
					case TypeSpec.LONG_TYPE: {
						if (!(arg instanceof Long)) {
							arg = Long.valueOf(((Number) arg).longValue());
						}
						break;
					}
					case TypeSpec.INTEGER_TYPE: {
						if (!(arg instanceof Integer)) {
							arg = Integer.valueOf(((Number) arg).intValue());
						}
						break;
					}
				}
				return arg;
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

		return new ServiceMethodBuilder(method, handler);
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
