/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.security.AllToolGroups;
import com.top_logic.tl.ai.core.security.AllToolNames;
import com.top_logic.tl.ai.core.security.ToolGroupResources;
import com.top_logic.tl.ai.core.security.ToolNameResources;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * One entry of the tools the MCP server publishes: a single tool, or every tool of a tool group.
 *
 * <p>
 * An entry names either a {@link #getTool() tool} or a {@link #getGroup() group}, never both. Only a
 * tool entry may say how the tool is published - its {@link #getName() name}, {@link #getTitle()
 * title} and {@link #getDescription() description} - because these describe one tool and a group
 * has many. A tool that is named by its own entry and is also a member of a listed group is
 * published once, as its own entry says.
 * </p>
 *
 * @see McpExposure
 */
@DisplayOrder({
	ExposedTool.TOOL,
	ExposedTool.GROUP,
	ExposedTool.NAME,
	ExposedTool.TITLE,
	ExposedTool.DESCRIPTION,
})
public interface ExposedTool extends ConfigurationItem {

	/**
	 * Configuration property name for {@link #getTool()}.
	 *
	 * @see #getTool()
	 */
	String TOOL = "tool";

	/**
	 * Configuration property name for {@link #getGroup()}.
	 *
	 * @see #getGroup()
	 */
	String GROUP = "group";

	/**
	 * Configuration property name for {@link #getName()}.
	 *
	 * @see #getName()
	 */
	String NAME = "name";

	/**
	 * Configuration property name for {@link #getTitle()}.
	 *
	 * @see #getTitle()
	 */
	String TITLE = "title";

	/**
	 * Configuration property name for {@link #getDescription()}.
	 *
	 * @see #getDescription()
	 */
	String DESCRIPTION = "description";

	/**
	 * The tool to publish, by its qualified name ({@code factory.tool}).
	 *
	 * <p>
	 * Leave this empty if the entry names a tool group instead.
	 * </p>
	 */
	@Name(TOOL)
	@Nullable
	@Options(fun = AllToolNames.class)
	@OptionLabels(ToolNameResources.class)
	String getTool();

	/**
	 * Setter for {@link #getTool()}.
	 */
	void setTool(String value);

	/**
	 * The tool group whose tools to publish, all of them under their default name, title and
	 * description.
	 *
	 * <p>
	 * A tool belongs to the group the agent tool access policy assigns it to, or else to the group it
	 * declares itself - the same group a role is granted. Leave this empty if the entry names a single
	 * tool instead.
	 * </p>
	 *
	 * @see ToolGroups
	 */
	@Name(GROUP)
	@Nullable
	@Options(fun = AllToolGroups.class)
	@OptionLabels(ToolGroupResources.class)
	String getGroup();

	/**
	 * Setter for {@link #getGroup()}.
	 */
	void setGroup(String value);

	/**
	 * The name an MCP client calls the tool by.
	 *
	 * <p>
	 * At most 64 characters out of letters, digits, {@code _} and {@code -}. Empty means the tool's
	 * registry name, {@code factory__tool}. Prefer a short, verb-first name that is unique within the
	 * application, for example {@code search_projects}: an MCP client puts its own server prefix in
	 * front of it.
	 * </p>
	 */
	@Name(NAME)
	@Nullable
	String getName();

	/**
	 * Setter for {@link #getName()}.
	 */
	void setName(String value);

	/**
	 * The name of the tool a person reads in the MCP client.
	 *
	 * <p>
	 * Empty means a title derived from the tool's own name ({@code listModules} becomes "List
	 * modules").
	 * </p>
	 */
	@Name(TITLE)
	@Nullable
	String getTitle();

	/**
	 * Setter for {@link #getTitle()}.
	 */
	void setTitle(String value);

	/**
	 * What the tool does, written for an external model.
	 *
	 * <p>
	 * Empty means the tool's own description, which is written for the application's own agents and
	 * may refer to things an external model does not have, such as the conversation's selection or
	 * other tools by their internal names.
	 * </p>
	 */
	@Name(DESCRIPTION)
	@Nullable
	@MultiLine
	String getDescription();

	/**
	 * Setter for {@link #getDescription()}.
	 */
	void setDescription(String value);
}
