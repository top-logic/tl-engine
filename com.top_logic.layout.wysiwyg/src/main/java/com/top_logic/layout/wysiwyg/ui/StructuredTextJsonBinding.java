/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.json.JsonValueBinding;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringSchema;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} for {@link StructuredText} values.
 *
 * <p>
 * A {@link StructuredText} is serialized as a JSON object with the following properties:
 * </p>
 * <ul>
 * <li><code>source-code</code> - The HTML source code.</li>
 * <li><code>images</code> - An object mapping image names to objects with <code>content-type</code>
 * and base64-encoded <code>data</code>.</li>
 * </ul>
 */
public class StructuredTextJsonBinding implements JsonValueBinding<StructuredText> {

	private static final String SOURCE_CODE = "source-code";

	private static final String IMAGES = "images";

	private static final String CONTENT_TYPE = "content-type";

	private static final String DATA = "data";

	@Override
	public StructuredText loadConfigItem(PropertyDescriptor property, JsonReader in, StructuredText baseValue)
			throws IOException, ConfigurationException {
		String sourceCode = "";
		Map<String, BinaryData> images = new HashMap<>();

		in.beginObject();
		while (in.hasNext()) {
			switch (in.nextName()) {
				case SOURCE_CODE:
					sourceCode = in.nextString();
					break;
				case IMAGES:
					images = readImages(in);
					break;
				default:
					in.skipValue();
					break;
			}
		}
		in.endObject();

		return new StructuredText(sourceCode, images);
	}

	private Map<String, BinaryData> readImages(JsonReader in) throws IOException {
		Map<String, BinaryData> images = new HashMap<>();
		in.beginObject();
		while (in.hasNext()) {
			String imageName = in.nextName();

			String contentType = BinaryDataSource.CONTENT_TYPE_OCTET_STREAM;
			String data = null;

			in.beginObject();
			while (in.hasNext()) {
				switch (in.nextName()) {
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

			if (data != null) {
				byte[] bytes = Base64.getDecoder().decode(data);
				images.put(imageName,
					BinaryDataFactory.createBinaryData(bytes, contentType, imageName));
			}
		}
		in.endObject();
		return images;
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, StructuredText item) throws IOException {
		out.beginObject();

		out.name(SOURCE_CODE);
		out.value(item.getSourceCode());

		out.name(IMAGES);
		writeImages(out, item.getImages());

		out.endObject();
	}

	private void writeImages(JsonWriter out, Map<String, BinaryData> images) throws IOException {
		out.beginObject();
		for (Map.Entry<String, BinaryData> entry : images.entrySet()) {
			out.name(entry.getKey());

			BinaryData imageData = entry.getValue();
			out.beginObject();

			out.name(CONTENT_TYPE);
			out.value(imageData.getContentType());

			out.name(DATA);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			imageData.deliverTo(buffer);
			out.value(Base64.getEncoder().encodeToString(buffer.toByteArray()));

			out.endObject();
		}
		out.endObject();
	}

	@Override
	public Schema buildSchema(PropertyDescriptor property) {
		// Image entry schema: {"content-type": "...", "data": "BASE64..."}
		ObjectSchema imageSchema = ObjectSchema.create();
		imageSchema.putProperty(CONTENT_TYPE, StringSchema.create());
		imageSchema.putProperty(DATA, StringSchema.create().setContentEncoding("base64"));
		imageSchema.addRequired(CONTENT_TYPE);
		imageSchema.addRequired(DATA);

		// Images map schema: additionalProperties of image entries
		ObjectSchema imagesSchema = ObjectSchema.create();
		imagesSchema.setAdditionalProperties(imageSchema);

		// Top-level schema
		ObjectSchema schema = ObjectSchema.create();
		schema.putProperty(SOURCE_CODE, StringSchema.create());
		schema.putProperty(IMAGES, imagesSchema);
		schema.addRequired(SOURCE_CODE);

		return schema;
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value == null || value instanceof StructuredText;
	}

	@Override
	public StructuredText defaultValue() {
		return null;
	}

	@Override
	public Object normalize(Object value) {
		return value;
	}

}
