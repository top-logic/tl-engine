/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.chat;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.ai.chat.view.AgentChatControl;
import com.top_logic.ai.chat.view.AgentChatElement.TraceMode;
import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.util.TLContext;

/**
 * The chat control of the demo composition: binds the session like every chat, and adds the
 * invoices the "Rechnungen" section lists.
 *
 * <p>
 * The invoices are the application's own model objects, not fixtures: the same
 * {@code tl.ai.demo.imageclassification:Invoice} instances the demo's invoice table shows. They
 * reach the browser as one JSON string in the control state, because the state map is transported
 * as scalars.
 * </p>
 */
public class DemoChatControl extends AgentChatControl {

	/** State key holding the invoice list as JSON. */
	public static final String INVOICES = "invoicesJson";

	/**
	 * Creates a {@link DemoChatControl}.
	 *
	 * @param invoiceType
	 *        Qualified name of the type whose instances are listed.
	 */
	public DemoChatControl(ReactContext context, TraceMode traceMode, String promptImproverAgent,
			String reactElement, String invoiceType) {
		super(context, traceMode, promptImproverAgent, reactElement);
		putState(INVOICES, invoicesJson(invoiceType));
	}

	private static String invoicesJson(String invoiceType) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginArray();
			for (TLObject invoice : invoices(invoiceType)) {
				writeInvoice(json, invoice);
			}
			json.endArray();
		} catch (IOException ex) {
			Logger.error("Invoices of '" + invoiceType + "' could not be written.", ex,
				DemoChatControl.class);
			return "[]";
		}
		return buffer.toString();
	}

	private static List<TLObject> invoices(String invoiceType) {
		List<TLObject> result = new ArrayList<>();
		try {
			if (TLContext.isAnonymous()) {
				return result;
			}
			TLClass type = (TLClass) TLModelUtil.findType(ModelService.getApplicationModel(), invoiceType);
			for (TLObject instance : MetaElementUtil.getAllInstancesOf(type, TLObject.class)) {
				if (instance.tValid()) {
					result.add(instance);
				}
			}
		} catch (RuntimeException ex) {
			// A demo whose invoice type is not deployed still shows its chat; the section is empty.
			Logger.info("No invoices of '" + invoiceType + "': " + ex.getMessage(), DemoChatControl.class);
		}
		return result;
	}

	private static void writeInvoice(JsonWriter json, TLObject invoice) throws IOException {
		json.beginObject();
		json.name("id").value(String.valueOf(invoice.tIdLocal()));
		json.name("name").value(text(invoice, "description", "Beleg"));
		json.name("meta").value(meta(invoice));
		json.name("state").value(text(invoice, "amountLabel", ""));
		json.endObject();
	}

	private static String meta(TLObject invoice) {
		Object date = value(invoice, "date");
		if (date instanceof Date day) {
			return DateFormat.getDateInstance(DateFormat.MEDIUM, locale()).format(day);
		}
		return "";
	}

	private static Locale locale() {
		TLContext context = TLContext.getContext();
		Locale locale = context == null ? null : context.getCurrentLocale();
		return locale == null ? Locale.getDefault() : locale;
	}

	private static String text(TLObject invoice, String attribute, String fallback) {
		Object value = value(invoice, attribute);
		String text = value == null ? "" : String.valueOf(value).trim();
		return text.isEmpty() ? fallback : text;
	}

	private static Object value(TLObject invoice, String attribute) {
		try {
			return invoice.tValueByName(attribute);
		} catch (RuntimeException ex) {
			return null;
		}
	}
}
