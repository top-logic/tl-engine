/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.infoservice;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.basic.WithPropertiesDelegateFactory;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.template.WithPropertiesBase;
import com.top_logic.util.Resources;

/**
 * A simple {@link HTMLFragment}, that displays the message part of an info service item.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class InfoServiceItemMessageFragment extends WithPropertiesBase implements HTMLFragment {

	private HTMLFragment _description;

	private HTMLFragment _detail;

	private static final WithPropertiesDelegate DELEGATE =
		WithPropertiesDelegateFactory.lookup(InfoServiceItemMessageFragment.class);

	/**
	 * Create a new {@link InfoServiceItemMessageFragment}.
	 */
	public InfoServiceItemMessageFragment(ResKey description, ResKey detailMessage) {
		this(description, nonEmpty(detailMessage, "tl-info-service-item__details-message-no-bottom"));
	}

	/**
	 * Create a new {@link InfoServiceItemMessageFragment}.
	 */
	public InfoServiceItemMessageFragment(ResKey description, HTMLFragment detail) {
		this(nonEmpty(description, StringServices.EMPTY_STRING), detail);
	}

	private static HTMLFragment nonEmpty(ResKey description, String cssClass) {
		if (isNotEmpty(description)) {
			if (StringServices.isEmpty(cssClass)) {
				return Fragments.p(Fragments.message(description));
			} else {
				return Fragments.p(cssClass, Fragments.message(description));
			}
		}

		return null;
	}

	/**
	 * Create a new {@link HTMLFragment} describing the content of a message.
	 * 
	 * <p>
	 * The given description is in contrast to the details always visible. The detail on the other
	 * hand is only visible if it isn't empty.
	 * </p>
	 */
	public InfoServiceItemMessageFragment(HTMLFragment description, HTMLFragment detail) {
		super(DELEGATE);
		_description = description;
		_detail = detail;
	}

	private static boolean isNotEmpty(ResKey resourceKey) {
		return resourceKey != ResKey.NONE && !StringServices.isEmpty(Resources.getInstance().getString(resourceKey));
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		Icons.INFO_SERVICE_MESSAGE_TEMPLATE.get().write(context, out, this);
	}

	/**
	 * Writes a summary of the message, if it exists.
	 */
	@TemplateVariable("summaryMessage")
	public void writeSummaryMessage(DisplayContext context, TagWriter out) throws IOException {
		if (_description != null) {
			_description.write(context, out);
		}
	}

	/**
	 * Checks whether a detailed message is available.
	 */
	@TemplateVariable("doesDetailsExist")
	public boolean doesDetailsExist() {
		return _detail != null;
	}

	/**
	 * Writes the header of the details.
	 */
	@TemplateVariable("detailsHeader")
	public void writeDetailsSummary(TagWriter out) {
		out.writeText(Resources.getInstance().getString(I18NConstants.DETAIL_MESSAGE_HEADER));
	}

	/**
	 * Writes the detailed message.
	 */
	@TemplateVariable("detailsMessage")
	public void writeDetails(DisplayContext context, TagWriter out) throws IOException {
		_detail.write(context, out);
	}

}
