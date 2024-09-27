/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.form.control.IconChooserEditor;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * An image from a {@link Theme}.
 * 
 * <p>
 * A {@link ThemeImage} is declared as a public static field in a class named <code>Icons</code>
 * within the using package. The <code>Icons</code> class must extend {@link IconsBase} to have the
 * declarations filled automatically.
 * </p>
 * 
 * <p>
 * A {@link ThemeImage} field <code>MY_IMAGE</code>in a class <code>my.package.Icons</code>
 * corresponds to a theme variable <code>my.package.Icons.MY_IMAGE</code> that gives the concrete
 * icon resource.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Icon")
@Format(ThemeImageConfigFormat.class)
@PropertyEditor(IconChooserEditor.class)
public abstract class ThemeImage implements HTMLFragment {

	/**
	 * Prefix prepended to mime types to build theme variables for configuring icons for that mime
	 * type.
	 */
	public static final String MIME_PREFIX = "mime.";

	private static final String LARGE_VAR_SUFFIX = ".large";

	/**
	 * The icon key for "no icon".
	 */
	public static final String NONE_ICON = "none";

	/**
	 * Prefix to an icon specification that references another theme variable.
	 * 
	 * @see #themeIcon(String)
	 */
	public static final String THEME_PREFIX = "theme:";

	/**
	 * Prefix to an icon that refers to the icon of a mime type.
	 * 
	 * @see #typeIcon(String)
	 */
	public static final String TYPE_PREFIX = "mime:";

	/**
	 * Prefix to an icon that refers to the large icon of a mime type.
	 * 
	 * @see #typeIconLarge(String)
	 */
	public static final String LARGE_PREFIX = "large:";

	/**
	 * Prefix to a locale-sensitive icon defined by a {@link ResKey}.
	 * 
	 * @see #i18n(ResKey)
	 */
	public static final String I18N_PREFIX = "i18n:";

	/**
	 * Prefix to an icon-font icon.
	 * 
	 * @see #cssIcon(String)
	 */
	public static final String CSS_PREFIX = "css:";

	/**
	 * Prefix to an multicolor icon-font icon.
	 * 
	 * @see #cssIcon(String)
	 */
	public static final String COLORED_CSS_PREFIX = "colored:";

	/**
	 * The specification of this {@link ThemeImage} as understood by {@link #icon(String)}.
	 */
	public abstract String toEncodedForm();
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @return true if this object is the same as the obj argument; false otherwise.
	 */
	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	/**
	 * Instead of invoking this constructor, declare a {@link ThemeImage} field in an
	 * <code>Icons</code> class in your package extending {@link IconsBase}.
	 */
	ThemeImage() {
		super();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toEncodedForm();
	}

	/**
	 * Creates a {@link ThemeImage} with the given image key.
	 * 
	 * @param imageKey
	 *        The name of the concrete image.
	 * 
	 * @deprecated Consider declaring a {@link ThemeImage} field in <code>Icons</code> class
	 *             extending {@link IconsBase} in your package.
	 * 
	 * @see #resourceIcon(String)
	 * @see #themeIcon(String)
	 * @see #cssIcon(String)
	 * @see #typeIcon(String)
	 */
	@Deprecated
	public static ThemeImage icon(String imageKey) {
		return internalIcon(imageKey);
	}

	/**
	 * Utility for internally loading icons from their serialized form.
	 */
	@FrameworkInternal
	public static ThemeImage internalDecode(String imageKey) {
		return internalIcon(imageKey);
	}

	/**
	 * Utility to create test icons without triggering production-level tools.
	 */
	@FrameworkInternal
	public static ThemeImage forTest(String imageKey) {
		return internalIcon(imageKey);
	}

