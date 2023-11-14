/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.tag.FormPageTag;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.form.tag.PageControl;
import com.top_logic.layout.form.tag.PageRenderer;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLMimeTypes;

/**
 * {@link LayoutControlProvider} that renders a {@link FormMember} of a {@link FormHandler} by
 * directly creating a control.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DirectFormDisplay {

	private Config _config;

	/**
	 * Configuration options for {@link DirectFormDisplay}.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * @see #getCssClass()
		 */
		String CSS_CLASS = "cssClass";

		/**
		 * @see #getControlProvider()
		 */
		String CONTROL_PROVIDER = "controlProvider";

		/**
		 * @see #getDisplayedMember()
		 */
		String DISPLAYED_MEMBER = "displayedMember";

		/**
		 * @see #getNoModelKey()
		 */
		String NO_MODEL_KEY = "noModelKey";

		/**
		 * @see #getShowHeader()
		 */
		String SHOW_HEADER = "showHeader";

		/**
		 * @see #getTitleKey()
		 */
		String TITLE_KEY = "titleKey";

		/**
		 * @see #getSubTitleKey()
		 */
		String SUB_TITLE_KEY = "subTitleKey";

		/**
		 * @see #getTitleNoModelKey()
		 */
		String TITLE_NO_MODEL_KEY = "titleNoModelKey";

		/**
		 * @see #getImage()
		 */
		String IMAGE = "image";

		/**
		 * @see #getImageTooltipKey()
		 */
		String IMAGE_TOOLTIP_KEY = "imageTooltipKey";

		/**
		 * @see #getImageOverlay()
		 */
		String IMAGE_OVERLAY = "imageOverlay";

		/**
		 * An additional custom CSS class to set in the layout control tag.
		 */
		@Name(CSS_CLASS)
		@StringDefault(FormConstants.FORM_BODY_CSS_CLASS)
		@Nullable
		String getCssClass();

		/** Setter for {@link #getCssClass()}. */
		void setCssClass(String value);

		/**
		 * {@link ControlProvider} to use to render primitive fields.
		 */
		@Name(CONTROL_PROVIDER)
		@InstanceFormat
		@InstanceDefault(DefaultFormFieldControlProvider.class)
		@NonNullable
		ControlProvider getControlProvider();

		/** Setter for {@link #getControlProvider()}. */
		void setControlProvider(ControlProvider value);

		/**
		 * Name of the {@link FormMember} in the {@link FormHandler}'s {@link FormContext} to
		 * display.
		 */
		@Name(DISPLAYED_MEMBER)
		@StringDefault(".")
		String getDisplayedMember();

		/** Setter for {@link #getDisplayedMember()}. */
		void setDisplayedMember(String value);

		/**
		 * {@link ResKey} for creating a "no model" message.
		 * 
		 * <p>
		 * If not given, a {@link FormTag#DEFAULT_NO_MODEL_KEY_SUFFIX suffix} to the component's
		 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getResPrefix()} is used. If
		 * this resource is also not defined, a generic message is shown.
		 * </p>
		 */
		@Name(NO_MODEL_KEY)
		ResKey getNoModelKey();

		/** Setter for {@link #getNoModelKey()}. */
		void setNoModelKey(ResKey value);

		/**
		 * Whether to show a header over the form displaying the model name.
		 * 
		 * @see #getTitleKey()
		 * @see #getTitleNoModelKey()
		 * @see #getImage()
		 */
		@Name(SHOW_HEADER)
		boolean getShowHeader();

		/** Setter for {@link #getShowHeader()}. */
		void setShowHeader(boolean value);

		/**
		 * The {@link ResKey} for the text to display in the title if the component has a model.
		 * 
		 * <p>
		 * The model name is passed as argument to the resource message.
		 * </p>
		 */
		@Name(TITLE_KEY)
		ResKey1 getTitleKey();

		/** Setter for {@link #getTitleKey()}. */
		void setTitleKey(ResKey1 value);

		/**
		 * The {@link ResKey} for the text to display in the title if the component has no model.
		 */
		@Name(TITLE_NO_MODEL_KEY)
		ResKey getTitleNoModelKey();

		/** Setter for {@link #getTitleNoModelKey()}. */
		void setTitleNoModelKey(ResKey value);

		/**
		 * The {@link ResKey} for the text to display in the sub-title row.
		 * 
		 * <p>
		 * If not given, the model type name is displayed.
		 * </p>
		 */
		@Name(SUB_TITLE_KEY)
		ResKey getSubTitleKey();

		/** Setter for {@link #getSubTitleKey()}. */
		void setSubTitleKey(ResKey value);

		/**
		 * The icon to display in the header.
		 * 
		 * @see #getImageTooltipKey()
		 * @see #getImageOverlay()
		 */
		@Name(IMAGE)
		ThemeImage getImage();

		/** Setter for {@link #getImage()}. */
		void setImage(ThemeImage value);

		/**
		 * The overlay icon to display over {@link #getImage()}.
		 * 
		 * @see #getImage()
		 */
		@Name(IMAGE_OVERLAY)
		ThemeImage getImageOverlay();

		/** Setter for {@link #getImageOverlay()}. */
		void setImageOverlay(ThemeImage value);

		/**
		 * {@link ResKey} for the tooltip over the header image.
		 * 
		 * @see #getImage()
		 */
		@Name(IMAGE_TOOLTIP_KEY)
		ResKey getImageTooltipKey();

		/** Setter for {@link #getImageTooltipKey()}. */
		void setImageTooltipKey(ResKey value);

	}

	/**
	 * Creates a {@link DirectFormDisplay} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DirectFormDisplay(Config config) {
		_config = config;
	}

	/**
	 * The configuration that was used to build this {@link DirectFormDisplay}.
	 */
	public Config getConfig() {
		return _config;
	}

	/**
	 * Creates an {@link HTMLFragment} to render the given {@link FormHandler}.
	 * 
	 * @param formHandler
	 *        The {@link FormHandler} defining the {@link FormContext} to render.
	 * @param model
	 *        The model to display view for. May be <code>null</code>.
	 * @param noModelKey
	 *        {@link ResKey} to display when no model is available. May be <code>null</code> if some
	 *        "no model" key is {@link Config#getNoModelKey() configured}.
	 * 
	 * @see com.top_logic.layout.form.tag.I18NConstants#NO_MODEL
	 */
	public HTMLFragment createView(FormHandler formHandler, Object model, ResKey noModelKey) {
		HTMLFragment contents = createContents(formHandler, noModelKey);
		if (getConfig().getShowHeader()) {
			PageControl page = new PageControl(PageRenderer.getThemeInstance());
			page.setTitleContent(createTitleContent(model, noModelKey));
			page.setSubtitleContent(createSubtitleContent(model));
			page.setIconBarContent(createIconBarContent(formHandler, model));
			page.setBodyContent(contents);
			return page;
		}
		return contents;
	}

	private HTMLFragment createTitleContent(Object model, ResKey noModelKey) {
		if (model != null) {
			return createTitleWithModel(model);
		} else {
			return createTitleWithoutModel(noModelKey);
		}
	}

	private HTMLFragment createTitleWithModel(Object model) {
		String modelLabel = MetaLabelProvider.INSTANCE.getLabel(model);
		ResKey1 titleKey = getConfig().getTitleKey();
		if (titleKey != null) {
			return message(titleKey.fill(modelLabel));
		} else {
			return text(modelLabel);
		}
	}

	private HTMLFragment createTitleWithoutModel(ResKey noModelKey) {
		ResKey titleKey = getConfig().getTitleNoModelKey();
		if (titleKey != null) {
			// Custom no-model title.
			return message(titleKey);
		} else {
			// Default missing-model title.
			return noModelDisplay(noModelKey);
		}
	}

	private HTMLFragment createSubtitleContent(Object model) {
		ResKey subTitleKey = getConfig().getSubTitleKey();
		if (subTitleKey != null) {
			return message(subTitleKey);
		} else {
			TLType type = getModelType(model);
			if (type == null) {
				return text(null);
			}
			return text(MetaResourceProvider.INSTANCE.getLabel(type));
		}
	}

	private HTMLFragment createIconBarContent(FormHandler handler, Object model) {
		IconControl typeIcon = new IconControl();
		IconControl actionIcon = new IconControl();

		ThemeImage actionImage = getActionThemeImage(handler);
		actionIcon.setCssClass(FormPageTag.ACTION_IMAGE_CSS_CLASS);
		if (actionImage != null) {
			actionIcon.setSrc(actionImage.toIcon());
			typeIcon.setCssClass(FormPageTag.IMAGE_WITH_ACTION_CSS_CLASS);
		} else {
			typeIcon.setCssClass(FormPageTag.IMAGE_CSS_CLASS);
		}

		initTypeIcon(model, typeIcon);
		initActionIcon(model, actionIcon);

		return concat(typeIcon, actionIcon);
	}

	private ThemeImage getActionThemeImage(FormHandler handler) {
		// Note: Must test against null (not empty string) to allow removing the default action
		// image on the JSP by setting action="".
		ThemeImage actionImage = getConfig().getImageOverlay();
		if (actionImage != null) {
			return actionImage;
		} else {
			if (handler instanceof AbstractCreateComponent) {
				return Icons.PLUS48;
			}
			if (inEditMode(handler)) {
				return Icons.EDIT48;
			}
			return null;
		}
	}

	private boolean inEditMode(FormHandler component) {
		return (component instanceof Editor && ((Editor) component).isInEditMode());
	}

	private void initActionIcon(Object model, IconControl icon) {
		initTypeTooltip(model, icon);
	}

	private void initTypeIcon(Object model, IconControl icon) {
		initTypeIconSrc(model, icon);
		initTypeTooltip(model, icon);
	}

	private void initTypeIconSrc(Object model, IconControl icon) {
		ThemeImage image = getConfig().getImage();
		if (image != null) {
			icon.setSrc(image.toIcon());
		} else {
			TLType type = getModelType(model);
			if (type != null) {
				icon.setSrcKey(TLMimeTypes.getInstance().getMimeTypeImageLarge(TLModelUtil.qualifiedNameDotted(type),
					ThemeImage.none()));
			} else {
				icon.setSrc(null);
			}
		}
	}

	TLType getModelType(Object model) {
		return TLModelUtil.type(model);
	}

	private void initTypeTooltip(Object model, IconControl icon) {
		ResKey imageTooltipKey = getConfig().getImageTooltipKey();
		if (imageTooltipKey != null) {
			icon.setTooltip(Resources.getInstance().getString(imageTooltipKey));
		} else {
			TLType type = getModelType(model);
			if (type != null) {
				icon.setTooltip(MetaResourceProvider.INSTANCE.getLabel(type));
			}
		}
	}

	/**
	 * Creates the fragment that actually renders the given {@link LayoutComponent}.
	 */
	private HTMLFragment createContents(FormHandler handler, ResKey noModelKey) {
		FormMember formMember = getFormMemberToDisplay(handler);
		if (formMember == null) {
			return noModelView(noModelKey);
		}
		ControlProvider controlProvider = getConfig().getControlProvider();
		return div(getConfig().getCssClass(),
			controlProvider.createFragment(formMember));
	}

	private HTMLFragment noModelView(ResKey noModelKey) {
		return div(FormTag.NO_MODEL_CSS_CLASS,
			table(
				tr(
					td(
						noModelDisplay(noModelKey)
					)
				)
			)
		);
	}

	private HTMLFragment noModelDisplay(ResKey noModelKey) {
		ResKey configuredKey = getConfig().getNoModelKey();
		ResKey key;
		if (configuredKey != null) {
			// Explicitly specified, no further default.
			key = configuredKey;
		} else {
			key = noModelKey;
		}
		return message(key);
	}

	/**
	 * Retrieve the {@link FormMember} of the given {@link FormHandler} to display.
	 */
	protected FormMember getFormMemberToDisplay(FormHandler component) {
		FormContext formContext = component.getFormContext();
		if (formContext == null) {
			return null;
		}
		return FormGroup.getMemberByRelativeName(formContext, getConfig().getDisplayedMember());
	}

}
