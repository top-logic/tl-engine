/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.resources;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.visit.ImageVisitor;
import com.top_logic.model.visit.LabelVisitor;
import com.top_logic.model.visit.LinkVisitor;
import com.top_logic.model.visit.TooltipVisitor;
import com.top_logic.model.visit.TypeVisitor;
import com.top_logic.util.Resources;

/**
 * Default {@link ResourceProvider} for {@link TLModelPart}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLPartResourceProvider extends AbstractResourceProvider {

	/** Singleton {@link TLPartResourceProvider} instance. */
	public static final TLPartResourceProvider INSTANCE = new TLPartResourceProvider();

	private static final Void none = null;

	private final TLModelVisitor<String, Void> _labelVisitor;

	private final TLModelVisitor<String, Void> _typeVisitor;

	private final TLModelVisitor<ResKey, Void> _tooltipVisitor;

	private final TLModelVisitor<ThemeImage, Flavor> _imageVisitor;

	private final TLModelVisitor<String, DisplayContext> _linkVisitor;

	/**
	 * Creates a new {@link TLPartResourceProvider}.
	 */
	protected TLPartResourceProvider() {
		_labelVisitor = createLabelVisitor();
		_typeVisitor = new TypeVisitor(this);
		_tooltipVisitor = new TooltipVisitor(this);
		_imageVisitor = new ImageVisitor(this);
		_linkVisitor = new LinkVisitor(this);
	}

	/**
	 * Creates the label aspect visitor.
	 */
	protected LabelVisitor createLabelVisitor() {
		return LabelVisitor.INSTANCE;
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return ((TLModelPart) object).visit(_labelVisitor, none);
	}

	@Override
	public String getType(Object object) {
		if (object == null) {
			return null;
		}
		return ((TLModelPart) object).visit(_typeVisitor, none);
	}

	@Override
	public String getTooltip(Object object) {
		if (object == null) {
			return null;
		}
		return Resources.getInstance().getString(((TLModelPart) object).visit(_tooltipVisitor, none), null);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor aFlavor) {
		if (object == null) {
			return null;
		}
		return ((TLModelPart) object).visit(_imageVisitor, aFlavor);
	}

	@Override
	public String getLink(DisplayContext context, Object object) {
		if (object == null) {
			return null;
		}
		return ((TLModelPart) object).visit(_linkVisitor, context);
	}

}
