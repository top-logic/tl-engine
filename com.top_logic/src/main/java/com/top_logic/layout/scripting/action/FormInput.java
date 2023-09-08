/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import static com.top_logic.basic.StringServices.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * {@link ApplicationAction} setting a value into a {@link FormField}.
 * 
 * @see #getValue()
 * @see #getField()
 * 
 * @see FormRawInput Setting the value using the raw value from the UI.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormInput extends AbstractFormInput {

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The processing mode of the input operation.
	 */
	Mode getMode();

	/**
	 * @see #getMode()
	 */
	void setMode(Mode value);

	/**
	 * Processing mode of a {@link FormInput} operation.
	 */
	enum Mode {
		/**
		 * Setting the new value replacing the old one completely.
		 */
		SET,

		/**
		 * Adding the given value to the existing value collection.
		 */
		ADD,

		/**
		 * Removing the given value form the existing value collection.
		 */
		REMOVE;

		/**
		 * Method to call in default case in switch blocks to trigger error in case some case is not
		 * covered.
		 */
		public static UnreachableAssertion noSuchMode(Mode mode) {
			return new UnreachableAssertion("No such mode: " + mode);
		}
	}

	/**
	 * Default implementation of {@link FormInput}.
	 */
	class Op<S extends FormInput> extends AbstractApplicationActionOp<S> {

		/**
		 * Creates a {@link FormInput.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		public Op(InstantiationContext context, S config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			FormField field = (FormField) ModelResolver.locateModel(context, getConfig().getField());
			Object value = ModelResolver.locateModel(context, field, getConfig().getValue());

			Object adaptedValue = checkAndAdaptValue(field, value);
			Object newValue = processMode(field, adaptedValue);
			try {
				if (field instanceof AbstractFormField) {
					FormFieldInternals.setValue((AbstractFormField) field, newValue);
				} else {
					field.setValue(newValue);
					field.checkWithAllDependencies();
				}
			} catch (VetoException ex) {
				ApplicationAssertions.fail(getConfig(), "Unexpected veto received.", ex);
			}

			return argument;
		}

		private Object processMode(FormField field, Object value) {
			switch (getConfig().getMode()) {
				case SET: {
					return value;
				}
				case ADD: {
					return addValue(field, value);
				}
				case REMOVE: {
					return removeValue(field, value);
				}
				default:
					throw Mode.noSuchMode(getConfig().getMode());
			}
		}

		private Object addValue(FormField field, Object value) {
			List<Object> current = currentList(field);
			Collection<Object> added = asList(value);
			added.removeAll(current);
			current.addAll(added);
			return current;
		}

		private Object removeValue(FormField field, Object value) {
			List<Object> current = currentList(field);
			Collection<Object> removed = asList(value);
			current.removeAll(removed);
			return current;
		}

		private List<Object> currentList(FormField field) {
			Object currentValue = field.getValue();
			if (!(currentValue instanceof Collection)) {
				throw ApplicationAssertions.fail(getConfig(), "Operation " + getConfig().getMode()
					+ " can only be applied to collection valued fields (e.g. SelectField).");
			}
			return new ArrayList<>((Collection<?>) currentValue);
		}

		private List<Object> asList(Object value) {
			if (value instanceof Collection) {
				return new ArrayList<>((Collection<?>) value);
			} else {
				return CollectionUtil.intoList(value);
			}
		}

		private Object checkAndAdaptValue(FormField field, Object value) {
			if (field instanceof SelectField) {
				SelectField selectField = (SelectField) field;
				Collection<?> valueCollection;
				if (value instanceof Collection<?>) {
					valueCollection = (Collection<?>) value;
				} else if (value == null) {
					valueCollection = Collections.emptyList();
				} else {
					valueCollection = Collections.singletonList(value);
				}
				if (selectField.isOptionsList()) {
					isInOptionList(selectField, valueCollection);
				} else if (selectField.isOptionsTree()) {
					isInOptionTree(selectField, valueCollection);
				}

				return valueCollection;
			} else {
				return value;
			}
		}

		private void isInOptionList(SelectField selectField, Collection<?> valueCollection) {
			List<?> optionList = selectField.getOptions();
			for (Object value : valueCollection) {
				if (!optionList.contains(value)) {
					// Note: An inconsistent selection is not required to be fixed when only
					// changing
					// other values, see Ticket #17315.
					if (!selectField.getSelection().contains(value)) {
						String errorMessage =
							"Tried to select something that is neither one of the options nor currently selected. Description of that object: "
								+ debug(valueCollection) + "; Options: " + debug(optionList);
						throw new RuntimeException(errorMessage);
					}
				}
			}
		}

		private void isInOptionTree(SelectField selectField, Collection<?> valueCollection) {
			@SuppressWarnings("unchecked")
			TLTreeModel<Object> optionTree = (TLTreeModel<Object>) selectField.getOptionsAsTree().getBaseModel();
			for (Object valueNode : valueCollection) {
				if (!optionTree.containsNode(valueNode)) {
					String errorMessage =
						"Tried to select something that is not one of the options. Description of that object: "
							+ debug(valueCollection);
					throw new RuntimeException(errorMessage);
				}
			}
		}

	}

}
