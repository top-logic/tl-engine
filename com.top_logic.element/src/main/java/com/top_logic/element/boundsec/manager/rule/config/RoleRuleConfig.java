/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.model.TLClass;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.resources.TLPartScopedResourceProvider;
import com.top_logic.model.util.AllClasses;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.RoleNameMapping;

/**
 * Configuration of a role rule.
 *
 * @see RoleRulesConfig#getRules()
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	RoleRuleConfig.ID,
	RoleRuleConfig.XML_ATTRIBUTE_META_ELEMENT,
	RoleRuleConfig.XML_ATTRIBUTE_ROLE,
	RoleRuleConfig.XML_ATTRIBUTE_INHERIT,
	RoleRuleConfig.XML_ATTRIBUTE_TYPE,
	RoleRuleConfig.XML_ATTRIBUTE_SOURCE_META_ELEMENT,
	RoleRuleConfig.XML_ATTRIBUTE_SOURCE_ROLE,
	RoleRuleConfig.XML_ATTRIBUTE_RESOURCE_KEY,
	RoleRuleConfig.XML_TAG_PATH_ELEMENT,
})
public interface RoleRuleConfig extends NavigationRuleConfig {

	/** Name of the value of {@link #getId()} in the configuration. */
	String ID = "id";

	/** Name of the value of {@link #getSourceMetaElement()} in the configuration. */
	String XML_ATTRIBUTE_SOURCE_META_ELEMENT = "source-meta-element";

	/** Name of the value of {@link #getRole()} in the configuration. */
	String XML_ATTRIBUTE_ROLE = "role";

	/** Name of the value of {@link #getSourceRole()} in the configuration. */
	String XML_ATTRIBUTE_SOURCE_ROLE = "source-role";

	/** Name of the value of {@link #getResKey()} in the configuration. */
	String XML_ATTRIBUTE_RESOURCE_KEY = "resource-key";

	/** Name of the value of {@link #getType()} in the configuration. */
	String XML_ATTRIBUTE_TYPE = "type";

	/**
	 * Stable, unique identifier of this rule within its {@link RoleRulesConfig}.
	 *
	 * <p>
	 * On creation in a form the value is prefilled with a generated id; in a
	 * configuration file it must be specified explicitly.
	 * </p>
	 */
	@Name(RoleRuleConfig.ID)
	@Mandatory
	@ValueInitializer(UUIDInitializer.class)
	String getId();

	/** @see #getId() */
	void setId(String id);

	/**
	 * Optional name of the {@link TLClass} which the source object must have to get the given
	 * {@link #getRole()}.
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_SOURCE_META_ELEMENT)
	@Nullable
	@Options(fun = AllClasses.class, mapping = TLModelPartMapping.class)
	@OptionLabels(TLPartScopedResourceProvider.class)
	@ControlProvider(SelectionControlProvider.class)
	String getSourceMetaElement();

	/**
	 * Target roles which a user gets on the target objects.
	 */
	@Format(CommaSeparatedStrings.class)
	@Name(RoleRuleConfig.XML_ATTRIBUTE_ROLE)
	@Options(fun = BoundedRole.AllRoles.class, mapping = RoleNameMapping.class)
	@ControlProvider(SelectionControlProvider.class)
	List<String> getRole();

	/**
	 * If the user has one of the role on the source object, it gets the {@link #getRole() target
	 * role} on the target object.
	 */
	@Format(CommaSeparatedStrings.class)
	@Name(RoleRuleConfig.XML_ATTRIBUTE_SOURCE_ROLE)
	@Options(fun = BoundedRole.AllRoles.class, mapping = RoleNameMapping.class)
	@ControlProvider(SelectionControlProvider.class)
	List<String> getSourceRole();

	/**
	 * Which kind of rule is this?
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_TYPE)
	Type getType();

	/**
	 * {@link ResKey} defining the internationalisation for the rule.
	 */
	@Name(RoleRuleConfig.XML_ATTRIBUTE_RESOURCE_KEY)
	ResKey getResKey();
}
