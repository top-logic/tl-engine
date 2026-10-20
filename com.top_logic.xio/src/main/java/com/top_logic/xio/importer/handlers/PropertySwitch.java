/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.handlers.Handler.Config.HandlerRef;
import com.top_logic.xio.importer.handlers.PropertySwitch.Config.ValueHandler;

/**
 * {@link Handler} that dispatches to an inner {@link Handler} based on the value of an XML
 * attribute.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see SwitchImportHandler
 */
public class PropertySwitch<C extends PropertySwitch.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link PropertySwitch}.
	 */
	@TagName("switch")
	public interface Config<I extends PropertySwitch<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * The name of the XML attribute to read.
		 */
		String getXmlAttribute();

		/**
		 * The cases assigning potential values of the {@link #getXmlAttribute()} to target
		 * {@link Handler}s.
		 * 
		 * <p>
		 * A value must not be listed in more than one case of the same switch.
		 * </p>
		 */
		@DefaultContainer
		List<ValueHandler> getCases();

		/**
		 * The default {@link Handler} to invoke, if the value does not match any of the configured
		 * {@link #getCases()}.
		 */
		HandlerRef getDefault();

		/**
		 * Combination of a {@link Handler} reference with the values selecting it.
		 */
		interface ValueHandler extends HandlerRef {
			/**
			 * Configuration name of {@link #getValue()}.
			 */
			public static final String VALUE = "value";

			/**
			 * The values of the
			 * {@link com.top_logic.xio.importer.handlers.PropertySwitch.Config#getXmlAttribute()}
			 * selecting this case, comma-separated.
			 * 
			 * <p>
			 * Each of the values selects this case. An empty entry, or an empty value of this
			 * property, stands for the empty attribute value.
			 * </p>
			 */
			@Name(VALUE)
			@Mandatory
			@Format(CaseValues.class)
			List<String> getValue();
		}
	}

	private Map<String, Handler> _cases;

	private Handler _default;

	/**
	 * Creates a {@link PropertySwitch} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PropertySwitch(InstantiationContext context, C config) {
		super(context, config);

		_cases = new HashMap<>();
		for (ValueHandler valueHandler : config.getCases()) {
			Handler handler = context.getInstance(valueHandler.getHandler());
			for (String value : valueHandler.getValue()) {
				if (_cases.containsKey(value)) {
					context.error("Duplicate case value '" + value + "' in the switch at " + config.location() + ".");
					continue;
				}
				_cases.put(value, handler);
			}
		}

		HandlerRef defaultRef = config.getDefault();
		if (defaultRef != null) {
			_default = context.getInstance(defaultRef.getHandler());
		} else {
			NoOp.Config<?> noOp = TypedConfiguration.newConfigItem(NoOp.Config.class);
			_default = context.getInstance(noOp);
		}
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String value = in.getAttributeValue(null, getConfig().getXmlAttribute());
		if (value != null) {
			Handler handler = _cases.get(value);
			if (handler == null) {
				handler = _default;
			}
			return handler.importXml(context, in);
		}
		return null;
	}

}
