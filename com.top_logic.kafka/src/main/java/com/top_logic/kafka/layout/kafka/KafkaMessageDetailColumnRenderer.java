/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.DefaultValueFactory;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.json.JSON.ValueFactory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.boxes.model.DescriptionBox;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * {@link Renderer} writing a button opening a message box with a formatted display of a
 * {@link KafkaMessage}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class KafkaMessageDetailColumnRenderer extends AbstractShowObjectColumnRenderer<KafkaMessage> {

	@Override
	protected Renderer<KafkaMessage> getContentRenderer() {
		return new KafkaMessageRenderer();
	}

	@Override
	protected ThemeImage getIcon() {
		return com.top_logic.element.layout.meta.search.quick.Icons.SEARCH;
	}

	@Override
	protected DisplayValue getTitle() {
		return new ResourceText(I18NConstants.MESSAGE_DETAIL_TITLE);
	}

	/**
	 * Rendering information to a {@link KafkaMessage}.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class KafkaMessageRenderer implements Renderer<KafkaMessage> {

		private static final char INVALID_CHARACTER = '\ufffd';

		private static final String MESSAGE_TIMESTAMP = "kafka-message-timestamp";

		private static final String MAP_DETAIL_CSS_CLASS = "mapDetailValue";

		private static final String LIST_DETAIL_CSS_CLASS = "listDetailValue";

		private DateFormat _format;

		/**
		 * Creates a new {@link KafkaMessageRenderer}.
		 */
		KafkaMessageRenderer() {
			_format = HTMLFormatter.getInstance().getDateFormatNonNull(MESSAGE_TIMESTAMP);
		}

		@Override
		public void write(DisplayContext context, TagWriter out, KafkaMessage value) throws IOException {
			out.beginTag(TABLE);
			writeMessage(context, out, value);
			out.endTag(TABLE);
		}

		private void writeMessage(DisplayContext context, TagWriter out, KafkaMessage message) {
			Resources resources = context.getResources();
			String topic = message.getTopic().getName();
			Date date = message.getDate();


			writeSimpleValue(out, resources.getString(I18NConstants.MESSAGE_DETAIL_TOPIC), formatString(topic));
			writeSimpleValue(out, resources.getString(I18NConstants.MESSAGE_DETAIL_TIMESTAMP), formatTimestamp(date));
			writeComplexValue(out, resources.getString(I18NConstants.MESSAGE_DETAIL_KEY),
				StringServices.toString(message.getKey()));
			writeComplexValue(out, resources.getString(I18NConstants.MESSAGE_DETAIL_VALUE),
				StringServices.toString(message.getValue()));
		}

		private void writeSimpleValue(TagWriter out, String key, CharSequence value) {
			out.beginTag(TR);
			{
				out.beginTag(TD, CLASS_ATTR, DescriptionBox.DEFAULT_DESCRIPTION_CSS_CLASS);
				out.writeText(key);
				out.writeText(':');
				out.endTag(TD);

				out.beginTag(TD, CLASS_ATTR, DescriptionBox.DEFAULT_CONTENT_CSS_CLASS);
				out.writeText(value);
				out.endTag(TD);
			}
			out.endTag(TR);
		}

		private void writeComplexValue(TagWriter out, String key, String value) {
			out.beginTag(TR);
			{
				out.beginTag(TD, CLASS_ATTR, DescriptionBox.DEFAULT_DESCRIPTION_CSS_CLASS);
				out.writeText(key);
				out.writeText(':');
				out.endTag(TD);

				out.beginTag(TD, CLASS_ATTR, DescriptionBox.DEFAULT_CONTENT_CSS_CLASS);
				writeValue(out, value);
				out.endTag(TD);
			}
			out.endTag(TR);
		}

		private void writeValue(TagWriter out, String value) {
			if (!StringServices.isEmpty(value)) {
				if (value.startsWith("{")) {
					writeJSON(out, value);
				} else {
					writeString(out, value);
				}
			}
		}

		private void writeString(TagWriter out, String value) {
			out.writeText(formatString(value));
		}

		private void writeJSON(TagWriter out, String value) {
			try {
				writeJSON(out, JSON.fromString(value, LongValueFactory.INSTANCE));
			} catch (ParseException ex) {
				writeString(out, value);
			}
		}

		@SuppressWarnings("unchecked")
		private void writeJSON(TagWriter anOut, Object anObject) {
			if (anObject instanceof Map) {
				writeMap(anOut, (Map<String, Object>) anObject);
			} else if (anObject instanceof List) {
				writeList(anOut, (List<Object>) anObject);
			} else if (anObject instanceof Number) {
				anOut.writeText(anObject.toString());
			} else if (anObject instanceof Date) {
				anOut.writeText(formatTimestamp((Date) anObject));
			} else if (anObject instanceof Boolean) {
				anOut.writeText(anObject.toString());
			} else if (anObject == null) {
				anOut.writeText("---");
			} else {
				writeString(anOut, '\"' + anObject.toString() + '\"');
			}
		}

		private void writeMap(TagWriter out, Map<String, Object> map) {
			out.beginTag(DIV, CLASS_ATTR, MAP_DETAIL_CSS_CLASS);

			List<String> keys = CollectionUtil.toList(map.keySet());
			Collections.sort(keys);

			boolean firstKey = true;
			for (String key : keys) {
				firstKey = writeSeparator(firstKey, out);
				writeString(out, key);
				out.writeText(':');
				out.writeText(NBSP);
				writeJSON(out, map.get(key));
			}

			out.endTag(DIV);
		}

		private void writeList(TagWriter out, List<Object> list) {
			out.beginTag(DIV, CLASS_ATTR, LIST_DETAIL_CSS_CLASS);

			boolean firstEntry = true;
			for (Object content : list) {
				firstEntry = writeSeparator(firstEntry, out);
				writeJSON(out, content);
			}

			out.endTag(DIV);
		}

		private boolean writeSeparator(boolean firstEntry, TagWriter out) {
			if (!firstEntry) {
				out.writeText(',');
				out.emptyTag(BR);
			}
			return false;
		}

		private CharSequence formatTimestamp(Date date) {
			return _format.format(date);
		}

		private CharSequence formatString(String string) {
			return KafkaMessageRenderer.escape(string);
		}

		/**
		 * Escape the given string (to be displayed in HTML front end).
		 * 
		 * @param string
		 *        String to be escaped.
		 * @return Escaped string containing no evil HTML characters.
		 */
		public static String escape(String string) {
			int size = string.length();
			StringBuilder builder = new StringBuilder(size);

			for (int i = 0; i < size; i++) {
				char c = string.charAt(i);
				if (isControlCharacter(c)) {
					builder.append(INVALID_CHARACTER);
				} else {
					builder.append(c);
				}
			}

			return builder.toString();
		}

		private static boolean isControlCharacter(char c) {
			if (c >= '\u0020' && c <= '\u007e') {
				return false;
			}
			if (c >= '\u00a0') {
				return false;
			}
			return false;
		}
	}

	@SuppressWarnings("javadoc")
	public static class LongValueFactory extends DefaultValueFactory {

		/** Only instance of this factory. */
		@SuppressWarnings("hiding")
		public static final ValueFactory INSTANCE = new LongValueFactory();

		@Override
		public Object createIntegerValue(CharSequence integerValue) {
			return Long.valueOf(Long.parseLong(integerValue.toString()));
		}
	}
}
