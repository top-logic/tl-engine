/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.edit;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.TypedChannelSPI;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.component.IComponent;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link LayoutComponent} plug-in interface for supporting edit-mode.
 * 
 * <p>
 * Note: When implementing this interface in a {@link LayoutComponent}, one must delegate to
 * {@link #linkChannels(Log)} from the {@link LayoutComponent#linkChannels(Log)} implementation.
 * </p>
 * 
 * <p>
 * Note: To switch to view mode when the component becomes invisible, {@link #setViewMode()} must be
 * called explicitly from {@link LayoutComponent#becomingInvisible()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EditMode extends IComponent {

	/**
	 * The mode in which an {@link Editor} can be.
	 */
	public enum EditorMode {

		/**
		 * The {@link Editor} is in view-mode.
		 * 
		 * @see Editor#isInViewMode()
		 */
		VIEW_MODE,

		/**
		 * The {@link Editor} is in edit-mode.
		 * 
		 * @see Editor#isInEditMode()
		 */
		EDIT_MODE;
	}

	/**
	 * Configuration options for {@link EditMode}.
	 */
	public interface Config extends IComponent.ComponentConfig {

		/**
		 * Component channel for controlling the edit mode of an edit component.
		 * 
		 * <p>
		 * The channel value is of type boolean. The value <code>true</code> means, the component is
		 * in edit mode, the value <code>false</code> signals that the component is in view mode.
		 * </p>
		 * 
		 * <p>
		 * An edit component can be switched to edit mode, by setting the edit mode channel value to
		 * <code>true</code>. In a {@link CommandHandler}, this can be done by setting the "edit
		 * mode channel model" in a {@link PostCreateAction}.
		 * </p>
		 */
		@Name(EDIT_MODE_CHANNEL)
		@DisplayMinimized
		ModelSpec getEditMode();

	}

	/**
	 * Name of the corresponding {@link ComponentChannel} delivering the edit mode state (boolean).
	 */
	String EDIT_MODE_CHANNEL = "editMode";

	/**
	 * Default {@link ChannelSPI} for creating a corresponding {@link ComponentChannel}.
	 */
	TypedChannelSPI<Boolean> EDIT_MODE_SPI = new TypedChannelSPI<>(EDIT_MODE_CHANNEL, Boolean.class, Boolean.FALSE);

	/**
	 * The mode of this editor.
	 */
	default EditorMode getEditorMode() {
		return isEditing() ? EditorMode.EDIT_MODE : EditorMode.VIEW_MODE;
	}

	/**
	 * Whether this component is currently in view mode (not in edit mode).
	 */
	default boolean isInViewMode() {
		return !isEditing();
	}

	/**
	 * Whether this component is currently in edit mode.
	 */
	default boolean isInEditMode() {
		return isEditing();
	}

	/**
	 * Brings this component to view mode.
	 */
	default void setViewMode() {
		setEditMode(false);
	}

	/**
	 * Brings this component to edit mode.
	 */
	default void setEditMode() {
		setEditMode(true);
	}

	/**
	 * Whether the component is currently in edit-mode.
	 */
	default boolean isEditing() {
		return ((Boolean) editModeChannel().get()).booleanValue();
	}

	/**
	 * Sets the edit-mode state.
	 * 
	 * @param newValue
	 *        Whether the new component state should be editing.
	 * 
	 * @see #isEditing()
	 */
	default void setEditMode(boolean newValue) {
		editModeChannel().set(Boolean.valueOf(newValue));
	}

	/**
	 * The {@link ComponentChannel} managing the component's edit mode.
	 */
	default ComponentChannel editModeChannel() {
		return getChannel(EDIT_MODE_CHANNEL);
	}

	/**
	 * Listener forwarding edit mode channel changes to component.
	 */
	static final ComponentChannel.ChannelListener EDIT_MODE_LISTENER = new ComponentChannel.ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			EditMode editor = (EditMode) sender.getComponent();
			editor.handleComponentModeChange(((Boolean) newValue).booleanValue());
		}
	};

	@Override
	default void linkChannels(Log log) {
		LayoutComponent self = (LayoutComponent) this;
		ChannelLinking channelLinking = self.getChannelLinking(((Config) getConfig()).getEditMode());
		editModeChannel().linkChannel(log, self(), channelLinking);
		editModeChannel().addListener(EDIT_MODE_LISTENER);
	}

	/**
	 * Callback invoked, if the mode channel changes its value.
	 */
	void handleComponentModeChange(boolean editMode);

}
