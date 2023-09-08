/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.component;

import java.io.StringReader;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.modeler.display.BPMNDisplay;
import com.top_logic.bpe.modeler.upload.BPMLUpdateCommand;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InEditModeExecutable;

/**
 * {@link CommandHandler} saving the current changes in an {@link BPMLEditor}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplyBPMNStructuralChanges extends AbstractCommandHandler {

	/**
	 * Creates a {@link ApplyBPMNStructuralChanges} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ApplyBPMNStructuralChanges(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		if (!someArguments.containsKey("resume")) {
			BPMNDisplay display = ((BPMLEditor) aComponent).getBPMNDisplay();
			if (display != null) {
				HandlerResult suspend = HandlerResult.suspended();
				Command continuation = suspend.resumeContinuation(Collections.singletonMap("resume", Boolean.TRUE));
				display.storeDiagram((storeContext, xml) -> {
					String currentlySelectedElement = display.selectedGUIId();
					try {
						Collaboration collaboration = display.getModel();
						StreamSource source = new StreamSource(new StringReader(xml));
						BPMLUpdateCommand.updateCollaborationInTransaction(collaboration, source, false);
					} catch (XMLStreamException ex) {
						throw new RuntimeException(ex);
					}
					if (currentlySelectedElement != null) {
						/* When the diagram was changed, it may be that the type of the currently
						 * selected element is changed. In that case, the selection becomes invalid.
						 * Select the element with the GUI identifier as before: This is a no-op
						 * when the currently selected object is not touched, but re-validates the
						 * selection in case the former selection is invalid now. */
						display.notifySelected(currentlySelectedElement);
					}
					continuation.executeCommand(storeContext);
				});
				display.resetChanged();
				return suspend;
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return InEditModeExecutable.INSTANCE;
	}

}
