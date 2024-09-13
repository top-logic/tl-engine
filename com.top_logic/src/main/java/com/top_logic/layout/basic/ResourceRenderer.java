/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.component.factory.adapter.TypeBasedContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.control.ContextCommandsControl;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuControl;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.CellObject;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.Media;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.css.CssUtil;

/**
 * A {@link Renderer} implementation that renders an arbitrary object given a
 * {@link ResourceProvider}.
 * 
 * <p>
 * Depending on the {@link ResourceProvider}, this renderer creates an
 * {@link ResourceProvider#getImage(Object, Flavor) image} followed by a
 * {@link ResourceProvider#getLabel(Object) text} surrounded by a
 * {@link ResourceProvider#getLink(DisplayContext, Object) link}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResourceRenderer<C extends ResourceRenderer.Config<?>>
		implements Renderer<Object>, ConfiguredInstance<C>, HTMLConstants {

	/**
	 * Configuration options for {@link ResourceRenderer}.
	 */
	public interface Config<I extends ResourceRenderer<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getResourceProvider()
		 */
		String RESOURCE_PROVIDER_PROPERTY = "resourceProvider";

		/**
		 * @see #getContextMenu()
		 */
		String CONTEXT_MENU = "contextMenu";

		/**
		 * @see #getUseContextMenu()
		 */
		String USE_CONTEXT_MENU_PROPERTY = "useContextMenu";

		/**
		 * @see #getUseImage()
		 */
		String USE_IMAGE_PROPERTY = "useImage";

		/**
		 * @see #getUseToolTip()
		 */
		String USE_TOOLTIP_PROPERTY = "useToolTip";

		/**
		 * @see #getUseLink()
		 */
		String USE_LINK_PROPERTY = "useLink";

		/**
		 * @see #getStartPos()
		 */
		String START_POS_PROPERTY = "startPos";

		/**
		 * @see #getMaxLength()
		 */
		String MAX_LENGTH_PROPERTY = "maxLength";

		/** Default for {@link #getStartPos()}. */
		int START_POS_DEFAULT = 0;

		/** Default for {@link #getMaxLength()}. */
		int MAX_LENGTH_DEFAULT = -1;

		/**
		 * Provider for label, images and tool-tips for the rendered objects.
		 */
		@Name(RESOURCE_PROVIDER_PROPERTY)
		@InstanceFormat
		@InstanceDefault(MetaResourceProvider.class)
		ResourceProvider getResourceProvider();

		/**
		 * @see #getResourceProvider()
		 */
		void setResourceProvider(ResourceProvider provider);

		/**
		 * Context menu entries for displayed objects.
		 */
		@Name(CONTEXT_MENU)
		@ItemDefault(NoContextMenuProvider.class)
		@ImplementationClassDefault(TypeBasedContextMenuProvider.class)
		PolymorphicConfiguration<ContextMenuProvider> getContextMenu();

		/**
		 * @see #getContextMenu()
		 */
		void setContextMenu(PolymorphicConfiguration<ContextMenuProvider> value);

		/**
		 * Maximum length of label displayed, may be negative (when deactivated).
		 */
		@Name(MAX_LENGTH_PROPERTY)
		@IntDefault(MAX_LENGTH_DEFAULT)
		int getMaxLength();

		/**
		 * Setter for {@link #getMaxLength()}.
		 */
		void setMaxLength(int maxLength);

		/**
		 * Starting position for cutting the label.
		 */
		@Name(START_POS_PROPERTY)
		@IntDefault(START_POS_DEFAULT)
		int getStartPos();

		/**
		 * Setter for {@link #getStartPos()}.
		 */
		void setStartPos(int startPos);

		/**
		 * Whether to render a link. If <code>false</code>, no link is rendered, even if the
		 * underlying {@link ResourceProvider} would provide one.
		 */
		@Name(USE_LINK_PROPERTY)
		@BooleanDefault(USE_LINK)
		boolean getUseLink();

		/**
		 * Setter for {@link #getUseLink()}.
		 */
		void setUseLink(boolean useLink);

		/**
		 * Whether to render a tool tip. If <code>false</code>, no tool tip is rendered, even if the
		 * underlying {@link ResourceProvider} would provide one.
		 */
		@Name(USE_TOOLTIP_PROPERTY)
		@BooleanDefault(USE_TOOLTIP)
		boolean getUseToolTip();

		/**
		 * Setter for {@link #getUseToolTip()}.
		 */
		void setUseToolTip(boolean useToolTip);

		/**
		 * Whether the context menu should be used. If <code>false</code>, no context menu is
		 * rendered, even if the given {@link #getContextMenu()} would provide one or object
		 * specific {@link LabelProviderService#getContextCommands(Object) context commands} exist.
		 */
		@Name(USE_CONTEXT_MENU_PROPERTY)
		@BooleanDefault(USE_CONTEXT_MENU)
		boolean getUseContextMenu();

		/**
		 * Setter for {@link #getUseContextMenu()}.
		 */
		void setUseContextMenu(boolean useContextMenu);

		/**
		 * Whether to render an image. If <code>false</code>, no image is rendered, even if the
		 * underlying {@link ResourceProvider} would provide one.
		 */
		@Name(USE_IMAGE_PROPERTY)
		@BooleanDefault(USE_IMAGE)
		boolean getUseImage();

		/**
		 * Setter for {@link #getUseImage()}.
		 */
		void setUseImage(boolean useImage);
	}

	/**
	 * Constant for {@link Config#setUseLink(boolean)}.
	 */
	public static final boolean USE_CONTEXT_MENU = true;

	/**
	 * Constant for {@link Config#setUseLink(boolean)}.
	 */
    public static final boolean USE_LINK = true;

	/**
	 * Constant for {@link Config#setUseToolTip(boolean)}.
	 */
    public static final boolean USE_TOOLTIP = true;

	/**
	 * Constant for {@link Config#setUseImage(boolean)}.
	 */
    public static final boolean USE_IMAGE = true;

    /** Default instance that uses the default {@link MetaResourceProvider}. */
	public static final ResourceRenderer<?> INSTANCE = newResourceRenderer(MetaResourceProvider.INSTANCE);

    /**
     * Instance that renders plain labels provided by
     * {@link MetaResourceProvider} but without links, images and tool tips.
     */
	public static final ResourceRenderer<?> NO_LINK_INSTANCE =
		newResourceRenderer(MetaResourceProvider.INSTANCE, !USE_LINK, USE_TOOLTIP, USE_IMAGE);

    /**
	 * Instance that renders plain labels provided by {@link MetaResourceProvider} but without links
	 * and tool-tips.
	 */
	public static final ResourceRenderer<?> ICON_TEXT_INSTANCE = newResourceRenderer(MetaResourceProvider.INSTANCE, !USE_LINK, !USE_TOOLTIP, USE_IMAGE);

    /**
     * Instance that renders plain labels provided by
     * {@link MetaResourceProvider} but without links, images and tool tips.
     */
	public static final ResourceRenderer<?> PLAIN_INSTANCE =
		newResourceRenderer(MetaResourceProvider.INSTANCE, !USE_LINK, !USE_TOOLTIP, !USE_IMAGE);

	/**
	 * Whether object links should be displayed in the current context.
	 * 
	 * <p>
	 * The property is set on {@link DisplayContext} to temporarily disable link rendering.
	 * </p>
	 * 
	 * @see #noLinks(CellRenderer)
	 */
	public static final Property<Boolean> RENDER_LINKS =
		TypedAnnotatable.property(Boolean.class, "renderLinks", Boolean.TRUE);

	/** @see #getResourceProvider() */
	private ResourceProvider _resourceProvider;

	/** @see #getMaxLength() */
	private final int _maxLength;

	/** @see #getStartPos() */
	private final int _startPos;

	/** @see #isUseLink() */
	private final boolean _useLink;

	/** @see #isUseToolTip() */
	private final boolean _useToolTip;

	/** @see #isUseImage() */
	private final boolean _useImage;

	private ContextMenuProvider _contextMenu;

	private C _config;

	/**
	 * Create a {@link ResourceRenderer} form configuration.
	 * 
	 * @param config
	 *        The primitive configuration values.
	 */
	public ResourceRenderer(InstantiationContext context, C config) {
		this(config, config.getResourceProvider(), context.getInstance(config.getContextMenu()),
			config.getMaxLength(), config.getStartPos(),
			config.getUseLink(), config.getUseToolTip(), config.getUseImage());
	}

	/**
	 * Create a {@link ResourceRenderer} form configuration.
	 * 
	 * @param config See {@link #getConfig()}.
	 * @param resourceProvider See {@link #getResourceProvider()}.
	 * @param contextMenu See {@link #getContextMenu()}.
	 * @param maxLength See {@link #getMaxLength()}.
	 * @param startPos See {@link #getStartPos()}.
	 * @param useLink See {@link #isUseLink()}.
	 * @param useToolTip See {@link #isUseToolTip()}.
	 * @param useImage See {@link #isUseImage()}.
	 */
	private ResourceRenderer(C config, ResourceProvider resourceProvider, ContextMenuProvider contextMenu,
			int maxLength, int startPos, boolean useLink, boolean useToolTip, boolean useImage) {
		_config = config;
		_resourceProvider = resourceProvider;
		_contextMenu = contextMenu;
		_maxLength = maxLength;
		_startPos = startPos;
		_useLink = useLink;
		_useToolTip = useToolTip;
		_useImage = useImage;
	}

	@Override
	public C getConfig() {
		if (_config != null) {
			return _config;
		}

		@SuppressWarnings("unchecked")
		C result = (C) createConfig(this.getClass());
		buildConfig(result);
		return result;
	}

	/**
	 * Fill the configuration for a programmatically constructed renderer.
	 */
	protected void buildConfig(C result) {
		result.setResourceProvider(getResourceProvider());
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<ContextMenuProvider> contextMenuConfig =
			(PolymorphicConfiguration<ContextMenuProvider>) InstanceAccess.INSTANCE.getConfig(getContextMenu());
		result.setContextMenu(contextMenuConfig);
		result.setMaxLength(getMaxLength());
		result.setStartPos(getStartPos());
		result.setUseLink(isUseLink());
		result.setUseToolTip(isUseToolTip());
	}

	/**
	 * Provider for label, images and tool-tips for the rendered objects.
	 */
	public ResourceProvider getResourceProvider() {
		return _resourceProvider;
	}

	/**
	 * The {@link ContextMenuProvider} to use for the rendered model object.
	 */
	public ContextMenuProvider getContextMenu() {
		return _contextMenu;
	}

	/**
	 * Starting position for cutting the label.
	 */
	public int getStartPos() {
		return _startPos;
	}

	/**
	 * Maximum length of label displayed, may be negative (when deactivated).
	 */
	public int getMaxLength() {
		return _maxLength;
	}

	/**
	 * Whether to render a link. If <code>false</code>, no link is rendered, even if the underlying
	 * {@link ResourceProvider} would provide one.
	 */
	public boolean isUseLink() {
		return _useLink;
	}

	/**
	 * Whether to render a tool tip. If <code>false</code>, no tool tip is rendered, even if the
	 * underlying {@link ResourceProvider} would provide one.
	 */
	public boolean isUseToolTip() {
		return _useToolTip;
	}

	/**
	 * Whether to render an image. If <code>false</code>, no image is rendered, even if the
	 * underlying {@link ResourceProvider} would provide one.
	 */
	public boolean isUseImage() {
		return _useImage;
	}
    
    @Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		boolean useContextMenu = getConfig() != null ? getConfig().getUseContextMenu() : USE_CONTEXT_MENU;
		write(context, out, value, _useLink, _useToolTip, _useImage, useContextMenu);
	}

	/**
	 * Writes the given value using custom options for {@link #isUseLink()},
	 * {@link #isUseToolTip()}, {@link #isUseImage()}, and {@link Config#getUseContextMenu()}.
	 */
	public void write(DisplayContext context, TagWriter out, Object value, boolean useLink, boolean useToolTip,
			boolean useImage, boolean useContextMenu) throws IOException {
		if (useContextMenu && context.getOutputMedia() != Media.PDF) {
			ContextMenuProvider contextMenu = getContextMenu();
			if (contextMenu.hasContextMenu(value)) {
				new ContextMenuControl<>(contextMenu, value) {
					@Override
					protected void writeContents(DisplayContext innerContext, TagWriter innerOut) throws IOException {
						doWrite(context, out, value, useLink, useToolTip, useImage, useContextMenu);
					}
				}.write(context, out);
			} else if (LabelProviderService.getInstance().hasContextMenuCommands(value)) {
				new ContextCommandsControl<>(LabelProviderService.getInstance(), getResourceProvider(), value) {
					@Override
					protected void writeContents(DisplayContext innerContext, TagWriter innerOut) throws IOException {
						doWrite(innerContext, innerOut, value, useLink, useToolTip, useImage, useContextMenu);
					}
				}.write(context, out);
			} else {
				doWrite(context, out, value, useLink, useToolTip, useImage, useContextMenu);
			}
		} else {
			doWrite(context, out, value, useLink, useToolTip, useImage, useContextMenu);
		}
	}

	/**
	 * Implementation of
	 * {@link #write(DisplayContext, TagWriter, Object, boolean, boolean, boolean, boolean)}
	 */
	final void doWrite(DisplayContext context, TagWriter out, Object value, boolean useLink, boolean useToolTip,
			boolean useImage, boolean useContextMenu) throws IOException {
		if (value == null) {
			return;
		}
		useLink &= context.getOutputMedia() != Media.PDF;
		useToolTip &= context.getOutputMedia() != Media.PDF;

        if (value instanceof CellObject) {
            writeCellObject(context, out, (CellObject)value, useLink, useToolTip, useImage, useContextMenu);
            return;
        }
		if (value instanceof HTMLFragment) {
			((HTMLFragment) value).write(context, out);
	        return;
	    }
		if (value instanceof Collection) {
			writeCollection(context, out, (Collection<?>) value, useLink, useToolTip, useImage, useContextMenu);
	        return;
	    }

		String link;
        String tooltip;
        boolean hasLink;
        boolean hasTooltip;
        String theLabel;
		String cssClass;
		boolean hasCssClass;
        try {
			link = useLink && context.get(RENDER_LINKS) ? _resourceProvider.getLink(context, value) : null;
			tooltip = useToolTip ? _resourceProvider.getTooltip(value) : null;
			cssClass = _resourceProvider.getCssClass(value);
			hasCssClass = cssClass != null;
            hasLink    = link != null;
            hasTooltip = tooltip != null;
			theLabel = _resourceProvider.getLabel(value);
		} catch (WrapperRuntimeException ex) {
            // fix for error while trying to render a deleted object
            return;
        }

        if (hasLink) {
			out.beginBeginTag(ANCHOR);
			out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
			CssUtil.writeCombinedCssClasses(out, GotoHandler.GOTO_CLASS, cssClass);
            out.writeAttribute(ONCLICK_ATTR, link);
            if (hasTooltip) {
                // OverLib attributes are OK for ANCHOR, too
				OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
            }
			out.endBeginTag();
		} 
        else if (hasTooltip) {
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, cssClass);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
			out.endBeginTag();
		}
		else if (hasCssClass) {
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, cssClass);
			out.endBeginTag();
		}

		ThemeImage theImage = useImage ? _resourceProvider.getImage(value, Flavor.DEFAULT) : null;
        if (theImage != null) {
			ThemeImage resolved = theImage.resolve();
			if (resolved != ThemeImage.none()) {
				writeImage(context, out, value, resolved);
			}
		}

        if (theLabel != null) {
			String theShort = (_maxLength > -1) && (_maxLength < theLabel.length())
				? StringServices.minimizeString(theLabel, _maxLength, _startPos)
				: null;
            if (theShort != null) {
                if (!hasTooltip) {
					out.beginBeginTag(HTMLConstants.SPAN);
					OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, theLabel);
                    out.endBeginTag();
                }
                if (theShort.trim().length() == 0) {
					out.writeText(NBSP);
                }
                else {
                    out.writeText(theShort);
                }
                if (!hasTooltip) {
					out.endTag(HTMLConstants.SPAN);
                }
            }
            else {
                if (theLabel.trim().length() == 0) {
					out.writeText(NBSP);
                }
                else {
                    out.writeText(theLabel);
                }
            }
		}

		if (hasLink) {
			out.endTag(ANCHOR);
		} else if (hasTooltip) {
			out.endTag(SPAN);
		} else if (hasCssClass) {
			out.endTag(SPAN);
		}
	}

    /**
	 * Dispatched from
	 * {@link #write(DisplayContext, TagWriter, Object, boolean, boolean, boolean,boolean)}.
	 */
	protected void writeCollection(DisplayContext context, TagWriter out, Collection<?> aCollection, boolean useLink,
			boolean useToolTip, boolean useImage, boolean useContextMenu) throws IOException {
        boolean needsSpacer = false;

        if (aCollection.isEmpty()) {
			out.writeText(NBSP);
        }
        else {
			String separator = ApplicationConfig.getInstance().getConfig(LayoutConfig.class).getCollectionSeparator();
			for (Iterator<?> theIt = aCollection.iterator(); theIt.hasNext();) {
                Object theObject = theIt.next();
                if (!(theObject instanceof ErrorControl) && needsSpacer) {
					out.beginBeginTag(SPAN);
					out.writeAttribute(CLASS_ATTR, "tl-separator");
					out.endBeginTag();
					out.writeText(separator);
					out.endTag(SPAN);
                }
				write(context, out, theObject, useLink, useToolTip, useImage, useContextMenu);
				needsSpacer = !(theObject instanceof View) || ((View) theObject).isVisible();
            }
        }
    }

    /**
	 * Dispatched from
	 * {@link #write(DisplayContext, TagWriter, Object, boolean, boolean, boolean, boolean)}.
	 */
    protected void writeCellObject(DisplayContext context, TagWriter out, CellObject aCell, boolean useLink, boolean useToolTip, boolean useImage, boolean useContextMenu) throws IOException {
        Object theValue = aCell.getValue();
		String theTooltip = useToolTip ? _resourceProvider.getTooltip(aCell) : null;
		ThemeImage theImage = useImage ? _resourceProvider.getImage(aCell, Flavor.DEFAULT) : null;

        out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, _resourceProvider.getCssClass(aCell));
		out.writeAttribute(STYLE_ATTR, aCell.getStyle());
        if (theTooltip != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, theTooltip);
        }
        out.endBeginTag();
        if (theImage != null) {
            writeImage(context, out, aCell, theImage);
			out.writeText(NBSP);
        }

        boolean innerUseLink = useLink && !aCell.isLinkDisabled();
        boolean innerUseTooltip = useToolTip && !aCell.isTooltipDisabled() && theTooltip == null;
        boolean innerUseImage = useImage && !aCell.isImageDisabled() && theImage == null;
		write(context, out, theValue, innerUseLink, innerUseTooltip, innerUseImage, useContextMenu);
        out.endTag(SPAN);
    }

    /**
	 * Write an image tag with the given parameters.
	 * 
	 * @param context
	 *        The context this renderer lives in, must not be <code>null</code>.
	 * @param out
	 *        The stream to write the image tag to, must not be <code>null</code>.
	 * @param aValue
	 *        The object to be rendered, must not be <code>null</code>.
	 * @param image
	 *        The image to be rendered, must not be <code>null</code>.
	 * @throws IOException
	 *         If writing to the stream fails for a reason.
	 */
	protected void writeImage(DisplayContext context, TagWriter out, Object aValue, ThemeImage image)
			throws IOException {
		XMLTag tag = image.toIcon();
		tag.beginBeginTag(context, out);
		out.writeAttribute(CLASS_ATTR, FormConstants.TYPE_IMAGE_CSS_CLASS);
		out.writeAttribute(ALT_ATTR, StringServices.EMPTY_STRING);
		out.writeAttribute(TITLE_ATTR, StringServices.EMPTY_STRING); // Avoid popup for IE
		out.writeAttribute(BORDER_ATTR, 0);
		tag.endEmptyTag(context, out);
    }

	/**
	 * Creates a new {@link ResourceProvider} configuration.
	 */
	public static Config<?> createConfig(ResourceProvider resourceProvider) {
		Config<?> config = createConfig(ResourceRenderer.class);
		config.setResourceProvider(resourceProvider);
		return config;
	}

	/**
	 * Creates a new {@link ResourceRenderer} configuration.
	 * 
	 * @param resourceProvider
	 *        See {@link Config#getResourceProvider()}.
	 * @param useLink
	 *        See {@link Config#getUseLink()}.
	 * @param useToolTip
	 *        See {@link Config#getUseToolTip()}.
	 * @param useImage
	 *        See {@link Config#getUseImage()}.
	 */
	public static Config<?> createConfig(
			ResourceProvider resourceProvider, boolean useLink, boolean useToolTip, boolean useImage) {
		Config<?> config = createConfig(resourceProvider);
		config.setUseLink(useLink);
		config.setUseToolTip(useToolTip);
		config.setUseImage(useImage);
		return config;
	}

	/**
	 * Creates a new configuration to instantiate the given {@link ResourceRenderer} class.
	 * 
	 * @param resourceProvider
	 *        See {@link Config#getResourceProvider()}.
	 * @param useLink
	 *        See {@link Config#getUseLink()}.
	 * @param useToolTip
	 *        See {@link Config#getUseToolTip()}.
	 * @param useImage
	 *        See {@link Config#getUseImage()}.
	 */
	public static <I extends ResourceRenderer<?>> Config<I> createConfig(Class<I> implClass,
			ResourceProvider resourceProvider, boolean useLink, boolean useToolTip, boolean useImage) {
		Config<I> config = createConfig(implClass, resourceProvider);
		config.setUseLink(useLink);
		config.setUseToolTip(useToolTip);
		config.setUseImage(useImage);
		return config;
	}

	/**
	 * Creates a new configuration with the given {@link ResourceProvider} to instantiate the given
	 * {@link ResourceRenderer} class.
	 */
	public static <I extends ResourceRenderer<?>> Config<I> createConfig(Class<I> implClass,
			ResourceProvider resourceProvider) {
		Config<I> config = createConfig(implClass);
		config.setResourceProvider(resourceProvider);
		return config;
	}

	/**
	 * Creates a new configuration to instantiate the given {@link ResourceRenderer} class.
	 */
	public static <I extends ResourceRenderer<?>> Config<I> createConfig(Class<I> implClass) {
		try {
			Config<I> result = (Config<I>) TypedConfiguration.createConfigItemForImplementationClass(implClass);
			return result;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Creates a new {@link ResourceRenderer} with a {@link ResourceProvider} retrieved from the
	 * given {@link LabelProvider}.
	 */
	public static ResourceRenderer<?> newResourceRenderer(LabelProvider provider) {
		return newResourceRenderer(LabelResourceProvider.toResourceProvider(provider));
	}

	/**
	 * Creates a new {@link ResourceRenderer} with the given {@link ResourceProvider}.
	 */
	public static ResourceRenderer<?> newResourceRenderer(ResourceProvider resourceProvider) {
		return newResourceRenderer(resourceProvider, NoContextMenuProvider.INSTANCE);
	}

	/**
	 * Creates a new {@link ResourceRenderer} with the given {@link LabelProvider} and
	 * {@link ContextMenuProvider}.
	 */
	public static ResourceRenderer<?> newResourceRenderer(LabelProvider provider, ContextMenuProvider contextMenu) {
		return newResourceRenderer(LabelResourceProvider.toResourceProvider(provider), contextMenu);
	}

	/**
	 * Creates a new {@link ResourceRenderer} with the given {@link ResourceProvider} and
	 * {@link ContextMenuProvider}.
	 */
	public static ResourceRenderer<?> newResourceRenderer(ResourceProvider resourceProvider,
			ContextMenuProvider contextMenu) {
		return newResourceRenderer(resourceProvider, contextMenu,
			Config.MAX_LENGTH_DEFAULT, Config.START_POS_DEFAULT, USE_LINK, USE_TOOLTIP, USE_IMAGE);
	}

	/**
	 * Creates a new {@link ResourceRenderer} with the given values.
	 * 
	 * @param provider
	 *        See {@link ResourceRenderer#getResourceProvider()}.
	 * @param useLink
	 *        See {@link ResourceRenderer#isUseLink()}.
	 * @param useToolTip
	 *        See {@link ResourceRenderer#isUseToolTip()}.
	 * @param useImage
	 *        See {@link ResourceRenderer#isUseImage()}.
	 */
	public static ResourceRenderer<?> newResourceRenderer(ResourceProvider provider,
			boolean useLink, boolean useToolTip, boolean useImage) {
		return newResourceRenderer(provider, NoContextMenuProvider.INSTANCE, Config.MAX_LENGTH_DEFAULT,
			Config.START_POS_DEFAULT, useLink, useToolTip, useImage);
	}

	/**
	 * Creates a new {@link ResourceRenderer} with the given values.
	 * 
	 * @param resourceProvider
	 *        See {@link ResourceRenderer#getResourceProvider()}.
	 * @param maxLength
	 *        See {@link ResourceRenderer#getMaxLength()}.
	 * @param startPos
	 *        See {@link ResourceRenderer#getStartPos()}.
	 * @param useLink
	 *        See {@link ResourceRenderer#isUseLink()}.
	 * @param useToolTip
	 *        See {@link ResourceRenderer#isUseToolTip()}.
	 * @param useImage
	 *        See {@link ResourceRenderer#isUseImage()}.
	 */
	public static ResourceRenderer<?> newResourceRenderer(ResourceProvider resourceProvider, int maxLength,
			int startPos,
			boolean useLink, boolean useToolTip, boolean useImage) {
		return newResourceRenderer(resourceProvider, NoContextMenuProvider.INSTANCE, maxLength, startPos, useLink, useToolTip, useImage);
	}

	/**
	 * Creates a new {@link ResourceRenderer} with the given values.
	 * 
	 * @param resourceProvider
	 *        See {@link ResourceRenderer#getResourceProvider()}.
	 * @param contextMenu
	 *        See {@link #getContextMenu()}.
	 * @param maxLength
	 *        See {@link ResourceRenderer#getMaxLength()}.
	 * @param startPos
	 *        See {@link ResourceRenderer#getStartPos()}.
	 * @param useLink
	 *        See {@link ResourceRenderer#isUseLink()}.
	 * @param useToolTip
	 *        See {@link ResourceRenderer#isUseToolTip()}.
	 * @param useImage
	 *        See {@link ResourceRenderer#isUseImage()}.
	 */
	public static ResourceRenderer<?> newResourceRenderer(ResourceProvider resourceProvider, ContextMenuProvider contextMenu,
			int maxLength, int startPos, boolean useLink, boolean useToolTip, boolean useImage) {
		return new ResourceRenderer<>(null, resourceProvider, contextMenu, maxLength,
			startPos, useLink, useToolTip, useImage);
	}

	@Override
	public <X> Renderer<? super X> generic(Class<X> expectedType) {
		return this;
	}

	/**
	 * Wraps the given {@link CellRenderer} with an instance preventing links being rendered.
	 */
	public static CellRenderer noLinks(CellRenderer cellRenderer) {
		return new CellRenderer() {
			@Override
			public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
				Boolean before = context.set(RENDER_LINKS, Boolean.FALSE);
				try {
					cellRenderer.writeCell(context, out, cell);
				} finally {
					context.set(RENDER_LINKS, before);
				}
			}
		};
	}
}
