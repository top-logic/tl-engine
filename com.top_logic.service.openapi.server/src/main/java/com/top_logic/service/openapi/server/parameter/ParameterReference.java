/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.Constraints;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.admin.component.TLServiceConfigEditorFormBuilder;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;

/**
 * Reference to globally defined {@link RequestParameter.Config}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ParameterReference extends RequestParameter<ParameterReference.Config> {

	/**
	 * Configuration for {@link ParameterReference}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("reference")
	public interface Config extends RequestParameter.Config<ParameterReference>, ConfigPart {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** @see #getOwner() */
		String OWNER = "owner";

		/**
		 * The name of the referenced parameter. The parameter that can be referenced can be found
		 * in the
		 * {@link com.top_logic.service.openapi.server.OpenApiServer.Config#getGlobalParameters()
		 * global parameters} of the
		 * {@link com.top_logic.service.openapi.server.OpenApiServer.Config}.
		 */
		@Override
		@Options(fun = GlobalParameterNames.class)
		@Constraints({
			@Constraint(value = ReferencedParameterIsDeclared.class, args = {
				@Ref({
					OWNER,
					OpenAPIServerPart.SERVER_CONFIGURATION,
					OpenApiServer.Config.GLOBAL_PARAMETERS
				})
			}),
		})
		String getName();

		/**
		 * The container of this {@link ParameterReference.Config}.
		 */
		@Container
		@Hidden
		@Name(OWNER)
		ConfigPart getOwner();

		@Override
		default ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>> resolveParameter(
				Map<String, ReferencedParameter> globalParams) {
			ReferencedParameter referencedParameter = globalParams.get(getName());
			if (referencedParameter == null) {
				return null;
			}
			return referencedParameter.getParameterDefinition();
		}

		/**
		 * {@link GenericValueDependency} checking that there is a global parameter for the
		 * {@link ParameterReference.Config}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public class ReferencedParameterIsDeclared
				extends GenericValueDependency<String, Map<String, ReferencedParameter>> {

			/**
			 * Creates a new {@link ReferencedParameterIsDeclared}.
			 */
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public ReferencedParameterIsDeclared() {
				super(String.class, (Class) Map.class);
			}

			@Override
			protected void checkValue(PropertyModel<String> nameProperty,
					PropertyModel<Map<String, ReferencedParameter>> propertyB) {
				Map<String, ReferencedParameter> globalParams = propertyB.getValue();
				if (globalParams == null) {
					// reference to global parameter delivers null
					return;
				}
				if (!globalParams.containsKey(nameProperty.getValue())) {
					nameProperty.setProblemDescription(
						I18NConstants.UNDEFINED_PARAMETER_REFERENCE__REFERENCE.fill(nameProperty.getValue()));
				}
			}

		}

		/**
		 * Function determining the names of the global parameter in the definition order.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public class GlobalParameterNames extends Function0<List<String>> {

			private DeclarativeFormOptions _options;

			/**
			 * Creates a new {@link GlobalParameterNames}.
			 */
			public GlobalParameterNames(DeclarativeFormOptions options) {
				_options = options;
			}
			
			@Override
			public List<String> apply() {
				return new ArrayList<>(globalParameters().keySet());
			}

			private Map<String, ReferencedParameter> globalParameters() {
				TLServiceConfigEditorFormBuilder.EditModel formModel =
					(TLServiceConfigEditorFormBuilder.EditModel) _options.get(DeclarativeFormBuilder.FORM_MODEL);
				return ((OpenApiServer.Config<?>) formModel.getServiceConfiguration()).getGlobalParameters();
			}
		}


	}

	/**
	 * Create a {@link ParameterReference}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ParameterReference(InstantiationContext context, Config config) {
		super(context, config);
	}

}

