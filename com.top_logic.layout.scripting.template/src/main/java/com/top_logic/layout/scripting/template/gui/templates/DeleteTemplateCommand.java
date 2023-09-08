/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} for deleting a template resource.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteTemplateCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link DeleteTemplateCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteTemplateCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, final LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		final Selectable selectable = (Selectable) component;
		TLTreeNode<?> node = (TLTreeNode<?>) selectable.getSelected();
		final TemplateResource templateResource = (TemplateResource) node.getBusinessObject();


		Command delete = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				try {
					templateResource.delete();
					// Note: For absurd reasons, a component does not receive events sent by itself,
					// therefore, the sender is faked to be the main layout.
					component.fireModelDeletedEvent(templateResource, component.getMainLayout());
				} catch (IOException ex) {
					throw new TopLogicException(
						I18NConstants.ERROR_TEMPLATE_DELETING_FAILED__NAME_ERROR.fill(
							templateResource.getResourceSuffix(), ex.getMessage()), ex);
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		return MessageBox.confirm(aContext, MessageType.CONFIRM,
			aContext.getResources().getString(
				I18NConstants.REALLY_DELETE__NAME.fill(templateResource.getName())),
			dontRecord(MessageBox.button(ButtonType.YES, delete)),
			dontRecord(MessageBox.button(ButtonType.NO)));
	}

	private CommandModel dontRecord(CommandModel button) {
		ScriptingRecorder.annotateAsDontRecord(button);
		return button;
	}

}
