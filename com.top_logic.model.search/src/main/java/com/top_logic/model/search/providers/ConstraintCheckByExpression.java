/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLModel;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.expr.trace.ScriptTracer;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.Pointer;
import com.top_logic.util.model.ModelService;

/**
 * {@link ConstraintCheck} that can be parameterized by TL-Script expressions.
 * 
 * <p>
 * The check runs unsecured. Objects in the resulting message that the user may not discover are
 * replaced by a placeholder. A message rendered to a string inside the
 * script is not masked - pass objects as message arguments.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ConstraintCheckByExpression<C extends ConstraintCheckByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements ConstraintCheck {

	private final QueryExecutor _check;

	private final ScriptTracer _checkAnalyzer;

	private ConstraintType _type;

	/**
	 * Configuration options for {@link ConstraintCheckByExpression}.
	 */
	@TagName("constraint-by-expression")
	public interface Config<I extends ConstraintCheckByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Function testing a certain consistency constraint on an object.
		 * 
		 * <p>
		 * The function expects the current value of the context attribute as first argument and the
		 * base object defining the attribute as second argument. A failure is signaled by returning
		 * an internationalized text describing the problem. A <code>null</code> value is
		 * interpreted as success:
		 * </p>
		 * 
		 * <code>value -> object -> [expression computing {@link ResKey}]</code>
		 * 
		 * <p>
		 * The context attribute is the attribute, this {@link ConstraintCheck} algorithm is
		 * annotated to. It's value is the main value to check. Any check failure messages are
		 * reported at the input field for this context attribute, if the check is performed in the
		 * user interface.
		 * </p>
		 */
		Expr getCheck();

		/**
		 * The type of constraint to decide whether it should be reported as an error and saving
		 * should be prevented, or only a warning should be displayed in the GUI.
		 */
		ConstraintType getType();

	}

	/**
	 * Creates a {@link ConstraintCheckByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConstraintCheckByExpression(InstantiationContext context, C config) {
		super(context, config);

		_check = QueryExecutor.compile(config.getCheck());
		_checkAnalyzer = ScriptTracer.compile(model(), config.getCheck());
		disableSecurity();
		_type = config.getType();
	}

	/**
	 * Disables security for the scripts.
	 * 
	 * <p>
	 * For example, if an object's name must be unique, it must be unique across all objects, not
	 * just among the objects that user is allowed to see.
	 * </p>
	 */
	private void disableSecurity() {
		_check.disableSecurity();
		_checkAnalyzer.disableSecurity();
	}

	private static TLModel model() {
		return ModelService.getApplicationModel();
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		return maskUndiscoverable(toResKey(_check.execute(object.tValue(attribute), object)));
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		_checkAnalyzer.execute(trace, overlays, object.tValue(attribute), object);
	}

	private static ResKey toResKey(Object result) {
		if (result == null || result instanceof Boolean && ((Boolean) result).booleanValue()) {
			return null;
		}

		if (result instanceof ResKey) {
			return (ResKey) result;
		} else if (result instanceof String) {
			return ResKey.text((String) result);
		} else {
			return I18NConstants.ERROR_INVALID_VALUE;
		}
	}

	/**
	 * Replaces every argument of the given message that the current user may not discover by
	 * {@link I18NConstants#MASKED_OBJECT}, also in nested messages.
	 * 
	 * @param message
	 *        The message to mask, may be <code>null</code>.
	 * @return The given message, if nothing had to be masked, otherwise a copy with masked
	 *         arguments.
	 */
	@FrameworkInternal
	public static ResKey maskUndiscoverable(ResKey message) {
		if (message == null) {
			return null;
		}
		Object[] arguments = message.arguments();
		if (arguments == null || arguments.length == 0) {
			return message;
		}
		Object[] masked = new Object[arguments.length];
		boolean changed = false;
		for (int n = 0; n < arguments.length; n++) {
			Object argument = arguments[n];
			masked[n] = maskArgument(argument);
			changed |= masked[n] != argument;
		}
		return changed ? message.plain().asResKeyN().fill(masked) : message;
	}

	private static Object maskArgument(Object argument) {
		if (argument instanceof TLObject object) {
			return ModelAccessRights.getInstance().isDiscoverAllowed(object) ? argument
				: I18NConstants.MASKED_OBJECT;
		} else if (argument instanceof ResKey nested) {
			return maskUndiscoverable(nested);
		} else if (argument instanceof Collection<?> collection) {
			return maskElements(argument, collection);
		} else if (argument instanceof TupleFactory.Tuple tuple) {
			// Rendered element-wise like a collection.
			return maskElements(argument, Arrays.asList(TupleFactory.toArray(tuple)));
		} else if (argument instanceof Object[] array) {
			Object[] result = new Object[array.length];
			boolean changed = false;
			for (int n = 0; n < array.length; n++) {
				result[n] = maskArgument(array[n]);
				changed |= result[n] != array[n];
			}
			return changed ? result : argument;
		}
		return argument;
	}

	private static Object maskElements(Object original, Collection<?> elements) {
		List<Object> result = new ArrayList<>(elements.size());
		boolean changed = false;
		for (Object element : elements) {
			Object maskedElement = maskArgument(element);
			changed |= maskedElement != element;
			result.add(maskedElement);
		}
		return changed ? result : original;
	}

	@Override
	public ConstraintType type() {
		return _type;
	}

}
