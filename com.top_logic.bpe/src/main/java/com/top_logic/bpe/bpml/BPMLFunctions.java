/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions administrating workflow definitions ({@link Collaboration}s).
 *
 * <p>
 * The functions modifying workflows open no transaction of their own. In a view, they are called
 * within a <code>&lt;with-transaction&gt;</code>.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #create(String)} is called <code>bpmlCreate</code>.
 *
 * @see BPMLDefinitions
 */
@ScriptPrefix(BPMLFunctions.PREFIX)
public class BPMLFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "bpml";

	/**
	 * Script name of {@link #importFile(BinaryData)}, whose method name cannot be the Java keyword.
	 */
	public static final String IMPORT_NAME = "import";

	/**
	 * Creates a new workflow with the given name from the default diagram.
	 *
	 * @param name
	 *        The name of the new workflow.
	 * @return The new workflow.
	 */
	public static Collaboration create(@Mandatory String name) {
		return BPMLDefinitions.createCollaboration(name);
	}

	/**
	 * Imports a BPMN file as a new workflow.
	 *
	 * <p>
	 * A workflow without a name in the file is named after the file.
	 * </p>
	 *
	 * @param data
	 *        The BPMN file.
	 * @return The new workflow.
	 */
	@Name(BPMLFunctions.IMPORT_NAME)
	public static Collaboration importFile(@Mandatory BinaryData data) {
		return BPMLDefinitions.importCollaboration(data);
	}

	/**
	 * Updates a workflow from a BPMN file.
	 *
	 * <p>
	 * The elements of the workflow keep their identity when the file contains an element with the
	 * same ID.
	 * </p>
	 *
	 * @param workflow
	 *        The workflow to update.
	 * @param data
	 *        The BPMN file.
	 * @param updateExtensions
	 *        Whether the application specific settings (forms, actors, ...) are taken from the
	 *        file, too. By default, the settings of the workflow are kept.
	 * @return The updated workflow.
	 */
	public static Collaboration update(@Mandatory Collaboration workflow, @Mandatory BinaryData data,
			@BooleanDefault(false) boolean updateExtensions) {
		BPMLDefinitions.updateCollaboration(workflow, data, updateExtensions);
		return workflow;
	}

	/**
	 * Updates a workflow from the BPMN XML of a diagram edited in the browser.
	 *
	 * <p>
	 * The application specific settings of the workflow are kept.
	 * </p>
	 *
	 * @param workflow
	 *        The workflow to update.
	 * @param xml
	 *        The BPMN XML of the diagram.
	 * @return The updated workflow.
	 */
	public static Collaboration updateFromDiagram(@Mandatory Collaboration workflow, @Mandatory String xml) {
		BPMLDefinitions.updateFromDiagram(workflow, xml);
		return workflow;
	}

	/**
	 * Creates an independent copy of a workflow, in development state.
	 *
	 * @param workflow
	 *        The workflow to copy.
	 * @param name
	 *        The name of the copy.
	 * @return The copy.
	 */
	public static Collaboration copy(@Mandatory Collaboration workflow, @Mandatory String name) {
		return BPMLDefinitions.copyCollaboration(workflow, name);
	}

	/**
	 * Exports a workflow as BPMN file, including its application specific settings.
	 *
	 * @param workflow
	 *        The workflow to export.
	 * @param pretty
	 *        Whether the XML is indented.
	 * @return The BPMN file named after the workflow.
	 */
	@SideEffectFree
	public static BinaryData export(@Mandatory Collaboration workflow, @BooleanDefault(true) boolean pretty) {
		return BPMLDefinitions.export(workflow, pretty);
	}

	/**
	 * Whether a workflow can be released: it is in development or retired.
	 *
	 * @param workflow
	 *        The workflow to check.
	 * @return Whether {@link #release(Collaboration)} is allowed.
	 */
	@SideEffectFree
	public static boolean canRelease(@Mandatory Collaboration workflow) {
		return BPMLDefinitions.canRelease(workflow);
	}

	/**
	 * Releases a workflow in development or retired, so that processes can be started with it.
	 *
	 * @param workflow
	 *        The workflow to release.
	 * @return The released workflow.
	 */
	public static Collaboration release(@Mandatory Collaboration workflow) {
		BPMLDefinitions.release(workflow);
		return workflow;
	}

	/**
	 * Whether a workflow can be revoked: it is released.
	 *
	 * @param workflow
	 *        The workflow to check.
	 * @return Whether {@link #revoke(Collaboration)} is allowed.
	 */
	@SideEffectFree
	public static boolean canRevoke(@Mandatory Collaboration workflow) {
		return BPMLDefinitions.canRevoke(workflow);
	}

	/**
	 * Revokes a released workflow, it is retired afterwards.
	 *
	 * @param workflow
	 *        The workflow to revoke.
	 * @return The revoked workflow.
	 */
	public static Collaboration revoke(@Mandatory Collaboration workflow) {
		BPMLDefinitions.revoke(workflow);
		return workflow;
	}

}
