/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.WrappedXMLStreamException;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} that tests arbitrary conditions on an imported object for selecting an
 * appropriate target {@link Handler}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SwitchImportHandler<C extends SwitchImportHandler.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link SwitchImportHandler}.
	 */
	@TagName("switch-block")
	public interface Config<I extends SwitchImportHandler<?>> extends ConfiguredImportHandler.Config<I> {
		/**
		 * {@link Handler}s guarded by predicates.
		 * 
		 * @see com.top_logic.xio.importer.handlers.ConditionalHandler.Config#getPredicate()
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<ConditionalHandler<?>>> getCases();

		/**
		 * The default {@link Handler} to execute, if none of the predicates evaluate to
		 * <code>true</code>.
		 * 
		 * @see com.top_logic.xio.importer.handlers.ConditionalHandler.Config#getPredicate()
		 */
		List<PolymorphicConfiguration<? extends Handler>> getElse();

	}

	private List<ConditionalHandler<?>> _cases;

	private List<Handler> _else;


	/**
	 * Creates a {@link SwitchImportHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SwitchImportHandler(InstantiationContext context, C config) {
		super(context, config);
		_cases = TypedConfiguration.getInstanceList(context, config.getCases());
		_else = TypedConfiguration.getInstanceList(context, config.getElse());
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		try {
			exec(context, in, 0);
		} catch (WrappedXMLStreamException ex) {
			throw ex.getStreamException();
		}
		return null;
	}

	private void exec(ImportContext context, XMLStreamReader in, int index) {
		if (index == _cases.size()) {
			try {
				for (Handler handler : _else) {
					handler.importXml(context, in);
				}
			} catch (XMLStreamException ex) {
				throw new WrappedXMLStreamException(ex);
			}
		} else {
			_cases.get(index).execIf(context, in, () -> exec(context, in, index + 1));
		}
	}

}
