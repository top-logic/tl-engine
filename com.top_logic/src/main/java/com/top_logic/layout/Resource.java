/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.Objects;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.Resources;

/**
 * A programmatically defined resource consisting of label, image, tooltip, link, and CSS class.
 * 
 * <p>
 * This implementation can being displayed as application value in trees and tables.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Resource {
	
	/**
	 * {@link Mapping} returning {@link Resource#getUserObject()} for given {@link Resource}. 
	 * 
	 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class UserObject implements Mapping<Resource, Object> {

		@Override
		public Object map(Resource input) {
			if (input == null) {
				return null;
			}
			return input.getUserObject();
		}
		
	}

	/**
	 * Creates an image {@link Resources} without label.
	 * 
	 * @param image
	 *        See {@link #getImage()}
	 */
	public static Resource image(ThemeImage image) {
		return image(image, null);
	}

	/**
	 * Creates an image {@link Resources} without label.
	 * 
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 */
	public static Resource image(ThemeImage image, ResKey tooltip) {
		return image(image, tooltip, null);
	}

	/**
	 * Creates an image {@link Resources} without label.
	 * 
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 */
	public static Resource image(ThemeImage image, ResKey tooltip, String link) {
		return image(image, tooltip, link, null);
	}

	/**
	 * Creates an image {@link Resources} without label.
	 * 
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 * @param type
	 *        See {@link #getType()}.
	 */
	public static Resource image(ThemeImage image, ResKey tooltip, String link, String type) {
		return resource(null, image, tooltip, link, type);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 */
	public static Resource i18N(ResKey labelKey, ThemeImage image) {
		return i18N(labelKey, image, null);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 */
	public static Resource i18N(ResKey labelKey, ThemeImage image, ResKey tooltip) {
		return i18N(labelKey, image, tooltip, null);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 */
	public static Resource i18N(ResKey labelKey, ThemeImage image, ResKey tooltip, String link) {
		return i18N(labelKey, image, tooltip, link, null);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 * @param type
	 *        See {@link #getType()}.
	 */
	public static Resource i18N(ResKey labelKey, ThemeImage image, ResKey tooltip, String link, String type) {
		return resource(labelKey, image, tooltip, link, type);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 */
	public static Resource i18nFor(Object userObject, ResKey labelKey) {
		return i18nFor(userObject, labelKey, null);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 */
	public static Resource i18nFor(Object userObject, ResKey labelKey, ThemeImage image) {
		return i18nFor(userObject, labelKey, image, null);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 */
	public static Resource i18nFor(Object userObject, ResKey labelKey, ThemeImage image, ResKey tooltip) {
		return i18nFor(userObject, labelKey, image, tooltip, null);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 */
	public static Resource i18nFor(Object userObject, ResKey labelKey, ThemeImage image, ResKey tooltip, String link) {
		return i18nFor(userObject, labelKey, image, tooltip, link, null);
	}

	/**
	 * Creates a {@link Resources} from an I18N message key.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param labelKey
	 *        The resource key to derive {@link #getLabel()} (and optionally {@link #getTooltip()})
	 *        from.
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 * @param type
	 *        See {@link #getType()}.
	 */
	public static Resource i18nFor(Object userObject, ResKey labelKey, ThemeImage image, ResKey tooltip, String link,
			String type) {
		return resourceFor(userObject, labelKey, image, tooltip, link, type);
	}

	/**
	 * Creates a {@link Resources} without user object.
	 * 
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 */
	public static Resource resource(ResKey label, ThemeImage image) {
		return resource(label, image, null);
	}

	/**
	 * Creates a {@link Resources} without user object.
	 * 
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 */
	public static Resource resource(ResKey label, ThemeImage image, ResKey tooltip) {
		return resource(label, image, tooltip, null);
	}

	/**
	 * Creates a {@link Resources} without user object.
	 * 
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 */
	public static Resource resource(ResKey label, ThemeImage image, ResKey tooltip, String link) {
		return resource(label, image, tooltip, link, null);
	}

	/**
	 * Creates a {@link Resources} without user object.
	 * 
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 * @param type
	 *        See {@link #getType()}.
	 */
	public static Resource resource(ResKey label, ThemeImage image, ResKey tooltip, String link, String type) {
		return resourceFor(null, label, image, tooltip, link, type);
	}

	/**
	 * Creates a {@link Resources} for the given user object.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param label
	 *        See {@link #getLabel()}
	 */
	public static Resource resourceFor(Object userObject, ResKey label) {
		return resourceFor(userObject, label, null);
	}

	/**
	 * Creates a {@link Resources} for the given user object.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 */
	public static Resource resourceFor(Object userObject, ResKey label, ThemeImage image) {
		return resourceFor(userObject, label, image, null);
	}

	/**
	 * Creates a {@link Resources} for the given user object.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 */
	public static Resource resourceFor(Object userObject, ResKey label, ThemeImage image, ResKey tooltip) {
		return resourceFor(userObject, label, image, tooltip, null);
	}

	/**
	 * Creates a {@link Resources} for the given user object.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 */
	public static Resource resourceFor(Object userObject, ResKey label, ThemeImage image, ResKey tooltip, String link) {
		return resourceFor(userObject, label, image, tooltip, link, null);
	}

	/**
	 * Creates a {@link Resources} for the given user object and no CSS class.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 * @param type
	 *        See {@link #getType()}.
	 */
	public static Resource resourceFor(Object userObject, ResKey label, ThemeImage image, ResKey tooltip,
			String link,
			String type) {
		return resourceFor(userObject, label, image, tooltip, link, type, null);
	}

	/**
	 * Creates a {@link Resources} for the given user object.
	 * 
	 * @param userObject
	 *        See {@link #getUserObject()}.
	 * @param label
	 *        See {@link #getLabel()}
	 * @param image
	 *        See {@link #getImage()}
	 * @param tooltip
	 *        See {@link #getTooltip()}.
	 * @param link
	 *        See {@link #getLink()}.
	 * @param type
	 *        See {@link #getType()}.
	 * @param cssClass
	 *        See {@link #getCssClass()}.
	 */
	public static Resource resourceFor(Object userObject, ResKey label, ThemeImage image, ResKey tooltip,
			String link, String type, String cssClass) {
		return new Resource(userObject, label, image, tooltip, link, type, cssClass);
	}

	private final Object _userObject;

	private final ResKey _label;

	private final ThemeImage _image;

	private ResKey _tooltip;

	private String _link;

	private final String _type;

	private String _cssClass;

	private Resource(Object userObject, ResKey label, ThemeImage image, ResKey tooltip, String link, String type,
			String cssClass) {
		_userObject = userObject;
		_label = label;
		_image = image;
		_tooltip = tooltip != null ? tooltip : (label != null ? label.tooltip() : null);
		_link = link;
		_type = type;
		_cssClass = cssClass;
	}

	/**
	 * The wrapped object associated with this {@link Resource}.
	 */
	public Object getUserObject() {
		return _userObject;
	}

	/**
	 * The label to render for the {@link #getUserObject()}.
	 */
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * The {@link ThemeImage} to render.
	 * 
	 * <p>
	 * If none is given, an image is derived from {@link #getType()}.
	 * </p>
	 */
	public ThemeImage getImage() {
		return _image;
	}

	/**
	 * The {@link ResourceProvider#getTooltip(Object) tooltip}.
	 */
	public ResKey getTooltip() {
		return _tooltip;
	}

	/**
	 * The {@link ResourceProvider#getLink(DisplayContext, Object) link}.
	 */
	public String getLink() {
		return _link;
	}

	/**
	 * See {@link ResourceProvider#getType(Object)}
	 */
	public String getType() {
		return _type;
	}

	/**
	 * See {@link ResourceProvider#getCssClass(Object)}
	 */
	public String getCssClass() {
		return _cssClass;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_cssClass, _image, _label, _link, _tooltip, _type, _userObject);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		return Objects.equals(_cssClass, other._cssClass) && Objects.equals(_image, other._image)
				&& Objects.equals(_label, other._label) && Objects.equals(_link, other._link)
				&& Objects.equals(_tooltip, other._tooltip) && Objects.equals(_type, other._type)
				&& Objects.equals(_userObject, other._userObject);
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("Resource [");
		boolean first = true;
		first = appendToString(toString, first, "userObject", _userObject);
		first = appendToString(toString, first, "label", _label);
		first = appendToString(toString, first, "image", _image);
		first = appendToString(toString, first, "tooltip", _tooltip);
		first = appendToString(toString, first, "link", _link);
		first = appendToString(toString, first, "type", _type);
		first = appendToString(toString, first, "cssClass", _cssClass);
		toString.append("]");
		return toString.toString();
	}

	private boolean appendToString(StringBuilder out, boolean first, String key, Object value) {
		if (value != null) {
			if (first) {
				first = false;
			} else {
				out.append(", ");
			}
			out.append(key);
			out.append("=");
			out.append(value);
		}
		return first;
	}

	/**
	 * {@link ResourceProvider} for {@link Resource}s.
	 */
	public static class Provider implements ResourceProvider {

		/**
		 * Singleton {@link Resource.Provider} instance.
		 */
		public static final Resource.Provider INSTANCE = new Resource.Provider();

		/**
		 * Creates a new {@link Provider}.
		 * 
		 */
		protected Provider() {
			// Singleton constructor.
		}

		@Override
		public String getLabel(Object object) {
			Resource resource = resource(object);
			ResKey label = resource.getLabel();
			if (label != null) {
				return Resources.getInstance().getString(label);
			}

			Object userObject = resource.getUserObject();
			if (userObject != null) {
				return MetaResourceProvider.INSTANCE.getLabel(userObject);
			}

			return null;
		}

		@Override
		public String getTooltip(Object object) {
			Resource resource = resource(object);
			ResKey tooltip = resource.getTooltip();
			if (tooltip != null) {
				return Resources.getInstance().getStringOptional(tooltip);
			}

			Object userObject = resource.getUserObject();
			if (userObject != null) {
				return MetaResourceProvider.INSTANCE.getTooltip(userObject);
			}

			return null;
		}

		@Override
		public String getType(Object anObject) {
			Resource resource = resource(anObject);
			String type = resource.getType();
			return type;
		}

		@Override
		public ThemeImage getImage(Object object, Flavor flavor) {
			Resource resource = resource(object);
			ThemeImage image = resource.getImage();
			if (image != null) {
				return image;
			}

			String type = resource.getType();
			if (type != null) {
				return DefaultResourceProvider.getTypeImage(type, flavor);
			}

			Object userObject = resource.getUserObject();
			if (userObject != null) {
				return MetaResourceProvider.INSTANCE.getImage(userObject, flavor);
			}

			return null;
		}

		@Override
		public String getLink(DisplayContext context, Object anObject) {
			Resource resource = resource(anObject);
			String url = resource.getLink();
			if (!StringServices.isEmpty(url)) {
				StringBuilder link = new StringBuilder();
				link.append("window.open(");
				TagUtil.writeJsString(link, url);
				link.append(", '_blank');");
				return link.toString();
			}

			Object userObject = resource.getUserObject();
			if (userObject != null) {
				return GotoHandler.getJSCallStatement(userObject, null);
			}

			return null;
		}

		@Override
		public String getCssClass(Object anObject) {
			return resource(anObject).getCssClass();
		}

		private Resource resource(Object object) {
			return (Resource) object;
		}

	}

	/**
	 * {@link ResourceRenderer} for {@link Resource}s, that just renders the image with the label as
	 * alternative text and as tooltip, if no tooltip is set explicit.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class IconResourceRenderer extends ResourceRenderer<IconResourceRenderer.Config> {

		/**
		 * Special {@link Provider} for displaying a {@link Resource} just as icon.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public static class IconResources extends Provider {

			@Override
			public String getLabel(Object object) {
				return null;
			}

			@Override
			public String getTooltip(Object object) {
				String tooltip = super.getTooltip(object);
				if (StringServices.isEmpty(tooltip)) {
					// getLabel is overridden.
					tooltip = super.getLabel(object);
				}
				return tooltip;
			}
		}

		/**
		 * Typed configuration interface definition for {@link Resource.IconResourceRenderer}.
		 * 
		 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
		 */
		public interface Config extends ResourceRenderer.Config<Resource.IconResourceRenderer> {

			@Override
			@InstanceDefault(IconResources.class)
			ResourceProvider getResourceProvider();

		}

		/**
		 * Create a {@link Resource.IconResourceRenderer}.
		 * 
		 * @param context
		 *        the {@link InstantiationContext} to create the new object in
		 * @param config
		 *        the configuration object to be used for instantiation
		 */
		public IconResourceRenderer(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected void writeImage(DisplayContext context, TagWriter out, Object aValue, ThemeImage image)
				throws IOException {
			String altText;
			if (aValue instanceof Resource) {
				ResKey label = ((Resource) aValue).getLabel();
				altText = StringServices.nonNull(context.getResources().getString(label));
			} else {
				altText = StringServices.EMPTY_STRING;
			}
			XMLTag tag = image.toIcon();
			tag.beginBeginTag(context, out);
			out.writeAttribute(CLASS_ATTR, FormConstants.TYPE_IMAGE_CSS_CLASS);
			out.writeAttribute(ALT_ATTR, altText);
			out.writeAttribute(TITLE_ATTR, StringServices.EMPTY_STRING); // Avoid popup for IE
			out.writeAttribute(BORDER_ATTR, 0);
			tag.endEmptyTag(context, out);
		}

	}

}
