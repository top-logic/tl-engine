/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.conf;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency2;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function3;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.ParameterLocation;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;
import com.top_logic.service.openapi.server.parameter.ReferencedParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter;
import com.top_logic.service.openapi.server.parameter.RequestParameter.Config;
import com.top_logic.service.openapi.server.utils.OpenAPIServerUtils;

/**
 * Describes the operations available on a single path. A Path Item MAY be empty, due to
 * <a href="http://spec.openapis.org/oas/v3.1.0#securityFiltering">ACL constraints</a>. The path
 * itself is still exposed to the documentation viewer but they will not know which operations and
 * parameters are available.
 * 
 * @see com.top_logic.service.openapi.server.OpenApiServer.Config#getPaths()
 */
@DisplayOrder({
	PathItem.PATH,
	PathItem.PARAMETERS,
	PathItem.OPERATIONS,
})
@com.top_logic.basic.config.annotation.TagName("PathItem")
@TitleProperty(name = PathItem.PATH)
public interface PathItem extends ParametersConfig, ConfigPart, OpenAPIServerPart {

	/** Pattern that match a variable in a path definition. */
	String VARIABLE_PATTERN = "\\{(" + RequestParameter.Config.VARIABLE_NAME_PATTERN + ")\\}";

	/**
	 * @see #getPath()
	 */
	String PATH = "path";

	/**
	 * @see #getCompletePath()
	 */
	String COMPLETE_PATH = "complete-path";

	/**
	 * @see #getOperations()
	 */
	String OPERATIONS = "operations";

	/**
	 * @see #getOwner()
	 */
	String OWNER = "owner";

	/**
	 * The servlet path to bind the operation to.
	 */
	@com.top_logic.basic.config.annotation.Name(PATH)
	@RegexpConstraint("(?:/([\\w\\-\\.]+|([\\w\\-\\.]*" + PathItem.VARIABLE_PATTERN + "[\\w\\-\\.]*)))+")
	@Constraint(value = AllParametersDeclared.class, args = {
		@Ref(PARAMETERS),
		@Ref({ SERVER_CONFIGURATION, OpenApiServer.Config.GLOBAL_PARAMETERS })
	})
	@Constraint(value = AllParametersUsed.class, asWarning = true, args = {
		@Ref(PARAMETERS),
		@Ref({ SERVER_CONFIGURATION, OpenApiServer.Config.GLOBAL_PARAMETERS })
	})
	String getPath();

	/**
	 * Setter for {@link #getPath()}.
	 */
	void setPath(String value);

	/**
	 * The complete path considering all path parameters.
	 */
	@Derived(fun = PathWithVariables.class, args = {
		@Ref(PATH),
		@Ref(PARAMETERS),
		@Ref({ SERVER_CONFIGURATION, OpenApiServer.Config.GLOBAL_PARAMETERS })
	})
	@Hidden
	@Name(COMPLETE_PATH)
	String getCompletePath();

	/**
	 * Operations to perform for a certain HTTP method.
	 */
	@com.top_logic.basic.config.annotation.Name(OPERATIONS)
	@Key(OperationByMethod.METHOD)
	Map<HttpMethod, OperationByMethod> getOperations();

	/**
	 * The <i>OpenAPI</i> server configuration to which this {@link PathItem} belongs.
	 */
	@Container
	@Name(OWNER)
	@Hidden
	OpenApiServer.Config<?> getOwner();

	@Override
	@DerivedRef(PathItem.OWNER)
	OpenApiServer.Config<? extends OpenApiServer> getServerConfiguration();

