/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.timer.TimerControl;
import com.top_logic.layout.structure.BrowserWindowControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.HTMLUtil;

/**
 * The class {@link BrowserWindowRenderer} is used to render the content of a
 * {@link BrowserWindowRenderer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BrowserWindowRenderer extends LayoutControlRenderer<BrowserWindowControl> {

	/**
	 * The default {@link BrowserWindowControl} renderer.
	 */
	public static final ControlRenderer<BrowserWindowControl> INSTANCE = createRenderer();

	private static final String APPLICATION_CONTENT_ID = "applicationContent";

	/**
	 * Creates a {@link BrowserWindowRenderer}.
	 */
	/* package protected */ BrowserWindowRenderer() {
		super();
	}

	/**
	 * Creates a {@link Renderer} which uses the given css class as class attribute for the tag of
	 * the control.
	 */
	private static ControlRenderer<BrowserWindowControl> createRenderer() {
		/* must use a wrapper for the BrowserWindowRenderer to ensure that the whole content is
		 * written (including tag for the Control) before starting resizing the
		 * browserWindowControl */
		return new ControlRenderer<>() {

			private ControlRenderer<BrowserWindowControl> internalRenderer = new BrowserWindowRenderer();

			@Override
			public void write(DisplayContext context, TagWriter out, BrowserWindowControl value) throws IOException {
				internalRenderer.write(context, out, value);

				HTMLUtil.beginScriptAfterRendering(out);
				value.writeRenderingCommand(context, out);
				HTMLUtil.endScriptAfterRendering(out);
			}

			@Override
			public void appendControlCSSClasses(Appendable out, BrowserWindowControl control) throws IOException {
				internalRenderer.appendControlCSSClasses(out, control);
			}

		};
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, BrowserWindowControl control)
			throws IOException {
		BrowserWindowControl browserWindowControl = control;

		// It is necessary for the FF to write the history frame before the
		// actual content is written as otherwise that iframe will not be turned
		// back.
		browserWindowControl.writeHistoryFrame(context, out);

		// Create marker node for tabbing cycle
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, APPLICATION_CONTENT_ID);

		// it is necessary to have overflow:hidden which is done by this class.
//		writeCssClass(out, browserWindowControl);
		out.writeAttribute(TL_TAB_ROOT, true);
		browserWindowControl.writeLayoutSizeAttribute(out);
		writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
		out.endBeginTag();
		{
			// Write application content
			browserWindowControl.writeChildControl(context, out);
		}
		out.endTag(DIV);

		browserWindowControl.writeDialogs(context, out);
		
		// Write popup dialog pane 
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, BrowserWindowControl.POPUP_DIALOG_PANE);
		out.writeAttribute(CLASS_ATTR, BrowserWindowControl.NO_POPUP_DIALOG_OPENED_CLASS);
		out.writeAttribute(ONCLICK_ATTR, "services.form.BrowserWindowControl.closeAllPopupDialogs();");		
		out.endBeginTag();
		out.endTag(DIV);
		
		// Write anchor for popup dialogs.
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, BrowserWindowControl.POPUP_DIALOG_ANCHOR);
		out.endBeginTag();
		out.endTag(DIV);
		
		// Write pane to temporarily stop user interaction.
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, "waitPane");
		out.endBeginTag();
		out.endTag(DIV);

		TimerControl timer = browserWindowControl.getTimerControl();
		if (timer != null) {
			timer.write(context, out);
		}
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, BrowserWindowControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);
		BrowserWindowControl browserWindowControl = control;
		browserWindowControl.writeLayoutSizeAttribute(out);
		writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
	}

	@Override
	public void appendControlCSSClasses(Appendable out, BrowserWindowControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, Icons.MAINLAYOUT_CSS_CLASS.get());
	}
}
