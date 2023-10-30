/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.layout;

import static com.top_logic.service.openapi.common.schema.OpenAPISchemaConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationErrorProtocol;
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
import com.top_logic.service.openapi.common.OpenAPIConstants;
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
import com.top_logic.service.openapi.common.layout.MultiPartBodyTransferType;
import com.top_logic.service.openapi.common.schema.ArraySchema;
import com.top_logic.service.openapi.common.schema.ObjectSchema;
import com.top_logic.service.openapi.common.schema.ObjectSchemaProperty;
import com.top_logic.service.openapi.common.schema.PrimitiveSchema;
import com.top_logic.service.openapi.common.schema.Schema;
import com.top_logic.service.openapi.common.schema.SchemaVisitor;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.Information;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.conf.OperationResponse;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilder;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilderByExpression;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter.ParameterConfiguration;
import com.top_logic.service.openapi.server.parameter.CookieParameter;
import com.top_logic.service.openapi.server.parameter.HeaderParameter;
import com.top_logic.service.openapi.server.parameter.MultiPartBodyParameter;
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
			item.setParameterDefinition(createRequestParameter(paramObject, warnings, config));
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
		OpenAPIExporter.transferIfNotEmpty(info::getDescription, target::setDescription);
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
			createParameters(pathItem.getParameters(), newPath.getParameters(), pathParameters, warnings, config);
			addOperations(pathItem.getPath(), newPath, pathParameters, pathItem, globalSecurity, warnings, config);
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
			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParameters, PathItemObject pathItem,
			List<String> globalSecurity, List<ResKey> warnings, OpenapiDocument completeAPI) {
		addOperation(origPath, newPath, HttpMethod.DELETE, pathItem.getDelete(),
			pathParameters, globalSecurity, warnings, completeAPI);
		addOperation(origPath, newPath, HttpMethod.GET, pathItem.getGet(),
			pathParameters, globalSecurity, warnings, completeAPI);
		addOperation(origPath, newPath, HttpMethod.HEAD, pathItem.getHead(),
			pathParameters, globalSecurity, warnings, completeAPI);
		addOperation(origPath, newPath, HttpMethod.OPTIONS, pathItem.getOptions(),
			pathParameters, globalSecurity, warnings, completeAPI);
		addOperation(origPath, newPath, HttpMethod.PATCH, pathItem.getPatch(),
			pathParameters, globalSecurity, warnings, completeAPI);
		addOperation(origPath, newPath, HttpMethod.POST, pathItem.getPost(),
			pathParameters, globalSecurity, warnings, completeAPI);
		addOperation(origPath, newPath, HttpMethod.PUT, pathItem.getPut(),
			pathParameters, globalSecurity, warnings, completeAPI);
		addOperation(origPath, newPath, HttpMethod.TRACE, pathItem.getTrace(),
			pathParameters, globalSecurity, warnings, completeAPI);
	}

	private void addOperation(String origPath, PathItem newPath, HttpMethod method, OperationObject operation,
			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParameters, List<String> globalSecurity,
			List<ResKey> warnings, OpenapiDocument completeAPI) {
		if (operation == null) {
			return;
		}
		OperationByMethod newOperation =
			createOperation(origPath, method, operation, pathParameters, globalSecurity, warnings, completeAPI);
		newPath.getOperations().put(newOperation.getMethod(), newOperation);
	}

	private OperationByMethod createOperation(String origPath, HttpMethod method, OperationObject operation,
			List<RequestParameter.Config<? extends RequestParameter<?>>> pathParameters, List<String> globalSecurity,
			List<ResKey> warnings, OpenapiDocument completeAPI) {
		OperationByMethod newOperation = TypedConfiguration.newConfigItem(OperationByMethod.class);
		newOperation.setMethod(method);
		List<Map<String, List<String>>> sec = operation.getSecurity();
		if (!sec.isEmpty()) {
			newOperation.setAuthentication(new ArrayList<>(sec.get(0).keySet()));
		} else if (!globalSecurity.isEmpty()) {
			newOperation.setAuthentication(globalSecurity);
		}
		OpenAPIExporter.transferIfNotEmpty(operation::getDescription, newOperation::setDescription);
		OpenAPIExporter.transferIfNotEmpty(operation::getSummary, newOperation::setSummary);
		String[] tags = operation.getTags();
		if (tags.length > 0) {
			newOperation.setTags(Arrays.asList(tags));
		}
		RequestBodyObject requestBody = operation.getRequestBody();
		if (method.supportsBody() && requestBody != null) {
			processBody(newOperation, requestBody, warnings, origPath, completeAPI);
		}

		createParameters(operation.getParameters(), newOperation.getParameters(), pathParameters, warnings, completeAPI);
		
		for (ResponsesObject response : operation.getResponses().values()) {
			OperationResponse opResp = TypedConfiguration.newConfigItem(OperationResponse.class);
			opResp.setResponseCode(response.getStatusCode());
			OpenAPIExporter.transferIfNotEmpty(response::getDescription, opResp::setDescription);
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

	private void processBody(OperationByMethod newOperation, RequestBodyObject requestBody, List<ResKey> warnings,
			String origPath, OpenapiDocument completeAPI) {
		Collection<MediaTypeObject> possibleBodyTypes = requestBody.getContent().values();
		List<String> unsupportedMediaTypes = Collections.emptyList();
		ConcreteRequestParameter.Config<?> bodyParam = null;
		for (MediaTypeObject mediaObject : possibleBodyTypes) {
			String mediaType = mediaObject.getMediaType();
			switch (mediaType) {
				case OpenAPIConstants.MULTIPART_FORM_DATA_CONTENT_TYPE: {
					MultiPartBodyParameter.Config multiPartBody =
						newBodyParameter(MultiPartBodyParameter.class, requestBody, mediaObject);
					multiPartBody.setTransferType(MultiPartBodyTransferType.FORM_DATA);
					addBodyParts(multiPartBody, warnings, completeAPI);
					bodyParam = multiPartBody;
					break;
				}
				case OpenAPIConstants.APPLICATION_URL_ENCODED_CONTENT_TYPE: {
					MultiPartBodyParameter.Config multiPartBody =
						newBodyParameter(MultiPartBodyParameter.class, requestBody, mediaObject);
					multiPartBody.setTransferType(MultiPartBodyTransferType.URL_ENCODED);
					addBodyParts(multiPartBody, warnings, completeAPI);
					bodyParam = multiPartBody;
					break;
				}
				case JsonUtilities.JSON_CONTENT_TYPE: {
					bodyParam = newBodyParameter(RequestBodyParameter.class, requestBody, mediaObject);
					break;
				}
				default:
					if (unsupportedMediaTypes.isEmpty()) {
						unsupportedMediaTypes = new ArrayList<>();
					}
					unsupportedMediaTypes.add(mediaType);
					continue;
			}
		}
		if (bodyParam == null) {
			String method = origPath + ':' + newOperation.getMethod();
			if (unsupportedMediaTypes.isEmpty()) {
				warnings.add(I18NConstants.MISSING_BODY_TYPE__PATH.fill(method));
			} else {
				warnings.add(I18NConstants.UNSUPPORTED_BODY_TYPES__PATH_UNSUPPORTED_SUPPORTED
					.fill(method, unsupportedMediaTypes,
						Arrays.asList(OpenAPIConstants.MULTIPART_FORM_DATA_CONTENT_TYPE,
							JsonUtilities.JSON_CONTENT_TYPE)));
			}
			MediaTypeObject jsonMediaType = TypedConfiguration.newConfigItem(MediaTypeObject.class);
			jsonMediaType.setMediaType(JsonUtilities.JSON_CONTENT_TYPE);
			jsonMediaType.check(ConfigurationErrorProtocol.INSTANCE);
			bodyParam = newBodyParameter(RequestBodyParameter.class, requestBody, jsonMediaType);
		}

		newOperation.getParameters().add(bodyParam);
	}

	private void addBodyParts(MultiPartBodyParameter.Config multiPartBody, List<ResKey> warnings,
			OpenapiDocument completeAPI) {
		String schemaString = multiPartBody.getSchema();
		if (schemaString == null) {
			return;
		}
		Schema schema =
			parseSchema(schemaString, multiPartBody.getName(), completeAPI.getComponents().getSchemas(), warnings);
		if (schema instanceof ObjectSchema) {
			for (ObjectSchemaProperty property : ((ObjectSchema) schema).getProperties()) {
				Schema propertySchema = property.getSchema();
				if (propertySchema == null) {
					// Warning was logged before.
					continue;
				}
				MultiPartBodyParameter.BodyPart newPart =
					TypedConfiguration.newConfigItem(MultiPartBodyParameter.BodyPart.class);
				newPart.setName(property.getName());
				newPart.setRequired(property.isRequired());
				OpenAPIExporter.transferIfNotEmpty(propertySchema::getDescription, newPart::setDescription);
				ResKey problem = propertySchema.visit(applySchema(), newPart);
				if (problem != ResKey.NONE) {
					warnings.add(problem);
				}
				multiPartBody.getParts().put(newPart.getName(), newPart);
			}
		}
	}

	private <T extends ConcreteRequestParameter.Config<?>> T newBodyParameter(Class<? extends ConcreteRequestParameter<?>> implClass,
			RequestBodyObject requestBody, MediaTypeObject mediaType) {
		T bodyParam = newConfigForImplementation(implClass);
		bodyParam.setName("requestBody");
		bodyParam.setRequired(requestBody.isRequired());
		bodyParam.setFormat(ParameterFormat.OBJECT);
		OpenAPIExporter.transferIfNotEmpty(requestBody::getDescription, bodyParam::setDescription);
		OpenAPIExporter.transferIfNotEmpty(mediaType::getExample, bodyParam::setExample);
		OpenAPIExporter.transferIfNotEmpty(mediaType::getSchema, bodyParam::setSchema);
		return bodyParam;
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
			List<ResKey> warnings, OpenapiDocument completeAPI) {
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
				RequestParameter.Config<?> reqParam = createRequestParameter((ParameterObject) pathItemParam, warnings, completeAPI);
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
			List<ResKey> warnings, OpenapiDocument completeAPI) {
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
		OpenAPIExporter.transferIfNotEmpty(paramObject::getDescription, requestParam::setDescription);
		if (paramObject.getIn() != ParameterLocation.PATH) {
			// Path is always required and can not be set.
			requestParam.setRequired(paramObject.isRequired());
		}
		addSchema(paramObject.getSchema(), requestParam, warnings, completeAPI);
		return requestParam;
	}

	private Schema addSchema(String schema, ConcreteRequestParameter.Config<?> paramConf, List<ResKey> warnings,
			OpenapiDocument completeAPI) {
		if (schema == null) {
			return null;
		}
		Schema schemaObject =
			parseSchema(schema, paramConf.getName(), completeAPI.getComponents().getSchemas(), warnings);

		ResKey problem = schemaObject.visit(applySchema(), paramConf);
		if (problem != ResKey.NONE) {
			warnings.add(problem);
		}
		return schemaObject;
	}

	private SchemaVisitor<ResKey, ParameterConfiguration> applySchema() {
		return new SchemaVisitor<>() {

			@Override
			public ResKey visitPrimitiveSchema(PrimitiveSchema schema, ParameterConfiguration parameter) {
				String format = schema.getFormat();
				switch (schema.getType()) {
					case SCHEMA_TYPE_NUMBER:
						switch (format) {
							case SCHEMA_FORMAT_FLOAT:
								parameter.setFormat(ParameterFormat.FLOAT);
								break;
							case SCHEMA_FORMAT_DOUBLE:
							case "":
								parameter.setFormat(ParameterFormat.DOUBLE);
								break;
							default:
								parameter.setFormat(ParameterFormat.DOUBLE);
								return I18NConstants.UNSUPPORTED_PARAMETER_FORMAT__FORMAT__PARAMETER.fill(format,
									parameter.getName());
						}
						break;
					case SCHEMA_TYPE_INTEGER:
						switch (format) {
							case SCHEMA_FORMAT_INT32:
								parameter.setFormat(ParameterFormat.INTEGER);
								break;
							case SCHEMA_FORMAT_INT64:
							case "":
								parameter.setFormat(ParameterFormat.LONG);
								break;
							default:
								parameter.setFormat(ParameterFormat.LONG);
								return I18NConstants.UNSUPPORTED_PARAMETER_FORMAT__FORMAT__PARAMETER.fill(format,
									parameter.getName());
						}
						break;
					case SCHEMA_TYPE_STRING:
						switch (format) {
							case SCHEMA_FORMAT_BINARY:
								parameter.setFormat(ParameterFormat.BINARY);
								break;
							case SCHEMA_FORMAT_BYTE:
								parameter.setFormat(ParameterFormat.BYTE);
								break;
							case SCHEMA_FORMAT_DATE:
								parameter.setFormat(ParameterFormat.DATE);
								break;
							case SCHEMA_FORMAT_DATE_TIME:
								parameter.setFormat(ParameterFormat.DATE_TIME);
								break;
							case "":
								parameter.setFormat(ParameterFormat.STRING);
								break;
							default:
								parameter.setFormat(ParameterFormat.STRING);
								return I18NConstants.UNSUPPORTED_PARAMETER_FORMAT__FORMAT__PARAMETER.fill(format,
									parameter.getName());
						}
						break;
					case SCHEMA_TYPE_BOOLEAN:
						parameter.setFormat(ParameterFormat.BOOLEAN);
						break;
					case "":
						return I18NConstants.MISSING_PARAMETER_TYPE__PARAMETER.fill(parameter.getName());
					default:
						return I18NConstants.UNSUPPORTED_PARAMETER_TYPE__TYPE__PARAMETER.fill(schema.getType(),
							parameter.getName());
				}

				parameter.setSchema(schema.getAsString());
				addExampleValue(schema, parameter);
				return ResKey.NONE;
			}

			@Override
			public ResKey visitObjectSchema(ObjectSchema schema, ParameterConfiguration parameter) {
				parameter.setFormat(ParameterFormat.OBJECT);

				parameter.setSchema(schema.getAsString());
				addExampleValue(schema, parameter);
				return ResKey.NONE;
			}

			@Override
			public ResKey visitArraySchema(ArraySchema schema, ParameterConfiguration parameter) {
				parameter.setMultiple(true);

				parameter.setSchema(schema.getAsString());
				addExampleValue(schema, parameter);
				return schema.getItems().visit(this, parameter);
			}

			private void addExampleValue(Schema schema, ParameterConfiguration parameter) {
				parameter.setExample(schema.getExample());
			}
		};
	}

}

