/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.gui.AbstractCreateAttributedCommandHandler;
import com.top_logic.importer.base.KeyMappingProvider.DefaultKeyMappingProvider;
import com.top_logic.importer.excel.ImportPerformerPostProcessor;
import com.top_logic.importer.excel.ImportPerformerPostProcessor.SimpleImportPerformerPostProcessor;
import com.top_logic.importer.text.TextFileImportParser;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.assistent.EVAAssistantController;

/**
 * Import values provided by a {@link TextFileImportParser}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ListDataImportPerformer<C extends ListDataImportPerformer.Config> extends AbstractImportPerformer<C> {

    /**
     * Configuration for a ListDataImportPerformer. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends AbstractImportPerformer.Config {

		/**
		 * Handler for {@link ListDataImportPerformer#lookupObject(Map, Wrapper, Map) generating
		 * IDs}.
		 */
        @InstanceFormat
        @InstanceDefault(DefaultKeyMappingProvider.class)
        KeyMappingProvider getKeyMappingProvider();

        /** Optional post processor when performer is finished. */
        @InstanceFormat
        @InstanceDefault(SimpleImportPerformerPostProcessor.class)
        ImportPerformerPostProcessor getPostProcessor();

		@Name("lookupOnHandlerResponsibility")
		@BooleanDefault(false)
		boolean isLookupOnHandlerResponsibility();
    }

    /**
     * Configuration of one fixed value column.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface MappingConfig extends NamedConfiguration {

        /** Name of the attribute to import the values to. */
        String getAttribute();
    }

    /** List of {@link String} IDs to create a unique ID from. */
    public static final String PROP_KEY_MAPPING = "_keyMapping";

    private List<String> keyMapping;

    /** 
     * Called by {@link EVAAssistantController}.
     */
    public ListDataImportPerformer(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void importData(Map<String, Object> someData) throws Exception {
        this.keyMapping = (List<String>) someData.get(ListDataImportPerformer.PROP_KEY_MAPPING);

        if (CollectionUtil.isEmptyOrNull(this.keyMapping)) {
            Logger.info("No key mapping found, update of objects not supported!", ListDataImportPerformer.class);
        }

        super.importData(someData);
    }

    @Override
    public ImportResult doImport(List<Map<String, Object>> someValues, Transaction aTX) throws Exception {
        CommandHandlerFactory                  theFactory = CommandHandlerFactory.getInstance();
        AbstractCreateAttributedCommandHandler theHandler = (AbstractCreateAttributedCommandHandler) theFactory.getHandler(this.getCreateHandlerName());
        Map<Object, Map<String, Wrapper>>   theObjects = new HashMap<>();
        ImportResult                           theResult  = new ImportResult(aTX);

        int countObjectsCreated = 0;
        int countObjectsUpdated = 0;
        _current.incrementAndGet();

        for (Map<String, Object> theValueMap : someValues) {
            Wrapper theModel  = (Wrapper) theValueMap.get(AbstractImportPerformer.MODEL);

            if (this.getConfig().getObjectProvider().supportsModel(theModel)) {
                Wrapper theObject = this.lookupObject(theValueMap, theModel, theObjects);

                if (getConfig().isLookupOnHandlerResponsibility() || theObject == null) {
                    theObject = this.createObject(theHandler, theValueMap, theModel);

                    if (theObject != null) {
                        this.postProcessObject(theObject, theValueMap, theModel, true);
						this.addInfoMessage(
							I18NConstants.OBJECT_CREATED.fill(MetaLabelProvider.INSTANCE.getLabel(theObject)));
                        this.add2KnownAttributed(theObject, theObjects.get(theModel));

                        countObjectsCreated++;
                    }
                }
                else if (this.updateObject(theObject, theValueMap, theModel)) {
                    this.postProcessObject(theObject, theValueMap, theModel, false);
					this.addInfoMessage(
						I18NConstants.OBJECT_UPDATED.fill(MetaLabelProvider.INSTANCE.getLabel(theObject)));

                    countObjectsUpdated++;
                }

                theResult.put(theObject, theValueMap);
            }
            else {
                this.addInfoMessage(I18NConstants.MODEL_NOT_SUPPORTED, MetaLabelProvider.INSTANCE.getLabel(theModel));
            }

            _current.incrementAndGet();
        }

        this.writeImportFinishedMessage(countObjectsCreated, countObjectsUpdated);

        return theResult;
    }

    /** 
     * Return a unique ID from the given values by combining them as described in {@link #keyMapping}.
     *
     * <p>This method must return the same ID as {@link #getID(Wrapper)} for same data/object combination!</p>
     * 
     * @param    someValues    The values to get the unique ID from.
     * @return   The requested ID.
     * @see      #getID(Wrapper)
     */
    protected String getID(Map<String, Object> someValues) {
        return this.getConfig().getKeyMappingProvider().getID(this.keyMapping, someValues);
    }

    /** 
     * Return a unique ID from the given {@link Wrapper} by combining them as described in {@link #keyMapping}.
     *
     * <p>This method must return the same ID as {@link #getID(Map)} for same data/object combination!</p>
     * 
     * @param    anAttributed    The attributed to get the unique ID from.
     * @return   The requested ID.
     * @see      #getID(Map)
     */
    protected String getID(Wrapper anAttributed) {
        return this.getConfig().getKeyMappingProvider().getID(this.keyMapping, anAttributed);
    }

    /**
	 * Return the name of the {@link AbstractCreateAttributedCommandHandler} to be used when
	 * importing the values.
	 * 
	 * @return The requested name of the importer, must not be <code>null</code>
	 * @see #doImport(List, Transaction)
	 */
    protected String getCreateHandlerName() {
        return this.getConfig().getCreateCommandHandler();
    }

    /**
	 * Do a look up in the objects this importer supports and try to find the object described by
	 * the given map.
	 * 
	 * <p>
	 * Depending on the result of this method a new object will be created (via
	 * {@link #createObject(AbstractCreateAttributedCommandHandler, Map, Wrapper)}) or the returned
	 * will be updated (via {@link #updateObject(Wrapper, Map, Wrapper)}).
	 * </p>
	 * 
	 * @param someValues
	 *        Map with values describing the object to be imported.
	 * @param aModel
	 *        Model we work on.
	 * @param someCachedValues
	 *        Map of objects provided by {@link #initObjectMap(Wrapper)}.
	 * @return The matching object or <code>null</code>.
	 */
    protected Wrapper lookupObject(Map<String, Object> someValues, Wrapper aModel, Map<Object, Map<String, Wrapper>> someCachedValues) {
        Map<String, Wrapper> theMap = someCachedValues.get(aModel);

        if (theMap == null) {
            theMap = this.initObjectMap(aModel);

            someCachedValues.put(aModel, theMap);
        }

        return theMap.get(this.getID(someValues));
    }

    /** 
     * Initialize a map of values as provided by {@link #getExistingObjects(Wrapper)}
     * for the given model.
     * 
     * @param    aModel    The model to get the cache for.
     * @return   The requested cache.
     */
    protected Map<String, Wrapper> initObjectMap(Wrapper aModel) {
        Map<String, Wrapper> theMap = new HashMap<>();

        for (Wrapper theObject : this.getExistingObjects(aModel)) {
            String theID = this.getID(theObject);

            if (!StringServices.isEmpty(theID)) { 
                theMap.put(theID, theObject);
            }
        }

        return theMap;
    }

    /** 
     * Add the new created object to the map of known objects.
     * 
     * @param    anObject    The attributed object to be added.
     * @param    aMap        The local cache to add the given object to.
     */
    protected void add2KnownAttributed(Wrapper anObject, Map<String, Wrapper> aMap) {
        aMap.put(this.getID(anObject), anObject);
    }

    /** 
     * Create the object out of the given values.
     * 
     * @param    aHandler      The create handler to build the new object, must not be <code>null</code>.
     * @param    someValues    The values to create the object from, must not be <code>null</code>.
     * @param    aModel        The model in which context the creation will take place, must not be <code>null</code>.
     * @return   The new created object or <code>null</code>, when creation failed.
     */
    protected Wrapper createObject(AbstractCreateAttributedCommandHandler aHandler, Map<String, Object> someValues, Wrapper aModel) {
        return aHandler.createNewObjectFromMap(someValues, aModel);
    }

    /**
	 * Update the given object with values held in the given map.
	 * 
	 * @param anObject
	 *        Object to be updated.
	 * @param someValues
	 *        Map with values describing the object to be imported.
	 * @param aModel
	 *        Model we work on.
	 * @return <code>true</code> when the object has been updated.
	 */
    protected boolean updateObject(Wrapper anObject, Map<String, Object> someValues, Wrapper aModel) {
        TLClass theME     = (TLClass) anObject.tType();
        boolean     theResult = false;

        for (Entry<String, Object> theEntry : someValues.entrySet()) {
            if (MetaElementUtil.hasMetaAttribute(theME, theEntry.getKey())) {
                theResult = this.updateValue(anObject, theEntry) || theResult;
            }
        }

        return theResult;
    }

    /** 
     * Update a single value in the given {@link Wrapper}.
     * 
     * @param    anObject    The object to be updated.
     * @param    anEntry     The entry describing the meta attribute to be updated.
     * @return   <code>true</code> always.
     */
    protected boolean updateValue(Wrapper anObject, Entry<String, Object> anEntry) {
		return AbstractImportPerformer.setValue(anObject, anEntry.getKey(), anEntry.getValue());
    }

    /**
	 * Call to the {@link Config#getPostProcessor()} after creating or updating an attributed.
	 * 
	 * @param anAttributed
	 *        object which has been created or updated.
	 * @param someValues
	 *        the value map. Must not be <code>null</code>.
	 * @param aModel
	 *        Model which is selected by the importer.
	 * @param created
	 *        <code>true</code> when given attributed has been created.
	 */
    protected void postProcessObject(Wrapper anAttributed, Map<String, Object> someValues, Wrapper aModel, boolean created) {
        this.getConfig().getPostProcessor().postProcessObject(anAttributed, someValues, aModel, created);
    }

    /** 
     * Return the objects already known by the system.
     * 
     * <p>This will be used to look up the attributes.</p>
     * 
     * @param    aModel    The model we are working on.
     * @return   Requested collection of known attributed objects which might to be imported.
     */
    protected Collection<? extends Wrapper> getExistingObjects(Wrapper aModel) {
        return this.getConfig().getObjectProvider().getObjects(aModel);
    }

    /** 
     * Return the relevant attribute names from the given IDs as defined by the given mapping.
     * 
     * @param    someMappings    The mapping to get attribute names for.
     * @param    someIDs         The requested column names from the mapping.
     * @return   The requested list of attribute names.
     */
    public static List<String> getIDsFromMapping(Collection<? extends ListDataImportPerformer.MappingConfig> someMappings, List<String> someIDs) {
        List<String>        theKeys = new ArrayList<>();
        Map<String, String> theMap  = new HashMap<>();

        for (ListDataImportPerformer.MappingConfig theMapping : someMappings) {
            theMap.put(theMapping.getName(), theMapping.getAttribute());
        }

        for (String theID : someIDs) {
            String theKey = theMap.get(theID);

            if (!StringServices.isEmpty(theKey)) { 
                theKeys.add(theKey);
            }
        }

        return theKeys;
    }
}

