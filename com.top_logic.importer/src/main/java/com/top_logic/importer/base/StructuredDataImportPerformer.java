/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.simple.GenericDataObject;
import com.top_logic.importer.handler.structured.AbstractDOImportHandler;
import com.top_logic.importer.xml.XMLFileImportParser;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Import performer for structures (like those defined in XML files).
 *
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class StructuredDataImportPerformer<C extends StructuredDataImportPerformer.Config> extends AbstractImportPerformer<C> {

	/**
	 * Configuration for a StructuredDataImportPerformer.
	 * 
	 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public interface Config extends AbstractImportPerformer.Config {

        /**
		 * Tag name (from XML2DOParser.TagConfig) representing the root node in the import (must be
		 * same as
		 * {@link com.top_logic.importer.excel.ExcelStructureImportParser.Config#getRootName()} when
		 * defined there).
		 */
        @Mandatory
        String getRootName();

        /** Instructions how to handle the different tag names. */
        @InstanceFormat
		@Key(XMLFileImportParser.MappingConfig.NAME_ATTRIBUTE)
        Map<String, AbstractDOImportHandler<?, ?>> getMappings();
    }

    /** Key in the value map for the {@link DataObject}. */
    public static final String VALUE = "_value";

    /** Number of tags visited in the XML2DOParser. */
    public static final String COUNTER = "_counter";

    /** Map the DataObject types to commands. */
    private final Map<String, AbstractDOImportHandler<?, ?>> responsibleType;

    /** Name of the root object in the import structure. */
    private final String rootName;

    /** 
     * Creates a {@link StructuredDataImportPerformer}.
     */
    public StructuredDataImportPerformer(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);

        this.rootName        = aConfig.getRootName();
        this.responsibleType = aConfig.getMappings();
    }

    @Override
    protected List<Map<String, Object>> prepareImporter(Map<String, Object> someData) {
        List<Map<String, Object>> theResult  = super.prepareImporter(someData);
        Object                    theCounter = someData.get(StructuredDataImportPerformer.COUNTER);

        if (theCounter instanceof Integer) { 
            this._stepSize.set(((Integer) theCounter) + 2);
        }

        return theResult;
    }

    @Override
	public ImportResult doImport(List<Map<String, Object>> someValues, Transaction aTX) throws Exception {
        Map<String, Object>   currentObjects = new HashMap<>();
        StructureImportResult theResult      = new StructureImportResult(aTX);

        for (Map<String, Object> theValueMap : someValues) {
        	Object theModel = theValueMap.get(AbstractImportPerformer.MODEL);

        	if (this.isRoot(theModel)) {
        	    currentObjects.put(this.rootName, theModel);
        	    currentObjects.put(AbstractDOImportHandler.CURRENT_OBJECT_KEY, theModel);

	        	GenericDataObjectWithChildren theDO = (GenericDataObjectWithChildren) theValueMap.get(StructuredDataImportPerformer.VALUE);
	        	this.importDataObject(theDO, theResult, currentObjects);
        	}

			_current.incrementAndGet();
        }

        this.writeImportFinishedMessage(theResult.createdObjects.size(), theResult.updatedObjects.size());

        return theResult;
	}

	/**
	 * Check, if the given model can act as root to this performer.
	 * 
	 * @param aModel
	 *        The model to be checked.
	 * @return true when it is the root element.
	 */
	protected boolean isRoot(Object aModel) {
		return (aModel instanceof Wrapper)
			&& this.getConfig().getObjectProvider().supportsModel((Wrapper) aModel);
	}

	/**
	 * Import a {@link GenericDataObjectWithChildren}.
	 * 
	 * <p>
	 * Handles its attributes and children (recursively).
	 * </p>
	 * 
	 * @param aDO
	 *        The DataObject. Must not be <code>null</code>.
	 * @param aResult
	 *        Result of this import to be filled. Must not be <code>null</code>.
	 * @param someCachedObjects
	 *        Map with objects already visited.
	 */
    protected void importDataObject(GenericDataObjectWithChildren aDO, StructureImportResult aResult, Map<String, Object> someCachedObjects) {
        MOStructureImpl               theMO  = (MOStructureImpl) aDO.tTable();
        AbstractDOImportHandler<?, ?> theCmd = this.responsibleType.get(theMO.getName());

        if (theCmd != null) {
            ResKey theKey = theCmd.execute(someCachedObjects, new ImportValueProvider.DefaultImportValueProvider(aDO), aResult);

            this._current.incrementAndGet();

            if (theKey != null) {
                this.addInfoMessage(theKey);
            }

            for (GenericDataObjectWithChildren theChild : aDO.getChildren()) {
                // Occurred when a tag has been rejected by a filter. 
                if (theChild != null) { 
                    this.importDataObject(theChild, aResult, someCachedObjects);
                }
            }
        }
    }

    /**
     * Structure importer specific implementation of ImportResult. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class StructureImportResult extends ImportResult {

        // Attributes

		/** Set of new created objects. */
        public final Set<Object> createdObjects;

		/** Set of updated objects. */
        public final Set<Object> updatedObjects;

        // Constructors

        /** 
         * Creates a {@link StructureImportResult}.
         */
        public StructureImportResult(Transaction aTX) {
            super(aTX);

            this.createdObjects = new HashSet<>();
            this.updatedObjects = new HashSet<>();
        }

        // Public methods

		/**
		 * Add the given object as new created (and remove it from the set of updated once).
		 * 
		 * @param aCreated
		 *        The new created object.
		 * @param someRawValues
		 *        The values of the new created object as handed over by the parser.
		 */
        public void addCreated(Object aCreated, Map<String, Object> someRawValues) {
            if (someRawValues != null) {
                this.put(aCreated, someRawValues);
            }

            this.createdObjects.add(aCreated);
            this.updatedObjects.remove(aCreated);
        }
        
		/**
		 * Add the given object to the set of updated.
		 * 
		 * @param anUpdated
		 *        The updated object.
		 * @param someRawValues
		 *        The values of the updated object as handed over by the parser.
		 */
        public void addUpdated(Object anUpdated, Map<String, Object> someRawValues) {
            if (someRawValues != null) {
                this.put(anUpdated, someRawValues);
            }

            this.updatedObjects.add(anUpdated);
        }
    }

    /**
     * A DataObject that has an additional method to get child DataObjects.
     *
     * @author    <a href="mailto:kbu@top-logic.com">kbu</a>
     */
    @SuppressWarnings("serial")
    public static class GenericDataObjectWithChildren extends GenericDataObject {
        /** the children DataObjects. */
        private List<GenericDataObjectWithChildren> children;

        private GenericDataObjectWithChildren _parent;
        private AtomicInteger _level = new AtomicInteger(-1);


        /**
         * Creation with all values for super class and the children.
         */
        public GenericDataObjectWithChildren(Map<String, Object> aMap, String aType, TLID anID, List<GenericDataObjectWithChildren> someChildren) {
            super(aMap, aType, anID);

            children = someChildren;
            for (GenericDataObjectWithChildren child : someChildren) {
				if (child != null) {
					child._parent = this;
				}
			}
        }
        
        /**
         * the children. May be <code>null</code> or empty.
         */
        public List<GenericDataObjectWithChildren> getChildren() {
            return children;
        }
        
		/** 
		 * get ancestor of given type.
		 * 
		 * @param ancestoreType Requested type of DataObject ancestor
		 * @return the first ancestor within the upward hierarchy of given type. In case there is none, null is returned.
		 */
		public GenericDataObjectWithChildren getAncestor(String ancestoreType) {
			if(StringServices.equalsCharSequence(getMetaObjectName(), ancestoreType)) {
				return this;
			}
			if (_parent != null) {
				if (StringServices.equals(_parent.getMetaObjectName(), ancestoreType)) {
					return _parent;
				}
				return _parent.getAncestor(ancestoreType);
			}
			return null;
		}

		/**
		 * parent if any. Returns <code>null</code> if this Object is the root.
		 */
		public GenericDataObjectWithChildren getParent() {
			return _parent;
		}

		/**
		 * Check whether a super type until the number of <code>upSteps</code> steps above this DO
		 * matches the given Pattern.
		 * 
		 * @param upSteps
		 *        Number of steps to go upwards
		 * @param expected
		 *        Filter to check for the expected type.
		 * @return true in case there is an ancestor within the range of upper levels given by
		 *         <code>upSteps</code> of the requested type.
		 */
		public boolean isBelowUntil(long upSteps, TypedFilter expected) {
			final String type = this.getMetaObjectName();
			if (FilterResult.TRUE== expected.matches(type)) {
				return true;
			}
			if (_parent == null) {
				return false;
			}
			if (upSteps > 0) {
				return _parent.isBelowUntil(upSteps - 1, expected);

			}
			return false;

		}
		
		/**
		 * the level of this DataObject within it's hierarchy.
		 */
		public int getLevel() {
			if (!(_level.get() < 0)) {
				return (_level.get());
			}
			if (_parent == null) {
				_level.set(0);
				return 0;
			}
			_level.set(_parent.getLevel() + 1);
			return _level.get();
		}
    }
}
