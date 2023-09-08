/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.character.StringContent;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;

/**
 * An {@link ConfiguredItemChangeRewriter} transforming an XML attribute using a XSLT
 * transformation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XsltValueTransform extends ConfiguredItemChangeRewriter<XsltValueTransform.Config> {

	/**
	 * Configuration of a {@link XsltValueTransform}.
	 */
	public interface Config extends PolymorphicConfiguration<XsltValueTransform>, AttributeConfig {

		/**
		 * @see #getTransform()
		 */
		String TRANSFORM = "transform";

		/**
		 * Name of the XSLT transformation resource (path name relative to the web application root)
		 * to apply to the given {@link #getAttribute()}.
		 */
		@Name(TRANSFORM)
		@Mandatory
		String getTransform();

		/**
		 * Setter for {@link #getTransform()}.
		 */
		void setTransform(String value);

		/**
		 * Whether to properly indent the result.
		 */
		boolean getIndent();

	}

	private Transformer _transformer;

	/**
	 * Creates a new {@link XsltValueTransform}.
	 */
	public XsltValueTransform(InstantiationContext context, Config config) {
		super(context, config);
		_transformer = XsltUtil.createTransformer(config.getTransform(), config.getIndent());
	}

	@Override
	public void rewriteItemChange(ItemChange event) {
		Map<String, Object> values = event.getValues();
		String attribute = getConfig().getAttribute();
		Object oldAttributeValue = values.get(attribute);
		if (oldAttributeValue != null) {
			String oldValue = oldAttributeValue.toString();
			if (!oldValue.isEmpty()) {
				try {
					StringWriter buffer = new StringWriter();
					_transformer.transform(new StreamSource(new StringReader(oldValue)), new StreamResult(buffer));
					ItemUpdate update = new ItemUpdate(event.getRevision(), event.getObjectId(), true);
					String newValue = buffer.toString();
					try {
						Object newAttributeValue = oldAttributeValue instanceof ConfigurationItem
							? TypedConfiguration.parse(new StringContent(newValue))
							: newValue;
						update.setValue(attribute, event.getOldValue(attribute), newAttributeValue, false);
						event.visitItemEvent(ChangeSet.MERGE_UPDATE, update);
					} catch (ConfigurationException ex) {
						Logger.error("Cannot parse transformed value '" + newValue + "'.", ex,
							XsltValueTransform.class);
					}
				} catch (TransformerException ex) {
					Logger.error("Cannot transform value '" + oldValue + "'.", ex, XsltValueTransform.class);
				}
			}
		}
	}

}

