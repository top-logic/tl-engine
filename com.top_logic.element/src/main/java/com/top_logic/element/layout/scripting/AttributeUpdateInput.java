/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.action.AbstractFormInput;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link AbstractFormInput} to update the {@link AttributeUpdate} of the referenced
 * {@link FormMember}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AttributeUpdateInput extends AbstractFormInput {

	/**
	 * Records an {@link AttributeUpdateInput} with the given parameters.
	 * 
	 * @param member
	 *        The {@link FormMember} to create action for.
	 * @param value
	 *        The new value for the {@link AttributeUpdate} of the given member.
	 */
	static void recordAttributeUpdateInput(FormMember member, Object value) {
		AttributeUpdateInput action = TypedConfiguration.newConfigItem(AttributeUpdateInput.class);
		action.setField(ModelResolver.buildModelName(member));
		action.setValue(ModelResolver.buildModelName(member, value));
		ScriptingRecorder.recordAction(action);
	}

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * Default implementation of {@link AttributeUpdateInput}.
	 */
	class Op extends AbstractApplicationActionOp<AttributeUpdateInput> {

		/**
		 * Creates a {@link AttributeUpdateInput.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Op(InstantiationContext context, AttributeUpdateInput config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			FormMember member = (FormMember) ModelResolver.locateModel(context, getConfig().getField());
			Object value = ModelResolver.locateModel(context, member, getConfig().getValue());
			AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(member);
			if (update == null) {
				throw ApplicationAssertions.fail(getConfig(), "No update found for " + member.getQualifiedName());
			}
			update.updateValue(value);
			return argument;
		}
	}

}

