/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.layoutRenderer;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.WindowControl;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolbarControl;
import com.top_logic.mig.html.layout.DivLayoutUtils;

/**
 * The class {@link WindowRenderer} is used to render the content of a {@link WindowControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WindowRenderer extends LayoutControlRenderer<WindowControl<?>> {

	private static final String BOTTOM_SPACER = "bottomSpacer";

	private static final String BOTTOM_SPACER_LEFT = BOTTOM_SPACER + "Left";

	private static final String BOTTOM_SPACER_RIGHT = BOTTOM_SPACER + "Right";

	private static final String RIGHT_SPACER = "rightSpacer";

	private static final String LEFT_SPACER = "leftSpacer";

	private static final String TOP_SPACER = "topSpacer";

	private static final String TOP_SPACER_LEFT = TOP_SPACER + "Left";

	private static final String TOP_SPACER_LEFT_TEXT = TOP_SPACER + "LeftText";

	private static final String TOP_SPACER_RIGHT = TOP_SPACER + "Right";

	private static final String TOP_SPACER_RIGHT_IMGAGE = TOP_SPACER + "RightImg";

	/**
	 * Singleton {@link WindowRenderer} instance.
	 */
	public static final WindowRenderer INSTANCE = new WindowRenderer();

	private WindowRenderer() {
		// Singleton constructor.
	}

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, WindowControl<?> control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);

		WindowControl<?> windowControl = control;
		windowControl.writeLayoutSizeAttribute(out);
		writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
	}

	@Override
	public void writeControlContents(DisplayContext context, TagWriter out, WindowControl<?> windowControl)
			throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(STYLE_ATTR, "overflow:hidden;");
		writeLayoutConstraintInformation(windowControl.getTopSize(), DisplayUnit.PIXEL, out);
		out.endBeginTag();
		{
			writeWindowBarContent(context, out, windowControl);
		}
		out.endTag(DIV);

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, windowControl.getID() + DivLayoutUtils.OUTER_CONTAINER_SUFFIX);
		out.writeAttribute(STYLE_ATTR, "overflow:hidden;");
		writeLayoutConstraintInformation(100, DisplayUnit.PERCENT, out);
		writeLayoutInformationAttribute(Orientation.HORIZONTAL, 100, out);
		out.endBeginTag();
		{
			// write left
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, windowControl.getID() + DivLayoutUtils.LEFT_SUFFIX);
			out.writeAttribute(STYLE_ATTR, "overflow:hidden;");
			out.writeAttribute(CLASS_ATTR, LEFT_SPACER);
			/*
			 * Dont't use theArg.getTagWriter().endEmptyTag() since the browser will not compute the
			 * DOM tree correctly.
			 */
			writeLayoutConstraintInformation(windowControl.getLeftSize(), DisplayUnit.PIXEL, out);
			out.endBeginTag();
			out.endTag(DIV);

			// write center
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, windowControl.getID() + DivLayoutUtils.INNER_CONTAINER_SUFFIX);
			out.writeAttribute(STYLE_ATTR, "overflow:hidden;");
			writeLayoutConstraintInformation(100, DisplayUnit.PERCENT, out);
			writeLayoutInformationAttribute(Orientation.VERTICAL, 100, out);
			out.endBeginTag();
			{
				windowControl.getChildControl().write(context, out);
			}
			out.endTag(DIV);

			// write right
			out.beginBeginTag(DIV);
			out.writeAttribute(ID_ATTR, windowControl.getID() + DivLayoutUtils.RIGHT_SUFFIX);
			out.writeAttribute(STYLE_ATTR, "overflow:hidden;");
			out.writeAttribute(CLASS_ATTR, RIGHT_SPACER);
			/*
			 * Dont't use theArg.getTagWriter().endEmptyTag() since the browser will not compute the
			 * DOM tree correctly.
			 */
			writeLayoutConstraintInformation(windowControl.getRightSize(), DisplayUnit.PIXEL, out);
			out.endBeginTag();
			out.endTag(DIV);

		}
		out.endTag(DIV);

		// write bottom
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, windowControl.getID() + DivLayoutUtils.BOTTOM_SUFFIX);
		out.writeAttribute(STYLE_ATTR, "overflow:hidden;");
		out.writeAttribute(CLASS_ATTR, BOTTOM_SPACER);
		writeLayoutConstraintInformation(windowControl.getBottomSize(), DisplayUnit.PIXEL, out);
		out.endBeginTag();
		{
			out.beginBeginTag(TABLE);
			out.writeAttribute(CLASS_ATTR, BOTTOM_SPACER);
			out.writeAttribute(WIDTH_ATTR, "100%");
			out.writeAttribute(BORDER_ATTR, "0");
			out.endBeginTag();
			{
				out.beginBeginTag(TR);
				out.writeAttribute(CLASS_ATTR, BOTTOM_SPACER);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, BOTTOM_SPACER_LEFT);
					out.endBeginTag();
					out.endTag(TD);

					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, BOTTOM_SPACER);
					out.endBeginTag();
					out.writeText(NBSP);
					out.endTag(TD);

					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, BOTTOM_SPACER_RIGHT);
					out.endBeginTag();
					out.endTag(TD);

				}
				out.endTag(TR);
			}
			out.endTag(TABLE);
		}
		out.endTag(DIV);
	}

	protected void writeWindowBarContent(DisplayContext context, TagWriter out, WindowControl<?> windowControl)
			throws IOException {
		out.beginBeginTag(TABLE);
		out.writeAttribute(CLASS_ATTR, TOP_SPACER);
		out.endBeginTag();

		out.beginBeginTag(TR);
		out.writeAttribute(CLASS_ATTR, TOP_SPACER);
		out.endBeginTag();
		{
			out.beginBeginTag(TD);
			out.writeAttribute(CLASS_ATTR, TOP_SPACER_LEFT);
			out.endBeginTag();
			out.writeText(NBSP);
			out.endTag(TD);

			{
				if (!windowControl.isTitleBarShown()) {
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, TOP_SPACER);
					out.endBeginTag();
					out.writeText(NBSP);
					out.endTag(TD);

				} else {
					HTMLFragment title = windowControl.getTitle();
					if (title != null) {
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, TOP_SPACER_LEFT_TEXT);
						out.endBeginTag();
						// out.beginTag(SPAN);
						title.write(context, out);
						// out.endTag(SPAN);
						out.endTag(TD);
					}

					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, TOP_SPACER);
					out.endBeginTag();
					out.writeText(NBSP);
					out.endTag(TD);

					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, TOP_SPACER_RIGHT_IMGAGE);
					out.endBeginTag();
					{
						ToolBar toolbar = windowControl.getWindowModel().getToolbar();
						new ToolbarControl(toolbar).write(context, out);
					}
					out.endTag(TD);
				}
			}

			out.beginBeginTag(TD);
			out.writeAttribute(CLASS_ATTR, TOP_SPACER_RIGHT);
			out.endBeginTag();
			out.writeText(NBSP);
			out.endTag(TD);
		}
		out.endTag(TR);
		out.endTag(TABLE);
	}

}