	/**
	 * Creates a {@link ThemeImage} with the given image key that must have a server-side resource.
	 * 
	 * <p>
	 * Consider declaring a {@link ThemeImage} field in <code>Icons</code> class extending
	 * {@link IconsBase} in your package.
	 * </p>
	 * 
	 * <p>
	 * Note: When declaring a field of type {@link ThemeImage.Img} in an <code>Icons</code> class,
	 * the icon cannot be modified through a theme variable.
	 * </p>
	 * 
	 * @param imageKey
	 *        The name of the concrete image.
	 *
	 * @deprecated Use an {@link IconsBase} subclass and declare an icon pseudo-constant.
	 */
	@Deprecated
	public static ThemeImage.Img resourceIcon(String imageKey) {
		return internalImg(imageKey);
	}

	/**
	 * Creates a {@link ThemeImage} with the given image key in the given fixed {@link Theme}.
	 * 
	 * @param imageKey
	 *        The name of the concrete image.
	 * @param imagePath
	 *        The resolved application-local path to the icon.
	 */
	@FrameworkInternal
	public static ThemeImage.Img resourceIcon(String imageKey, String imagePath) {
		return new ResolvedImg(imageKey, imagePath);
	}

	static ThemeImage internalIcon(String imageKey) {
		if (imageKey.startsWith(THEME_PREFIX)) {
			return themeIcon(imageKey.substring(THEME_PREFIX.length()));
		} else if (imageKey.startsWith(CSS_PREFIX)) {
			return cssIcon(imageKey.substring(CSS_PREFIX.length()));
		} else if (imageKey.startsWith(COLORED_CSS_PREFIX)) {
			return coloredCssIcon(imageKey.substring(COLORED_CSS_PREFIX.length()));
		} else if (imageKey.startsWith(I18N_PREFIX)) {
			return i18n(ResKey.decode(imageKey.substring(I18N_PREFIX.length())));
		} else if (imageKey.startsWith(TYPE_PREFIX)) {
			return typeIcon(imageKey.substring(TYPE_PREFIX.length()));
		} else if (imageKey.startsWith(LARGE_PREFIX)) {
			return typeIconLarge(imageKey.substring(LARGE_PREFIX.length()));
		} else if (imageKey.equals(NONE_ICON)) {
			return none();
		} else {
			return internalImg(imageKey);
		}
	}

	/**
	 * The invisible icon.
	 */
	public static ThemeImage none() {
		return NoIcon.INSTANCE;
	}

	/**
	 * Internally create an {@link Img}.
	 */
	@FrameworkInternal
	public static ThemeImage.Img internalImg(String imageKey) {
		return new UnresolvedImg(imageKey);
	}

	/**
	 * Create a {@link ThemeImage} that can be rendered diffrently depending on the user's locale.
	 * 
	 * @param imageKey
	 *        The resource key that gives the concrete icon.
	 */
	public static ThemeImage i18n(ResKey imageKey) {
		return new I18NIcon(imageKey);
	}

	/**
	 * Creates a {@link ThemeImage} that may have a different type depending on the user's theme.
	 * 
	 * <p>
	 * Note: Instead of calling this method explicitly, an image resource should be defined using
	 * the {@link #THEME_PREFIX} protocol, see {@link IconsBase}.
	 * </p>
	 * 
	 * @param themeVar
	 *        The name of the theme variable that gives the concrete icon.
	 * 
	 * @deprecated Use a {@link ThemeIcon} pseudo-constant in an {@link IconsBase} subclass instead.
	 */
	@Deprecated
	public static ThemeImage themeIcon(String themeVar) {
		return themeIcon(themeVar, null);
	}

	/**
	 * Creates a {@link ThemeImage} that may have a different type depending on the user's theme.
	 * 
	 * <p>
	 * Note: Instead of calling this method explicitly, an image resource should be defined using
	 * the {@link #THEME_PREFIX} protocol, see {@link IconsBase}.
	 * </p>
	 * 
	 * @param themeVar
	 *        The name of the theme variable that gives the concrete icon.
	 * @param defaultValue
	 *        The {@link ThemeImage} to use, if the theme does not provide a variable binding with
	 *        the given name.
	 */
	public static ThemeImage themeIcon(String themeVar, ThemeImage defaultValue) {
		return new DynamicThemeIcon(themeVar, nonNull(defaultValue));
	}

