/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.infoservice;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Date;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.basic.WithPropertiesDelegateFactory;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.template.WithPropertiesBase;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.util.Resources;

/**
 * A simple item for {@link InfoService}, that just displays a item class icon and a message.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultInfoServiceItem extends WithPropertiesBase implements HTMLFragment {

	private static final ThemeImage ITEM_CLOSE_IMAGE = Icons.CLOSE_INFO_SERVICE_ITEM;

	private static final String INFO_SERVICE_ITEM_CLASS = "tl-info-service-item";

	private static final String ITEM_CLOSE_BUTTON_CLASS = "tl-info-service-item__close-button";

	private static final WithPropertiesDelegate DELEGATE =
		WithPropertiesDelegateFactory.lookup(DefaultInfoServiceItem.class);

	private final ThemeImage _infoHeaderIcon;

	private final ResKey _headerText;

	private final HTMLFragment _message;

	private final String _kindOfClass;

	private String _itemID;

	/** Create a new {@link DefaultInfoServiceItem}. */
	public DefaultInfoServiceItem(ThemeImage infoHeaderIcon, ResKey headerText, HTMLFragment textPart,
			String cssClass) {
		super(DELEGATE);
		_infoHeaderIcon = infoHeaderIcon;
		_headerText = headerText;
		_message = textPart;
		_kindOfClass = cssClass;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		createItemId(context);
		Icons.INFO_SERVICE_ITEM_TEMPLATE.get().write(context, out, this);
	}

	private void createItemId(DisplayContext context) {
		_itemID = MainLayout.getMainLayout(context).getEnclosingFrameScope().createNewID();
	}

	/** Returns the ID. */
	@TemplateVariable("messageBoxID")
	public String getItemID() {
		return _itemID;
	}

	/**
	 * Returns the individual classes for the different types of messages.
	 */
	@TemplateVariable("messageBoxClasses")
	public void getAllMessageBoxClasses(TagWriter out) throws IOException {
		out.append(INFO_SERVICE_ITEM_CLASS);
		out.append(_kindOfClass);
	}

	/**
	 * If there is no icon, the header is not displayed.
	 */
	@TemplateVariable("hasMessageIcon")
	public boolean getIconClass() {
		return _infoHeaderIcon != null;
	}

	/**
	 * Writes the existing error, warning or information icon.
	 */
	@TemplateVariable("messageIcon")
	public void writeIconClassImage(DisplayContext context, TagWriter out) throws IOException {
		_infoHeaderIcon.write(context, out);
	}

	/**
	 * Writes the button that closes the information dialog.
	 */
	@TemplateVariable("closeButton")
	public void writeCloseButton(DisplayContext context, TagWriter out) throws IOException {
		XMLTag icon = ITEM_CLOSE_IMAGE.toIcon();
		icon.beginBeginTag(context, out);
		out.writeAttribute(CLASS_ATTR, ITEM_CLOSE_BUTTON_CLASS);
		writeOnClickAttribute(out);
		icon.endEmptyTag(context, out);
	}

	private void writeOnClickAttribute(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("closeInfoItem(\"");
		out.append(getItemID());
		out.append("\")");
		out.endAttribute();
	}

	/** Writes the title. */
	@TemplateVariable("header")
	public void writeHeader(DisplayContext context, TagWriter out) throws IOException {
		String title = Resources.getInstance().getString(_headerText);
		if (title != null) {
			title = title.replaceAll("<br\\s*/?>", " ");
		}
		out.writeText(title);
	}

	/** Writes the message. */
	@TemplateVariable("message")
	public void writeInfoMessage(DisplayContext context, TagWriter out) throws IOException {
		_message.write(context, out);
	}

	/** Writes the date and time when this message was created. */
	@TemplateVariable("currentDateTime")
	public String getCurrentTime() {
		return retrieveCurrentTime();
	}

	/**
	 * Returns the date and time in a format that matches the user settings for each location and
	 * language.
	 */
	public static String retrieveCurrentTime() {
		Date currentTime = new Date();
		Formatter instance = HTMLFormatter.getInstance();
		return instance.formatDateTime(currentTime);
	}

	/**
	 * Only for warning and error messages, the time of the message should be specified.
	 */
	@TemplateVariable("isErrorOrWarning")
	public boolean isErrorOrWarningMessage() {
		return !_kindOfClass.equals(InfoService.INFO_CSS);
	}

}
