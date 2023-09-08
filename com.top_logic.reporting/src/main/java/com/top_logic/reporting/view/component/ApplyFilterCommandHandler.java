/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.component.UpdateCommandHandler;
import com.top_logic.layout.messagebox.MessageArea;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.view.component.progress.ApplyFilterProgressCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:olb@top-logic.com">olb</a>
 */
public class ApplyFilterCommandHandler extends UpdateCommandHandler {

	/** The unique ID of this command. */
	public final static String COMMAND_ID = "applyFilterCommand";

	/**
	 * Creates a new {@link ApplyFilterCommandHandler}.
	 */
	public ApplyFilterCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(final DisplayContext aContext, LayoutComponent formular, Object model, Map<String, Object> someArguments) {
		super.handleCommand(aContext, formular, model, someArguments);

			if (formular instanceof AbstractProgressFilterComponent) {
				final AbstractProgressFilterComponent<?> comp = (AbstractProgressFilterComponent<?>) formular;
				final int nbrOrdersInSearch = comp.getSearchBaseCount();

				// Check if the progress dialog needs to be displayed.
				if (comp.needProgressDialog(nbrOrdersInSearch)) {
				LayoutData layout = new DefaultLayoutData(dim(500, PIXEL), 100, dim(210, PIXEL), 100, Scrolling.AUTO);
			        DisplayValue title       = new ResourceText(I18NConstants.TITLE_KEY);
					MessageArea  contentMess = new MessageArea(MessageType.CONFIRM.getTypeImage()) {

						@SuppressWarnings("deprecation")
						@Override
						protected void writeMessage(DisplayContext context, TagWriter out) throws IOException {
							Resources theRes = aContext.getResources();
							String theMessage = theRes.getMessage(I18NConstants.CONTENT_KEY, nbrOrdersInSearch);

							out.beginTag(HTMLConstants.TABLE);
							out.beginTag(HTMLConstants.TR);
							out.beginTag(HTMLConstants.TD);
							out.beginTag(HTMLConstants.PARAGRAPH);
							out.writeContent(theMessage);
							out.endTag(HTMLConstants.PARAGRAPH);
							out.endTag(HTMLConstants.TD);
							out.endTag(HTMLConstants.TR);
							out.endTag(HTMLConstants.TABLE);
						}
					};

					return MessageBox.confirm(aContext, layout, true, title, contentMess,
			       		 MessageBox.button(ButtonType.YES, new Command() {
							@Override
							public HandlerResult executeCommand(DisplayContext anInnerContext) {
							return ApplyFilterCommandHandler.this.doActualFiltering(anInnerContext, comp);
			                    }
			                   }),  MessageBox.button(ButtonType.NO));
				}
				else {
					return doActualFiltering(aContext, comp);
				}
			} 
			else {
				throw new IllegalArgumentException("This component type is not supported. Must be an implementation of "
						+ AbstractProgressFilterComponent.class.getName());
			}
	}

	HandlerResult doActualFiltering(DisplayContext context, AbstractProgressFilterComponent<?> filterComponent) {
		Map<String, Object> parameters = prepareParametersForCommandHandler(filterComponent);
		ApplyFilterProgressCommandHandler applyFilterHandler = filterComponent.getApplyFilterProgressCommandHandler();
		return CommandHandlerUtil.handleCommand(applyFilterHandler, context, filterComponent, parameters);
	}

	/**
	 * Prepare some parameters for the inner command handler.
	 * 
	 * @param component
	 *        The component calling this command.
	 * @return The requested map, never <code>null</code>.
	 */
	public static Map<String, Object> prepareParametersForCommandHandler(AbstractProgressFilterComponent<?> component) {
		HashMap<String, Object> parameters = new HashMap<>();
		
		parameters.put(ApplyFilterProgressCommandHandler.PARAMETER_NAME_FILTER, component.getFilter());
		
		return parameters;
	}

}
