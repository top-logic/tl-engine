/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.meta;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.Computation;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ElementConfigUtil;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.ObjectTypeConfig;
import com.top_logic.element.config.PartConfig;
import com.top_logic.element.meta.MetaAttributeFactory;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Changes the meta attributes of a {@link TLClass} due to configuration in given config file.
 * 
 * <p>
 * Note: The update occurs in the open transaction. No commit is executed. It may be possible to
 * restart {@link MetaAttributeFactory} when e.g. type of the {@link TLStructuredTypePart} is changed,
 * because then the wrapper class is changed.
 * </p>
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateMetaAttributes {

	public static final String DEFAULT_FACTORY_CONFIG_PATH = "/WEB-INF/conf";

	/**
	 * Constant to use in {@link #UpdateMetaAttributes(Protocol, Filter, String, String)} to
	 * indicate all meta attributes of the meta element must be rewritten
	 */
	public static final Filter<? super String> ALL_META_ATTRIBUTES = FilterFactory.trueFilter();

	private Collection<PartConfig> _meAttributes;

	private final Protocol _log;

	private final String _globalMEType;

	private final Filter<? super String> _maFilter;

	private final String _configFile;

	/**
	 * Creates a new {@link UpdateMetaAttributes}.
	 * 
	 * @param maFilter
	 *        Filter matching the meta attributes to adapt or {@link #ALL_META_ATTRIBUTES} when all
	 *        meta attributes must be adapted.
	 * @param globalMEType
	 *        The name of the global meta element to adapt.
	 * @param configFile
	 *        File name of the model definition defining the global type with the given name.
	 */
	public UpdateMetaAttributes(Protocol log, Filter<? super String> maFilter, String globalMEType, String configFile) {
		_log = log;
		_globalMEType = globalMEType;
		_maFilter = maFilter;
		_configFile = configFile;
	}

	private Collection<PartConfig> fetchAttributeDefinition() {
		BinaryData configFile = FileManager.getInstance().getData(_configFile);
		ModelConfig config = DefinitionReader.readElementConfig(configFile, null, true);
		ObjectTypeConfig meConfiguration = searchInterface(config);
		if (meConfiguration == null) {
			_log.error("No global definition of ME '" + _globalMEType + "' in file '" + _configFile + "'.");
			return Collections.emptyList();

		}
		return meConfiguration.getAttributes();
	}

	private ObjectTypeConfig searchInterface(ModelConfig config) {
		for (ModuleConfig module : config.getModules()) {
			ObjectTypeConfig typeConfig = ElementConfigUtil.getObjectType(module, _globalMEType);
			if (typeConfig != null) {
				return typeConfig;
			}
		}
		return null;
	}

	/**
	 * Adapts the {@link TLStructuredTypePart}s.
	 */
	public void changeMetaAttributes() {
		ModuleUtil.INSTANCE.inModuleContext(MetaElementFactory.Module.INSTANCE, new Computation<Void>() {

			@Override
			public Void run() {
				internalRewrite();
				return null;
			}
		});
	}

	void internalRewrite() {
		_meAttributes = fetchAttributeDefinition();
		TLClass me = MetaElementFactory.getInstance().getGlobalMetaElement(_globalMEType);
		for (PartConfig partConfig : _meAttributes) {
			String maName = partConfig.getName();
			if (_maFilter != ALL_META_ATTRIBUTES && !_maFilter.accept(maName)) {
				continue;
			}
			TLStructuredTypePart ma = MetaElementUtil.getLocalMetaAttributeOrNull(me, maName);
			if (ma == null) {
				continue;
			}
			ma.tSetDataBoolean(KBBasedMetaAttribute.MANDATORY, partConfig.getMandatory());
		}
	}

	/**
	 * Adapts the {@link TLStructuredTypePart} of the given {@link TLClass} by setting the values as
	 * given in the given configuration.
	 * 
	 * @param maFilter
	 *        Filter matching the attribute names that must be "re-configured"
	 * @param globalMEType
	 *        Name of the global meta element holding the attributes.
	 * @param configFile
	 *        The configuration file of the factory defining the given meta element.
	 */
	public static void changeMetaAttributes(Protocol log, Filter<? super String> maFilter, String globalMEType,
			String configFile) {
		new UpdateMetaAttributes(log, maFilter, globalMEType, configFile).changeMetaAttributes();
	}

	/**
	 * Adapts all {@link TLStructuredTypePart} of the given {@link TLClass}.
	 * 
	 * @see #changeMetaAttributes(Protocol, Filter, String, String)
	 */
	public static void changeAllMetaAttributes(Protocol log, String globalMEType, String configFile) {
		changeMetaAttributes(log, ALL_META_ATTRIBUTES, globalMEType, configFile);
	}

}

