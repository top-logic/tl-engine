/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.view;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewFunctions;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions about the step view of a task: the view the user working on the task sees,
 * see the attribute <code>tl.bpe.bpml:ManualTask#view</code>.
 *
 * <p>
 * A step view declares the channels {@link #TOKEN_CHANNEL} and {@link #PROCESS_CHANNEL}, which the
 * task screen embedding it binds to the token being worked on and to its process execution.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #defaultStepView(Node)} is called <code>bpeDefaultStepView</code>.
 */
@ScriptPrefix(StepViewFunctions.PREFIX)
public class StepViewFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "bpe";

	/** Name of the channel of a step view holding the token being worked on. */
	public static final String TOKEN_CHANNEL = "token";

	/** Name of the channel of a step view holding the process execution of the token. */
	public static final String PROCESS_CHANNEL = "process";

	private static final String VIEW = "view";

	private static final String CHANNELS = "channels";

	private static final String CHANNEL = "channel";

	private static final String NAME = "name";

	private static final String FORM = "form";

	private static final String INPUT = "input";

	private static final String WITH_INSET = "with-inset";

	private static final String FIELD = "field";

	private static final String ATTRIBUTE = "attribute";

	/**
	 * The step view of a task that has none of its own.
	 *
	 * <p>
	 * The view declares the channels of a step view and shows a form on the process execution
	 * with a field for each attribute the model type of the task's participant declares itself.
	 * The task screen shows it for a task without a step view, and the workflow administration
	 * stores it as the initial step view of a task about to be designed.
	 * </p>
	 *
	 * @param task
	 *        The task node of a workflow.
	 * @return The view configuration, the value of a <code>tl.view:ViewDefinition</code>
	 *         attribute.
	 */
	@Label("Default step view")
	@SideEffectFree
	public static ViewElement.Config defaultStepView(@Mandatory Node task) {
		TagWriter out = new TagWriter();
		out.beginTag(VIEW);
		{
			out.beginTag(CHANNELS);
			channel(out, TOKEN_CHANNEL);
			channel(out, PROCESS_CHANNEL);
			out.endTag(CHANNELS);

			out.beginBeginTag(FORM);
			out.writeAttribute(INPUT, PROCESS_CHANNEL);
			out.writeAttribute(WITH_INSET, true);
			out.endBeginTag();
			TLClass modelType = modelType(task);
			if (modelType != null) {
				for (TLStructuredTypePart part : modelType.getLocalParts()) {
					out.beginBeginTag(FIELD);
					out.writeAttribute(ATTRIBUTE, part.getName());
					out.endEmptyTag();
				}
			}
			out.endTag(FORM);
		}
		out.endTag(VIEW);
		return ViewFunctions.parse(out.toString());
	}

	private static void channel(TagWriter out, String name) {
		out.beginBeginTag(CHANNEL);
		out.writeAttribute(NAME, name);
		out.endEmptyTag();
	}

	private static TLClass modelType(Node task) {
		Process process = task.getProcess();
		if (process == null) {
			return null;
		}
		Participant participant = process.getParticipant();
		if (participant == null) {
			return null;
		}
		return participant.getModelType();
	}

}
