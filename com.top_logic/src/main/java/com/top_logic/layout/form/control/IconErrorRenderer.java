/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * {@link ErrorRenderer} implementation that displays an error message as icon
 * with mouse-over tooltip.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IconErrorRenderer extends AbstractErrorRenderer {

	public static final AbstractErrorRenderer INSTANCE = new IconErrorRenderer();

	/**
	 * Singleton constructor.
	 */
	protected IconErrorRenderer() {
		super();
	}
	
	@Override
	public void writeError(DisplayContext context, TagWriter out, ErrorDisplay error) 
		throws IOException 
	{
		boolean hasError = error.hasError();
		
		String iconID = getIconID(error);
//		String tooltipDisplayID = getTooltipDisplayID(error);

		out.beginBeginTag(SPAN);
		writeControlTagAttributes(context, out, error);
		out.endBeginTag();
		{
			XMLTag icon = getCurrentIcon(hasError).toIcon();
			icon.beginBeginTag(context, out);
			out.writeAttribute(ID_ATTR, iconID);
		
			// The error icon is not hidden, if no error exists but replaced 
			// with a transparent spacer of the same size.
			//
			// writeAttribute(CLASS_ATTR, FormConstants.IS_ERROR_CSS_CLASS);
		
			// TODO BHU: Themes should provide more information about their
			// images. The following is a hard restriction for the images that
			// a theme could provide for a given name.
			out.writeAttribute(WIDTH_ATTR, 8);
			out.writeAttribute(HEIGHT_ATTR, 8);

			StringWriter theWriter = new StringWriter();
			renderErrorMessage(new TagWriter(theWriter), error);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
				ResKey.text(theWriter.toString()));
			
			out.writeAttribute(ALT_ATTR, 
				Resources.getInstance().getString(I18NConstants.ERROR_ICON_ALT));
			icon.endEmptyTag(context, out);
		}
		out.endTag(SPAN);
	}

	/**
	 * Constructs an identifier for the error icon based on the identifier for
	 * the given {@link ErrorDisplay}.
	 */
	private String getIconID(ErrorDisplay error) {
		return error.getID() + "-icon";
	}

	/**
	 * Constructs an identifier for the container element storing the error
	 * message as contents (shown as tooltip).
	 */
	private String getTooltipDisplayID(ErrorDisplay display) {
		return display.getID() + "-value";
	}

	@Override
	public void handleHasErrorPropertyChange(ErrorDisplay display, boolean oldValue, boolean newValue) {
		display.requestRepaint();
	}

	@Override
	public void handleErrorPropertyChange(final ErrorDisplay display) {
		addUpdate(display, 
			new ContentReplacement(
				getTooltipDisplayID(display), 
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						renderErrorMessage(out, display);
					}
				}));
	}
	
	/**
	 * Looks up the icon depending on the has-error state.
	 */
	protected ThemeImage getCurrentIcon(boolean hasError) {
		return hasError ? Icons.ALERT : Icons.ALERT_SPACER;
	}
	
	private static void renderErrorMessage(TagWriter out, ErrorDisplay display) throws IOException {
		if (! display.hasError()) return;
		
		if (display.getErrorCount() == 1) {
			out.writeText(
				(String) TO_ERROR_MESSAGE.map(
					display.getErrorFields().next()));
		} else {
			out.beginTag(UL);
			{
				for (Iterator it = display.getErrorFields(); it.hasNext(); ) {
					FormField field = (FormField) it.next();
					
					out.beginTag(LI);
					out.writeText((String) TO_ERROR_MESSAGE.map(field));
					out.endTag(LI);
				}
			}
			out.endTag(UL);
		}
	}
	
}
