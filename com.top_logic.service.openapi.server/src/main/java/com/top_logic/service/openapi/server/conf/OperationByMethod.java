/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.conf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency2;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.Constraints;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.admin.component.TLServiceConfigEditorFormBuilder;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.ImplOptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.common.authentication.AllAuthenticationDomains;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;
import com.top_logic.service.openapi.common.authentication.SecretAvailableConstraint;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.ParameterLocation;
import com.top_logic.service.openapi.common.document.TagObject;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;
import com.top_logic.service.openapi.server.authentication.ServerSecret;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.ParameterUsedIn;
import com.top_logic.service.openapi.server.parameter.ReferencedParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter.Config;

/**
 * {@link Operation} bound to a HTTP {@link #getMethod()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	OperationByMethod.METHOD,
	OperationByMethod.AUTHENTICATION_NAME,
	OperationByMethod.SUMMARY,
	OperationByMethod.DESCRIPTION,
	OperationByMethod.TAGS,
	OperationByMethod.PARAMETERS,
	OperationByMethod.RESPONSES,
	OperationByMethod.IMPLEMENTATION,
})
@Label("Operation")
public interface OperationByMethod extends Operation, ConfigPart, OpenAPIServerPart {

	/** Configuration name for {@link #getEnclosingPathItem()}. */
	String ENCLOSING_PATH_ITEM = "enclosing-path-item";

	/**
	 * @see #getAuthentication()
	 */
	String AUTHENTICATION_NAME = "authentication";

	/**
	 * @see #getMethod()
	 */
	String METHOD = "method";

	/**
	 * @see #getMethod()
	 */
	String TAGS = "tags";

	/**
	 * The HTTP method that triggers this operation.
	 */
	@Name(METHOD)
	@Mandatory
	HttpMethod getMethod();

	/**
	 * Setter for {@link #getMethod()}.
	 */
	void setMethod(HttpMethod value);

	/**
	 * The {@link PathItem} this {@link OperationByMethod} is a part of.
	 */
	@Container
	@Name(ENCLOSING_PATH_ITEM)
	PathItem getEnclosingPathItem();

	/**
	 * Tags for this operation.
	 */
	@Format(CommaSeparatedStrings.class)
	@Name(TAGS)
	@Options(fun = AllTags.class, args = {
		@Ref({ ENCLOSING_PATH_ITEM, OpenAPIServerPart.SERVER_CONFIGURATION, OpenApiServer.Config.TAGS }) })
	List<String> getTags();

	/**
	 * Setter for {@link #getTags()}.
	 */
	void setTags(List<String> value);

	/**
	 * Name of an {@link AuthenticationConfig} in the {@link OpenApiServer} that is used to
	 * authenticate access to this {@link PathItem}.
	 * 
	 * @return May be <code>null</code>, when no authentication is necessary.
	 */
	@Name(AUTHENTICATION_NAME)
	@Options(fun = AllAuthenticationDomains.class, args = {
		@Ref({ ENCLOSING_PATH_ITEM, OpenAPIServerPart.SERVER_CONFIGURATION, OpenApiServer.Config.AUTHENTICATIONS }) })
	@Nullable
	@Constraint(value = ServerSecretAvailable.class, asWarning = true, args = {
		@Ref({ ENCLOSING_PATH_ITEM, OpenAPIServerPart.SERVER_CONFIGURATION, OpenApiServer.Config.SECRETS }) })
	@Format(CommaSeparatedStrings.class)
	List<String> getAuthentication();

	/**
	 * Setter for {@link #getAuthentication()}.
	 */
	void setAuthentication(List<String> value);

	@Override
	@Constraints({
		@Constraint(value = NoPathParameterAssertion.class, args = {
			@Ref({ OperationByMethod.ENCLOSING_PATH_ITEM,
				PathItem.SERVER_CONFIGURATION,
				OpenApiServer.Config.GLOBAL_PARAMETERS })
		}),
		@Constraint(value = NonClashingParameters.class, args = {
			@Ref({ OperationByMethod.ENCLOSING_PATH_ITEM,
				PathItem.PARAMETERS }),
			@Ref({ OperationByMethod.ENCLOSING_PATH_ITEM,
				PathItem.SERVER_CONFIGURATION,
				OpenApiServer.Config.GLOBAL_PARAMETERS }),
		}),
		@Constraint(value = OnlyOneRequestBodyParameter.class, args = {
			@Ref({ OperationByMethod.ENCLOSING_PATH_ITEM,
				PathItem.PARAMETERS }) }),
		@Constraint(value = BodyParameterAllowedChecker.class, args = {
			@Ref({ OperationByMethod.ENCLOSING_PATH_ITEM,
				PathItem.PARAMETERS }),
			@Ref({ OperationByMethod.METHOD })
		}),
	})
	@Options(fun = AllRequestButPathParameters.class, mapping = ImplOptionMapping.class)
	List<Config<? extends RequestParameter<?>>> getParameters();

	@Override
	@DerivedRef({ OperationByMethod.ENCLOSING_PATH_ITEM, PathItem.OWNER })
	OpenApiServer.Config<? extends OpenApiServer> getServerConfiguration();

	/**
	 * Assertion that no path parameters are configured.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class NoPathParameterAssertion extends
			GenericValueDependency<List<Config<? extends RequestParameter<?>>>, Map<String, ReferencedParameter>> {

		/**
		 * Creates a new {@link NoPathParameterAssertion}.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public NoPathParameterAssertion() {
			super((Class) List.class, (Class) Map.class);
		}

		@Override
		protected void checkValue(PropertyModel<List<Config<? extends RequestParameter<?>>>> self,
				PropertyModel<Map<String, ReferencedParameter>> other) {
			List<String> pathParams = CollectionUtil.nonNull(self.getValue())
				.stream()
				.map(RequestParameter.Config.resolveUsing(CollectionUtil.nonNull(other.getValue())))
				.filter(Objects::nonNull)
				.filter(config -> config.getParameterLocation() == ParameterLocation.PATH)
				.map(RequestParameter.Config::getName)
				.collect(Collectors.toList());
			if (!pathParams.isEmpty()) {
				self.setProblemDescription(
					I18NConstants.ERROR_PATH_PARAMETER_NOT_ALLOWED_AT_OPERATION__PARAMS.fill(pathParams));
			}
		}

	}

	/**
	 * Function offering concrete {@link RequestParameter} classes that are <b>not</b> located in
	 * the API path.
	 * 
	 * @see ParameterUsedIn
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class AllRequestButPathParameters extends Function0<OptionModel<Class<?>>> {

		@Override
		public OptionModel<Class<?>> apply() {
			Collection<Class<?>> implClasses =
				TypeIndex.getInstance().getSpecializations(RequestParameter.class, true, false, false);

			ArrayList<Class<?>> implTypes = new ArrayList<>(implClasses.size());
			for (Class<?> implType : implClasses) {
				Hidden hidden = implType.getAnnotation(Hidden.class);
				if (hidden != null && hidden.value() == true) {
					continue;
				}
				ParameterUsedIn paramLoc = implType.getAnnotation(ParameterUsedIn.class);
				if (paramLoc != null && paramLoc.value() == ParameterLocation.PATH) {
					continue;
				}

				try {
					DefaultConfigConstructorScheme.getFactory(implType);
				} catch (NoClassDefFoundError | ConfigurationException ex) {
					// Ignore problematic class.
					continue;
				}

				implTypes.add(implType);
			}

			return new DefaultListOptionModel<>(implTypes);
		}

	}

	/**
	 * {@link ValueDependency} asserting that there is no clash between locally (i.e. at the
	 * operation) and globally (i.e. at the path item) defined parameters.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class NonClashingParameters extends
			GenericValueDependency2<List<Config<? extends RequestParameter<?>>>, List<Config<? extends RequestParameter<?>>>, Map<String, ReferencedParameter>> {

		/**
		 * Creates a new {@link NonClashingParameters}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public NonClashingParameters() {
			super((Class) List.class, (Class) List.class, (Class) Map.class);
		}

		@Override
		protected void checkValue(PropertyModel<List<Config<? extends RequestParameter<?>>>> self,
				PropertyModel<List<Config<? extends RequestParameter<?>>>> other,
				PropertyModel<Map<String, ReferencedParameter>> globalParams
				) {
					Map<String, ReferencedParameter> globalParameters = CollectionUtil.nonNull(globalParams.getValue());
			Set<TupleFactory.Pair<String, ParameterLocation>> localParameter = self.getValue()
				.stream()
				.map(RequestParameter.Config.resolveUsing(globalParameters))
				.filter(Objects::nonNull)
				.map(NonClashingParameters::getUniqueKey)
				.collect(Collectors.toSet());
			List<TupleFactory.Pair<String, ParameterLocation>> clashedParams = Collections.emptyList();
			for (Config<? extends RequestParameter<?>> apiParam : other.getValue()) {
				ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>> resolvedApiParam =
					apiParam.resolveParameter(globalParameters);
				if (resolvedApiParam == null) {
					// Another constraint has to check that all references can be resolved.
					continue;
				}
				Pair<String, ParameterLocation> globalParameter = getUniqueKey(resolvedApiParam);
				if (localParameter.contains(globalParameter)) {
					if (clashedParams.isEmpty()) {
						clashedParams = new ArrayList<>();
					}
					clashedParams.add(globalParameter);
				}
			}
			if (!clashedParams.isEmpty()) {
				self.setProblemDescription(
					I18NConstants.ERROR_LOCAL_GLOBAL_PARAMETER_CLASH__CLASHES.fill(clashedParams));
			}
		}

		static TupleFactory.Pair<String, ParameterLocation> getUniqueKey(ConcreteRequestParameter.Config<?> param) {
			return new TupleFactory.Pair<>(param.getName(), param.getParameterLocation());
		}

	}

	/**
	 * {@link GenericPropertyConstraint} checking that no body parameter are configured when the
	 * corresponding method does not allows it.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class BodyParameterAllowedChecker extends
			GenericValueDependency2<List<Config<? extends RequestParameter<?>>>, List<Config<? extends RequestParameter<?>>>, HttpMethod> {

		/**
		 * Creates a new {@link BodyParameterAllowedChecker}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public BodyParameterAllowedChecker() {
			super((Class) List.class, (Class) List.class, HttpMethod.class);
		}

		@Override
		protected void checkValue(PropertyModel<List<Config<? extends RequestParameter<?>>>> localParameters,
				PropertyModel<List<Config<? extends RequestParameter<?>>>> globalParameters,
				PropertyModel<HttpMethod> httpMethod) {
			if (httpMethod.getValue() == null || httpMethod.getValue().supportsBody()) {
				return;
			}
			checkParameters(localParameters, httpMethod);
			checkParameters(globalParameters, httpMethod);
		}

		private void checkParameters(PropertyModel<List<Config<? extends RequestParameter<?>>>> parameters,
				PropertyModel<HttpMethod> httpMethod) {
			Optional<Config<? extends RequestParameter<?>>> bodyParam =
				CollectionUtil.nonNull(parameters.getValue())
					.stream()
					.filter(RequestParameter.Config::isBodyParameter)
					.findAny();
			if (bodyParam.isPresent()) {
				Config<? extends RequestParameter<?>> config = bodyParam.get();
				ResKey error = I18NConstants.ERROR_BODY_PARAM_UNSUPPORTED_FOR_METHOD__PARAM__METHOD
					.fill(config.getName(), httpMethod.getValue());
				parameters.setProblemDescription(error);
				httpMethod.setProblemDescription(error);
			}
		}

	}

	/**
	 * {@link ValueDependency} asserting that there is at most one
	 * {@link com.top_logic.service.openapi.server.parameter.RequestParameter.Config#isBodyParameter()
	 * request body parameter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class OnlyOneRequestBodyParameter extends ValueDependency<List<Config<? extends RequestParameter<?>>>> {

		/**
		 * Creates a new {@link OnlyOneRequestBodyParameter}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public OnlyOneRequestBodyParameter() {
			super((Class) List.class);
		}

		@Override
		protected void checkValue(PropertyModel<List<Config<? extends RequestParameter<?>>>> self,
				PropertyModel<List<Config<? extends RequestParameter<?>>>> other) {
			Set<String> reqBodyParameters = Stream.concat(
				self.getValue()
					.stream()
					.filter(RequestParameter.Config::isBodyParameter)
					.map(RequestParameter.Config::getName),
				other.getValue()
					.stream()
					.filter(RequestParameter.Config::isBodyParameter)
					.map(RequestParameter.Config::getName))
				.collect(Collectors.toSet());
			if (reqBodyParameters.size() > 1) {
				self.setProblemDescription(
					I18NConstants.ERROR_MULTIPLE_REQUEST_BODY_PARAMETERS__PARAMETERS.fill(reqBodyParameters));
			}
		}

	}

	/**
	 * {@link ValueDependency} asserting that a {@link ServerSecret} for the configured
	 * {@link OperationByMethod#getAuthentication()} exists.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class ServerSecretAvailable extends SecretAvailableConstraint<List<ServerSecret>> {

		/**
		 * Creates a new {@link ServerSecretAvailable}.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ServerSecretAvailable() {
			super((Class) List.class);
		}

		@Override
		protected void checkValue(PropertyModel<List<String>> authentication,
				PropertyModel<List<ServerSecret>> serverSecrets) {
			checkSecretAvailable(authentication, serverSecrets.getValue());
		}

	}

	/**
	 * Determines the names of all available tags.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class AllTags extends Function1<List<String>, List<TagObject>> {

		private DeclarativeFormOptions _options;

		/**
		 * Creates a new {@link AllTags}.
		 */
		public AllTags(DeclarativeFormOptions options) {
			_options = options;
		}

		@Override
		public List<String> apply(List<TagObject> tags) {
			return resolveTags(tags).stream()
				.map(TagObject::getName)
				.collect(Collectors.toList());
		}

		private List<TagObject> resolveTags(
				List<TagObject> tags) {
			if (tags != null) {
				return tags;
			}
			TLServiceConfigEditorFormBuilder.EditModel formModel =
				(TLServiceConfigEditorFormBuilder.EditModel) _options.get(DeclarativeFormBuilder.FORM_MODEL);
			return ((OpenApiServer.Config<?>) formModel.getServiceConfiguration()).getTags();
		}


	}

}
