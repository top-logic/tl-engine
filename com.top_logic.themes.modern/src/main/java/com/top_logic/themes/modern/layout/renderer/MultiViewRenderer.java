/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.themes.modern.layout.renderer;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.ConfigurableControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.component.configuration.PopupViewConfiguration;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 * 
 * @deprecated Use {@link PopupViewConfiguration}
 */
@Deprecated
public class MultiViewRenderer
		extends ConfigurableControlRenderer<CompositeControl, ConfigurableControlRenderer.Config<?>> {

	/**
	 * Creates a new {@link MultiViewRenderer}.
	 */
	public MultiViewRenderer(InstantiationContext context, Config<?> atts) {
		super(context, atts);
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, CompositeControl control)
			throws IOException {

		XMLTag tag = Icons.PERSON.toButton();
		tag.beginBeginTag(context, out);

		out.beginAttribute(ONCLICK_ATTR);
		LinkGenerator.renderLink(context, out, openPopupCommand(control));
		out.endAttribute();

		tag.endEmptyTag(context, out);
	}

	private AbstractCommandModel openPopupCommand(CompositeControl control) {
		return new AbstractCommandModel() {

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				FrameScope fc = ((AbstractControlBase) control).getFrameScope();

				PopupDialogModel popupModel = new DefaultPopupDialogModel(DefaultLayoutData.scrollingLayout(0, 0), 1);
				PopupDialogControl popupDialogControl = new PopupDialogControl(fc, popupModel, control.getID());
				popupDialogControl.setContent(popupContent(control));
				fc.getWindowScope().openPopupDialog(popupDialogControl);
				return HandlerResult.DEFAULT_RESULT;
			}

		};
	}

	HTMLFragment popupContent(CompositeControl control) {
		return new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.beginBeginTag(HTMLConstants.UL);
				out.writeAttribute(CLASS_ATTR, "UserHover");
				out.endBeginTag();
				CompositeControl composite = control;
				List<? extends HTMLFragment> children = composite.getChildren();
				for (int index = 0, size = children.size(); index < size; index++) {
					out.beginTag(HTMLConstants.LI);
					{
						children.get(index).write(context, out);
					}
					out.endTag(HTMLConstants.LI);
				}
				out.endTag(HTMLConstants.UL);
			}
		};
	}

	@Override
	public void appendControlCSSClasses(Appendable out, CompositeControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, "User");
	}
}
