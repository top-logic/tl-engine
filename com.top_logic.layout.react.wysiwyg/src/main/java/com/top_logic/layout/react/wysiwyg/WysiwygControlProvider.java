/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;

/**
 * {@link ReactFieldControlProvider} for {@code tl.model.wysiwyg:Html} attributes.
 *
 * <p>
 * The editor offers a button inserting a link to an application object where an
 * {@link ObjectLinkConfig} says which dialog the object is picked in. Without that option, the
 * editor formats text and nothing else.
 * </p>
 */
public class WysiwygControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link WysiwygControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<WysiwygControlProvider> {

		/** @see #getObjectLink() */
		String OBJECT_LINK = "object-link";

		@Override
		@ClassDefault(WysiwygControlProvider.class)
		Class<? extends WysiwygControlProvider> getImplementationClass();

		/**
		 * Where the editor gets the object a link is inserted for.
		 *
		 * <p>
		 * Set to offer the button inserting a link to an application object. Without it, the
		 * editor has no such button.
		 * </p>
		 */
		@Name(OBJECT_LINK)
		@Nullable
		ObjectLinkConfig getObjectLink();

	}

	private final ObjectLinkConfig _objectLink;

	/**
	 * Creates a {@link WysiwygControlProvider} from configuration.
	 */
	@CalledByReflection
	public WysiwygControlProvider(InstantiationContext context, Config config) {
		_objectLink = config.getObjectLink();
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new ReactWysiwygControl(context, model, _objectLink);
	}

}
