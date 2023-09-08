/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.base.ocr.TLPDFCompress;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.upload.FileNameConstraint;
import com.top_logic.knowledge.gui.layout.upload.FileNameStrategy;
import com.top_logic.knowledge.gui.layout.upload.I18NConstants;
import com.top_logic.knowledge.gui.layout.upload.SimpleFileNameStrategy;
import com.top_logic.knowledge.gui.layout.upload.UploadAware;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.commandhandlers.UploadHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Component to upload File. Most commonly used as a modal dialog.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public abstract class FileUploadComponent extends FormComponent 
        implements FileNameStrategy, UploadAware {
    
	/**
	 * Configuration options for {@link FileUploadComponent}.
	 */
    public interface Config extends FormComponent.Config {

    	/** @see #isMultiUpload() */
    	String MULTI_UPLOAD = "multiUpload";

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@StringDefault(PAGE)
		String getPage();

		@Name(XML_CONFIG_KEY_UPLOAD_DATE_FIELD)
		@BooleanDefault(false)
		boolean getUploadDateField();

		/**
		 * Flag whether the upload field shall be multi upload aware.
		 */
		@Name(MULTI_UPLOAD)
		@BooleanDefault(false)
		boolean isMultiUpload();

		@Name(XML_CONFIG_KEY_EXTENSIONS)
		String getExtensions();

		/** Name of the upload handler that should be used */
		@StringDefault(UploadHandler.COMMAND)
		@Name(XML_CONFIG_KEY_UPLOAD_HANDLER_NAME)
		String getUploadHandlerName();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(getUploadHandlerName());
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
		}

	}

	/** Configuration key for allowed extensions. */
    public static final String XML_CONFIG_KEY_EXTENSIONS = "extensions";
    
    public static final String XML_CONFIG_KEY_UPLOAD_DATE_FIELD = "uploadDateField";

	public static final String XML_CONFIG_KEY_UPLOAD_HANDLER_NAME = "uploadHandlerName";

    /** Name of the Form used for upload (this ties us to the related JSPs */
    public static final String UPLOAD_FORM_NAME = "upload";
    
    /** also used on jsp's */
    public static final String UPLOAD_FIELD_NAME = "uploadField";

    public static final String DATE_FIELD = "dateField";

    /** The name of the form field with the supported languages. */
    public static final String FIELD_NAME_LANGUAGE = "language";
    
    /** The page displayed by this component. */
    private static final String PAGE = "/jsp/layout/webfolder/UploadFile.jsp";

    private Date date;
    
    private boolean uploadDateField;

	/** @see Config#isMultiUpload() */
	private final boolean multiUpload;

    /** Array of allowed extensions as gotten from attributes */
    protected String[] allowedExts;

    public FileUploadComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		this.uploadDateField = atts.getUploadDateField();
		multiUpload = atts.isMultiUpload();
		this.allowedExts =
			StringServices.toArray(StringServices.nonEmpty(atts.getExtensions()));
    }

    protected FileUploadComponent(InstantiationContext context, Config atts, String aPage) throws ConfigurationException {
        super(context, atts);
        this.page = aPage;
		multiUpload = false;
    }
        
    @Override
	protected boolean supportsInternalModel(Object aObject) {
        return aObject == null;
    }

	protected final String getUploadHandlerName() {
		return ((Config) getConfig()).getUploadHandlerName();
    }

    /** Is an ORC attached to the upload (implies choosing a language) */
    public boolean useOCR() {
    	return false;
    }
    
    /**
     * Return a SelectField based on {@link TLPDFCompress#supportedLanguages()}.
     * 
     * @return null in case TLPDFCompress is not installed/configured
     */
    public SelectField createLanguageField() {
        SelectField   field = null;
        if (this.useOCR()) {
            Collection<String> supported = TLPDFCompress.getInstance().supportedLanguages();
            List<String> languages = new ArrayList<>(1 + supported.size());
            languages.addAll(supported);
            String currLang = TLContext.getLocale().getLanguage();
            field = FormFactory.newSelectField(FIELD_NAME_LANGUAGE, languages, /* multiple */ false, 
                    Collections.singletonList(currLang), /* immutable */ false);
            
            field.setOptionLabelProvider(new LabelProvider() {
    		
    			@Override
				public String getLabel(Object aObject) {
					return Resources.getDisplayLanguage((String) aObject);
    			}
    		
    		});
        }
        return field;
    }
    
    public boolean useMultipleFolders() {
    	return false;
    }
    
    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
        FormContext theCtx = new FormContext(UPLOAD_FORM_NAME, getResPrefix());
        if(showUploadDateField()) {
            DateFormat theDF = HTMLFormatter.getInstance().getDateFormat();
    		ComplexField theDateField = FormFactory.newComplexField(DATE_FIELD, theDF);
            theCtx.addMember(theDateField);
            theDateField.addValueListener(new ChangeValueListener());
        }
        
		DataField uploadField = FormFactory.newDataField(UPLOAD_FIELD_NAME, isMultiUpload());
		uploadField.setDownload(false);
        uploadField.setFileNameConstraint(getFileNameConstraint());
		uploadField.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
        theCtx.addMember(uploadField);
        return theCtx;
    }
    
    protected Constraint getFileNameConstraint() {
    	return new FileNameConstraint(this);
    }

    @Override
	public ResKey checkFileName(String fileName) {
		ResKey theResult = SimpleFileNameStrategy.INSTANCE.checkFileName(fileName);

        if (theResult == null && !StringServices.isEmpty(allowedExts)) {
            fileName = fileName.toLowerCase();

            for (int thePos = 0; thePos < allowedExts.length; thePos++) {
                if (fileName.endsWith(allowedExts[thePos]))
					return null;
            }
			return I18NConstants.ERROR_FILENAME_WITH_UNSUPPORTED_EXTENSION__EXTENSIONS.fill(
				StringServices.join(Arrays.asList(allowedExts), ", "));
        }

        return theResult;
    }
    
    @Override
    public BoundCommandGroup getDefaultCommandGroup() {
    	return SimpleBoundCommandGroup.WRITE;
    }

    public boolean showUploadDateField() {
    	return this.uploadDateField;
    }

    /**
	 * Gets the {@link #multiUpload} flag.
	 */
	public boolean isMultiUpload() {
		return multiUpload;
	}

    /**
     * Returns the date.
     */
    public Date getDate() {
    	return (date);
    }
    
    /*package protected*/ class ChangeValueListener implements ValueListener{

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			if(newValue instanceof Date) {
				date = (Date) newValue;
			}
			else {
				date = null;
			}
        }
    }
}
