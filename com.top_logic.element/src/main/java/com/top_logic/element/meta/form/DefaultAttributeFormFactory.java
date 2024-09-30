/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateContainer.Handle;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContextProxy;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.model.annotate.ui.CssClassProvider;
import com.top_logic.model.annotate.ui.PDFRendererAnnotation;
import com.top_logic.model.annotate.ui.TLCssClass;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.export.pdf.PDFRenderer;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;


/**
 * Factory that creates {@link FormField}s for {@link AttributeUpdate}
 * containers.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultAttributeFormFactory extends AttributeFormFactoryBase {

	/** Switch to enable(default)/disable multi selection for classification attributes in the search. */
	protected static Boolean useSearchMultiForLists;

	/**
	 * Get the flag to enable(default)/disable multi selection for classification attributes in the search.
	 *
	 * @return the flag
	 */
	public static boolean getSearchMultiForLists() {
		if (useSearchMultiForLists == null) {
			useSearchMultiForLists = Boolean.valueOf(!"false".equalsIgnoreCase(Configuration.getConfigurationByName("AttributeFormFactory")
											.getProperties().getProperty("useSearchMultiForLists")));
		}

		return useSearchMultiForLists.booleanValue();
	}

	public static final Mapping<FormMember, String> ATTRIBUTED_CONFIG_NAME_MAPPING =
	new Mapping<>() {
		@Override
		public String map(FormMember member) {
			AttributeUpdate attributeUpdate = AttributeFormFactory.getAttributeUpdate(member);
			if (attributeUpdate != null) {
				TLStructuredTypePart metaAttribute = attributeUpdate.getAttribute();
				return IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(metaAttribute));
			} else {
				return member.getQualifiedName();
			}
		}
	};

	private static final Property<String> DYNAMIC_CSS_CLASS =
		TypedAnnotatable.property(String.class, "dynamicCssClass");

	@Override
	protected FormMember toFormField(AttributeUpdate update, AttributeUpdateContainer container, String fieldName) {
        TLStructuredTypePart theMA = update.getAttribute();
		FormMember result = createFormMember(update, container, fieldName);

		if (result instanceof FormField) {
			FormField resultField = (FormField) result;
			initFormField(update, resultField);
			if (AttributeOperations.isClassified(theMA) && !ThreadContext.isAdmin()) {
			    AccessManager theAM = AccessManager.getInstance();
			    Person        thePerson = TLContext.getContext().getCurrentPersonWrapper();
				TLObject theAttrib = update.getObject();
			    if (theAttrib instanceof BoundObject) {
					Collection<BoundRole> theRoles = theAM.getRoles(thePerson, (BoundObject) theAttrib);
					Collection<BoundCommandGroup> theAccess = AttributeOperations.getAccess(theMA, theRoles);
                    if ( ! theAccess.contains(SimpleBoundCommandGroup.READ)) {
                        resultField.setBlocked(true);
                    } else if ( ! theAccess.contains(SimpleBoundCommandGroup.WRITE)) {
                        resultField.setFrozen(true);
                    }
                }
            }
		} else if (result != null) {
			initFormMember(update, result);
		}

		if (result != null) {
			addCssClass(update, theMA, result);
		}

		return result;
	}

	private void addCssClass(AttributeUpdate update, TLStructuredTypePart attribute, FormMember member) {
		TLCssClass annotation = update.getAnnotation(TLCssClass.class);
		if (annotation == null) {
			return;
		}

		String staticCssClass = annotation.getValue();
		if (member instanceof FormField field) {
			PolymorphicConfiguration<? extends CssClassProvider> dynamicCssClass = annotation.getDynamicCssClass();
			if (dynamicCssClass != null) {
				CssClassProvider provider = TypedConfigUtil.createInstance(dynamicCssClass);
				ValueListener cssUpdate = (f, oldValue, newValue) -> {
					String oldClass = f.get(DYNAMIC_CSS_CLASS);
					String newClass = provider.getCssClass(update.getObject(), attribute, newValue);
					if (StringServices.equals(oldClass, newClass)) {
						return;
					}
					if (oldClass != null) {
						f.removeCssClass(oldClass);
					}
					if (newClass == null) {
						newClass = staticCssClass;
					}
					if (newClass != null) {
						f.addCssClass(newClass);
					}
					f.set(DYNAMIC_CSS_CLASS, newClass);
				};
				field.addValueListener(cssUpdate);

				// Setup initial value.
				cssUpdate.valueChanged(field, null, field.getValue());
				return;
			}
		}
		member.addCssClass(staticCssClass);
	}

	protected FormMember createFormMember(
			AttributeUpdate update, final AttributeUpdateContainer updateContainer, String name) {
		final TLStructuredTypePart attribute = update.getAttribute();

		if (update.getOutputMedia() == Media.PDF) {
			PDFRendererAnnotation rendererAnnotation = update.getAnnotation(PDFRendererAnnotation.class);
			if (rendererAnnotation != null) {
				PDFRenderer renderer = TypedConfigUtil.createInstance(rendererAnnotation.getImpl());
				FormMember result = FormFactory.newHiddenField(name);
				result.setControlProvider(new PDFControlProvider(renderer));
				return result;
			}
		}

		FieldProvider provider = AttributeOperations.getFieldProvider(attribute);
		if (provider != null) {
			FormMember result = provider.getFormField(update, name);

			if (result instanceof FormField) {
				FormField field = (FormField) result;

				TLDynamicVisibility modeAnnotation = attribute.getAnnotation(TLDynamicVisibility.class);
				if (modeAnnotation != null) {
					ModeSelector modeSelector = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
						.getInstance(modeAnnotation.getModeSelector());

					TLObject object = update.getOverlay();

					class Observer implements ValueListener, Sink<Pointer> {
						private final List<AttributeUpdateContainer.Handle> _handles = new ArrayList<>();

						@Override
						public void valueChanged(FormField changedField, Object oldValue, Object newValue) {
							FormVisibility mode = modeSelector.getMode(object, attribute);
							mode.applyTo(result);
							switch (mode) {
								case READ_ONLY:
									// Reset to original value to prevent modifying values by
									// temporarily activating fields.
									((FormField) result).reset();
									break;
								case HIDDEN:
									// Clear value to prevent leaking irrelevant values into the
									// model.
									((FormField) result).setValue(null);
									break;
								default:
									break;
							}

							removeListeners();

							modeSelector.traceDependencies(object, attribute, this);
						}

						private void removeListeners() {
							for (Handle handle : _handles) {
								handle.release();
							}
							_handles.clear();
						}

						@Override
						public void add(Pointer p) {
							_handles.add(updateContainer.addValueListener(p.object(), p.attribute(), this));
						}
					}

					new Observer().valueChanged(null, null, null);
				}

				TLConstraints annotation = attribute.getAnnotation(TLConstraints.class);
				if (annotation != null) {
					List<ConstraintCheck> checks = TypedConfiguration.getInstanceList(
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, annotation.getConstraints());
					Constraint constraint = new Constraint() {
						private Collection<FormField> _dependencies = Collections.emptyList();

						@Override
						public Collection<FormField> reportDependencies() {
							return _dependencies;
						}

						@Override
						public boolean check(Object value) throws CheckException {
							// Lazily initialize dependencies, since the are not yet available
							// at the time, the field is constructed. Moreover, the dependencies
							// must be updated whenever a new check occurs, since the test
							// expression may access other fields depending on the values received.
							// The simplest example is boolean evaluation, where the second
							// condition of an and condition is only evaluated, if the first
							// condition yields true.
							Set<FormField> newDependencies = computeDependencies();

							Collection<FormField> oldDependencies = _dependencies;
							for (FormField dependency : oldDependencies) {
								if (!newDependencies.contains(dependency)) {
									dependency.removeDependant(field);
								}
							}
							for (FormField dependency : newDependencies) {
								if (!oldDependencies.contains(dependency)) {
									dependency.addDependant(field);
								}
							}
							_dependencies = newDependencies;

							for (FormField dependency : _dependencies) {
								if (!dependency.hasValue()) {
									return false;
								}
							}

							TLObject object = updateContainer.getOverlay(update);
							for (ConstraintCheck check : checks) {
								ResKey failure = check.check(object, attribute);
								if (failure != null) {
									throw new CheckException(Resources.getInstance().getString(failure));
								}
							}
							return true;
						}

						private Set<FormField> computeDependencies() {
							HashSet<FormField> dependencies = new HashSet<>();
							TLObject object = updateContainer.getOverlay(update);
							for (ConstraintCheck dependency : checks) {
								dependency.traceDependencies(object, attribute, p -> {
									AttributeUpdate other = updateContainer.getAttributeUpdate(p.attribute(), p.object());
									if (other == null) {
										return;
									}
									
									FormMember otherMember = updateContainer.getFormContext().getMember(other);
									if (otherMember instanceof FormField && otherMember != field) {
										dependencies.add((FormField) otherMember);
									}
								});
							}
							return dependencies;
						}
					};
					field.addConstraint(constraint);
				}
			}
			return result;
		}

		Logger.error("No form field available for attribute '" + attribute + "'.",
			DefaultAttributeFormFactory.class);
		return null;
	}

	public static OptionModel<?> getOptionList(final EditContext editContext, final LabelProvider theProv,
			final SelectField selectField) {
		// Set the used comparator to the field to guarantee the same order of the options
		// in OptimizedSelectiorContext
		selectField.setOptionComparator(LabelComparator.newCachingInstance(theProv));
		
		// Create the possible options to this field as lazy list that
        // is only created, if it is really used. If the field is
        // rendered as popup selection and the user never touches the
        // popup button, the options list is never created.
		return AttributeOperations.allOptions(editContext);
	}

    /**
     * Reset the options of a SelectField trying to maintain the selection
     *
     * @param aContext		the form context
     * @param anAttName		the meta attribute name
     * @param anAttributed	the attributed object
     * @param aNewOptions   the new options. If null use the original options
     */
	public static void resetSelect(AttributeFormContext aContext, String anAttName, Wrapper anAttributed,
			OptionModel<?> aNewOptions) {
        try {
			SelectField theField = (SelectField) aContext.getFirstMemberRecursively(anAttName, anAttributed);
			OptionModel<?> theOptions = theField.getOptionModel();
			List theValues  = (List) theField.getValue();
			if (aNewOptions != null) {
				theOptions = aNewOptions;
			}
			else {
				TLStructuredTypePart ma = anAttributed.tType().getPart(anAttName);
				if (ma == null) {
					Logger.error("Failed to reset select for " + anAttName + " of " + anAttributed
						+ ": attribute does not exist.", DefaultAttributeFormFactory.class);
					return;
				}
				theOptions = getOptionList(aContext.getAttributeUpdateContainer().getAttributeUpdate(ma, anAttributed),
					MetaLabelProvider.INSTANCE, theField);
			}

			theField.setOptionModel(theOptions);

			HashSet<Object> options = CollectionUtil.toSet(theOptions.iterator());
			List theNewValues   = new ArrayList();
			Iterator theOldVals = theValues.iterator();
			while (theOldVals.hasNext()) {
				Object theVal = theOldVals.next();
				if (options.contains(theVal)) {
					theNewValues.add(theVal);
				}
			}

			theField.setValue(theNewValues);
		} catch (Exception e) {
			Logger.error("Failed to reset select for " + anAttName + " of " + anAttributed, e, DefaultAttributeFormFactory.class);
		}
    }

	protected void initFormField(AttributeUpdate aAttributeUpdate, FormField result) {
		initFormMember(aAttributeUpdate, result);

		result.setExampleValue(createExampleValue(aAttributeUpdate));
	}

	protected void initFormMember(AttributeUpdate aAttributeUpdate, FormMember result) {
		ResKey resKey = aAttributeUpdate.getLabelKey();
		result.setLabel(resKey);
		result.setTooltip(resKey.tooltipOptional());
		result.setTooltipCaption(resKey.suffix(FormMember.TOOLTIP_CAPTION_SUFFIX).optional());
		
		if (AttributeOperations.isReadOnly(aAttributeUpdate.getAttribute())) {
			/* The value for the attribute can not be updated. Therefore a constraint is not useful,
			 * because the user can not change the value to fulfil the constraint. */
			result.setMandatory(false);
			result.clearConstraints();
		}
		AttributeFormFactory.setAttributeUpdate(result, aAttributeUpdate);
	}

	protected Object createExampleValue(AttributeUpdate aAttributeUpdate) {
		TLStructuredTypePart aMetaAttribute = aAttributeUpdate.getAttribute();

		switch (AttributeOperations.getMetaAttributeType(aMetaAttribute)) {
			case LegacyTypeCodes.TYPE_DATE: {
				if (aAttributeUpdate.isMultiple()) {
					GregorianCalendar calendar = new GregorianCalendar();
					Date a = calendar.getTime();
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					Date b = calendar.getTime();
					return Arrays.asList(a, b);
				} else {
					return new Date();
				}
			}
			case LegacyTypeCodes.TYPE_LONG: {
				if (aAttributeUpdate.isMultiple()) {
					return Arrays.asList(clipLong(aMetaAttribute, 1), clipLong(aMetaAttribute, 1234));
				} else {
					return clipLong(aMetaAttribute, 1234);
				}
			}
			case LegacyTypeCodes.TYPE_FLOAT: {
				if (aAttributeUpdate.isMultiple()) {
					return Arrays.asList(clipFloat(aMetaAttribute, 1.3f), clipFloat(aMetaAttribute, 1234.56f));
				} else {
					return clipFloat(aMetaAttribute, 1234.56f);
				}
			}

			default: {
				return null;
			}
		}
	}

	private static Object clipFloat(TLStructuredTypePart aMetaAttribute, float value) {
		Number minimum = AttributeOperations.getMinimum(aMetaAttribute);
		if (minimum != null && value < Utils.getfloatValue(minimum)) {
			value = Utils.getfloatValue(minimum);
		}
		Number maximum = AttributeOperations.getMaximum(aMetaAttribute);
		if (maximum != null && value > Utils.getfloatValue(maximum)) {
			value = Utils.getfloatValue(maximum);
		}
		return Float.valueOf(value);
	}

	private static Object clipLong(TLStructuredTypePart aMetaAttribute, long value) {
		Number minimum = AttributeOperations.getMinimum(aMetaAttribute);
		if (minimum != null && value < Utils.getlongValue(minimum)) {
			value = Utils.getlongValue(minimum);
		}
		Number maximum = AttributeOperations.getMaximum(aMetaAttribute);
		if (maximum != null && value > Utils.getlongValue(maximum)) {
			value = Utils.getlongValue(maximum);
		}
		return Long.valueOf(value);
	}

	/**
	 * {@link ControlProvider} displaying a {@link FormField} using a {@link PDFRenderer}.
	 */
	private static final class PDFControlProvider extends AbstractFormFieldControlProvider {
		/**
		 * {@link Control} displaying a {@link FormField} in a PDF export using a
		 * {@link PDFRenderer}.
		 */
		private static final class PDFFieldControl extends AbstractVisibleControl {
			private final FormField _field;

			private PDFRenderer _renderer;

			/** 
			 * Creates a {@link PDFFieldControl}.
			 */
			private PDFFieldControl(FormField field, PDFRenderer renderer) {
				_field = field;
				_renderer = renderer;
			}

			@Override
			public FormMember getModel() {
				return _field;
			}

			@Override
			protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
				AttributeUpdate fieldUpdate = AttributeFormFactory.getAttributeUpdate(_field);
				Object value = _field.getValue();
				_renderer.write(context, out, fieldUpdate.getObject(), value);
			}
		}

		private final PDFRenderer _renderer;

		/**
		 * Creates a {@link PDFControlProvider}.
		 */
		private PDFControlProvider(PDFRenderer renderer) {
			_renderer = renderer;
		}

		@Override
		protected Control createInput(FormMember member) {
			FormField field = (FormField) member;

			return new PDFFieldControl(field, _renderer);
		}
	}

	public static class LazyFormContext implements FormContextProxy {
		FormContextProxy proxy;

		public LazyFormContext(FormContextProxy aProxy) {
			this.proxy = aProxy;
		}

		@Override
		public FormContext getFormContext() {
			return this.proxy.getFormContext();
		}
	}

	public static void configureWithMetaElement(TableConfiguration table, Set<? extends TLClass> types) {
		GenericTableConfigurationProvider tableConfigurator = new GenericTableConfigurationProvider(types);
		tableConfigurator.adaptConfigurationTo(table);
	}
    
}
