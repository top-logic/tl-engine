/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.UploadHandler;
import com.top_logic.util.Resources;

/**
 * Upload handler, which will validate the uploaded file. 
 * 
 * After uploading the file this handler will call the method 
 * {@link #validateFile(AssistentComponent, File)} to perform 
 * a consistency check on the uploaded file. If this succeeds 
 * (returned list is empty or <code>null</code>), the 
 * assistant can go further.
 * 
 * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public abstract class ValidatingUploadHandler extends UploadHandler {

    /** 
     * Creates a {@link ValidatingUploadHandler}.
     */
    public ValidatingUploadHandler(InstantiationContext context, Config config) {
        super(context, config);
    }

    /** 
     * Validate the file and it's content.
     * 
     * @param    aComponent    The assistant performing the upload, never <code>null</code>.
     * @param    aFile         The file to be validated, never <code>null</code>.
     * @return   The import messages occurred in the validation process, may be <code>null</code> or empty.
     * @see      #handleCommand(DisplayContext, LayoutComponent, Object, Map)
     */
    protected abstract List/* ImportMessage */ validateFile(AssistentComponent aComponent, File aFile);

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        HandlerResult theResult = super.handleCommand(aContext, aComponent, model, someArguments);

        if (theResult.isSuccess()) {
            AssistentComponent theAssistent = AssistentComponent.getEnclosingAssistentComponent(aComponent);
            File               theFile      = (File) theAssistent.getData(AssistentFileUploadComponent.FILE);

            if (theFile != null) {
                List    theList = this.validateFile(theAssistent, theFile);
                boolean isOK    = theList == null;

                theAssistent.setData(EVAAssistantController.VALIDATION_RESULT, theList);

                if (!isOK) {
                    boolean theFlag = true;

                    for (Iterator theIt = theList.iterator(); theFlag && theIt.hasNext();) {
                        ImportMessage theMessage = (ImportMessage) theIt.next();
                        
                        if (theMessage.isError()) {
                            theFlag = false;
                        }
                    }

                    isOK = theFlag;
                }

                if (isOK) {
                    return theResult;
                }
                else {
                    theAssistent.setData(AssistentComponent.SHOW_FAILTURE, Boolean.TRUE);
                }
            }
            else {
                theAssistent.setData(AssistentComponent.SHOW_FAILTURE, Boolean.TRUE);
				theResult.addErrorMessage(I18NConstants.ERROR_UPLOAD_NO_FILE);
            }
        }

        return theResult;
    }

    /**
     * Message containing one information snippet generated during the 
     * import process.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class ImportMessage {

        // Constants

        public static final String ERROR = "ERROR";
        
        public static final String INFO = "INFO";

        // Attributes

        private final String type;

		private final ResKey message;

        private final Object[] parameters;

        // Constructors
        
        /** 
         * Creates a {@link ImportMessage}.
         * 
         * @param    aType       The type, must be {@link #ERROR} or {@link #INFO}.
         * @param    aMessage    The I18N key describing the message, must not be <code>null</code>.
         */
		public ImportMessage(String aType, ResKey aMessage) {
            this(aType, aMessage, null);
        }
        
        /** 
         * Creates a {@link ImportMessage}.
         * 
         * @param    aType             The type, must be {@link #ERROR} or {@link #INFO}.
         * @param    aMessage          The I18N key describing the message, must not be <code>null</code>.
         * @param    someParameters    Parameters to append to the message, may be <code>null</code>.
         */
		public ImportMessage(String aType, ResKey aMessage, Object[] someParameters) {
            this.type       = aType;
            this.message    = aMessage;
            this.parameters = someParameters;
        }
        
        // Overridden methods from Object

        @Override
		public String toString() {
            return this.getClass().getName() + '[' + this.toStringValues() + ']';
        }

        // Public methods

        /** 
         * Return the I18N message (with contained parameters).
         * 
         * @param    aResources    The resources to be used for I18N, must not be <code>null</code>.
         * @return   The I18N representation of this message, never <code>null</code>.
         */
        public String getString(Resources aResources) {
            if (ArrayUtil.isEmpty(this.parameters)) {
                return aResources.getString(this.message);
            }
            else {
                return aResources.getMessage(this.message, this.parameters);
            }
        }

        /**
         * This method returns the message.
         * 
         * @return    Returns the message.
         */
		public ResKey getMessage() {
            return this.message;
        }

        /**
         * This method returns the someParameters.
         * 
         * @return    Returns the parameters.
         */
        public Object[] getSomeParameters() {
            return this.parameters;
        }

        /** 
         * Return <code>true</code>, if this is an error message.
         * 
         * @return    <code>true</code> when {@link #type} equals {@link #ERROR}.
         */
        public boolean isError() {
            return ERROR.equals(this.type);
        }

        // Protected methods

        /** 
         * Return the values to be displayed in {@link #toString()}.
         * 
         * @return    The values representing this instance.
         * @see       #toString()
         */
        protected String toStringValues() {
            return "type: " + this.type + ", message: " + this.message +
                   ", parameters: " + StringServices.toString(this.parameters);
        }
    }
}