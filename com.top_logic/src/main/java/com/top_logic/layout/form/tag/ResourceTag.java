/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * {@link Tag} for directly displaying some application resource.
 * 
 * @see #setReskey(String)
 * @see #setKey(String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResourceTag extends AbstractFormTag {

	private static final String TOOLTIP_SUFFIX = ".tooltip";

	/** 
	 * @deprecated No longer in use.
	 */
	@Deprecated
	public static final String TEXT_TYPE = "text";
	
	/**
	 * @deprecated Never implemented, replaced with {@link #setImage(String)} 
	 */
	@Deprecated
	public static final String IMAGE_TYPE = "image";
	
	private ResKey key;

	private String keySuffix;
	
	/** Default key is always global... */
	public ResKey defaultKey;

	private ThemeImage _image;
	
    /** 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
        		+ "key: '" + this.key
        		+ "keySuffix: '" + this.keySuffix
                + "', defaultKey: '" + this.defaultKey
			+ "', image: '" + this._image
                + "']");
    }

    /**
     * @deprecated No longer in use.
     */
	@Deprecated
	public void setType(String type) {
		// Ignore.
	}

	/**
	 * Sets the key suffix to resolve locally to the parent's form container resources.
	 */
	@CalledFromJSP
	public void setKey(String suffix) {
		this.keySuffix = suffix;
	}

	/**
	 * Sets the fully qualified resource key to display.
	 */
	@CalledFromJSP
	public void setReskey(String key) {
		setReskeyConst(ResKey.internalJsp(key));
	}

	/**
	 * Sets the fully qualified resource key to display.
	 */
	@CalledFromJSP
	public void setReskeyConst(ResKey resKey) {
		this.key = resKey;
	}
	
	/**
	 * Use {@link #setIcon(ThemeImage)}.
	 */
	@CalledFromJSP
	@Deprecated
	public void setImage(String imageName) {
		setIcon(ThemeImage.icon(imageName));
	}
	
	/**
	 * Sets the image to render.
	 */
	@CalledFromJSP
	public void setIcon(ThemeImage icon) {
		_image = icon;
	}

	public ThemeImage getIcon() {
		return _image;
	}

	public void setDefaultKey(String aDefaultKey) {
		setDefaultKeyConst(ResKey.internalJsp(aDefaultKey));
	}

	public void setDefaultKeyConst(ResKey resourceKey) {
		this.defaultKey = resourceKey;
	}
	
	@Override
	protected int startFormMember() throws IOException {
		String tooltip = getTooltip();
		boolean hasTooltip = tooltip != null;
		
		if (hasTooltip) {
			beginBeginTag(SPAN);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(getDisplayContext(), out(), tooltip);
			endBeginTag();
		}
		
		if (_image != null) {
			_image.write(getDisplayContext(), getOut());
		} else {
			writeText(getLabel());
		}
		
		if (hasTooltip) {
			endTag(SPAN);
		}
		
		return SKIP_BODY;
	}

	protected String getLabel() {
		if (!hasResource()) {
			return "";
		}
		String label;
		if (defaultKey != null) {
			// There is a default. Do not mark the key resource as missing,
			// if it does not exist.
			label = lookupKeyOptional(null);
			
			if (label == null) {
				// There is no custom resource given. Lookup the default
				// key. Make sure to mark the default resource as missing,
				// if it does not exist.
				label = lookupDefaultKeyMandatory(null);
			}
		} else {
			// There is no default. Lookup the key resource an make sure to
			// mark it as missing, if it does not exist.
			label = lookupKeyMandatory(null);
		}
		return label;
	}

	protected String getTooltip() {
		if (!hasResource()) {
			return null;
		}
		String label;
		if (defaultKey != null) {
			// There is a default. Do not mark the key resource as missing,
			// if it does not exist.
			label = lookupKeyOptional(TOOLTIP_SUFFIX);

			if (label == null) {
				// There is no custom resource given. Lookup the default
				// key. Make sure to mark the default resource as missing,
				// if it does not exist.
				label = lookupDefaultKeyOptional(TOOLTIP_SUFFIX);
			}
		} else {
			// There is no default. Lookup the key resource an make sure to
			// mark it as missing, if it does not exist.
			label = lookupKeyOptional(TOOLTIP_SUFFIX);
		}
		return label;
	}

	/**
	 * Whether some resource key information was given.
	 */
	protected final boolean hasResource() {
		return key != null || keySuffix != null;
	}

	/**
	 * Looks up the default resource key in the global resources.
	 */
	protected final String lookupDefaultKeyMandatory(String suffix) {
		return Resources.getInstance().getString(this.defaultKey.suffix(suffix));
	}
	
	/**
	 * Looks up the default resource key in the global resources.
	 */
	protected final String lookupDefaultKeyOptional(String suffix) {
		return Resources.getInstance().getString(this.defaultKey.suffix(suffix), null);
	}

	/**
	 * Looks up the resource key (either local or global).
	 * 
	 * <p>
	 * Do not mark a missing resource as such.
	 * </p>
	 */
	protected final String lookupKeyOptional(String suffix) {
		if (this.key != null) {
			return Resources.getInstance().getString(globalWithSuffix(suffix), null);
		} else {
			return getParentFormContainer().getResources().getStringResource(localWithSuffix(suffix), null);
		}
	}
	
	/**
	 * Looks up the resource key (either local or global).
	 * 
	 * <p>
	 * Does mark a missing resource as such.
	 * </p>
	 */
	protected final String lookupKeyMandatory(String suffix) {
		if (this.key != null) {
			return Resources.getInstance().getString(globalWithSuffix(suffix));
		} else {
			return getParentFormContainer().getResources().getStringResource(localWithSuffix(suffix));
		}
	}

	private ResKey globalWithSuffix(String suffix) {
		return key.suffix(suffix);
	}

	private String localWithSuffix(String suffix) {
		if (suffix != null) {
			return keySuffix + suffix;
		} else {
			return keySuffix;
		}
	}

	@Override
	protected int endFormMember() throws IOException {
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		super.teardown();
		key = null;
		keySuffix = null;
		_image = null;
		defaultKey = null;
	}
}
