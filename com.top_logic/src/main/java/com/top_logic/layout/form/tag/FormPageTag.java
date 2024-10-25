/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.control.BooleanChoiceControl;
import com.top_logic.layout.form.control.CheckboxControl;
import com.top_logic.layout.form.control.ChoiceControl;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.IconSelectControl;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.control.SelectOptionControl;
import com.top_logic.layout.form.control.ValueDisplayControl;
import com.top_logic.layout.form.control.WithPlaceHolder;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLMimeTypes;

/**
 * {@link FormTag} rendering a fixed title area.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FormPageTag extends PageAreaTag implements FormTagProperties {

	/**
	 * The CSS class to use for a simple icon in the icon bar.
	 */
	public static final String IMAGE_CSS_CLASS = "fptIcon";

	/**
	 * The CSS class to use for a icon with overlay in the icon bar.
	 * 
	 * @see #ACTION_IMAGE_CSS_CLASS
	 */
	public static final String IMAGE_WITH_ACTION_CSS_CLASS = "fptIconWithOverlay";

	/**
	 * The CSS class for the icon overlay, see {@link #IMAGE_WITH_ACTION_CSS_CLASS}.
	 */
	public static final String ACTION_IMAGE_CSS_CLASS = "fptIconOverlay";

	private final InnerFormTag _formTag = new InnerFormTag();

	private TLType _type;

	/**
	 * @see #setTypeField(String)
	 */
	private FormField _typeField;

	private ResKey _titleKey;

	private ResKey _titleMessageKey;

	private String _titleField;

	private ResKey _subtitleKey;

	private String _subtitleField;

	private ThemeImage _icon;

	private ResKey _imageTooltipKey;

	private ThemeImage _actionIcon;

	private String _typeFieldName;

	private String _imageTooltipKeySuffix;

	private String _subtitleKeySuffix;

	private String _titleKeySuffix;

	private String _titleMessageKeySuffix;

	/**
	 * The type of the displayed model element.
	 * 
	 * <p>
	 * The type information is used to derive the {@link #setImage(String)} and
	 * {@link #setImageTooltipKey(String)} properties, if not explicitly specified.
	 * </p>
	 * 
	 * <p>
	 * Used in context where the {@link #getComponent()} has no model (e.g. in create contexts).
	 * </p>
	 * 
	 * @see #setTypeField(String)
	 */
	public void setType(TLType type) {
		_type = type;
	}

	/**
	 * The field name to take the model type from (if the component does not provide a model).
	 * 
	 * <p>
	 * Takes precedence over {@link #setType(TLType)}.
	 * </p>
	 * 
	 * <p>
	 * The field is expected to contain either a single {@link TLType} instance, or a {@link String}
	 * with a types qualified dotted name (see {@link TLModelUtil#qualifiedNameDotted(TLType)}).
	 * </p>
	 * 
	 * @see #setType(TLType)
	 */
	public void setTypeField(String typeFieldName) {
		_typeFieldName = typeFieldName;
	}

	/**
	 * Name of the form field to display in the title area.
	 * 
	 * @see #setTitleField(String)
	 * @see #setTitleKey(String)
	 * @see #setTitleKeySuffix(String)
	 * @see #setTitleMessageKey(String)
	 * @see #setTitleMessageKeySuffix(String)
	 */
	@CalledFromJSP
	public final void setTitleField(String titleField) {
		_titleField = titleField;
	}

	/**
	 * @see #setTitleField(String)
	 */
	protected String getTitleField() {
		return _titleField;
	}

	/**
	 * The message key suffix for creating the page title for edit (with model).
	 * 
	 * @see #setTitleField(String)
	 * @see #setTitleKey(String)
	 * @see #setTitleKeySuffix(String)
	 * @see #setTitleMessageKey(String)
	 * @see #setTitleMessageKeyConst(ResKey)
	 * @see #setTitleMessageKeySuffix(String)
	 */
	@CalledFromJSP
	public void setTitleMessageKey(String titleMessageKey) {
		setTitleMessageKeyConst(ResKey.internalJsp(titleMessageKey));
	}

	/**
	 * The message key suffix for creating the page title for edit (with model).
	 * 
	 * @see #setTitleField(String)
	 * @see #setTitleKey(String)
	 * @see #setTitleKeySuffix(String)
	 * @see #setTitleMessageKey(String)
	 * @see #setTitleMessageKeyConst(ResKey)
	 * @see #setTitleMessageKeySuffix(String)
	 */
	@CalledFromJSP
	public void setTitleMessageKeyConst(ResKey titleMessageKey) {
		_titleMessageKey = titleMessageKey;
	}

	/**
	 * The suffix to the component's resource key for creating the page title for edit (with model).
	 * 
	 * @see #setTitleField(String)
	 * @see #setTitleKey(String)
	 * @see #setTitleKeySuffix(String)
	 * @see #setTitleMessageKey(String)
	 * @see #setTitleMessageKeySuffix(String)
	 */
	@CalledFromJSP
	public void setTitleMessageKeySuffix(String titleMessageKeySuffix) {
		_titleMessageKeySuffix = titleMessageKeySuffix;
	}

	/**
	 * Guess a default value for the model type, if there is no type specification in the tag.
	 */
	protected TLType guessType() {
		return null;
	}

	/**
	 * The resource key suffix to the component's resource prefix for creating the page title for
	 * create (without model).
	 * 
	 * @see #setTitleField(String)
	 * @see #setTitleKey(String)
	 * @see #setTitleKeySuffix(String)
	 * @see #setTitleMessageKey(String)
	 * @see #setTitleMessageKeySuffix(String)
	 */
	@CalledFromJSP
	public void setTitleKeySuffix(String titleKeySuffix) {
		_titleKeySuffix = titleKeySuffix;
	}

	/**
	 * The resource key for creating the page title for create (without model).
	 * 
	 * @see #setTitleField(String)
	 * @see #setTitleKey(String)
	 * @see #setTitleKeyConst(ResKey)
	 * @see #setTitleKeySuffix(String)
	 * @see #setTitleMessageKey(String)
	 * @see #setTitleMessageKeySuffix(String)
	 */
	@CalledFromJSP
	public void setTitleKey(String titleKey) {
		setTitleKeyConst(ResKey.internalJsp(titleKey));
	}

	/**
	 * The resource key for creating the page title for create (without model).
	 * 
	 * @see #setTitleField(String)
	 * @see #setTitleKey(String)
	 * @see #setTitleKeySuffix(String)
	 * @see #setTitleMessageKey(String)
	 * @see #setTitleMessageKeySuffix(String)
	 */
	@CalledFromJSP
	public void setTitleKeyConst(ResKey titleKey) {
		_titleKey = titleKey;
	}

	boolean hasCustomTitle() {
		return _titleKey != null;
	}

	/**
	 * The form field name of the field to display in the sub-title area.
	 * 
	 * @see #setSubtitleKeySuffix(String)
	 */
	@CalledFromJSP
	public void setSubtitleField(String subtitleField) {
		_subtitleField = subtitleField;
	}

	/**
	 * @see #setSubtitleField(String)
	 */
	public String getSubtitleField() {
		return _subtitleField;
	}

	/**
	 * The key suffix to the component's resource prefix of the resource to display in the sub-title
	 * area.
	 * 
	 * @see #setSubtitleKey(String)
	 * @see #setSubtitleField(String)
	 */
	@CalledFromJSP
	public void setSubtitleKeySuffix(String subtitleKeySuffix) {
		_subtitleKeySuffix = subtitleKeySuffix;
	}

	/**
	 * The key of the resource to display in the sub-title area.
	 * 
	 * @see #setSubtitleKeySuffix(String)
	 * @see #setSubtitleKeyConst(ResKey)
	 * @see #setSubtitleField(String)
	 */
	@CalledFromJSP
	public void setSubtitleKey(String subtitleKey) {
		setSubtitleKeyConst(ResKey.internalJsp(subtitleKey));
	}

	/**
	 * The key of the resource to display in the sub-title area.
	 * 
	 * @see #setSubtitleKeySuffix(String)
	 * @see #setSubtitleKeyConst(ResKey)
	 * @see #setSubtitleField(String)
	 */
	@CalledFromJSP
	public void setSubtitleKeyConst(ResKey subtitleKey) {
		_subtitleKey = subtitleKey;
	}

	/**
	 * Theme image key of the image to display in the header area.
	 * 
	 * Use {@link #setIcon(ThemeImage)}.
	 */
	@CalledFromJSP
	@Deprecated
	public void setImage(String image) {
		setIcon(ThemeImage.icon(image));
	}

	/**
	 * {@link ThemeImage} to display in the header area.
	 */
	@CalledFromJSP
	public void setIcon(ThemeImage icon) {
		_icon = icon;
	}

	/**
	 * Image of the overlay image to display over the header area image.
	 */
	public ThemeImage getActionIcon() {
		// Note: Must test against null (not empty string) to allow removing the default action
		// image on the JSP by setting action="".
		if (_actionIcon == null) {
			if (isCreateMode()) {
				return Icons.PLUS48;
			}
			if (inEditMode()) {
				return Icons.EDIT48;
			}
			return null;
		} else {
			return _actionIcon;
		}
	}

	private boolean isCreateMode() {
		return getComponent() instanceof AbstractCreateComponent;
	}

	/**
	 * @see #getActionIcon()
	 */
	@CalledFromJSP
	public void setActionIcon(ThemeImage icon) {
		_actionIcon = icon;
	}

	/**
	 * Use {@link #setActionIcon(ThemeImage)}.
	 * 
	 * @see #getActionIcon()
	 */
	@CalledFromJSP
	@Deprecated
	public void setActionImage(String actionImage) {
		setActionIcon(ThemeImage.icon(actionImage));
	}

	/**
	 * The key suffix (to the component's resource prefix) of the resource to display as tooltip
	 * over the header image.
	 * 
	 * @see #setImage(String)
	 * @see #setImageTooltipKey(String)
	 */
	@CalledFromJSP
	public void setImageTooltipKeySuffix(String imageTooltipKeySuffix) {
		_imageTooltipKeySuffix = imageTooltipKeySuffix;
	}

	/**
	 * The fully qualified resource key to display as tooltip over the header image.
	 * 
	 * @see #setImage(String)
	 * @see #setImageTooltipKey(String)
	 * @see #setImageTooltipKeyConst(ResKey)
	 */
	@CalledFromJSP
	public void setImageTooltipKey(String imageTooltipKey) {
		setImageTooltipKeyConst(ResKey.internalJsp(imageTooltipKey));
	}

	/**
	 * The fully qualified resource key to display as tooltip over the header image.
	 * 
	 * @see #setImage(String)
	 * @see #setImageTooltipKey(String)
	 * @see #setImageTooltipKeyConst(ResKey)
	 */
	@CalledFromJSP
	public void setImageTooltipKeyConst(ResKey key) {
		_imageTooltipKey = key;
	}

	@Override
	public void setDisplayCondition(boolean value) {
		formTag().setDisplayCondition(value);
	}

	@Override
	public void setDisplayWithoutModel(boolean value) {
		formTag().setDisplayWithoutModel(value);
	}

	@Override
	public void setIgnoreModel(boolean value) {
		formTag().setIgnoreModel(value);
	}

	@Override
	public void setNoModelKey(String value) {
		formTag().setNoModelKey(value);
	}

	@Override
	public void setNoModelKeyConst(ResKey value) {
		formTag().setNoModelKeyConst(value);
	}

	@Override
	public void setNoModelKeySuffix(String value) {
		formTag().setNoModelKeySuffix(value);
	}

	/**
	 * Sets the value for whether the label is rendered above the content.
	 * 
	 * @param labelAbove
	 *        If <code>true</code> label is rendered above, else it will be rendered before.
	 */
	public void setLabelAbove(Boolean labelAbove) {
		_formTag.setLabelAbove(labelAbove);
	}

	/**
	 * Returns whether the label is rendered above the content.
	 * 
	 * @return If <code>true</code> label is rendered above, else it will be rendered before.
	 */
	public Boolean getLabelAbove() {
		return _formTag.getLabelAbove();
	}

	@Override
	protected void setup() throws JspException {
		FormTag formTag = formTag();
		formTag.doStartTag();

		super.setup();

		if (_titleMessageKeySuffix != null) {
			_titleMessageKey = formTag.toResourceKey(_titleMessageKeySuffix);
		}
		if (_typeFieldName != null) {
			_typeField = (FormField) FormGroup.getMemberByRelativeName(formTag.getFormContext(), _typeFieldName);
		}
		if (_imageTooltipKeySuffix != null) {
			setImageTooltipKeyConst(formTag.toResourceKey(_imageTooltipKeySuffix));
		}
		if (_subtitleKeySuffix != null) {
			setSubtitleKeyConst(formTag.toResourceKey(_subtitleKeySuffix));
		}
		if (_titleKeySuffix != null) {
			setTitleKeyConst(formTag.toResourceKey(_titleKeySuffix));
		}
	}

	@Override
	protected int startElement() throws JspException, IOException {
		int result = super.startElement();

		if (preventBodyRendering()) {
			return SKIP_BODY;
		}

		return result;
	}

	@Override
	protected boolean writeAttributeTitleContent() throws IOException {
		String titleField = getTitleField();
		if (titleField != null && formTag().shouldDisplay()) {
			writeField(titleField);
			return true;
		}

		return super.writeAttributeTitleContent();
	}

	@Override
	protected void writeDefaultTitleContent() throws IOException {
		if (getModel() != null) {
			writeTitleWithModel();
		} else {
			writeTitleNoModel();
		}
	}

	@Override
	protected boolean writeAttributeSubTitleContent() throws IOException {
		String subtitleField = getSubtitleField();
		if (subtitleField != null && formTag().shouldDisplay()) {
			writeField(subtitleField);
			return true;
		} else if (_subtitleKey != null) {
			writeResource(_subtitleKey);
			return true;
		} else {
			return super.writeAttributeSubTitleContent();
		}
	}

	@Override
	protected void writeDefaultSubTitleContent() throws IOException {
		final SimpleConstantControl<Object> display =
			new SimpleConstantControl<>(null, TypeNameRenderer.INSTANCE);
		basedOnType(new ConstantControlValue<>(display, display));
		display.write(getDisplayContext(), getOut());
	}

	@Override
	protected void writeDefaultIconBarContent() throws IOException {
		if (getModel() != null || isCreateMode()) {
			IconControl typeIcon = new IconControl();
			IconControl actionIcon = new IconControl();
	
			ThemeImage actionImage = getActionIcon();
			boolean hasActionImage = actionImage != null;
	
			typeIcon.setCssClass(hasActionImage ? IMAGE_WITH_ACTION_CSS_CLASS : IMAGE_CSS_CLASS);
			actionIcon.setCssClass(ACTION_IMAGE_CSS_CLASS);
			if (actionImage != null) {
				actionIcon.setSrc(actionImage.toIcon());
			}
	
			initTypeIcon(typeIcon);
			initActionIcon(actionIcon);
	
			write(typeIcon);
			write(actionIcon);
		}
	}

	@Override
	protected boolean hasSynthesizedBody() {
		return preventBodyRendering() || super.hasSynthesizedBody();
	}

	@Override
	protected void writeDefaultBodyContent() throws IOException {
		if (preventBodyRendering()) {
			formTag().doWriteNoModel();
		}
	}

	private boolean preventBodyRendering() {
		return !formTag().shouldDisplay();
	}

	private void write(View view) throws IOException {
		view.write(getDisplayContext(), getOut());
	}

	private void writeTitleWithModel() throws IOException {
		String modelLabel = MetaLabelProvider.INSTANCE.getLabel(getModel());
		if (_titleMessageKey != null) {
			writeText(Resources.getInstance().getMessage(_titleMessageKey, modelLabel));
		} else {
			writeText(modelLabel);
		}
	}

	private void writeTitleNoModel() throws IOException {
		if (hasCustomTitle()) {
			// Custom no-model title.
			writeResource(_titleKey);
		} else {
			// Default missing-model title.
			formTag().writeNoModelText();
		}
	}

	private void writeField(String fieldName) throws IOException {
		FormContext fc = formTag().getFormContext();
		FormMember member = fc.getMember(fieldName);
		HTMLFragment titleControl = inputEmbedded(member);
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
		TagWriter out = getOut();
		titleControl.write(displayContext, out);
	}

	/**
	 * Display of the given {@link FormMember} compressed label and error icon.
	 */
	public static HTMLFragment inputEmbedded(FormMember member) {
		return inputEmbedded(DefaultFormFieldControlProvider.INSTANCE, member);
	}

	/**
	 * Display of the given {@link FormMember} compressed label and error icon.
	 */
	public static HTMLFragment inputEmbedded(ControlProvider cp, FormMember member) {
		HTMLFragment input = input(cp, member);
		HTMLFragment error = error(cp, member);
		if (input instanceof WithPlaceHolder) {
			String placeHolder =
				DefaultDisplayContext.getDisplayContext().getResources().getString(I18NConstants.INPUT_VALUE__ATTRIBUTE.fill(member.getLabel()));
			((WithPlaceHolder) input).setPlaceHolder(placeHolder);
			return concat(input, nbsp(), error);
		} else if (input instanceof SelectControl || input instanceof DropDownControl) {
			String emptyLabel =
				DefaultDisplayContext.getDisplayContext().getResources().getString(I18NConstants.SELECT_VALUE__ATTRIBUTE.fill(member.getLabel()));
			((SelectField) ((Control) input).getModel()).setEmptyLabel(emptyLabel);
			return concat(input, nbsp(), error);
		} else if (input instanceof ValueDisplayControl) {
			// Only a value should be displayed, no label.
			return concat(input, nbsp(), error);
		} else if (input instanceof CheckboxControl
			|| input instanceof IconSelectControl
			|| input instanceof ChoiceControl
			|| input instanceof SelectOptionControl
			|| input instanceof BooleanChoiceControl) {
			// Cannot embed label, use explicit label after value display.
			return concat(input, nbsp(), error, nbsp(), label(cp, member));
		} else {
			// Cannot embed label, use explicit label before value display.
			return concat(label(cp, member), nbsp(), input, nbsp(), error);
		}
	}

	/**
	 * Display of the given {@link FormMember}'s label.
	 */
	public static HTMLFragment label(FormMember member) {
		return label(DefaultFormFieldControlProvider.INSTANCE, member);
	}

	/**
	 * Display of the given {@link FormMember}'s label.
	 */
	public static HTMLFragment label(ControlProvider cp, FormMember member) {
		return cp.createControl(member, FormTemplateConstants.STYLE_LABEL_VALUE);
	}

	/**
	 * Display of the given {@link FormMember}'s input element.
	 */
	public static HTMLFragment input(FormMember member) {
		return input(DefaultFormFieldControlProvider.INSTANCE, member);
	}

	/**
	 * Display of the given {@link FormMember}'s input element.
	 */
	public static HTMLFragment input(ControlProvider cp, FormMember member) {
		return cp.createControl(member);
	}

	/**
	 * Display of the given {@link FormMember}'s error view.
	 */
	public static HTMLFragment error(FormMember member) {
		return error(DefaultFormFieldControlProvider.INSTANCE, member);
	}

	/**
	 * Display of the given {@link FormMember}'s error view.
	 */
	public static HTMLFragment error(ControlProvider cp, FormMember member) {
		return cp.createControl(member, FormTemplateConstants.STYLE_ERROR_VALUE);
	}

	private void writeResource(ResKey messageKey) throws IOException {
		writeText(Resources.getInstance().decodeMessageFromKeyWithEncodedArguments(messageKey));
	}

	private void initActionIcon(IconControl icon) {
		initTypeTooltip(icon);
	}

	private void initTypeIcon(IconControl icon) {
		initTypeIconSrc(icon);
		initTypeTooltip(icon);
	}

	private void initTypeIconSrc(final IconControl icon) {
		if (_icon != null) {
			icon.setSrcKey(_icon);
		} else {
			Object model = getModel();
			if (model != null && _typeField == null && _type == null) {
				// Nothing special given on the JSP, try to ask the resource provider for the
				// current model.
				ThemeImage modelIcon = MetaResourceProvider.INSTANCE.getImage(model, Flavor.ENLARGED);
				if (modelIcon != null) {
					icon.setSrcKey(modelIcon);
					return;
				}
			}

			basedOnType(new ControlValue<>(icon) {
				@Override
				public void setValue(Object type) {
					if (type instanceof ResKey) {
						icon.setSrcKey(
							TLMimeTypes.getInstance().getMimeTypeImageLarge(((ResKey) type).getKey(),
							ThemeImage.none()));
					} else if (type instanceof TLType) {
						ThemeImage image = DefaultResourceProvider.getTypeImage((TLType) type, Flavor.ENLARGED);
						icon.setSrcKey(image == null ? ThemeImage.none() : image);
					} else {
						icon.setSrc(null);
					}
				}
			});
		}
	}

	private void initTypeTooltip(final IconControl icon) {
		if (_imageTooltipKey != null) {
			icon.setTooltip(Resources.getInstance().decodeMessageFromKeyWithEncodedArguments(_imageTooltipKey));
		} else {
			basedOnType(new ControlValue<>(icon) {
				@Override
				public void setValue(Object type) {
					if (type != null) {
						icon.setTooltip(MetaResourceProvider.INSTANCE.getLabel(type));
					}
				}
			});
		}
	}

	private <T> void basedOnType(final ValueSink<Object> sink) {
		Object model = getModel();
		if (_typeField != null) {
			final ValueListener transformation = new ValueListener() {
				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					Object dynamicValue = newValue instanceof Collection<?>
						? CollectionUtil.getSingleValueFromCollection((Collection<?>) newValue)
						: newValue;
					sink.setValue(dynamicValue);
				}
			};
			final FormField typeField = _typeField;
			typeField.addValueListener(transformation);
			transformation.valueChanged(typeField, null, typeField.getValue());
			sink.addDisposeListener(new Runnable() {
				@Override
				public void run() {
					typeField.removeValueListener(transformation);
				}
			});
		} else if (_type != null) {
			sink.setValue(_type);
		} else if (model instanceof TLObject) {
			sink.setValue(((TLObject) model).tType());
		} else {
			sink.setValue(guessType());
		}
	}

	private boolean inEditMode() {
		LayoutComponent component = getComponent();
		return (component instanceof EditComponent && ((EditComponent) component).isInEditMode());
	}

	/**
	 * The current component model.
	 */
	protected final Object getModel() {
		return formTag().getModel();
	}

	@Override
	protected void teardown() {
		_type = null;
		_icon = null;
		_imageTooltipKey = null;

		_titleField = null;
		_titleKey = null;
		_titleMessageKey = null;

		_subtitleKey = null;
		_actionIcon = null;

		super.teardown();

		try {
			_formTag.doEndTag();
		} catch (JspException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setParent(Tag parent) {
		_formTag.setParent(parent);

		super.setParent(_formTag);
	}

	@Override
	public void setPageContext(PageContext pageContext) {
		_formTag.setPageContext(pageContext);

		super.setPageContext(pageContext);
	}

	private InnerFormTag formTag() {
		return _formTag;
	}

	static final class TypeNameRenderer implements Renderer<Object> {
		/**
		 * Singleton {@link FormPageTag.TypeNameRenderer} instance.
		 */
		public static final TypeNameRenderer INSTANCE = new TypeNameRenderer();

		private TypeNameRenderer() {
			// Singleton constructor.
		}

		@Override
		public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
			if (value != null) {
				out.writeText(MetaResourceProvider.INSTANCE.getLabel(value));
			}
		}
	}

	static final class InnerFormTag extends FormTag {
		@Override
		protected void writeNoModel() throws IOException {
			// Write later.
		}

		public void doWriteNoModel() throws IOException {
			super.writeNoModel();
		}
	}

	interface ValueSink<T> {
		void setValue(T value);
	
		void addDisposeListener(Runnable runnable);
	}

	static abstract class ControlValue<T> implements ValueSink<T> {
	
		static final class OnDispose implements AttachedPropertyListener {
			private final Runnable _runnable;

			public OnDispose(Runnable runnable) {
				_runnable = runnable;
			}

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					_runnable.run();
				}
			}
		}

		private final AbstractControlBase _control;
	
		public ControlValue(AbstractControlBase control) {
			_control = control;
		}
	
		@Override
		public void addDisposeListener(final Runnable runnable) {
			_control.addListener(AbstractControlBase.ATTACHED_PROPERTY, new OnDispose(runnable));
		}
	}

	static final class ConstantControlValue<T> extends ControlValue<T> {
		private final SimpleConstantControl<T> _display;

		public ConstantControlValue(AbstractControlBase control, SimpleConstantControl<T> display) {
			super(control);
			_display = display;
		}

		@Override
		public void setValue(T value) {
			_display.setModel(value);
		}
	}

}
