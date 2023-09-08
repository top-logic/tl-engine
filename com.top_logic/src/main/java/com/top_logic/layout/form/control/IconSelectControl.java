/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.provider.BooleanResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Utils;
import com.top_logic.util.css.CssUtil;

/**
 * {@link Control} that selects an option by cycling through a list of icons.
 * 
 * <p>
 * As model of an {@link IconSelectControl} either a {@link BooleanField} or a
 * {@link SelectField} can be used.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IconSelectControl extends AbstractFormFieldControl {

	/**
	 * CSS class for the actual images.
	 */
	private static final String TRISTATE_CSS_CLASS = FormConstants.INPUT_IMAGE_CSS_CLASS + " tristate";

	/**
	 * Custom HTML attribute to annotate the JS expression that should be
	 * evaluated and sent to the server as value for a certain selection.
	 */
	private static final String TL_VALUE_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "value";
	
	/**
	 * Custom HTML attribute to annotate the cycle direction in which the
	 * client-side control cycles through its images.
	 */
	private static final String TL_POSITIVE_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "positive";
	
	private static final String TL_RESETABLE_ATTR = HTMLConstants.DATA_ATTRIBUTE_PREFIX + "resetable";

	private static final List<Boolean> BOOLEAN_OPTIONS = 
		Arrays.asList(Boolean.TRUE, Boolean.FALSE);
	
    private static final String[] BOOLEAN_VALUE_KEYS = 
    	{BooleanField.NONE_RAW_VALUE, BooleanField.TRUE_RAW_VALUE, BooleanField.FALSE_RAW_VALUE};

	protected static final Map COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] {
		});

	/**
     * Order of state change.
     *
     * <p>
     * If state change order is positive, the state changes from "true" to "false" to
     * "empty". if the state change order is negative, the state changes from "false" to
     * "true" to "empty". The initial display decides about the state change order. If a
     * field is initially "true" or "empty", the state change order is positive, if a field
     * is initially "false", the state change order is negative.
     * </p>
     */
    private boolean positive;

    /**
     * @see #isResetable()
     */
    private boolean resetable = true;

	private ResourceProvider resourceProvider;

	/**
	 * Creates a {@link IconSelectControl}.
	 * 
	 * @param model
	 *        the {@link BooleanField} or {@link SelectField} model.
	 */
	public IconSelectControl(FormField model, ResourceProvider resourceProvider) {
		super(model, COMMANDS);
		
		assert (model instanceof BooleanField) || (model instanceof SelectField) : "Invalid field: " + model;
		assert resourceProvider != null : "Resource provider is mandatory.";
		
		List<?> options = getOptions(model);
		Object value = getValue(model);
		// All but the last option cycles in positive direction.
		this.positive = options.size() == 0 || (! Utils.equals(options.get(options.size() - 1), value));
		this.resourceProvider = resourceProvider;
	}
	
    /**
     * Whether the user may reset the value to <code>null</code> alias no selection.
     */
    public boolean isResetable() {
        return this.resetable;
    }

    /**
     * @see #isResetable()
     */
    public void setResetable(boolean aResetable) {
        this.resetable = aResetable;
        requestRepaint();
    }
    
	@Override
	protected String getTypeCssClass() {
		return "cIconSelect";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		FormField field = getFieldModel();
		List<?> options = getOptions(field);
		Object fieldValue = getValue(field);
		
		boolean hasValue = fieldValue != null;
		
		// The client view must be able to select the first option, either if
		// this is resettabel, or the null option has not been rendered as all,
		// which make the first option a regular choice.
		boolean clientResettable = this.resetable || hasValue;
		
		boolean mandatory = field.isMandatory();
		boolean disabled = field.isDisabled();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.writeAttribute(TL_POSITIVE_ATTR, this.positive);
		out.writeAttribute(TL_RESETABLE_ATTR, clientResettable);
		out.endBeginTag();

		Flavor imageFlavor;
		if (mandatory) {
			if (disabled) {
				imageFlavor = Flavor.MANDATORY_DISABLED;
			} else {
				imageFlavor = Flavor.MANDATORY;
			}
		} else {
			if (disabled) {
				imageFlavor = Flavor.DISABLED;
			} else {
				imageFlavor = Flavor.DEFAULT;
			}
		}
		
        if (! disabled) {
        	if ((! hasValue) || this.resetable) {
        		// Either the value is currently null, or the value can be changed back to null.
        		writeActiveImage(context, out, disabled, imageFlavor, null, 0, ! hasValue);
        	}
        	for (int n = 0, cnt = options.size(); n < cnt; n++) {
        		Object optionValue = options.get(n);
				writeActiveImage(context, out, disabled, imageFlavor, optionValue, n + 1, Utils.equals(fieldValue, optionValue));
        	}
        }
        else {
			writeActiveImage(context, out, disabled, imageFlavor, fieldValue, -1, true);
        }

        out.endTag(SPAN);
	}

	private void writeValueAttribute(TagWriter out, Object fieldValue, int index) throws IOException {
		out.beginAttribute(TL_VALUE_ATTR);
		writeValueAttributeContent(out, index, fieldValue);
		out.endAttribute();
	}

	private void writeValueAttributeContent(TagWriter out, int imageIndex, Object value) throws IOException {
		if (getModel() instanceof BooleanField) {
			TagUtil.writeJsString(out, BOOLEAN_VALUE_KEYS[imageIndex]);
		} else {
			if (value == null) {
				out.append("[]");
			} else {
				SelectField selectField = (SelectField) getModel();
				out.append("[");
				TagUtil.writeJsString(out, selectField.getOptionID(value));
				out.append("]");
			}
		}
	}

	protected Object getValue(FormField field) {
		return CollectionUtil.getSingleValueFrom(field.getValue());
	}

	protected List<?> getOptions(FormField field) {
		List<?> options;
		if (field instanceof BooleanField) {
			options = BOOLEAN_OPTIONS;
		} else {
			options = ((SelectField) field).getOptions();
		}
		return options;
	}

	private void writeActiveImage(DisplayContext context, TagWriter out, boolean disabled, Flavor imageFlavor, Object fieldValue, int index, boolean isOptionActive)
		throws IOException
	{
		XMLTag icon = resourceProvider.getImage(fieldValue, imageFlavor).toButton();
		icon.beginBeginTag(context, out);
		CssUtil.writeCombinedCssClasses(out,
			TRISTATE_CSS_CLASS,
			resourceProvider.getCssClass(fieldValue),
			disabled ? FormConstants.DISABLED_CSS_CLASS : null);
		if (index >= 0) {
			writeValueAttribute(out, fieldValue, index);
		}
		out.writeAttribute(STYLE_ATTR, (isOptionActive ? "display: inline; " : "display: none; "));

		if (disabled) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		} else {
			writeOnClick(out, FormConstants.ICON_SELECT_CONTROL_CLASS, this, null);
		}
		icon.endEmptyTag(context, out);
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		FormField field = getFieldModel();
		Object fieldValue = getValue(field);
		
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();

		XMLTag icon = resourceProvider.getImage(fieldValue, Flavor.IMMUTABLE).toButton();
		icon.beginBeginTag(context, out);
		CssUtil.writeCombinedCssClasses(out,
			TRISTATE_CSS_CLASS,
			resourceProvider.getCssClass(fieldValue),
			FormConstants.IMMUTABLE_CSS_CLASS);
		out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);

		out.writeAttribute(ALT_ATTR, "");
		icon.endEmptyTag(context, out);

		out.endTag(SPAN);
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent(sender);
	}

	/**
	 * {@link ControlProvider} that creates {@link IconSelectControl}s for
	 * {@link BooleanField} and {@link SelectField}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Provider extends DefaultFormFieldControlProvider {
		
		public interface Config extends PolymorphicConfiguration<ControlProvider> {
		@Name(XML_CONFIG_RESETABLE)
		@BooleanDefault(false)
		boolean getResetable();

		@Name(XML_CONFIG_RESOURCE_PROVIDER)
		@InstanceFormat
		ResourceProvider getResourceProvider();
	}

	private static final String XML_CONFIG_RESOURCE_PROVIDER = "resourceProvider";
		private static final String XML_CONFIG_RESETABLE = "resetable";
		private final ResourceProvider resourceProvider;
		private final boolean resetable;
		
		/**
		 * Singleton {@link IconSelectControl.Provider} instance that creates
		 * {@link IconSelectControl#isResetable() non-resettable}
		 * {@link IconSelectControl}s.
		 */
		public static final Provider INSTANCE = new Provider(false, null);

		/**
		 * Singleton {@link IconSelectControl.Provider} instance that creates
		 * {@link IconSelectControl#isResetable() resettable}
		 * {@link IconSelectControl}s.
		 */
		public static final Provider INSTANCE_RESETTABLE = new Provider(true, null);
		
		/**
		 * Creates a {@link Provider}.
		 *
		 * @param resetable See {@link IconSelectControl#IconSelectControl(FormField, ResourceProvider)}
		 * @param resourceProvider See {@link IconSelectControl#IconSelectControl(FormField, ResourceProvider)}
		 */
		public Provider(boolean resetable, ResourceProvider resourceProvider) {
			this.resetable = resetable;
			this.resourceProvider = resourceProvider;
		}
		
		public Provider(InstantiationContext context, Config config) throws ConfigurationException {
			this.resetable = config.getResetable();
			this.resourceProvider =
				config.getResourceProvider();
		}

		@Override
		public Control visitBooleanField(BooleanField member, Void arg) {
			IconSelectControl iconSelectControl =
				new IconSelectControl(member, resourceProvider != null ? resourceProvider
					: DefaultBooleanTristateResourceProvider.INSTANCE);
			iconSelectControl.setResetable(this.resetable);
			return iconSelectControl;
		}
		
		@Override
		public Control visitSelectField(SelectField member, Void arg) {
			ResourceProvider customResourceProvider;
			if (resourceProvider != null) {
				customResourceProvider = this.resourceProvider;
			} else {
				LabelProvider optionLabelProvider = member.getOptionLabelProvider();
				if (optionLabelProvider instanceof ResourceProvider) {
					customResourceProvider = (ResourceProvider) optionLabelProvider;
				} else {
					customResourceProvider = MetaResourceProvider.INSTANCE;
				}
			}

			IconSelectControl result = new IconSelectControl(member, customResourceProvider);
			result.setResetable(resetable);
			
			return result;
		}
		
	}

	/**
	 * The class {@link BooleanTristateResourceProvider} returns images depending on
	 * {@link Flavor#MANDATORY} and {@link Flavor#DISABLED}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static abstract class BooleanTristateResourceProvider extends DefaultResourceProvider {

		private Map<Boolean, ? extends ThemeImage> mandatoryDisabledImages;

		private Map<Boolean, ? extends ThemeImage> mandatoryNonDisabledImages;

		private Map<Boolean, ? extends ThemeImage> nonMandatoryDisabledImages;

		private Map<Boolean, ? extends ThemeImage> nonMandatoryNonDisabledImages;

		protected BooleanTristateResourceProvider() {
			mandatoryDisabledImages = mandatoryDisabledImages();
			mandatoryNonDisabledImages = mandatoryNonDisabledImages();
			nonMandatoryDisabledImages = nonMandatoryDisabledImages();
			nonMandatoryNonDisabledImages = nonMandatoryNonDisabledImages();
		}

		/**
		 * creates a map which maps {@link Boolean#TRUE}, {@link Boolean#FALSE} and
		 * <code>null</code> to some non null {@link ThemeImage} in flavor {@link Flavor#MANDATORY}
		 * and {@link Flavor#DISABLED}.
		 */
		protected abstract Map<Boolean, ? extends ThemeImage> mandatoryDisabledImages();

		/**
		 * creates a map which maps {@link Boolean#TRUE}, {@link Boolean#FALSE} and
		 * <code>null</code> to some non null {@link ThemeImage} in flavor {@link Flavor#MANDATORY}
		 * but not {@link Flavor#DISABLED}.
		 */
		protected abstract Map<Boolean, ? extends ThemeImage> mandatoryNonDisabledImages();

		/**
		 * creates a map which maps {@link Boolean#TRUE}, {@link Boolean#FALSE} and
		 * <code>null</code> to some non null {@link ThemeImage} in flavor {@link Flavor#DISABLED}
		 * but not {@link Flavor#MANDATORY}.
		 */
		protected abstract Map<Boolean, ? extends ThemeImage> nonMandatoryDisabledImages();

		/**
		 * creates a map which maps {@link Boolean#TRUE}, {@link Boolean#FALSE} and
		 * <code>null</code> to some non null {@link ThemeImage} in flavor neither
		 * {@link Flavor#MANDATORY} nor {@link Flavor#DISABLED}.
		 */
		protected abstract Map<Boolean, ? extends ThemeImage> nonMandatoryNonDisabledImages();

		@Override
		public ThemeImage getImage(Object object, Flavor aFlavor) {
			if (aFlavor.implies(Flavor.MANDATORY)) {
				if (aFlavor.implies(Flavor.DISABLED)) {
					return mandatoryDisabledImages.get(object);
				} else {
					return mandatoryNonDisabledImages.get(object);
				}
			} else {
				if (aFlavor.implies(Flavor.DISABLED)) {
					return nonMandatoryDisabledImages.get(object);
				} else {
					return nonMandatoryNonDisabledImages.get(object);
				}
			}
		}

		@Override
		public String getCssClass(Object anObject) {
			if (anObject instanceof Boolean) {
				return BooleanResourceProvider.BOOLEAN_CSS_CLASS;
			}
			if (anObject == null) {
				return BooleanResourceProvider.BOOLEAN_NULL_CSS_CLASS;
			}
			return super.getCssClass(anObject);
		}
	}
	
	/**
	 * {@link ResourceProvider} for {@link Boolean} values that produces checkbox images.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class DefaultBooleanTristateResourceProvider extends BooleanTristateResourceProvider {

		/**
		 * Singleton {@link IconSelectControl.DefaultBooleanTristateResourceProvider} instance.
		 */
		public static final DefaultBooleanTristateResourceProvider INSTANCE =
			new DefaultBooleanTristateResourceProvider();

		private DefaultBooleanTristateResourceProvider() {
			// Singleton constructor.
		}

		@Override
		protected Map<Boolean, ? extends ThemeImage> mandatoryDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.MANDATORY_TRUE_DISABLED)
				.put(Boolean.FALSE, Icons.MANDATORY_FALSE_DISABLED)
				.put(null, Icons.MANDATORY_NULL_DISABLED).toMap();
		}

		@Override
		protected Map<Boolean, ? extends ThemeImage> mandatoryNonDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.MANDATORY_TRUE)
				.put(Boolean.FALSE, Icons.MANDATORY_FALSE)
				.put(null, Icons.MANDATORY_NULL).toMap();
		}

		@Override
		protected Map<Boolean, ? extends ThemeImage> nonMandatoryDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.TRISTATE_TRUE_DISABLED)
				.put(Boolean.FALSE, Icons.TRISTATE_FALSE_DISABLED)
				.put(null, Icons.TRISTATE_NULL_DISABLED).toMap();
		}

		@Override
		protected Map<Boolean, ? extends ThemeImage> nonMandatoryNonDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.TRISTATE_TRUE)
				.put(Boolean.FALSE, Icons.TRISTATE_FALSE)
				.put(null, Icons.TRISTATE_NULL).toMap();
		}

	}

	/**
	 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
	 */
	public static class AccumulatedBooleanTristateResourceProvider extends BooleanTristateResourceProvider {
	
		public static final AccumulatedBooleanTristateResourceProvider INSTANCE =
			new AccumulatedBooleanTristateResourceProvider();
	
		private AccumulatedBooleanTristateResourceProvider() {
			// singleton instance
		}
	
		@Override
		protected Map<Boolean, ? extends ThemeImage> mandatoryDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.ACCUMULATED_TRISTATE_TRUE_DISABLED)
				.put(Boolean.FALSE, Icons.ACCUMULATED_TRISTATE_FALSE_DISABLED)
				.put(null, Icons.ACCUMULATED_TRISTATE_NULL_DISABLED).toMap();
		}
	
		@Override
		protected Map<Boolean, ? extends ThemeImage> mandatoryNonDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.ACCUMULATED_TRISTATE_TRUE)
				.put(Boolean.FALSE, Icons.ACCUMULATED_TRISTATE_FALSE)
				.put(null, Icons.ACCUMULATED_TRISTATE_NULL).toMap();
		}
	
		@Override
		protected Map<Boolean, ? extends ThemeImage> nonMandatoryDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.ACCUMULATED_TRISTATE_TRUE_DISABLED)
				.put(Boolean.FALSE, Icons.ACCUMULATED_TRISTATE_FALSE_DISABLED)
				.put(null, Icons.ACCUMULATED_TRISTATE_NULL_DISABLED).toMap();
		}
	
		@Override
		protected Map<Boolean, ? extends ThemeImage> nonMandatoryNonDisabledImages() {
			return new MapBuilder<Boolean, ThemeImage>()
				.put(Boolean.TRUE, Icons.ACCUMULATED_TRISTATE_TRUE)
				.put(Boolean.FALSE, Icons.ACCUMULATED_TRISTATE_FALSE)
				.put(null, Icons.ACCUMULATED_TRISTATE_NULL).toMap();
		}
	
	}

}
