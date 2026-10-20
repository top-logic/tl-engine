/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Display names and explanations of the {@link ToolGroups tool groups}.
 *
 * <p>
 * A group is what an administrator grants, so its name is read by someone deciding whether a role
 * should hold it - in the tool access policy editor, and in the capability notice an agent is given.
 * {@code data.read} says nothing about what it covers to a reader who has not read the
 * documentation, so each group carries a label and one sentence naming the tools behind it.
 * </p>
 *
 * <p>
 * The tooltips are the one place where the meaning of a group is stated for a human. Keep them to a
 * single sentence, name what the group lets an agent <em>do</em> rather than which classes implement
 * it, and say "privileged" where the group is one - that is the difference an administrator has to
 * see before granting.
 * </p>
 *
 * @see ToolGroups
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Label of the {@link ToolGroups#MODEL_READ} tool group.
	 *
	 * @en Read the model
	 * @de Modell lesen
	 * @tooltip Inspect the application model, its layouts and the configuration schema, read-only. The
	 *          model, not the data in it.
	 */
	public static ResKey GROUP_MODEL_READ;

	/**
	 * Label of the {@link ToolGroups#DATA_READ} tool group.
	 *
	 * @en Read data
	 * @de Daten lesen
	 * @tooltip Read business data: browse instances, search the model for them, search semantically.
	 */
	public static ResKey GROUP_DATA_READ;

	/**
	 * Label of the {@link ToolGroups#DATA_WRITE} tool group.
	 *
	 * @en Change data
	 * @de Daten ändern
	 * @tooltip Change business data. Every call still asks the acting user to approve it.
	 */
	public static ResKey GROUP_DATA_WRITE;

	/**
	 * Label of the {@link ToolGroups#SCRIPT} tool group.
	 *
	 * @en Execute TL-Script
	 * @de TL-Script ausführen
	 * @tooltip Privileged: execute TL-Script, which can express any data operation, and read its
	 *          function reference.
	 */
	public static ResKey GROUP_SCRIPT;

	/**
	 * Label of the {@link ToolGroups#MODEL_STRUCTURE} tool group.
	 *
	 * @en Change the model
	 * @de Modell ändern
	 * @tooltip Privileged: create and change modules, types, attributes, enumerations, annotations and
	 *          layouts. That is the shape the access policy itself is written against.
	 */
	public static ResKey GROUP_MODEL_STRUCTURE;

	/**
	 * Label of the {@link ToolGroups#AGENT_AUTHORING} tool group.
	 *
	 * @en Author agents
	 * @de Agenten erstellen
	 * @tooltip Privileged: create and rewire agents, skills and TL-Script tools. The data model is a
	 *          different group, model.structure.
	 */
	public static ResKey GROUP_AGENT_AUTHORING;

	/**
	 * Label of the {@link ToolGroups#AGENT_META} tool group.
	 *
	 * @en Inspect agents
	 * @de Agenten einsehen
	 * @tooltip List which agents, skills and tools the application has, read-only.
	 */
	public static ResKey GROUP_AGENT_META;

	/**
	 * Label of the {@link ToolGroups#INTERACTION} tool group.
	 *
	 * @en Converse
	 * @de Interagieren
	 * @tooltip Ask the user a question, load a skill, delegate to a sub-agent. An agent without it cannot
	 *          even ask what it should do.
	 */
	public static ResKey GROUP_INTERACTION;

	/**
	 * Label of the {@link ToolGroups#INTROSPECTION} tool group.
	 *
	 * @en Inspect itself
	 * @de Sich selbst einsehen
	 * @tooltip List the tools and skills this agent itself carries, and why one of them is unusable for
	 *          the acting account. Granted implicitly, so an agent can always say what it carries.
	 */
	public static ResKey GROUP_INTROSPECTION;

	/**
	 * Label of the {@link ToolGroups#WORKSPACE} tool group.
	 *
	 * @en Use the workspace
	 * @de Arbeitsbereich nutzen
	 * @tooltip The acting user's own tasks and published pages, scoped to that user.
	 */
	public static ResKey GROUP_WORKSPACE;

	/**
	 * Label of the {@link ToolGroups#EGRESS} tool group.
	 *
	 * @en Send data out
	 * @de Daten nach außen senden
	 * @tooltip Reach outside the application: web fetch and search, issue trackers, remote MCP servers.
	 *          This is how data leaves the application.
	 */
	public static ResKey GROUP_EGRESS;

	static {
		initConstants(I18NConstants.class);
	}
}
