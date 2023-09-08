/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import java.text.ParseException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.form.format.ThemeImageFormat;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link LabelBasedPreview} whose label, description, and image are computed by some configured
 * {@link Expr expressions}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExprBasedTilePreview extends LabelBasedPreview<ExprBasedTilePreview.Config> {

	/**
	 * Typed configuration interface definition for {@link ExprBasedTilePreview}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@DisplayOrder({
		Config.MODEL,
		Config.LABEL,
		Config.DESCRIPTION,
		Config.IMAGE,
	})
	public interface Config extends LabelBasedPreview.Config<ExprBasedTilePreview> {

		/** Configuration name for the value of {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for the value of {@link #getDescription()}. */
		String DESCRIPTION = "description";

		/** Configuration name for the value of {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for the value of {@link #getModel()}. */
		String MODEL = "model";

		/**
		 * Actually unused.
		 */
		@Override
		@Hidden
		ThemeImage getIcon();

		/**
		 * {@link ModelSpec} that is used to compute {@link #getLabel()}, {@link #getDescription()},
		 * and {@link #getImage()} from.
		 */
		@Mandatory
		@Name(MODEL)
		@ImplementationClassDefault(DirectLinking.class)
		ModelSpec getModel();

		/**
		 * {@link Expr} computing the label of the preview.
		 * 
		 * <p>
		 * The expression is expected to accept one input element, the model returned by
		 * {@link #getModel()}. It is expected that the {@link Expr} returns an Object, that can be
		 * displayed as textual value, e.g. a {@link ResKey} or plain string.
		 * </p>
		 */
		@Mandatory
		@Name(LABEL)
		Expr getLabel();

		/**
		 * {@link Expr} computing the description of the preview.
		 * 
		 * <p>
		 * The expression is expected to accept one input element, the model returned by
		 * {@link #getModel()}. It is expected that the {@link Expr} returns an Object, that can be
		 * displayed as textual value, e.g. a {@link ResKey} or plain string.
		 * </p>
		 *
		 * @return May be <code>null</code>. In this case no description is rendered.
		 */
		@Name(DESCRIPTION)
		Expr getDescription();

		/**
		 * {@link Expr} computing the icon of the preview.
		 * 
		 * <p>
		 * The expression is expected to accept one input element, the model returned by
		 * {@link #getModel()}. It is expected that the {@link Expr} returns either a
		 * {@link ThemeImage} or the source for an image, e.g. "css:fas fa-bug".
		 * </p>
		 *
		 * @return May be <code>null</code>. In this case no image is rendered.
		 */
		@Name(IMAGE)
		Expr getImage();

	}

	private QueryExecutor _description;

	private QueryExecutor _image;

	private QueryExecutor _label;

	private ChannelLinking _model;

	/**
	 * Create a {@link ExprBasedTilePreview}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ExprBasedTilePreview(InstantiationContext context, Config config) {
		super(context, config);
		_model = context.getInstance(config.getModel());
		_label = QueryExecutor.compile(config.getLabel());
		_image = compileOptional(config.getImage());
		_description = compileOptional(config.getDescription());
	}

	private static QueryExecutor compileOptional(Expr expr) {
		if (expr == null) {
			return null;
		}
		return QueryExecutor.compile(expr);
	}

	@Override
	protected HTMLFragment labelContent(ComponentTile tile) {
		Object description = _label.execute(model(tile));
		if (description != null) {
			return rendered(description);
		}
		return Fragments.empty();
	}

	@Override
	protected ThemeImage icon(ComponentTile tile) {
		if (_image == null) {
			return null;
		}
		Object image = CollectionUtil.getSingleValueFrom(_image.execute(model(tile)));
		if (image == null) {
			return null;
		}
		if (image instanceof ThemeImage) {
			return (ThemeImage) image;
		} else if (image instanceof CharSequence) {
			try {
				return (ThemeImage) ThemeImageFormat.INSTANCE.parseObject(image.toString());
			} catch (ParseException ex) {
				InfoService.showError(I18NConstants.ERROR_INVALID_IMAGE_DEFINITION__SOURCE__POSITION
					.fill(image.toString(), ex.getErrorOffset()));
				markPreviewWithError();
				return null;
			}
		} else {
			InfoService.showError(I18NConstants.ERROR_NO_IMAGE__OBJ.fill(image));
			markPreviewWithError();
			return null;
		}
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		if (_description != null) {
			Object description = _description.execute(model(tile));
			if (description != null) {
				return rendered(description);
			}
		}
		return Fragments.empty();
	}

	private Object model(ComponentTile tile) {
		return ChannelLinking.eval(tile.getTileComponent(), _model);
	}

	private static <T> HTMLFragment rendered(T obj) {
		return Fragments.rendered(renderer(obj), obj);
	}

	private static <T> Renderer<? super T> renderer(T obj) {
		return LabelProviderService.getInstance().getRenderer(obj);
	}

}
