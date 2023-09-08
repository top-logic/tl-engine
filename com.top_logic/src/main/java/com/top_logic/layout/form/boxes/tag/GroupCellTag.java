/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.DefaultExpansionModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.VisibilityRequestListener;
import com.top_logic.layout.form.boxes.layout.VerticalLayout;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.Boxes;
import com.top_logic.layout.form.boxes.model.ContentBox;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;
import com.top_logic.layout.form.boxes.model.FieldSetBox;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.FormMemberControl;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.tag.FormContainerTag;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.FormTagUtil;
import com.top_logic.layout.form.tag.LayoutTag;
import com.top_logic.layout.form.tag.util.ExpressionSyntax;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.css.CssUtil;

/**
 * {@link BoxContainerTag} rendering a title row with a border around some content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GroupCellTag extends AbstractBoxContainerTag implements FormContainerTag {

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
	public static final String TITLE_TEXT_CSS_CLASS = FieldSetBox.TITLE_TEXT_CSS_CLASS;

	static final String GROUP_CELL_TAG = "form:groupCell";

	private static final boolean DEFAULT_LEGEND = true;

	private FormContainer _container;

	private String _personalizationName;

	private boolean _legend = DEFAULT_LEGEND;

	private Boolean _border;

	private boolean _preventCollapse;

	private FieldSetBox _groupBox;

	private HTMLFragment _title;

	/**
	 * @see #setCssClass(String)
	 */
	private String _cssClass;

	private boolean _initiallyCollapsed;

	/**
	 * The name of the {@link FormContainer} that provides the {@link Collapsible} state.
	 * 
	 * <p>
	 * The group name is used to store the expansion state personalization information.
	 * </p>
	 */
	public void setGroupName(String groupName) {
		String realGroupName = ExpressionSyntax.expandVariables(pageContext, groupName);
		FormContainer parentContainer = FormTagUtil.findParentFormContainer(this);
		_container = (FormContainer) FormContext.getMemberByRelativeName(parentContainer, realGroupName);
	}

	/**
	 * Mark this group as not collapsible, even if a {@link #setGroupName(String)} is specified.
	 */
	public void setPreventCollapse(boolean preventCollapse) {
		_preventCollapse = preventCollapse;
	}

	/**
	 * The {@link FormMember} that decides about the visibility of this group.
	 */
	public FormMember getVisibility() {
		return _container;
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
	 * @see #setGroupName(String)
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
	 * The box title as resource key with optionally encoded arguments.
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
	 * The box title as plain (already internationalized) text.
	 */
	public void setTitleText(String plainText) {
		setTitle(Fragments.text(plainText));
	}

	/**
	 * Whether the fieldset should initially be collapsed.
	 */
	public void setInitiallyCollapsed(boolean collapsed) {
		_initiallyCollapsed = collapsed;
	}

	/**
	 * The combined CSS classes to use in the UI.
	 * 
	 * @see #setCssClass(String)
	 */
	public String getCssClass() {
		return CssUtil.joinCssClasses(_cssClass, _container == null ? null : _container.getCssClasses());
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
	 * Prepends the group expansion state toggle button to the given content.
	 * 
	 * @return The prepended contents.
	 */
	private HTMLFragment appendErrorDisplay(final Collapsible expansionModel, HTMLFragment content) {
		Box center = _groupBox.getContentBox();
		final Set<FormMember> formMembers = findFormMembers(center);
		if (formMembers.isEmpty()) {
			return content;
		}

		HTMLFragment contentWithErrors = Fragments.concat(content, new ErrorControl(formMembers, true));

		if (_preventCollapse) {
			return contentWithErrors;
		}

		if (expansionModel == null) {
			return contentWithErrors;
		}

		// Expand, if inner form member requests visibility. This explicit handling may be required,
		// if no form group is used as expansion model, or a member is displayed in the content,
		// that is not contained in the group used as expansion model.
		getRenderingControl().addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {
			final VisibilityRequestListener _expander = new VisibilityRequestListener() {
				@Override
				public Bubble handleVisibilityRequested(FormMember sender) {
					expansionModel.setCollapsed(false);
					return Bubble.BUBBLE;
				}
			};

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					for (FormMember member : formMembers) {
						member.addListener(FormMember.VISIBILITY_REQUEST, _expander);
					}
				} else {
					for (FormMember member : formMembers) {
						member.removeListener(FormMember.VISIBILITY_REQUEST, _expander);
					}
				}
			}
		});

		return contentWithErrors;
	}

	private Set<FormMember> findFormMembers(Box box) {
		Set<FormMember> members = new HashSet<>();
		findFormMembers(members, box);
		return members;
	}

	private void findFormMembers(Collection<FormMember> result, Box box) {
		if (box instanceof ContentBox) {
			HTMLFragment content = ((ContentBox) box).getContentRenderer();
			if (content instanceof JSPLayoutedControls) {
				for (HTMLFragment control : ((JSPLayoutedControls) content).getControls()) {
					if (control instanceof FormMemberControl) {
						result.add(((FormMemberControl) control).getModel());
					}
				}
			}
		}
		for (Box child : box.getChildren()) {
			findFormMembers(result, child);
		}
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

		return null;
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
			final PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			boolean collapsed;
			if (pc.hasBoolean(personalizationKey)) {
				collapsed = pc.getBooleanValue(personalizationKey);
			} else {
				collapsed = _initiallyCollapsed;
			}
			expansionModel = new DefaultExpansionModel(collapsed);
			expansionModel.addListener(Collapsible.COLLAPSED_PROPERTY, new CollapsedListener() {

				@Override
				public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
					pc.setBoolean(personalizationKey, newValue);
					return Bubble.BUBBLE;
				}
			});
			pageContext.setAttribute(personalizationKey, expansionModel);
		}

		return expansionModel;
	}

	/**
	 * Whether a border should be drawn around the box.
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
		_border = Boolean.valueOf(border);
	}

	private boolean hasBorder() {
		if (_border != null) {
			return _border;
		}
		return getDefaultBorder();
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
		_legend = legend;
	}

	private boolean hasLegend() {
		return _legend;
	}

	private boolean isCollapsible() {
		return !_preventCollapse;
	}

	/**
	 * Whether group border is enabled by {@link Theme}.
	 */
	public static boolean getDefaultBorder() {
		return ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.GROUP_BORDER).booleanValue();
	}

	/**
	 * Sets the title area to an arbitrary fragment.
	 */
	public void setTitle(HTMLFragment title) {
		_title = title;
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

	@Override
	protected void setup() {
		super.setup();

		addToParentOfParentTag(mkFieldSet());
	}

	private FieldSetBox mkFieldSet() {
		if (_groupBox == null) {
			boolean showTitlebar = hasLegend() || isCollapsible();
			boolean hasBorder = hasBorder();
			_groupBox = FieldSetBox.createFieldSet(showTitlebar, hasBorder);
			_groupBox.setCssClass(getCssClass());
		}
		return _groupBox;
	}

	@Override
	protected void initContent() {
		super.initContent();

		FieldSetBox fieldSet = mkFieldSet();

		Collapsible expansionModel = getExpansionModel();

		fieldSet.setExpansionModel(expansionModel);

		// Make sure, everything is initialized before rendering.
		fieldSet.setLegend(appendErrorDisplay(expansionModel, createTitle()), isCollapsible());

		if (fieldSet.getContentBox() == null) {
			fieldSet.setContentBox(Boxes.contentBox());
		}

		final FormMember visibility = getVisibility();
		if (visibility != null) {
			fieldSet.setVisibility(visibility);
		}
	}

	private HTMLFragment createTitle() {
		HTMLFragment title;
		if (_title != null) {
			title = _title;
		} else {
			HTMLFragment defaultTitle = null;
			if (_container != null && hasLegend()) {
				defaultTitle = new LabelControl(_container);
			}
			if (defaultTitle == null) {
				defaultTitle = Fragments.text(HTMLConstants.NBSP);
			}
			title = defaultTitle;
		}
		return title;
	}

	@Override
	protected void tearDown() {
		_legend = DEFAULT_LEGEND;
		_border = null;
		_groupBox = null;
		_container = null;
		_personalizationName = null;
		_preventCollapse = false;
		_title = null;
		
		super.tearDown();
	}

	@Override
	protected DefaultCollectionBox createCollectionBox() {
		return new DefaultCollectionBox(VerticalLayout.INSTANCE);
	}

	/**
	 * Captures the content area created be the super class and adds it to the content area of the
	 * wrapped {@link #_groupBox}.
	 */
	@Override
	protected void addToParent(Box newBox) {
		_groupBox.setContentBox(newBox);
	}

	/**
	 * Add the given box into the container of the parent box tag (if such exists).
	 * 
	 * @see #addToParent(Box)
	 * @see AbstractBoxContainerTag#addToParent(Box)
	 */
	protected void addToParentOfParentTag(Box newBox) {
		super.addToParent(newBox);
	}

	@Override
	protected Box getRenderingBox() {
		return _groupBox;
	}

	@Override
	protected String getTagName() {
		return GROUP_CELL_TAG;
	}

	@Override
	protected boolean getIndependentDefault() {
		BoxContainerTag anchestorOrSelf = this;
		while (true) {
			anchestorOrSelf = AbstractBoxContainerTag.getBoxContainer(anchestorOrSelf);
			if (anchestorOrSelf == null) {
				return false;
			}
			if (anchestorOrSelf instanceof GroupCellTag) {
				// Render nested boxes independently to prevent rendering problems in IE and Chrome,
				// see Ticket #16935.
				return true;
			}
		}
	}
}
