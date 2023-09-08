/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import java.awt.Color;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.MappingSorter;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.NumbersOnlyConstraint;
import com.top_logic.layout.form.constraints.SequenceDependency;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.reporting.layout.provider.ResourcedMetaLabelProvider;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.CountFunction;
import com.top_logic.reporting.report.model.aggregation.CountUniqueFunction;
import com.top_logic.reporting.report.model.aggregation.NeedsAttribute;
import com.top_logic.reporting.report.model.aggregation.SupportsType;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.model.partition.TimeRangeFactory;
import com.top_logic.reporting.report.model.partition.function.ClassificationPartitionFunction;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction;
import com.top_logic.reporting.report.model.partition.function.DatePartitionFunction.DatePartitionConfiguration;
import com.top_logic.reporting.report.model.partition.function.NumberPartitionFunction;
import com.top_logic.reporting.report.model.partition.function.NumberPartitionFunction.NumberPartitionConfiguration;
import com.top_logic.reporting.report.model.partition.function.PartitionFunctionFactory;
import com.top_logic.reporting.report.model.partition.function.SamePartitionFunction;
import com.top_logic.reporting.report.model.partition.function.StringPartitionFunction;
import com.top_logic.reporting.report.util.Icons;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.resource.ResourceMapMapping;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class ChartConfigurationFieldHelper extends ConfigurationFormFieldHelper {

    /**
     * Interval options for {@link DatePartitionConfiguration#getSubIntervalLength()},
     * depends on selected {@link DatePartitionConfiguration#getDateRange()}
     */
    @SuppressWarnings("unchecked")
	private static final Map DATE_SUBINTERVAL_OPTIONS = new MapBuilder().put(
            DatePartitionFunction.DATE_RANGE_AUTOMATIC,
            new ListBuilder().add(TimeRangeFactory.WEEK_INT) // no day range, might cause a
                                                            // out of memory in JFreeChart
                            .add(TimeRangeFactory.MONTH_INT)
                            .add(TimeRangeFactory.QUARTER_INT)
                            .add(TimeRangeFactory.HALFYEAR_INT)
                            .add(TimeRangeFactory.YEAR_INT).toList()).put(
            DatePartitionFunction.DATE_RANGE_DAY,
            new ListBuilder().add(TimeRangeFactory.HOUR_INT)
                             .add(TimeRangeFactory.DAY_INT).toList()).put(
            DatePartitionFunction.DATE_RANGE_WEEK,
            new ListBuilder().add(TimeRangeFactory.DAY_INT)
                            .add(TimeRangeFactory.WEEK_INT).toList()).put(
            DatePartitionFunction.DATE_RANGE_MONTH,
            new ListBuilder().add(TimeRangeFactory.DAY_INT)
                            .add(TimeRangeFactory.WEEK_INT)
                            .add(TimeRangeFactory.MONTH_INT).toList()).put(
            DatePartitionFunction.DATE_RANGE_QUARTER,
            new ListBuilder().add(TimeRangeFactory.DAY_INT)
                            .add(TimeRangeFactory.WEEK_INT)
                            .add(TimeRangeFactory.MONTH_INT)
                            .add(TimeRangeFactory.QUARTER_INT).toList()).put(
            DatePartitionFunction.DATE_RANGE_HALFYEAR,
            new ListBuilder().add(TimeRangeFactory.WEEK_INT) // no day range, might cause a
                            // out of memory in JFreeChart
                            .add(TimeRangeFactory.MONTH_INT)
                            .add(TimeRangeFactory.QUARTER_INT)
                            .add(TimeRangeFactory.HALFYEAR_INT).toList()).put(
            DatePartitionFunction.DATE_RANGE_YEAR,
            new ListBuilder().add(TimeRangeFactory.WEEK_INT) // no day range, might cause a
                                                            // out of memory in JFreeChart
                            .add(TimeRangeFactory.MONTH_INT)
                            .add(TimeRangeFactory.QUARTER_INT)
                            .add(TimeRangeFactory.HALFYEAR_INT)
                            .add(TimeRangeFactory.YEAR_INT).toList()).put(
            DatePartitionFunction.DATE_RANGE_MANUAL,
            new ListBuilder().add(TimeRangeFactory.HOUR_INT)
                            .add(TimeRangeFactory.DAY_INT)
                            .add(TimeRangeFactory.WEEK_INT)
                            .add(TimeRangeFactory.MONTH_INT)
                            .add(TimeRangeFactory.QUARTER_INT)
                            .add(TimeRangeFactory.HALFYEAR_INT)
                            .add(TimeRangeFactory.YEAR_INT).toList()).toMap();

    /** meta element used to fill attribute selection */
    private final TLClass metaElement;
    /** excluded attributes from selection */
    private final Map<String, TLStructuredTypePart> filteredMetaAttributesByName;
    
    /**
     * Creates a {@link ChartConfigurationFieldHelper}.
     */
    public ChartConfigurationFieldHelper(TLClass aMeta, Set<String> excludeAttributes) {
        this.metaElement = aMeta;
        
        this.filteredMetaAttributesByName = new HashMap<>();
		List<TLStructuredTypePart> theAttrs = new ArrayList<>(TLModelUtil.getMetaAttributes(metaElement));
        for (Iterator<TLStructuredTypePart> theIter = theAttrs.iterator(); theIter.hasNext(); ) {
            TLStructuredTypePart theMA   = theIter.next();
            String        theName = theMA.getName();
            
            if (! excludeAttributes.contains(theName)) {
                this.filteredMetaAttributesByName.put(theName, theMA);
            }
        }
    }
    
    @Override
	protected void initListValue(FormGroup aTableGroup, PropertyDescriptor aDesc, ConfigBuilder aConfigBuilder) {
        // single selection for partitions
//        if (ReportConfiguration.PARTITION_CONFIGURATION_PROPERTY_NAME.getName().equals(aDesc.getConfigurationName())) {
//            ConfigurationItem theItem = extractSelectedConfigurationItem(aTableGroup, aDesc, aConfigBuilder);
//            if (theItem != null) {
//                aConfigBuilder.initValue(aDesc, Collections.singletonList(theItem));
//                return;
//            }
//        }
        super.initListValue(aTableGroup, aDesc, aConfigBuilder);
    }

    @Override
	protected FormGroup createItemGroup(ConfigurationItem anItem, ResourceView aResource, PropertyDescriptor aProperty, String aGroupname) {

        Class theIFace = aProperty.getDescriptor().getConfigurationInterface();

        if (PartitionFunctionConfiguration.class.isAssignableFrom(theIFace)) {
            return null;
        }

        FormGroup theGroup = super.createItemGroup(anItem, aResource, aProperty, aGroupname);
        if (ReportConfiguration.AGGREGATION_CONFIGURATIONS_NAME.equals(aProperty.getPropertyName())) {

            // the set of attribute options depends on the selected function
            SelectField theSelect = (SelectField) theGroup.getField(SELECT_FIELD);
            // save the current selection before the possible options are recalculated
            List<Class<?>> theCurrentSelection   = theSelect.getSelection();
            List<Class<?>> theAggregationOptions = this.getAggregationFunctionOptions(aProperty);
            
            // sort the aggregation functions using their translated names
			Map<Class<?>, ResKey> theMapping = new HashMap<>();
            
            for (Class<?> c:theAggregationOptions) {
				ResKey className = extractKey(c);
            	theMapping.put(c, className);
            }
            List<Class<?>>     theList = new ArrayList<>(theMapping.keySet());
            ResourceMapMapping<Class<?>> rmm     = new ResourceMapMapping<>(theMapping);
            Comparator<Object>      comp    = Collator.getInstance(TLContext.getLocale());

            MappingSorter.sortByMappingInline(theList, rmm, comp);
			theSelect.setOptions(theList);
            // reset the old selection
            // TODO : what happens if the old selection is not possible anymore?
            theSelect.setAsSelection(theCurrentSelection);

            final Set<FormField> theAttributeFields = new HashSet<>();
            FormGroup theContainer = (FormGroup) theGroup.getMember(GROUP_CONTAINER);
            Iterator<? extends FormMember> theIter = theContainer.getMembers();
            while (theIter.hasNext()) {
                FormGroup theAggregationGroup = (FormGroup) theIter.next();
                SelectField theField = (SelectField) theAggregationGroup.getMember(AggregationFunctionConfiguration.ATTRIBUTE_PATH_NAME);
                theAttributeFields.add(theField);
            }

            ValueListener theListener = new ValueListener() {
                @Override
				public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                    Class<?> theClass = (Class<?>) CollectionUtil.getFirst((Collection) aNewValue);
                    
                    for (Iterator theIter = theAttributeFields.iterator(); theIter.hasNext(); ) {
                        SelectField theField = (SelectField) theIter.next();
                        if (theClass != null) {
                            String theSelection         = (String) theField.getSingleSelection();
                            List   theOptions           = getMetaAttributeOptionsForAggregation(theClass, filteredMetaAttributesByName);
                            NeedsAttribute  attributeAnnotation = theClass.getAnnotation(NeedsAttribute.class);
                            theField.setMandatory(attributeAnnotation.value());
                            
                            theField.setOptions(theOptions);
                            if (theOptions.contains(theSelection)) {
                                theField.setAsSingleSelection(theSelection);
                            }
                            else {
                                theField.setAsSingleSelection(CollectionUtil.getFirst(theOptions));
                            }
                        }
                        else {
                            theField.reset();
                        }
                    }
                }
            };
            FormFieldHelper.initDependency(theSelect, theListener);
        }

        // single selection for partitions
        if (ReportConfiguration.PARTITION_CONFIGURATION_NAME.equals(aProperty.getPropertyName())) {
            SelectField theSelect = (SelectField) theGroup.getField(SELECT_FIELD);

            if (CollectionUtil.isEmptyOrNull(theSelect.getSelection())) {
                theSelect.setAsSingleSelection(CollectionUtil.getFirst(theSelect.getOptions()));
            }

            theSelect.setMandatory(true);
            return theGroup;
        }
        return theGroup;
    }

    @Override
	protected FormField createPlainValueField(ConfigurationItem anItem, PropertyDescriptor aProperty, String aFieldname) {

        Class<?> theIFace = aProperty.getDescriptor().getConfigurationInterface();
        String theName = aProperty.getPropertyName();

        if (ReportConfiguration.class.isAssignableFrom(theIFace)) {
            if (ReportConfiguration.CHART_TYPE_NAME.equals(theName)) {
                List<String> theTypes = this.getChartTypeOptions();
				SelectField theSelect = newCustomOptionOrderComparator(aFieldname, theTypes, anItem, aProperty);
                theSelect.setMandatory(true);
                
                theSelect.addValueListener(new ValueListener() {
					@Override
					public void valueChanged(FormField field, Object oldValue, Object newValue) {
						FormContext theContext = field.getFormContext();
						FormGroup theGroup = (FormGroup) theContext.getFirstMemberRecursively(TABLE_PREFIX + ReportConfiguration.AGGREGATION_CONFIGURATIONS_NAME);
						if (theGroup != null) {
							Iterator<? extends FormMember> theIter  = theGroup.getMembers();
							Object                         theFirst = CollectionUtil.getFirst(newValue);
							
                            if (ReportConstants.REPORT_TYPE_WATERFALL_CHART.equals(theFirst) || ReportConstants.REPORT_TYPE_PIE_CHART.equals(theFirst)) {
    							List<FormGroup> toRemove = new ArrayList<>();
    							boolean isFirst = true;
    							while (theIter.hasNext()) {
    								FormGroup theInnerGroup = (FormGroup) theIter.next();

    								if (isFirst) {
    									theInnerGroup.getFirstMemberRecursively(REMOVE_TABLE_ROW).setVisible(false);
    									FormMember theColor = theInnerGroup.getFirstMemberRecursively(AggregationFunctionConfiguration.COLOR_PROPERTY_NAME);
    									if (theColor != null) {
    										theColor.setVisible(false);
    									}
    									isFirst = false;
    								}
    								else {
    									if (theIter.hasNext()) {
    										toRemove.add(theInnerGroup);
    									}
    									else {
    										theInnerGroup.setVisible(false);
    									}
    								}
    							}
    							
    							for (FormGroup g : toRemove) {
    								theGroup.removeMember(g);
    							}
    						}
    						else {
    						    boolean isFirst = true;

    							while (theIter.hasNext()) {
    								FormGroup theInnerGroup = (FormGroup) theIter.next();
    								if (isFirst) {
    									theInnerGroup.getFirstMemberRecursively(REMOVE_TABLE_ROW).setVisible(true);
    									FormMember theColor = theInnerGroup.getFirstMemberRecursively(AggregationFunctionConfiguration.COLOR_PROPERTY_NAME);
    									if (theColor != null) {
    										theColor.setVisible(true);
    									}
    									isFirst = false;
    								}
    								theInnerGroup.setVisible(true);
    							}
    						}
						}
					}
				});

                return theSelect;
            }
            else if(ReportConfiguration.ATTRIBUTE_NAME.equals(theName)) {
				FormField theSelect = newMetaAttributeField(aFieldname, anItem, aProperty);
                theSelect.setMandatory(true);
                return theSelect;
            }
        }
        else if (AggregationFunctionConfiguration.class.isAssignableFrom(theIFace)) {
            if (AggregationFunctionConfiguration.ATTRIBUTE_PATH_NAME.equals(theName)) {
				return newMetaAttributeField(aFieldname, anItem, aProperty);
            }
        }
        else if (PartitionFunctionConfiguration.class.isAssignableFrom(theIFace)) {
            if(PartitionFunctionConfiguration.ATTRIBUTE_PROPERTY_NAME.equals(theName)) {
				return newMetaAttributeField(aFieldname, anItem, aProperty);
            }
        }

        if (DatePartitionConfiguration.class.isAssignableFrom(theIFace)) {
            if (DatePartitionConfiguration.DATE_RANGE_NAME.equals(theName)) {
                List<String> theOptions = this.getDateRangeOptions();
				SelectField theSelect = newCustomOptionOrderComparator(aFieldname, theOptions, anItem, aProperty);
                return theSelect;
            }
            else if (DatePartitionConfiguration.SUB_INTERVAL_LENGTH_NAME.equals(theName)) {
                List        theOptions = this.getDateSubIntervalLengthOptions(DatePartitionFunction.DATE_RANGE_MANUAL);
				SelectField theSelect = newCustomOptionOrderComparator(aFieldname, theOptions, anItem, aProperty);
                return theSelect;
            }
        }
        else if(NumberPartitionConfiguration.class.isAssignableFrom(theIFace)) {
//            if (NumberPartitionConfiguration..getName().equals(theName)) {
//                List        theOptions = this.getDateRangeOptions();
//                SelectField theSelect  = SelectField.newInstance(aFieldname, theOptions, false, CollectionUtil.getFirstElementsAsList(theOptions, 1), false);
//                theSelect.setOptionLabelProvider(new I18NResourceProvider("reporting.chart."));
//                return theSelect;
//            }
//            else if (NumberPartitionConfiguration.SUB_INTERVAL_LENGTH_PROPERTY_NAME.getName().equals(theName)) {
//                List        theOptions = this.getDateSubIntervalLengthOptions(DatePartitionFunction.DATE_RANGE_MANUAL);
//                SelectField theSelect  = SelectField.newInstance(aFieldname, theOptions, false, CollectionUtil.getFirstElementsAsList(theOptions, 1), false);
//                theSelect.setOptionLabelProvider(new I18NResourceProvider("reporting.chart."));
//                return theSelect;
//            }
        }

        return super.createPlainValueField(anItem, aProperty, aFieldname);
    }

	private SelectField newCustomOptionOrderComparator(String fieldName, List options, ConfigurationItem item,
			PropertyDescriptor property) {
		List initialSelection = this.getDefaultOrFirst(item, property, options);
		SelectField theSelect = FormFactory.newSelectField(fieldName, options, false, initialSelection, false);
		theSelect.setOptionLabelProvider(new I18NResourceProvider(I18NConstants.CHART));
		SelectFieldUtils.setCustomOrderComparator(theSelect);
		return theSelect;
	}

	private FormField newMetaAttributeField(String fieldName, ConfigurationItem item, PropertyDescriptor property) {
		List<String> maNames = this.getMetaAttributeOptionsForPartition();
		LabelProvider optionLabelProvider =
			new ResourcedMetaLabelProvider(DefaultResourceProvider.INSTANCE, this.metaElement, null);
		Comparator optionComparator = LabelComparator.newCachingInstance(optionLabelProvider);
		List initialSelection = this.getDefaultOrFirst(item, property, maNames, optionComparator);
		SelectField select = FormFactory.newSelectField(fieldName, maNames, false, initialSelection, false);
		select.setOptionComparator(optionComparator);
		select.setOptionLabelProvider(optionLabelProvider);
		return select;
	}

    protected List getDefaultOrFirst(ConfigurationItem anItem, PropertyDescriptor aProperty, List selectionOptions) {
		return getDefaultOrFirst(anItem, aProperty, selectionOptions, null);
    }

	protected List getDefaultOrFirst(ConfigurationItem anItem, PropertyDescriptor aProperty,
			List selectionOptions, Comparator comparator) {
		Object theDefault = anItem != null ? anItem.value(aProperty) : aProperty.getDefaultValue();

		if (theDefault != null && selectionOptions.contains(theDefault)) {
			return Collections.singletonList(theDefault);
		}
		if (selectionOptions.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.singletonList(getFirst(selectionOptions, comparator));
	}

	/**
	 * Returns the first object in the given collection according to the given comparator.
	 * 
	 * @param comparator
	 *        If <code>null</code>, the first element in the collection is returned.
	 */
	private static <T> T getFirst(Collection<T> col, Comparator<? super T> comparator) {
		int size = col.size();
		if (col instanceof RandomAccess) {
			List<T> l = (List<T>) col;
			T result = l.get(0);
			if (comparator == null || size == 1) {
				return result;
			}
			for (int i = 1; i < size; i++) {
				T next = l.get(i);
				if (comparator.compare(next, result) < 0) {
					result = next;
				}
			}
			return result;
		} else {
			Iterator<T> it = col.iterator();
			T result = it.next();
			if (comparator == null || size == 1) {
				return result;
			}
			while (it.hasNext()) {
				T next = it.next();
				if (comparator.compare(next, result) < 0) {
					result = next;
				}
			}
			return result;
		}
	}

    @Override
	protected void postProcessFormMember(FormMember aMember, PropertyDescriptor aProperty, String aPropertyName) {
        String theName = aProperty.getPropertyName();
        if (AggregationFunctionConfiguration.COLOR_PROPERTY_NAME.equals(theName)) {
            if (aMember instanceof FormField && ((FormField)aMember).getValue() == null) {
				((FormField) aMember).setValue(getGradientColor(getIdCount() % 5));
            }
        }
        if (PartitionFunctionConfiguration.class.isAssignableFrom(aProperty.getDescriptor().getConfigurationInterface())) {
        	if (PartitionFunctionConfiguration.ATTRIBUTE_PROPERTY_NAME.equals(theName)) {
            	aMember.setVisible(false);
        	}
        }
        super.postProcessFormMember(aMember, aProperty, aPropertyName);
    }

	/**
	 * Returns the color of the given gradient color number.
	 */
	public static Color getGradientColor(int gradientColor) {
		switch (gradientColor) {
			case 0:
				return ThemeFactory.getTheme().getValue(Icons.GRAD0);
			case 1:
				return ThemeFactory.getTheme().getValue(Icons.GRAD1);
			case 2:
				return ThemeFactory.getTheme().getValue(Icons.GRAD2);
			case 3:
				return ThemeFactory.getTheme().getValue(Icons.GRAD3);
			case 4:
				return ThemeFactory.getTheme().getValue(Icons.GRAD4);
			default:
				throw new RuntimeException("Found no theme color for gradient " + gradientColor + ".");
		}
	}

    @Override
	protected void postProcessFormGroup(FormGroup aGroup, ConfigurationDescriptor aConfigDesc) {
        Class<?> theIFace = aConfigDesc.getConfigurationInterface();

        if (ReportConfiguration.class.isAssignableFrom(theIFace)) {

            // copy selection of attribute to the partition function
            SelectField theAttributeSelect = (SelectField) aGroup.getField(ReportConfiguration.ATTRIBUTE_NAME);

            final Set theAttributeFields = new HashSet();
            FormGroup thePartitionGroup = (FormGroup) aGroup.getMember(ReportConfiguration.PARTITION_CONFIGURATION_NAME);
            FormGroup thePartitions     = (FormGroup) thePartitionGroup.getMember(GROUP_CONTAINER);
            Iterator<? extends FormMember>  theIter = thePartitions.getMembers();
            while (theIter.hasNext()) {
                FormGroup   thePartition = (FormGroup) theIter.next();
                SelectField theAttribute = (SelectField) thePartition.getField(PartitionFunctionConfiguration.ATTRIBUTE_PROPERTY_NAME);
//                theAttribute.setVisible(false);
                theAttributeFields.add(theAttribute);
            }
            //  copy selection of attribute to the partition function
            ValueListener theListener = new ValueListener() {
                @Override
				public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                    for (Iterator theIter = theAttributeFields.iterator(); theIter.hasNext();) {
                        SelectField theField = (SelectField) theIter.next();
                        theField.setAsSelection((List) aNewValue);
                    }
                }
            };
            FormFieldHelper.initDependency(theAttributeSelect, theListener);

            // the partition function selection depends on the selected attribute
            final SelectField thePartitionSelect = (SelectField) thePartitionGroup.getMember(SELECT_FIELD);
            theListener = new ValueListener() {
                @Override
				public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                    String theMeta  = (String) CollectionUtil.getFirst((Collection) aNewValue);

                    List   theOptions   = getPartitionOptionsForMetaAttribute(theMeta);
                    Object theSelection = thePartitionSelect.getSingleSelection();
                    thePartitionSelect.setOptions(theOptions);
                    if (! theOptions.contains(theSelection)) {
                        theSelection = CollectionUtil.getFirst(theOptions);
                    }
                    thePartitionSelect.setAsSingleSelection(theSelection);
                }
            };
            // trigger listeners
            FormFieldHelper.initDependency(theAttributeSelect, theListener);
            thePartitionSelect.setVisible(true);

            // the highlight options are only visibly if the boolean checkbox is activated
            Set<FormField> theHighlightFields = new HashSet<>();
            FormField fromField = aGroup.getField(ReportConfiguration.HIGHLIGHT_AREA_FROM_NAME);
            fromField.addConstraint(NumbersOnlyConstraint.INSTANCE);
			theHighlightFields.add(fromField);
            theHighlightFields.add(aGroup.getField(ReportConfiguration.HIGHLIGHT_AREA_LABEL_NAME));
            FormField toField = aGroup.getField(ReportConfiguration.HIGHLIGHT_AREA_TO_NAME);
            toField.addConstraint(NumbersOnlyConstraint.INSTANCE);
			theHighlightFields.add(toField);

            FormField theShowHighlightField = aGroup.getField(ReportConfiguration.SHOW_HIGHLIGHT_AREA_NAME);
            // trigger listeners
            FormFieldHelper.initDependency(theShowHighlightField, new ValueDependendVisibilityListener(theHighlightFields, Boolean.TRUE, false, true));
        }

        if (DatePartitionConfiguration.class.isAssignableFrom(theIFace)) {

            SelectField theSelect = (SelectField) aGroup.getField(DatePartitionConfiguration.DATE_RANGE_NAME);

            FormField theFrom = aGroup.getField(DatePartitionConfiguration.INTERVAL_START_NAME);
            FormField theEnd  = aGroup.getField(DatePartitionConfiguration.INTERVAL_END_NAME);

            final FormField[] theFields = new FormField[] { theFrom, theEnd };
            SequenceDependency theSeq = new SequenceDependency(theFields);
            theSeq.attach();

            FormFieldHelper.initDependency(theSelect, new ValueDependendVisibilityListener(Arrays.asList(theFields), Collections.singletonList(DatePartitionFunction.DATE_RANGE_MANUAL)));

            ValueListener theListener = new ValueListener() {
                @Override
				public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                    if (Utils.equals(Collections.singletonList(DatePartitionFunction.DATE_RANGE_AUTOMATIC), aNewValue)) {
                        for (int i=0; i<theFields.length; i++) {
                            theFields[i].setValue(null);
                        }
                    }
                }
            };
            FormFieldHelper.initDependency(theSelect, theListener);

            final SelectField theSubInterval = (SelectField) aGroup.getField(DatePartitionConfiguration.SUB_INTERVAL_LENGTH_NAME);
            theListener = new ValueListener() {
                @Override
				public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                    Object theSelection = theSubInterval.getSingleSelection();
                    List   theOptions   = getDateSubIntervalLengthOptions((String) CollectionUtil.getFirst((Collection) aNewValue));
                    theSubInterval.setOptions(theOptions);
                    if (theOptions.contains(theSelection)) {
                        theSubInterval.setAsSingleSelection(theSelection);
                    }
                    else {
                        theSubInterval.setAsSingleSelection(CollectionUtil.getFirst(theOptions));
                    }
                }
            };
            FormFieldHelper.initDependency(theSelect, theListener);
        }
        else if (NumberPartitionConfiguration.class.isAssignableFrom(theIFace)) {

            FormField theFrom = aGroup.getField(NumberPartitionConfiguration.INTERVAL_START_NAME);
            FormField theEnd  = aGroup.getField(NumberPartitionConfiguration.INTERVAL_END_NAME);

            FormField[] theFields = new FormField[] { theFrom, theEnd };
            SequenceDependency theSeq = new SequenceDependency(theFields);
            theSeq.attach();

            FormFieldHelper.initDependency(aGroup.getField(NumberPartitionConfiguration.USE_AUTOMATIC_RANGE_NAME), new ValueDependendVisibilityListener(Arrays.asList(theFields), Boolean.TRUE, true));
        }
    }

    protected List<String> getChartTypeOptions() {
        List<String> theList = new ArrayList();
        theList.add(ReportConstants.REPORT_TYPE_BAR_CHART);
        theList.add(ReportConstants.REPORT_TYPE_LINE_CHART);
//        theList.add(ReportConstants.REPORT_TYPE_BOXWHISKER_CHART);
        theList.add(ReportConstants.REPORT_TYPE_PIE_CHART);
        theList.add(ReportConstants.REPORT_TYPE_WATERFALL_CHART);
        return theList;
    }

    protected List<Class<?>> getAggregationFunctionOptions(PropertyDescriptor aProperty) {
		return new ArrayList<Class<?>>(PropertyDescriptorImpl.getImplementationClasses(aProperty));
    }

    /**
     * Return a list of metaelement names (keys) sorted alphabetically by their label (as shown on the GUI)
     */
    private static List<String> getMetaAttributeNamesSorted(Collection<TLStructuredTypePart> someAttributes) {
		Map<String, ResKey> theMapping = new HashMap<>();
        
        for (TLStructuredTypePart theAttr : someAttributes) {
            String theName     = theAttr.getName();
            ResKey theRealName = TLModelI18N.getI18NKey(theAttr);
            theMapping.put(theName, theRealName);
        }
        
        List<String>       theList = new ArrayList<>(theMapping.keySet());
        ResourceMapMapping<String> rmm  = new ResourceMapMapping<>(theMapping);
        Comparator<Object>      comp = Collator.getInstance(TLContext.getLocale());

        MappingSorter.sortByMappingInline(theList, rmm, comp);
        
        return theList;
    }
    
    protected List<String> getMetaAttributeNames() {
        return getMetaAttributeNamesSorted(this.filteredMetaAttributesByName.values());
    }

    protected List<String> getMetaAttributeOptionsForPartition() {
        return this.getMetaAttributeNames();
    }

    protected List<Class<?>> getPartitionOptionsForMetaAttribute(String aMetaAttribte) {
        TLStructuredTypePart  theMeta   = this.filteredMetaAttributesByName.get(aMetaAttribte);
        
        List<Class<?>> theResult    = new ArrayList<>(2);
        Set<Class<?>>  allFunctions = PartitionFunctionFactory.getInstance().getImplementationClasses(null);
        for (Class<?> theFunction : allFunctions) {
            SupportsType typeAnnotation = theFunction.getAnnotation(SupportsType.class);
            int[] supportedTypes = null;
            if (typeAnnotation != null) {
                supportedTypes = typeAnnotation.value();
                Arrays.sort(supportedTypes);
                if (supportedTypes.length == 0 || Arrays.binarySearch(supportedTypes, AttributeOperations.getMetaAttributeType(theMeta)) > -1) {
                    theResult.add(theFunction);
                }
            }
            else {
                Logger.warn("PartitionFunction "+ theFunction + " without " + SupportsType.class + " annotation found", ChartConfigurationFieldHelper.class);
            }
        }
        return theResult;
    }

    protected List<String> getMetaAttributeOptionsForAggregation(Class<?> aClass, Map<String, TLStructuredTypePart> someAttributes) {
        
        List<TLStructuredTypePart> theRest = new ArrayList<>();
        
        SupportsType  typeAnnotation = aClass.getAnnotation(SupportsType.class);
        int[] supportedTypes = null;
        if (typeAnnotation != null) {
            supportedTypes = typeAnnotation.value();
            if (supportedTypes == null || supportedTypes.length == 0) {
                supportedTypes = null;
            }
            else {
                Arrays.sort(supportedTypes);
            }
        }
        
        for (TLStructuredTypePart theMetaAttribute : someAttributes.values()) {
            int theType = AttributeOperations.getMetaAttributeType(theMetaAttribute);
            if (supportedTypes == null || Arrays.binarySearch(supportedTypes, theType) > -1) {
                theRest.add(theMetaAttribute);
            }
            
        }
        
        return getMetaAttributeNamesSorted(theRest);
    }

    protected List<String> getDateRangeOptions() {
        List<String> theList = new ArrayList<>();
        theList.add(DatePartitionFunction.DATE_RANGE_AUTOMATIC);
        theList.add(DatePartitionFunction.DATE_RANGE_WEEK);
        theList.add(DatePartitionFunction.DATE_RANGE_MONTH);
        theList.add(DatePartitionFunction.DATE_RANGE_QUARTER);
        theList.add(DatePartitionFunction.DATE_RANGE_HALFYEAR);
        theList.add(DatePartitionFunction.DATE_RANGE_YEAR);
        theList.add(DatePartitionFunction.DATE_RANGE_MANUAL);
        return theList;
    }

    protected List getDateSubIntervalLengthOptions(String aDateRange) {
        List theList = (List) DATE_SUBINTERVAL_OPTIONS.get(aDateRange);
        if (theList == null) {
            return Collections.EMPTY_LIST;
        }
        return theList;
    }

    protected List<?> getNumberRangeOptions() {
        return Collections.EMPTY_LIST;
    }

    public static class ValueDependendVisibilityListener implements ValueListener {

        private final Collection members;
        private final Object     value;
        private final boolean    negate;
		private final boolean    mandatory;

        public ValueDependendVisibilityListener(Collection someMembers, Object aValue) {
            this(someMembers, aValue, false);
        }

        public ValueDependendVisibilityListener(Collection someMembers, Object aValue, boolean negate) {
        	this(someMembers, aValue, negate, false);
        }
        public ValueDependendVisibilityListener(Collection someMembers, Object aValue, boolean negate, boolean mandatory) {
            this.members = someMembers;
            this.negate  = negate;
            this.value   = aValue;
            this.mandatory = mandatory;
        }

        @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {

            boolean isVisible = this.isVisible(aField, aOldValue, aNewValue) ^ this.negate;

            for (Iterator theIter = this.members.iterator(); theIter.hasNext(); )  {
                FormMember theMember = (FormMember) theIter.next();

                if (isVisible != theMember.isVisible()) {
                    // reset member to default
                    if (theMember instanceof FormField && shouldReset(aField, aOldValue, aNewValue)) {
                        ((FormField) theMember).reset();
                    }

                    // new visibility
                    theMember.setVisible(isVisible);

                    // if not visible, the field must not be mandatory.
                    if (! isVisible) {
                        theMember.setMandatory(false);
                    }
                    else {
                    	theMember.setMandatory(mandatory);
                    }
                }
            }
        }

        protected boolean isVisible(FormField aField, Object aOldValue, Object aNewValue) {
            return Utils.equals(this.value, aNewValue);
        }

        protected boolean shouldReset(FormField aField, Object aOldValue, Object aNewValue) {
            return true;
        }
    }
    
    private static class SupportedTypesFilter implements CollectionFilter {
        
        private final Class functionClass;
        
        SupportedTypesFilter(Class aFunctionClass) {
            this.functionClass = aFunctionClass;
        }
        
        @Override
		public Collection filter(Collection aCollection) throws NoSuchAttributeException, AttributeException {
            
            // function that can handle all types
            if (CountFunction.class         == this.functionClass ||
                CountUniqueFunction.class   == this.functionClass ||
                SamePartitionFunction.class == this.functionClass
                ) {
                return aCollection;
            }
            
            
            List theList = new ArrayList();
            for (Object theObject : aCollection) {
                TLStructuredTypePart theAttribute = (TLStructuredTypePart) theObject;
                int theType = AttributeOperations.getMetaAttributeType(theAttribute);
                
                switch (theType) {
                    case LegacyTypeCodes.TYPE_STRING:
                        if (this.functionClass == StringPartitionFunction.class) {
                            theList.add(theAttribute);
                            break;
                        }
                    case LegacyTypeCodes.TYPE_DATE:
                        if (this.functionClass == DatePartitionFunction.class) {
                            theList.add(theAttribute);
                            break;
                        }
                    case LegacyTypeCodes.TYPE_LONG:
                    case LegacyTypeCodes.TYPE_FLOAT:
                        if (this.functionClass == NumberPartitionFunction.class || AggregationFunction.class.isAssignableFrom(this.functionClass)) {
                            theList.add(theAttribute);
                            break;
                        }
                    case LegacyTypeCodes.TYPE_STRING_SET:
                        if (this.functionClass == StringPartitionFunction.class) {
                            theList.add(theAttribute);
                            break;
                        }
                    case LegacyTypeCodes.TYPE_CLASSIFICATION:
                        if (this.functionClass == ClassificationPartitionFunction.class) {
                            theList.add(theAttribute);
                            break;
                        }
                    default:
                        break;
                    }
            }
            
            return theList;
        }
        
        // useless methods
        
        @Override
		public int compareTo(Object aO) {
            return 0;
        }
        @Override
		public void setUpFromValueMap(Map aValueMap) throws IllegalArgumentException {
        }
        @Override
		public Map<String, Object> getValueMap() {
            return null;
        }
    }
}

