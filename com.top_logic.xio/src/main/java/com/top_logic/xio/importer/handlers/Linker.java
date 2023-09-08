/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.xio.importer.I18NConstants;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.binding.ObjectLinking;
import com.top_logic.xio.importer.linkings.NoLinking;

/**
 * {@link Handler} linking an object to its context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Linker<C extends Linker.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link Linker}.
	 */
	@TagName("linking")
	public interface Config<I extends Linker<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * Name of the {@link ImportContext#getVar(String) variable} containing the context object.
		 */
		@StringDefault(ImportContext.SCOPE_VAR)
		@Nullable
		String getTargetVar();

		/**
		 * Name of the {@link ImportContext#getVar(String) variable} containing the current object
		 * to link.
		 */
		@StringDefault(ImportContext.THIS_VAR)
		@Nullable
		String getValueVar();

		/**
		 * The linker(s) to execute.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<ObjectLinking>> getLinkings();

	}

	private List<ObjectLinking> _linkings;

	/**
	 * Creates a {@link Linker} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Linker(InstantiationContext context, C config) {
		super(context, config);

		List<PolymorphicConfiguration<ObjectLinking>> linkingConfigs = config.getLinkings();
		if (linkingConfigs.isEmpty()) {
			_linkings = Collections.singletonList(NoLinking.INSTANCE);
		} else {
			_linkings = TypedConfiguration.getInstanceList(context, linkingConfigs);
		}
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		Object target = context.getVar(getConfig().getTargetVar());
		if (target == null) {
			context.error(in.getLocation(),
				I18NConstants.ERROR_LINKING_NULL_TARGET__HANDLER_VAR.fill(location(), getConfig().getTargetVar()));
			return null;
		}

		Object value = context.getVar(getConfig().getValueVar());
		if (value == null) {
			context.error(in.getLocation(),
				I18NConstants.ERROR_LINKING_NULL_VALUE__HANDLER_VAR.fill(location(), getConfig().getValueVar()));
			return null;
		}

		Iterator<ObjectLinking> linkings = _linkings.iterator();
		new Runnable() {
			@Override
			public void run() {
				if (linkings.hasNext()) {
					linkings.next().linkOrElse(context, target, value, this);
				} else {
					context.error(in.getLocation(), I18NConstants.ERROR_NO_LINKING__HANDLER.fill(location()));
				}
			}

		}.run();
		return null;
	}

}
