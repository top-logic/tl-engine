/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.layout;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.mutable.MutableObject;
import org.yaml.snakeyaml.DumperOptions.Version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.basic.shared.io.StringW;
import com.top_logic.basic.shared.io.W;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfigVisitor;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.common.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.common.authentication.oauth.ClientCredentials;
import com.top_logic.service.openapi.common.authentication.oauth.DefaultURIProvider;
import com.top_logic.service.openapi.common.authentication.oauth.OpenIDURIProvider;
import com.top_logic.service.openapi.common.authentication.oauth.TokenURIProvider;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.ComponentsObject;
import com.top_logic.service.openapi.common.document.IParameterObject;
import com.top_logic.service.openapi.common.document.InfoObject;
import com.top_logic.service.openapi.common.document.MediaTypeObject;
import com.top_logic.service.openapi.common.document.OAuthFlow;
import com.top_logic.service.openapi.common.document.OAuthFlowObject;
import com.top_logic.service.openapi.common.document.OpenapiDocument;
import com.top_logic.service.openapi.common.document.OperationObject;
import com.top_logic.service.openapi.common.document.ParameterObject;
import com.top_logic.service.openapi.common.document.PathItemObject;
import com.top_logic.service.openapi.common.document.ReferencableParameterObject;
import com.top_logic.service.openapi.common.document.ReferencingParameterObject;
import com.top_logic.service.openapi.common.document.RequestBodyObject;
import com.top_logic.service.openapi.common.document.ResponsesObject;
import com.top_logic.service.openapi.common.document.SchemaObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeType;
import com.top_logic.service.openapi.common.document.ServerObject;
import com.top_logic.service.openapi.common.document.TagObject;
import com.top_logic.service.openapi.server.DisplayFullPathTemplate;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.Config;
import com.top_logic.service.openapi.server.OpenApiServer.Information;
import com.top_logic.service.openapi.server.conf.OperationByMethod;
import com.top_logic.service.openapi.server.conf.OperationResponse;
import com.top_logic.service.openapi.server.conf.PathItem;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.ParameterFormat;
import com.top_logic.service.openapi.server.parameter.ReferencedParameter;
import com.top_logic.service.openapi.server.parameter.RequestBodyParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter;

