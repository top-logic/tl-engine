/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DefaultPopupMenuModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.util.css.CssUtil;

/**
 * {@link ConfiguredTilePreview} that displays an optional icon and the label of a
 * {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LabelBasedPreview<C extends LabelBasedPreview.Config<?>> extends ConfiguredTilePreview<C> {

	private static final String CSS_PREVIEW_ERROR = "preview-error";

	private static final TypedAnnotatable.Property<Boolean> ERROR =
		TypedAnnotatable.property(Boolean.class, "Preview error marker", Boolean.FALSE);

	/** Name of the outer box containing the preview */
	protected static final String CSS_PREVIEW_BOX_OUTER = "previewBoxOuter";

	/** Name of the inner box containing the preview */
	protected static final String CSS_PREVIEW_BOX_INNER = "previewBoxInner";

	/**
	 * Configuration of a {@link LabelBasedPreview}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends LabelBasedPreview<?>> extends ConfiguredTilePreview.Config<I> {

		/** Configuration name for the value of {@link #getIcon()}. */
		String ICON = "icon";

		/**
		 * A static icon for the {@link TileLayout}.
		 * 
		 * @return May be <code>null</code>.
		 */
		@Name(ICON)
		ThemeImage getIcon();

		/**
		 * Setter for {@link #getIcon()}.
		 */
		void setIcon(ThemeImage icon);

	}

	/**
	 * Creates a new {@link LabelBasedPreview}.
	 */
	public LabelBasedPreview(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createPreview(ComponentTile tile) {
		DisplayContext interaction = interaction();
		try {
			Object errors = InlineList.newInlineList();
			HTMLFragment label;
			try {
				label = label(tile);
			} catch (RuntimeException ex) {
				errors = InlineList.add(ResKey.class, errors,
					I18NConstants.ERROR_LABEL_PREVIEW_LABEL__MSG.fill(errorMessage(ex)));
				label = text("ERROR");
			}
			HTMLFragment description;
			try {
				description = description(tile);
			} catch (RuntimeException ex) {
				errors = InlineList.add(ResKey.class, errors,
					I18NConstants.ERROR_LABEL_PREVIEW_DESCRIPTION__MSG.fill(errorMessage(ex)));
				description = text("ERROR");
			}
			HTMLFragment previewContent;
			try {
				previewContent = previewContent(tile);
			} catch (RuntimeException ex) {
				errors = InlineList.add(ResKey.class, errors,
					I18NConstants.ERROR_LABEL_PREVIEW_CONTENT__MSG.fill(errorMessage(ex)));
				previewContent = defaultPreviewContent(text("ERROR"), null);
			}
			HTMLFragment preview = concat(
				label,
				description,
				previewContent,
				commands(tile));
			if (InlineList.isEmpty(errors)) {
				if (interaction.get(ERROR).booleanValue()) {
					return div(CSS_PREVIEW_ERROR, preview);
				} else {
					return preview;
				}
			} else {
				InfoService.showErrorList(InlineList.toList(ResKey.class, errors));
				return div(CSS_PREVIEW_ERROR, preview);
			}
		} finally {
			interaction.reset(ERROR);
		}
	}

	private Object errorMessage(RuntimeException ex) {
		if (ex instanceof I18NFailure) {
			return ((I18NFailure) ex).getErrorKey();
		}
		return ex.getMessage();
	}

	/**
	 * Marks the result in {@link #createPreview(ComponentTile)} with error border.
	 * 
	 * <p>
	 * Method to call from sub classes, when an error happens during creation of the preview or some
	 * parts of it.
	 * </p>
	 */
	protected final void markPreviewWithError() {
		DisplayContext dc = interaction();
		dc.set(ERROR, Boolean.TRUE);
	}

	private DisplayContext interaction() {
		return DefaultDisplayContext.getDisplayContext();
	}

	/**
	 * Creates the commands that can be executed for the given {@link ComponentTile}.
	 */
	protected HTMLFragment commands(ComponentTile tile) {
		Menu menu = tile.getBurgerMenu().get();
		if (menu == null || menu.isEmpty()) {
			return Fragments.empty();
		}

		DefaultPopupMenuModel model = new DefaultPopupMenuModel(Icons.BURGER_COMMANDS_ICONS, menu);
		return new PopupMenuButtonControl(model, DefaultToolBar.BUTTON_RENDERER);
	}

	/**
	 * Creates the content of the preview area.
	 */
	protected HTMLFragment previewContent(ComponentTile tile) {
		return defaultPreviewContent(image(tile), getAdditionalPreviewClass(tile));
	}

	/**
	 * Default frame for the preview area.
	 * 
	 * @param content
	 *        The actual content of the preview.
	 * @param additionalPreviewClass
	 *        Optional additional class for the preview box. May be <code>null</code>.
	 */
	public static HTMLFragment defaultPreviewContent(HTMLFragment content, String additionalPreviewClass) {
		return div(
			attributes(css(CssUtil.joinCssClasses(CSS_PREVIEW_BOX_OUTER, additionalPreviewClass))),
			div(
				attributes(css(CSS_PREVIEW_BOX_INNER)),
				content));
	}

	/**
	 * Implementation specific CSS class to be able to style the preview in its own way.
	 * 
	 * @param tile
	 *        for which the preview is created.
	 * 
	 * @return May be <code>null</code>, but should not be empty.
	 */
	protected String getAdditionalPreviewClass(ComponentTile tile) {
		return null;
	}

	/**
	 * Creates the label (including styling) for the preview.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create label for.
	 */
	protected HTMLFragment label(ComponentTile tile) {
		return h5("card-title",
			a(attributes(attribute(HTMLConstants.HREF_ATTR, HTMLConstants.HREF_EMPTY_LINK)), 
				labelContent(tile)));
	}

	/**
	 * /** Creates the actual label for the preview.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create label for.
	 */
	protected HTMLFragment labelContent(ComponentTile tile) {
		return message(tile.getTileLabel());
	}

	/**
	 * Creates the icon (including styling) for the preview.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create image for.
	 */
	protected HTMLFragment image(ComponentTile tile) {
		ThemeImage image = icon(tile);
		if (image != null) {
			return span("card-img-top", image);
		} else {
			return empty();
		}
	}

	/**
	 * Creates the actual displayed icon for the preview.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create image for.
	 */
	protected ThemeImage icon(ComponentTile tile) {
		return getConfig().getIcon();
	}

	/**
	 * Creates a description (including styling) that is displayed under the label.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create description for.
	 */
	protected HTMLFragment description(ComponentTile tile) {
		HTMLFragment content = descriptionContent(tile);
		if (content != empty()) {
			return div("card-text", content);
		} else {
			return empty();
		}
	}

	/**
	 * Creates the actual description for the preview.
	 * 
	 * @param tile
	 *        The {@link ComponentTile} to create description for.
	 */
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		return empty();
	}

}

