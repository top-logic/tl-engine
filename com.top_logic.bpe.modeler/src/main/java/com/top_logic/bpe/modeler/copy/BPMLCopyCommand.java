/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.copy;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.modeler.copy.CopyFormBuilder.Form;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * {@link CommandHandler} copying a workflow.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMLCopyCommand extends PreconditionCommandHandler implements WithPostCreateActions {

	/**
	 * Configuration options for {@link BPMLCopyCommand}.
	 */
	public interface Config extends PreconditionCommandHandler.Config, WithPostCreateActions.Config {
		// Pure sum interface.
	}

	final List<PostCreateAction> _postCreateActions;

	/**
	 * Creates a {@link BPMLCopyCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BPMLCopyCommand(InstantiationContext context, Config config) {
		super(context, config);
		_postCreateActions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		Form form = (Form) EditorFactory.getModel((FormHandler) component);
		
		if (!(model instanceof Collaboration)) {
			return new Failure(I18NConstants.ERROR_NO_WORKFLOW_SELECTED);
		}

		Collaboration orig = (Collaboration) model;

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
					Collaboration newVersion = DeepCopy.copyDeep(orig);
					newVersion.setName(form.getNewName());
					newVersion.setState(TLModelUtil.findPart("tl.bpe.bpml:ApprovalState#Development"));
					tx.commit();

					_postCreateActions.forEach(action -> action.handleNew(component, newVersion));
				}
				component.closeDialog();
			}
		};
	}

}
