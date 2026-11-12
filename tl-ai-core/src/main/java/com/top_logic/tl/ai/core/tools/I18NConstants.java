/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * The commit messages the tools of this module record their calls with.
 *
 * <p>
 * One message per kind of change, filled from the call's arguments.
 * </p>
 *
 * @see com.top_logic.tl.ai.tool.api.Tool#commitMessage(java.util.Map,
 *      com.top_logic.tl.ai.tool.api.ToolExecutionContext)
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Commit message of an agent tool call: object updated.
	 *
	 * @en Object ''{0}'' edited
	 * @de Objekt „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.model.ObjectEditTool#updateObject
	 */
	public static ResKey1 OBJECT_UPDATED__OBJECT;

	/**
	 * Commit message of an agent tool call: object created, when no name is given.
	 *
	 * @en {0} created
	 * @de {0} angelegt
	 * @see com.top_logic.tl.ai.core.tools.model.ObjectEditTool#createObject
	 */
	public static ResKey1 OBJECT_CREATED__TYPE;

	/**
	 * Commit message of an agent tool call: object created, with the name given to the new object.
	 *
	 * @en ''{1}'' ({0}) created
	 * @de „{1}“ ({0}) angelegt
	 * @see com.top_logic.tl.ai.core.tools.model.ObjectEditTool#createObject
	 */
	public static ResKey2 OBJECT_CREATED__TYPE_NAME;

	/**
	 * Commit message of an agent tool call: object deleted.
	 *
	 * @en ''{0}'' deleted
	 * @de „{0}“ gelöscht
	 * @see com.top_logic.tl.ai.core.tools.model.ObjectEditTool#deleteObject
	 */
	public static ResKey1 OBJECT_DELETED__OBJECT;

	/**
	 * Commit message of an agent tool call: text edited.
	 *
	 * @en ''{0}'' of ''{1}'' edited
	 * @de „{0}“ von „{1}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.text.TextEditTool#editText
	 */
	public static ResKey2 TEXT_EDITED__ATTRIBUTE_OBJECT;

	/**
	 * Commit message of an agent tool call: data changed by script, which cannot say what its script
	 * changed.
	 *
	 * @en Data changed by TL-Script
	 * @de Daten per TL-Script geändert
	 * @see com.top_logic.tl.ai.core.tools.script.ChangeDataTool#changeData
	 */
	public static ResKey DATA_CHANGED_BY_SCRIPT;

	/**
	 * Commit message of an agent tool call: skill created.
	 *
	 * @en Skill ''{0}'' created
	 * @de Fähigkeit „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool#createSkill
	 */
	public static ResKey1 SKILL_CREATED__NAME;

	/**
	 * Commit message of an agent tool call: skill updated.
	 *
	 * @en Skill ''{0}'' edited
	 * @de Fähigkeit „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool#updateSkill
	 */
	public static ResKey1 SKILL_UPDATED__NAME;

	/**
	 * Commit message of an agent tool call: skill deleted.
	 *
	 * @en Skill ''{0}'' deleted
	 * @de Fähigkeit „{0}“ gelöscht
	 * @see com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool#deleteSkill
	 */
	public static ResKey1 SKILL_DELETED__NAME;

	/**
	 * Commit message of an agent tool call: agent created.
	 *
	 * @en Agent ''{0}'' created
	 * @de Agent „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool#createAgent
	 */
	public static ResKey1 AGENT_CREATED__NAME;

	/**
	 * Commit message of an agent tool call: agent updated.
	 *
	 * @en Agent ''{0}'' edited
	 * @de Agent „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool#updateAgent
	 */
	public static ResKey1 AGENT_UPDATED__NAME;

	/**
	 * Commit message of an agent tool call: agent deleted.
	 *
	 * @en Agent ''{0}'' deleted
	 * @de Agent „{0}“ gelöscht
	 * @see com.top_logic.tl.ai.core.tools.authoring.AgentAuthoringTool#deleteAgent
	 */
	public static ResKey1 AGENT_DELETED__NAME;

	/**
	 * Commit message of an agent tool call: tool added to agent.
	 *
	 * @en Tool ''{0}'' added to agent ''{1}''
	 * @de Werkzeug „{0}“ zu Agent „{1}“ hinzugefügt
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#addToolToAgent
	 */
	public static ResKey2 TOOL_ADDED_TO_AGENT__TOOL_AGENT;

	/**
	 * Commit message of an agent tool call: tool removed from agent.
	 *
	 * @en Tool ''{0}'' removed from agent ''{1}''
	 * @de Werkzeug „{0}“ aus Agent „{1}“ entfernt
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#removeToolFromAgent
	 */
	public static ResKey2 TOOL_REMOVED_FROM_AGENT__TOOL_AGENT;

	/**
	 * Commit message of an agent tool call: skill added to agent.
	 *
	 * @en Skill ''{0}'' added to agent ''{1}''
	 * @de Fähigkeit „{0}“ zu Agent „{1}“ hinzugefügt
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#addSkillToAgent
	 */
	public static ResKey2 SKILL_ADDED_TO_AGENT__SKILL_AGENT;

	/**
	 * Commit message of an agent tool call: skill removed from agent.
	 *
	 * @en Skill ''{0}'' removed from agent ''{1}''
	 * @de Fähigkeit „{0}“ aus Agent „{1}“ entfernt
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#removeSkillFromAgent
	 */
	public static ResKey2 SKILL_REMOVED_FROM_AGENT__SKILL_AGENT;

	/**
	 * Commit message of an agent tool call: delegation added to agent.
	 *
	 * @en Agent ''{1}'' may delegate to ''{0}''
	 * @de Agent „{1}“ darf an „{0}“ delegieren
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#addDelegationToAgent
	 */
	public static ResKey2 DELEGATION_ADDED_TO_AGENT__DELEGATE_AGENT;

	/**
	 * Commit message of an agent tool call: delegation removed from agent.
	 *
	 * @en Agent ''{1}'' no longer delegates to ''{0}''
	 * @de Agent „{1}“ delegiert nicht mehr an „{0}“
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#removeDelegationFromAgent
	 */
	public static ResKey2 DELEGATION_REMOVED_FROM_AGENT__DELEGATE_AGENT;

	/**
	 * Commit message of an agent tool call: tool added to skill.
	 *
	 * @en Tool ''{0}'' added to skill ''{1}''
	 * @de Werkzeug „{0}“ zu Fähigkeit „{1}“ hinzugefügt
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#addToolToSkill
	 */
	public static ResKey2 TOOL_ADDED_TO_SKILL__TOOL_SKILL;

	/**
	 * Commit message of an agent tool call: tool removed from skill.
	 *
	 * @en Tool ''{0}'' removed from skill ''{1}''
	 * @de Werkzeug „{0}“ aus Fähigkeit „{1}“ entfernt
	 * @see com.top_logic.tl.ai.core.tools.authoring.FrameworkWiringTool#removeToolFromSkill
	 */
	public static ResKey2 TOOL_REMOVED_FROM_SKILL__TOOL_SKILL;

	/**
	 * Commit message of an agent tool call: script tool created.
	 *
	 * @en TL-Script tool ''{0}'' created
	 * @de TL-Script-Werkzeug „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.authoring.ScriptToolAuthoringTool#createScriptTool
	 */
	public static ResKey1 SCRIPT_TOOL_CREATED__NAME;

	/**
	 * Commit message of an agent tool call: script tool updated.
	 *
	 * @en TL-Script tool ''{0}'' edited
	 * @de TL-Script-Werkzeug „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.authoring.ScriptToolAuthoringTool#updateScriptTool
	 */
	public static ResKey1 SCRIPT_TOOL_UPDATED__NAME;

	/**
	 * Commit message of an agent tool call: script tool deleted.
	 *
	 * @en TL-Script tool ''{0}'' deleted
	 * @de TL-Script-Werkzeug „{0}“ gelöscht
	 * @see com.top_logic.tl.ai.core.tools.authoring.ScriptToolAuthoringTool#deleteScriptTool
	 */
	public static ResKey1 SCRIPT_TOOL_DELETED__NAME;

	/**
	 * Commit message of an agent tool call: layout created.
	 *
	 * @en Layout ''{0}'' created
	 * @de Layout „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.config.LayoutTemplateTool#createLayout
	 */
	public static ResKey1 LAYOUT_CREATED__LAYOUT;

	/**
	 * Commit message of an agent tool call: application context saved.
	 *
	 * @en Application context saved
	 * @de Anwendungskontext gespeichert
	 * @see com.top_logic.tl.ai.core.tools.context.ApplicationContextTool#saveApplicationContext
	 */
	public static ResKey APPLICATION_CONTEXT_SAVED;

	/**
	 * Commit message of an agent tool call: memory saved, the kind of the memory, not its text,
	 * which is the user's private matter.
	 *
	 * @en Memory of kind {0} kept
	 * @de Erinnerung der Art {0} gespeichert
	 * @see com.top_logic.tl.ai.core.tools.memory.UserMemoryTool#remember
	 */
	public static ResKey1 MEMORY_SAVED__KIND;

	/**
	 * Commit message of an agent tool call: annotation added.
	 *
	 * @en Annotation {0} added to ''{1}''
	 * @de Annotation {0} zu „{1}“ hinzugefügt
	 * @see com.top_logic.tl.ai.core.tools.model.ModelAnnotationEditorTool#addAnnotation
	 */
	public static ResKey2 ANNOTATION_ADDED__ANNOTATION_TARGET;

	/**
	 * Commit message of an agent tool call: annotation removed.
	 *
	 * @en Annotation {0} removed from ''{1}''
	 * @de Annotation {0} von „{1}“ entfernt
	 * @see com.top_logic.tl.ai.core.tools.model.ModelAnnotationEditorTool#removeAnnotation
	 */
	public static ResKey2 ANNOTATION_REMOVED__ANNOTATION_TARGET;

	/**
	 * Commit message of an agent tool call: module created.
	 *
	 * @en Module ''{0}'' created
	 * @de Modul „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#createModule
	 */
	public static ResKey1 MODULE_CREATED__MODULE;

	/**
	 * Commit message of an agent tool call: module updated.
	 *
	 * @en Module ''{0}'' edited
	 * @de Modul „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#updateModule
	 */
	public static ResKey1 MODULE_UPDATED__MODULE;

	/**
	 * Commit message of an agent tool call: class created.
	 *
	 * @en Type ''{0}'' created
	 * @de Typ „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#createClass
	 */
	public static ResKey1 CLASS_CREATED__CLASS;

	/**
	 * Commit message of an agent tool call: class updated.
	 *
	 * @en Type ''{0}'' edited
	 * @de Typ „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#updateClass
	 */
	public static ResKey1 CLASS_UPDATED__CLASS;

	/**
	 * Commit message of an agent tool call: property created.
	 *
	 * @en Attribute ''{0}'' created
	 * @de Attribut „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#createProperty
	 */
	public static ResKey1 PROPERTY_CREATED__PROPERTY;

	/**
	 * Commit message of an agent tool call: property updated.
	 *
	 * @en Attribute ''{0}'' edited
	 * @de Attribut „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#updateProperty
	 */
	public static ResKey1 PROPERTY_UPDATED__PROPERTY;

	/**
	 * Commit message of an agent tool call: reference created.
	 *
	 * @en Reference ''{0}'' created
	 * @de Referenz „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#createReference
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#createBackwardReference
	 */
	public static ResKey1 REFERENCE_CREATED__REFERENCE;

	/**
	 * Commit message of an agent tool call: enumeration created.
	 *
	 * @en Enumeration ''{0}'' created
	 * @de Aufzählung „{0}“ angelegt
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#createEnumeration
	 */
	public static ResKey1 ENUMERATION_CREATED__ENUMERATION;

	/**
	 * Commit message of an agent tool call: enumeration updated.
	 *
	 * @en Enumeration ''{0}'' edited
	 * @de Aufzählung „{0}“ bearbeitet
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#updateEnumeration
	 */
	public static ResKey1 ENUMERATION_UPDATED__ENUMERATION;

	/**
	 * Commit message of an agent tool call: model element deleted.
	 *
	 * @en Model element ''{0}'' deleted
	 * @de Modellelement „{0}“ gelöscht
	 * @see com.top_logic.tl.ai.core.tools.model.ModelEditorTool#deleteModelElement
	 */
	public static ResKey1 MODEL_ELEMENT_DELETED__NAME;

	/**
	 * Commit message of an agent tool call: page published.
	 *
	 * @en Page ''{0}'' published
	 * @de Seite „{0}“ veröffentlicht
	 * @see com.top_logic.tl.ai.core.tools.util.PublishHtmlPageTool#publishPage
	 */
	public static ResKey1 PAGE_PUBLISHED__TITLE;

	static {
		initConstants(I18NConstants.class);
	}
}