	/**
	 * Creates an icon-font icon with the given CSS classes set.
	 *
	 * <p>
	 * Note: Instead of calling this method explicitly, an image resource should be defined using
	 * the {@link #CSS_PREFIX} protocol, see {@link IconsBase}.
	 * </p>
	 * 
	 * @param classes
	 *        The CSS classes identifying the icon.
	 * 
	 * @deprecated Use an {@link IconsBase} subclass and declare an icon pseudo-constant.
	 */
	@Deprecated
	public static ThemeImage cssIcon(String classes) {
		return new CssIcon(classes);
	}

	/**
	 * Creates an icon-font multicolor icon with the given CSS classes set.
	 *
	 * <p>
	 * Note: Instead of calling this method explicitly, an image resource should be defined using
	 * the {@link #CSS_PREFIX} protocol, see {@link IconsBase}.
	 * </p>
	 * 
	 * @param classes
	 *        The CSS classes identifying the icon.
	 * 
	 * @deprecated Use an {@link IconsBase} subclass and declare an icon pseudo-constant.
	 */
	@Deprecated
	public static ThemeImage coloredCssIcon(String classes) {
		return new ColoredCssIcon(classes);
	}

	/**
	 * {@link ThemeImage} dispatching on the current theme using the internal theme cache.
	 * 
	 * <p>
	 * Note, this functionality is only possible, if the given <code>themeVar</code> is used by a
	 * singleton instance, only.
	 * </p>
	 * 
	 * @see #themeIcon(String, ThemeImage)
	 */
	@FrameworkInternal
	public static ThemeImage singletonThemeIcon(String themeVar, ThemeImage defaultValue) {
		return new CachedThemeIcon(themeVar, nonNull(defaultValue));
	}

	private static ThemeImage nonNull(ThemeImage defaultValue) {
		return defaultValue == null ? none() : defaultValue;
	}

	/**
	 * Creates a {@link ThemeImage} that represents the given MIME type.
	 */
	public static ThemeImage typeIcon(String mimeType) {
		return typeIcon(mimeType, none());
	}

	/**
	 * Creates a {@link ThemeImage} that represents the given MIME type.
	 */
	public static ThemeImage typeIcon(String mimeType, ThemeImage defaultIcon) {
		return themeIcon(MIME_PREFIX + mimeType, defaultIcon);
	}

	/**
	 * Creates a high resolution {@link ThemeImage} that represents the given MIME type.
	 */
	public static ThemeImage typeIconLarge(String mimeType) {
		return typeIconLarge(mimeType, none());
	}

	/**
	 * Creates a high resolution {@link ThemeImage} that represents the given MIME type.
	 */
	public static ThemeImage typeIconLarge(String mimeType, ThemeImage defaultIcon) {
		return themeIcon(MIME_PREFIX + mimeType + LARGE_VAR_SUFFIX, typeIcon(mimeType, defaultIcon));
	}

	/**
	 * Resolves to the concrete icon in case of a proxy implementation.
	 */
	public abstract ThemeImage resolve();

