/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.json.JsonValueBinding;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringSchema;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} for {@link BinaryData} values.
 *
 * <p>
 * Binary data is serialized as a JSON object with the following properties:
 * </p>
 * <ul>
 * <li><code>name</code> - The file name.</li>
 * <li><code>content-type</code> - The MIME content type.</li>
 * <li><code>data</code> - The base64-encoded binary content.</li>
 * </ul>
 *
 * @see BinaryDataBinding
 */
public class BinaryDataJsonBinding implements JsonValueBinding<BinaryData> {

	private static final String NAME = "name";

	private static final String CONTENT_TYPE = "content-type";

	private static final String DATA = "data";

	@Override
	public BinaryData loadConfigItem(PropertyDescriptor property, JsonReader in, BinaryData baseValue)
			throws IOException, ConfigurationException {
		String name = null;
		String contentType = BinaryDataSource.CONTENT_TYPE_OCTET_STREAM;
		String data = null;

		in.beginObject();
		while (in.hasNext()) {
			switch (in.nextName()) {
				case NAME:
					name = in.nextString();
					break;
				case CONTENT_TYPE:
					contentType = in.nextString();
					break;
				case DATA:
					data = in.nextString();
					break;
				default:
					in.skipValue();
					break;
			}
		}
		in.endObject();

		if (data == null) {
			return null;
		}

		byte[] bytes = Base64.getDecoder().decode(data);
		return BinaryDataFactory.createBinaryData(bytes, contentType,
			name != null ? name : BinaryData.NO_NAME);
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, BinaryData item) throws IOException {
		out.beginObject();

		out.name(NAME);
		out.value(item.getName());

		out.name(CONTENT_TYPE);
		out.value(item.getContentType());

		out.name(DATA);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		item.deliverTo(buffer);
		out.value(Base64.getEncoder().encodeToString(buffer.toByteArray()));

		out.endObject();
	}

	@Override
	public Schema buildSchema(PropertyDescriptor property) {
		ObjectSchema schema = ObjectSchema.create();

		schema.putProperty(NAME, StringSchema.create());
		schema.putProperty(CONTENT_TYPE, StringSchema.create());
		schema.putProperty(DATA, StringSchema.create().setContentEncoding("base64"));

		schema.addRequired(NAME);
		schema.addRequired(CONTENT_TYPE);
		schema.addRequired(DATA);

		return schema;
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value == null || value instanceof BinaryData;
	}

	@Override
	public BinaryData defaultValue() {
		return null;
	}

	@Override
	public Object normalize(Object value) {
		return value;
	}

}
