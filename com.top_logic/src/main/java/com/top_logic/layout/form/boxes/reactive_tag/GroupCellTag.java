/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.DefaultExpansionModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.tag.BoxContainerTag;
import com.top_logic.layout.form.boxes.tag.JSPLayoutedControls;
import com.top_logic.layout.form.boxes.tag.PersonalizedExpansionModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.tag.ControlBodyTag;
import com.top_logic.layout.form.tag.ControlTagUtil;
import com.top_logic.layout.form.tag.FormContainerTag;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.FormTagUtil;
import com.top_logic.layout.form.tag.LayoutTag;
import com.top_logic.layout.form.tag.util.ExpressionSyntax;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.css.CssUtil;

/**
 * {@link BoxContainerTag} rendering a title row with a border around some content.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GroupCellTag extends AbstractBodyTag implements FormContainerTag, ControlBodyTag {

	/**
	 * The CSS class to add to a {@link HTMLConstants#SPAN} around the title text for correct
	 * display in filter form mode.
	 * 
	 * <p>
	 * The additional span is required to add a solid background with left and right padding. This
	 * is required to render the text above the border in filter form display.
	 * </p>
	 */
	@CalledFromJSP
	public static final String TITLE_TEXT_CSS_CLASS = "header";

	static final String GROUP_CELL_TAG = "form:groupCell";

	private static final boolean DEFAULT_INITILY_COLLAPSED = false;

	private FormContainer _container;

	private String _personalizationName;

	private HTMLFragment _title;

	private String _firstColumnWidth;

	private String _cssClass;

	private boolean _initiallyCollapsed = DEFAULT_INITILY_COLLAPSED;

	private JSPLayoutedControls _jspControl;

	private DefaultGroupSettings _settings;

	/**
	 * The name of the {@link FormContainer} that provides the {@link Collapsible} state.
	 * 
	 * <p>
	 * The group name is used to store the expansion state personalization information.
	 * </p>
	 */
	public void setName(String name) {
		String realGroupName = ExpressionSyntax.expandVariables(pageContext, name);
		FormContainer parentContainer = FormTagUtil.findParentFormContainer(this);
		_container = (FormContainer) FormContext.getMemberByRelativeName(parentContainer, realGroupName);
	}

	/**
	 * Mark this group as not collapsible, even if a {@link #setName(String)} is specified.
	 */
	public void setPreventCollapse(boolean preventCollapse) {
		settings().setPreventCollapse(preventCollapse);
	}

	/**
	 * The {@link FormMember} that decides about the visibility of this group.
	 */
	public FormMember getVisibility() {
		return _container;
	}

	@Override
	protected void setup() {
		super.setup();
		_jspControl = new JSPLayoutedControls();
	}

	@Override
	protected void tearDown() {
		_jspControl = null;
		_container = null;

		_firstColumnWidth = null;
		_personalizationName = null;
		_title = null;
		_cssClass = null;
		_initiallyCollapsed = DEFAULT_INITILY_COLLAPSED;

		_settings = null;
		super.tearDown();
	}

	@Override
	public FormTag getFormTag() {
		return FormTagUtil.findFormTag(this);
	}

	@Override
	public FormMember getMember() {
		return container();
	}

	@Override
	public FormContainer getFormContainer() {
		return container();
	}

	private FormContainer container() {
		if (_container != null) {
			return _container;
		}
		return FormTagUtil.findParentFormContainer(this);
	}

	/**
	 * A name under which the {@link Collapsible} state of this group is stored relative to the
	 * component.
	 * 
	 * <p>
	 * This option can be used, if there is not model element that provides the expansion state.
	 * </p>
	 * 
	 * @see #setName(String)
	 */
	public void setPersonalizationName(String personalizationName) {
		_personalizationName = personalizationName;
	}

	/**
	 * The title based on a suffix to the components resource prefix.
	 */
	public void setTitleKeySuffix(String titleKeySuffix) {
		setTitleKeySuffix(titleKeySuffix, null);
	}

	/**
	 * The title based on a suffix to the components resource prefix with <code>fallback</code> as
	 * fallback {@link ResKey}.
	 * 
	 * @param fallback
	 *        Key used as fallback when the title based on the components resource prefix does not
	 *        exist. May be <code>null</code> which leads to no fallback.
	 */
	public void setTitleKeySuffix(String titleKeySuffix, ResKey fallback) {
		LayoutTag layout = (LayoutTag) findAncestorWithClass(this, LayoutTag.class);
		ResKey titleKey = layout.getComponent().getResPrefix().key(titleKeySuffix);
		titleKey = ResKey.fallback(titleKey, fallback);
		setTitleKeyConst(titleKey);
	}

	/**
	 * The GroupCell title as resource key with optionally encoded arguments.
	 */
	public void setTitleKey(String titleKey) {
		setTitleKeyConst(ResKey.internalJsp(titleKey));
	}

	/**
	 * Sets the title key with a {@link ResKey} instance.
	 */
	public void setTitleKeyConst(ResKey key) {
		setTitle(Fragments.message(key));

		// Use resource key as default for the personalisation name.
		if (_personalizationName == null) {
			ResKey plainKey = key.plain();
			if (plainKey.hasKey()) {
				setPersonalizationName(plainKey.getKey());
			}
		}
	}

	/**
	 * The GroupCell title as plain (already internationalized) text.
	 */
	public void setTitleText(String plainText) {
		setTitle(wrapTitle(Fragments.text(plainText)));
	}

	private HTMLFragment createTitle() {
		HTMLFragment title;
		if (_title != null) {
			title = _title;
		} else {
			HTMLFragment defaultTitle = null;
			if (_container != null && hasLegend()) {
				defaultTitle = wrapTitle(Fragments.text(_container.getLabel()));
			}
			if (defaultTitle == null) {
				defaultTitle = Fragments.text(HTMLConstants.NBSP);
			}
			title = defaultTitle;
		}
		return title;
	}

	private boolean hasLegend() {
		return settings().hasLegend();
	}

	private final DefaultGroupSettings settings() {
		if (_settings == null) {
			_settings = new DefaultGroupSettings();
		}
		return _settings;
	}

	/**
	 * Whether the fieldset should initially be collapsed.
	 */
	public void setInitiallyCollapsed(boolean collapsed) {
		_initiallyCollapsed = collapsed;
	}


	/**
	 * The CSS class to add to the group decorating top-level element(s).
	 * 
	 * <p>
	 * Note: CSS classes defined on the {@link FormContainer} model are also applied.
	 * </p>
	 * 
	 * @see FormContainer#getCssClasses()
	 */
	@CalledFromJSP
	public void setCssClass(String value) {
		_cssClass = value;
	}

	/**
	 * The combined CSS classes to use in the UI.
	 * 
	 * @see #setCssClass(String)
	 */
	public String createCssClass() {
		return CssUtil.joinCssClasses(_cssClass, _container == null ? null : _container.getCssClasses());
	}

	/**
	 * The CSS style to add to the group decorating top-level element(s).
	 * 
	 * @param style
	 *        The CSS styling attributes.
	 */
	public void setStyle(String style) {
		settings().setStyle(style);
	}

	/**
	 * Sets over how many rows this {@link GroupCellTag} is rendered. If there are not that many
	 * rows it will be rendered over the highest number possible.
	 * 
	 * @param rows
	 *        Number of rows over this {@link GroupCellTag} is rendered.
	 */
	public void setRows(int rows) {
		settings().setRows(rows);
	}


	/**
	 * Whether a border should be drawn around the GroupCell.
	 * 
	 * <p>
	 * Only relevant, if a title area is drawn also, see {@link #setLegend(boolean)}.
	 * </p>
	 * 
	 * <p>
	 * If <code>false</code>, only a title region is drawn, if {@link #setLegend(boolean)} is set.
	 * </p>
	 */
	public void setBorder(boolean border) {
		settings().setHasBorder(Boolean.valueOf(border));
	}

	/**
	 * Sets the value for whether the label is rendered above the content.
	 * 
	 * @param labelAbove
	 *        If <code>true</code> label is rendered above, else it will be rendered before.
	 */
	public void setLabelAbove(boolean labelAbove) {
		settings().setLabelAbove(labelAbove);
	}

	/**
	 * Returns whether the label is rendered above the content.
	 * 
	 * @return If <code>true</code> label is rendered above, else it will be rendered before.
	 */
	public boolean getLabelAbove() {
		return settings().getLabelAbove();
	}

	/**
	 * The group has a title area.
	 * 
	 * <p>
	 * If <code>false</code>, only a title region is drawn.
	 * </p>
	 */
	@CalledFromJSP
	public void setLegend(boolean legend) {
		settings().setHasLegend(legend);
	}

	/**
	 * Sets the title area to an arbitrary fragment.
	 */
	public void setTitle(HTMLFragment title) {
		_title = title;
	}

	/**
	 * Sets the width of the GroupCell.
	 * 
	 * @param width
	 *        The width.
	 */
	public void setWidth(String width) {
		settings().setWidth(width);
	}

	/**
	 * The CSS width of the first column of the cell. By default its the cell for the label.
	 * 
	 * @param firstColumnWidth
	 *        CSS width
	 */
	public void setFirstColumnWidth(String firstColumnWidth) {
		_firstColumnWidth = firstColumnWidth;
	}

	/**
	 * Adds an additional span is required to add a solid background with left and right padding to
	 * render the text above the border in filter form display
	 * 
	 * @param title
	 *        The group title text.
	 * @return The wrapped title.
	 * 
	 * @see #TITLE_TEXT_CSS_CLASS
	 */
	public static HTMLFragment wrapTitle(HTMLFragment title) {
		return Fragments.span(TITLE_TEXT_CSS_CLASS, title);
	}

	/**
	 * Whether the form group is rendered over the whole line, even when it is in a multicolumn
	 * container.
	 * 
	 * @param wholeLine
	 *        <code>true</code> is rendered over every cell of the entire row.
	 */
	public void setWholeLine(boolean wholeLine) {
		settings().setWholeLine(wholeLine);
	}

	/**
	 * Sets the number of (maximal) columns of the GroupCell.
	 * 
	 * @param columns
	 *        The maximal number of columns.
	 */
	public void setColumns(int columns) {
		settings().setColumns(columns);
	}

	@Override
	protected String getTagName() {
		return GROUP_CELL_TAG;
	}

	/**
	 * Returns the CSS width of the first column.
	 * 
	 * @return CSS width.
	 */
	public String getFirstColumnWidth() {
		return _firstColumnWidth;
	}

	private Collapsible getExpansionModel() {
		// Note the personalization name overrides the container setting: With an explicit
		// personalization name on the JSP it is extremely simple to create boxes that collapse and
		// expand synchronously. With linking the container to (different) groups the same effect
		// requires implementing listeners in the form context.
		if (_personalizationName != null) {
			return createSimpleExpansionModel(_personalizationName);
		}

		if (_container != null) {
			return _container;
		}

		return new DefaultExpansionModel(false);
	}

	private Collapsible createSimpleExpansionModel(final String personalizationName) {
		final LayoutComponent component = MainLayout.getComponent(pageContext);
		if (LayoutConstants.hasSynthesizedName(component)) {
			throw new IllegalStateException(
				"In a component with a synthesized name, no personalization can take place.");
		}
		final String personalizationKey = component.getName() + "." + personalizationName + ".groupCellCollapsed";

		Collapsible expansionModel = (Collapsible) pageContext.getAttribute(personalizationKey);
		if (expansionModel == null) {
			expansionModel = new PersonalizedExpansionModel(_initiallyCollapsed, new ConfigKey() {

				@Override
				public String get() {
					return personalizationKey;
				}
			});
			pageContext.setAttribute(personalizationKey, expansionModel);
		}

		return expansionModel;
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		return _jspControl.addControl(childControl);
	}

	@Override
	public int doAfterBody() throws JspException {
		int doAfterBody = super.doAfterBody();
		if (getBodyContent() != null) {
			String contentPattern = getBodyContent().getString();
			_jspControl.setContentPattern(contentPattern);
		}

		return doAfterBody;
	}

	@Override
	public int doEndTag() throws JspException {
		settings().setCssClass(createCssClass());
	
		GroupCellControl groupCellControl = new GroupCellControl(_jspControl, getExpansionModel(), settings())
			.setTitle(createTitle());

		try {
			ControlTagUtil.writeControl(getOut(), this, pageContext, groupCellControl);
		} catch (IOException ex) {
			throw new JspException(ex);
		}
		
		return super.doEndTag();
	}
}