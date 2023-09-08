/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.io.IOException;
import java.util.Objects;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachListener;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.DefaultPopupMenuModel;
import com.top_logic.layout.basic.PopupMenuModel;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Control to write the tag rendering a title row with a border around some content.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GroupCellControl extends ConstantControl<HTMLFragment> implements CollapsedListener, GroupSettings {

	private final Collapsible _collapsible;

	private final ButtonControl _toggle;

	private HTMLFragment _title;

	private final GroupSettings _settings;
	
	private static final AbstractButtonRenderer<?> TOGGLE_RENDERER =
		ImageButtonRenderer.newSystemButtonRenderer(FormConstants.TOGGLE_BUTTON_CSS_CLASS);

	/**
	 * Creates a {@link GroupCellControl}.
	 * 
	 * @param model
	 *        The content of this GroupCell.
	 * @param collapsible
	 *        The container to attach the {@link CollapsedListener} whether this GroupCell is
	 *        collapsed or expanded.
	 * @param settings
	 *        Configuration options for the group display.
	 */
	public GroupCellControl(HTMLFragment model, Collapsible collapsible, GroupSettings settings) {
		super(model);
		_collapsible = Objects.requireNonNull(collapsible);
		_settings = settings;
		_toggle = createToggleButton();

		if (!isCollapsible()) {
			_toggle.getModel().setNotExecutable(I18NConstants.GROUP_NOT_COLLAPSIBLE);
		}
	}

	/**
	 * Whether the group is currently displayed in minimized state.
	 */
	@TemplateVariable("collapsed")
	public boolean isCollapsed() {
		return _collapsible.isCollapsed();
	}

	private ButtonControl createToggleButton() {
		return createToggleImageButton(_collapsible);
	}

	/**
	 * The {@link Collapsible collapsible model} which is used.
	 */
	public Collapsible getCollapsible() {
		return _collapsible;
	}

	/**
	 * Title of the group.
	 */
	@TemplateVariable("title")
	public HTMLFragment getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public GroupCellControl setTitle(HTMLFragment title) {
		_title = title;
		requestRepaint();
		return this;
	}

	@Override
	public int getColumns() {
		return _settings.getColumns();
	}

	@Override
	public String getCssClass() {
		return _settings.getCssClass();
	}

	@Override
	public String getStyle() {
		return _settings.getStyle();
	}

	@Override
	public boolean isWholeLine() {
		return _settings.isWholeLine();
	}

	@Override
	public boolean getLabelAbove() {
		return _settings.getLabelAbove();
	}

	@Override
	public boolean hasBorder() {
		return _settings.hasBorder();
	}

	@Override
	public boolean hasLegend() {
		return _settings.hasLegend();
	}

	@Override
	public boolean isDraggable() {
		return _settings.isDraggable();
	}

	@Override
	public String getDataId() {
		return _settings.getDataId();
	}

	@Override
	public boolean isCollapsible() {
		return _settings.isCollapsible();
	}

	@Override
	public int getRows() {
		return _settings.getRows();
	}

	@Override
	public String getWidth() {
		return _settings.getWidth();
	}

	@Override
	public Menu getMenu() {
		return _settings.getMenu();
	}

	@Override
	public ThemeImage getCommandButton() {
		return _settings.getCommandButton();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		com.top_logic.layout.form.boxes.reactive_tag.Icons.FORM_GROUP_TEMPLATE.get().write(context, out, this);
	}

	/**
	 * The menu button to open a context menu displaying additional commands.
	 */
	@TemplateVariable("menuButton")
	public void writeMenuButton(DisplayContext context, TagWriter out) throws IOException {
		PopupMenuModel popupMenuField = new DefaultPopupMenuModel(getCommandButton(), getMenu());
		PopupMenuButtonControl menuControl =
			new PopupMenuButtonControl(popupMenuField, DefaultToolBar.BUTTON_RENDERER);
		menuControl.write(context, out);
	}

	/**
	 * The button for toggling the collapsed state of the group.
	 */
	@TemplateVariable("toggleButton")
	public void writeToggleButton(DisplayContext context, TagWriter out) throws IOException {
		_toggle.write(context, out);
	}

	/**
	 * The HTML contents of the group.
	 */
	@TemplateVariable("content")
	public void writeContent(DisplayContext context, TagWriter out) throws IOException {
		getModel().write(context, out);
	}


	@Override
	protected void internalAttach() {
		super.internalAttach();
		_collapsible.addListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	@Override
	protected void internalDetach() {
		_collapsible.removeListener(Collapsible.COLLAPSED_PROPERTY, this);
		super.internalDetach();
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	public Bubble handleCollapsed(Collapsible sender, Boolean oldValue, Boolean newValue) {
		if (sender.equals(_collapsible)) {
			if (isCollapsible()) {
				requestRepaint();
			}
		}
		return Bubble.BUBBLE;
	}

	/**
	 * Creates a button displaying the {@link Collapsible#isCollapsed() collapsed} state of the
	 * given {@link Collapsible}.
	 * 
	 * @param model
	 *        The {@link Collapsible} whose collapsed state is displayed.
	 */
	public static ButtonControl createToggleImageButton(Collapsible model) {
		AbstractCommandModel command = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				model.setCollapsed(!model.isCollapsed());
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		command.setLabel(StringServices.EMPTY_STRING);
		class UpdateImageListener implements CollapsedListener {

			@Override
			public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
				if (collapsible.equals(model)) {
					setCommandImage(newValue.booleanValue());
				}
				return Bubble.BUBBLE;
			}

			void setCommandImage(boolean collapsed) {
				command.setImage(collapsed ? com.top_logic.layout.form.control.Icons.BOX_COLLAPSED
					: com.top_logic.layout.form.control.Icons.BOX_EXPANDED);
			}
		}
		UpdateImageListener updateImage = new UpdateImageListener();
		ButtonControl buttonControl = new ButtonControl(command, TOGGLE_RENDERER);
		buttonControl.addListener(ATTACHED_PROPERTY,
			new AttachListener(model, Collapsible.COLLAPSED_PROPERTY, updateImage) {

				@Override
				protected void updateObservedState(AbstractControlBase sender) {
					updateImage.setCommandImage(model.isCollapsed());
				}
			});
		return buttonControl;
	}

}