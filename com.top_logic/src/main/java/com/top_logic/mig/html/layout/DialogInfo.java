/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.component.title.ConstantTitle;
import com.top_logic.layout.component.title.TitleProvider;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Configuration of a dialog display.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayInherited(DisplayStrategy.IGNORE)
@DisplayOrder({
	DialogInfo.TITLE,
	DialogInfo.WIDTH,
	DialogInfo.HEIGHT,
	DialogInfo.HELP_ID,
	DialogInfo.RESIZABLE,
	DialogInfo.OPEN_MAXIMIZED,
	DialogInfo.CLOSABLE,
	DialogInfo.CREATE_OPENER_BUTTONS,
	DialogInfo.OPEN_HANDLER
})
public interface DialogInfo extends AbstractWindowInfo {

	/** @see #getHelpId() */
	String HELP_ID = LayoutComponent.Config.HELP_ID;

	/** @see #getSecurityComponentName() */
	String OPEN_BUTTON_SEC_COMP = "openButtonSecComp";

	/** @see #isResizable() */
	String RESIZABLE = "resizable";

	/** @see #isClosable() */
	String CLOSABLE = "closableUsingX";

	/** @see #isOpenMaximized() */
	String OPEN_MAXIMIZED = "openMaximized";

	/** @see #getTitle() */
	String TITLE = "title";

	/**
	 * Name of the component in the dialog that is responsible for answering security question about
	 * the open dialog button.
	 */
	@Name(OPEN_BUTTON_SEC_COMP)
	ComponentName getSecurityComponentName();

	/**
	 * @see #getSecurityComponentName()
	 */
	void setSecurityComponentName(ComponentName value);

	/**
	 * Whether the dialog can be resized.
	 */
	@Name(RESIZABLE)
	@BooleanDefault(true)
	boolean isResizable();

	/**
	 * @see #isResizable()
	 */
	void setResizable(boolean value);

	/**
	 * Whether this dialog can be closed using the 'X'-button in the title bar.
	 */
	@BooleanDefault(true)
	@Name(CLOSABLE)
	boolean isClosable();

	/**
	 * @see #isClosable()
	 */
	void setClosable(boolean value);

	/**
	 * Whether this dialog is opened maxmimized.
	 */
	@Name(OPEN_MAXIMIZED)
	boolean isOpenMaximized();

	/**
	 * @see #isOpenMaximized()
	 */
	void setOpenMaximized(boolean value);

	@Override
	@ClassDefault(OpenModalDialogCommandHandler.class)
	Class<? extends CommandHandler> getOpenHandlerClass();

	/**
	 * The help ID associated with the dialog.
	 */
	@Name(HELP_ID)
	@Nullable
	String getHelpId();

	/**
	 * @see #getHelpId()
	 */
	void setHelpId(String value);

	@Override
	@NullDefault
	@ImplementationClassDefault(OpenModalDialogCommandHandler.class)
	PolymorphicConfiguration<? extends OpenModalDialogCommandHandler> getOpenHandler();


	/**
	 * A {@link TitleProvider} creating the title to display in the title bar of the represented
	 * dialog.
	 * 
	 * <p>
	 * The configured {@link TitleProvider} gets the top level {@link LayoutComponent} of the
	 * component and creates a non <code>null</code> {@link HTMLFragment} used as title.
	 * </p>
	 * 
	 * @return May be <code>null</code>. In this case the title is a simple translation of a
	 *         {@link ResKey} derived from the dialog component.
	 */
	@Name(TITLE)
	@DefaultContainer
	@ImplementationClassDefault(ConstantTitle.class)
	PolymorphicConfiguration<TitleProvider> getTitle();

	/**
	 * Setter for {@link #getTitle()}
	 */
	void setTitle(PolymorphicConfiguration<TitleProvider> value);
}