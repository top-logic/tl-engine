/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.comment.wrap.Comment;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.SimpleOpenDialog;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.FormBinding;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * This component provides an input field for a new comment.
 *
 * @author    <a href="mailto:tsa@top-logic.de">Theo Sattler</a>
 */
public class CommentDialogComponent extends FormComponent {

    public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(EDIT_COMMENT_XML_ATTRIBUTE)
		@BooleanDefault(false)
		boolean getEditComment();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(getEditComment()
			? EDIT_COMMENT_HANDLER
			: CREATE_NEW_COMMENT_HANDLER);
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
		}

	}

	public static final String EDIT_COMMENT_XML_ATTRIBUTE = "editComment";

    private static final String CREATE_NEW_COMMENT_HANDLER = "addComment";

    private static final String EDIT_COMMENT_HANDLER = "editComment";

    private static final String COMMENT_FIELD = "comments";

    /**
     * Creates a {@link CommentDialogComponent}.
     */
    public CommentDialogComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null || anObject instanceof Comment;
    }

    /**
	 * Indicates that not an new comment is to be created, but an existing comment is to be changed
	 */
    public boolean isEditComment() {
		return ((Config) getConfig()).getEditComment();
    }
    
    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
		FormField theField = FormFactory.newStringField(COMMENT_FIELD, true, false, null);
		FormBinding.addMOSizeConstraint(theField, Comment.KO_TYPE, Comment.CONTENT_ATTRIBUTE);

		Comment quote = getBaseComment();
		if (quote != null) {
			String theQuoteText = quote.getContent();
            String theAuthor    = "";

            try {
				Person theCreator = quote.getCreator();
                if (theCreator != null) theAuthor = theCreator.getFullName();
            }
            catch(Exception ex) {
            	Logger.error("Unable to get creator name from comment",ex,this);
            }

			if (!isEditComment()) {
                theQuoteText = "[quote="+theAuthor+"]" + theQuoteText + "[/quote]\n";
            }

            theField.initializeField(theQuoteText);
        }

        return new FormContext("comment", this.getResPrefix(), Collections.singletonList(theField));
    }

    protected String getCommentsAttributeName() {
        return ((CommentTableComponent) this.getDialogParent()).getCommentsAttributeName();
    }

    protected void afterHandleNew(Comment aComment, HandlerResult aHandlerRsult) {
        LayoutComponent theParent = this.getDialogParent();
		removeFormContext();
        theParent.invalidate();
    }

    protected void afterHandleEdit(Comment aComment, HandlerResult aHandlerRsult) {
		removeFormContext();
        LayoutComponent theParent = this.getDialogParent();
        theParent.invalidate();
    }

    /**
     * @see com.top_logic.layout.basic.component.ControlComponent#becomingInvisible()
     */
    @Override
	protected void becomingInvisible() {
		removeFormContext();
    	super.becomingInvisible();
    }

    public Comment getBaseComment() {
		return (Comment) getModel();
    }

    /**
     * @author    <a href=mailto:mga@top-logic.com>mga</a>
     */
    public static class NewCommentHandler extends AbstractCommandHandler {

		/**
		 * Configuration options for {@link CommentDialogComponent.NewCommentHandler}.
		 */
		public interface Config extends AbstractCommandHandler.Config {

			@Override
			@FormattedDefault(TARGET_MODEL_DIALOG_PARENT)
			ModelSpec getTarget();
		}

		/**
		 * Creates a {@link CommentDialogComponent.NewCommentHandler} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
        public NewCommentHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aDialog, Object model, Map<String, Object> someArguments) {
            HandlerResult          theResult    = new HandlerResult();
            CommentDialogComponent theComponent = (CommentDialogComponent) aDialog;
            FormContext            theContext   = theComponent.getFormContext();

            if (theContext.checkAll()) {
				Wrapper theAttributed = (Wrapper) model;
                String     theContent    = (String) theContext.getField(COMMENT_FIELD).getValue();
                Comment    theComment    = Comment.createComment(theContent);

                AttributeOperations.addValue(theAttributed, theComponent.getCommentsAttributeName(), theComment);

				{
                    if (!theAttributed.getKnowledgeBase().commit()) {
						theResult.addErrorText("Unable to commit comment");
                    }
                }
                theComponent.afterHandleNew(theComment, theResult);
                aDialog.closeDialog();
                return theResult;

            }
            else {
				AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, theResult);
                return theResult;
            }

        }

        /**
         * @see com.top_logic.tool.boundsec.AbstractCommandHandler#needsConfirm()
         */
        @Override
        public boolean needsConfirm() {
            return false;
        }

        @Override
		@Deprecated
        public ResKey getDefaultI18NKey() {
			return (I18NConstants.SAVE);
        }
    }

    /**
     * Perform the editing of a comment.
     *
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class EditCommentHandler extends AbstractCommandHandler {

        // Constructors

        /**
         * Creates a {@link EditCommentHandler}.
         */
        public EditCommentHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aDialog, Object model, Map<String, Object> someArguments) {
            HandlerResult          theResult    = new HandlerResult();
            CommentDialogComponent theComponent = (CommentDialogComponent) aDialog;
            FormContext            theContext   = theComponent.getFormContext();

            if (theContext.checkAll()) {
                Comment theOrginal = theComponent.getBaseComment();
                String  theContent = (String) theContext.getField(COMMENT_FIELD).getValue();

                theOrginal.setContent(theContent);
				theOrginal.getKnowledgeBase().commit();
                theComponent.afterHandleEdit(theOrginal, theResult);
                aDialog.closeDialog();
                
                return theResult;
            }
            else {
				AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, theResult);
                return theResult;
            }
        }

        /**
         * @see com.top_logic.tool.boundsec.AbstractCommandHandler#needsConfirm()
         */
        @Override
        public boolean needsConfirm() {
            return false;
        }

        @Override
		@Deprecated
        public ResKey getDefaultI18NKey() {
			return (I18NConstants.SAVE);
        }
    }

    /**
     * Very special execution rule for the comment table.
     *
     * This table will render buttons for every row (comment) of the table. Dependend
     * on this rows, two commands will be requested, "quote..." and "edit...".
     *
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class OwnerExecutabilityRule implements ExecutabilityRule {

        // Constants

        
        public static final String COMMAND_ATTRIBUTE = "command";

        
		public static final String WRAPPER_ATTRIBUTE = SimpleOpenDialog.MODEL;

		/** Singleton {@link OwnerExecutabilityRule} instance. */
		public static final OwnerExecutabilityRule INSTANCE = new OwnerExecutabilityRule();

		private OwnerExecutabilityRule() {
			// singleton instance
		}

        /**
         * @see com.top_logic.tool.execution.ExecutabilityRule#isExecutable(com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
         */
        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			{
                if (someValues != null) {
                    String theCommand = (String) someValues.get(COMMAND_ATTRIBUTE);

                    if ((theCommand == null) || theCommand.startsWith("quote")) {
                        return ExecutableState.EXECUTABLE;
                    }
                    else {
                        AbstractWrapper theComment;

                        if (someValues.containsKey(WRAPPER_ATTRIBUTE)) {
                            theComment = (AbstractWrapper) someValues.get(WRAPPER_ATTRIBUTE);
                        }
                        else if (someValues.containsKey(AbstractCommandHandler.OBJECT_ID)) {
							TLID theID =
								IdentifierUtil.fromExternalForm(LayoutComponent.getParameter(someValues,
									AbstractCommandHandler.OBJECT_ID));
                            String theType = LayoutComponent.getParameter(someValues, AbstractCommandHandler.TYPE);

							theComment = (AbstractWrapper) WrapperFactory.getWrapper(theID, theType);
                        }
                        else {
                            throw new TopLogicException(OwnerExecutabilityRule.class, "comment.get.noInfo");
                        }

                        Person theCreator = theComment.getCreator();

                        if (theCreator != null) {
                            Person thePerson = TLContext.getContext().getCurrentPersonWrapper();

                            return WrapperHistoryUtils.equalsUnversioned(theCreator, thePerson) || TLContext.isSuperUser()
                                ? ExecutableState.EXECUTABLE
                                : ExecutableState.NOT_EXEC_HIDDEN;
                        }
                        else {
                            return ExecutableState.NOT_EXEC_HIDDEN;
                        }
                    }
                }
            }

            return ExecutableState.NOT_EXEC_HIDDEN;
        }
    }

    /**
     * TODO TSA what is this command needed for ?
     *
     * @author    <a href=mailto:tsa@top-logic.com>Theo Sattler</a>
     */
    public static class NewCommentOpenerHandler extends OpenModalDialogCommandHandler {

        /**
         * Creates a {@link NewCommentOpenerHandler}.
         */
        public NewCommentOpenerHandler(InstantiationContext context, Config config) {
            super(context, config);
        }
    }
}
