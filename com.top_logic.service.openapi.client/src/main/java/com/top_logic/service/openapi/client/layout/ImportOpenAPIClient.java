/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.layout;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.admin.component.TLServiceConfigEditorFormBuilder;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.conf.ParameterDefinition;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.client.registry.impl.call.request.CookieHeaderArgument;
import com.top_logic.service.openapi.client.registry.impl.call.request.HeaderArgument;
import com.top_logic.service.openapi.client.registry.impl.call.request.JSONRequestBody;
import com.top_logic.service.openapi.client.registry.impl.call.request.SimpleHeaderArgument;
import com.top_logic.service.openapi.client.registry.impl.call.uri.PathArgument;
import com.top_logic.service.openapi.client.registry.impl.call.uri.QueryArgument;
import com.top_logic.service.openapi.client.registry.impl.call.uri.SimpleQueryArgument;
import com.top_logic.service.openapi.client.registry.impl.value.ConstantValue;
import com.top_logic.service.openapi.client.registry.impl.value.ParameterValue;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducerFactory;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.IParameterObject;
import com.top_logic.service.openapi.common.document.MediaTypeObject;
import com.top_logic.service.openapi.common.document.OpenapiDocument;
import com.top_logic.service.openapi.common.document.OperationObject;
import com.top_logic.service.openapi.common.document.ParameterObject;
import com.top_logic.service.openapi.common.document.PathItemObject;
import com.top_logic.service.openapi.common.document.ReferencableParameterObject;
import com.top_logic.service.openapi.common.document.RequestBodyObject;
import com.top_logic.service.openapi.common.document.ServerObject;
import com.top_logic.service.openapi.common.layout.ImportOpenAPIConfiguration;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link CommandHandler} importing an <i>OpenAPI</i> client.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImportOpenAPIClient extends ImportOpenAPIConfiguration {

	/**
	 * Configuration for the {@link ImportOpenAPIClient}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ImportOpenAPIConfiguration.Config {

		// Currently no additional configuration.

	}

	/**
	 * Creates a new {@link ImportOpenAPIClient}.
	 */
	public ImportOpenAPIClient(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void applyConfig(OpenapiDocument config, LayoutComponent component, Object model, List<ResKey> warnings) {
		EditComponent editor = (EditComponent) component;

		FormContext formContext = editor.getFormContext();
		TLServiceConfigEditorFormBuilder.EditModel m =
			(TLServiceConfigEditorFormBuilder.EditModel) EditorFactory.getModel(formContext);
		ServiceMethodRegistry.Config<?> serviceConfiguration =
			(ServiceMethodRegistry.Config<?>) m.getServiceConfiguration();

		addAuthentications(config, serviceConfiguration, warnings);
		addMethods(config, serviceConfiguration, warnings);
	}

	private void addMethods(OpenapiDocument config, ServiceMethodRegistry.Config<?> serviceConfiguration,
			List<ResKey> warnings) {
		String globalURL = findGlobalURL(config);
		for (PathItemObject path : config.getPaths().values()) {
			String pathName = synthesizeMethodName(path);
			String localURL = firstServerURL(path.getServers());
			String url = localURL != null ? localURL : globalURL;
			Map<String, MethodDefinition> methods = serviceConfiguration.getMethodDefinitions();

			createMethod(methods, config, path.getGet(), HttpMethod.GET, pathName, url, path, warnings);
			createMethod(methods, config, path.getPut(), HttpMethod.PUT, pathName, url, path, warnings);
			createMethod(methods, config, path.getPost(), HttpMethod.POST, pathName, url, path, warnings);
			createMethod(methods, config, path.getDelete(), HttpMethod.DELETE, pathName, url, path, warnings);
			createMethod(methods, config, path.getOptions(), HttpMethod.OPTIONS, pathName, url, path, warnings);
			createMethod(methods, config, path.getHead(), HttpMethod.HEAD, pathName, url, path, warnings);
			createMethod(methods, config, path.getPatch(), HttpMethod.PATCH, pathName, url, path, warnings);
			createMethod(methods, config, path.getTrace(), HttpMethod.TRACE, pathName, url, path, warnings);
		}
	}

	private void createMethod(Map<String, MethodDefinition> methods, OpenapiDocument completeAPI,
			OperationObject operation, HttpMethod httpMethod, String baseName, String url, PathItemObject path,
			List<ResKey> warnings) {
		if (operation == null) {
			return;
		}

		MethodDefinition newMethod = TypedConfiguration.newConfigItem(MethodDefinition.class);
		newMethod.setName(getMethodName(operation, httpMethod, baseName));
		newMethod.setBaseUrl(url);
		String description = operation.getDescription();
		if (description != null && !description.isBlank()) {
			newMethod.setDescription(description);
		} else {
			String summary = operation.getSummary();
			if (summary != null && !summary.isBlank()) {
				newMethod.setDescription(summary);
			}
		}
		newMethod.setHttpMethod(httpMethod);
		addParameters(newMethod, completeAPI, operation, path, warnings);
		addSecurity(newMethod, completeAPI, operation);
		methods.put(newMethod.getName(), newMethod);
	}

	private void addParameters(MethodDefinition newMethod, OpenapiDocument completeAPI, OperationObject operation,
			PathItemObject path, List<ResKey> warnings) {
		Pattern paramNamePattern = Pattern.compile(ParameterDefinition.NAME_PATTERN);
		Set<String> parameterNames = new HashSet<>();
		for (ParameterObject parameter : mergeParameters(completeAPI, path, operation, warnings)) {
			String requiredParameterName = getParameterName(parameter);
			String parameterName = toValidParamName(paramNamePattern, requiredParameterName);

			boolean newName = parameterNames.add(parameterName);
			if (!newName) {
				String clashingName = parameterName;
				int nameIDX = 1;
				do {
					parameterName = clashingName + nameIDX;
					if (parameterNames.add(parameterName)) {
						break;
					}
					nameIDX++;
				} while (true);
			}

			ParameterDefinition paramDef = createParameter(parameterName, parameter, warnings);
			newMethod.getParameters().add(paramDef);

			switch (parameter.getIn()) {
				case COOKIE:
					CookieHeaderArgument.Config<?> cookieArgument;
					if (requiredParameterName != parameterName) {
						cookieArgument = newConfigForImplementation(CookieHeaderArgument.class);
						cookieArgument.setValue(newParameterValue(parameterName));
					} else {
						cookieArgument = newConfigForImplementation(CookieHeaderArgument.class);
					}
					cookieArgument.setName(requiredParameterName);
					newMethod.getCallBuilders().add(cookieArgument);
					break;
				case HEADER:
					HeaderArgument.Config<?> headerArgument;
					if (requiredParameterName != parameterName) {
						headerArgument = newConfigForImplementation(HeaderArgument.class);
						headerArgument.setValue(newParameterValue(parameterName));
					} else {
						headerArgument = newConfigForImplementation(SimpleHeaderArgument.class);
					}
					headerArgument.setName(requiredParameterName);
					newMethod.getCallBuilders().add(headerArgument);
					break;
				case PATH:
					// Path parameters are processed later
					break;
				case QUERY:
					QueryArgument.Config<?> queryArgument;
					if (requiredParameterName != parameterName) {
						queryArgument = newConfigForImplementation(QueryArgument.class);
						queryArgument.setValue(newParameterValue(parameterName));
					} else {
						queryArgument = newConfigForImplementation(SimpleQueryArgument.class);
					}
					queryArgument.setName(requiredParameterName);
					newMethod.getCallBuilders().add(queryArgument);
					break;
			}
		}
		if (newMethod.getHttpMethod().supportsBody()) {
			RequestBodyObject requestBody = operation.getRequestBody();
			if (requestBody != null) {
				MediaTypeObject mediaObject = requestBody.getContent().values().iterator().next();
				String schema = mediaObject.getSchema();
				String description = requestBody.getDescription();
				ParameterDefinition paramDef = createParameter(requestBodyParameterName(parameterNames), description,
					requestBody.isRequired(), schema, warnings);
				newMethod.getParameters().add(paramDef);
				try {
					JSONRequestBody.Config<?> bodyArgument =
							newConfigForImplementation(JSONRequestBody.class);
					bodyArgument.setJson(
						ExprFormat.INSTANCE.getValue(JSONRequestBody.Config.JSON, "$" + paramDef.getName()));
					newMethod.getCallBuilders().add(bodyArgument);
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
			}
		} else {
			// Body is either not allowed or behaviour is undefined. In this case a consumer
			// SHALL ignore it.
		}

		handlePathParameters(newMethod, path.getPath());
	}

	/**
	 * @param paramNamePattern
	 *        Compiled Pattern fetched from {@link ParameterDefinition#NAME_PATTERN}.
	 */
	private String toValidParamName(Pattern paramNamePattern, String origParameterName) {
		Matcher matcher = paramNamePattern.matcher(origParameterName);
		if (matcher.matches()) {
			return origParameterName;
		}
		if (origParameterName.length() == 0) {
			return "param";
		}
		StringBuilder newName = new StringBuilder();
		int index = 0;
		char firstChar = origParameterName.charAt(index);
		if (!('A' <= firstChar && firstChar <= 'Z') && !('a' <= firstChar && firstChar <= 'z')) {
			newName.append("p_");
		}
		do {
			char c = origParameterName.charAt(index++);
			if ('a' <= c && c <= 'z') {
				newName.append(c);
				continue;
			}
			if ('A' <= c && c <= 'Z') {
				newName.append(c);
				continue;
			}
			if ('0' <= c && c <= '9') {
				newName.append(c);
				continue;
			}
			if ('_' == c) {
				newName.append(c);
				continue;
			}
			newName.append('_');
		} while (index < origParameterName.length());

		return newName.toString();
	}

	private ParameterValue.Config<?> newParameterValue(String name) {
		ParameterValue.Config<?> param = newConfigForImplementation(ParameterValue.class);
		param.setParameter(name);
		return param;
	}

	private String requestBodyParameterName(Set<String> parameterNames) {
		String baseName = "body";
		if (parameterNames.contains(baseName)) {
			int i = 1;
			while (i < Integer.MAX_VALUE) {
				String requestBodyParamName = baseName + i;
				if (!parameterNames.contains(requestBodyParamName)) {
					return requestBodyParamName;
				}
				i++;
			}
			throw new IllegalArgumentException("Unable to determine parameter name for request body.");
		} else {
			return baseName;
		}
	}

	private void addSecurity(MethodDefinition newMethod, OpenapiDocument completeAPI, OperationObject operation) {
		Map<String, List<String>> securityConfig;
		List<Map<String, List<String>>> localSecurities = operation.getSecurity();
		if (localSecurities.isEmpty()) {
			if (operation.valueSet(operation.descriptor().getProperty(OperationObject.SECURITY))) {
				// Empty security array is set explicitly => security was removed;
				return;
			}
			List<Map<String, List<String>>> globalSecurities = completeAPI.getSecurity();
			if (globalSecurities.isEmpty()) {
				// No security at all.
				return;
			}
			securityConfig = globalSecurities.get(0);
		} else {
			securityConfig = localSecurities.get(0);
		}
		newMethod.setAuthentication(new ArrayList<>(securityConfig.keySet()));
	}

	private void handlePathParameters(MethodDefinition newMethod, String path) {
		List<PolymorphicConfiguration<? extends CallBuilderFactory>> pathConfigs = new ArrayList<>();
		String[] parts = path.split("/");
		for (String pathPart : parts) {
			if (pathPart.isEmpty()) {
				continue;
			}
			PolymorphicConfiguration<? extends ValueProducerFactory> value;
			if (pathPart.startsWith("{") && pathPart.endsWith("}")) {
				ParameterValue.Config<?> paramValue = newConfigForImplementation(ParameterValue.class);
				String paramName = pathPart.substring(1, pathPart.length() - 1);
				paramValue.setParameter(paramName);
				value = paramValue;
			} else {
				ConstantValue.Config<?> constantValue = newConfigForImplementation(ConstantValue.class);
				constantValue.setValue(pathPart);
				value = constantValue;
			}
			PathArgument.Config<?> pathArgument = newConfigForImplementation(PathArgument.class);
			pathArgument.setValue(value);
			pathConfigs.add(pathArgument);
		}
		newMethod.getCallBuilders().addAll(0, pathConfigs);
	}

	private Collection<ParameterObject> mergeParameters(OpenapiDocument completeAPI, PathItemObject path,
			OperationObject operation, List<ResKey> warnings) {
		List<IParameterObject> pathParams = path.getParameters();
		List<IParameterObject> operationParams = operation.getParameters();
		Map<String, ReferencableParameterObject> globalParameters = globalParameters(completeAPI);
		LinkedHashMap<Object, ParameterObject> tmp = new LinkedHashMap<>();
		for (IParameterObject param : pathParams) {
			ParameterObject paramObj = resolveParameterObject(param, globalParameters, warnings);
			if (paramObj != null) {
				tmp.put(uniqueIdentifier(paramObj), paramObj);
			}
		}
		/* Local parameters may override global parameters. */
		for (IParameterObject param : operationParams) {
			ParameterObject paramObj = resolveParameterObject(param, globalParameters, warnings);
			if (paramObj != null) {
				tmp.put(uniqueIdentifier(paramObj), paramObj);
			}
		}
		return tmp.values();
	}

	private ParameterDefinition createParameter(String parameterName, ParameterObject parameter,
			List<ResKey> warnings) {
		return createParameter(parameterName, parameter.getDescription(), parameter.isRequired(), parameter.getSchema(),
			warnings);
	}

	private String getParameterName(ParameterObject parameter) {
		return parameter.getName();
	}

	private ParameterDefinition createParameter(String paramName, String description, boolean isRequired,
			String schema, List<ResKey> warnings) {
		ParameterDefinition newParameterDef = TypedConfiguration.newConfigItem(ParameterDefinition.class);
		newParameterDef.setName(paramName);
		newParameterDef.setRequired(isRequired);
		if (description != null && !description.isBlank()) {
			newParameterDef.setDescription(description);
		}
		if (!StringServices.isEmpty(schema)) {
			try (JsonReader reader = new JsonReader(new StringR(schema))) {
				reader.beginObject();
				String primitiveType = StringServices.EMPTY_STRING, primitiveFormat = StringServices.EMPTY_STRING;
				outerLoop:
				while (reader.hasNext()) {
					switch (reader.nextName()) {
						case "type": {
							String typeName = reader.nextString();
							if (typeName.equals("array")) {
								newParameterDef.setMultiple(true);
							} else {
								primitiveType = typeName;
								if (!primitiveFormat.isEmpty()) {
									skipObjectEntries(reader);
									break outerLoop;
								}
							}
							break;
						}
						case "items": {
							reader.beginObject();
							primitiveType = primitiveFormat = StringServices.EMPTY_STRING;
							innerLoop:
							while (reader.hasNext()) {
								switch (reader.nextName()) {
									case "type": {
										primitiveType = reader.nextString();
										if (!primitiveFormat.isEmpty()) {
											skipObjectEntries(reader);
											break innerLoop;
										}
										break;
									}
									case "format": {
										primitiveFormat = reader.nextString();
										if (!primitiveType.isEmpty()) {
											skipObjectEntries(reader);
											break innerLoop;
										}
										break;
									}
									default:
										reader.skipValue();
										break;
								}
							}
							reader.endObject();
							break;
						}
						case "format": {
							primitiveFormat = reader.nextString();
							if (!primitiveType.isEmpty()) {
								skipObjectEntries(reader);
								break outerLoop;
							}
							break;
						}
						default: {
							reader.skipValue();
						}
					}
				}
				if (!primitiveType.isEmpty()) {
					addTypeRefFromSchemaType(newParameterDef, primitiveType, primitiveFormat, warnings);
				}
				reader.endObject();
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		}

		return newParameterDef;
	}

	void skipObjectEntries(JsonReader r) throws IOException {
		while (r.hasNext()) {
			r.nextName();
			r.skipValue();
		}
	}

	private void addTypeRefFromSchemaType(ParameterDefinition newParameterDef, String typeName, String typeFormat,
			List<ResKey> warnings) {
		String typespec;
		switch (typeName) {
			case "string":
				switch (typeFormat) {
					case "date":
						typespec = TypeSpec.DATE_TYPE;
						break;
					case "date-time":
						typespec = TypeSpec.DATE_TIME_TYPE;
						break;
					default:
						typespec = TypeSpec.STRING_TYPE;
						break;
				}
				break;
			case "number":
				switch (typeFormat) {
					case "float":
						typespec = TypeSpec.FLOAT_TYPE;
						break;
					case "double":
						typespec = TypeSpec.DOUBLE_TYPE;
						break;
					default:
						typespec = TypeSpec.FLOAT_TYPE;
						break;
				}
				break;
			case "integer":
				switch (typeFormat) {
					case "int32":
						typespec = TypeSpec.INTEGER_TYPE;
						break;
					case "int64":
						typespec = TypeSpec.LONG_TYPE;
						break;
					default:
						typespec = TypeSpec.INTEGER_TYPE;
						break;
				}
				break;
			case "boolean":
				typespec = TypeSpec.BOOLEAN_TYPE;
				break;
			case "object":
				typespec = TypeSpec.JSON_TYPE;
				break;
			default:
				warnings.add(I18NConstants.UNSUPPORTED_PARAMETER_TYPE__PARAMETER__TYPE.fill(newParameterDef.getName(),
					typeName));
				return;
		}
		newParameterDef.setType(TLModelPartRef.ref(typespec));
	}

	private String getMethodName(OperationObject operation, HttpMethod httpMethod, String baseName) {
		String operationId = operation.getOperationId();
		if (!operationId.isEmpty()) {
			return operationId;
		}
		switch (httpMethod) {
			case DELETE:
				return "delete_" + baseName;
			case GET:
				return "get_" + baseName;
			case HEAD:
				return "head_" + baseName;
			case OPTIONS:
				return "options_" + baseName;
			case PATCH:
				return "patch_" + baseName;
			case POST:
				return "post_" + baseName;
			case PUT:
				return "put_" + baseName;
			case TRACE:
				return "trace_" + baseName;
			default:
				throw new UnreachableAssertion("Unknown method: " + httpMethod);

		}
	}

	private String findGlobalURL(OpenapiDocument config) {
		String firstServerURL = firstServerURL(config.getServers());
		if (firstServerURL != null) {
			return firstServerURL;
		}
		/* When no servers object is given; the URL is "/" as defined in OpenAPI Specification */
		return "/";
	}

	private String firstServerURL(List<ServerObject> servers) {
		if (servers.size() == 0) {
			return null;
		}
		return servers.iterator().next().getUrl();
	}

	private String synthesizeMethodName(PathItemObject item) {
		String path = item.getPath();
		char replacement = '_';

		StringBuilder name = new StringBuilder();
		boolean lastCharReplaced = false;
		for (int index = 0; index < path.length(); index++) {
			char c = path.charAt(index);
			if (isIllegalChar(c)) {
				if (name.length() == 0) {
					// no replacement at begin.
				} else if (lastCharReplaced) {
					// no duplicate replacement.
				} else {
					name.append(replacement);
					lastCharReplaced = true;
				}
			} else {
				name.append(c);
				lastCharReplaced = false;
			}
		}
		if (lastCharReplaced) {
			name.setLength(name.length() - 1);
		}
		return name.toString();
	}

	private boolean isIllegalChar(char c) {
		return c == '/' || c == '\\' || c == '{' || c == '}';
	}

	private Object uniqueIdentifier(ParameterObject param) {
		return new TupleFactory.Pair<>(getParameterName(param), param.getIn());
	}

}
