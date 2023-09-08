/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.admin.component.TLServiceConfigEditorFormBuilder;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.ComponentsObject;
import com.top_logic.service.openapi.common.document.IParameterObject;
import com.top_logic.service.openapi.common.document.InfoObject;
import com.top_logic.service.openapi.common.document.MediaTypeObject;
import com.top_logic.service.openapi.common.document.OpenapiDocument;
import com.top_logic.service.openapi.common.document.OperationObject;
import com.top_logic.service.openapi.common.document.ParameterLocation;
import com.top_logic.service.openapi.common.document.ParameterObject;
import com.top_logic.service.openapi.common.document.PathItemObject;
import com.top_logic.service.openapi.common.document.ReferencableParameterObject;
import com.top_logic.service.openapi.common.document.ReferencingParameterObject;
import com.top_logic.service.openapi.common.document.RequestBodyObject;
import com.top_logic.service.openapi.common.document.ResponsesObject;
import com.top_logic.service.openapi.common.document.SchemaObject;
import com.top_logic.service.openapi.common.document.TagObject;
import com.top_logic.service.openapi.common.layout.ImportOpenAPIConfiguration;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.Information;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.conf.OperationResponse;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilder;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilderByExpression;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.CookieParameter;
import com.top_logic.service.openapi.server.parameter.HeaderParameter;
import com.top_logic.service.openapi.server.parameter.ParameterFormat;
import com.top_logic.service.openapi.server.parameter.PathParameter;
import com.top_logic.service.openapi.server.parameter.QueryParameter;
import com.top_logic.service.openapi.server.parameter.ReferencedParameter;
import com.top_logic.service.openapi.server.parameter.RequestBodyParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter;

