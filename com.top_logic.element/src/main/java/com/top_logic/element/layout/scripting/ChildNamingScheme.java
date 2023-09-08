/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import com.top_logic.basic.StringServices;
import com.top_logic.element.layout.scripting.ChildNamingScheme.ChildName;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * A {@link ModelNamingScheme} that identifies an {@link StructuredElement} by it's parent and it's
 * own name.
 * <p>
 * This is necessary for template scripts that have a {@link ValueRef} to the parent and the name of
 * the object, but no {@link ValueRef} to the object itself but need one.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ChildNamingScheme extends AbstractModelNamingScheme<StructuredElement, ChildName> {

	/** @see ChildNamingScheme */
	public interface ChildName extends NamedModelName {

		/** The parent of the object. */
		ModelName getParent();

		/** @see #getParent() */
		void setParent(ModelName parent);

	}

	@Override
	public Class<ChildName> getNameClass() {
		return ChildName.class;
	}

	@Override
	public Class<StructuredElement> getModelClass() {
		return StructuredElement.class;
	}

	@Override
	public StructuredElement locateModel(ActionContext context, ChildName name) {
		StructuredElement parent = (StructuredElement) context.resolve(name.getParent());
		return findChild(name, parent);
	}

	private StructuredElement findChild(ChildName childName, StructuredElement parent) {
		StructuredElement match = null;
		for (StructuredElement child : parent.getChildren()) {
			if (StringServices.equals(child.getName(), childName.getName())) {
				if (match != null) {
					throw new AssertionError("Found more than one child named '" + childName.getName()
						+ "' for parent: " + parent + "; First Match: " + match + "; Second Match: " + child);
				}
				match = child;
			}
		}
		if (match == null) {
			throw new AssertionError("Found no child named '" + childName.getName() + "' for parent: " + parent);
		}
		return match;
	}

	@Override
	protected void initName(ChildName name, StructuredElement model) {
		name.setName(model.getName());
		name.setParent(ModelResolver.buildModelName(model.getParent()));
	}

	@Override
	protected boolean isCompatibleModel(StructuredElement model) {
		// Should never be used automatically, as structured elements are already supported by the
		// ReferenceFactory.
		return false;
	}

}
