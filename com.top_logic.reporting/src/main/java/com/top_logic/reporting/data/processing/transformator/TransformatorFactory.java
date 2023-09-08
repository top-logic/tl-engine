/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.processing.transformator;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Reloadable;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.reporting.data.processing.transformator.common.EntryExtractor;

/** 
 * Whats up with this class ?
 * 
 * All Transformators for use with this factory have to declare a 
 * Contructor with the pattern (String)
 * where the String is the SectionName of the configuration section 
 * containing the transformators parameters.
 * Each Transformator has to have such a configuration section. 
 * Each section has to contain an entry named "class".
 * This entry is used by this factory.
 *
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 *
 */
public class TransformatorFactory implements Reloadable {

	private static TransformatorFactory singleton = null;

	private List<EntryExtractor> theTransformators = null;

	/**
	 * Configuration for the {@link TransformatorFactory}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * Configuration for a specific transformator.
		 * 
		 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
		 */
		interface Transformator extends NamedConfigMandatory, PolymorphicConfiguration<EntryExtractor> {
			String getDisplayKey();

			String getIndices();
		}

		/**
		 * Multiple transformators.
		 */
		List<Transformator> getTransformators();
	}

	/**
	 * Constructs a new Transformerfactory
	 * The new Factory holds one instance of each transformator, configured in the xml properties
	 */
	protected TransformatorFactory() {
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);

		this.theTransformators = new ArrayList<>();

		for (Config.Transformator transformatorConfig : config.getTransformators()) {
			try {
				this.theTransformators
					.add(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(transformatorConfig));
			} catch (SecurityException | IllegalArgumentException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * the singleton instance of this factory
	 */
	public static synchronized TransformatorFactory getInstance() {
		if (singleton == null) {
			singleton = new TransformatorFactory();
		}
		return singleton;
	}



	/**
	 * all Transformators compatible with the given type and size
	 * @param aType - the type of the Values, the Transformators should be applied to
	 * @param aSize - the number of entries in these Values
	 */
	public TableTransformator[] getTransformatorsForTypeAndSize(Class aType, int aSize) {
		ArrayList res = new ArrayList();
		for (int i = 0; i < theTransformators.size(); i++) {
			TableTransformator tmp = (TableTransformator)theTransformators.get(i);
			Class requieredType = tmp.getType();
			int requieredDm = tmp.getRequiredValueSize();
			if (requieredType.isAssignableFrom(aType) && (requieredDm <= aSize)) {
				res.add(tmp);
			}
		}
		if (res.isEmpty()) {
			return null;
		}
		TableTransformator[] theResult = new TableTransformator[res.size()];
		res.toArray(theResult);
		return theResult;
	}


	//interface reloadable ...ab top-logic 3.1

	/**
	 * @see com.top_logic.basic.Reloadable#reload()
	 */	
	@Override
	public boolean reload(){
		removeSingleton();
		this.theTransformators = null;
		return true;
	}

	private static synchronized TransformatorFactory removeSingleton() {
		return singleton = null;
	}

	/**
	 * @see com.top_logic.basic.Reloadable#getName()
	 */	
	@Override
	public String getName(){
		return this.getClass().getName();
	}
	
	/**
	 * @see com.top_logic.basic.Reloadable#getDescription()
	 */	
	@Override
	public String getDescription(){
        return ("The known transformators for the reporting engine. These can " +
                "be used for performing transformations on variuos data in the " +
                "system.");
	}

	/**
	 * @see com.top_logic.basic.Reloadable#usesXMLProperties()
	 */	
	@Override
	public boolean usesXMLProperties(){
		return false;
	}
}
