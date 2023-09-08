/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
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
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;

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
				try (InputStream in = data.getStream()) {
					updateCollaborationInTransaction(origCollaboration, new StreamSource(in),
						form.isUpdateBPMLExtensions());
				} catch (IOException | XMLStreamException | KnowledgeBaseException ex) {
					throw new TopLogicException(I18NConstants.ERROR_IMPORT_FAILED, ex)
						.initDetails(ResKey.text(ex.getMessage()));
				}

				component.closeDialog();
			}
		};
	}

	/**
	 * Calls {@link #updateCollaboration(Collaboration, Source, boolean)} in a separate transaction.
	 * 
	 * @param updateExtensions
	 *        Whether the BPML extensions of the original collaboration must be updated with the
	 *        extensions from the new one.
	 */
	public static void updateCollaborationInTransaction(Collaboration collaboration, Source source,
			boolean updateExtensions) throws XMLStreamException {
		KnowledgeBase kb = collaboration.tKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			updateCollaboration(collaboration, source, updateExtensions);
			tx.commit();
		}
	}

	/**
	 * Updates the given {@link Collaboration} so that it is structurally equivalent to the BPML
	 * given as the source attribute.
	 *
	 * @param collaboration
	 *        The {@link Collaboration} to update.
	 * @param source
	 *        The BPML source.
	 * @param updateExtensions
	 *        Whether the BPML extensions of the original collaboration must be updated with the
	 *        extensions from the new one.
	 */
	public static void updateCollaboration(Collaboration collaboration, Source source, boolean updateExtensions)
			throws XMLStreamException {
		ModelBinding binding =
			new ApplicationModelBinding(collaboration.tKnowledgeBase(), ModelService.getApplicationModel());
		Collaboration newCollaboration = BPMLUploadCommand.importBPML(source, binding);
		new Updater(collaboration, newCollaboration, updateExtensions).update();
	}

}