/**
 * {@link ImportOpenAPIConfiguration} that creates an <i>OpenAPI</i> server from an imported
 * configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImportOpenAPIServer extends ImportOpenAPIConfiguration {

	/**
	 * Creates a new {@link ImportOpenAPIServer}.
	 */
	public ImportOpenAPIServer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void applyConfig(OpenapiDocument config, LayoutComponent component, Object model, List<ResKey> warnings) {
		EditComponent editor = (EditComponent) component;

		FormContext formContext = editor.getFormContext();
		TLServiceConfigEditorFormBuilder.EditModel m =
			(TLServiceConfigEditorFormBuilder.EditModel) EditorFactory.getModel(formContext);
		OpenApiServer.Config<?> serviceConfiguration =
			(OpenApiServer.Config<?>) m.getServiceConfiguration();

		Information information = serviceConfiguration.getInformation();
		if (information != null) {
			copyInfoObject(config, information);
		} else {
			Information infoObject = TypedConfiguration.newConfigItem(Information.class);
			Information copyInfoObject = copyInfoObject(config, infoObject);
			serviceConfiguration.setInformation(copyInfoObject);
		}
		addGlobalSchemas(config, serviceConfiguration);
		addGlobalParameters(config, serviceConfiguration, warnings);
		addTags(config, serviceConfiguration);
		addAuthentications(config, serviceConfiguration, warnings);
		addPaths(config, serviceConfiguration, warnings);
	}

	private void addTags(OpenapiDocument config, OpenApiServer.Config<?> serviceConfiguration) {
		for (TagObject tag : config.getTags()) {
			serviceConfiguration.getTags().add(TypedConfiguration.copy(tag));
		}
	}

	private void addGlobalParameters(OpenapiDocument config, OpenApiServer.Config<?> serviceConfiguration, List<ResKey> warnings) {
		ComponentsObject components = config.getComponents();
		if (components == null) {
			return;
		}
		for (ReferencableParameterObject paramObject : components.getParameters().values()) {
			ReferencedParameter item = TypedConfiguration.newConfigItem(ReferencedParameter.class);
			item.setReferenceName(paramObject.getReferenceName());
			item.setParameterDefinition(createRequestParameter(paramObject, warnings));
			serviceConfiguration.getGlobalParameters().put(item.getReferenceName(), item);
		}
	}

	private void addGlobalSchemas(OpenapiDocument config, OpenApiServer.Config<?> serviceConfiguration) {
		ComponentsObject components = config.getComponents();
		if (components == null) {
			return;
		}
		for (SchemaObject schema : components.getSchemas().values()) {
			SchemaObject schemaCopy = TypedConfiguration.copy(schema);
			serviceConfiguration.getGlobalSchemas().put(schemaCopy.getName(), schemaCopy);
		}
	}

	private Information copyInfoObject(OpenapiDocument openApiDoc, Information target) {
		InfoObject info = openApiDoc.getInfo();
		target.setTitle(info.getTitle());
		target.setVersion(info.getVersion());
		target.setDescription(info.getDescription());
		target.setTermsOfService(info.getTermsOfService());
		target.setContact(TypedConfiguration.copy(info.getContact()));
		target.setLicense(TypedConfiguration.copy(info.getLicense()));
		return target;
	}

	private void addPaths(OpenapiDocument config, OpenApiServer.Config<?> serviceConfiguration, List<ResKey> warnings) {
		Collection<PathItemObject> paths = config.getPaths().values();
		List<String> globalSecurity = globalSecurity(config);
		for (PathItemObject pathItem : paths) {
			PathItem newPath = TypedConfiguration.newConfigItem(PathItem.class);

			newPath.setPath(pathItem.getPath());

			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParameters = new ArrayList<>();
			createParameters(pathItem.getParameters(), newPath.getParameters(), pathParameters, warnings);
			addOperations(pathItem.getPath(), newPath, pathParameters, pathItem, globalSecurity, warnings);
			newPath.getParameters().addAll(0, pathParameters);
			serviceConfiguration.getPaths().add(newPath);
		}

	}

	private List<String> globalSecurity(OpenapiDocument config) {
		List<String> globalSecurity;
		List<Map<String, List<String>>> security = config.getSecurity();
		if (!security.isEmpty()) {
			globalSecurity = new ArrayList<>(security.get(0).keySet());
		} else {
			globalSecurity = Collections.emptyList();
		}
		return globalSecurity;
	}

	private void addOperations(String origPath, PathItem newPath,
			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParameters,
			PathItemObject pathItem, List<String> globalSecurity, List<ResKey> warnings) {
		addOperation(origPath, newPath, HttpMethod.DELETE, pathItem.getDelete(),
			pathParameters, globalSecurity, warnings);
		addOperation(origPath, newPath, HttpMethod.GET, pathItem.getGet(),
			pathParameters, globalSecurity, warnings);
		addOperation(origPath, newPath, HttpMethod.HEAD, pathItem.getHead(),
			pathParameters, globalSecurity, warnings);
		addOperation(origPath, newPath, HttpMethod.OPTIONS, pathItem.getOptions(),
			pathParameters, globalSecurity, warnings);
		addOperation(origPath, newPath, HttpMethod.PATCH, pathItem.getPatch(),
			pathParameters, globalSecurity, warnings);
		addOperation(origPath, newPath, HttpMethod.POST, pathItem.getPost(),
			pathParameters, globalSecurity, warnings);
		addOperation(origPath, newPath, HttpMethod.PUT, pathItem.getPut(),
			pathParameters, globalSecurity, warnings);
		addOperation(origPath, newPath, HttpMethod.TRACE, pathItem.getTrace(),
			pathParameters, globalSecurity, warnings);
	}

	private void addOperation(String origPath, PathItem newPath, HttpMethod method, OperationObject operation,
			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParameters, List<String> globalSecurity,
			List<ResKey> warnings) {
		if (operation == null) {
			return;
		}
		OperationByMethod newOperation =
			createOperation(origPath, method, operation, pathParameters, globalSecurity, warnings);
		newPath.getOperations().put(newOperation.getMethod(), newOperation);
	}

	private OperationByMethod createOperation(String origPath, HttpMethod method, OperationObject operation,
			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParameters, List<String> globalSecurity,
			List<ResKey> warnings) {
		OperationByMethod newOperation = TypedConfiguration.newConfigItem(OperationByMethod.class);
		newOperation.setMethod(method);
		List<Map<String, List<String>>> sec = operation.getSecurity();
		if (!sec.isEmpty()) {
			newOperation.setAuthentication(new ArrayList<>(sec.get(0).keySet()));
		} else if (!globalSecurity.isEmpty()) {
			newOperation.setAuthentication(globalSecurity);
		}
		newOperation.setDescription(operation.getDescription());
		newOperation.setSummary(operation.getSummary());
		String[] tags = operation.getTags();
		if (tags.length > 0) {
			newOperation.setTags(Arrays.asList(tags));
		}
		RequestBodyObject requestBody = operation.getRequestBody();
		if (method.supportsBody() && requestBody != null) {
			RequestBodyParameter.Config bodyParam = newConfigForImplementation(RequestBodyParameter.class);
			bodyParam.setName("requestBody");
			bodyParam.setDescription(requestBody.getDescription());
			bodyParam.setRequired(requestBody.isRequired());
			MediaTypeObject jsonResponse = requestBody.getContent().get(JsonUtilities.JSON_CONTENT_TYPE);
			if (jsonResponse != null) {
				bodyParam.setFormat(ParameterFormat.OBJECT);
				bodyParam.setSchema(jsonResponse.getSchema());
				bodyParam.setExample(jsonResponse.getExample());
			}
			newOperation.getParameters().add(bodyParam);
		}

		createParameters(operation.getParameters(), newOperation.getParameters(), pathParameters, warnings);
		
		for (ResponsesObject response : operation.getResponses().values()) {
			OperationResponse opResp = TypedConfiguration.newConfigItem(OperationResponse.class);
			opResp.setResponseCode(response.getStatusCode());
			opResp.setDescription(response.getDescription());
			MediaTypeObject jsonResponse = response.getContent().get(JsonUtilities.JSON_CONTENT_TYPE);
			if (jsonResponse != null) {
				opResp.setFormat(ParameterFormat.OBJECT);
				opResp.setSchema(jsonResponse.getSchema());
				opResp.setExample(jsonResponse.getExample());
			}
			newOperation.getResponses().put(opResp.getResponseCode(), opResp);
		}
		PolymorphicConfiguration<? extends ServiceMethodBuilder> implementation;
		try {
			implementation = getImplementation(operation);
		} catch (ConfigurationException ex) {
			warnings.add(I18NConstants.INVALID_IMPLEMENTATION_FOR_OPERATION___PATH__METHOD__MSG.fill(origPath,
				method.name(), ex.getErrorKey()));
			implementation = null;
		}
		if (implementation == null) {
			ServiceMethodBuilderByExpression.Config dummyImpl =
				newConfigForImplementation(ServiceMethodBuilderByExpression.class);

			try {
				dummyImpl.setOperation(ExprFormat.INSTANCE.getValue(ServiceMethodBuilderByExpression.Config.OPERATION,
					"throw(\"Operation '" + method + "' on '" + origPath + "' not yet implemented\")"));
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
			implementation = dummyImpl;
		}
		newOperation.setImplementation(implementation);
		return newOperation;
	}

	@SuppressWarnings("unchecked")
	private PolymorphicConfiguration<? extends ServiceMethodBuilder> getImplementation(OperationObject operation)
			throws ConfigurationException {
		PolymorphicConfiguration<?> configuration = operation.getImpl();
		if (configuration == null) {
			return null;
		}
		if (ServiceMethodBuilder.class.isAssignableFrom(configuration.getImplementationClass())) {
			return (PolymorphicConfiguration<ServiceMethodBuilder>) configuration;
		}
		throw new ConfigurationException(I18NConstants.INVALID_IMPLEMENTATION_TYPE, OperationObject.X_TL_IMPLEMENTATION,
			operation.getImplementation());
	}

	private void createParameters(List<IParameterObject> paramObjects,
			List<RequestParameter.Config<? extends RequestParameter<?>>> requestParams,
			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParams,
			List<ResKey> warnings) {
		Set<String> knownPathParameterNames = pathParams.stream()
				.map(RequestParameter.Config::getName)
				.collect(Collectors.toSet());
		for (IParameterObject pathItemParam : paramObjects) {
			if (pathItemParam instanceof ReferencingParameterObject) {
				String referencedName = referencedParameterName((ReferencingParameterObject) pathItemParam, warnings);
				if (referencedName == null) {
					continue;
				}
				com.top_logic.service.openapi.server.parameter.ParameterReference.Config newItem =
						TypedConfiguration
						.newConfigItem(
							com.top_logic.service.openapi.server.parameter.ParameterReference.Config.class);
				newItem.setName(referencedName);
				requestParams.add(newItem);
			} else {
				RequestParameter.Config<?> reqParam = createRequestParameter((ParameterObject) pathItemParam, warnings);
				if (pathItemParam instanceof ParameterObject
						&& ((ParameterObject) pathItemParam).getIn() == ParameterLocation.PATH) {
					if (!knownPathParameterNames.contains(reqParam.getName())) {
						pathParams.add(reqParam);
					}
				} else {
					requestParams.add(reqParam);
				}
			}
		}
	}

	private ConcreteRequestParameter.Config<?> createRequestParameter(ParameterObject paramObject,
			List<ResKey> warnings) {
		ConcreteRequestParameter.Config<?> requestParam;
		switch (paramObject.getIn()) {
			case COOKIE:
				requestParam = newConfigForImplementation(CookieParameter.class);
				break;
			case HEADER:
				requestParam = newConfigForImplementation(HeaderParameter.class);
				break;
			case PATH:
				requestParam = newConfigForImplementation(PathParameter.class);
				break;
			case QUERY:
				requestParam = newConfigForImplementation(QueryParameter.class);
				break;
			default:
				throw new UnreachableAssertion("No such parameter location: " + paramObject.getIn());
		}
		requestParam.setName(paramObject.getName());
		requestParam.setDescription(paramObject.getDescription());
		if (paramObject.getIn() != ParameterLocation.PATH) {
			// Path is always required and can not be set.
			requestParam.setRequired(paramObject.isRequired());
		}
		setParameterFormat(paramObject, requestParam, warnings);
		if (requestParam.getFormat() == ParameterFormat.OBJECT) {
			requestParam.setSchema(paramObject.getSchema());
			requestParam.setExample(paramObject.getExample());
		}
		return requestParam;
	}

	private void setParameterFormat(ParameterObject pathItemParam, ConcreteRequestParameter.Config<?> paramConf,
			List<ResKey> warnings) {
		String schema = pathItemParam.getSchema();
		if (schema == null) {
			return;
		}
		Object schemaObject = JsonUtilities.parse(schema);
		if (schemaObject instanceof Map) {
			Map<?, ?> schemaMap = (Map<?, ?>) schemaObject;
			String referencedSchema = globallyReferencedSchema(schemaMap);
			if (referencedSchema != null) {
				paramConf.setFormat(ParameterFormat.OBJECT);
				paramConf.setSchema(schema);
				return;
			}
			String type = StringServices.nonNull((String) schemaMap.get("type"));
			String format = StringServices.EMPTY_STRING;
			if ("array".equals(type)) {
				paramConf.setMultiple(true);
				Object items = schemaMap.get("items");
				if (items instanceof Map) {
					Map<?, ?> itemsMap = (Map<?, ?>) items;
					referencedSchema = globallyReferencedSchema(itemsMap);
					if (referencedSchema != null) {
						paramConf.setFormat(ParameterFormat.OBJECT);
						paramConf.setSchema(schema);
						return;
					}
					type = StringServices.nonNull((String) itemsMap.get("type"));
					format = StringServices.nonNull((String) itemsMap.get("format"));
				}
			} else {
				format = StringServices.nonNull((String) schemaMap.get("format"));
			}
			switch (type) {
				case "number":
					switch (format) {
						case "float":
							paramConf.setFormat(ParameterFormat.FLOAT);
							break;
						case "double":
						case "":
							paramConf.setFormat(ParameterFormat.DOUBLE);
							break;
						default:
							paramConf.setFormat(ParameterFormat.DOUBLE);
							warnings.add(I18NConstants.UNSUPPORTED_PARAMETER_FORMAT__FORMAT__PARAMETER.fill(format,
								pathItemParam.getName()));
							break;
					}
					break;
				case "integer":
					switch (format) {
						case "int32":
							paramConf.setFormat(ParameterFormat.INTEGER);
							break;
						case "int64":
						case "":
							paramConf.setFormat(ParameterFormat.LONG);
							break;
						default:
							paramConf.setFormat(ParameterFormat.LONG);
							warnings.add(I18NConstants.UNSUPPORTED_PARAMETER_FORMAT__FORMAT__PARAMETER.fill(format,
								pathItemParam.getName()));
							break;
					}
					break;
				case "string":
					switch (format) {
						case "date":
							paramConf.setFormat(ParameterFormat.DATE);
							break;
						case "date-time":
							paramConf.setFormat(ParameterFormat.DATE_TIME);
							break;
						case "":
							paramConf.setFormat(ParameterFormat.STRING);
							break;
						default:
							paramConf.setFormat(ParameterFormat.STRING);
							warnings.add(I18NConstants.UNSUPPORTED_PARAMETER_FORMAT__FORMAT__PARAMETER.fill(format,
								pathItemParam.getName()));
					}
					break;
				case "boolean":
					paramConf.setFormat(ParameterFormat.BOOLEAN);
					break;
				case "object":
					paramConf.setFormat(ParameterFormat.OBJECT);
					paramConf.setSchema(schema);
					break;
				case "":
					warnings.add(I18NConstants.MISSING_PARAMETER_TYPE__PARAMETER.fill(pathItemParam.getName()));
					break;
				default:
					warnings.add(I18NConstants.UNSUPPORTED_PARAMETER_TYPE__TYPE__PARAMETER.fill(type,
						pathItemParam.getName()));
					break;
			}
		}
	}

	private String globallyReferencedSchema(Map<?, ?> schemaMap) {
		Object referencedSchema = schemaMap.get("$ref");
		if (referencedSchema instanceof String && ((String) referencedSchema).startsWith("#/components/schemas/")) {
			return (String) referencedSchema;
		}
		return null;
	}

}

