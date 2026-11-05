/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.upload;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.BPMLDefinitions;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} importing a BPML file.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMLUpdateCommand extends PreconditionCommandHandler {

	/**
	 * Configuration options for {@link BPMLUpdateCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {
		/**
		 * The model to update.
		 */
		ModelSpec getModel();
	}

	private final ChannelLinking _model;

	/**
	 * Creates a {@link BPMLUpdateCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BPMLUpdateCommand(InstantiationContext context, Config config) {
		super(context, config);
		_model = context.getInstance(config.getModel());
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object modelx, Map<String, Object> arguments) {
		UpdateForm form = (UpdateForm) EditorFactory.getModel((FormHandler) component);
		BinaryData data = form.getData();
		if (data == null) {
			return new Failure(I18NConstants.ERROR_NO_DATA);
		}
		
		Object baseModel = ChannelLinking.eval(component, _model);
		assert baseModel instanceof Collaboration : "Import did not result in a collaboration: " + baseModel;

		Collaboration origCollaboration = (Collaboration) baseModel;

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				KnowledgeBase kb = origCollaboration.tKnowledgeBase();
				try (Transaction tx = kb.beginTransaction(I18NConstants.UPDATED_WORKFLOW)) {
					BPMLDefinitions.updateCollaboration(origCollaboration, data, form.isUpdateBPMLExtensions());
					tx.commit();
				} catch (KnowledgeBaseException ex) {
					throw new TopLogicException(
						com.top_logic.bpe.execution.engine.I18NConstants.ERROR_IMPORT_FAILED__DETAILS
							.fill(ex.getMessage()),
						ex).initDetails(ResKey.text(ex.getMessage()));
				}

				component.closeDialog();
			}
		};
	}

}
