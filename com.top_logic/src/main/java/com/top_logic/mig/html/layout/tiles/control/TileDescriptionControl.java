/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorPluginFactory;
import com.top_logic.layout.scripting.recorder.gui.inspector.InspectorModel;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.PersonalizedTile;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.ComponentTileSupplier;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.mig.html.layout.tiles.component.TilePreview;
import com.top_logic.mig.html.layout.tiles.scripting.DisplayedPathAssertion;
import com.top_logic.mig.html.layout.tiles.scripting.TileAllowedAssertion;
import com.top_logic.mig.html.layout.tiles.scripting.TileContentsAssertion;
import com.top_logic.mig.html.layout.tiles.scripting.TileHiddenAssertion;
import com.top_logic.mig.html.layout.tiles.scripting.TilePathAssertion;
import com.top_logic.mig.html.layout.tiles.scripting.TilePathInfoPlugin;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractTileControl} rendering a description for the given {@link ComponentTile}.
 * 
 * <p>
 * When the description is clicked, the corresponding {@link ComponentTile} is displayed.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileDescriptionControl extends AbstractTileControl<ComponentTile> {

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(AbstractTileControl.TILE_COMMANDS, new Select());

	private final TilePreview _preview;

	/**
	 * Creates a new {@link TileDescriptionControl}.
	 */
	public TileDescriptionControl(ComponentTile tile) {
		super(tile, COMMANDS);
		_preview = getModel().getPreview();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		if (tileAllowed()) {
			out.beginAttribute(ONCLICK_ATTR);
			out.write("return ");
			getCommand(Select.COMMAND_NAME).writeInvokeExpression(out, this);
			out.endAttribute();
		}
		out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
		out.endBeginTag();

		_preview.createPreview(getModel()).write(context, out);

		out.endTag(DIV);
	}

	private boolean tileAllowed() {
		return getModel().isAllowed();
	}

	@Override
	protected String getTypeCssClass() {
		return "card";
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		if (!tileAllowed()) {
			HTMLUtil.appendCSSClass(out, "cardNotAllowed");
		}
	}

	@Override
	public void buildInspector(InspectorModel inspector) {
		ComponentTile model = getModel();
		if (model instanceof ContainerComponentTile) {
			ContainerComponentTile containerModel = (ContainerComponentTile) model;
			inspector.add(new TileAllowedAssertion(containerModel));
			if (containerModel.getBusinessObject() instanceof PersonalizedTile) {
				inspector.add(new TileHiddenAssertion(containerModel));
			}
			if (containerModel.getBusinessObject() instanceof CompositeTile) {
				inspector.add(new TileContentsAssertion(containerModel));
			}
			inspector.add(new TilePathInfoPlugin(containerModel));
		} else if (model instanceof ComponentTileSupplier) {
			LayoutComponent tileContainer = ((ComponentTileSupplier) model).getRootComponent();
			if (tileContainer instanceof TileContainerComponent) {
				inspector.add(new TilePathAssertion((TileContainerComponent) tileContainer));
			} else {
				inspector.add(new DisplayedPathAssertion((RootTileComponent) tileContainer));
			}
		}

		LayoutComponent component = model.getTileComponent();
		if (component != null) {
			GuiInspectorPluginFactory.createComponentInformation(inspector, component);
		}
	}

	private static class Select extends ControlCommand {

		/**
		 * Command name of the {@link Select} command.
		 */
		public static final String COMMAND_NAME = "select";

		/**
		 * Creates a {@link Select}.
		 */
		public Select() {
			super(COMMAND_NAME);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CARD_SELECT_COMMAND;
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			ComponentTile tile = ((TileDescriptionControl) control).getModel();
			tile.displayTile();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}

