/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.genericimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.genericimport.AbstractGenericDataImportBase;
import com.top_logic.element.genericimport.GenericDataImportConfiguration;
import com.top_logic.element.genericimport.GenericDataImportConfiguration.ColumnAttributeMapping;
import com.top_logic.element.genericimport.GenericDataImportHelper;
import com.top_logic.element.genericimport.MetaElementBasedImportBase;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.genericimport.interfaces.GenericDataImportConfigurationAware;
import com.top_logic.element.genericimport.interfaces.GenericImporter;
import com.top_logic.element.genericimport.interfaces.GenericValueMap;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.UniqueValuesDependency;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/** 
 * This component is used to configure a {@link GenericDataImportConfiguration}
 *
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportConfigComponent extends FormComponent implements GenericDataImportConfigurationAware {

    public interface Config extends FormComponent.Config {
		@Name(XML_CONF_MAX_ROWS)
		@IntDefault(3)
		int getRowSize();

		@Name(XML_CONF_ALLOW_CREATE)
		@BooleanDefault(false)
		boolean getAllowCreate();

		@Name(XML_CONF_ALLOW_UPDATE)
		@BooleanDefault(false)
		boolean getAllowUpdate();

		@Name(XML_CONF_ALLOW_COMMIT)
		@BooleanDefault(false)
		boolean getAllowCommit();
	}

	public static final String META_ELEMENT_SELECT_FIELD = "metaElement";

    public static final String FKEY_SELECT_FIELD         = "fkeySel";

    public static final String META_ATTRIBUTE_TABLE      = "gdiTable";

    public static final String HEADER_SELECT_TYPE        = "maSelect";

    public static final String HEADER_ERROR_TYPE         = "headerError";

    public static final String XML_CONF_MAX_ROWS         = "rowSize";
    public static final String XML_CONF_ALLOW_CREATE     = "allowCreate";
    public static final String XML_CONF_ALLOW_UPDATE     = "allowUpdate";
    public static final String XML_CONF_ALLOW_COMMIT     = "allowCommit";

    // MetaElements of object that can be imported here
    private List<TLClass> metaElements;

    private String[] columnIDs;

    private int rowSize;

    /**
     * Whether the user can overwrite the configured value of {@link GenericDataImportConfiguration#isDoCreate()} or not. 
     */
    private boolean allowCreate;
    /**
     * Whether the user can overwrite the configured value of {@link GenericDataImportConfiguration#isDoUpdate()} or not. 
     */
    private boolean allowUpdate;
    /**
     * Whether the user can overwrite the configured value of {@link GenericDataImportConfiguration#isDoCommit()} or not.
     */
    private boolean allowCommit;

    /**
     * Creates a {@link GenericDataImportConfigComponent}.
     */
    public GenericDataImportConfigComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);

        this.rowSize = atts.getRowSize();

        this.allowCreate = atts.getAllowCreate();
        this.allowUpdate = atts.getAllowUpdate();
        this.allowCommit = atts.getAllowCommit();
    }

    @Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

	@Override
	public FormContext createFormContext() {
		FormContext theContext = new FormContext("ctx", I18NConstants.IMPORT_CONFIG);

        GenericDataImportConfiguration theConf = this.getImportConfiguration();
        List<TLClass> theMetas = getMetaElements();
        if (! theMetas.isEmpty()) {
            TLClass theMeta = theMetas.iterator().next();

            // field for meta element selection
            SelectField theMetaElementField = this.addMetaElementSelectField(theContext, theMetas, theMeta);

            SelectField theFKeySelect = this.addFKeySelectField(theContext);

            // fields for commit, update and create
            BooleanField theCommit = FormFactory.newBooleanField(GenericDataImportConfiguration.DO_COMMIT, Boolean.valueOf(theConf.isDoCommit()), ! this.allowCommit);
            theCommit.setVisible(this.allowCommit);
            theContext.addMember(theCommit);

            BooleanField theUpdate = FormFactory.newBooleanField(GenericDataImportConfiguration.DO_UPDATE, Boolean.valueOf(theConf.isDoUpdate()), ! this.allowUpdate);
            theUpdate.setVisible(this.allowUpdate);
            theContext.addMember(theUpdate);

            BooleanField theCreate = FormFactory.newBooleanField(GenericDataImportConfiguration.DO_CREATE, Boolean.valueOf(theConf.isDoCreate()), ! this.allowCreate);
            theCreate.setVisible(this.allowCreate);
            theContext.addMember(theCreate);

            // build the table model for column - attribute config
            String[] theColumns = this.getColumnIDs();
            TableConfiguration theManager = TableConfiguration.table();
			theManager.getDefaultColumn().setAccessor(new MappedDOAccessor());

            String[] theColumnLabels = getColumLabels();
            for (int i=0; i<theColumns.length; i++) {
                String theName = String.valueOf(i);
                ColumnConfiguration theDesc = theManager.declareColumn(theName);
                theDesc.setHeadControlProvider(HeaderControlProvider.INSTANCE);
                theDesc.setColumnLabel(theColumnLabels[i]);
            }

			ObjectTableModel theModel = new ObjectTableModel(theColumns, theManager, getPlainObjects());

            FormTableModel theTable  = new FormTableModel(theModel, theContext);
            List<TLStructuredTypePart> theMetaAttributes   = this.getSupportedMetaAttributes(theMeta);

            List<FormField> theDependend = new ArrayList<>(theColumns.length);
            
            for (int i=0; i<theColumns.length; i++) {
                FormContainer theContainer = theTable.getColumnGroup(i);
                SelectField   theAttributeField = FormFactory.newSelectField(HEADER_SELECT_TYPE, theMetaAttributes, false, Collections.EMPTY_LIST, false);
				theAttributeField.setOptionComparator(LabelComparator.newCachingInstance());
                theDependend.add(theAttributeField);

                TLStructuredTypePart theAttr = this.searchMetaAttribute(theMetaAttributes, theColumnLabels[i]);
                if (theMeta != null) {
                	theAttributeField.setAsSingleSelection(theAttr);
                }

                theContainer.addMember(theAttributeField);
            }

            theFKeySelect.addConstraint(new ForeignKeyConstraint(theDependend, theTable));

            UniqueValuesDependency theDep = new UniqueValuesDependency(theDependend.toArray(new FormField[]{}), true);
            theDep.attach();

            theMetaElementField.addValueListener(new MetaElementSelectionListener(theDependend));

            TableField theTableField = FormFactory.newTableField(META_ATTRIBUTE_TABLE, theTable);
            theTableField.setSelectable(false);
            theContext.addMember(theTableField);
        }


        return theContext;
    }

    private TLStructuredTypePart searchMetaAttribute(List<TLStructuredTypePart> someMetas, String anAttributeName) {
    	for(TLStructuredTypePart theMeta : someMetas) {
    		if (anAttributeName.equals(theMeta.getName())) {
    			return theMeta;
    		}
    	}
    	return null;
    }

    private List<TLClass> getMetaElements()  {
        if (this.metaElements == null || this.metaElements.isEmpty()) {
            Set theTypes = this.getImportConfiguration().getInternalTypes();
            this.metaElements = new ArrayList<>(theTypes.size());
            for (Iterator theIter = theTypes.iterator(); theIter.hasNext();) {
                String theType = (String) theIter.next();
                this.getImportConfiguration().getConverter(theType);
                
                TLClass meta = MetaElementBasedImportBase.getUniqueMetaElement(theType);
                if (meta == null) {
					throw new ConfigurationError("Type '" + theType + "' does not exist.");
                }
                this.metaElements.add(meta);
            }
        }
        return this.metaElements;
    }

    private GenericDataImportHelper getImportHelper() {
        return (GenericDataImportHelper) this.getModel();
    }

    private GenericImporter getImporter() {
        return this.getImportConfiguration().getImporter();
    }

    private String[] getColumnIDs() {
        if (this.columnIDs == null) {
            String[] theCols = this.getColumLabels();
            this.columnIDs   = new String[theCols.length];
            for (int i=0; i<theCols.length; i++) {
                this.columnIDs[i] = String.valueOf(i);
            }
        }
        return this.columnIDs;
    }

    private String[] getColumLabels() {
        return this.getImporter().getColumns();
    }

    private List getPlainObjects() {
        try {
            List theList = this.getImporter().getPlainObjects();

            if (theList.size() > this.rowSize) {
                return theList.subList(0, this.rowSize);
            }
            return theList;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
	public GenericDataImportConfiguration getImportConfiguration() {
        return getImportHelper().getImportConfiguration();
    }

    @Override
	public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
        GenericImporter theImporter = this.getImporter();
        if (theImporter instanceof GenericDataImportConfigurationAware) {
            return ((GenericDataImportConfigurationAware) theImporter).setImportConfiguration(aConfig, anInternalType);
        }
        return false;
    }

    private void setModel(GenericDataImportConfiguration aConfig) {
		this.removeFormContext();
        this.metaElements = null;
        this.columnIDs = null;
    }
    
    private List<TLStructuredTypePart> getSupportedMetaAttributes(TLClass metaElement) {

        if (metaElement == null) {
            return Collections.emptyList();
        }

		Set configuredAttributes = this.getImportConfiguration().getAttributes(metaElement.getName());
        List<TLStructuredTypePart> supportedAttributes = new ArrayList<>(configuredAttributes.size());
        for (Iterator theIter = configuredAttributes.iterator(); theIter.hasNext();) {
            String theAttr = (String) theIter.next();

			// Configured attributes may contain attributes of more concrete elements. These
			// attributes are filtered here
			if (MetaElementUtil.hasMetaAttribute(metaElement, theAttr)) {
				try {
					supportedAttributes.add(MetaElementUtil.getMetaAttribute(metaElement, theAttr));
				} catch (NoSuchAttributeException e) {
					throw new UnreachableAssertion(e);
				}
            }
        }
        
        return supportedAttributes;
    }

    private SelectField addMetaElementSelectField(FormContext aContext, List allMetas, TLClass aSelected) {
        SelectField theField = FormFactory.newSelectField(META_ELEMENT_SELECT_FIELD, allMetas, false, Collections.singletonList(aSelected), false);
        theField.setMandatory(true);
        aContext.addMember(theField);
        return theField;
    }

    private SelectField addFKeySelectField(FormContext aContext) {
        SelectField theField = FormFactory.newSelectField(FKEY_SELECT_FIELD, Arrays.asList(this.getColumnIDs()), false, Collections.EMPTY_LIST, false);
        theField.setMandatory(true);
        theField.setOptionLabelProvider(new LabelProvider() {
            @Override
			public String getLabel(Object aObject) {
                int idx = Integer.parseInt((String) aObject);
                return getColumLabels()[idx];
            }
        });
        aContext.addMember(theField);
        return theField;
    }

    private static class ForeignKeyConstraint implements Constraint {

        private Collection<FormField>     dependent;
        private FormTableModel table;

        public ForeignKeyConstraint(Collection<FormField> dependendFields, FormTableModel aTable) {
            this.dependent = dependendFields;
            this.table     = aTable;
        }

        @Override
		public boolean check(Object aValue) throws CheckException {
            Collection<String> theColl = (Collection<String>) aValue;
            boolean    isError = theColl.isEmpty();
            if (! isError) {
                int idx = Integer.parseInt(theColl.iterator().next());
                SelectField theField = ((SelectField) this.table.getColumnGroup(idx).getMember(HEADER_SELECT_TYPE));
                isError = theField.getSingleSelection() == null;
            }
            if (isError) {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.ERROR_FOREIGN_KEY_NOT_MAPPED));
            }
            return true;
        }

        @Override
		public Collection<FormField> reportDependencies() {
            return this.dependent;
        }
    }

    private static class HeaderControlProvider implements ControlProvider {

        public static final ControlProvider INSTANCE = new HeaderControlProvider();

        @Override
		public Control createControl(Object aModel, String aStyle) {
            FormContainer theCont  = (FormContainer) aModel;
            FormField     theField = null;
            if (HEADER_SELECT_TYPE.equals(aStyle)) {
                theField = theCont.getField(aStyle);
				return new DropDownControl((SelectField) theField);
            }
            else if(HEADER_ERROR_TYPE.equals(aStyle)) {
                theField = theCont.getField(HEADER_SELECT_TYPE);
                return new ErrorControl(theField, true);
            }
            return null;
        }
    }

    /*package protected*/ class MappedDOAccessor implements Accessor {

        @Override
		public Object getValue(Object aObject, String aProperty) {
            try {
                String theProp = getColumLabels()[Integer.parseInt(aProperty)];
                return ((GenericValueMap) aObject).getAttributeValue(theProp);
            } catch (NoSuchAttributeException nsax) {
                Logger.error("Unable to get value!", nsax, this.getClass());
            }
            return null;
        }

        @Override
		public void setValue(Object aObject, String aProperty, Object aValue) {
            // do nothing
        }
    }

    private class MetaElementSelectionListener implements ValueListener {

        private Collection<FormField> attributeFields;

        public MetaElementSelectionListener(Collection<FormField> someFields) {
            this.attributeFields = someFields;
        }

        @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
            TLClass theME = CollectionUtil.getFirst((Collection<TLClass>) aNewValue);

            List<TLStructuredTypePart> theOptions = getSupportedMetaAttributes(theME);
            
            for (FormField field : this.attributeFields) {
                field.reset();
                ((SelectField) field).setOptions(theOptions);
            }
        }
    }

    public static class GenericDataImportConverterConfigHandler extends AJAXCommandHandler {

        public static final String COMMAND_ID = "configureGDIConverter";

		public static final CommandHandler INSTANCE = newInstance(GenericDataImportConverterConfigHandler.class,
			COMMAND_ID);

        public GenericDataImportConverterConfigHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {

            GenericDataImportConfigComponent theComp = (GenericDataImportConfigComponent) aComponent;

            FormContext theFC = theComp.getFormContext();

            if (theFC.checkAll()) {

                GenericDataImportConfiguration theConfig = theComp.getImportConfiguration();

                if (theFC.hasMember(META_ATTRIBUTE_TABLE)) {

                    adjustConfiguration(theComp, theFC, theConfig);

                    theConfig.setDoCommit(((BooleanField) theFC.getField(GenericDataImportConfiguration.DO_COMMIT)).getAsBoolean());
                    theConfig.setDoUpdate(((BooleanField) theFC.getField(GenericDataImportConfiguration.DO_UPDATE)).getAsBoolean());
                    theConfig.setDoCreate(((BooleanField) theFC.getField(GenericDataImportConfiguration.DO_CREATE)).getAsBoolean());

                }

                theComp.setImportConfiguration(theConfig, null);

                return HandlerResult.DEFAULT_RESULT;
            }
            else {
				return AbstractApplyCommandHandler.createErrorResult(theFC);
            }
        }

        private void adjustConfiguration(GenericDataImportConfigComponent aComp, FormContext aContext, GenericDataImportConfiguration aConfig) {

            TLClass    theMeta       = (TLClass) ((SelectField) aContext.getField(META_ELEMENT_SELECT_FIELD)).getSingleSelection();
            String         theType       = theMeta.getName();

            String         theFKeyS      = (String) ((SelectField) aContext.getField(FKEY_SELECT_FIELD)).getSingleSelection();
            int            theFKey       = Integer.parseInt(theFKeyS);
            String         theMappedFkey = null;

            FormTableModel theTable      = (FormTableModel) ((TableField) aContext.getMember(META_ATTRIBUTE_TABLE)).getTableModel();

            String[] theCols = aComp.getColumLabels();
            for (int i=0; i<theCols.length; i++) {
                FormContainer theContainer = theTable.getColumnGroup(i);
                String        theCol  = theCols[i];
                TLStructuredTypePart theMA   = (TLStructuredTypePart) ((SelectField) theContainer.getField(HEADER_SELECT_TYPE)).getSingleSelection();
                if (theMA != null) {
                    String theAttr = theMA.getName();

                    if (theFKey == i) {
                        theMappedFkey = theCol;
                    }

                    ColumnAttributeMapping theMapping = aConfig.getMappingForAttribute(theType, theAttr);

                    if (theMapping == null) {
                        theMapping = new ColumnAttributeMapping(theCol, theAttr, null);
                    }
                    else {
                        theMapping.setColumn(theCol);
                    }
                    aConfig.setMapping(theType, theMapping);
                }
            }

            // set foreign key
            aConfig.setForeignKey(theType, theMappedFkey);

//            MetaElementBasedTypeResolver theResolver = (MetaElementBasedTypeResolver) aConfig.getTypeResolver();
//            theResolver.setMetaElement(theMeta);
        }
    }

    public abstract static class GenericCacheBasedMapping extends AbstractGenericDataImportBase implements Mapping {

        GenericCache cache;
        String       type;

        public GenericCacheBasedMapping(String aType) {
            super(new Properties());
            this.type = aType;
        }

        @Override
		public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
            boolean theResult = super.setImportConfiguration(aConfig, anInternalType);
            if (theResult) {
                this.cache = this.getImportConfiguration().getCache();
            }
            return theResult;
        }

        @Override
		public Object map(Object aInput) {
            return this.cache.get(this.type, aInput);
        }
    }
}

