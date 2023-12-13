/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.basic.WithPropertiesDelegateFactory;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.util.Resources;

/**
 * Contains information about an individual tab from a tab bar.
 * 
 * <p>
 * Used to render tabs {@link TabBarControl#writeTabs(DisplayContext, TagWriter)}.
 * </p>
 * 
 * @author <a href="mailto:pjah@top-logic.com">pja</a>
 */
public class TabContent implements WithProperties {

	private int _tabIndex;

	private Card _tabCard;

	private boolean _isSelectedTab;

	private String _ID;

	private final WithPropertiesDelegate _propertyDelegate;

	/**
	 * Constructor for {@link TabContent}. Instantiates a {@link #_propertyDelegate} for the usage
	 * in html templates.
	 */
	public TabContent() {
		_propertyDelegate = WithPropertiesDelegateFactory.lookup(getClass());
	}

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		return _propertyDelegate.getPropertyValue(this, propertyName);
	}

	@Override
	public Optional<Collection<String>> getAvailableProperties() {
		return Optional.of(_propertyDelegate.getAvailableProperties(this));
	}

	@Override
	public void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
		_propertyDelegate.renderProperty(context, out, this, propertyName);
	}

	/** Returns the position of the tab in the tab bar. */
	public int getTabIndex() {
		return _tabIndex;
	}

	/**
	 * @see #getTabIndex
	 * 
	 * @param tabIndex
	 *        The current looping index.
	 */
	public void setTabIndex(int tabIndex) {
		_tabIndex = tabIndex;
	}

	/** Returns this tab's {@link Card} information. */
	public Card getTabCard() {
		return _tabCard;
	}

	/**
	 * @see #getTabCard
	 * 
	 * @param tabCard
	 *        {@link Card} for this tab.
	 */
	public void setTabCard(Card tabCard) {
		_tabCard = tabCard;
	}

	/** Whether this tab is selected or not. */
	public boolean isSelectedTab() {
		return _isSelectedTab;
	}

	/**
	 * @see #isSelectedTab
	 * 
	 * @param isSelectedTab
	 *        indicates if this tab is currently selected.
	 */
	public void setSelectedTab(boolean isSelectedTab) {
		_isSelectedTab = isSelectedTab;
	}

	/** Returns the {@link #_ID} for the this tab. */
	public String getID() {
		return _ID;
	}

	/**
	 * @see #getID
	 * 
	 * @param id
	 *        new ID.
	 */
	public void setID(String id) {
		_ID = id;
	}

	/**
	 * Returns the ID of the tab. Not all tabs have the same ID. The index of the tab is appended to
	 * the ID at the end.
	 */
	@TemplateVariable("tabID")
	public String getIndexedID() {
		return getID() + "-" + _tabIndex;
	}

	/**
	 * Indicates whether this tab is currently active and selected.
	 */
	@TemplateVariable("isTabSelected")
	public boolean isTabSelected() {
		return _isSelectedTab;
	}

	/**
	 * The tab container moves the tabs so that the clicked tab becomes fully visible on the screen.
	 */
	@TemplateVariable("adjustContainer")
	public void writeOnClick(DisplayContext context, TagWriter out) throws IOException {
		if (!_isSelectedTab) {
			out.append("services.viewport.adjustContainerScrollPosition(");
			writeJSONViewportReferences(out);
			out.append(", ");
			out.writeInt(_tabIndex);
			out.append(");");
		}
	}

	/** Opens the page of the pressed tab. */
	@TemplateVariable("onclick")
	public void writeOnClickSelectContent(TagWriter out) throws IOException {
		out.append("services.form.TabBarControl.handleClick(");
		out.writeJsString(getID());
		out.append(", ");
		out.writeInt(_tabIndex);
		out.append("); return false;");
	}

	/**
	 * Short explanation that is displayed when the mouse pointer is moved over the tab.
	 * 
	 * <p>
	 * <b>Tooltip</b> should be used for the <b>data-tooltip</b> variable, because it returns text
	 * inside HTML tags.
	 * </p>
	 */
	@TemplateVariable("tooltip")
	public void writeTabTooltip(DisplayContext context, TagWriter out) throws IOException {
		Object cardInfo = getTabCard().getCardInfo();
		if (cardInfo instanceof TabInfo) {
			ResKey labelKey = ((TabInfo) cardInfo).getLabelKey();
			if (labelKey != null) {
				String tooltip = Resources.getInstance().getString(labelKey.tooltipOptional());
				if (!StringServices.isEmpty(tooltip)) {
					OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context,
						out, tooltip);
				}
			}
		}
	}

	/** Writes the full technical name of this tab. */
	@TemplateVariable("tabKey")
	public String writeTabKey(DisplayContext context, TagWriter out) {
		Object cardInfo = getTabCard().getCardInfo();
		String tabKey;
		if (cardInfo instanceof TabInfo) {
			tabKey = ((TabInfo) cardInfo).getId();
		} else {
			tabKey = String.valueOf(_tabIndex);
		}
		return tabKey;
	}

	/** Writes the label to the tab. */
	@TemplateVariable("label")
	public void writeTabLabel(DisplayContext context, TagWriter out) throws IOException {
		Object cardInfo = getTabCard().getCardInfo();
		if (cardInfo instanceof TabInfo && ((TabInfo) cardInfo).getLabel() != null) {
			TabInfo tabInfo = (TabInfo) cardInfo;
			out.writeText(tabInfo.getLabel());
		} else {
			_tabCard.writeCardInfo(context, out);
		}
	}

	/** Writes the icon to the tab. */
	@TemplateVariable("tabIcon")
	public void writeTabIcon(DisplayContext context, TagWriter out) throws IOException {
		Object cardInfo = getTabCard().getCardInfo();
		if (cardInfo instanceof TabInfo) {
			TabInfo tabInfo = (TabInfo) cardInfo;
			ThemeImage tabImage = tabInfo.getImage();
			if (tabImage != null) {
				XMLTag tabIcon = tabImage.toIcon();
				if (tabIcon != null) {
					tabIcon.beginBeginTag(context, out);
					out.writeAttribute(HTMLConstants.CLASS_ATTR, "tabIcon");
					tabIcon.endBeginTag(context, out);
					tabIcon.endTag(context, out);
				}
			}
		}
	}

	private void writeJSONViewportReferences(Appendable out) throws IOException {
		String controlID = getID();
		out.append("{");
		out.append("controlID:'");
		out.append(controlID);
		out.append("',");
		out.append("viewport:'");
		out.append(controlID);
		out.append("',");
		out.append("elementContainer:'");
		out.append(controlID + "-elementContainer");
		out.append("',");
		out.append("scrollContainer:'");
		out.append(controlID + "-scrollContainer");
		out.append("',");
		out.append("scrollLeftButton:'");
		out.append(controlID + "-scrollLeftButton");
		out.append("',");
		out.append("scrollLeftButtonImage:'");
		out.append(controlID + "-scrollLeftButtonImage");
		out.append("',");
		out.append("scrollRightButton:'");
		out.append(controlID + "-scrollRightButton");
		out.append("',");
		out.append("scrollRightButtonImage:'");
		out.append(controlID + "-scrollRightButtonImage");
		out.append("',");
		out.append("additionalRightContent:'");
		out.append(controlID + "-additionalRightContent");
		out.append("',");
		out.append("visibleElement:'");
		out.append("tab");
		out.append("',");
		out.append("activeElement:'");
		out.append("activeTab");
		out.append("'}");
	}

	/**
	 * Sets all the information needed for the rendering process at once instead of three individual
	 * methods.
	 */
	public void setContentInformation(boolean isSelectedTab, Card currentTab, int tabIndex) {
		_isSelectedTab = isSelectedTab;
		_tabCard = currentTab;
		_tabIndex = tabIndex;
	}

	/** Renders a tab. */
	public void write(DisplayContext context, TagWriter out) throws IOException {
		Icons.TAB_TEMPLATE.get().write(context, out, this);
	}

}
