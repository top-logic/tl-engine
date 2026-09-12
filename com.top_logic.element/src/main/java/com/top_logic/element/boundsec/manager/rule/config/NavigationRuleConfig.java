/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.rule.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.model.TLClass;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.resources.TLPartScopedResourceProvider;
import com.top_logic.model.util.AllClasses;

/**
 * Configuration to navigate from an object of a configured type to some target objects.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	NavigationRuleConfig.ID,
	NavigationRuleConfig.XML_ATTRIBUTE_META_ELEMENT,
	NavigationRuleConfig.XML_ATTRIBUTE_INHERIT,
	NavigationRuleConfig.XML_TAG_PATH_ELEMENT,
})
public interface NavigationRuleConfig extends ConfigurationItem {

	/** Name of the value of {@link #getId()} in the configuration. */
	String ID = "id";

	/** Name of the value of {@link #getMetaElement()} in the configuration. */
	String XML_ATTRIBUTE_META_ELEMENT = "meta-element";

	/** Name of the value of {@link #isInherit()} in the configuration. */
	String XML_ATTRIBUTE_INHERIT = "inherit";

	/**
	 * Name of the value of {@link #getPathElements()} in the configuration.
	 */
	String XML_TAG_PATH_ELEMENT = "path";

	/**
	 * Stable, unique identifier of this rule within the configuration declaring it.
	 *
	 * <p>
	 * On creation in a form the value is prefilled with a generated id; in a configuration file it
	 * must be specified explicitly.
	 * </p>
	 *
	 * @see RoleRulesConfig#getRules()
	 * @see SecurityParentsConfig#getRules()
	 */
	@Name(NavigationRuleConfig.ID)
	@Mandatory
	@ValueInitializer(UUIDInitializer.class)
	String getId();

	/** @see #getId() */
	void setId(String id);

	/**
	 * Full qualified name of the {@link TLClass} that an object must have such that
	 * {@link NavigationRuleConfig} is applied.
	 */
	@Name(NavigationRuleConfig.XML_ATTRIBUTE_META_ELEMENT)
	@Nullable
	@Options(fun = AllClasses.class, mapping = TLModelPartMapping.class)
	@OptionLabels(TLPartScopedResourceProvider.class)
	@ControlProvider(SelectionControlProvider.class)
	String getMetaElement();

	/**
	 * Whether this {@link NavigationRuleConfig} should also be applied to all sub types of
	 * {@link #getMetaElement()}.
	 */
	@Name(NavigationRuleConfig.XML_ATTRIBUTE_INHERIT)
	boolean isInherit();

	/**
	 * The configuration of the steps to get from the source object to the target object.
	 */
	@Name(NavigationRuleConfig.XML_TAG_PATH_ELEMENT)
	@DefaultContainer
	List<PolymorphicConfiguration<? extends PathElement>> getPathElements();

}
