/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.dispatching;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.importer.ImporterService;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.AssistentFileUploadComponent;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler;

/**
 * Upload component which allows selection of an importer to be used. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class DispatchingAssistentFileUploadComponent extends AssistentFileUploadComponent {

    /** Field for selecting the importer to be used here. */ 
    public static final String FIELD_IMPORTER = "_importer";

    /** Field for displaying the supported extensions. */ 
    public static final String FIELD_EXTENSIONS = "_extensions";

    private List<String> options;

    /** 
     * Creates a {@link DispatchingAssistentFileUploadComponent}.
     */
	public DispatchingAssistentFileUploadComponent(InstantiationContext context, Config atts)
			throws ConfigurationException {
		super(context, atts);
    }

    @Override
    public FormContext createFormContext() {
        FormContext theContext = super.createFormContext();

        theContext.addMember(this.getImporterField());
        theContext.addMember(this.getExtensionsField());

        return theContext;
    }

    @Override
    public ResKey checkFileName(String fileName) {
        if (this.hasFormContext()) {
            this.allowedExts = ArrayUtil.toStringArray(this.getExtensions());
        }

        return super.checkFileName(fileName);
    }

    public AbstractImportParser getParser() {
        return this.getController().getParser();
    }

    protected List<String> getExtensions() {
        return this.getController().getExtensions();
    }

    protected List<String> getOptions() {
        if (this.options == null) {
            this.options = this.createOptionList();
        }

        return this.options;
    }

    protected void setImporterName(String aValue) {
        this.getController().setImporterName(aValue);
    }

    protected DispatchingEVAAssistantController getController() {
        return (DispatchingEVAAssistantController) AssistentComponent.getAssistentController(this);
    }

    protected FormMember getImporterField() {
        List<String> theOptions = this.getOptions();
        SelectField  theField   = FormFactory.newSelectField(FIELD_IMPORTER, theOptions);

        theField.setMandatory(true);
		theField.setOptionLabelProvider(new I18NResourceProvider(I18NConstants.IMPORTER_NAME));
        theField.addValueListener(new ValueListener() {
            
            @Override
            public void valueChanged(FormField aField, Object oldValue, Object newValue) {
                DispatchingAssistentFileUploadComponent theComp    = DispatchingAssistentFileUploadComponent.this;
                FormContext                             theContext = aField.getFormContext();

                theComp.setImporterName((String) CollectionUtil.getSingleValueFrom(newValue));

                if (theContext != null) {
                    FormField theField = theContext.getField(FIELD_EXTENSIONS);

                    theField.setValue(StringServices.toString(theComp.getController().getExtensions(), ", "));
                }
            }
        });

        if (theOptions.size() == 1) {
            theField.initializeField(theOptions);
            theField.setImmutable(true);
        }

        return theField;
    }

    protected AbstractFormField getExtensionsField() {
        List<String> theOptions = this.getExtensions();
            
        return FormFactory.newStringField(FIELD_EXTENSIONS, StringServices.toString(theOptions, ", "), true);
    }

    protected List<String> createOptionList() {
        List<String> theNames = this.getController().getImporterNames();

        return (theNames == null) ? this.getImportService().getImporterNames() : theNames;
    }

    protected ImporterService getImportService() {
        return ImporterService.getInstance();
    }

    /**
     * Dispatcher for an import upload handler. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class DispatchingUploadHandler extends ValidatingUploadHandler {

        // Constructors
        
        /** 
         * Creates a {@link DispatchingUploadHandler}.
         */
		public DispatchingUploadHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        // Overridden methods from ValidatingUploadHandler

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
            DispatchingAssistentFileUploadComponent theComp    = (DispatchingAssistentFileUploadComponent) aComponent;
            FormContext                             theContext = theComp.getFormContext();

            if (theContext.checkAll()) {
				return super.handleCommand(aContext, aComponent, model, someArguments);
//                return this.getParser(theComp).handleCommand(aContext, aComponent, someArguments);
            }
            else {
                HandlerResult theResult = new HandlerResult();

                for (Iterator<? extends FormField> theIt = theContext.getDescendantFields(); theIt.hasNext(); ) {
                    FormField theField = theIt.next();

                    if (theField.hasError()) {
						theResult.addErrorMessage(theField.getLabel(), theField.getError());
                    }
                }

                return theResult;
            }
        }

        @Override
        protected List<ImportMessage> validateFile(AssistentComponent aComponent, File aFile) {
			return this.getParser(aComponent).validateFile(aComponent, BinaryDataFactory.createBinaryData(aFile));
        }

        // Protected methods

        /** 
         * Return the real upload handler to be used (where 
         * {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)} will be called).
         * 
         * @return   The requested upload handler, never <code>null</code>.
         */
        protected AbstractImportParser getParser(DispatchingAssistentFileUploadComponent aComp) {
            return aComp.getParser();
        }

        /** 
         * Return the real upload handler to be used (where 
         * {@link #validateFile(AssistentComponent, File)} will be called).
         * 
         * @return   The requested upload handler, never <code>null</code>.
         */
        protected AbstractImportParser getParser(AssistentComponent aComponent) {
            return ((DispatchingEVAAssistantController) aComponent.getController()).getParser();
        }
    }
}
