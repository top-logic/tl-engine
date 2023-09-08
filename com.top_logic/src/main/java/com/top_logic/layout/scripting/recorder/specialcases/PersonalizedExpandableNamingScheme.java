/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.specialcases;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.structure.CollapsibleControl;
import com.top_logic.layout.structure.Expandable;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.PersonalizingExpandable;

/**
 * {@link ModelNamingScheme} for a {@link PersonalizingExpandable}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersonalizedExpandableNamingScheme
		extends ModelNamingScheme<LayoutControl, PersonalizingExpandable, PersonalizedExpandableNamingScheme.Name> {

	/**
	 * {@link ModelName} of a {@link PersonalizingExpandable}.
	 */
	public interface Name extends ModelName {

		/**
		 * {@link PersonalizingExpandable#getPersonalizationKey() Personalization key} of the
		 * identified expandable.
		 */
		String getKey();

		/**
		 * @see #getKey()
		 */
		void setKey(String personalizationKey);

	}

	@Override
	public Class<? extends Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<PersonalizingExpandable> getModelClass() {
		return PersonalizingExpandable.class;
	}

	@Override
	public Class<LayoutControl> getContextClass() {
		return LayoutControl.class;
	}

	@Override
	protected Maybe<Name> buildName(LayoutControl valueContext, PersonalizingExpandable model) {
		Name name = TypedConfiguration.newConfigItem(Name.class);
		name.setKey(model.getPersonalizationKey());
		return Maybe.some(name);
	}

	@Override
	public PersonalizingExpandable locateModel(ActionContext context, LayoutControl valueContext, Name name) {
		return findExpandable(context.getDisplayContext(), valueContext, name);
	}

	private PersonalizingExpandable findExpandable(DisplayContext context, LayoutControl layoutControl, Name name) {
		List<PersonalizingExpandable> expandables = new ArrayList<>();
		addAllExpandables(context, layoutControl, expandables, name.getKey());
		return getUniqueExpandable(name, expandables);
	}

	private PersonalizingExpandable getUniqueExpandable(Name name, List<PersonalizingExpandable> expandables) {
		switch (expandables.size()) {
			case 0:
				throw ApplicationAssertions.fail(name, "No collapsible toolbar of '" + name.getKey() + "' found.");
			case 1:
				return expandables.get(0);

			default:
				throw ApplicationAssertions.fail(name,
					"Multiple collapsible toolbars of '" + name.getKey() + "' found.");
		}
	}

	@SuppressWarnings("unused")
	private void addAllExpandables(DisplayContext context, LayoutControl control,
			List<PersonalizingExpandable> expandables, String key) {
		if (false) {
			/* Need to descend in layout control hierarchy also when control is not visible, because
			 * when a control is maximized, it is displayed in its chockpit whereas its parent is
			 * not displayed and therefore detached. */
			if (!control.isAttached()) {
				// Ignore detached controls as they are not visible.
				return;
			}
		}
		if (control instanceof CollapsibleControl) {
			// Ignore not displayed CollapsibleControls.
			if (control.isAttached()) {
				CollapsibleControl collapsible = (CollapsibleControl) control;
				Expandable model = collapsible.getToolbar().getModel();
				if (model instanceof PersonalizingExpandable) {
					PersonalizingExpandable expandable = (PersonalizingExpandable) model;
					if (key.equals(expandable.getPersonalizationKey())) {
						expandables.add(expandable);
					}
				}
			}
		}
		for (LayoutControl child : control.getChildren()) {
			addAllExpandables(context, child, expandables, key);
		}
	}

}
