/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueListener;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProviderDelegate;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Table of comments for objects. 
 * 
 * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class CommentTableComponent extends TableComponent implements DecorationValueProvider {
    
	public interface Config extends TableComponent.Config {
		@Name(XML_COMMENTS_ATTRIBUTE)
		@StringDefault(COMMENTS_ATTRIBUTE)
		String getAttribute();

		/**
		 * The name of the component that quotes a given comment.
		 */
		@Name(QUOTE_COMMENT_COMPONENT_NAME)
		ComponentName getQuoteCommentComponentName();

		/**
		 * The name of the component that edits a given comment.
		 */
		@Name(EDIT_COMMENT_COMPONENT_NAME)
		ComponentName getEditCommentComponentName();

		@Override
		@FormattedDefault("tl.comments:Comment")
		List<String> getObjectType();

	}

	public static final String QUOTE_COMMENT_COMPONENT_NAME = "quoteCommentComponentName";

	public static final String EDIT_COMMENT_COMPONENT_NAME = "editCommentComponentName";

    public static final String XML_COMMENTS_ATTRIBUTE = "attribute";

    private static final String COMMENTS_ATTRIBUTE = "comments";

    /** Name of the attribute storing the comments in the handled wrapper. */
    private String commentsAttributeName;

    /** Handler for decorator events and notifications. */
    private DecorationValueProviderDelegate decorationValueProviderDelegate;

	private ComponentName _quoteCommentComponentName;

	private ComponentName _editCommentComponentName;

	private CommandHandler _editCommentComponentOpener;

	private CommandHandler _quoteCommentComponentOpener;

    
    /** 
     * Creates a new instance of CommentTableComponent.
     */
    public CommentTableComponent(InstantiationContext context, Config attr) throws ConfigurationException {
        super(context, attr);

        this.decorationValueProviderDelegate = new DecorationValueProviderDelegate();
        this.commentsAttributeName           = attr.getAttribute();
		_quoteCommentComponentName = attr.getQuoteCommentComponentName();
		_editCommentComponentName = attr.getEditCommentComponentName();
    }

    /**
     * @see com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider#getTabBarDecorationValues()
     */
    @Override
	public String[] getTabBarDecorationValues() {
		int theSize = (getListBuilder().getModel(getModel(), this)).size();

        return new String[] { Integer.toString(theSize) };
    }

    /**
     * @see com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider#registerDecorationValueListener(com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueListener)
     */
    @Override
	public void registerDecorationValueListener(DecorationValueListener aListener) {
        this.decorationValueProviderDelegate.registerDecorationValueListener(aListener);
    }

	@Override
	public boolean unregisterDecorationValueListener(DecorationValueListener aListener) {
		return decorationValueProviderDelegate.unregisterDecorationValueListener(aListener);
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);
		editCommentComponentComponent:
		if (_editCommentComponentName != null) {
			LayoutComponent editComponent = getMainLayout().getComponentByName(_editCommentComponentName);
			if (editComponent == null) {
				StringBuilder noComponent = new StringBuilder();
				noComponent.append("No appropriate edit comment dialog declared in xml for component ");
				noComponent.append(getName());
				noComponent.append(". Creation of quoted comments will NOT work.");
				Logger.warn(noComponent.toString(), CommentTableComponent.class);
				break editCommentComponentComponent;
			}
			_editCommentComponentOpener = findOpener(editComponent);
		}

		quoteCommentComponentComponent:
		if (_quoteCommentComponentName != null) {
			LayoutComponent quoteComponent = getMainLayout().getComponentByName(_quoteCommentComponentName);
			if (quoteComponent == null) {
				StringBuilder noComponent = new StringBuilder();
				noComponent.append("No appropriate quote comment dialog declared in xml for component ");
				noComponent.append(getName());
				noComponent.append(". Creation of quoted comments will NOT work.");
				Logger.warn(noComponent.toString(), CommentTableComponent.class);
				break quoteCommentComponentComponent;
			}
			_quoteCommentComponentOpener = findOpener(quoteComponent);
		}
	}

	private CommandHandler findOpener(LayoutComponent component) {
		LayoutComponent dialog = component.getDialogTopLayout();
		DialogInfo dialogInfo = dialog.getConfig().getDialogInfo();
		PolymorphicConfiguration<? extends OpenModalDialogCommandHandler> openHandler = dialogInfo.getOpenHandler();
		String openHandlerName;
		if (openHandler != null) {
			/* Need to cast to raw type before cast to CommandHandler.Config, as javac rejects
			 * direct cast because of incompatible types: PolymorphicConfiguration<CAP#1> cannot be
			 * converted to Config where CAP#1 is a fresh type-variable: CAP#1 extends
			 * OpenModalDialogCommandHandler from capture of ? extends
			 * OpenModalDialogCommandHandler. */
			@SuppressWarnings({ "rawtypes", "cast" })
			PolymorphicConfiguration rawOpenHandler = (PolymorphicConfiguration) openHandler;
			openHandlerName = ((CommandHandler.Config) rawOpenHandler).getId();
		} else {
			openHandlerName = dialogInfo.getOpenHandlerName();
		}
		return getCommandById(openHandlerName);
	}

    public String getCommentsAttributeName() {
        return this.commentsAttributeName;
    }
    
    public static CommandHandler getNewCommentDialogOpenerCommandId(LayoutComponent aComponent) {
        return aComponent.getCommandById("openNewCommentDialogDialog");
    }

	public ComponentName getEditComponentName() {
		return _editCommentComponentName;
	}

	public ComponentName getQuoteComponentName() {
		return _quoteCommentComponentName;
	}

	public CommandHandler getEditComponentOpener() {
		return _editCommentComponentOpener;
	}

	public CommandHandler getQuoteComponentOpener() {
		return _quoteCommentComponentOpener;
	}

}
