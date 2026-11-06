/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.InterfacesCategory;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.AbstractImportPerformer;

/**
 * Provides the configured data importers, each combining a parser and a performer.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
@Label("Data importers")
@ServiceCategory.Of(InterfacesCategory.class)
public class ImporterService extends ConfiguredManagedClass<ImporterService.Config> {
    
    /**
     * Configuration options for {@link ImporterService}.
     *
     * @author    <a href="mailto:mga@top-logic.com">mga</a>
     */
	public interface Config extends ConfiguredManagedClass.Config<ImporterService> {

        /** The available importers by their name. */
        @Mandatory
        @Key(ImporterConfig.NAME_ATTRIBUTE)
        Map<String, ImporterConfig> getImporters();
    }

    /**
     * Configuration for an importer providing a parser and a performer. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface ImporterConfig extends NamedConfiguration {

        /** Parser for reading the data from an external source (file) and converting it into a generic import format. */
        @Mandatory
		AbstractImportParser.Config getParser();

        /** Performer for create business objects from the generic import format. */
        @Mandatory
		AbstractImportPerformer.Config getPerformer();
    }

    /** 
     * Creates a {@link ImporterService}.
     */
    public ImporterService(InstantiationContext aContext, Config aConfig) {
        super(aContext, aConfig);
    }

    /** The sorted names of all configured importers. */
    public List<String> getImporterNames() {
        List<String> theKeys = new ArrayList<>(this.getConfig().getImporters().keySet());

        Collections.sort(theKeys);

        return theKeys;
    }

	/**
	 * Creates the parser of the importer with the given name.
	 *
	 * @param aName
	 *        the name of the importer
	 * @return a new instance of the parser of the importer
	 */
    public AbstractImportParser getParser(String aName) {
		AbstractImportParser parser = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(getConfig().getImporters().get(aName).getParser());
		parser.setName(aName);
		return parser;
    }

	/**
	 * Creates the performer of the importer with the given name.
	 *
	 * @param aName
	 *        the name of the importer
	 * @return a new instance of the performer of the importer
	 */
    public AbstractImportPerformer getPerformer(String aName) {
    	return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(getConfig().getImporters().get(aName).getPerformer());
    }

    /** The {@link ImporterService} instance. */
    public static ImporterService getInstance() {
        return Module.INSTANCE.getImplementationInstance();
    }

    /** Module for the {@link ImporterService}. */
    public static class Module extends TypedRuntimeModule<ImporterService> {

        /** Singleton {@link ImporterService.Module} instance. */
        public static final Module INSTANCE = new Module();

        private Module() {
            // singleton instance
        }

        @Override
        public Class<ImporterService> getImplementation() {
            return ImporterService.class;
        }
    }
}

