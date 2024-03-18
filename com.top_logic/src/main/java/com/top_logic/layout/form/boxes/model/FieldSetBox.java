/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Box} consisting of a legend and content box.
 * 
 * <p>
 * Constructing a {@link FieldSetBox} requires the following steps:
 * </p>
 * 
 * <ol>
 * <li>Allocating the object using {@link #createFieldSet(boolean, boolean)}.</li>
 * <li>Initializing the legend content using {@link #setLegend(HTMLFragment)}.</li>
 * <li>Initializing the content box using {@link #setContentBox(Box)}.</li>
 * </ol>
 * 
 * @see #createFieldSet(boolean, boolean)
 * @see #setLegend(HTMLFragment)
 * @see #setContentBox(Box)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldSetBox extends ProxyBox implements CollapsedListener {

	private static final String TITLE_LEFT_CSS_CLASS = "frmGroupTitleLeft";

	private static final String TITLE_LEFT_NO_BORDER_CSS_CLASS = "frmGroupTitleLeftNoBorder";

	private static final String TITLE_CONTENT_CSS_CLASS = "frmGroupTitleContent";

	private static final String EXPAND_CSS_CLASS = "frmGroupExpand";

	/**
	 * CSS class for the content part of the legend area.
	 */
	public static final String TITLE_TEXT_CSS_CLASS = "frmGroupTitleText";

	private static final String TITLE_RIGTH_CSS_CLASS = "frmGroupTitleRight";

	private static final String TITLE_RIGTH_NO_BORDER_CSS_CLASS = "frmGroupTitleRightNoBorder";

	private static final String CONTENT_LEFT_CSS_CLASS = "frmGroupContentLeft";

	private static final String CONTENT_RIGHT_CSS_CLASS = "frmGroupContentRight";

	private static final String BOTTOM_LEFT_CSS_CLASS = "frmGroupBottomLeft";

	private static final String BOTTOM_CENTER_CSS_CLASS = "frmGroupBottomCenter";

	private static final String BOTTOM_RIGHT_CSS_CLASS = "frmGroupBottomRight";

	private static final String TITLE_LEFT_COLLAPSED_CSS_CLASS = "frmGroupTitleLeftCollapsed";

	private static final String TITLE_CONTENT_COLLAPSED_CSS_CLASS = "frmGroupTitleContentCollapsed";

	private static final String TITLE_RIGTH_COLLAPSED_CSS_CLASS = "frmGroupTitleRightCollapsed";

	private static final AbstractButtonRenderer<?> TOGGLE_RENDERER =
		ImageButtonRenderer.newSystemButtonRenderer(FormConstants.TOGGLE_BUTTON_CSS_CLASS);

	/**
	 * Creates a {@link FieldSetBox} with legend and border.
	 * 
	 * @see #createFieldSet(boolean, boolean)
	 */
	public static FieldSetBox createFieldSet() {
		return createFieldSet(true, true);
	}

	/**
	 * Creates a {@link CollapsibleBorderBox} with the given settings.
	 * 
	 * @param hasLegend
	 *        Whether a top border box should be allocated.
	 * @param hasBorder
	 *        Whether border boxes should be allocated.
	 * @return The new {@link CollapsibleBorderBox}.
	 */
	public static FieldSetBox createFieldSet(boolean hasLegend, boolean hasBorder) {
		return new FieldSetBox(hasLegend, hasBorder);
	}

	private final ButtonControl _toggle;

	private FieldSetBox(boolean hasLegend, boolean hasBorder) {
		super(new CollapsibleBorderBox());

		initStructure(hasLegend, hasBorder);

		_toggle = createToggleButton();

		applyStyles();
	}

	private void initStructure(boolean hasLegend, boolean hasBorder) {
		BorderBox top = top();

		if (hasLegend) {
			FragmentBox titleBox = Boxes.contentBox();
			titleBox.setCssClass(TITLE_CONTENT_CSS_CLASS);

			// This makes titles of collapsed boxes render incorrectly.
			// titleBox.setWidth(DisplayDimension.ZERO_PERCENT);

			top.setTopBorder(titleBox);
		} else {
			top.setTopBorder(null);
		}

		if (hasLegend && hasBorder) {
			top.setTopLeft(border(TITLE_LEFT_CSS_CLASS));
			top.setTopRight(border(TITLE_RIGTH_CSS_CLASS));

			top.setRightBorder(border(CONTENT_RIGHT_CSS_CLASS));
			top.setLeftBorder(border(CONTENT_LEFT_CSS_CLASS));

			top.setBottomLeft(border(BOTTOM_LEFT_CSS_CLASS));
			top.setBottomBorder(border(BOTTOM_CENTER_CSS_CLASS, DisplayDimension.HUNDERED_PERCENT));
			top.setBottomRigth(border(BOTTOM_RIGHT_CSS_CLASS));
		} else {
			if (hasLegend) {
				top.setTopLeft(border(TITLE_LEFT_NO_BORDER_CSS_CLASS));
				top.setTopRight(border(TITLE_RIGTH_NO_BORDER_CSS_CLASS));
			} else {
				top.setTopLeft(null);
				top.setTopRight(null);
			}

			top.setRightBorder(border(null));
			top.setLeftBorder(border(null));
		}
	}

	private ButtonControl createToggleButton() {
		return new ButtonControl(new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				Collapsible expansionModel = top().getExpansionModel();
				expansionModel.setCollapsed(!expansionModel.isCollapsed());
				return HandlerResult.DEFAULT_RESULT;
			}
		}, TOGGLE_RENDERER);
	}

	private void applyStyles() {
		if (top().getExpansionModel().isCollapsed()) {
			addCssClass(top().getTopLeft(), TITLE_LEFT_COLLAPSED_CSS_CLASS);
			addCssClass(top().getTopBorder(), TITLE_CONTENT_COLLAPSED_CSS_CLASS);
			addCssClass(top().getTopRight(), TITLE_RIGTH_COLLAPSED_CSS_CLASS);
			_toggle.setImage(com.top_logic.layout.form.control.Icons.BOX_COLLAPSED);
		} else {
			removeCssClass(top().getTopLeft(), TITLE_LEFT_COLLAPSED_CSS_CLASS);
			removeCssClass(top().getTopBorder(), TITLE_CONTENT_COLLAPSED_CSS_CLASS);
			removeCssClass(top().getTopRight(), TITLE_RIGTH_COLLAPSED_CSS_CLASS);
			_toggle.setImage(com.top_logic.layout.form.control.Icons.BOX_EXPANDED);
		}
	}

	private static void addCssClass(Box box, String cssClass) {
		if (box != null) {
			ContentBox contentBox = (ContentBox) box;
			String currentCssClass = contentBox.getCssClass();
			if (currentCssClass != null) {
				contentBox.setCssClass(currentCssClass + " " + cssClass);
			}
		}
	}

	private static void removeCssClass(Box box, String cssClass) {
		if (box != null) {
			ContentBox contentBox = (ContentBox) box;
			String currentCssClass = contentBox.getCssClass();
			if (currentCssClass != null && currentCssClass.endsWith(cssClass)) {
				String newCssClass = currentCssClass.substring(0, currentCssClass.length() - (cssClass.length() + 1));
				contentBox.setCssClass(newCssClass);
			}
		}
	}

	@Override
	public void setImpl(Box impl) {
		assert impl != null && getImpl() == null : "The implementation must not be changed.";
		super.setImpl(impl);
	}

	@Override
	public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
		applyStyles();
		return Bubble.BUBBLE;
	}

	private static FragmentBox box(String cssClass) {
		FragmentBox result = Boxes.contentBox();
		result.setCssClass(cssClass);
		return result;
	}

	private static FragmentBox border(String cssClass) {
		return border(cssClass, DisplayDimension.ZERO_PERCENT);
	}

	private static FragmentBox border(String cssClass, DisplayDimension dimension) {
		FragmentBox result = box(cssClass);
		result.setWidth(dimension);
		return result;
	}

	/**
	 * The content area of this fieldset.
	 */
	public Box getContentBox() {
		return top().getCenter();
	}

	/**
	 * @see #getContentBox()
	 */
	public void setContentBox(Box newBox) {
		top().setCenter(newBox);
	}

	/**
	 * Whether this box is visible.
	 * 
	 * <p>
	 * An invisible box is not rendered at all.
	 * </p>
	 */
	public boolean isVisible() {
		return top().isVisible();
	}

	/**
	 * The underlying {@link VisibilityModel}.
	 */
	public VisibilityModel getVisibility() {
		return top().getVisibilityModel();
	}

	/**
	 * Injects a separate {@link VisibilityModel} that controls the visibility of this box.
	 * 
	 * @param visibility
	 *        The {@link VisibilityModel} to observe. <code>null</code> means that a default local
	 *        visibility model is used.
	 * 
	 * @see #getVisibility()
	 */
	public void setVisibility(VisibilityModel visibility) {
		top().setVisibilityModel(visibility);
	}

	/**
	 * Whether only the legend is shown.
	 */
	public boolean isCollapsed() {
		return top().isCollapsed();
	}

	/**
	 * The underlying {@link Collapsible} model providing the {@link #isCollapsed()} state.
	 */
	public Collapsible getExpansionModel() {
		return top().getExpansionModel();
	}

	/**
	 * Injects a separate {@link Collapsible} that controls the expansion state of this box.
	 * 
	 * @param expansionModel
	 *        The {@link Collapsible} to observe. <code>null</code> means that this box is expanded.
	 * 
	 * @see #getExpansionModel()
	 */
	public void setExpansionModel(Collapsible expansionModel) {
		top().setExpansionModel(expansionModel);

		applyStyles();
	}

	final CollapsibleBorderBox top() {
		return (CollapsibleBorderBox) getImpl();
	}

	/**
	 * Installs the given fragment as legend content into collapsible field set.
	 */
	public void setLegend(HTMLFragment legend) {
		setLegend(legend, true);
	}

	/**
	 * Installs the given fragment as legend content.
	 */
	public void setLegend(HTMLFragment legend, boolean collapsible) {
		FragmentBox legendBox = legendBox();
		if (legendBox == null) {
			// There is no legend area.
			return;
		}
		if (collapsible) {
			legendBox.setContentRenderer(concat(span(TITLE_TEXT_CSS_CLASS, legend), span(EXPAND_CSS_CLASS, _toggle)));
		} else {
			legendBox.setContentRenderer(span(TITLE_TEXT_CSS_CLASS, legend));
		}
	}

	private FragmentBox legendBox() {
		return (FragmentBox) top().getTopBorder();
	}

	@Override
	protected void attach() {
		super.attach();

		top().getExpansionModel().addListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	@Override
	protected void detach() {
		super.detach();

		top().getExpansionModel().removeListener(Collapsible.COLLAPSED_PROPERTY, this);
	}
}