	/**
	 * Convenience method for rendering this image.
	 */
	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		writeWithTooltip(context, out, null);
	}

	/**
	 * Convenience method for rendering this image with a HTML tooltip.
	 */
	public void writeWithTooltip(DisplayContext context, TagWriter out, ResKey tooltipHTML) throws IOException {
		writeWithCssTooltip(context, out, null, tooltipHTML);
	}

	/**
	 * Convenience method for rendering this image with an added CSS class
	 */
	public void writeWithCss(DisplayContext context, TagWriter out, String cssClass) throws IOException {
		writeWithCssTooltip(context, out, cssClass, null);
	}

	/**
	 * Convenience method for rendering this image with an added CSS class and a HTML tooltip.
	 */
	public void writeWithCssTooltip(DisplayContext context, TagWriter out, String cssClass, ResKey tooltipHTML)
			throws IOException {
		write(context, out, cssClass, tooltipHTML, null);
	}

	/**
	 * Convenience method for rendering this image with an added CSS class, a HTML tooltip, and a
	 * tooltip caption.
	 */
	public void write(DisplayContext context, TagWriter out, String cssClass, ResKey tooltipHTML,
			ResKey captionHTML) throws IOException {
		write(context, out, null, cssClass, tooltipHTML, captionHTML);
	}

	/**
	 * Convenience method for rendering this image with {@link HTMLConstants#ID_ATTR ID}, an added
	 * {@link HTMLConstants#CLASS_ATTR CSS class}, a HTML tooltip, and a tooltip caption.
	 * 
	 * @param id
	 *        The client side unique ID of the icon. May be <code>null</code>.
	 * @param cssClass
	 *        A CSS class for the icon. May be <code>null</code>.
	 * @param tooltipHTML
	 *        The tooltip for the icon. May be <code>null</code>.
	 * @param captionHTML
	 *        The caption of the tooltip for the icon. May be <code>null</code>.
	 */
	public void write(DisplayContext context, TagWriter out, String id, String cssClass, ResKey tooltipHTML,
			ResKey captionHTML) throws IOException {
		XMLTag icon = toIcon();
		icon.beginBeginTag(context, out);
		out.writeAttribute(HTMLConstants.ID_ATTR, id);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
		out.writeAttribute(HTMLConstants.ALT_ATTR, StringServices.EMPTY_STRING);
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltipHTML, captionHTML);
		out.writeAttribute(HTMLConstants.TITLE_ATTR, StringServices.EMPTY_STRING);
		icon.endEmptyTag(context, out);
	}

	/**
	 * Convenience method for rendering this image with a plain text tooltip.
	 */
	public void writeWithPlainTooltip(DisplayContext context, TagWriter out, ResKey tooltipText) throws IOException {
		writeWithCssPlainTooltip(context, out, null, tooltipText);
	}

	/**
	 * Convenience method for rendering this image with a CSS class and a plain text tooltip.
	 */
	public void writeWithCssPlainTooltip(DisplayContext context, TagWriter out, String cssClass, ResKey tooltipText)
			throws IOException {
		XMLTag icon = toIcon();
		icon.beginBeginTag(context, out);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
		out.writeAttribute(HTMLConstants.ALT_ATTR,
			tooltipText == null ? StringServices.EMPTY_STRING : context.getResources().getString(tooltipText));
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, tooltipText);
		out.writeAttribute(HTMLConstants.TITLE_ATTR, StringServices.EMPTY_STRING);
		icon.endEmptyTag(context, out);
	}

	/**
	 * Creates a {@link HTMLFragment} from this image.
	 */
	public HTMLFragment toFragment(final String cssClass, final ResKey tooltipHTML, final ResKey captionHTML) {
		return new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				ThemeImage.this.write(context, out, cssClass, tooltipHTML, captionHTML);
			}
		};
	}

	/**
	 * Creates a {@link XMLTag} rendering this image a plain icon.
	 */
	public abstract XMLTag toIcon();

	/**
	 * Creates a {@link XMLTag} rendering this image a focusable element.
	 */
	public abstract XMLTag toButton();

	/**
	 * {@link ThemeImage} that has a URL resource.
	 */
	static class UnresolvedImg extends Img {

		UnresolvedImg(String imageKey) {
			super(imageKey);
		}

		@Override
		public ThemeImage resolve() {
			String imagePath = getImagePath();
			return new ResolvedImg(imagePath, ThemeFactory.getTheme().getFileLink(imagePath));
		}

		@Override
		public
		final String getFileLink() {
			String imageKey = getImagePath();
			String imagePath = ThemeFactory.getTheme().getFileLink(imageKey);
			assert imagePath != null : "A theme image must be found at least in the default theme: " + imageKey;
			return imagePath;
		}

	}

	/**
	 * Completely resolved {@link ThemeImage} displaying an image resource of a concrete theme.
	 * 
	 * <p>
	 * A {@link ResolvedImg} can be cached in the context of a theme and needs no further theme
	 * dispatch when rendering.
	 * </p>
	 */
	static class ResolvedImg extends Img {
		private final String _imagePath;

		ResolvedImg(String imageKey, String imagePath) {
			super(imageKey);
			_imagePath = imagePath;
		}

		@Override
		public ThemeImage resolve() {
			return this;
		}

		@Override
		public
		final String getFileLink() {
			return _imagePath;
		}

	}

	/**
	 * {@link ThemeImage} that has a URL resource.
	 */
	public abstract static class Img extends ThemeImage implements DisplayValue {

		private String _imageKey;

		Img(String imageKey) {
			_imageKey = imageKey;
		}

		static boolean isLegalThemeImagePath(String key) {
			return !StringServices.isEmpty(key) && key.charAt(0) == '/';
		}

		@Override
		public String toEncodedForm() {
			return getImagePath();
		}

		/**
		 * The theme-local path of the image resource.
		 * 
		 * <p>
		 * The theme-extended image path can be written into the <code>src</code> attribute of an
		 * <code>img</code> tag.
		 * </p>
		 * 
		 * @see Theme#getFileLink(String)
		 */
		public String getImagePath() {
			return _imageKey;
		}

		/**
		 * @deprecated Use {@link #write(DisplayContext, TagWriter)}, {@link #toIcon()}, or
		 *             {@link #toButton()}
		 */
		@Override
		@Deprecated
		public String get(DisplayContext context) {
			return imageUrl(context.getContextPath());
		}

		/**
		 * @deprecated Use {@link #write(DisplayContext, TagWriter)}, {@link #toIcon()}, or
		 *             {@link #toButton()}
		 */
		@Override
		@Deprecated
		public void append(DisplayContext context, Appendable out) throws IOException {
			appendUrl(context, out);
		}

		private final XMLTag _icon = new AbstractXMLTag() {
			@Override
			public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
				out.beginBeginTag(HTMLConstants.IMG);
				writeSrc(context, out);
				// This is to ensure an error if someone tries to write the icon's style attribute.
				out.writeAttribute(STYLE_ATTR, "");
			}

			@Override
			public void endBeginTag(DisplayContext context, TagWriter out) {
				// Ignore.
			}

			@Override
			public void endTag(DisplayContext context, TagWriter out) {
				out.endEmptyTag();
			}
		};

		final void writeSrc(DisplayContext context, TagWriter out) throws IOException {
			out.beginAttribute(SRC_ATTR);
			appendUrl(context, out);
			out.endAttribute();
		}

		/**
		 * Writes the URL to this resource icon to the given output.
		 */
		public final void appendUrl(DisplayContext context, Appendable out) throws IOException {
			out.append(context.getContextPath());
			out.append(getFileLink());
		}

		/**
		 * Utility for computing {@link #get(DisplayContext)}.
		 */
		private String imageUrl(String contextPath) {
			return contextPath + getFileLink();
		}

		/**
		 * Create the context-local part of the URL to the image identified by the given key.
		 */
		public abstract String getFileLink();

		@Override
		public XMLTag toIcon() {
			return _icon;
		}

		private final XMLTag _button = new AbstractXMLTag() {
			@Override
			public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
				out.beginBeginTag(INPUT);
				out.writeAttribute(TYPE_ATTR, IMAGE_TYPE_VALUE);
				writeSrc(context, out);
			}
		
			@Override
			public void endBeginTag(DisplayContext context, TagWriter out) {
				// Ignore.
			}
		
			@Override
			public void endTag(DisplayContext context, TagWriter out) {
				out.endEmptyTag();
			}
		
		};

		@Override
		public XMLTag toButton() {
			return _button;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_imageKey == null) ? 0 : _imageKey.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Img))
				return false;
			Img other = (Img) obj;
			if (_imageKey == null) {
				if (other._imageKey != null)
					return false;
			} else if (!_imageKey.equals(other._imageKey))
				return false;
			return true;
		}
	}

	static abstract class ThemeIcon extends ProxyIcon {

		@Override
		public String toEncodedForm() {
			String var = getThemeVarName();
			// Traditionally, theme variables are surrounded with `%`, but this character is added
			// implicitly during icon creation.
			if (var.length() >= 2 && var.charAt(0) == '%' && var.charAt(var.length() - 1) == '%') {
				var = var.substring(1, var.length() - 1);
			}
			return THEME_PREFIX + var;
		}

		/**
		 * The name of the theme variable being used.
		 */
		protected abstract String getThemeVarName();

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			String themeVarName = getThemeVarName();
			result = prime * result + ((themeVarName == null) ? 0 : themeVarName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ThemeIcon other = (ThemeIcon) obj;
			String themeVarName = getThemeVarName();

			if (themeVarName == null) {
				if (other.getThemeVarName() != null)
					return false;
			} else if (!themeVarName.equals(other.getThemeVarName()))
				return false;
			return true;
		}
	}

	/**
	 * Icon variant that is automatically instantiated in {@link ThemeImage} constants in
	 * {@link IconsBase} classes.
	 */
	@FrameworkInternal
	public static class CachedThemeIcon extends ThemeIcon {

		private String _key;

		private ThemeImage _defaultValue;

		CachedThemeIcon(String key, final ThemeImage defaultValue) {
			_key = key;
			_defaultValue = defaultValue;
		}

		@Override
		protected String getThemeVarName() {
			return _key;
		}

		/**
		 * Default value when nothing is configured under the given key.
		 */
		public ThemeImage defaultValue() {
			return _defaultValue;
		}

		@Override
		public ThemeImage resolve() {
			ThemeImage result = ThemeFactory.getTheme().getValue(getThemeVarName(), ThemeImage.class);
			if (result == null) {
				result = defaultValue();
			}
			if (result == null) {
				return null;
			}
			return result.resolve();
		}

	}

	static class DynamicThemeIcon extends ThemeIcon {

		private final String _key;

		private final ThemeImage _defaultValue;

		public DynamicThemeIcon(String key, final ThemeImage defaultValue) {
			_key = key;
			_defaultValue = defaultValue;
		}

		@Override
		protected String getThemeVarName() {
			return _key;
		}

		@Override
		public ThemeImage resolve() {
			// Note: Cannot use typed (cached) theme variables, sine those must be singletons (each
			// key must be used by one instance only).
			String rawValue = ThemeFactory.getTheme().getRawValue(_key);
			if (rawValue == null) {
				return _defaultValue.resolve();
			}
			return internalIcon(rawValue).resolve();
		}
	}

	static class CssIcon extends ThemeImage {

		final CharSequence _imageClasses;

		public CssIcon(String imageClasses) {
			_imageClasses = imageClasses;
		}

		@Override
		public String toEncodedForm() {
			return CSS_PREFIX + _imageClasses;
		}

		@Override
		public ThemeImage resolve() {
			return this;
		}

		private final XMLTag _icon = writeIcon();

		protected XMLTag writeIcon() {
			return new AbstractXMLTag() {
				@Override
				public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(SPAN);
					// This is to ensure an error if someone tries to write the icon's style
					// attribute.
					out.writeAttribute(STYLE_ATTR, "");
				}

				@Override
				public void endBeginTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // SPAN

					out.beginBeginTag(ITALICS);
					out.writeAttribute(CLASS_ATTR, _imageClasses);
				}

				@Override
				public void endTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // ITALICS

					out.endTag(ITALICS);
					out.endTag(SPAN);
				}
			};
		}

		@Override
		public XMLTag toIcon() {
			return _icon;
		}

		private final XMLTag _button = writeButton();

		protected XMLTag writeButton() {
			return new AbstractXMLTag() {
				@Override
				public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, "#");
					// This is to ensure an error if someone tries to write the icon's style
					// attribute.
					out.writeAttribute(STYLE_ATTR, "");
				}

				@Override
				public void endBeginTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // ANCHOR

					out.beginBeginTag(ITALICS);
					out.writeAttribute(CLASS_ATTR, _imageClasses);
				}

				@Override
				public void endTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // ITALICS

					out.endTag(ITALICS);
					out.endTag(ANCHOR);
				}
			};
		}

		@Override
		public XMLTag toButton() {
			return _button;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_imageClasses == null) ? 0 : _imageClasses.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CssIcon other = (CssIcon) obj;
			if (_imageClasses == null) {
				if (other._imageClasses != null)
					return false;
			} else if (!_imageClasses.equals(other._imageClasses))
				return false;
			return true;
		}
	}

	static class ColoredCssIcon extends CssIcon {

		public ColoredCssIcon(String imageClasses) {
			super(imageClasses);
		}

		@Override
		public String toEncodedForm() {
			return COLORED_CSS_PREFIX + _imageClasses;
		}

		@Override
		protected XMLTag writeIcon() {
			return new AbstractXMLTag() {
				@Override
				public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(SPAN);
					// This is to ensure an error if someone tries to write the icon's style
					// attribute.
					out.writeAttribute(STYLE_ATTR, "");
				}

				@Override
				public void endBeginTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // SPAN

					out.beginBeginTag(ITALICS);
					out.writeAttribute(CLASS_ATTR, _imageClasses);
				}

				@Override
				public void endTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // ITALICS

					out.beginTag(SPAN);
					out.endTag(SPAN);
					out.endTag(ITALICS);
					out.endTag(SPAN);
				}
			};
		}

		@Override
		protected XMLTag writeButton() {
			return new AbstractXMLTag() {
				@Override
				public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, "#");
					// This is to ensure an error if someone tries to write the icon's style
					// attribute.
					out.writeAttribute(STYLE_ATTR, "");
				}

				@Override
				public void endBeginTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // ANCHOR

					out.beginBeginTag(ITALICS);
					out.writeAttribute(CLASS_ATTR, _imageClasses);
				}

				@Override
				public void endTag(DisplayContext context, TagWriter out) {
					out.endBeginTag(); // ITALICS

					out.beginTag(SPAN);
					out.endTag(SPAN);
					out.endTag(ITALICS);
					out.endTag(ANCHOR);
				}
			};
		}
	}

	static class I18NIcon extends ProxyIcon {

		private final ResKey _imageKey;

		I18NIcon(ResKey imageKey) {
			_imageKey = imageKey;
		}

		@Override
		public ThemeImage resolve() {
			String key = Resources.getInstance().getString(_imageKey, null);
			if (key == null || key.isEmpty()) {
				return NoIcon.INSTANCE;
			}
			return internalIcon(key);
		}

		@Override
		public String toEncodedForm() {
			return I18N_PREFIX + ResKey.encode(_imageKey);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_imageKey == null) ? 0 : _imageKey.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			I18NIcon other = (I18NIcon) obj;
			if (_imageKey == null) {
				if (other._imageKey != null)
					return false;
			} else if (!_imageKey.equals(other._imageKey))
				return false;
			return true;
		}
	}

	static abstract class ProxyIcon extends ThemeImage {

		@Override
		public XMLTag toIcon() {
			return resolve().toIcon();
		}
	
		@Override
		public XMLTag toButton() {
			return resolve().toButton();
		}
	
	}

	static class NoIcon extends ThemeImage {

		/**
		 * Singleton {@link ThemeImage.NoIcon} instance.
		 */
		public static final NoIcon INSTANCE = new NoIcon();

		private NoIcon() {
			// Singleton constructor.
		}

		@Override
		public ThemeImage resolve() {
			return this;
		}

		private static final XMLTag EMPTY_TAG = new AbstractXMLTag() {

			@Override
			public void beginBeginTag(DisplayContext context, TagWriter out) throws IOException {
				out.beginBeginTag(SPAN);
				out.writeAttribute(STYLE_ATTR, "display:none;");
			}

			@Override
			public void endBeginTag(DisplayContext context, TagWriter out) {
				out.endBeginTag();
			}

			@Override
			public void endTag(DisplayContext context, TagWriter out) {
				out.endTag(SPAN);
			}

		};

		@Override
		public String toEncodedForm() {
			return NONE_ICON;
		}

		@Override
		public XMLTag toIcon() {
			return EMPTY_TAG;
		}

		@Override
		public XMLTag toButton() {
			return EMPTY_TAG;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return (this == obj);
		}

	}

}
