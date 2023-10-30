/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.layout;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.i18n.log.BufferingI18NLog.Entry;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.BinaryDataEditor;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfigs;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.common.authentication.http.basic.BasicAuthentication;
import com.top_logic.service.openapi.common.authentication.oauth.ClientCredentials;
import com.top_logic.service.openapi.common.authentication.oauth.DefaultURIProvider;
import com.top_logic.service.openapi.common.authentication.oauth.OpenIDURIProvider;
import com.top_logic.service.openapi.common.document.ComponentsObject;
import com.top_logic.service.openapi.common.document.IParameterObject;
import com.top_logic.service.openapi.common.document.OAuthFlow;
import com.top_logic.service.openapi.common.document.OAuthFlowObject;
import com.top_logic.service.openapi.common.document.OpenapiDocument;
import com.top_logic.service.openapi.common.document.ParameterObject;
import com.top_logic.service.openapi.common.document.ReferencableParameterObject;
import com.top_logic.service.openapi.common.document.ReferencingParameterObject;
import com.top_logic.service.openapi.common.document.SchemaObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.schema.OpenAPISchemaUtils;
import com.top_logic.service.openapi.common.schema.Schema;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} importing an <i>OpenAPI</i> specification document.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ImportOpenAPIConfiguration extends AbstractCommandHandler {

	/** Pattern that must be matched by a parameter reference. */
	protected static final Pattern GLOBAL_PARAMETER_REFERENCE = Pattern.compile("#/components/parameters/" + "([^/]+)");

	/** Pattern that must be matched by a schema reference. */
	protected static final Pattern GLOBAL_SCHEMA_REFERENCE = Pattern.compile("#/components/schemas/" + "([^/]+)");

	/**
	 * Configuration for the {@link ImportOpenAPIConfiguration}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		// Currently no additional configuration.

	}

	/**
	 * Configuration of the <i>OpenAPI</i> document containing the server configuration to
	 * create access methods for.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ImportConfiguration extends ConfigurationItem {

		/** Configuration name of {@link #getOpenAPIConfiguration()}. */
		String OPEN_API_CONFIGURATION = "open-api-configuration";

		/**
		 * {@link BinaryData} containing the JSON configuration of the <i>OpenAPI</i> server
		 * to create access methods for.
		 */
		@PropertyEditor(BinaryDataEditor.class)
		@AcceptedTypes({ ".yaml", ".yml", ".json", JsonUtilities.JSON_CONTENT_TYPE })
		@Mandatory
		BinaryData getOpenAPIConfiguration();

		/**
		 * Setter for {@link #getOpenAPIConfiguration()}.
		 */
		void setOpenAPIConfiguration(BinaryData conf);
	}

	/**
	 * Creates a new {@link ImportOpenAPIConfiguration}.
	 */
	public ImportOpenAPIConfiguration(InstantiationContext context, Config config) {
		super(context, config);
	}

	Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		DisplayDimension width = DisplayDimension.dim(300, DisplayUnit.PIXEL);
		DisplayDimension height = DisplayDimension.dim(150, DisplayUnit.PIXEL);
		return new CreateConfigurationDialog<>(ImportConfiguration.class,
			importConfig -> processUpload(importConfig, aComponent, model),
			I18NConstants.IMPORT_OPEN_API_CONFIG_DIALOG_TITLE,
			width,
			height)
				.open(aContext);
	}

	HandlerResult processUpload(ImportConfiguration importConfig, LayoutComponent component, Object model) {
		BinaryData openAPIConfiguration = importConfig.getOpenAPIConfiguration();
		ConfigurationDescriptor documentDescr = TypedConfiguration.getConfigurationDescriptor(OpenapiDocument.class);
		BufferingI18NLog log = new BufferingI18NLog();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log.asLog());
		JsonConfigurationReader reader = new JsonConfigurationReader(context, documentDescr);
		reader.treatUnexpectedEntriesAsWarn(true);
		try {
			reader.setSource(yamlToJson(openAPIConfiguration));
		} catch (IOException ex) {
			return HandlerResult.error(I18NConstants.ERROR_YAML_TO_JSON_FAILED, ex);
		}
		try {
			OpenapiDocument config = (OpenapiDocument) reader.read();
			if (!log.hasEntries()) {
				return apply(config, component, model);
			}
			Map<Level, List<Entry>> messages =
				log.getEntries().stream().collect(Collectors.groupingBy(Entry::getLevel));
			if (!CollectionUtil.nonNull(messages.get(Level.FATAL)).isEmpty()
				|| !CollectionUtil.nonNull(messages.get(Level.FATAL)).isEmpty()) {
				// Import has errors. Skip.
				return errorResult(log);
			}
			if (CollectionUtil.nonNull(messages.get(Level.WARN)).isEmpty()) {
				// No warning, just infos. Import.
				return apply(config, component, model);
			}

			InfoService.showWarningList(I18NConstants.WARNING_WHEN_PARSING_OPEN_API_DOCUMENT,
				messages(log).collect(Collectors.toList()));
			return apply(config, component, model);
		} catch (ConfigurationException ex) {
			HandlerResult error = errorResult(log);

			error.setException(new TopLogicException(ex.getErrorKey(), ex));
			return error;
		}
	}

	private Content yamlToJson(BinaryData openAPIConfiguration)
			throws IOException, StreamReadException, DatabindException, StreamWriteException {
		String name = openAPIConfiguration.getName();
		if (endsWithIgnoreCase(name, ".yaml") || endsWithIgnoreCase(name, ".yml")) {
			try (InputStream input = openAPIConfiguration.getStream()) {
				ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
				Object readValue = yamlReader.readValue(input, Object.class);

				ByteArrayStream jsonContent = new ByteArrayStream();
				new ObjectMapper().writeValue(jsonContent, readValue);
				return jsonData(name, jsonContent);
			}
		}
		return openAPIConfiguration;
	}

	private boolean endsWithIgnoreCase(String name, String suffix) {
		return name.regionMatches(true, name.length() - suffix.length(), suffix, 0, suffix.length());
	}

	private BinaryData jsonData(String name, ByteArrayStream jsonContent) {
		return new AbstractBinaryData() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public InputStream getStream() throws IOException {
				return jsonContent.getStream();
			}

			@Override
			public long getSize() {
				return jsonContent.size();
			}

			@Override
			public String getContentType() {
				return JsonUtilities.JSON_CONTENT_TYPE;
			}
		};
	}

	private HandlerResult apply(OpenapiDocument config, LayoutComponent component, Object model) {
		List<ResKey> warnings = new ArrayList<>();
		applyConfig(config, component, model, warnings);
		if (!warnings.isEmpty()) {
			InfoService.showWarningList(I18NConstants.WARNING_WHEN_PROCESSING_OPEN_API_SPECIFICATION, warnings);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Applies the given <i>OpenAPI</i> configuration.
	 * 
	 * @param config
	 *        The configuration to import.
	 * @param component
	 *        Component that imports the the specification.
	 * @param model
	 *        Model given in {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * @param warnings
	 *        Log to add potential warnings to.
	 */
	protected abstract void applyConfig(OpenapiDocument config, LayoutComponent component, Object model,
			List<ResKey> warnings);

	private HandlerResult errorResult(BufferingI18NLog log) {
		HandlerResult error = new HandlerResult();
		messages(log).forEach(error::addError);
		return error;
	}

	private Stream<ResKey> messages(BufferingI18NLog log) {
		return log.getEntries()
			.stream()
			.map(BufferingI18NLog.Entry::getMessage);
	}

	/**
	 * Creates (and adds) {@link AuthenticationConfig}'s based on the given {@link OpenapiDocument}.
	 * 
	 * @param openAPI
	 *        <i>OpenAPI</i> specification.
	 * @param auth
	 *        {@link AuthenticationConfigs} to enhance.
	 * @param warnings
	 *        Log to add potential warnings to.
	 */
	protected void addAuthentications(OpenapiDocument openAPI, AuthenticationConfigs auth, List<ResKey> warnings) {
		Map<String, AuthenticationConfig> authentications = auth.getAuthentications();
		ComponentsObject components = openAPI.getComponents();
		if (components != null) {
			Map<String, SecuritySchemeObject> securitySchemes = components.getSecuritySchemes();
			for (SecuritySchemeObject schema : securitySchemes.values()) {
				AuthenticationConfig authentication = createAuthentication(schema, warnings);
				if (authentication != null) {
					authentication.setDomain(schema.getSchemaName());
					authentications.put(authentication.getDomain(), authentication);
				}
			}
		}
	}

	private AuthenticationConfig createAuthentication(SecuritySchemeObject value, List<ResKey> warnings) {
		switch (value.getType()) {
			case API_KEY:
				APIKeyAuthentication apiKeyAuthentication =
					TypedConfiguration.newConfigItem(APIKeyAuthentication.class);
				apiKeyAuthentication.setPosition(value.getIn());
				apiKeyAuthentication.setParameterName(value.getName());
				return apiKeyAuthentication;
			case HTTP:
				String scheme = value.getScheme();
				if (scheme == null) {
					warnings.add(I18NConstants.MISSING_HTTP_SCHEME__SCHEMA.fill(value.getSchemaName()));
					return null;
				}
				switch (scheme.toLowerCase()) {
					case "basic": {
						// Actually "Basic" as of https://www.rfc-editor.org/rfc/rfc7617.html
						return TypedConfiguration.newConfigItem(BasicAuthentication.class);
					}
					default: {
						warnings.add(I18NConstants.MISSING_UNSUPPORTED_HTTP_SCHEME__SCHEME_SCHEMA.fill(scheme,
							value.getSchemaName()));
						return null;
					}
				}
			case OAUTH2: {
				Map<OAuthFlow, OAuthFlowObject> flows = value.getFlows();
				OAuthFlowObject credentialFlow = flows.get(OAuthFlow.CLIENT_CREDENTIALS);
				if (credentialFlow == null) {
					warnings.add(I18NConstants.MISSING_CLIENT_CREDENTIALS_FLOW__SCHEMA.fill(value.getSchemaName()));
					return null;
				}
				ClientCredentials clientCredentials = TypedConfiguration.newConfigItem(ClientCredentials.class);
				DefaultURIProvider.Config defaultURIProvider = newConfigForImplementation(DefaultURIProvider.class);
				try {
					defaultURIProvider.setTokenURL(new URL(credentialFlow.getTokenUrl()));
				} catch (MalformedURLException ex) {
					warnings.add(I18NConstants.INVALID_URL__URL__SCHEMA.fill(credentialFlow.getTokenUrl(),
						value.getSchemaName()));
				}
				clientCredentials.setURIProvider(defaultURIProvider);
				return clientCredentials;
			}
			case OPEN_ID_CONNECT: {
				URL connectURL = value.getOpenIdConnectUrl();
				OpenIDURIProvider.Config openIdURIProvider = newConfigForImplementation(OpenIDURIProvider.class);
				openIdURIProvider.setOpenIDIssuer(connectURL);

				ClientCredentials clientCredentials = TypedConfiguration.newConfigItem(ClientCredentials.class);
				clientCredentials.setURIProvider(openIdURIProvider);
				return clientCredentials;
			}
			default:
				throw new UnreachableAssertion("Unexpected SecuritySchemeType: " + value.getType());
		}

	}

	/**
	 * Creates a {@link PolymorphicConfiguration} that is appropriate for the given implementation
	 * class.
	 * 
	 * <p>
	 * <b>WARN:</b> The return value is unchecked. The caller must ensure that the types match.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	protected <T extends PolymorphicConfiguration<?>> T newConfigForImplementation(Class<?> implClass) {
		try {
			return (T) TypedConfiguration.createConfigItemForImplementationClass(implClass);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Resolves the concrete {@link ParameterObject} for the given {@link IParameterObject}, i.e.
	 * when the given parameter is a reference, the corresponding concrete {@link ParameterObject}
	 * is returned.
	 * 
	 * @return May be <code>null</code> when the given parameter is a reference but the referenced
	 *         parameter does not exist.
	 */
	protected ParameterObject resolveParameterObject(IParameterObject param,
			Map<String, ReferencableParameterObject> globalParameters, List<ResKey> warnings) {
		if (param instanceof ParameterObject) {
			return (ParameterObject) param;
		}
		String referencedParameterName = referencedParameterName((ReferencingParameterObject) param, warnings);
		if (referencedParameterName == null) {
			return null;
		}
		ReferencableParameterObject referencedParameter = globalParameters.get(referencedParameterName);
		if (referencedParameter == null) {
			warnings.add(I18NConstants.REFERENCED_PARAMETER_NOT_FOUND__REFERENCE__AVAILABLE
				.fill(referencedParameterName, globalParameters.keySet()));
			return null;
		}
		return referencedParameter;
	}

	/**
	 * Determines the reference name of a globally defined parameter.
	 * 
	 * @return May be <code>null</code>, when the reference does not contain a reference to a
	 *         globally defined parameter. If not <code>null</code> the reference is syntactically
	 *         correct but may not exist.
	 */
	protected String referencedParameterName(ReferencingParameterObject paramReference, List<ResKey> warnings) {
		String reference = paramReference.getReference();
		Matcher refMatcher = GLOBAL_PARAMETER_REFERENCE.matcher(reference);
		if (!refMatcher.matches()) {
			warnings.add(I18NConstants.REFERENCE_NOT_A_GLOBAL_PARAMETER__REFERENCE.fill(reference));
			return null;
		}
		return refMatcher.group(1);
	}

	/**
	 * Globally declared {@link ReferencableParameterObject} in the given {@link OpenapiDocument}.
	 */
	protected Map<String, ReferencableParameterObject> globalParameters(OpenapiDocument api) {
		ComponentsObject components = api.getComponents();
		if (components == null) {
			return Collections.emptyMap();
		}
		return components.getParameters();
	}

	/**
	 * Globally declared {@link SchemaObject } in the given {@link OpenapiDocument}.
	 */
	protected Map<String, SchemaObject> globalSchemas(OpenapiDocument api) {
		ComponentsObject components = api.getComponents();
		if (components == null) {
			return Collections.emptyMap();
		}
		return components.getSchemas();
	}

	/**
	 * Parses the given string serialization of an <code>OpenAPI</code> schema.
	 *
	 * @param schema
	 *        The scheme to de-serialize.
	 * @param parameterName
	 *        Name of the parameter in which the schema is defined. Used to log problems.
	 * @param globalSchemas
	 *        Mapping of global {@link SchemaObject} to resolve schema references.
	 * @param problems
	 *        {@link List} to add possible problems.
	 * @return The parsed schema. May be <code>null</code>, if there is a problem.
	 */
	protected Schema parseSchema(String schema, String parameterName, Map<String, SchemaObject> globalSchemas,
			List<ResKey> problems) {
		Schema schemaObject;
		try {
			Pattern globalSchemaReference = GLOBAL_SCHEMA_REFERENCE;
			schemaObject = OpenAPISchemaUtils.parseSchema(schema, new Function<String, Schema>() {
				@Override
				public Schema apply(String globalSchemaRef) {
					Matcher matcher = globalSchemaReference.matcher(globalSchemaRef);
					if (matcher.matches()) {
						String schemaName = matcher.group(1);
						SchemaObject globalSchema = globalSchemas.get(schemaName);
						if (globalSchema != null) {
							try {
								return OpenAPISchemaUtils.parseSchema(globalSchema.getSchema(), this);
							} catch (ParseException ex) {
								problems.add(
									I18NConstants.INVALID_GLOBAL_SCHEMA_DEFINITION__PARAMETER_NAME__PROBLEM__SCHEMA_NAME
										.fill(parameterName, schemaName, ex.getErrorKey()));
								return null;
							}
						}
	
						problems.add(I18NConstants.MISSING_GLOBAL_SCHEMA_DEFINITION__PARAMETER_SCHEMA
							.fill(parameterName, schemaName));
						return null;
					}
					problems.add(I18NConstants.INVALID_GLOBAL_SCHEMA_DEFINITION__PARAMETER_SCHEMA
						.fill(parameterName, globalSchemaRef));
					return null;
				}
			});
		} catch (ParseException ex) {
			problems.add(I18NConstants.INVALID_SCHEMA_DEFINITION__PARAMETER_PROBLEM
				.fill(parameterName, ex.getErrorKey()));
			schemaObject = null;
		}
		return schemaObject;
	}

}
