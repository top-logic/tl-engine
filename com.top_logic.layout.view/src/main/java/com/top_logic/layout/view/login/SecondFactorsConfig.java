/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * Application-configured list of the {@link SecondFactor} methods the React login offers.
 *
 * <p>
 * This module contributes the one-time codes of an authenticator app ({@link TotpSecondFactor}). A
 * feature module adds a method of its own through an application configuration fragment of its
 * own, without the view module depending on it:
 * </p>
 *
 * <pre>
 * &lt;config config:interface="com.top_logic.layout.view.login.SecondFactorsConfig"&gt;
 *     &lt;methods&gt;
 *         &lt;method name="my-method" class="my.MySecondFactor" image="css:bi bi-key"&gt;
 *             &lt;label&gt;&lt;en&gt;My method&lt;/en&gt;&lt;/label&gt;
 *         &lt;/method&gt;
 *     &lt;/methods&gt;
 * &lt;/config&gt;
 * </pre>
 *
 * @see SecondFactorStep#configured()
 */
public interface SecondFactorsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getMethods()}. */
	String METHODS = "methods";

	/** Tag name of an entry of {@link #getMethods()}. */
	String METHOD = "method";

	/**
	 * The second-factor methods offered, in the order they are listed for a choice.
	 *
	 * @implNote Keyed by {@link NamedConfiguration#NAME_ATTRIBUTE} so that the fragments of several
	 *           modules merge, and an application addresses a method by its name.
	 */
	@Key(NamedConfiguration.NAME_ATTRIBUTE)
	@Name(METHODS)
	@DefaultContainer
	@EntryTag(METHOD)
	List<SecondFactor.Config<?>> getMethods();

}
