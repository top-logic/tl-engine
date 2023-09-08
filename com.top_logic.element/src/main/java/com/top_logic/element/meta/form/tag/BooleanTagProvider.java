/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.tag.CheckboxInputTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * {@link DisplayProvider} for {@link Boolean} attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanTagProvider extends IndirectDisplayProvider {

	/**
	 * Options for {@link BooleanTagProvider}
	 */
	public interface Config extends PolymorphicConfiguration<DisplayProvider> {

		/**
		 * @see #getResetable()
		 */
		String RESETABLE = "resetable";

		/**
		 * Whether the value can be reset to <code>null</code> at the UI.
		 */
		@Name(RESETABLE)
		boolean getResetable();

	}

	private final boolean _tristate;

	/**
	 * Creates a {@link BooleanTagProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BooleanTagProvider(InstantiationContext context, Config config) {
		_tristate = config.getResetable();
	}

	@Override
	public ControlProvider getControlProvider(EditContext editContext) {
		CheckboxInputTag result = new CheckboxInputTag();
		result.setResetable(isResetable(editContext));
		BooleanPresentation display = AttributeOperations.getBooleanDisplay(editContext);
		result.setDisplay(display);
		return result;
	}

	/**
	 * <pre>
	 *  mode  | tristate | mandatory | null value | resetable
	 *        |          |           | in storage |          
	 * ------------------------------------------------------
	 *  edit  |   nein   |   nein    |   nein(1)  |  nein
	 *  edit  |   ja     |   nein    |   ja       |  ja
	 *  edit  |   nein   |   ja      |   nein(1)  |  nein
	 *  edit  |   ja     |   ja      |   ja       |  nein
	 *        |          |           |            | = tristate && !mandatory
	 * ------------------------------------------------------
	 * search |   nein   |   nein    |   nein(1)  |  nein
	 * search |   ja     |   nein    |   ja       |  ja
	 * search |   nein   |   ja      |   nein(1)  |  nein
	 * search |   ja     |   ja      |   ja       |  ja
	 *        |          |           |            | = tristate
	 * ------------------------------------------------------
	 * </pre>
	 * 
	 * (1) Only as optimized internal value replacing <code>false</code> in flex storage.
	 */
	private boolean isResetable(EditContext editContext) {
		boolean search = editContext.isSearchUpdate();
		if (search) {
			return _tristate;
		} else {
			return _tristate && !editContext.isMandatory();
		}
	}

}
