/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputRefFormat;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.SingleInput;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * {@link UIElement} showing the BPMN diagram of a workflow definition (a {@link Collaboration}),
 * drawn with bpmn-js, and editing it where the view allows it.
 *
 * <p>
 * The element shows the collaboration of its {@link Config#getInput() input} channel and follows
 * that collaboration being changed in the knowledge base: the diagram is then drawn anew from the
 * model. The element of the diagram the user clicks is written to the
 * {@link Config#getSelection() selection} channel, and an element written there by someone else is
 * shown selected.
 * </p>
 *
 * <p>
 * An {@link Config#getEditable() editable} diagram is shown in the bpmn-js modeler. Every change
 * the user makes is published as the BPMN XML of the whole diagram on the
 * {@link Config#getDiagram() diagram} channel, which holds <code>null</code> while the diagram shows
 * the model as it is stored. The element does not store a change itself: a view saves the diagram
 * with an action chain calling <code>bpmlUpdateFromDiagram($workflow, $diagram)</code> inside a
 * <code>&lt;with-transaction&gt;</code>. The update changes the collaboration in the knowledge base,
 * upon which the element draws the stored model and resets the diagram channel. Writing
 * <code>null</code> to the diagram channel from outside discards the changes.
 * </p>
 *
 * <p>
 * While a change is not saved, the element blocks the write of its input channel like a form with
 * unsaved input does: the user is asked whether to discard the change before another collaboration
 * is shown, and so is the user leaving the enclosing dirty-tracked scope. The question offers no
 * "save", because saving is the business of the view's action chain.
 * </p>
 *
 * @see BpmnEditorControl
 */
@InApp
public class BpmnEditorElement implements UIElement {

	/**
	 * Configuration of a {@link BpmnEditorElement}.
	 */
	@TagName(Config.TAG_NAME)
	@TypeIcon("css:bi bi-diagram-3")
	public interface Config extends UIElement.Config, SingleInput {

		/** The tag of the element in a view. */
		String TAG_NAME = "bpmn-editor";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		/** Configuration name for {@link #getDiagram()}. */
		String DIAGRAM = "diagram";

		/** Configuration name for {@link #getEditable()}. */
		String EDITABLE = "editable";

		@Override
		@ClassDefault(BpmnEditorElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The channel holding the workflow definition (<code>tl.bpe.bpml:Collaboration</code>)
		 * whose diagram is shown.
		 *
		 * <p>
		 * Another value than a collaboration shows an empty canvas.
		 * </p>
		 */
		@Override
		@Name(INPUT)
		@Mandatory
		@Format(InputRefFormat.class)
		InputRef getInput();

		/**
		 * Optional channel holding the selected element of the diagram.
		 *
		 * <p>
		 * The element the user clicks is written there as the model element of the workflow (a
		 * task, a gateway, an event, a lane, a sequence flow, ...), a click on the empty canvas as
		 * the collaboration itself. A model element of the shown workflow written there by another
		 * element of the view is shown selected; any other value shows no selection and is left
		 * alone. When another workflow is shown, a selected element of the previous one is
		 * replaced with <code>null</code>.
		 * </p>
		 */
		@Name(SELECTION)
		@Format(ChannelRefFormat.class)
		ChannelRef getSelection();

		/**
		 * Optional channel receiving the changed diagram as BPMN XML.
		 *
		 * <p>
		 * The channel holds <code>null</code> while the diagram shows the stored model, and the
		 * BPMN XML of the whole diagram after the user changed it in an
		 * {@link #getEditable() editable} diagram. A view saves the change with
		 * <code>bpmlUpdateFromDiagram($workflow, $diagram)</code> in a
		 * <code>&lt;with-transaction&gt;</code> of an action chain. The element resets the channel
		 * to <code>null</code> when another workflow is shown or the shown one was changed in the
		 * knowledge base in a way the diagram shows; a change of an application specific extension
		 * of an element (its form, its step view, ...) keeps the change. A <code>null</code>
		 * written from outside discards the change.
		 * </p>
		 */
		@Name(DIAGRAM)
		@Format(ChannelRefFormat.class)
		ChannelRef getDiagram();

		/**
		 * Whether the user may change the diagram.
		 *
		 * <p>
		 * The expression sees the workflow of the {@link #getInput() input} as a variable named
		 * after the channel, or by its <code>as</code> name, and is evaluated again whenever
		 * another workflow is shown or the shown one is changed. An editable diagram is shown in
		 * the bpmn-js modeler with its palette and context pad, any other one in a viewer that can
		 * be moved and zoomed only.
		 * </p>
		 *
		 * <pre>
		 * editable="$workflow.get(`tl.bpe.bpml:Collaboration#state`) == `tl.bpe.bpml:ApprovalState#Development`"
		 * </pre>
		 */
		@Name(EDITABLE)
		@NonNullable
		@FormattedDefault("false")
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getEditable();
	}

	private final Config _config;

	private final QueryExecutor _editable;

	/**
	 * Creates a {@link BpmnEditorElement} from configuration.
	 *
	 * @param context
	 *        The context for instantiating nested configurations.
	 * @param config
	 *        The configuration of the element.
	 */
	@CalledByReflection
	public BpmnEditorElement(InstantiationContext context, Config config) {
		_config = config;
		_editable = InputScript.compile(config, Config.EDITABLE);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = context.resolveChannel(_config.getInput().getChannel());
		ViewChannel selection = resolveOptional(context, _config.getSelection());
		ViewChannel diagram = resolveOptional(context, _config.getDiagram());

		BpmnEditorControl control = new BpmnEditorControl(context, input, selection, diagram,
			collaboration -> Boolean.TRUE.equals(_editable.execute(collaboration)));
		control.setCssClass(_config.getCssClass());
		return control;
	}

	private static ViewChannel resolveOptional(ViewContext context, ChannelRef ref) {
		return ref == null ? null : context.resolveChannel(ref);
	}

}
