/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.passkey.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.security.passkey.PasskeyService;
import com.top_logic.security.passkey.PasskeyUsage;

/**
 * {@link UIElement} that shows its content only when the passkeys of the application are used as
 * {@link Config#getUsage() configured}, the usage set in the configuration of the
 * {@link PasskeyService}.
 *
 * <p>
 * A view offers what belongs to one usage of the passkeys - a passkey that logs in by itself, or a
 * passkey as second factor - inside an element for that usage, so that the application shows only
 * the offers of the usage it is configured with:
 * </p>
 *
 * <pre>
 * &lt;if-passkey-usage usage="login"&gt;
 *     &lt;text&gt;
 *         &lt;label&gt;&lt;en&gt;A passkey signs you in without user name and password.&lt;/en&gt;&lt;/label&gt;
 *     &lt;/text&gt;
 *     ...
 * &lt;/if-passkey-usage&gt;
 * </pre>
 *
 * <p>
 * The usage is part of the configuration of the {@link PasskeyService}, so it is decided once, when
 * the control of the element is created. In another usage, the element shows an empty, hidden
 * content, which takes no place in its container (no gap of the enclosing stack on either side of
 * it), as a conditional element whose condition does not hold.
 * </p>
 *
 * @implNote The element has no control of its own: in the matching usage, the control of its
 *           content stands in its place, combined by {@link ContentControls#combine(ViewContext,
 *           List)} - a sole element as it is, several in a column.
 *
 * @see PasskeyService#getUsage()
 */
@InApp
public class PasskeyUsageElement extends ContainerElement {

	/**
	 * Configuration of a {@link PasskeyUsageElement}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-key")
	public interface Config extends ContainerElement.Config {

		/** The tag of the element in a view. */
		String TAG_NAME = "if-passkey-usage";

		/** Configuration name for {@link #getUsage()}. */
		String USAGE = "usage";

		@Override
		@ClassDefault(PasskeyUsageElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The usage of the passkeys in which the content is shown.
		 */
		@Name(USAGE)
		@Mandatory
		PasskeyUsage getUsage();
	}

	private final PasskeyUsage _usage;

	/**
	 * Creates a {@link PasskeyUsageElement} from configuration.
	 */
	@CalledByReflection
	public PasskeyUsageElement(InstantiationContext context, Config config) {
		super(context, config);
		_usage = config.getUsage();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		if (PasskeyService.getInstance().getUsage() != _usage) {
			ReactControl empty = ContentControls.toControl(List.of(), context);
			empty.setHidden(true);
			return empty;
		}
		return ContentControls.combine(context, createChildControls(context));
	}

}
