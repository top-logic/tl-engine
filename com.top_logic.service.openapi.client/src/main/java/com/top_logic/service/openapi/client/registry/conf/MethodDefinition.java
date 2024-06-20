/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.conf;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.client.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.client.authentication.ClientSecret;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry.ServiceRegistryPart;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandlerByExpression;
import com.top_logic.service.openapi.client.registry.impl.response.ResponseHandlerFactory;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretAvailableConstraint;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.Described;

/**
 * Configuration of a TL-Script function calling an external API.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	MethodDefinition.NAME_ATTRIBUTE,
	MethodDefinition.DESCRIPTION,
	MethodDefinition.PARAMETERS,
	MethodDefinition.BASE_URL,
	MethodDefinition.HTTP_METHOD,
	MethodDefinition.AUTHENTICATION,
	MethodDefinition.CALL_BUILDERS,
	MethodDefinition.RESPONSE_HANDLER,
	MethodDefinition.OPTIONAL_RESPONSE_HANDLERS,
})
public interface MethodDefinition extends ParameterContext, NamedConfigMandatory, ServiceRegistryPart, Described {

	/**
	 * @see #getParameters()
	 */
	String PARAMETERS = "parameters";

	/**
	 * @see #getBaseUrl()
	 */
	String BASE_URL = "base-url";

	/**
	 * @see #getHttpMethod()
	 */
	String HTTP_METHOD = "http-method";

	/**
	 * @see #getCallBuilders()
	 */
	String CALL_BUILDERS = "call-builders";

	/**
	 * @see #getResponseHandler()
	 */
	String RESPONSE_HANDLER = "response-handler";

	/**
	 * @see #getOptionalResponseHandlers()
	 */
	String OPTIONAL_RESPONSE_HANDLERS = "optional-response-handlers";

	/**
	 * @see #getAuthentication()
	 */
	String AUTHENTICATION = "authentication";

	/**
	 * The name of the TL-Script function that calls the external API.
	 */
	@Override
	String getName();

	/**
	 * Template for the URL for invoking the external API.
	 * 
	 * <p>
	 * The template may contain placeholders (<i>{param}</i>) with references to
	 * {@link #getParameters()} that are replaced upon API invocation to transmit arguments to the
	 * called API.
	 * </p>
	 */
	@Name(BASE_URL)
	@Mandatory
	String getBaseUrl();
	
	/**
	 * Setter for {@link #getBaseUrl()}.
	 */
	void setBaseUrl(String value);

	/**
	 * The {@link HttpMethod} to invoke the external API.
	 */
	@Name(HTTP_METHOD)
	HttpMethod getHttpMethod();

	/**
	 * Setter for {@link #getHttpMethod()}.
	 */
	void setHttpMethod(HttpMethod value);

	/**
	 * Definitions of parameters that can be passed to the TL-Script function that invokes the
	 * external API.
	 * 
	 * <p>
	 * Those parameters can be used in {@link #getCallBuilders()} to pass a value to the external
	 * API.
	 * </p>
	 */
	@Name(PARAMETERS)
	@Key(ParameterDefinition.NAME_ATTRIBUTE)
	@Label("Function parameters")
	List<ParameterDefinition> getParameters();

	/**
	 * Name of the {@link AuthenticationConfig}s that are used to access the Open API server.
	 */
	@Name(AUTHENTICATION)
	@Options(fun = AllAuthenticationDomains.class, args = {
		@Ref({ ServiceRegistryPart.SERVICE_REGISTRY, ServiceMethodRegistry.Config.AUTHENTICATIONS }) })
	@Nullable
	@Constraint(value = ClientSecretAvailable.class, asWarning = true, args = {
		@Ref({ ServiceRegistryPart.SERVICE_REGISTRY, ServiceMethodRegistry.Config.SECRETS }),
		@Ref({ ServiceRegistryPart.SERVICE_REGISTRY, ServiceMethodRegistry.Config.AUTHENTICATIONS })
	})
	@Format(CommaSeparatedStrings.class)
	List<String> getAuthentication();

	/**
	 * Setter for {@link #getAuthentication()}.
	 */
	void setAuthentication(List<String> value);

	/**
	 * Specification of arguments passed to the request invoking the external API.
	 */
	@Name(CALL_BUILDERS)
	List<PolymorphicConfiguration<? extends CallBuilderFactory>> getCallBuilders();

	/**
	 * Post-processor of the response to the API invocation.
	 */
	@Name(RESPONSE_HANDLER)
	@NonNullable
	@ItemDefault
	@ImplementationClassDefault(ResponseHandlerByExpression.class)
	PolymorphicConfiguration<? extends ResponseHandlerFactory> getResponseHandler();

	/**
	 * Setter for {@link #getResponseHandler()}.
	 */
	void setResponseHandler(PolymorphicConfiguration<? extends ResponseHandlerFactory> value);

	/**
	 * Optional post-processors of the response to the API invocation for special response codes.
	 */
	@Name(OPTIONAL_RESPONSE_HANDLERS)
	List<ResponseForStatusCodes> getOptionalResponseHandlers();

	@Override
	@Derived(fun = GetParameterNames.class, args = { @Ref(PARAMETERS) })
	List<String> getParameterNames();

	/**
	 * Implementation of {@link MethodDefinition#getParameterNames()}.
	 */
	class GetParameterNames extends Function1<List<String>, List<ParameterDefinition>> {
		@Override
		public List<String> apply(List<ParameterDefinition> defs) {
			return defs.stream().map(p -> p.getName()).collect(Collectors.toList());
		}
	}

	/**
	 * {@link ValueDependency} asserting that a {@link ClientSecret} for the configured
	 * {@link MethodDefinition#getAuthentication()} exists.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class ClientSecretAvailable extends SecretAvailableConstraint<Map<String, ClientSecret>> {

		/**
		 * Creates a new {@link ClientSecretAvailable}.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ClientSecretAvailable() {
			super((Class) Map.class);
		}

		@Override
		public boolean isChecked(int index) {
			return index == 0;
		}

		@Override
		protected void checkValue(PropertyModel<List<String>> authentication,
				PropertyModel<Map<String, ClientSecret>> clientSecrets,
				PropertyModel<Map<String, ? extends AuthenticationConfig>> availableAuthentications
		) {
			checkSecretAvailable(authentication, CollectionUtil.nonNull(clientSecrets.getValue()).values(),
				CollectionUtil.nonNull(availableAuthentications.getValue()));
		}

	}

}