/**
 * Exporter that transforms a {@link Config} to <i>OpenAPI</i>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OpenAPIExporter {

	/**
	 * Export format of an {@link OpenAPIExporter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public enum ExportFormat implements ExternallyNamed {

		/**
		 * The <i>OpenAPI</i> document must be exported in "json" format.
		 */
		JSON() {
			@Override
			public String getExternalName() {
				return "json";
			}
		},
		/**
		 * The <i>OpenAPI</i> document must be exported in "yaml" format.
		 */
		YAML() {
			@Override
			public String getExternalName() {
				return "yaml";
			}
		},
		;

	}

	private final Config<?> _serverConfig;

	private OpenapiDocument _document;

	private ExportFormat _exportFormat = ExportFormat.JSON;

	/**
	 * Creates a new {@link OpenAPIExporter} for the given configuration.
	 */
	public OpenAPIExporter(OpenApiServer.Config<?> serverConfiguration) {
		_serverConfig = Objects.requireNonNull(serverConfiguration);
	}

	/**
	 * Determines the {@link ExportFormat} of this exporter.
	 */
	public ExportFormat getExportFormat() {
		return _exportFormat;
	}

	/**
	 * Setter for {@link #getExportFormat()}.
	 */
	public void setExportFormat(ExportFormat exportFormat) {
		_exportFormat = Objects.requireNonNull(exportFormat);
	}

	/**
	 * Creates the {@link OpenapiDocument} corresponding to the the given {@link OpenApiServer}
	 * configuration.
	 * 
	 * @param context
	 *        Current rendering context to determine correct server URL.
	 */
	public OpenapiDocument createDocument(DisplayContext context) {
		OpenapiDocument doc = newItem(OpenapiDocument.class);
		doc.setOpenapi(OpenapiDocument.VERSION_3_0_3);

		ServerObject server = newItem(ServerObject.class);
		server.setUrl(DisplayFullPathTemplate.createFullBaseURL(context, _serverConfig.getBaseURL()));
		doc.getServers().add(server);

		doc.setInfo(copyInfoObject(_serverConfig));
		setTags(doc, _serverConfig);
		Map<String, AuthenticationConfig> authentications = _serverConfig.getAuthentications();
		Map<String, SchemaObject> schemas = _serverConfig.getGlobalSchemas();
		Map<String, ReferencedParameter> parameters = _serverConfig.getGlobalParameters();
		if (!authentications.isEmpty() || !schemas.isEmpty() || !parameters.isEmpty()) {
			ComponentsObject componentsObject = newItem(ComponentsObject.class);
			for (Entry<String, AuthenticationConfig> authentication : authentications.entrySet()) {
				addAuthentication(componentsObject, authentication);
			}
			for (Entry<String, SchemaObject> schema : schemas.entrySet()) {
				addSchema(componentsObject, schema);
			}
			addParameters(parameters, componentsObject);
			doc.setComponents(componentsObject);
		}
		for (PathItem path : _serverConfig.getPaths()) {
			addPathItem(doc, path);
		}
		try {
			new ConstraintChecker().check(doc);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		_document = doc;
		return doc;
	}

	private void setTags(OpenapiDocument doc, Config<?> serverConfig) {
		for (TagObject tag : serverConfig.getTags()) {
			doc.getTags().add(copy(tag));
		}
	}

	private void addParameters(Map<String, ReferencedParameter> parameters, ComponentsObject componentsObject) {
		for (Entry<String, ReferencedParameter> entry : parameters.entrySet()) {
			ReferencableParameterObject target = TypedConfiguration.newConfigItem(ReferencableParameterObject.class);
			ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>> source =
				entry.getValue().getParameterDefinition();
			exportData(source, target);
			target.setReferenceName(entry.getKey());
			componentsObject.getParameters().put(target.getReferenceName(), target);
		}
	}

	/**
	 * Determines the default name for the <i>OpenAPI</i> specification file.
	 */
	public String getExportFileName() {
		Information info = _serverConfig.getInformation();
		StringBuilder rawFileName = new StringBuilder();
		rawFileName.append(info.getTitle());
		rawFileName.append("_");
		rawFileName.append(info.getVersion());
		rawFileName.append(".");
		switch (getExportFormat()) {
			case JSON:
				rawFileName.append("json");
				break;
			case YAML:
				rawFileName.append("yaml");
				break;
		}
		return FileUtilities.getCompatibleFilenamePart(rawFileName.toString());
	}

	/**
	 * Delivers the content of the document to the given {@link OutputStream}.
	 * 
	 * <p>
	 * The content is written in {@link JsonUtilities#DEFAULT_JSON_ENCODING}.
	 * </p>
	 */
	public void deliverTo(OutputStream out) throws IOException {
		if (_document == null) {
			throw new IllegalStateException();
		}
		switch (getExportFormat()) {
			case JSON:
				writeJSON(out);
				break;

			case YAML:
				ByteArrayStream tmp = new ByteArrayStream();
				writeJSON(tmp);

				Object readValue =
					new ObjectMapper().readValue(tmp.toByteArray(), Object.class);

				YAMLFactory yf = YAMLFactory.builder().yamlVersionToWrite(Version.V1_1).build();
				yf.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
				new ObjectMapper(yf).writeValue(out, readValue);
				break;
		}

	}

	private void writeJSON(OutputStream out) throws IOException, UnsupportedEncodingException {
		try (Writer w = new OutputStreamWriter(out, JsonUtilities.DEFAULT_JSON_ENCODING)) {
			new JsonConfigurationWriter(w)
				.prettyPrint()
				.write(_document);
		}
	}

	private InfoObject copyInfoObject(OpenApiServer.Config<?> serverConfig) {
		InfoObject infoObject = newItem(InfoObject.class);
		InfoObject serverInfo = serverConfig.getInformation();
		transferIfNotEmpty(serverInfo::getTitle, infoObject::setTitle);
		transferIfNotEmpty(serverInfo::getVersion, infoObject::setVersion);
		transferIfNotEmpty(serverInfo::getDescription, infoObject::setDescription);
		transferIfNotEmpty(serverInfo::getTermsOfService, infoObject::setTermsOfService);
		transferCopyIfNotNull(serverInfo::getContact, infoObject::setContact);
		transferCopyIfNotNull(serverInfo::getLicense, infoObject::setLicense);
		return infoObject;
	}

	private void addSchema(ComponentsObject componentsObject, Entry<String, SchemaObject> schema) {
		String schemaName = schema.getKey();
		componentsObject.getSchemas().put(schemaName, copy(schema.getValue()));
	}

	private void addAuthentication(ComponentsObject componentsObject,
			Entry<String, AuthenticationConfig> authentication) {
		String authName = authentication.getKey();
		componentsObject.getSecuritySchemes().put(authName,
			authentication.getValue().visit(SecuritySchemeObjects.INSTANCE, authName));
	}

	private void addPathItem(OpenapiDocument doc, PathItem path) {
		PathItemObject pathItem = newItem(PathItemObject.class);
		MutableObject<RequestBodyObject> requestBody = new MutableObject<>();
		List<IParameterObject> parameters = createParameters(path.getParameters(), requestBody);
		pathItem.getParameters().addAll(parameters);
		pathItem.setPath(path.getCompletePath());

		for (OperationByMethod operation : path.getOperations().values()) {
			OperationObject operationObj = createOperationObject(operation, requestBody);
			HttpMethod method = operation.getMethod();
			switch (method) {
				case DELETE:
					pathItem.setDelete(operationObj);
					break;
				case GET:
					pathItem.setGet(operationObj);
					break;
				case HEAD:
					pathItem.setHead(operationObj);
					break;
				case OPTIONS:
					pathItem.setOptions(operationObj);
					break;
				case PATCH:
					pathItem.setPatch(operationObj);
					break;
				case POST:
					pathItem.setPost(operationObj);
					break;
				case PUT:
					pathItem.setPut(operationObj);
					break;
				case TRACE:
					pathItem.setTrace(operationObj);
					break;
				default:
					throw new UnreachableAssertion("No such HTTP-Method: " + method);
			}
		}

		doc.getPaths().put(pathItem.getPath(), pathItem);
	}

	private OperationObject createOperationObject(OperationByMethod value,
			MutableObject<RequestBodyObject> requestBody) {
		OperationObject result = newItem(OperationObject.class);
		transferIfNotEmpty(value::getSummary, result::setSummary);
		transferIfNotEmpty(value::getDescription, result::setDescription);
		result.getParameters().addAll(createParameters(value.getParameters(), requestBody));
		if (requestBody.getValue() != null) {
			result.setRequestBody(requestBody.getValue());
		}
		List<String> tags = value.getTags();
		if (!tags.isEmpty()) {
			result.setTags(tags.toArray(ArrayUtil.EMPTY_STRING_ARRAY));
		}

		List<String> authentication = value.getAuthentication();
		if (!authentication.isEmpty()) {
			Map<String, List<String>> securityConfig = authentication.stream()
				.collect(Collectors.toMap(Function.identity(), (s) -> Collections.emptyList()));
			result.setSecurity(Collections.singletonList(securityConfig));
		}
		Map<String, OperationResponse> responses = value.getResponses();
		if (responses.isEmpty()) {
			ResponsesObject okResponse = okVoidResponse();
			result.getResponses().put(okResponse.getStatusCode(), okResponse);
		} else {
			for (OperationResponse resonse : responses.values()) {
				ResponsesObject response = newResponsesObject(resonse.getResponseCode(), resonse.getDescription());
				MediaTypeObject content = newMediaTypeObject(resonse.getFormat(), resonse.getSchema());
				transferIfNotEmpty(resonse::getExample, content::setExample);
				response.getContent().put(content.getMediaType(), content);
				result.getResponses().put(response.getStatusCode(), response);
			}
		}
		PolymorphicConfiguration<?> implementation = value.getImplementation();
		if (implementation != null) {
			switch (getExportFormat()) {
				case JSON:
					result.setImpl(implementation, false);
					break;
				case YAML:
					result.setImpl(implementation, true);
					break;
			}
		}
		return result;
	}

	private ResponsesObject okVoidResponse() {
		return newResponsesObject(Integer.toString(HttpServletResponse.SC_OK), "Ok");
	}

	private ResponsesObject newResponsesObject(String statusCode, String description) {
		ResponsesObject resonses = newItem(ResponsesObject.class);
		resonses.setStatusCode(statusCode);
		resonses.setDescription(description);
		return resonses;
	}

	private List<IParameterObject> createParameters(
			Collection<? extends RequestParameter.Config<? extends RequestParameter<?>>> parameters,
			MutableObject<RequestBodyObject> requestBody) {
		List<IParameterObject> params = new ArrayList<>();
		for (RequestParameter.Config<? extends RequestParameter<?>> parameter : parameters) {
			if (parameter instanceof RequestBodyParameter.Config) {
				if (requestBody.getValue() != null) {

					// multiple body parameters
					continue;
				}
				RequestBodyParameter.Config requestBodyParam = (RequestBodyParameter.Config) parameter;
				RequestBodyObject bodyObject = newItem(RequestBodyObject.class);
				transferIfNotEmpty(requestBodyParam::getDescription, bodyObject::setDescription);
				transferIfNotEmpty(requestBodyParam::getRequired, bodyObject::setRequired);
				MediaTypeObject content =
					newMediaTypeObject(requestBodyParam.getFormat(), requestBodyParam.getSchema());
				transferIfNotEmpty(requestBodyParam::getExample, content::setExample);
				bodyObject.getContent().put(content.getMediaType(), content);
				requestBody.setValue(bodyObject);
			} else if (parameter instanceof ConcreteRequestParameter.Config) {
				params.add(exportData((ConcreteRequestParameter.Config<?>) parameter, newItem(ParameterObject.class)));
			} else {
				ReferencingParameterObject reference = newItem(ReferencingParameterObject.class);
				reference.setReference(getReferenceName(parameter));
				params.add(reference);
			}
		}
		return params;
	}

	private <T extends ParameterObject> T exportData(ConcreteRequestParameter.Config<?> source, T target) {
		ParameterFormat format = source.getFormat();
		if (format != null) {
			target.setSchema(newSchema(format, source.isMultiple(), source.getSchema()));
			if (format == ParameterFormat.OBJECT) {
				transferIfNotEmpty(source::getExample, target::setExample);
			}
		}
		transferIfNotEmpty(source::getName, target::setName);
		transferIfNotEmpty(source::getDescription, target::setDescription);
		transferIfNotEmpty(source::getRequired, target::setRequired);
		transferIfNotEmpty(source::getParameterLocation, target::setIn);
		return target;
	}

	private String getReferenceName(RequestParameter.Config<? extends RequestParameter<?>> parameter) {
		return "#/components/parameters/" + parameter.getName();
	}

	private MediaTypeObject newMediaTypeObject(ParameterFormat format, String schema) {
		MediaTypeObject result = newItem(MediaTypeObject.class);
		result.setSchema(newSchema(format, false, schema));
		switch (format) {
			case OBJECT:
				result.setMediaType(JsonUtilities.JSON_CONTENT_TYPE);
				break;
			default:
				result.setMediaType("text/plain; charset=utf-8");

		}
		return result;
	}

	private String newSchema(ParameterFormat format, boolean multiple, String schema) {
		switch (format) {
			case INTEGER:
				return primitiveSchema("integer", "int32", multiple);
			case LONG:
				return primitiveSchema("integer", "int64", multiple);
			case OBJECT:
				if (StringServices.isEmpty(schema)) {
					return primitiveSchema("object", null, multiple);
				} else {
					return schema;
				}
			case STRING:
				return primitiveSchema("string", null, multiple);
			case DATE:
				return primitiveSchema("string", "date", multiple);
			case DATE_TIME:
				return primitiveSchema("string", "date-time", multiple);
			case BOOLEAN:
				return primitiveSchema("boolean", null, multiple);
			case DOUBLE:
				return primitiveSchema("number", "double", multiple);
			case FLOAT:
				return primitiveSchema("number", "float", multiple);
			default:
				throw new UnreachableAssertion("No such format: " + format);
		}
	}

	private String primitiveSchema(String type, String format, boolean multiple) {
		W out = new StringW();
		try (JsonWriter json = new JsonWriter(out)) {
			if (!multiple) {
				singlePrimitiveSchema(json, type, format);
			} else {
				multiplePrimitiveSchema(json, type, format);
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return out.toString();
	}

	private void multiplePrimitiveSchema(JsonWriter json, String type, String format) throws IOException {
		json.beginObject();
		json.name("type");
		json.value("array");
		json.name("items");
		singlePrimitiveSchema(json, type, format);
		json.endObject();
	}

	private void singlePrimitiveSchema(JsonWriter json, String type, String format) throws IOException {
		json.beginObject();
		{
			json.name("type");
			json.value(type);
			if (format != null) {
				json.name("format");
				json.value(format);
			}
		}
		json.endObject();
	}

	static <T extends ConfigurationItem> T newItem(Class<T> configInterface) {
		return TypedConfiguration.newConfigItem(configInterface);
	}

	static <T> void transferIfNotEmpty(Supplier<T> supplier, Consumer<? super T> consumer) {
		T t = supplier.get();
		if (!StringServices.isEmpty(t)) {
			consumer.accept(t);
		}
	}

	static <T extends ConfigurationItem> void transferCopyIfNotNull(Supplier<T> supplier,
			Consumer<? super T> consumer) {
		T t = supplier.get();
		if (t != null) {
			consumer.accept(copy(t));
		}
	}

	private static <T extends ConfigurationItem> T copy(T item) {
		return TypedConfiguration.copy(item);
	}

	private static class SecuritySchemeObjects implements AuthenticationConfigVisitor<SecuritySchemeObject, String> {

		/** Singleton {@link SecuritySchemeObjects} instance. */
		public static final SecuritySchemeObjects INSTANCE = new SecuritySchemeObjects();

		/**
		 * Creates a new {@link SecuritySchemeObjects}.
		 */
		protected SecuritySchemeObjects() {
			// singleton instance
		}

		@Override
		public SecuritySchemeObject visitAPIKeyAuthentication(APIKeyAuthentication config, String schemaName) {
			SecuritySchemeObject securityScheme = newSecuritySchema(schemaName);
			securityScheme.setType(SecuritySchemeType.API_KEY);
			transferIfNotEmpty(config::getParameterName, securityScheme::setName);
			transferIfNotEmpty(config::getPosition, securityScheme::setIn);
			return securityScheme;
		}

		@Override
		public SecuritySchemeObject visitClientCredentials(ClientCredentials config, String schemaName) {
			SecuritySchemeObject securityScheme = newSecuritySchema(schemaName);
			PolymorphicConfiguration<? extends TokenURIProvider> uriProvider = config.getURIProvider();
			if (uriProvider instanceof OpenIDURIProvider.Config) {
				securityScheme.setType(SecuritySchemeType.OPEN_ID_CONNECT);
				securityScheme.setOpenIdConnectUrl(((OpenIDURIProvider.Config) uriProvider).getOpenIDIssuer());
			} else if (uriProvider instanceof DefaultURIProvider.Config) {
				securityScheme.setType(SecuritySchemeType.OAUTH2);
				OAuthFlowObject flowObject = newItem(OAuthFlowObject.class);
				flowObject.setFlow(OAuthFlow.CLIENT_CREDENTIALS);
				flowObject.setTokenUrl(((DefaultURIProvider.Config) uriProvider).getTokenURL().toExternalForm());
				flowObject.setScopes(Collections.emptyMap());
				securityScheme.getFlows().put(flowObject.getFlow(), flowObject);
			} else {
				throw new UnsupportedOperationException();
			}
			return securityScheme;
		}

		@Override
		public SecuritySchemeObject visitBasicAuthentication(BasicAuthentication config, String schemaName) {
			SecuritySchemeObject securityScheme = newHTTPAuthentication(schemaName);
			securityScheme.setScheme("Basic");
			return securityScheme;
		}

		private SecuritySchemeObject newHTTPAuthentication(String schemaName) {
			SecuritySchemeObject securityScheme = newSecuritySchema(schemaName);
			securityScheme.setType(SecuritySchemeType.HTTP);
			return securityScheme;
		}

		private SecuritySchemeObject newSecuritySchema(String schemaName) {
			SecuritySchemeObject securityScheme = newItem(SecuritySchemeObject.class);
			securityScheme.setSchemaName(schemaName);
			return securityScheme;
		}

	}

}