	/**
	 * Constraint checking that all path parameters are used in the path.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllParametersUsed extends
			GenericValueDependency2<String, List<? extends Config<? extends RequestParameter<?>>>, Map<String, ReferencedParameter>> {

		/**
		 * Creates a new {@link AllParametersUsed}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public AllParametersUsed() {
			super(String.class, (Class) List.class, (Class) Map.class);
		}

		@Override
		protected void checkValue(PropertyModel<String> path,
				PropertyModel<List<? extends Config<? extends RequestParameter<?>>>> params,
				PropertyModel<Map<String, ReferencedParameter>> globalParams) {
			List<String> containedParameters =
				OpenAPIServerUtils.containedParameters(StringServices.nonNull(path.getValue()));
			if (containedParameters.isEmpty()) {
				return;
			}
			Map<String, ReferencedParameter> globalParameters = CollectionUtil.nonNull(globalParams.getValue());
			Set<String> pathParams = CollectionUtil.nonNull(params.getValue()).stream()
				.map(RequestParameter.Config.resolveUsing(globalParameters))
				.filter(Objects::nonNull)
				.filter(config -> config.getParameterLocation() == ParameterLocation.PATH)
				.map(RequestParameter.Config::getName)
				.collect(Collectors.toCollection(HashSet::new));
			pathParams.removeAll(containedParameters);
			if (!pathParams.isEmpty()) {
				path.setProblemDescription(I18NConstants.WARN_PATH_PARAMETER_NOT_USED__PARAMS.fill(pathParams));
			}
		}

	}

	/**
	 * Constraint checking that all parameters in the given path are declared.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllParametersDeclared extends
			GenericValueDependency2<String, List<? extends Config<? extends RequestParameter<?>>>, Map<String, ReferencedParameter>> {

		/**
		 * Creates a new {@link AllParametersDeclared}.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public AllParametersDeclared() {
			super(String.class, (Class) List.class, (Class) Map.class);
		}

		@Override
		protected void checkValue(PropertyModel<String> path,
				PropertyModel<List<? extends Config<? extends RequestParameter<?>>>> params,
				PropertyModel<Map<String, ReferencedParameter>> globalParams
				) {
			List<String> containedParameters =
				OpenAPIServerUtils.containedParameters(StringServices.nonNull(path.getValue()));
			if (containedParameters.isEmpty()) {
				return;
			}
			Map<String, ReferencedParameter> globalParameters = CollectionUtil.nonNull(globalParams.getValue());
			Set<String> pathParams = CollectionUtil.nonNull(params.getValue()).stream()
				.map(RequestParameter.Config.resolveUsing(globalParameters))
				.filter(Objects::nonNull)
				.filter(config -> config.getParameterLocation() == ParameterLocation.PATH)
				.map(RequestParameter.Config::getName)
				.collect(Collectors.toSet());
			containedParameters.removeAll(pathParams);
			if (!containedParameters.isEmpty()) {
				path.setProblemDescription(
					I18NConstants.ERROR_PATH_PARAMETER_NOT_DECLARED__PARAMS.fill(containedParameters));
			}
		}

	}

	/**
	 * Computation of the final complete path considering all path parameters.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class PathWithVariables extends
			Function3<String, String, List<? extends RequestParameter.Config<? extends RequestParameter<?>>>, Map<String, ReferencedParameter>> {

		@Override
		public String apply(String path,
				List<? extends Config<? extends RequestParameter<?>>> pathParams,
				Map<String, ReferencedParameter> globalParams) {
			List<String> containedParameters = OpenAPIServerUtils.containedParameters(StringServices.nonNull(path));
			if (!containedParameters.isEmpty()) {
				return path;
			}
			List<String> pathParameters = pathParams.stream()
				.map(RequestParameter.Config.resolveUsing(CollectionUtil.nonNull(globalParams)))
				.filter(Objects::nonNull)
				.filter(config -> config.getParameterLocation() == ParameterLocation.PATH)
				.map(RequestParameter.Config::getName)
				.collect(Collectors.toList());
			StringBuilder finalPath = new StringBuilder(path);
			for (String paramName : pathParameters) {
				finalPath.append('/');
				finalPath.append('{');
				finalPath.append(paramName);
				finalPath.append('}');
			}
			return finalPath.toString();
		}

	}

}
