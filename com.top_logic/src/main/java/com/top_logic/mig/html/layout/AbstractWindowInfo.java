/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.OnlySetIfUnset;
import com.top_logic.basic.config.format.JavaIdentifier;
import com.top_logic.knowledge.gui.layout.SizeInfo;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.window.WindowInfo;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ExecutabilityConfig;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Common properties of {@link DialogInfo} and {@link WindowInfo}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractWindowInfo
		extends SizeInfo, ExecutabilityConfig, CommandHandler.TargetConfig, CommandHandler.ImageConfig {

	/** @see #getCreateOpenerButtons() */
	String CREATE_OPENER_BUTTONS = "createOpenerButtons";

	/** @see #getOpenHandler() */
	String OPEN_HANDLER = "open-handler";

	/** @see #getOpenerCommandGroup() */
	String OPENER_COMMAND_GROUP = "openerCommandGroup";

	/** @see #getTargetComponent() */
	String TARGET_COMPONENT = OpenModalDialogCommandHandler.Config.TARGET_COMPONENT;

	/** @see #getCommandClique() */
	String OPENER_CLIQUE = "openerClique";

	/** @see #getOpenHandlerClass() */
	String OPEN_HANDLER_CLASS_NAME = "openHandlerClass";

	/** @see #getOpenHandlerName() */
	String OPEN_HANDLER_NAME = "openHandlerName";

	/** @see #getImage() */
	String IMAGE = "image";

	/** @see #getDisabledImage() */
	String IMAGE_DISABLED = "disabledImage";

	/**
	 * Whether the {@link #getOpenHandler()} should be visible as button.
	 * 
	 * <p>
	 * If the value is <code>false</code>, the dialog cannot be directly opened by the user. In that
	 * case, an open handler must be configured in some other component to open the dialog.
	 * </p>
	 */
	@Name(CREATE_OPENER_BUTTONS)
	@BooleanDefault(true)
	boolean getCreateOpenerButtons();

	/**
	 * @see #getCreateOpenerButtons()
	 */
	void setCreateOpenerButtons(boolean value);

	/**
	 * Stand-alone configuration of the open handler.
	 * 
	 * <p>
	 * Note: Currently, this option can only be configured directly in sub-classes.
	 * </p>
	 */
	@Name(OPEN_HANDLER)
	@Abstract
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends CommandHandler> getOpenHandler();

	/**
	 * @see #getOpenHandler()
	 */
	void setOpenHandler(PolymorphicConfiguration<? extends CommandHandler> value);

	/**
	 * Command group for the open command.
	 */
	@Name(OPENER_COMMAND_GROUP)
	@FormattedDefault(SimpleBoundCommandGroup.READ_NAME)
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	BoundCommandGroup getOpenerCommandGroup();

	/**
	 * @see #getOpenerCommandGroup()
	 */
	void setOpenerCommandGroup(BoundCommandGroup value);

	/**
	 * Command clique of the opener command, if defined in parts using
	 * {@link #getOpenHandlerClass()}.
	 * 
	 * <p>
	 * If not set, a dynamic default applies defined by
	 * {@link com.top_logic.tool.boundsec.CommandHandlerFactory.Config#getDefaultClique()}.
	 * </p>
	 */
	@Name(OPENER_CLIQUE)
	@Nullable
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	String getCommandClique();

	/**
	 * @see #getCommandClique()
	 */
	void setCommandClique(String value);

	/**
	 * Class of the handler opening the dialog.
	 */
	@Name(OPEN_HANDLER_CLASS_NAME)
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	Class<? extends CommandHandler> getOpenHandlerClass();

	/**
	 * @see #getOpenHandlerClass()
	 */
	void setOpenHandlerClass(Class<? extends CommandHandler> value);

	/**
	 * Name of the opener command opening the dialog as found in the {@link CommandHandlerFactory}.
	 */
	@Name(OPEN_HANDLER_NAME)
	@Format(JavaIdentifier.class)
	@Nullable
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	String getOpenHandlerName();

	/**
	 * @see #getOpenHandlerName()
	 */
	void setOpenHandlerName(String value);

	@Override
	@Name(TARGET)
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	ModelSpec getTarget();

	/**
	 * {@link com.top_logic.tool.boundsec.OpenModalDialogCommandHandler.Config#getTargetComponent()}
	 * of the generated dialog open command.
	 */
	@Name(TARGET_COMPONENT)
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	ComponentName getTargetComponent();

	/**
	 * The icon to use for the opening command.
	 */
	@Override
	@Name(IMAGE)
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	ThemeImage getImage();

	/**
	 * The icon to use for the opening command, if the dialog cannot be opened.
	 */
	@Override
	@Name(IMAGE_DISABLED)
	@Constraint(value = OnlySetIfUnset.class, args = { @Ref(OPEN_HANDLER) })
	ThemeImage getDisabledImage();

}
