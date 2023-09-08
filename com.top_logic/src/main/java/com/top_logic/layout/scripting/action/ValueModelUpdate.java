/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link FormInput} which does not updates the referenced {@link FormMember}, but the
 * {@link ValueModel} attached to it.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ValueModelUpdate extends FormInput {

	/**
	 * Records a {@link ValueModelUpdate} with given arguments and mode {@link FormInput.Mode#SET}.
	 * 
	 * @see #recordValueModelUpdate(FormMember, Object,
	 *      com.top_logic.layout.scripting.action.FormInput.Mode)
	 */
	static void recordValueModelUpdate(FormMember member, Object value) {
		recordValueModelUpdate(member, value, Mode.SET);
	}

	/**
	 * Records a {@link ValueModelUpdate} with given arguments.
	 * 
	 * @param member
	 *        The {@link FormMember} to create action for.
	 * @param value
	 *        The new value to set into the {@link ValueModel} of the given member.
	 * @param mode
	 *        Whether the value must be updated absolute or incremental.
	 */
	static void recordValueModelUpdate(FormMember member, Object value, Mode mode) {
		ValueModelUpdate action = TypedConfiguration.newConfigItem(ValueModelUpdate.class);
		action.setField(ModelResolver.buildModelName(member));
		action.setValue(ModelResolver.buildModelName(member, value));
		action.setMode(mode);
		ScriptingRecorder.recordAction(action);
	}

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * Default implementation of {@link ValueModelUpdate}.
	 */
	class Op extends AbstractApplicationActionOp<ValueModelUpdate> {

		/**
		 * Creates a {@link ValueModelUpdate.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Op(InstantiationContext context, ValueModelUpdate config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			FormMember member = (FormMember) ModelResolver.locateModel(context, getConfig().getField());
			Object value = ModelResolver.locateModel(context, member, getConfig().getValue());
			ValueModel valueModel = EditorFactory.getValueModel(member);
			if (valueModel == null) {
				throw ApplicationAssertions.fail(getConfig(), "No value model found for " + member.getQualifiedName());
			}
			switch (getConfig().getMode()) {
				case ADD:
					valueModel.addValue(value);
					break;
				case REMOVE:
					valueModel.removeValue(value);
					break;
				case SET:
					valueModel.setValue(value);
					break;
				default:
					throw Mode.noSuchMode(getConfig().getMode());
			}
			return argument;
		}
	}

}

