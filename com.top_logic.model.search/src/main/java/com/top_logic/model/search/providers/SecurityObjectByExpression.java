/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link SecurityObjectProvider} computing the security object by evaluating an {@link Expr} on the
 * base model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class SecurityObjectByExpression extends AbstractConfiguredInstance<SecurityObjectByExpression.Config>
		implements SecurityObjectProvider {

	/**
	 * Typed configuration interface definition for {@link SecurityObjectByExpression}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("security-object-by-expression")
	@DisplayOrder({
		Config.FUNCTION_PROPERTY,
		Config.SECURITY_OBJECT_TYPES,
	})
	public interface Config extends PolymorphicConfiguration<SecurityObjectByExpression> {

		/** Name of the property {@link #getFunction()}. */
		String FUNCTION_PROPERTY = "function";

		/** Configuration name for the value {@link #getSecurityObjectTypes()}. */
		String SECURITY_OBJECT_TYPES = "security-object-types";

		/**
		 * Function that computes the security object from the given model.
		 * 
		 * <p>
		 * The function is expected to accept one parameter, the model for which a security object
		 * is required. It must return the security object for the given model.
		 * </p>
		 * 
		 * @implNote The returned object must be a {@link BoundObject}.
		 */
		@Mandatory
		@Name(FUNCTION_PROPERTY)
		Expr getFunction();

		/**
		 * The collection of types that a security object may have.
		 */
		@Name(SECURITY_OBJECT_TYPES)
		@Options(fun = AllClasses.class, mapping = TLModelPartRef.PartMapping.class)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getSecurityObjectTypes();
	}

	private final QueryExecutor _function;

	private final Set<TLClass> _securityObjectTypes = new HashSet<>();

	/**
	 * Create a {@link SecurityObjectByExpression}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public SecurityObjectByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_function = QueryExecutor.compile(config.getFunction());
		TLModel applicationModel = ModelService.getApplicationModel();
		for (TLModelPartRef secObjectType : config.getSecurityObjectTypes()) {
			try {
				_securityObjectTypes.add(secObjectType.resolveClass(applicationModel));
			} catch (ConfigurationException ex) {
				context.error("Unable to resolve " + secObjectType.qualifiedName(), ex);
			}
		}
		if (_securityObjectTypes.isEmpty()) {
			// Compatibility
			_securityObjectTypes.add(TLModelUtil.tlObjectType(applicationModel));
		}
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		Object securityObject = _function.execute(model);
		if (securityObject != null && !(securityObject instanceof BoundObject)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NO_BOUND_OBJECT_RESULT__EXPR__SRC__VALUE.fill(_function.getSearch(), model,
					securityObject));
		}
		return (BoundObject) securityObject;
	}

	@Override
	public Set<TLClass> getPossibleSecurityObjectTypes() {
		return _securityObjectTypes;
	}

}

