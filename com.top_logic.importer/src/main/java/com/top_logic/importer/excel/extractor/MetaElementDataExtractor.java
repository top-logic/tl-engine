/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.importer.excel.transformer.BooleanTransformer;
import com.top_logic.importer.excel.transformer.DateTransformer;
import com.top_logic.importer.excel.transformer.DoubleTransformer;
import com.top_logic.importer.excel.transformer.FastListElementTransformer;
import com.top_logic.importer.excel.transformer.LongTransformer;
import com.top_logic.importer.excel.transformer.StringTransformer;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Extract values out of the definition of a meta element.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MetaElementDataExtractor<O extends TLStructuredTypePart, C extends MetaElementDataExtractor.Config> extends AbstractDataExtractor<O, C> {

	/**
	 * Configuration of the MetaElementDataExtractor.
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public interface Config extends AbstractDataExtractor.Config {

        /** Name of the global meta element to get the transformers for. */
        @Mandatory
        @Name("meta-element")
        String getMetaElement();
    }

    /** Label for an empty entry. */
    public static final String EMPTY_LABEL = "----";

    /** Meta element this instance is working on. */
    private TLClass metaElement;

    /** 
     * Creates a {@link MetaElementDataExtractor}.
     */
	public MetaElementDataExtractor(InstantiationContext aContext, C aConfig) {
		super(aContext, aConfig);
    }

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<O> getObjects() {
		return (Collection<O>) TLModelUtil.getMetaAttributes(this.getMetaElement());
	}

	@Override
	protected String getI18NName(O anObject) {
		return TLTypePartResourceProvider.INSTANCE.getLabel(anObject);
	}

	@Override
	protected Transformer<?> createInnerTransformer(O aMA) {
        Transformer<?> theTransformer = null;

        if (DisplayAnnotations.isEditable(aMA)) {
			switch (AttributeOperations.getMetaAttributeType(aMA)) {
				case LegacyTypeCodes.TYPE_BOOLEAN:
                    theTransformer = this.getBooleanTransformer(aMA);
                    break;
				case LegacyTypeCodes.TYPE_STRING:
                    theTransformer = this.getStringTransformer(aMA);
                    break;
				case LegacyTypeCodes.TYPE_FLOAT:
                    theTransformer = this.getDoubleTransformer(aMA);
                    break;
				case LegacyTypeCodes.TYPE_LONG:
                    theTransformer = this.getLongTransformer(aMA);
                    break;
				case LegacyTypeCodes.TYPE_DATE:
                    theTransformer = this.getDateTransformer(aMA);
                    break;
				case LegacyTypeCodes.TYPE_CLASSIFICATION:
                    theTransformer = this.getClassificationTransformer(aMA);
                    break;
                default:
            }
        }

		return theTransformer;
	}

    /**
	 * @see #createInnerTransformer(TLStructuredTypePart)
	 */
    protected Transformer<?> getBooleanTransformer(TLStructuredTypePart aMA) {
		return new BooleanTransformer(aMA.isMandatory());
    }

    /**
	 * @see #createInnerTransformer(TLStructuredTypePart)
	 */
    protected Transformer<?> getDoubleTransformer(TLStructuredTypePart aMA) {
        return new DoubleTransformer(aMA.isMandatory());
    }
    
    /**
	 * @see #createInnerTransformer(TLStructuredTypePart)
	 */
    protected Transformer<?> getLongTransformer(TLStructuredTypePart aMA) {
        return new LongTransformer(aMA.isMandatory());
    }
    
    /**
	 * @see #createInnerTransformer(TLStructuredTypePart)
	 */
    protected Transformer<?> getDateTransformer(TLStructuredTypePart aMA) {
        return new DateTransformer(aMA.isMandatory(), null, 0, EMPTY_LABEL);
    }

    /**
	 * @see #createInnerTransformer(TLStructuredTypePart)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    protected Transformer<?> getStringTransformer(TLStructuredTypePart aMA) {
		return new StringTransformer(aMA.isMandatory(), AttributeOperations.getUpperBound(aMA),
			CollectionUtil.createList("", EMPTY_LABEL), true, true);
    }

    /**
	 * @see #createInnerTransformer(TLStructuredTypePart)
	 */
    protected Transformer<?> getClassificationTransformer(TLStructuredTypePart aMA) {
		FastList theList = AttributeOperations.getClassificationList(aMA);

        return new FastListElementTransformer(aMA.isMandatory(), false, false, this.getClassificationMap(theList), null, theList.getName(), ',');
    }

    /** 
     * Return the meta element supported by this extractor.
     * 
     * @return    The requested meta element.
     */
    protected TLClass getMetaElement() {
        if (this.metaElement == null) {
            String theName = this.config.getMetaElement();

            try {
                this.metaElement = this.createMetaElement(theName);
            }
            catch (ConfigurationException ex) {
                throw new TopLogicException(MetaElementDataExtractor.class, "metaElement.create", new Object[] {theName}, ex);
            }
        }

        return this.metaElement;
    }

    /** 
     * Create the meta element with the given name.
     * 
     * @param    aName    Name of the requested meta element.
     * @return   The requested meta element.
     * @throws   ConfigurationException     When meta element cannot be found.
     */
    protected TLClass createMetaElement(String aName) throws ConfigurationException {
        TLClass theME = MetaElementFactory.getInstance().getGlobalMetaElement(aName);

        if (theME == null) {
            throw new ConfigurationException("Cannot find meta element named '" + aName + "' in global meta element factory!");
        }
        else { 
            return theME;
        }
    }

    private Map<String, String> getClassificationMap(FastList aList) {
        Map<String, String> theMap = new HashMap<>();

		{
            Resources theRes = Resources.getInstance();

            for (FastListElement theElement : aList.elements()) {
                theMap.put(theRes.getString(FastListElementLabelProvider.labelKey(theElement)), theElement.getName());
            }
        }

        return theMap;
    }
}

