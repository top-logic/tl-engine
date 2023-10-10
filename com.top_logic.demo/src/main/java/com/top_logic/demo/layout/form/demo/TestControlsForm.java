/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import static com.top_logic.basic.DateUtil.*;
import static com.top_logic.basic.time.CalendarUtil.*;
import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.basic.fragments.Fragments.*;
import static com.top_logic.layout.basic.fragments.Fragments.table;
import static com.top_logic.layout.table.model.TableConfigurationFactory.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.charsize.ProportionalCharSizeMap;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.format.DoubleFormat;
import com.top_logic.basic.format.LongFormat;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.demo.chart.DemoChartProducer;
import com.top_logic.demo.model.annotations.DemoMarkerFactory;
import com.top_logic.element.i18n.I18NStringControlProvider;
import com.top_logic.element.i18n.I18NStringField;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.objects.label.ObjectLabel;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultIdentifierSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.IdentifierSource;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.KeyCode;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.KeySelector;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.MapAccessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.Resource;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlViewAdapter;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.basic.HTMLFragmentView;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.component.configuration.ExternalLink;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.folder.FolderControl;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.PlainKeyResources;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.component.SimpleFormComponent;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.constraints.DateRangeConstraint;
import com.top_logic.layout.form.constraints.ErrorConstraint;
import com.top_logic.layout.form.constraints.ExtendedStringLengthConstraint;
import com.top_logic.layout.form.constraints.IntRangeConstraint;
import com.top_logic.layout.form.constraints.NotEmptyConstraint;
import com.top_logic.layout.form.constraints.NumbersOnlyConstraint;
import com.top_logic.layout.form.constraints.RangeConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.CalendarControl;
import com.top_logic.layout.form.control.CalendarMarker;
import com.top_logic.layout.form.control.ColoredSelectControl;
import com.top_logic.layout.form.control.DataItemControl;
import com.top_logic.layout.form.control.DefaultCalendarMarker;
import com.top_logic.layout.form.control.DisplayImageControl;
import com.top_logic.layout.form.control.DownloadControl;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.FractionSelectControl;
import com.top_logic.layout.form.control.IntegerInputControl;
import com.top_logic.layout.form.control.MegaMenuControl;
import com.top_logic.layout.form.control.OpenCalendarControl.OpenCalendar;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.control.SelectionPartControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.control.TextPopupControl;
import com.top_logic.layout.form.control.ValueDisplayControl.ValueDisplay;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.format.ThemeImageFormat;
import com.top_logic.layout.form.model.AbstractFormMemberVisitor;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.ConstantField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.DateTimeField;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.model.DefaultPopupMenuField;
import com.top_logic.layout.form.model.ExecutableCommandField;
import com.top_logic.layout.form.model.ExpandableStringField;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.form.model.FormArray;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.ImageField;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.PopupMenuField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.SelectionTableField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.ValueVetoListener;
import com.top_logic.layout.form.selection.DefaultSelectDialogProvider;
import com.top_logic.layout.form.selection.SelectDialogConfig;
import com.top_logic.layout.form.selection.SelectDialogProvider;
import com.top_logic.layout.form.tag.TextInputTag;
import com.top_logic.layout.form.template.ButtonControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.ExpandableTextInputFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.TextInputControlProvider;
import com.top_logic.layout.form.util.FormFieldValueMapping;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.image.gallery.GalleryImage;
import com.top_logic.layout.image.gallery.ImageDataUtil;
import com.top_logic.layout.image.gallery.TransientGalleryImage;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.provider.BooleanLabelProvider;
import com.top_logic.layout.provider.DateTimeLabelProvider;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.resources.NestedResourceView;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.filter.FirstCharacterFilterProvider;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.DateFieldProvider;
import com.top_logic.layout.table.provider.StringFieldProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeNodeComparator;
import com.top_logic.layout.tree.model.UserObjectNodeComparator;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextFieldFactory;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextControlProvider;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextField;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.GenericSelectionModelOwner;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelFilter;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * Show / Test several examples of {@link FormMember}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestControlsForm extends FormComponent {

	private static final String COLUMN_EXTERNAL_LINK = "link";

	public interface Config extends FormComponent.Config {
		@Name(XML_CONF_FOLDER_ID)
		String getWebFolder();
	}

	/**
	 * Configuration key to configure the displayed webFolder.
	 */
	private static final String XML_CONF_FOLDER_ID = "webFolder";

	static abstract class ForEachMember {
		public HandlerResult execute(DisplayContext aContext, LayoutComponent component) {
			FormContext formContext = ((FormComponent) component).getFormContext();
			StringField switchStateField =
				(StringField) formContext.getContainer("configuration").getField("switchStateField");
			String configuredFields = switchStateField.getAsString().trim();
			if (configuredFields.isEmpty()) {
				String[] groupNames = new String[] { "controls", "dataFieldGroup", "controls.i18n" };
				for (int i = 0; i < groupNames.length; i++) {
					FormGroup controlsGroup = (FormGroup) FormGroup.getMemberByRelativeName(formContext, groupNames[i]);

					for (Iterator<? extends FormMember> it = controlsGroup.getMembers(); it.hasNext();) {
						doForMember(aContext, it.next());
					}
				}
			} else {
				HashSet<String> labels = new HashSet<>();
				Collections.addAll(labels, configuredFields.split("\\s*,\\s*"));
				for (Iterator<? extends FormMember> it = formContext.getDescendants(); it.hasNext();) {
					FormMember member = it.next();
					if (member.hasLabel()) {
						boolean contained = labels.remove(member.getLabel());
						if (contained) {
							doForMember(aContext, member);
						}
					}
				}
				if (!labels.isEmpty()) {
					return HandlerResult.error(ResKey.text("No field with label in: " + labels));
				}
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Subclass hook to modify some (inherited) member of the
		 * {@link FormContext} of the component this command is executed in.
		 * 
		 * @param context
		 *        the context in which command is executed
		 * @param member
		 *        the memebr to handle
		 */
		protected abstract void doForMember(DisplayContext context, FormMember member);
		
	}
	
	/**
	 * {@link CommandHandler} {@link FormField#check() checking} each {@link FormField} for errors.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class CheckAll extends AbstractCommandHandler {

		public CheckAll(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					if (member instanceof FormField) {
						((FormField) member).check();
					}
				}
			}.execute(aContext, aComponent);
		}
		
	}

	/**
	 * Action that toggles the mandatory property of all test fields. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ToggleMandatory extends AbstractCommandHandler {

		public ToggleMandatory(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					if (member instanceof FormField) {
						FormField field = (FormField) member;
						field.setMandatory(!field.isMandatory());
					}
				}
			}.execute(aContext, aComponent);
		}
	}
	
	/**
	 * Action that makes all test fields invisible. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SetInvisible extends ToggleCommandHandler {
		
		private static final Property<Boolean> HIDDEN_PROPERTY = TypedAnnotatable.property(Boolean.class, "hidden", Boolean.FALSE);
		
		public SetInvisible(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, final LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			HandlerResult result = super.handleCommand(aContext, aComponent, model, someArguments);
			if (!result.isSuccess()) {
				return result;
			}

			final boolean visible = !getState(aContext, aComponent);
			
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					member.setVisible(visible);
				}
			}.execute(aContext, aComponent);
		}
		
		@Override
		protected boolean getState(DisplayContext context, LayoutComponent component) {
			return component.get(HIDDEN_PROPERTY).booleanValue();
		}

		@Override
		protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
			component.set(HIDDEN_PROPERTY, Boolean.valueOf(newValue));
		}
	}
	
	/**
	 * Action that makes all test fields immutable. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SetImmutable extends AbstractCommandHandler {
		
		public SetImmutable(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					member.setVisible(true);
					member.setImmutable(true);
				}
			}.execute(aContext, aComponent);
		}
	}
	
	/**
	 * Action that blocks the values of all test fields. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ToggleBlocked extends AbstractCommandHandler {
		
		public ToggleBlocked(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					if (member instanceof FormField) {
						FormField field = (FormField) member;
						boolean block = !field.isBlocked();
						updateLabel(field, block, "Blocked ");
						field.setBlocked(block);
					}
				}
			}.execute(aContext, aComponent);
		}

	}
	
	/**
	 * Action that adds/removes a value veto listener to/from all test fields. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ToggleVeto extends AbstractCommandHandler {
		
		private static final ValueVetoListener VETO = new ValueVetoListener() {
			@Override
			public void checkVeto(FormField field, Object newValue) throws VetoException {
				throw new VetoException() {
					@Override
					public void process(WindowScope window) {
						MessageBox.confirm(window, MessageType.INFO, "Veto!", MessageBox.button(ButtonType.OK));
					}
				};
			}
		};
		
		public ToggleVeto(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					if (member instanceof FormField) {
						FormField field = (FormField) member;

						boolean added = field.addValueVetoListener(VETO);
						if (!added) {
							field.removeValueVetoListener(VETO);
						}
						updateLabel(field, added, "Veto ");
					}
				}
			}.execute(aContext, aComponent);
		}
	}
	
	/**
	 * Action that blocks the values of all test fields. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ToggleFrozen extends AbstractCommandHandler {
		
		public ToggleFrozen(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					if (member instanceof FormField) {
						FormField field = (FormField) member;
						boolean freeze = !field.isFrozen();
						updateLabel(field, freeze, "Frozen ");
						field.setFrozen(freeze);
					}
				}
			}.execute(aContext, aComponent);
		}
	}
	
	/**
	 * Action that disables all test fields. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SetDisabled extends AbstractCommandHandler {
		
		public SetDisabled(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					member.setVisible(true);
					member.setImmutable(false);
					member.setDisabled(true);
				}
			}.execute(aContext, aComponent);
		}
	}
	
	/**
	 * Action that makes all test fields editable. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SetEditable extends AbstractCommandHandler {
		
		public SetEditable(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			return new ForEachMember() {
				@Override
				protected void doForMember(DisplayContext context, FormMember member) {
					member.setVisible(true);
					member.setImmutable(false);
					member.setDisabled(false);
				}
			}.execute(aContext, aComponent);
		}
		
		@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			final ExecutabilityRule executabilityRule = super.createExecutabilityRule();
			
			return new ExecutabilityRule() {
				
				@Override
				public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
					if (((TestControlsForm) aComponent).banEditing) {
						return ExecutableState.createDisabledState(aComponent.getResPrefix().append(getID())
							.key(".disabled"));
					}
					return executabilityRule.isExecutable(aComponent, model, someValues);
				}
			};
		}
	}

	/**
	 * Action that resets all test fields to their default value. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ResetValues extends AbstractCommandHandler {

		public ResetValues(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			FormGroup controlsGroup = (FormGroup) ((FormComponent) component).getFormContext().getMember("controls");
			controlsGroup.reset();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Action that programatically changes values in all test fields. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ChangeValues extends AbstractCommandHandler {
		
		public ChangeValues(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			final TestControlsForm self = (TestControlsForm) component;
			FormGroup controlsGroup = (FormGroup) self.getFormContext().getMember("controls");
			
			for (Iterator<? extends FormMember> it = controlsGroup.getMembers(); it.hasNext(); ) {
				FormMember member = it.next();
				{
					member.visit(new AbstractFormMemberVisitor<Void, Void>() {
						@Override
						public Void visitBooleanField(BooleanField member, Void arg) {
							if (member.hasValue()) {
								Object currentValue = member.getValue();
								if (currentValue == null) {
									member.setValue(false);
								} else if (((Boolean) currentValue).booleanValue()) {
									member.setValue(null);
								} else {
									member.setValue(true);
								}
							} else {
								member.setValue(true);
							}
							return null;
						}

						@Override
						public Void visitComplexField(ComplexField member, Void arg) {
							Format format = member.getFormat();
							if (format instanceof DateFormat) {
								member.setValue(new Date());
								return null;
							} else if (format instanceof NumberFormat) {
								if (member.hasValue() && member.getValue() != null) {
									member.setValue(((Number) member.getValue()).intValue() + 1);
								} else {
									member.setValue(1);
								}
								return null;
							} else if (format instanceof LongFormat) {
								if (member.hasValue() && member.getValue() != null) {
									member.setValue(((Long) member.getValue()) + 1);
								} else {
									member.setValue(1);
								}
								return null;
							} else if (format instanceof DoubleFormat) {
								if (member.hasValue() && member.getValue() != null) {
									member.setValue(((Double) member.getValue()) + 1.0);
								} else {
									member.setValue(1.0);
								}
								return null;
							} else if (format instanceof ThemeImageFormat) {
								member.setValue(ThemeImage.forTest("css:fas fa-trash-alt"));

								return null;
							} else if (format instanceof ColorFormat) {
								if (member.hasValue() && member.getValue() != null) {
									Color currentColor = (Color) member.getValue();
									member.setValue(new Color(currentColor.getRGB() ^ 0xFFFFFF));
								} else {
									member.setValue(Color.RED);
								}
								return null;
							} else {
								throw new AssertionError("No update test for format '" + format.getClass().getName() + "'.");
							}
						}

						@Override
						public Void visitIntField(IntField member, Void arg) {
							if (member.hasValue() && member.getValue() != null) {
								member.setValue(((Number) member.getAsInteger()).intValue() + 1);
							} else {
								member.setValue(1);
							}
							return null;
						}

						@Override
						public Void visitSelectField(SelectField member, Void arg) {
							if (!member.isOptionsList()) {
								return null;
							}
							List options = member.getOptions();
							List currentSelection = member.getSelection();
							if (member.isMultiple()) {
								List newSelection;
								if (currentSelection == null) {
									newSelection = new ArrayList();
								} else {
									newSelection = new ArrayList(currentSelection);
								}
								int newIndex = newSelection.size();
								Object newElement;
								do {
									newElement = options.get(newIndex % options.size());
									newIndex++;
								} while (isFixedOption(member, newElement));
								if (newSelection.contains(newElement)) {
									newSelection.remove(newElement);
								} else {
									newSelection.add(newElement);
								}
								member.setValue(newSelection);
							} else {
								
								int currentIndex;
								if (member.hasValue() && currentSelection != null && currentSelection.size() > 0) {
									currentIndex = options.indexOf(currentSelection.get(0)) + 1;
								} else {
									currentIndex = 0;
								}

								if (options.size() > 0) {
									Object newElement;
									do {
										newElement = options.get(currentIndex % options.size());
										currentIndex++;
									} while (isFixedOption(member, newElement));

									member.setValue(Collections.singletonList(newElement));
								}
							}
							return null;
						}

						private boolean isFixedOption(SelectField member, Object newElement) {
							Filter fixedOptions = member.getFixedOptions();
							return fixedOptions != null && fixedOptions.accept(newElement);
						}

						@Override
						public Void visitStringField(StringField member, Void arg) {
							changeStringValue(member);
							return null;
						}

						private void changeStringValue(StringField member) {
							String currentValue = (String) member.getValue();
							if (member.hasValue() && currentValue != null && currentValue.length() > 0) {
								member.setValue(currentValue + "!");
							} else {
								member.setValue("Hello world");
							}
						}

						@Override
						public Void visitExpandableStringField(ExpandableStringField member, Void arg) {
							changeStringValue(member);
							member.setCollapsed(!member.isCollapsed());
							return null;
						}

						@Override
						public Void visitCommandField(CommandField member, Void arg) {
							return null;
						}

						@Override
						public Void visitDataField(DataField member, Void arg) {
							Object data = member.getValue();
							if (data == null) {
								data = Collections.singletonList(
									new DefaultDataItem(
										"hello-world.txt",
										BinaryDataFactory.createBinaryData("Hello world!".getBytes()),
										"text/plain"));
							} else {
								data = null;
							}
							member.setValue(data);
							return null;
						}

						@Override
						public Void visitImageField(ImageField member, Void arg) {
							ImageComponent image = member.getImageComponent();
							int id;
							if (image instanceof DemoChartProducer) {
								id = ((DemoChartProducer) image).getId() + 1;
							} else {
								id = 1;
							}
							member.setImageComponent(new DemoChartProducer(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE,
								id));
							return null;
						}

						@Override
						public Void visitListField(ListField member, Void arg) {
							DefaultListModel<String> listModel = (DefaultListModel<String>) member.getListModel();
							listModel.addElement("Entry " + (listModel.size() + 1));

							int index = member.getSelectionModel().getLeadSelectionIndex();
							if (index < 0) {
								index = 0;
							} else {
								index = index + 1;
							}
							member.getSelectionModel().setSelectionInterval(index, index);
							return null;
						}

						@Override
						public Void visitTableField(TableField member, Void arg) {
							ColumnCustomization columnCustomization =
								member.getTableModel().getTableConfiguration().getColumnCustomization();
							ResourceView containerResources = member.getParent().getResources();
							TableModel newApplicationModel =
								self.createApplicationModel(member.getName(), containerResources, columnCustomization,
									null);
							member.setTableModel(newApplicationModel);
							return null;
						}

						@Override
						public Void visitFolderField(FolderField member, Void arg) {
							// Nothing to change.
							return null;
						}

						@Override
						public Void visitFormContainer(FormContainer member, Void arg) {
							if (member instanceof DateTimeField) {
								DateTimeField dateTime = (DateTimeField) member;
								Calendar cal = CalendarUtil.createCalendar();
								if (dateTime.hasValue()) {
									Date oldValue = (Date) dateTime.getValue();
									if (oldValue != null) {
										cal.setTime(oldValue);
										cal.add(Calendar.DAY_OF_YEAR, 42);
										cal.add(Calendar.MINUTE, 42);
									}
								}
								cal.set(Calendar.SECOND, 0);
								cal.set(Calendar.MILLISECOND, 0);

								dateTime.setValue(cal.getTime());
							}
							return null;
						}

						@Override
						protected Void visitConstantField(ConstantField member, Void arg) {
							Object value = member.getValue();

							if (value instanceof StructuredText) {
								String sourceCode = ((StructuredText) value).getSourceCode();

								String newSourceCode = sourceCode + " Hello World!";
								member.setValue(new StructuredText(newSourceCode));
							}
							return null;
						}

						@Override
						public Void visitHiddenField(HiddenField member, Void arg) {
							// HiddenField is ConstantField
							return visitConstantField(member, arg);
						}

						@Override
						public Void visitPopupMenuField(PopupMenuField member, Void arg) {
							// Nothing to change.
							return null;
						}

						@Override
						public Void visitFormMember(FormMember member, Void arg) {
							throw new AssertionError("No update test for form member '" + member.getClass().getName() + "'.");
						}
					}, null);
				}
			}
			
			return HandlerResult.DEFAULT_RESULT;
		}
	}
	
	/**
	 * Accepts all strings containing a '7' character.
	 */
	private static final Filter FILTER_1 = new Filter() {
		@Override
		public boolean accept(Object anObject) {
			return ((String) anObject).indexOf('7') >= 0;
		}
	};
	private static final boolean FIXED = true;
	private static final boolean LARGE = true;
	private static final boolean MULTI = true;
	private static final boolean SMALL = ! LARGE;
	private static final boolean SINGLE = ! MULTI;
	private static final boolean CUSTOM_ORDER = true;

	protected static final Property<TestControlsForm> COMPONENT = TypedAnnotatable.property(TestControlsForm.class, "component");

	private static final Comparator<Object> NO_ORDER = new Comparator<>() {
		@Override
		public int compare(Object o1, Object o2) {
			return 0;
		}
	};
	
	private boolean banEditing;
	
	/**
	 * id of the {@link WebFolder} which is displayed by the {@link FolderField}
	 * 
	 * @see #addFolderFields(FormContainer)
	 */
	private String webFolderID;
	
	private static Constraint XML_FILENAME_CONSTRAINT = new AbstractConstraint() {
		
		@Override
		public boolean check(Object value) throws CheckException {
			if (value != null && !((String)value).endsWith(".xml")) {
				throw new CheckException(Resources.getInstance().getString(I18NConstants.ONLY));
			}
			return true;
		}
	};

	/**
	 * Create from XML.
	 */
	public TestControlsForm(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
        this.webFolderID = StringServices.nonEmpty(atts.getWebFolder());
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext(this);
		
		FormGroup controlsGroup = new FormGroup("controls", I18NConstants.CONTROLS);
		controlsGroup.setResources(PlainKeyResources.INSTANCE);
		context.addMember(controlsGroup);
        
		controlsGroup.addMember(createMailLinkContentField());

		RangeConstraint theConstraint = new RangeConstraint(-15, -5);
		final IntField theFirstField = FormFactory.newIntField("intControl", false, false, theConstraint);
        controlsGroup.addMember(theFirstField);
        
        IntField theField = FormFactory.newIntField("intControl2", false, false, theConstraint);
        theField.addValueListener(new ValueListener() {

            @Override
			public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
                theFirstField.setValue(aNewValue);
            }
            
        });
        controlsGroup.addMember(theField);
        
		final ComplexField doubleField =
			FormFactory.newDoubleField("double", null, /* immutable */false);
		controlsGroup.addMember(doubleField);

		final StringField doubleValue =
			FormFactory.newStringField("doubleValue", true);
		controlsGroup.addMember(doubleValue);

		doubleField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				doubleValue.setValue(
					String.valueOf(newValue) +
						(newValue == null ? "" : " (" + newValue.getClass().getName() + ")"));
			}
		});

		doubleField.setValue(Math.PI);

		final ComplexField longField =
			FormFactory.newLongField("long", null, /* immutable */false);
		controlsGroup.addMember(longField);

		final StringField longValue =
			FormFactory.newStringField("longValue", /* immutable */true);
		controlsGroup.addMember(longValue);

		longField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				longValue.setValue(
					String.valueOf(newValue) +
						(newValue == null ? "" : " (" + newValue.getClass().getName() + ")"));
			}
		});

		longField.setValue(42L);

        List<String> formats = Arrays.asList(new String[] { // See Javadoc for DecimalFormat
                "000,000.000", "###,###.###########", "###,###", "0.000###E00", "\u00A4 ###,###.##", 
                "% ###,###.##", "\u2030 ###,###.##",  "+###,##0.0##########;-###,##0.0##########"
        });
        final SelectField doubleFormat = FormFactory.newSelectField("doubleFormat", formats);
        final SelectField doubleParser = FormFactory.newSelectField("doubleParser", formats);
        final SelectField doubleFormatAndParser = FormFactory.newSelectField("doubleFormatAndParser", formats);
        
		final Format initialFormat = doubleField.getFormat();

        final BooleanFlag changing = new BooleanFlag(false);
        class FormatListener implements ValueListener {
            
            private final int changeType;
            
            public FormatListener(int changeType) {
				this.changeType = changeType;
			}

			@Override
			public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
				if (changing.get()) {
					return;
				}
                Object singleSelection = CollectionUtil.getSingleValueFrom(aNewValue);
				Format theFormat;
                if (singleSelection != null) {
                    theFormat = new DecimalFormat((String) singleSelection, new DecimalFormatSymbols(TLContext.getLocale())); 
                } else {
					theFormat = initialFormat;
                }
                
                ComplexField  targetField = doubleField;
                
                // Prevent recursive calls.
                changing.set(true);
                try {
                    switch (changeType) {
                    case 0: {
                    	targetField.setFormat(theFormat);
                    	doubleFormatAndParser.reset();
                    	break;
                    }
                    case 1: {
                    	targetField.setParser(theFormat);
                    	doubleFormatAndParser.reset();
                    	break;
                    }
                    case 2: {
                    	targetField.setFormatAndParser(theFormat);
                    	doubleFormat.setAsSingleSelection(singleSelection);
                    	doubleParser.setAsSingleSelection(singleSelection);
                    	break;
                    }
                    }
				} finally {
					changing.set(false);
				}
            }
        }
        
        doubleFormat.addValueListener(new FormatListener(0));
        doubleParser.addValueListener(new FormatListener(1));
        doubleFormatAndParser.addValueListener(new FormatListener(2));
        
        controlsGroup.addMember(doubleFormat);
        controlsGroup.addMember(doubleParser);
        controlsGroup.addMember(doubleFormatAndParser);
        
        controlsGroup.addMember(FormFactory.newBooleanField("checkboxControl"));
		controlsGroup.addMember(FormFactory.newBooleanField("tristateControl"));
		
		ResourceProvider errorImages = images(
			new MapBuilder<String, ThemeImage>()
				.put(null, Icons.DIALOG_QUESTION)
				.put("information", Icons.DIALOG_INFORMATION)
				.put("warning", Icons.DIALOG_WARNING)
				.put("error", Icons.DIALOG_ERROR)
				.toMap()
		);
		SelectField imageSelect = FormFactory.newSelectField("imageSelect", Arrays.asList("information", "warning", "error"), false, Collections.emptyList(), false, false, null);
		imageSelect.setOptionLabelProvider(errorImages);
		controlsGroup.addMember(imageSelect);
		
		SelectField imageSelectReset = FormFactory.newSelectField("imageSelectReset", Arrays.asList("information", "warning", "error"), false, Collections.emptyList(), false, false, null);
		imageSelectReset.setOptionLabelProvider(errorImages);
		controlsGroup.addMember(imageSelectReset);
		
		controlsGroup.addMember(FormFactory.newBooleanField("booleanChoiceControl"));
		
		final StringField member = FormFactory.newStringField("textInputControl");
		controlsGroup.addMember(member);
		controlsGroup.addMember(createTextInputWithPlaceholder());
		controlsGroup.addMember(createTextInputWithContextMenu());
		controlsGroup.addMember(StructuredTextFieldFactory.create("structuredText", new StructuredText()));
		addI18NGroup(controlsGroup);
		controlsGroup.addMember(createCodeEditorField());
		controlsGroup.addMember(FormFactory.newStringField("textInputControl2"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl3"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl4"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl5"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl6"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl7"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl8"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl9"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl10"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl11"));
		controlsGroup.addMember(FormFactory.newStringField("textInputControl12"));
		StringField popUpTextInput = FormFactory.newStringField("popUpTextInputControl");
		popUpTextInput.setControlProvider(TextPopupControl.CP.DEFAULT_INSTANCE);
		controlsGroup.addMember(popUpTextInput);
		controlsGroup.addMember(FormFactory.newExpandableStringField("expandableTextInputControl"));
		controlsGroup.addMember(FormFactory.newExpandableStringField("blockTextInputControl"));

		addTestKeyEventListener(controlsGroup);

		final StringField infoField = FormFactory.newStringField("infoControl");
		infoField.setTooltip("Lorem ipsum...");
		infoField.setTooltipCaption("Caption");
		controlsGroup.addMember(infoField);

		controlsGroup.addMember(createCalendarMarkerDemoField());
		controlsGroup.addMember(FormFactory.newDateField("dateInputControl2", new Date(), false));
		controlsGroup.addMember(FormFactory.newTimeField("timeInputControl", new Date(), false));
		
		addPopupControl(controlsGroup);

		addDateTimeFields(controlsGroup);

		controlsGroup.addMember(FormFactory.newComplexField("colorChooserControl", ColorFormat.INSTANCE));
		
		controlsGroup.addMember(FormFactory.newComplexField("iconChooserControl", ThemeImageFormat.INSTANCE));
		
		controlsGroup.addMember(FormFactory.newSelectField("beaconControl-selectField", abcdOptions()));
		controlsGroup.addMember(createBeaconComplexField());

		addListFields(controlsGroup);

		controlsGroup.addMember(FormFactory.newSelectField("choiceControl", abcdOptions()));
		controlsGroup.addMember(FormFactory.newSelectField("choiceControlMulti", abcdOptions(), true, false));
		controlsGroup.addMember(FormFactory.newSelectField("choiceControlVertical", abcdOptions()));
		controlsGroup.addMember(FormFactory.newSelectField("choiceControlMultiVertical", abcdOptions(), true, false));
		
		final SelectField fractionSelectField = FormFactory.newSelectField("fractionSelectControl", abcdOptions());
		fractionSelectField.setControlProvider(new DefaultFormFieldControlProvider() {

			@Override
			public Control visitSelectField(SelectField member, Void arg) {
				return new FractionSelectControl(member);
			}
		});
		controlsGroup.addMember(fractionSelectField);
		final SelectField coloredSelectControl = FormFactory.newSelectField("coloredSelectControl", abcdOptions());
		coloredSelectControl.setControlProvider(new DefaultFormFieldControlProvider() {
			
			@Override
			public Control visitSelectField(SelectField member, Void arg) {
				return new ColoredSelectControl(member);
			}
		});
		controlsGroup.addMember(coloredSelectControl);
		controlsGroup.addMember(FormFactory.newSelectField("selectControl", abcdOptions()));
		addSelectControlWithContextMenu(controlsGroup);
		controlsGroup.addMember(FormFactory.newSelectField("selectControlAsList", abcdOptions()));
		controlsGroup.addMember(FormFactory.newSelectField("selectControlAsListMandatory",
			abcdOptions(), false, true, false, null));
		controlsGroup.addMember(FormFactory.newSelectField("selectControlNotEmptyConstraint", abcdOptions(), false,
			false, false, NotEmptyConstraint.INSTANCE));
		controlsGroup.addMember(FormFactory.newSelectField("selectControlMulti", abcdOptions(), true, dabcSelection(),
			false));
		controlsGroup.addMember(FormFactory.newSelectField("selectControlMultiMandatory",
			abcdOptions(), true, true, false, null));

		controlsGroup.addMember(booleanSelectField("booleanSelectControl", false));
		controlsGroup.addMember(booleanSelectField("booleanSelectControlUndecided", true));

		controlsGroup.addMember(FormFactory.newSelectField("selectControlEmptyOptionList", Collections.emptyList()));
		SelectField singleInvalidSelectionSelectField =
			FormFactory.newSelectField("selectControlInvalidSelection",
				abcdOptions());
		singleInvalidSelectionSelectField.setAsSingleSelection("e");
		singleInvalidSelectionSelectField.setMandatory(true);
		controlsGroup.addMember(singleInvalidSelectionSelectField);
		SelectField multipleInvalidSelectionSelectField =
			FormFactory.newSelectField("selectControlMultiInvalidSelection",
				abcdOptions(), true, false);
		multipleInvalidSelectionSelectField.setAsSelection(Arrays.asList(new String[] { "ab", "e", "f" }));
		controlsGroup.addMember(multipleInvalidSelectionSelectField);

		SelectField dropDown = FormFactory.newSelectField("dropDown", getItemsWithTooltip());
		dropDown.setControlProvider((model, style) -> new DropDownControl((FormField) model));
		dropDown.setOptionComparator(LabelComparator.newCachingInstance());
		controlsGroup.addMember(dropDown);

		SelectField dropDownMulti = FormFactory.newSelectField("dropDownMulti", getItemsWithTooltip(), true, false);
		dropDownMulti.setControlProvider((model, style) -> new DropDownControl((FormField) model));
		dropDownMulti.setCustomOrder(false);
		dropDownMulti.setOptionComparator(LabelComparator.newCachingInstance());
		controlsGroup.addMember(dropDownMulti);

		addMegaMenus(controlsGroup);

		FormGroup selectionControlGroup = new FormGroup("selectionControlGroup", I18NConstants.CONTROLS);
		selectionControlGroup.setResources(PlainKeyResources.INSTANCE);
		addTestSelectionControl(selectionControlGroup);
		addTestSelectionControlFixed(selectionControlGroup);
		addTestSelectionControlFlipped(selectionControlGroup);
		addDelayedDblClickTestSelectionControl(selectionControlGroup);
		addTestSelectionControlTree(selectionControlGroup);
		addTestSelectionControlTreeAllExpanded(selectionControlGroup);
		addTestSelectionControlTreeCustomLabel(selectionControlGroup);
		addTestSelectionControlTreeFlipped(selectionControlGroup);
		addTestSelectionControlNoAutoComplete(selectionControlGroup);
		addTestSelectionControlMulti(selectionControlGroup);
		addTestSelectionControlMultiFlipped(selectionControlGroup);
		addTestSelectionControlTableSingle(selectionControlGroup);
		addTestSelectionControlTableMulti(selectionControlGroup);
		addTestSelectionControlTreeTableInfinite(selectionControlGroup);
		addTestSelectionControlTreeTableSingle(selectionControlGroup);
		addTestSelectionControlTreeTableMultiWithRootNode(selectionControlGroup);
		addTestSelectionControlTreeTableMultiWithoutRootNode(selectionControlGroup);
		addTestSelectionControlMultiTree(selectionControlGroup);
		addTestSelectionControlMultiTreeFlipped(selectionControlGroup);
		addTestSelectionControlMultiTreeUnordered(selectionControlGroup);
		addTestSelectionControlMultiTreeFixed(selectionControlGroup);
		addTestSelectionControlMultiCustomOrder(selectionControlGroup);
		addTestSelectionTextControlMultiFixed(selectionControlGroup);

		selectionControlGroup
			.addMember(createFixedOptionSelectField("selectionControlLarge", !FIXED, LARGE, SINGLE, !CUSTOM_ORDER));
		selectionControlGroup
			.addMember(createFixedOptionSelectField("selectionControlLargeMulti", !FIXED, LARGE, MULTI, !CUSTOM_ORDER));
		selectionControlGroup.addMember(
			createFixedOptionSelectField("selectionControlLargeMultiCustomOrder", !FIXED, LARGE, MULTI, CUSTOM_ORDER));
		selectionControlGroup
			.addMember(createFixedOptionSelectField("selectionControlFixedMulti", FIXED, SMALL, MULTI, !CUSTOM_ORDER));
		selectionControlGroup.addMember(
			createFixedOptionSelectField("selectionControlFixedMultiCustomOrder", FIXED, SMALL, MULTI, CUSTOM_ORDER));
		selectionControlGroup.addMember(
			createFixedOptionSelectField("selectionControlFixedLargeMulti", FIXED, LARGE, MULTI, !CUSTOM_ORDER));
		selectionControlGroup.addMember(createFixedOptionSelectField("selectionControlFixedLargeMultiCustomOrder",
			FIXED, LARGE, MULTI, CUSTOM_ORDER));
		
		selectionControlGroup.addMember(createFixedOptionSelectFieldWithFixedSelection(
			"selectionControlOnlyFixedSelectionSingle", !MULTI, !CUSTOM_ORDER));
		selectionControlGroup.addMember(createFixedOptionSelectFieldWithFixedSelection(
			"selectionControlOnlyFixedSelectionMulti", MULTI, !CUSTOM_ORDER));
		selectionControlGroup.addMember(createFixedOptionSelectFieldWithFixedSelection(
			"selectionControlOnlyFixedSelectionSingleCustomOrder", !MULTI, CUSTOM_ORDER));
		selectionControlGroup.addMember(createFixedOptionSelectFieldWithFixedSelection(
			"selectionControlOnlyFixedSelectionMultiCustomOrder", MULTI, CUSTOM_ORDER));
		
		SelectField s1 =
			FormFactory.newSelectField("invalidSelectionSingle", abcOptions(), false,
				Arrays.asList(new String[] { "d" }), false);
		selectionControlGroup.addMember(s1);
		
		SelectField s1Large =
			FormFactory.newSelectField("invalidSelectionSingleLarge", createOptionList(LARGE), false,
				Arrays.asList(new String[] { "d" }), false);
		selectionControlGroup.addMember(s1Large);
		
		SelectField s2 =
			FormFactory.newSelectField("invalidSelectionMulti", abcOptions(), true,
				Arrays.asList(new String[] { "d", "e", "f" }), false);
		selectionControlGroup.addMember(s2);
		
		SelectField s2Large =
			FormFactory.newSelectField("invalidSelectionMultiLarge", createOptionList(LARGE), true,
				Arrays.asList(new String[] { "d", "e", "f" }), false);
		selectionControlGroup.addMember(s2Large);
		
		SelectField s3 =
			FormFactory.newSelectField("invalidSelectionSingleFixedSelection",
				abcOptions(),
				false, Arrays.asList(new String[] { "d" }), false);
		s3.setFixedOptions(Arrays.asList(new String[] {"d"}));
		selectionControlGroup.addMember(s3);
		
		SelectField s3Large =
			FormFactory.newSelectField("invalidSelectionSingleFixedSelectionLarge", createOptionList(LARGE), false,
				Arrays.asList(new String[] { "d" }), false);
		s3Large.setFixedOptions(Arrays.asList(new String[] {"d"}));
		selectionControlGroup.addMember(s3Large);
		
		SelectField s4 =
			FormFactory.newSelectField("invalidSelectionSingleFixedOption", abcOptions(),
				false, Arrays.asList(new String[] { "d" }), false);
		s4.setFixedOptions(Arrays.asList(new String[] {"b"}));
		selectionControlGroup.addMember(s4);
		
		SelectField s4Large =
			FormFactory.newSelectField("invalidSelectionSingleFixedOptionLarge", createOptionList(LARGE), false,
				Arrays.asList(new String[] { "d" }), false);
		s4Large.setFixedOptions(Arrays.asList(new String[] {"2"}));
		selectionControlGroup.addMember(s4Large);
		
		SelectField s5 =
			FormFactory.newSelectField("invalidSelectionMultiFixed", abcOptions(), true,
				Arrays.asList(new String[] { "d", "e", "f" }), false);
		s5.setFixedOptions(Arrays.asList(new String[] {"b", "e"}));
		selectionControlGroup.addMember(s5);
		
		SelectField s5Large =
			FormFactory.newSelectField("invalidSelectionMultiFixedLarge", createOptionList(LARGE), true,
				Arrays.asList(new String[] { "d", "e", "f" }), false);
		s5Large.setFixedOptions(Arrays.asList(new String[] {"2", "e"}));
		selectionControlGroup.addMember(s5Large);
		
		SelectField s6 =
			FormFactory.newSelectField("invalidSelectionMultiFixedCustomOptionOrder", dabcSelection(), true,
				Arrays.asList(new String[] { "d", "e", "f" }), false);
		s6.setFixedOptions(Arrays.asList(new String[] { "b", "e" }));
		SelectFieldUtils.setCustomOrderComparator(s6);
		selectionControlGroup.addMember(s6);

		selectionControlGroup
			.addMember(FormFactory.newSelectField("selectionControlEmptyOptionList", Collections.emptyList()));

		controlsGroup.addMember(selectionControlGroup);

		controlsGroup.addMember(FormFactory.newSelectField("selectOptionControl", abcdOptions()));
		controlsGroup.addMember(FormFactory.newSelectField("selectOptionControlMulti", abcdOptions(), true, false));

		SelectField listenerSelect =
			FormFactory.newSelectField("longRunningListener", Arrays.asList(new Object[] { 1, 2, 3 }), false,
				Collections.singletonList(1), false);
		listenerSelect.setMandatory(true);
		controlsGroup.addMember(listenerSelect);
		
		
		controlsGroup.addMember(FormFactory.newDataField("dataField"));
		
		final FormGroup listenerGroup = new FormGroup("listenerGroup", I18NConstants.CONTROLS);
		controlsGroup.addMember(listenerGroup);
		
		listenerSelect.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				Iterator<? extends FormMember> it = listenerGroup.getMembers();
				if (it.hasNext()) {
					listenerGroup.removeMember(it.next());
				}

				Integer selection = (Integer) ((Collection) newValue).iterator().next();
				switch (selection.intValue()) {
				case 1: {
					break;
				}
				case 2: {
					listenerGroup.addMember(FormFactory.newStringField("tempString"));
					break;
				}
				case 3: {
					listenerGroup.addMember(FormFactory.newSelectField("tmpSelect", Arrays.asList(new Object[] {"a", "b", "c"}), false, null, false));
					break;
				}
				}
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
					// Ignore.
				}
			}
		});
		
//		FormGroup commandsGroup = new FormGroup("commands", "commands");
//		context.addMember(commandsGroup);
//		
		ResPrefix resPrefix = I18NConstants.CONTROLS;
		DeckField deckField = new DeckField("deckField", resPrefix);
		deckField.addMember(new FormGroup("tab1", resPrefix, new FormMember[] { FormFactory.newBooleanField("checkboxControl2") }));
		StringField justNumbersField = FormFactory.newStringField("justNumbers", false, false, NumbersOnlyConstraint.INSTANCE);
		deckField.addMember(new FormGroup("tab2", resPrefix, new FormMember[] { justNumbersField }));
		deckField.addMember(new FormGroup("tab3", resPrefix, new FormMember[] { FormFactory.newSelectField("selectControl2", abcdOptions()) }));
		controlsGroup.addMember(deckField);
		
		addPopupMenus(controlsGroup);

		final ExecutableCommandField command1 =
			(ExecutableCommandField) FormFactory.newCommandField("command1ToggleExecutablityCommand2", Command.DO_NOTHING);
		command1.setNotExecutableReasonKey(I18NConstants.NOT_EXECUTABLE_REASON);
		final CommandField command2 = FormFactory.newCommandField("command2ToggleExecutablityCommand1", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				command1.setDisabled(command1.isExecutable());
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		command2.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
		command2.setNotExecutableImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED);
		command1.setExecutable(new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				boolean otherExecutable = command2.isExecutable();
				if (otherExecutable) {
					command2.setNotExecutable(I18NConstants.NOT_EXECUTABLE_REASON);
				} else {
					command2.setExecutable();
				}
				return HandlerResult.DEFAULT_RESULT;
			}
			
		});
		final CommandField command3 = FormFactory.newCommandField("command3ToggleVisibilityOfCommand1And2", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				command2.setVisible(!command2.isVisible());
				command1.setVisible(!command1.isVisible());
				return HandlerResult.DEFAULT_RESULT;
			}
			
		});
		controlsGroup.addMember(command1);
		controlsGroup.addMember(command2);
		controlsGroup.addMember(command3);
		final CommandField command4 = new CommandField("command4ToggleExecutablitySetEditable") {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				TestControlsForm component = get(COMPONENT);
				component.banEditing = !component.banEditing;
				return HandlerResult.DEFAULT_RESULT;
			}
			
		};
		command4.set(COMPONENT, TestControlsForm.this);
		controlsGroup.addMember(command4);
		
		final CommandField command5 =
			FormFactory.newCommandField("alertWithLineBreak", createAlertCommand("ersteZeile\nzweiteZeile"));
		command5.set(COMPONENT, TestControlsForm.this);
		controlsGroup.addMember(command5);
		
		final String specials = "(special chars <'\"&\r\n>)";

		final CommandField command6 = FormFactory.newCommandField("failWithMultipleErrors", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				HandlerResult error = new HandlerResult();
				error.addErrorText("error1" + specials);
				error.addErrorText("error2" + specials);
				error.addErrorText("error3" + specials);
				return error;
			}
		});
		controlsGroup.addMember(command6);

		final CommandField command7 =
			FormFactory.newCommandField("failWithLegacyTopLogicExceptionNoI18N", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				throw new TopLogicException(TestControlsForm.class, "noI18N" + specials);
			}
		});
		command7.set(COMPONENT, TestControlsForm.this);
		controlsGroup.addMember(command7);

		{
			final CommandField command =
				FormFactory.newCommandField("failWithTopLogicExceptionInfo", new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext commandContext) {
						throw new TopLogicException(I18NConstants.TEST_ERROR_TITLE)
							.initDetails(I18NConstants.TEST_ERROR_DESCRIPTION)
							.initSeverity(ErrorSeverity.INFO);
					}
				});
			command.set(COMPONENT, TestControlsForm.this);
			controlsGroup.addMember(command);
		}

		{
			final CommandField command =
				FormFactory.newCommandField("failWithTopLogicExceptionWarning", new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext commandContext) {
						throw new TopLogicException(I18NConstants.TEST_ERROR_TITLE)
							.initDetails(I18NConstants.TEST_ERROR_DESCRIPTION)
							.initSeverity(ErrorSeverity.WARNING);
					}
				});
			command.set(COMPONENT, TestControlsForm.this);
			controlsGroup.addMember(command);
		}

		{
			final CommandField command =
				FormFactory.newCommandField("failWithTopLogicExceptionError", new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext commandContext) {
						throw new TopLogicException(I18NConstants.TEST_ERROR_TITLE)
							.initDetails(I18NConstants.TEST_ERROR_DESCRIPTION)
							.initSeverity(ErrorSeverity.ERROR);
					}
				});
			command.set(COMPONENT, TestControlsForm.this);
			controlsGroup.addMember(command);
		}

		{
			final CommandField command =
				FormFactory.newCommandField("failWithTopLogicExceptionSystemFailure", new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext commandContext) {
						throw new TopLogicException(I18NConstants.TEST_ERROR_TITLE)
							.initDetails(I18NConstants.TEST_ERROR_DESCRIPTION)
							.initSeverity(ErrorSeverity.SYSTEM_FAILURE);
					}
				});
			command.set(COMPONENT, TestControlsForm.this);
			controlsGroup.addMember(command);
		}

		final CommandField command8 = FormFactory.newCommandField("failWithFatalTopLogicException", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				throw new TopLogicException(I18NConstants.TEST_ERROR_TITLE__VALUE.fill(specials),
					new IllegalArgumentException("internal error" + specials));
			}
		});
		command8.set(COMPONENT, TestControlsForm.this);
		controlsGroup.addMember(command8);

		final CommandField command9 = FormFactory.newCommandField("failWithRuntimeException", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				throw new IllegalArgumentException("internal error" + specials);
			}
		});
		command9.set(COMPONENT, TestControlsForm.this);
		controlsGroup.addMember(command9);

		final CommandField command10 = FormFactory.newCommandField("failWithThrowable", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				throw new AssertionError("internal error" + specials);
			}
		});
		command10.set(COMPONENT, TestControlsForm.this);
		controlsGroup.addMember(command10);

		CommandField command11 = FormFactory.newCommandField("messageBoxLongRunningCommand", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				CommandModel delayedClose = MessageBox.button(ButtonType.CLOSE, new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException ex) {
							Logger.error("Arbitrary delay interrupted!", ex, TestControlsForm.class);
						}
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				delayedClose.setShowProgress();

				return MessageBox.confirm(getWindowScope(), MessageType.CONFIRM,
					"Dialog closal triggers a long running command, so that progress animation will be shown.",
					delayedClose);
			}
		});
		controlsGroup.addMember(command11);

		controlsGroup.addMember(createProgressCommand());
		controlsGroup.addMember(createProgressWithFinalizeCommand());
		controlsGroup.addMember(createLinearProgressCommand());
		createInfoServiceMessages(controlsGroup);
		createErrorDialogWindow(controlsGroup);
		controlsGroup.addMember(createIconControlTest());

		addDependencies(controlsGroup);
		
		controlsGroup.addMember(testForeachControl());
		controlsGroup.addMember(testLengthConstraint());
		controlsGroup.addMember(testTableField(controlsGroup.getResources()));
		controlsGroup.addMember(testGlobalSelectionTableField(controlsGroup.getResources()));
		controlsGroup.addMember(testTableListField(controlsGroup.getResources()));
		controlsGroup.addMember(testTableFieldNoColumnSelect(controlsGroup.getResources()));
		controlsGroup.addMember(testTableFieldNoColumnConfig(controlsGroup.getResources()));
		controlsGroup.addMember(testTableFieldEditable());

		controlsGroup.addMember(testSelectionTableField(controlsGroup.getResources()));
		controlsGroup.addMember(testSelectionTableFieldMandatory(controlsGroup.getResources()));

		FormGroup tablesGroup = new FormGroup("tables", PlainKeyResources.INSTANCE);
		controlsGroup.addMember(tablesGroup);

		addDataFields(context);
		addSelectionPartControlFieldsSingle(context);
		addSelectionPartControlFieldsMultiple(context);

		final Command onOk = createAlertCommand("Es wurde OK gedrückt.");
		final Command onCancel = createAlertCommand("Es wurde Cancel gedrückt.");
		final Command onYes = createAlertCommand("Es wurde Yes gedrückt.");
		final Command onNo = createAlertCommand("Es wurde No gedrückt.");
		final Command onContinue = createAlertCommand("Es wurde Continue gedrückt.");
		
		FormGroup messagesGroup = new FormGroup("messages", I18NConstants.MESSAGES);
		messagesGroup.setResources(PlainKeyResources.INSTANCE);
		context.addMember(messagesGroup);
		
		messagesGroup.addMember(FormFactory.newCommandField("infoMessage", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return MessageBox.confirm(context, MessageType.INFO, "Eine Information", 
					MessageBox.button(ButtonType.OK, onOk));
			}
		}));
		
		messagesGroup.addMember(FormFactory.newCommandField("confirmMessage", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return MessageBox.confirm(context, MessageType.CONFIRM, "Wollen Sie wirklich?", 
					MessageBox.button(ButtonType.YES, onYes),
					MessageBox.button(ButtonType.NO, onNo),
					MessageBox.button(ButtonType.CANCEL, onCancel));
			}
		}));
	
		messagesGroup.addMember(FormFactory.newCommandField("warnMessage", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return MessageBox.confirm(context, MessageType.WARNING, "Wollen Sie trotzdem weitermachen?", 
					MessageBox.button(ButtonType.CONTINUE, onContinue),
					MessageBox.button(ButtonType.CANCEL, onCancel));
			}
		}));
		
		messagesGroup.addMember(FormFactory.newCommandField("errorMessage", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return MessageBox.confirm(context, MessageType.ERROR, "Es ist ein Fehler aufgetreten!", 
					MessageBox.button(ButtonType.CANCEL, onCancel));
			}
		}));
		
		messagesGroup.addMember(FormFactory.newCommandField("dynamicDialog", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return new TestDynamicDialog().open(context);
			}
		}));
		
		addFolderFields(controlsGroup);
		
		addImageField(controlsGroup);

		context.addMember(testCSSClassChangeOnUserInput());

		try {
			showDisplayImageControl(controlsGroup);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		addButtonControls(controlsGroup);

		context.addMember(createTestFormGroup("collapsibleNoName"));
		context.addMember(createTestFormGroup("nonCollapsibleName"));
		context.addMember(createTestFormGroup("nonCollapsibleNoName"));

		context.addMember(createExternalLinkGroup());
		context.addMember(createConfigurationGroup());

		addOpenCalendarControl(controlsGroup);
		return context;
	}

	private void addMegaMenus(FormGroup parent) {
		ResPrefix resPrefix = getResPrefix();
		FormGroup megaMenu = new FormGroup("megaMenu", resPrefix);
		addEmptyMandatoryMegaMenu(megaMenu, resPrefix);
		addSmallMegaMenu(megaMenu, resPrefix);
		addBigMegaMenu(megaMenu, resPrefix);
		addBiggestMegaMenu(megaMenu, resPrefix);
		addMegaMenuWithFixedOption(megaMenu, resPrefix);
		addMegaMenuWithInvalidSelection(megaMenu, resPrefix);
		parent.addMember(megaMenu);
	}

	private CommandField createProgressCommand() {
		return FormFactory.newCommandField("progressCommand", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return new ProgressDialog(I18NConstants.PROGRESS_TITLE, px(400), px(300)) {
					@Override
					protected void run(I18NLog log) {
						for (int n = 0; n < 20; n++) {
							log.log(randomLevel(), I18NConstants.PROGRESS_MESSAGE__NUM.fill(n));

							try {
								Thread.sleep(500);
							} catch (InterruptedException ex) {
								// Ignore.
							}
						}
					}
				}.open(context);
			}
		});
	}

	private CommandField createProgressWithFinalizeCommand() {
		return FormFactory.newCommandField("progressWithFinalizeCommand", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				return new ProgressDialog(I18NConstants.PROGRESS_TITLE, px(400), px(300)) {
					@Override
					protected void run(I18NLog log) {
						for (int n = 0; n < 20; n++) {
							log.log(randomLevel(), I18NConstants.PROGRESS_MESSAGE__NUM.fill(n));

							try {
								Thread.sleep(500);
							} catch (InterruptedException ex) {
								// Ignore.
							}
						}
					}

					@Override
					protected void handleCompleted(DisplayContext context) {
						MessageBox.newBuilder(MessageType.INFO)
							.title("Success")
							.message("Background task has completed sucessfully.")
							.buttons(MessageBox.button(ButtonType.OK, getDiscardClosure()))
							.confirm(context);
					}
				}.open(commandContext);
			}
		});
	}

	private CommandField createLinearProgressCommand() {
		return FormFactory.newCommandField("progressWithLinearCompletionCommand", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				return new ProgressDialog(I18NConstants.PROGRESS_TITLE, px(400), px(300)) {
					@Override
					protected int getStepCnt() {
						return 200;
					}

					@Override
					protected void run(I18NLog log) {
						for (int n = 0; n < 20; n++) {
							log.log(randomLevel(), I18NConstants.PROGRESS_MESSAGE__NUM.fill(n));

							try {
								Thread.sleep(500);
							} catch (InterruptedException ex) {
								// Ignore.
							}

							setProgress(n * 10);
						}
					}

				}.open(context);
			}
		});
	}

	final Level randomLevel() {
		Level logLevel = Level.values()[(int) (Math.random() * Level.values().length)];
		return logLevel;
	}

	private void createInfoServiceMessages(FormGroup controlsGroup) {
		controlsGroup.addMember(createInfoServiceErrorMessage());
		controlsGroup.addMember(createInfoServiceWarningMessage());
		controlsGroup.addMember(createInfoServiceInfoMessage());
	}

	private void createErrorDialogWindow(FormGroup controlsGroup) {
		controlsGroup.addMember(createFormFieldWithWarning());
		controlsGroup.addMember(createFormFieldWithError());
		controlsGroup.addMember(openCheckFormForErrorsDialog());
	}

	private FormMember createFormFieldWithWarning() {
		StringField stringField = FormFactory.newStringField("DialogWindowWarningMessage");
		stringField.addWarningConstraint(new ErrorConstraint("Always Warning"));
		return stringField;
	}

	private FormMember createFormFieldWithError() {
		StringField stringField = FormFactory.newStringField("DialogWindowErrorMessage");
		stringField.addConstraint(new ErrorConstraint("Always Error"));
		return stringField;
	}

	private FormMember openCheckFormForErrorsDialog() {
		return FormFactory.newCommandField("ThrowErrorDialogWindow",
			(Command) displayContext -> {
				FormContext formContext = getFormContext();
				if (!formContext.checkAll()) {
					return AbstractApplyCommandHandler.createErrorResult(formContext);
				}
				return HandlerResult.DEFAULT_RESULT;
			});
	}

	private FormMember createInfoServiceErrorMessage() {
		return FormFactory.newCommandField("InfoServiceErrorWithDetails",
			(Command) displayContext -> {
				InfoService.showError(I18NConstants.TEST_ERROR_TITLE, I18NConstants.TEST_ERROR_DESCRIPTION);
				return HandlerResult.DEFAULT_RESULT;
			});
	}

	private FormMember createInfoServiceWarningMessage() {
		return FormFactory.newCommandField("InfoServiceWarning",
			(Command) displayContext -> {
				InfoService.showWarning(Fragments.text("Info Service Warning"));
				return HandlerResult.DEFAULT_RESULT;
			});
	}

	private FormMember createInfoServiceInfoMessage() {
		return FormFactory.newCommandField("InfoServiceInfo",
			(Command) displayContext -> {
				InfoService.showInfo(Fragments.text("Info Service Info"));
				return HandlerResult.DEFAULT_RESULT;
			});
	}

	private void addI18NGroup(FormGroup controlsGroup) {
		FormGroup group = new FormGroup("i18n", controlsGroup.getResources());

		Constraint noConstraint = null;
		FormField stringField = I18NStringField.newI18NStringField("i18nString", !FormFactory.MANDATORY,
			!FormFactory.IMMUTABLE, false, noConstraint);
		stringField.setControlProvider(new I18NStringControlProvider(false, 0, TextInputControl.NO_COLUMNS));
		group.addMember(stringField);

		FormField stringFieldMulti = I18NStringField.newI18NStringField("i18nStringMultiLine", !FormFactory.MANDATORY,
			!FormFactory.IMMUTABLE, true, noConstraint);
		stringFieldMulti.setControlProvider(
			new I18NStringControlProvider(true, MultiLineText.DEFAULT_ROWS, TextInputControl.NO_COLUMNS));
		group.addMember(stringFieldMulti);

		List<String> defaultFeatureSet = null;
		FormField structuredTextField =
			I18NStructuredTextField.new18NStructuredTextField("i18nHTML", !FormFactory.MANDATORY, false, noConstraint, defaultFeatureSet);
		structuredTextField.setControlProvider(I18NStructuredTextControlProvider.INSTANCE);
		group.addMember(structuredTextField);

		controlsGroup.addMember(group);
	}

	protected void addSelectControlWithContextMenu(FormGroup controlsGroup) {
		SelectField field = FormFactory.newSelectField("selectControlOptionContextMenu", abcdOptions());
		ContextMenuProvider contextMenu = new ContextMenuProvider() {
			@Override
			public boolean hasContextMenu(Object obj) {
				return true;
			}

			@Override
			public Menu getContextMenu(Object obj) {
				Command command = new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						InfoService.showInfo(Fragments.text("Option: " + obj));
						return HandlerResult.DEFAULT_RESULT;
					}
				};
				CommandModel button = CommandModelFactory.commandModel(command);
				button.setLabel("Show context info.");
				return Menu.create(button);
			}
		};
		field.setOptionContextMenu(contextMenu);
		controlsGroup.addMember(field);
	}

	private void addPopupMenus(FormGroup controlsGroup) {
		addDefaultPopupMenu(controlsGroup);
		addInvalidateComponentPopup(controlsGroup);

		CommandModel command1 = simpleCommandModel("command1", null, null);
		CommandModel command2 = simpleCommandModel("command2", null, null);
		PopupMenuField visibilityTestMenuOpener =
			DefaultPopupMenuField.newField("visibilityOfMenuOpenerTest", command1, command2);
		visibilityTestMenuOpener.setLabel("PopupMenu which is just visible if any content is visible.");
		BooleanField command1Visibility =
			FormFactory.newBooleanField("command1Visibility", Boolean.TRUE, !FormFactory.IMMUTABLE);
		command1Visibility
			.addValueListener((field, oldVal, newVal) -> command1.setVisible(Utils.isTrue((Boolean) newVal)));
		command1Visibility.setLabel("Visibility of " + command1.getLabel() + " in menu.");
		BooleanField command2Visibility =
			FormFactory.newBooleanField("command2Visibility", Boolean.TRUE, !FormFactory.IMMUTABLE);
		command2Visibility.setLabel("Visibility of " + command2.getLabel() + " in menu.");
		command2Visibility
			.addValueListener((field, oldVal, newVal) -> command2.setVisible(Utils.isTrue((Boolean) newVal)));

		controlsGroup.addMember(command1Visibility);
		controlsGroup.addMember(command2Visibility);
		controlsGroup.addMember(visibilityTestMenuOpener);
	}

	private void addDefaultPopupMenu(FormGroup controlsGroup) {
		List<CommandModel> commands = new ArrayList<>(3);
		commands.add(simpleCommandModel("Bearbeiten", com.top_logic.element.layout.grid.Icons.SAVE_BUTTON_ICON,
			com.top_logic.element.layout.grid.Icons.SAVE_BUTTON_ICON_DISABLED));
		CommandModel exportModel = simpleCommandModel("Export", com.top_logic.layout.table.component.Icons.EXPORT_GRID,
			com.top_logic.layout.table.component.Icons.EXPORT_GRID_DISABLED);
		exportModel.setNotExecutable(com.top_logic.tool.execution.I18NConstants.ERROR_DISABLED);
		commands.add(exportModel);
		commands.add(simpleCommandModel("Löschen", null, null));
		PopupMenuField popupMenuField = DefaultPopupMenuField.newField("openPopupMenu", commands);
		popupMenuField.setLabel("Open command menu");
		controlsGroup.addMember(popupMenuField);
	}

	private void addInvalidateComponentPopup(FormGroup controlsGroup) {
		CommandModel commandModel = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				TestControlsForm.this.invalidate();
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		commandModel.setLabel("Invalidate component");
		commandModel.setVisible(false);
		PopupMenuField invalidateComponentField =
			DefaultPopupMenuField.newField("openInvalidateCompnoentField", commandModel);
		invalidateComponentField.setLabel("Open invalidate component menu");
		controlsGroup.addMember(invalidateComponentField);
	}

	CommandModel simpleCommandModel(String label, ThemeImage image, ThemeImage nonExecImagePath) {
		CommandModel commandModel = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				MessageBox.confirm(getWindowScope(), MessageType.CONFIRM, "Kommando '" + label
					+ "' wird ausgeführt.", MessageBox.button(ButtonType.OK));
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		commandModel.setLabel(label);
		if (image != null) {
			commandModel.setImage(image);
		}
		if (nonExecImagePath != null) {
			commandModel.setNotExecutableImage(nonExecImagePath);
		}
		return commandModel;
	}

	private ComplexField createCalendarMarkerDemoField() {
		ComplexField markableDateInput = FormFactory.newDateField("dateInputControl", new Date(), false);
		Calendar calStart = createCalendar(addDays(new Date(), -7));
		Calendar calEnd = createCalendar(addDays(new Date(), 7));
		CalendarMarker calendarMarker =
			new DefaultCalendarMarker(calStart, calEnd, false, DemoMarkerFactory.MARKED_GREEN, DemoMarkerFactory.MARKED_RED, false);
		markableDateInput.set(CalendarControl.MARKER_CSS, calendarMarker);
		return markableDateInput;
	}

	private void addOpenCalendarControl(FormGroup controlsGroup) {
		Date initialDate = null;
		ComplexField displayField = FormFactory.newDateField("displayDate", initialDate, FormFactory.IMMUTABLE);
		displayField.setLabel(Resources.getInstance().getString(I18NConstants.DISPLAY_SELECTED_DATE));
		displayField.setControlProvider(ValueDisplay.INSTANCE);
		ComplexField editField = FormFactory.newDateField("openCalendar", initialDate, !FormFactory.IMMUTABLE);
		editField.setControlProvider(OpenCalendar.INSTANCE);
		editField.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				displayField.setValue(newValue);
			}
		});
		controlsGroup.addMember(editField);
		controlsGroup.addMember(displayField);

	}

	private void addPopupControl(FormGroup controlsGroup) {
		HiddenField simpleField = FormFactory.newHiddenField("popupControl", null);
		controlsGroup.addMember(simpleField);
		simpleField.setControlProvider(new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return new FragmentControl(new HTMLFragment() {

					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						// Classes and Input are used to show background of image.
						out.beginTag(HTMLConstants.SPAN, HTMLConstants.CLASS_ATTR, "cImageButton");
						XMLTag icon = com.top_logic.layout.form.tag.Icons.OPEN_CHOOSER.toIcon();
						icon.beginBeginTag(context, out);
						out.writeAttribute(HTMLConstants.CLASS_ATTR, "input-image");
						out.beginAttribute(HTMLConstants.ONCLICK_ATTR);
						DefaultPopupDialogModel popupModel = new DefaultPopupDialogModel(
							DefaultLayoutData.newLayoutData(
							DisplayDimension.dim(100, DisplayUnit.PIXEL),
							DisplayDimension.dim(100, DisplayUnit.PIXEL)));
						LayoutUtils.renderOpenPopupDialog(context, out, () -> Fragments.text("Popup content"),
							popupModel);
						out.endAttribute();
						icon.endEmptyTag(context, out);
						out.endTag(HTMLConstants.SPAN);
					}
				});
			}
		});
	}

	private FormMember createConfigurationGroup() {
		FormGroup group = new FormGroup("configuration", getResPrefix());
		group.addMember(FormFactory.newStringField("switchStateField"));
		return group;
	}

	private FormMember createExternalLinkGroup() {
		FormGroup group = new FormGroup("externalLinks", getResPrefix());
		ControlProvider cp = new ControlProvider() {
			
			@Override
			public Control createControl(Object model, String style) {
				return new ControlViewAdapter(new HTMLFragmentView((HTMLFragment) ((HiddenField) model).getValue()));
			}
		};
		ExternalLink simpleLink = new ExternalLink("http://www.top-logic.com");
		HiddenField simpleLinkField = FormFactory.newHiddenField("simpleLink", simpleLink);
		simpleLinkField.setControlProvider(cp);
		group.addMember(simpleLinkField);
		ExternalLink linkWithLabel = new ExternalLink("http://www.top-logic.com");
		linkWithLabel.setLabel(ResKey.text("<i>TopLogic</i>"));
		HiddenField linkWithLabelField = FormFactory.newHiddenField("linkWithLabel", linkWithLabel);
		linkWithLabelField.setControlProvider(cp);
		group.addMember(linkWithLabelField);
		ExternalLink linkWithImage = new ExternalLink("http://www.top-logic.com");
		linkWithImage.setLabel(ResKey.text("<i>TopLogic</i>"));
		linkWithImage.setImage(Icons.TOP_LOGIC);
		HiddenField linkWithImageField = FormFactory.newHiddenField("linkWithImage", linkWithImage);
		linkWithImageField.setControlProvider(cp);
		group.addMember(linkWithImageField);
		return group;
	}

	private SelectField createMailLinkContentField() {
		List<String> singletonEntry = Collections.singletonList("E-Mail-Link");
		SelectField mailContentField = FormFactory.newSelectField("mailLinkContent", singletonEntry);
		mailContentField.setAsSelection(singletonEntry);
		mailContentField.setImmutable(true);
		mailContentField.setOptionLabelProvider(new DefaultResourceProvider() {
			@Override
			public String getLink(DisplayContext context, Object anObject) {
				return HTMLUtil.getMailToJS("example@top-logic.com");
			}
		});
		return mailContentField;
	}

	private void addGalleryField(FormGroup context) {
		try {
			GalleryField galleryField =
				FormFactory.newGalleryField("galleryField", DisplayDimension.px(320), DisplayDimension.px(256));
			galleryField.setImages(createImages());
			context.addMember(galleryField);
		} catch (IOException ex) {
			Logger.error("Cannot create gallery field!", ex, this);
		}
	}

	private List<GalleryImage> createImages() throws IOException {
		List<GalleryImage> images = new ArrayList<>();
		images.add(getImage("/images/test/BOS Firmengebaeude.jpg"));
		images.add(getImage("/images/test/BOS Buero.jpg"));
		images.add(getImage("/images/test/BOS Garage.jpg"));
		images.add(getImage("/images/test/BOS Gebaeudeplan.jpg"));
		return images;
	}

	private GalleryImage getImage(String imagePath) throws IOException {
		return new TransientGalleryImage(getDataItem(imagePath));
	}

	private BinaryData getDataItem(String imagePath) throws IOException {
		return FileManager.getInstance().getData(imagePath);
	}

	private void addTestSelectionControlTableSingle(FormGroup controlsGroup) {
		addTestSelectionControlTable(controlsGroup, "selectionControlTableSingle", false);
	}

	private void addTestSelectionControlTableMulti(FormGroup controlsGroup) {
		addTestSelectionControlTable(controlsGroup, "selectionControlTableMulti", true);
	}

	private void addTestSelectionControlTable(FormGroup controlsGroup, String selectFieldName, boolean multiSelection) {
		String preselectedFixedOption = "cccc";
		List<String> fixedOptions = new ArrayList<>();
		if (multiSelection) {
			fixedOptions.add(preselectedFixedOption);
		}
		fixedOptions.add("aaaa");
		List<String> selection = new ArrayList<>();
		if (multiSelection) {
			selection.add(preselectedFixedOption);
		}
		selection.add("invalidSelection");
		SelectField tableSelectField =
			FormFactory.newSelectField(selectFieldName, abcdOptions(), multiSelection, selection, false);
		TableConfigurationProvider additionalProvider = new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				ColumnConfiguration nameColumn = table.declareColumn("name");
				nameColumn.setAccessor(IdentityAccessor.INSTANCE);
				nameColumn.setResourceProvider(
					LabelResourceProvider.toResourceProvider(tableSelectField.getOptionLabelProvider()));
				nameColumn.setColumnLabel("Name");
				createExternalLinkColumn(table);
			}
		};
		TableConfigurationProvider baseProvider = tableSelectField.getTableConfigurationProvider();
		tableSelectField.setTableConfigurationProvider(combine(baseProvider, additionalProvider));
		tableSelectField.setSelectDialogProvider(createDialogProvider(3));
		tableSelectField.setFixedOptions(fixedOptions);
		controlsGroup.addMember(tableSelectField);
	}

	private SelectDialogProvider createDialogProvider(int optionsPerPage) {
		SelectDialogConfig dialogConfig = SelectDialogProvider.newTableConfig();
		dialogConfig.setOptionsPerPage(optionsPerPage);
		return TypedConfigUtil.createInstance(dialogConfig);
	}

	private void addTestSelectionControlTreeTableInfinite(FormGroup controlsGroup) {
		boolean showRootNode = true;
		addTestSelectionControlTreeTable(controlsGroup, "selectionControlTreeTableInfinite", showRootNode, false, true);
	}

	private void addTestSelectionControlTreeTableSingle(FormGroup controlsGroup) {
		boolean showRootNode = true;
		addTestSelectionControlTreeTable(controlsGroup, "selectionControlTreeTableSingle", showRootNode, false, false);
	}

	private void addTestSelectionControlTreeTableMultiWithRootNode(FormGroup controlsGroup) {
		boolean showRootNode = true;
		addTestSelectionControlTreeTable(controlsGroup, "selectionControlTreeTableMultiWithRootNode", showRootNode,
			true, false);
	}

	private void addTestSelectionControlTreeTableMultiWithoutRootNode(FormGroup controlsGroup) {
		boolean showRootNode = false;
		addTestSelectionControlTreeTable(controlsGroup, "selectionControlTreeTableMultiWithoutRootNode", showRootNode,
			true, false);
	}

	private void addTestSelectionControlTreeTable(FormGroup controlsGroup, final String selectFieldName,
			boolean showRootNode, boolean multiSelection, boolean infiniteOptions) {
		DefaultMutableTLTreeModel options;
		if (infiniteOptions) {
			options = createOptionTreeInfinite();
		} else {
			options = createOptionTree();
		}
		List<?> fixedOptions = createFixedOptions(options);
		List<Object> selection = new ArrayList<>();
		if (multiSelection) {
			selection.add(fixedOptions.get(fixedOptions.size() - 1));
		}
		selection.add(createInvalidNode(options));

		SelectField tableSelectField =
			FormFactory.newSelectField(selectFieldName, options, multiSelection, false);
		tableSelectField.setAsSelection(selection);
		tableSelectField.getOptionsAsTree().setSelectableOptionsFilter(new SelectionModelFilter(fixedOptions));
		tableSelectField.getOptionsAsTree().setShowRoot(showRootNode);
		TableConfigurationProvider additionalProvider = new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				createExternalLinkColumn(table);
				ColumnConfiguration nameColumn = table.declareColumn("name");
				nameColumn.setAccessor(IdentityAccessor.INSTANCE);
				nameColumn.setResourceProvider(
					LabelResourceProvider.toResourceProvider(tableSelectField.getOptionLabelProvider()));
				nameColumn.setColumnLabel("Name");
				table.setDefaultColumns(Arrays.asList(COLUMN_EXTERNAL_LINK, "name"));
			}

		};
		TableConfigurationProvider baseProvider = tableSelectField.getTableConfigurationProvider();
		tableSelectField.setTableConfigurationProvider(combine(baseProvider, additionalProvider));
		tableSelectField.setSelectDialogProvider(createDialogProvider(10000));
		tableSelectField.setFixedOptions(fixedOptions);
		controlsGroup.addMember(tableSelectField);
	}

	void createExternalLinkColumn(TableConfiguration table) {
		ColumnConfiguration linkColumn = table.declareColumn(COLUMN_EXTERNAL_LINK);
		linkColumn.setRenderer(DemoLinkToGoogleRenderer.INSTANCE);
		linkColumn.setColumnLabel("Externer Link");
	}

	private FormGroup createTestFormGroup(String groupName) {
		FormGroup controlsGroup = new FormGroup(groupName, I18NConstants.CONTROLS);
		controlsGroup.setResources(PlainKeyResources.INSTANCE);
		return controlsGroup;
	}

	private void addDateTimeFields(FormGroup controlsGroup) {
		Date initialValue = DateUtil.createDate(2016, Calendar.OCTOBER, 22);
		DateTimeField dateTimeField = new DateTimeField("dateTimeField", initialValue, false);
		Constraint constraint = new DateRangeConstraint(null, initialValue);
		dateTimeField.addConstraint(constraint);
		final StringField valueField = FormFactory.newStringField("dateTimeFieldValue", true);
		valueField.setLabel("Value of date time field");
		class Listener implements ValueListener, HasErrorChanged {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				setDateAsString((Date) newValue);
			}

			void setDateAsString(Date newDateValue) {
				String newValueString;
				if (newDateValue == null) {
					newValueString = "Field has 'null' value";
				} else {
					StringBuilder value = new StringBuilder();
					SimpleDateFormat format = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy HH:mm:ss Z");
					value.append("Server time zone: ");
					value.append(format.format(newDateValue));
					value.append(", User time zone: ");
					format.setTimeZone(ThreadContext.getTimeZone());
					value.append(format.format(newDateValue));
					newValueString = value.toString();
				}
				valueField.setValue(newValueString);
			}

			@Override
			public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
				if (sender.hasError()) {
					valueField.setAsString("Field has error");
				} else {
					setDateAsString((Date) sender.getValue());
				}
				return Bubble.BUBBLE;
			}

		}
		Listener listener = new Listener();
		dateTimeField.addValueListener(listener);
		dateTimeField.addListener(FormField.HAS_ERROR_PROPERTY, listener);

		controlsGroup.addMember(dateTimeField);
		listener.setDateAsString((Date) dateTimeField.getValue());
		controlsGroup.addMember(valueField);
	}

	private void addListFields(FormGroup controlsGroup) {
		DefaultListModel<String> model = createListModel();
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		ListField listField = FormFactory.newListField("listControl", model, selectionModel);
		controlsGroup.addMember(listField);
	}

	final DefaultListModel<String> createListModel() {
		DefaultListModel<String> model = new DefaultListModel<>();
		model.addElement("aaaaa");
		model.addElement("bbbb");
		model.addElement("ccc");
		model.addElement("dd");
		model.addElement("e");
		return model;
	}

	private void addImageField(FormGroup controlsGroup) {
		FormGroup imageGroup = new FormGroup("image", getResPrefix());
		controlsGroup.addMember(imageGroup);

		DemoChartProducer imageProducer = new DemoChartProducer(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
		
		final ImageField imageField = new ImageField("imageField");
		imageGroup.addMember(imageField);
		imageField.setImageComponent(imageProducer);

		final FormField widthField = FormFactory.newIntField("imageWidth");
		imageGroup.addMember(widthField);
		widthField.initializeField(200);
		widthField.addConstraint(new IntRangeConstraint(1, 5000));

		final FormField heightField = FormFactory.newIntField("imageHeight");
		imageGroup.addMember(heightField);
		heightField.initializeField(150);
		heightField.addConstraint(new IntRangeConstraint(1, 5000));

		final Runnable setDimensionToModel = new Runnable() {

			@Override
			public void run() {
				if (!widthField.checkConstraints()) {
					return;
				}
				if (!heightField.checkConstraints()) {
					return;
				}
				Dimension d = new Dimension((Integer) widthField.getValue(), (Integer) heightField.getValue());
				// set image size
				imageField.setDimension(d);
			}

		};
		setDimensionToModel.run();

		ValueListener fixedSizeListener = new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				setDimensionToModel.run();
			}
		};
		widthField.addValueListener(fixedSizeListener);
		heightField.addValueListener(fixedSizeListener);

		FormField isDynamic = FormFactory.newBooleanField("imageDynamic");
		imageGroup.addMember(isDynamic);
		isDynamic.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				boolean dynamic = Utils.isTrue((Boolean) newValue);
				heightField.setDisabled(dynamic);
				widthField.setDisabled(dynamic);
				if (dynamic) {
					// mark image dynamic
					imageField.setDimension(null);
				} else {
					setDimensionToModel.run();
				}
			}
		});

		addGalleryField(imageGroup);

		final FormField pictureField = FormFactory.newPictureField("pictureField", null, new Dimension(400, 400));
		DataField pictureInputField = FormFactory.newDataField("pictureInputField");
		pictureInputField.addConstraint(new AbstractConstraint() {

			@Override
			public boolean check(Object value) throws CheckException {
				if (!isPictureOrNull(value)) {
					throw new CheckException(ImageDataUtil.noValidImageExtension());
				}
				return true;
			}
		});
		pictureInputField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (isPictureOrNull(newValue)) {
					pictureField.setValue(newValue);
				}
			}

		});
		imageGroup.addMember(pictureField);
		imageGroup.addMember(pictureInputField);

	}

	boolean isPictureOrNull(Object value) {
		if (value == null) {
			return true;
		}
		return ImageDataUtil.isSupportedImageFilename(((BinaryDataSource) value).getName());
	}

	private void addButtonControls(FormGroup context) {
		FormGroup group = new FormGroup("buttons", context.getResources());
		context.addMember(group);
		group.addMember(createCommandModel("ButtonComponentButtonRenderer"));
		group.addMember(createCommandModel("DefaultButtonRenderer"));
		group.addMember(createCommandModel("DefaultButtonRendererNoReason"));
		group.addMember(createCommandModel("ImageButtonRenderer"));
		group.addMember(createCommandModel("ImageButtonRendererNoReason"));
		group.addMember(createCommandModel("ImageLinkButtonRenderer"));
		group.addMember(createCommandModel("ImageLinkButtonRendererNoReason"));
		group.addMember(createCommandModel("LinkButtonRenderer"));
		group.addMember(createCommandModel("LinkButtonRendererNoReason"));
	}

	private CommandField createCommandModel(String name) {
		CommandField field = FormFactory.newCommandField(name, createAlertCommand("Button pressed"));
		field.setLabel("Label of command model.");
		field.setTooltip("Tooltip of command model.");
		field.setTooltipCaption("Tooltip caption of command model.");
		field.setImage(com.top_logic.layout.form.tag.Icons.OPEN_CHOOSER);
		field.setNotExecutableImage(com.top_logic.layout.form.tag.Icons.OPEN_CHOOSER_DISABLED);
		return field;
	}

	private ComplexField createBeaconComplexField() {
		ComplexField complexField = FormFactory.newComplexField("beaconControl-complexField", NumberFormat.getInstance());
		complexField.setAsString("0");
		return complexField;
	}

	private FormMember createIconControlTest() {
		FormGroup group = new FormGroup("iconControlTest", PlainKeyResources.INSTANCE);

		class NamedColor {
			private final String _name;

			private final Color _color;

			public NamedColor(String name, Color color) {
				_name = name;
				_color = color;
			}

			public Color getColor() {
				return _color;
			}

			@Override
			public String toString() {
				return _name;
			}
		}

		SelectField icons =
			FormFactory.newSelectField("icons",
				Arrays.asList(
					new NamedColor("black", Color.black),
					new NamedColor("blue", Color.blue),
					new NamedColor("red", Color.red),
					new NamedColor("green", Color.green),
					new NamedColor("yellow", Color.yellow)));
		icons.setOptionLabelProvider(DefaultLabelProvider.INSTANCE);
		group.addMember(icons);

		HiddenField iconField = FormFactory.newHiddenField("icon");
		final IconControl icon = new IconControl();
		iconField.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return icon;
			}
		});
		group.addMember(iconField);

		icons.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				NamedColor namedColor = toColor(newValue);
				if (namedColor == null) {
					icon.setSrc(null);
					return;
				}

				Color color = namedColor.getColor();
				String iconPath = createIcon(color);
				icon.setSrcPath(iconPath);
			}

			private String createIcon(Color color) {
				try {
					BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = (Graphics2D) image.getGraphics();
					graphics.setColor(color);
					graphics.fillOval(0, 0, 31, 31);
					File iconFile = File.createTempFile("img", ".png", Settings.getInstance().getImageTempDir());
					ImageIO.write(image, "png", iconFile);

					String iconPath = "/" + Settings.getInstance().getImageTempDirName() + "/" + iconFile.getName();
					return iconPath;
				} catch (IOException ex) {
					throw new IOError(ex);
				}
			}

			private NamedColor toColor(Object newValue) {
				return (NamedColor) CollectionUtil.getSingleValueFrom(newValue);
			}
		});

		return group;
	}

	private FormMember createTextInputWithPlaceholder() {
		StringField field = FormFactory.newStringField("textInputWithPlaceholder");
		field.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				TextInputControl control = new TextInputControl((FormField) model);
				control.setPlaceHolder("enter value");
				return control;
			}
		});
		return field;
	}

	private FormMember createTextInputWithContextMenu() {
		StringField field = FormFactory.newStringField("textInputWithContextMenu");
		field.setContextMenu(new ContextMenuProvider() {

			@Override
			public boolean hasContextMenu(Object obj) {
				return field(obj).isActive();
			}

			public StringField field(Object obj) {
				return (StringField) obj;
			}

			@Override
			public Menu getContextMenu(Object obj) {
				return Menu.create(
					option(obj, "Option 1"),
					option(obj, "Option 2"),
					option(obj, "Option 3"));
			}

			private CommandModel option(Object obj, String value) {
				Command command = new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						field(obj).setValue(value);
						return HandlerResult.DEFAULT_RESULT;
					}
				};
				CommandModel result = CommandModelFactory.commandModel(command);
				result.setLabel(value);
				return result;
			}
		});
		return field;
	}

	private FormMember createCodeEditorField() {
		StringField result = FormFactory.newStringField("codeEditor");
		result.setValue("<r><x/></r>");
		result.setControlProvider(new CodeEditorControl.CP(CodeEditorControl.MODE_XML));
		return result;
	}

	private BooleanField booleanSelectField(String name, boolean mandatory) {
		BooleanField result = FormFactory.newBooleanField(name, mandatory ? null : false, mandatory, false, null);

		SelectFieldUtils.setOptions(result, Arrays.asList(true, false));
		SelectFieldUtils.setOptionLabelProvider(result, BooleanLabelProvider.INSTANCE);
		SelectFieldUtils.setOptionComparator(result, Equality.INSTANCE);

		result.setControlProvider(new ControlProvider() {
			@Override
			public Control createControl(Object model, String style) {
				return new SelectControl((FormField) model, true);
			}
		});

		return result;
	}

	private void showDisplayImageControl(FormGroup controlsGroup) throws IOException {
		DataField f = FormFactory.newDataField("displayImageControl");
		f.setAcceptedTypes("image/*");
		String themedLink = ThemeFactory.getTheme().getFileLink(ImageComponent.DEFAULT_IMG_PATH);
		BinaryData emptyImage = FileManager.getInstance().getData(themedLink);
		final DisplayImageControl imageControl = new DisplayImageControl(emptyImage, null);
		f.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				BinaryDataSource newItem;
				Dimension dimension;
				if (newValue != null) {
					Collection<?> listValue = (Collection<?>) newValue;
					if (listValue.isEmpty()) {
						newItem = emptyImage;
						dimension = null;
					} else {
						newItem = (BinaryDataSource) listValue.iterator().next();
						dimension = DisplayImageControl.DEFAULT_DIMENSION;
					}
				} else {
					newItem = emptyImage;
					dimension = null;
				}
				imageControl.setDataItem(newItem, dimension);
			}
		});
		f.setControlProvider(new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				BlockControl blockControl = new BlockControl();
				Control uploadControl =
					DefaultFormFieldControlProvider.INSTANCE.createControl(model,
						FormTemplateConstants.STYLE_DIRECT_VALUE);
				blockControl.addChild(uploadControl);
				blockControl.addChild(imageControl);
				return blockControl;
			}
		});
		controlsGroup.addMember(f);
	}

	private void addTestKeyEventListener(FormGroup controlsGroup) {
		KeyCode[] allCodes = KeyCode.values();
		final KeySelector[] allKeys = new KeySelector[8 * allCodes.length];
		int k = 0;
		for (int shift = 0; shift < 2; shift++) {
			for (int ctrl = 0; ctrl < 2; ctrl++) {
				for (int alt = 0; alt < 2; alt++) {
					for (int n = 0, cnt = allCodes.length; n < cnt; n++) {
						allKeys[k * allCodes.length + n] = new KeySelector(allCodes[n], shift > 0, ctrl > 0, alt > 0);
					}
					k++;
				}
			}
		}
		FormGroup group = new FormGroup("testKeyEventListener", PlainKeyResources.INSTANCE);
		final StringField textInput = FormFactory.newStringField("keyListener");
		final StringField returnInput = FormFactory.newStringField("returnListener");
		final StringField messageOutput = FormFactory.newStringField("keyMessage");
		messageOutput.setImmutable(true);
		messageOutput.setInheritDeactivation(false);

		class InspectingKeyListener extends KeyEventListener {
			public InspectingKeyListener(KeySelector... keySelectors) {
				super(keySelectors);
			}

			@Override
			public HandlerResult handleKeyEvent(DisplayContext commandContext, KeyEvent event) {
				messageOutput.setValue("Current value: '" + ((FormField) event.getSource()).getValue() + "', key: '" +
					(event.hasShiftModifier() ? "Shift-" : "") +
					(event.hasCtrlModifier() ? "Ctrl-" : "") +
					(event.hasAltModifier() ? "Alt-" : "") +
					event.getKeyCode() + "'");
				return HandlerResult.DEFAULT_RESULT;
			}
		}

		textInput.addKeyListener(new InspectingKeyListener(allKeys));
		returnInput.addKeyListener(new InspectingKeyListener(new KeySelector(KeyCode.RETURN, 0)));
		group.addMember(textInput);
		group.addMember(returnInput);
		group.addMember(messageOutput);
		controlsGroup.addMember(group);
	}

	private FormMember testForeachControl() {
		FormGroup group = new FormGroup("testForeachControl", PlainKeyResources.INSTANCE);

		FormArray foreach = new FormArray("foreach", ResPrefix.NONE);
		FormGroup row = new ForeachContentBuilder().mkRow();
		foreach.addMember(row);

		// The structure of the form model is as follows:
		//
		// foreach
		//  +- row-1
		//  |   +- addRow
		//  |   +- values
		//  |   |   +- valueGroup-1
		//  |   |   |   +- valueInput
		//  |   |   |   |   +- value
		//  |   |   |   +- removeValue
		//  |   |   |  
		//  |   |   +- valueGroup-2 ...
		//  |   |     
		//  |   +- addValue
		//  |   
		//  +- row-2 ...
		foreach.setControlProvider(new DefaultFormFieldControlProvider() {
			@Override
			public Control visitFormContainer(FormContainer member, Void arg) {
				return foreach(member,
					table(
						loop("row",
							tr(
								td(refValue("addRow", ButtonControlProvider.INSTANCE)),
								td(
									refForeach("values",
										table(
											tr(
												loop(
													td(
														refScope("valueInput",
															refValue("value"), refError("value")),
														refValue("removeValue", ButtonControlProvider.INSTANCE))),
												td(
													refValue("$row.addValue", ButtonControlProvider.INSTANCE))))))))));
			}
		});
		group.addMember(foreach);

		return group;
	}

	class ForeachContentBuilder {
		IdentifierSource _ids = new DefaultIdentifierSource();

		FormGroup mkRow() {
			FormGroup row = new FormGroup("row-" + _ids.createNewID(), PlainKeyResources.INSTANCE);
			CommandField addRow = new ADD_ROW("addRow");
			addRow.setLabel("+");
			row.addMember(addRow);
			FormArray values = new FormArray("values", ResPrefix.NONE);
			row.addMember(values);
			FormMember value = mkValue();
			values.addMember(value);
			CommandField addValue = new ADD_VALUE("addValue");
			addValue.setLabel("+");
			row.addMember(addValue);
			return row;
		}

		FormMember mkValue() {
			// Note: Qualified names of input elements must be unique, because this name is used as
			// client-side identifier for the input element. This in turn is required to link the
			// label with the input element.
			FormGroup group = new FormGroup("valueGroup-" + _ids.createNewID(), PlainKeyResources.INSTANCE);

			// Note: The valueInput group is only for demonstrating the scope reference in the
			// foreach display.
			FormGroup valueInput = new FormGroup("valueInput", PlainKeyResources.INSTANCE);
			FormField value = FormFactory.newDateField("value", null, false);
			valueInput.addMember(value);
			group.addMember(valueInput);

			CommandField removeValue = new REMOVE_VALUE("removeValue");
			removeValue.setLabel("-");
			group.addMember(removeValue);
			return group;
		}

		class ADD_ROW extends CommandField {

			public ADD_ROW(String name) {
				super(name);
			}

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				FormContainer myRow = getParent();
				FormArray myContainer = (FormArray) myRow.getParent();
				int index = indexOf(myContainer, myRow);

				myContainer.addMember(index < 0 ? myContainer.size() : index + 1, mkRow());
				return HandlerResult.DEFAULT_RESULT;
			}

			private int indexOf(FormArray myContainer, FormMember member) {
				int index = 0;
				for (Iterator<?> it = myContainer.getMembers(); it.hasNext(); index++) {
					if (it.next() == member) {
						return index;
					}
				}
				return -1;
			}
		};

		public class ADD_VALUE extends CommandField {
			public ADD_VALUE(String name) {
				super(name);
			}

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				FormContainer myRow = getParent();
				FormArray myValues = (FormArray) myRow.getMember("values");
				myValues.addMember(mkValue());
				return HandlerResult.DEFAULT_RESULT;
			}
		};

		class REMOVE_VALUE extends CommandField {
			public REMOVE_VALUE(String name) {
				super(name);
			}

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				FormContainer valueGroup = getParent();
				FormContainer valuesGroup = valueGroup.getParent();
				FormContainer myRow = valuesGroup.getParent();
				FormContainer myContainer = myRow.getParent();

				// Do not remove last input field.
				if (valuesGroup.size() > 1 || myContainer.size() > 1) {
					valuesGroup.removeMember(valueGroup);
					if (valuesGroup.size() == 0) {
						if (myContainer.size() > 1) {
							// Remove empty row.
							myContainer.removeMember(myRow);
						}
					}
				}

				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	private FormMember testLengthConstraint() {
		FormGroup group = new FormGroup("testLengthConstraint", PlainKeyResources.INSTANCE);

		{
			StringField field = FormFactory.newStringField("maxLength20");
			field.addConstraint(new StringLengthConstraint(0, 20));
			field.setTooltip("Field with a length constraint of 20 characters maximum");
			group.addMember(field);
		}

		{
			StringField field = FormFactory.newStringField("minLength10");
			field.addConstraint(new StringLengthConstraint(10));
			field.setTooltip("Field with a minimum length constraint of 10 characters");
			group.addMember(field);
		}

		{
			StringField field = FormFactory.newStringField("lengthBetween10And20");
			field.addConstraint(new StringLengthConstraint(10, 20));
			field.setTooltip("Field with a range constraint of 10 to 20 characters");
			group.addMember(field);
		}

		{
			StringField field = FormFactory.newStringField("lengthBetween10And20OrEmpty");
			field.addConstraint(new StringLengthConstraint(10, 20, true));
			field.setTooltip("Field with a range constraint of 10 to 20 characters that may also be empty");
			group.addMember(field);
		}

		TextInputTag multiLine = new TextInputTag();
		multiLine.setMultiLine(true);
		multiLine.setColumns(30);
		multiLine.setRows(6);
		{
			StringField field = FormFactory.newStringField("maxRows5a15");
			field.addConstraint(new ExtendedStringLengthConstraint(5, 15, ProportionalCharSizeMap.INSTANCE));
			field.setControlProvider(multiLine);
			field.setTooltip("Field with a text block constraint of 5 row with at most 15 characters");
			group.addMember(field);
		}

		{
			StringField field = FormFactory.newStringField("maxRows5a15Or50Char");
			field.addConstraint(new ExtendedStringLengthConstraint(5, 15, ProportionalCharSizeMap.INSTANCE, 50));
			field.setTooltip("Field with a text block constraint of 5 row with at most 15 characters and a maximum character limit of 50");
			field.setControlProvider(multiLine);
			group.addMember(field);
		}

		return group;
	}

	private FormMember testTableField(ResourceView containerResPrefix) {
		return createTableField("tableField", containerResPrefix, ColumnCustomization.SELECTION, false);
	}


	private TableField testGlobalSelectionTableField(ResourceView containerResPrefix) {
		TableField tableField =
			createTableField("tableFieldGlobalSelection", containerResPrefix, ColumnCustomization.SELECTION, true);
		tableField.setSelectable(true);
		return tableField;
	}

	private FormMember testTableListField(ResourceView containerResPrefix) {
		return createTableField("tableListField", containerResPrefix, ColumnCustomization.SELECTION, false);
	}

	private FormMember testTableFieldNoColumnSelect(ResourceView containerResPrefix) {
		return createTableField("tableFieldNoColumnSelect", containerResPrefix, ColumnCustomization.ORDER, false);
	}

	private FormMember testTableFieldNoColumnConfig(ResourceView containerResPrefix) {
		return createTableField("tableFieldNoColumnConfig", containerResPrefix, ColumnCustomization.NONE, false);
	}

	private FormMember testTableFieldEditable() {
		FormContainer tableContainer = new FormGroup("tableFieldContainer", PlainKeyResources.INSTANCE);
		TableField tableField = FormFactory.newTableField("tableFieldEditable");
		
		TableConfigurationProvider customTableConfig = new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				ColumnConfiguration stringColumn = table.declareColumn("string");
				stringColumn.setFieldProvider(StringFieldProvider.INSTANCE);
				stringColumn.setControlProvider(TextInputControlProvider.INSTANCE);
				stringColumn.setFilterProvider(new FirstCharacterFilterProvider(DefaultLabelProvider.INSTANCE));
				stringColumn.setSortKeyProvider(FormFieldValueMapping.INSTANCE);

				ColumnConfiguration intColumn = table.declareColumn("int");
				intColumn.setFieldProvider(new AbstractFieldProvider() {

					@Override
					public FormMember createField(Object model, Accessor accessor, String property) {
						Object value = accessor.getValue(model, property);
						return FormFactory.newIntField(getFieldName(model, accessor, property), value, false);
					}
				});
				intColumn.setControlProvider(IntegerInputControl.Provider.INSTANCE);
				intColumn.setSortKeyProvider(FormFieldValueMapping.INSTANCE);

				ColumnConfiguration dateColumn = table.declareColumn("date");
				dateColumn.setFieldProvider(DateFieldProvider.INSTANCE);
				dateColumn.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
				dateColumn.setSortKeyProvider(FormFieldValueMapping.INSTANCE);

				ColumnConfiguration expandableColumn = table.declareColumn("expandable");
				expandableColumn.setFieldProvider(new AbstractFieldProvider() {
					@Override
					public FormMember createField(Object model, Accessor accessor, String property) {
						Object value = accessor.getValue(model, property);
						return FormFactory.newExpandableStringField(getFieldName(model, accessor, property), value,
							false);
					}
				});
				expandableColumn.setControlProvider(new ExpandableTextInputFormFieldControlProvider(5, 15, true));
				expandableColumn.setSortKeyProvider(FormFieldValueMapping.INSTANCE);

			}
		};
		EditableRowTableModel applicationModel =
			createApplicationModel("tableFieldEditable", tableContainer.getResources(), ColumnCustomization.SELECTION,
				customTableConfig);

		FormTableModel formTableModel = new FormTableModel(applicationModel, tableContainer);
		
		tableField.setTableModel(formTableModel);
		
		return tableField;
	}

	public TableField createTableField(String fieldName, ResourceView containerResPrefix,
			ColumnCustomization columnCustomization, boolean useGlobalSelection) {
		final TableField tableField = FormFactory.newTableField(fieldName);

		GenericSelectionModelOwner.AnnotatedModel algorithm = GenericSelectionModelOwner.AnnotatedModel.INSTANCE;

		SelectionModelOwner owner = new GenericSelectionModelOwner(tableField, algorithm);
		final DefaultMultiSelectionModel selectionModel = new DefaultMultiSelectionModel(owner);
		algorithm.annotate(tableField, selectionModel);

		TableModel applicationModel = createApplicationModel(fieldName, containerResPrefix, columnCustomization, null);

		tableField.setTableModel(applicationModel);
		if (useGlobalSelection) {
			tableField.setSelectionModel(selectionModel);
		}

		CommandModel button = new AbstractCommandModel() {
			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				Collection allRows = tableField.getTableModel().getAllRows();
				Set selection = selectionModel.getSelection();
				List<Integer> selectedRowNumbers = new ArrayList<>(selection.size());
				int index = 0;
				for (Object row : allRows) {
					if (selection.contains(row)) {
						selectedRowNumbers.add(index);
					}
					index++;
				}
				MessageBox.confirm(context.getWindowScope(), MessageType.INFO,
					"Following rows are selected: " + selectedRowNumbers, MessageBox.button(ButtonType.OK));
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		button.setImage(com.top_logic.layout.form.tag.Icons.OPEN_CHOOSER);
		button.setTooltip("Show selected rows.");
		tableField.getToolBar().defineGroup(CommandHandlerFactory.ADDITIONAL_GROUP).addButton(button);
		return tableField;
	}

	final EditableRowTableModel createApplicationModel(final String fieldName, final ResourceView containerResPrefix,
			ColumnCustomization columnCustomization, TableConfigurationProvider customTableConfig) {
		TableConfigurationProvider tableConfig = new TableConfigurationProvider() {

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				defaultColumn.setAccessor(MapAccessor.INSTANCE);
			}

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setResPrefix(new NestedResourceView(containerResPrefix, fieldName));
				ColumnConfiguration intColumn = table.declareColumn("int");
				intColumn.setCellStyle("align: right");

				ColumnConfiguration dateColumn = table.declareColumn("date");
				dateColumn.setLabelProvider(DateTimeLabelProvider.INSTANCE);
			}
		};
		if (customTableConfig != null) {
			tableConfig = TableConfigurationFactory.combine(tableConfig, customTableConfig);
		}
		TableConfigurationProvider configuredConfig = lookupTableConfigurationBuilder(fieldName);
		if (configuredConfig != null) {
			tableConfig = TableConfigurationFactory.combine(tableConfig, configuredConfig);
		}
		TableConfiguration table = TableConfigurationFactory.build(tableConfig);
		table.setColumnCustomization(columnCustomization);

		List objects = rowObjects();

		EditableRowTableModel applicationModel =
			new ObjectTableModel(new String[] { "string", "int", "date", "double", "expandable" }, table, objects);
		return applicationModel;
	}

	private FormMember testSelectionTableField(final ResourceView containerResPrefix) {
		List objects = rowObjects();

		final String fieldName = "selectionTableField";
		SelectionTableField field =
			FormFactory.newSelectionTableField(fieldName, objects, MapAccessor.INSTANCE, true, false);
		field.setOptionLabelProvider(DefaultLabelProvider.INSTANCE);
		field.setOptionComparator(NO_ORDER);
		field.setTableConfigurationProvider(new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setResPrefix(new NestedResourceView(containerResPrefix, fieldName));
			}
		});
		makeConfigurable(field);

		return field;
	}

	private FormMember testSelectionTableFieldMandatory(final ResourceView containerResPrefix) {
		List objects = rowObjects();

		final String fieldName = "selectionTableFieldMandatory";
		SelectionTableField field =
			FormFactory.newSelectionTableField(fieldName, objects, MapAccessor.INSTANCE, true, false);
		field.setOptionLabelProvider(DefaultLabelProvider.INSTANCE);
		field.setOptionComparator(NO_ORDER);
		field.setMandatory(true);
		field.setTableConfigurationProvider(new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setResPrefix(new NestedResourceView(containerResPrefix, fieldName));
			}
		});

		makeConfigurable(field);

		return field;
	}

	private List<Map<String, Object>> rowObjects() {
		Date date;
		try {
			date = CalendarUtil.newSimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY).parse("2017/03/07 15:23:15");
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
		return Arrays.asList(
			new MapBuilder<String, Object>().put("string", "Hello World!").toMap(),
			new MapBuilder<String, Object>().put("int", 42)
				.put("expandable", "Some multi-\nline\ntext\nwith\nmany\nusless\nlines.").toMap(),
			new MapBuilder<String, Object>().put("date", date).toMap(),
			new MapBuilder<String, Object>().put("double", 13.42).toMap()
			);
	}

	private FormMember testCSSClassChangeOnUserInput() {
		final FormGroup group = new FormGroup("CSSClassChangeOnUserInput", PlainKeyResources.INSTANCE);

		final Date now = new Date();
		final ComplexField d1 = FormFactory.newDateField("date1", now, false);
		final ComplexField d2 = FormFactory.newDateField("date2", now, false);

		ValueListener classChanger;
		d1.addValueListener(classChanger = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				int comparision = ((Date) d1.getValue()).compareTo((Date) d2.getValue());
				if (comparision > 0) {
					d1.setCssClasses("smaller");
					d2.setCssClasses("greater");
				}
				else if (comparision < 0) {
					d1.setCssClasses("greater");
					d2.setCssClasses("smaller");
				}
				else {
					d1.setCssClasses(null);
					d2.setCssClasses(null);
				}
			}
		});
		d2.addValueListener(classChanger);

		group.addMember(d1);
		group.addMember(d2);

		return group;
	}

	private void addTestSelectionControlMultiTreeFixed(FormGroup controlsGroup) {
		DefaultMutableTLTreeModel optionTree = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = optionTree.getRoot();
		root.createChild("a");
		DefaultMutableTLTreeNode bNode = root.createChild("b");
		bNode.createChild("b1");
		bNode.createChild("b2");
		DefaultMutableTLTreeNode b3Node = bNode.createChild("b3");
		DefaultMutableTLTreeNode b4Node = bNode.createChild("b4");
		DefaultMutableTLTreeNode cNode = root.createChild("c");
		root.createChild("d");
		DefaultMutableTLTreeNode eNode = root.createChild("e");
		eNode.createChild("e1");
		eNode.createChild("e2");
		DefaultMutableTLTreeNode e3Node = eNode.createChild("e3");
		eNode.createChild("e4");
		eNode.createChild("e5");
		root.createChild("f");
		root.createChild("g");
		List<DefaultMutableTLTreeNode> nonSelectableOptions = new LinkedList<>();
		nonSelectableOptions.add(b3Node);
		nonSelectableOptions.add(b4Node);
		nonSelectableOptions.add(cNode);
		nonSelectableOptions.add(eNode);
		nonSelectableOptions.add(e3Node);
		
		SelectField treeSelectField = FormFactory.newSelectField("selectionControlMultiTreeFixed", optionTree, true, false);
		treeSelectField.getOptionsAsTree().setSelectableOptionsFilter(new SelectionModelFilter(nonSelectableOptions));
		treeSelectField.setOptionComparator(new Comparator<DefaultMutableTLTreeNode>() {

			@Override
			public int compare(DefaultMutableTLTreeNode o1, DefaultMutableTLTreeNode o2) {
				String firstNode = o1.toString();
				String secondNode = o2.toString();
				return firstNode.compareTo(secondNode);
			}
		});
		controlsGroup.addMember(treeSelectField);
	}

	private void addTestSelectionControlMultiCustomOrder(FormGroup controlsGroup) {
		SelectField selectionControlMultiCustomOrder = FormFactory.newSelectField("selectionControlMultiCustomOrder", abcdOptions(), true, false);
		selectionControlMultiCustomOrder.setCustomOrder(true);
		controlsGroup.addMember(selectionControlMultiCustomOrder);
	}

	private void addTestSelectionControlMulti(FormGroup controlsGroup) {
		controlsGroup.addMember(FormFactory.newSelectField("selectionControlMulti", abcdOptions(), true,
			dabcSelection(), false));
	}

	private void addTestSelectionTextControlMultiFixed(FormGroup controlsGroup) {
		SelectField selection =
			FormFactory.newSelectField("selectionTextControlMultiFixed", abcdOptions(), true,
				cabSelection(), false);
		selection.setFixedOptions(new Filter<String>() {
			@Override
			public boolean accept(String anObject) {
				return anObject.contains("aaaa") || anObject.contains("dddd");
			}
		});
		selection.setCustomOrder(true);

		controlsGroup.addMember(selection);
	}

	private void addTestSelectionControlMultiFlipped(FormGroup controlsGroup) {
		SelectField selection =
			FormFactory.newSelectField("selectionControlMultiFlipped", abcdOptions(), true, false);
		setFlippedSelectDialogProvider(selection);
		controlsGroup.addMember(selection);
	}

	private List<String> abcOptions() {
		return Arrays.asList("a", "b", "c");
	}

	private List<String> abcdOptions() {
		return Arrays.asList("aaaa", "bbbb", "cccc", "dddd");
	}

	private List<String> dabcSelection() {
		// "Wrong" order to to test sorted option display.
		return Arrays.asList("dddd", "aaaa", "bbbb", "cccc");
	}

	private List<String> cabSelection() {
		// "Wrong" order to to test sorted option display. Without d to test fixed options.
		return Arrays.asList("cccc", "aaaa", "bbbb");
	}

	private List<Resource> getItemsWithTooltip() {
		ResPrefix item = I18NConstants.DROPDOWN_ITEM;
		return Arrays.asList(
			Resource.resource(item.key("01"), null, item.key("01.tooltip")),
			Resource.resource(item.key("02"), null, item.key("02.tooltip")),
			Resource.resource(item.key("03"), null, item.key("03.tooltip")),
			Resource.resource(item.key("04"), null, item.key("04.tooltip")),
			Resource.resource(item.key("05"), null, null),
			Resource.resource(item.key("06"), null, null),
			Resource.resource(item.key("07"), null, null),
			Resource.resource(item.key("08"), null, null),
			Resource.resource(item.key("09"), null, null),
			Resource.resource(item.key("10"), null, null),
			Resource.resource(item.key("11"), null, null),
			Resource.resource(item.key("12"), null, null),
			Resource.resource(item.key("13"), null, null),
			Resource.resource(item.key("14"), null, null),
			Resource.resource(item.key("15"), null, null),
			Resource.resource(item.key("16"), null, null),
			Resource.resource(item.key("17"), null, null),
			Resource.resource(item.key("18"), null, null),
			Resource.resource(item.key("19"), null, null),
			Resource.resource(item.key("20"), null, null),
			Resource.resource(item.key("21"), null, null),
			Resource.resource(item.key("22"), null, null),
			Resource.resource(item.key("23"), null, null),
			Resource.resource(item.key("24"), null, null),
			Resource.resource(item.key("25"), null, null),
			Resource.resource(item.key("26"), null, null),
			Resource.resource(item.key("27"), null, null),
			Resource.resource(item.key("28"), null, null),
			Resource.resource(item.key("29"), null, null),
			Resource.resource(item.key("30"), null, null),
			Resource.resource(item.key("31"), null, null),
			Resource.resource(item.key("32"), null, null),
			Resource.resource(item.key("33"), null, null),
			Resource.resource(item.key("34"), null, null));
	}

	private Resource megaMenuOption(ResKey label, ResKey tooltip) {
		return com.top_logic.layout.Resource.resourceFor(null, label, null, tooltip);
	}

	private Resource megaMenuOptionWithIcon(ResKey label, ThemeImage img, ResKey tooltip) {
		return com.top_logic.layout.Resource.resourceFor(null, label, img, tooltip);
	}

	private void addMegaMenuWithInvalidSelection(FormGroup megaMenuGroup, ResPrefix resPrefix) {
		List<com.top_logic.layout.Resource> options = Arrays
			.asList(
				megaMenuOption(I18NConstants.ADMINISTRATION, I18NConstants.ADMINISTRATION.tooltip()),
				megaMenuOption(I18NConstants.ACCESS_RIGHTS, I18NConstants.ACCESS_RIGHTS.tooltip()),
				megaMenuOption(I18NConstants.PERMISSIONS, I18NConstants.PERMISSIONS.tooltip()),
				megaMenuOption(I18NConstants.BASISADMINISTRATION, I18NConstants.BASISADMINISTRATION.tooltip()));
		SelectField field =
			FormFactory.newSelectField("megaMenuInvalidSelection", options, false, false);
		field.setAsSingleSelection(megaMenuOption(I18NConstants.DELETED_VIEW, null));
		field.setControlProvider(MegaMenuControl.CP.INSTANCE);

		megaMenuGroup.addMember(field);
	}

	private void addMegaMenuWithFixedOption(FormGroup megaMenuGroup, ResPrefix resPrefix) {
		List<com.top_logic.layout.Resource> options = Arrays
			.asList(
				megaMenuOptionWithIcon(I18NConstants.ADMINISTRATION, Icons.USER_ICON,
					I18NConstants.ADMINISTRATION.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.ACCESS_RIGHTS, Icons.USER_LOCK,
					I18NConstants.ACCESS_RIGHTS.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.PERMISSIONS, Icons.KEY,
					I18NConstants.PERMISSIONS.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.BASISADMINISTRATION, Icons.USER_CONFIG,
					I18NConstants.BASISADMINISTRATION.tooltip()));
		SelectField field =
			FormFactory.newSelectField("megaMenuFixedOptions", options, false, false);
		field.setFixedOptions(Arrays.asList(options.get(1), options.get(3)));
		field.setControlProvider(MegaMenuControl.CP.INSTANCE);

		megaMenuGroup.addMember(field);
	}

	private void addEmptyMandatoryMegaMenu(FormGroup megaMenu, ResPrefix resPrefix) {
		List<com.top_logic.layout.Resource> megaMenuSelectListSmallest = Arrays.asList();
		SelectField megaMenuEmptyMandatory =
			FormFactory.newSelectField("emptyMandatoryMegaMenu", megaMenuSelectListSmallest, false, true, false, null);
		megaMenu.addMember(megaMenuEmptyMandatory);
	}

	private void addSmallMegaMenu(FormGroup megaMenuGroup, ResPrefix resPrefix) {
		List<com.top_logic.layout.Resource> megaMenuSelectListSmallest = Arrays
			.asList(
				megaMenuOptionWithIcon(I18NConstants.ADMINISTRATION, Icons.USER_ICON,
					I18NConstants.ADMINISTRATION.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.ACCESS_RIGHTS, Icons.USER_LOCK,
					I18NConstants.ACCESS_RIGHTS.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.PERMISSIONS, Icons.KEY,
					I18NConstants.PERMISSIONS.tooltip()),
				megaMenuOption(I18NConstants.MONITOR, I18NConstants.MONITOR));
		SelectField megaMenuOptionsBiggest =
			FormFactory.newSelectField("smallestMegaMenuOptions", megaMenuSelectListSmallest, false, false);
		megaMenuOptionsBiggest.setEmptySelectionMegaMenu(I18NConstants.CUSTOMIZED_NO_OPTION_MEGAMENU);

		megaMenuGroup.addMember(megaMenuOptionsBiggest);
	}

	private void addBigMegaMenu(FormGroup megaMenuGroup, ResPrefix resPrefix) {
		List<com.top_logic.layout.Resource> megaMenuSelectListBig = Arrays
			.asList(
				megaMenuOptionWithIcon(I18NConstants.ADMINISTRATION, Icons.USER_ICON,
					I18NConstants.ADMINISTRATION.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.ACCESS_RIGHTS, Icons.USER_LOCK,
					I18NConstants.ACCESS_RIGHTS.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.PERMISSIONS, Icons.KEY,
					I18NConstants.PERMISSIONS.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.BASISADMINISTRATION, Icons.USER_CONFIG,
					I18NConstants.BASISADMINISTRATION.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.TECHNICAL_ADMINISTRATION, Icons.TOOLS,
					I18NConstants.TECHNICAL_ADMINISTRATION.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.MONITOR, Icons.DESKTOP,
					I18NConstants.MONITOR.tooltip()),
				megaMenuOptionWithIcon(I18NConstants.DEVELOPMENT, Icons.FILE_CODE,
					I18NConstants.DEVELOPMENT.tooltip()));

		SelectField megaMenuOptionsBiggest =
			FormFactory.newSelectField("bigMegaMenuOptions", megaMenuSelectListBig, false, true, false, null);
		megaMenuGroup.addMember(megaMenuOptionsBiggest);
	}

	private void addBiggestMegaMenu(FormGroup megaMenuGroup, ResPrefix resPrefix) {
		List<com.top_logic.layout.Resource> megaMenuSelectListBiggest = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			ResKey label = ResKey.text("Development " + i);
			megaMenuSelectListBiggest
				.add(com.top_logic.layout.Resource.resourceFor(null, label, null, I18NConstants.DEVELOPMENT.tooltip()));
		}
		SelectField megaMenuOptionsBiggest =
			FormFactory.newSelectField("biggestMegaMenuOptions", megaMenuSelectListBiggest, false, false);
		megaMenuGroup.addMember(megaMenuOptionsBiggest);
	}

	private void addTestSelectionControlMultiTree(FormGroup controlsGroup) {
		TLTreeModel options = createOptionTree();
		SelectField selection = FormFactory.newSelectField("selectionControlMultiTree", options, true, false);
		selection.setOptionComparator(new TreeNodeComparator(options, new UserObjectNodeComparator(
			ComparableComparator.INSTANCE)));
		controlsGroup.addMember(selection);
	}

	private void addTestSelectionControlMultiTreeFlipped(FormGroup controlsGroup) {
		TLTreeModel options = createOptionTree();
		SelectField selection = FormFactory.newSelectField("selectionControlMultiTreeFlipped", options, true, false);
		selection.setOptionComparator(new TreeNodeComparator(options, new UserObjectNodeComparator(
			ComparableComparator.INSTANCE)));
		setFlippedSelectDialogProvider(selection);
		controlsGroup.addMember(selection);
	}

	private void addTestSelectionControlMultiTreeUnordered(FormGroup controlsGroup) {
		// See Ticket #3546: Automatische Sortierung in SelectField lässt sich nicht ausschalten.
		controlsGroup.addMember(FormFactory.newSelectField("selectionControlMultiTreeUnordered", createOptionTree(), true, false));
	}

	private void addTestSelectionControlNoAutoComplete(FormGroup controlsGroup) {
		List<String> options =
			Arrays.asList("aaa", "aaab", "aaac", "aaad", "aabc", "aabb", "aabg", "aabh", "aabz", "abcd", "abcf",
				"abct", "abcz", "abcgh", "bbb");
		SelectField field = FormFactory.newSelectField("selectionControlNoAutoComplete", options);
		controlsGroup.addMember(field);
	}

	private void addTestSelectionControl(FormGroup controlsGroup) {
		controlsGroup.addMember(FormFactory.newSelectField("selectionControl", createOptionList()));
	}

	private void addDelayedDblClickTestSelectionControl(FormGroup controlsGroup) {
		SelectField newSelectField = FormFactory.newSelectField("selectionControlDelayedDblClick", createOptionList());
		newSelectField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					Logger.error("Delayed double click has been bypassed!", ex, this);
				}
			}
		});
		controlsGroup.addMember(newSelectField);
	}

	private void addTestSelectionControlFixed(FormGroup controlsGroup) {
		SelectField selection = FormFactory.newSelectField("selectionControlFixed", createOptionList());
		selection.setFixedOptions(new Filter<String>() {
			@Override
			public boolean accept(String anObject) {
				return anObject.contains("2") || anObject.contains("5");
			}
		});
		controlsGroup.addMember(selection);
	}

	private void addTestSelectionControlFlipped(FormGroup controlsGroup) {
		SelectField selection = FormFactory.newSelectField("selectionControlFlipped", createOptionList());
		setFlippedSelectDialogProvider(selection);
		controlsGroup.addMember(selection);
	}

	private void setFlippedSelectDialogProvider(SelectField selection) {
		boolean direction = !SelectDialogProvider.newDefaultConfig().isLeftToRight();
		selection.setSelectDialogProvider(createDefaultSelectDialogProvider(direction));
	}

	private List<String> createOptionList() {
		List<String> options = new ArrayList<>();
		options.add("Name with  multiple   spaces");
		options.add("   Name with leading and tailing spaces   ");
		options.add("\r\nName with leading and tailing blanks\r\n");
		for(int i = 0; i < 15; i++) {
			options.add("aab" + i + "xxx");
		}
		return options;
	}

	private void addTestSelectionControlTree(FormGroup controlsGroup) {
		// See Ticket #3959: Baum-Selektion für SelectField mit Einfachselektion.
		SelectField selection = createSingleSelectField("selectionControlTree", null);
		controlsGroup.addMember(selection);
	}

	private void addTestSelectionControlTreeAllExpanded(FormGroup controlsGroup) {
		SelectField selectField = createSingleSelectField("selectionControlTreeAllExpanded", null);
		SelectDialogConfig dialogConfig = TypedConfiguration.newConfigItem(SelectDialogConfig.class);
		TypedConfigUtil.setProperty(dialogConfig, SelectDialogConfig.INITIAL_TREE_EXPANSION_DEPTH, SelectDialogConfig.SHOW_ALL_NODES);
		selectField.setSelectDialogProvider(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(dialogConfig));
		controlsGroup.addMember(selectField);
	}

	/**
	 * Returns a new {@link DefaultSelectDialogProvider} which displays the options on the demanded side
	 * 
	 * @param leftToRight
	 *        whether the options must be displayed on the right side
	 */
	public static SelectDialogProvider createDefaultSelectDialogProvider(boolean leftToRight) {
		SelectDialogConfig config = TypedConfiguration.newConfigItem(SelectDialogConfig.class);
		// Default instance shows options on left side
		if (!leftToRight) {
			config.setLeftToRight(leftToRight);
		}
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private void addTestSelectionControlTreeCustomLabel(FormGroup controlsGroup) {
		// See Ticket #3959: Baum-Selektion für SelectField mit Einfachselektion.
		SelectField selection = createSingleSelectField("selectionControlTreeCustomLabel", new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				return MetaResourceProvider.INSTANCE.getLabel(object) + "-customLabel";
			}
		});
		controlsGroup.addMember(selection);
	}

	private SelectField createSingleSelectField(String selectFieldName, LabelProvider optionLabelProvider) {
		TLTreeModel options = createOptionTree();
		List<?> fixedOptions = createFixedOptions(options);
		SelectField selectField = FormFactory.newSelectField(selectFieldName, options);
		selectField.getOptionsAsTree().setSelectableOptionsFilter(new SelectionModelFilter(fixedOptions));
		if (optionLabelProvider != null) {
			selectField.setOptionLabelProvider(optionLabelProvider);
		}
		return selectField;
	}

	private List<?> createFixedOptions(TLTreeModel<?> options) {
		TLTreeNode root = (TLTreeNode) options.getRoot();
		List<TLTreeNode> fixedNodes = new ArrayList<>();
		fixedNodes.add(root.getChildAt(0));
		fixedNodes.add(root.getChildAt(2));
		return fixedNodes;
	}

	private TLTreeNode createInvalidNode(AbstractMutableTLTreeModel options) {
		TLTreeNode root = (TLTreeNode) options.getRoot();
		return new DefaultMutableTLTreeNode(options, (DefaultMutableTLTreeNode) root.getChildAt(0), "A1.5-invalid");
	}

	private void addTestSelectionControlTreeFlipped(FormGroup controlsGroup) {
		SelectField selection = createSingleSelectField("selectionControlTreeFlipped", null);
		setFlippedSelectDialogProvider(selection);
		controlsGroup.addMember(selection);
	}

	private DefaultMutableTLTreeModel createOptionTree() {
		DefaultMutableTLTreeModel options = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode root = options.getRoot();
		root.setBusinessObject("Root");

		DefaultMutableTLTreeNode a1 = root.createChild("A1");
		DefaultMutableTLTreeNode a11 = a1.createChild("A1.1");
		DefaultMutableTLTreeNode a12 = a1.createChild("A1.2");
		DefaultMutableTLTreeNode a121 = a12.createChild("A1.2.1");
		DefaultMutableTLTreeNode a122 = a12.createChild("A1.2.2");
		DefaultMutableTLTreeNode a13 = a1.createChild("A1.3");
		DefaultMutableTLTreeNode a14 = a1.createChild("A1.4");
		DefaultMutableTLTreeNode a2 = root.createChild("A2");
		DefaultMutableTLTreeNode a3 = root.createChild("A3");
		DefaultMutableTLTreeNode a31 = a3.createChild("A3.1");
		DefaultMutableTLTreeNode a32 = a3.createChild("A3.2");

		return options;
	}

	private DefaultMutableTLTreeModel createOptionTreeInfinite() {
		String rootUserObject = "Root";
		class InfinitTree extends AbstractTLTreeNodeBuilder {

			@Override
			public boolean isFinite() {
				return false;
			}

			@Override
			public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
				List<DefaultMutableTLTreeNode> children = new ArrayList<>();
				if (rootUserObject.equals(node.getBusinessObject())) {
					for (int i = 1; i < 4; i++) {
						children.add(createNode(node.getModel(), node, "A" + i));
					}
				} else {
					for (int i = 1; i < 4; i++) {
						children.add(createNode(node.getModel(), node, node.getBusinessObject() + "." + i));
					}
				}
				return children;
			}

		}
		DefaultMutableTLTreeModel options = new DefaultMutableTLTreeModel(new InfinitTree(), rootUserObject);
		return options;
	}

	/**
	 * This method creates a Command which simply opens an alert.
	 * 
	 * @param message
	 *        the message of the alert.
	 */
	public static Command createAlertCommand(String message) {
		final JSSnipplet alert = JSSnipplet.createAlert(message);
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				context.getWindowScope().getTopLevelFrameScope().addClientAction(alert);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	/**
	 * adds fields to show usage of {@link FolderField} and
	 * {@link FolderControl}
	 * 
	 * @param container
	 *        the container to add creates fields
	 */
	private void addFolderFields(FormContainer container) {
		if (this.webFolderID != null) {
			WebFolder webFolder =
					(WebFolder) WrapperFactory.getWrapper(ObjectLabel.getLabeledObject(this.webFolderID));
			assert webFolder != null : "Static webfolder not found.";
			container.addMember(FolderField.createFolderField("folderField", webFolder));
		}
	}

	private void addDataFields(FormContext context) {
		FormGroup uploadGroup = new FormGroup("dataFieldGroup", I18NConstants.CONTROLS);
		uploadGroup.setCollapsed(true);
		final DataField uploadForDownloadField = FormFactory.newDataField("uploadForDownload");
		uploadForDownloadField.setControlProvider(createDownloadControlProvider());
		uploadGroup.addMember(uploadForDownloadField);
		final DataField uploadField = FormFactory.newDataField("upload");
		uploadField.setFileNameConstraint(XML_FILENAME_CONSTRAINT);
		uploadGroup.addMember(uploadField);
		final DataField justDownloadField = FormFactory.newDataField("justDownloads");
		justDownloadField.setReadOnly(true);
		uploadGroup.addMember(justDownloadField);
		final DataField uploadField2 = FormFactory.newDataField("uploadAndSetDataToOtherUpload");
		uploadGroup.addMember(uploadField2);
		uploadField2.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (newValue != null) {
					uploadField.setValue(newValue);
				}
				justDownloadField.setValue(newValue);
				uploadForDownloadField.setValue(newValue);
			}
			
		});
		uploadGroup.addMember(FormFactory.newDataField("upload3DownloadInline"));
		final DataField individualNoValueText = FormFactory.newDataField("upload5IndividualNoValueText");
		individualNoValueText.setNoValueResourceKey(I18NConstants.NO_VALUE);
		uploadGroup.addMember(individualNoValueText);
		uploadGroup.addMember(FormFactory.newCommandField("toggleReadOnly", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				individualNoValueText.setReadOnly(!individualNoValueText.isReadOnly());
				return HandlerResult.DEFAULT_RESULT;
			}
		}));
		context.addMember(uploadGroup);
		
		final DataField emptyFileWithConstraint = FormFactory.newDataField("upload7AllowEmptyFileWithNotEmptyConstraint");
		emptyFileWithConstraint.addConstraint(new AbstractConstraint() {

			@Override
			public boolean check(Object value) throws CheckException {
				if (value != null && ((BinaryDataSource) value).getSize() == 0) {
					throw new CheckException("No empty files allowed");
				}
				return true;
			}
		});
		uploadGroup.addMember(emptyFileWithConstraint);

		uploadGroup.addMember(FormFactory.newDataField("upload6AllowEmptyFile"));
		
		try {
			BinaryData dataItem = getDataItem("/images/test/BOS Buero.jpg");

			final DataField immutable = FormFactory.newDataField("uploadImmutable");
			immutable.setValue(dataItem);
			immutable.setImmutable(true);
			uploadGroup.addMember(immutable);
			final DataField immutableDisabledDownload = FormFactory.newDataField("uploadImmutableDisabledDownload");
			immutableDisabledDownload.setValue(dataItem);
			immutableDisabledDownload.setImmutable(true);
			immutableDisabledDownload.setDownload(false);
			uploadGroup.addMember(immutableDisabledDownload);

		} catch (IOException ex) {
			throw new IOError(ex);
		}

		uploadGroup.addMember(FormFactory.newDataField("multiUpload", FormFactory.MULTIPLE));
	}

	private void addSelectionPartControlFieldsSingle(FormContext context) {
		DefaultSingleSelectionModel selectionModel = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		addSelectionPartControlFields(context, "selectionPartSingle", selectionModel);
	}

	private void addSelectionPartControlFieldsMultiple(FormContext context) {
		DefaultMultiSelectionModel selectionModel = new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER);
		addSelectionPartControlFields(context, "selectionPartMultiple", selectionModel);
	}

	private void addSelectionPartControlFields(FormContext context, String name, final SelectionModel selectionModel) {
		FormGroup testGroup = new FormGroup(name, I18NConstants.CONTROLS);
		context.addMember(testGroup);

		final StringField selectionDisplay = FormFactory.newStringField("selectionDisplay");
		testGroup.addMember(selectionDisplay);

		final List<String> allElements = Arrays.asList("A", "B", "C", "D");
		for (String partName : allElements) {
			final String currentPartName = partName;
			HiddenField partA = FormFactory.newHiddenField("part" + currentPartName);
			partA.setControlProvider(new ControlProvider() {
				@Override
				public Control createControl(Object model, String style) {
					return new SelectionPartControl(selectionModel, currentPartName);
				}
			});
			testGroup.addMember(partA);
		}

		final BooleanFlag displayUpdateDisabled = new BooleanFlag(false);
		final BooleanFlag selectionUpdateDisabled = new BooleanFlag(false);

		SelectionListener displayUpdate = new SelectionListener() {
			@Override
			public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
				if (displayUpdateDisabled.get()) {
					return;
				}

				selectionUpdateDisabled.set(true);
				try {
					ArrayList sorted = new ArrayList(selectedObjects);
					Collections.sort(sorted);
					selectionDisplay.setValue(StringServices.join(sorted, ", "));
				} finally {
					selectionUpdateDisabled.set(false);
				}
			}
		};
		selectionModel.addSelectionListener(displayUpdate);

		ValueListener selectionUpdate = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (selectionUpdateDisabled.get()) {
					return;
				}

				displayUpdateDisabled.set(true);
				try {
					String newSelectionString = (String) newValue;
					HashSet<String> newSelected =
						new HashSet<>(Arrays.asList(newSelectionString.split("\\s*,\\s*")));
					newSelected.retainAll(allElements);

					Set currentSelection = selectionModel.getSelection();

					HashSet removedSelected = new HashSet(currentSelection);
					removedSelected.removeAll(newSelected);

					newSelected.removeAll(currentSelection);
					for (String newPart : newSelected) {
						selectionModel.setSelected(newPart, true);
					}
					for (Object removedPart : removedSelected) {
						selectionModel.setSelected(removedPart, false);
					}
				} finally {
					displayUpdateDisabled.set(false);
				}
			}
		};
		selectionDisplay.addValueListener(selectionUpdate);
	}

	/**
	 * Creates a {@link ControlProvider} for {@link DataField}.
	 * 
	 * The resulting control works as follows: If the {@link DataField} has no value a
	 * {@link DataItemControl} is displayed. Otherwise a {@link DownloadControl} is displayed
	 */
	private ControlProvider createDownloadControlProvider() {
		return new ControlProvider() {
			
			@Override
			public Control createControl(Object model, String style) {
				final DataField dataField = (DataField) model;

				Map<String, ControlCommand> commands = Collections.<String, ControlCommand> emptyMap();
				final AbstractControl result = new AbstractVisibleControl(commands) {

					@Override
					public Object getModel() {
						return null;
					}

					@Override
					protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(SPAN);
						writeControlAttributes(context, out);
						out.endBeginTag();
						writeContent(dataField, context, out);
						out.endTag(SPAN);
					}

					private void writeContent(final DataField f, DisplayContext context, TagWriter out)
							throws IOException {
						BinaryData dataItem = f.getDataItem();
						if (dataItem == null) {
							new DataItemControl(f).write(context, out);
						} else {
							new DownloadControl(dataItem).write(context, out);
						}
					}
				};
				dataField.addValueListener(new ValueListener() {

					@Override
					public void valueChanged(FormField field, Object oldValue, Object newValue) {
						result.requestRepaint();
					}
				});
				return result;
			}
		};
	}

	private ResourceProvider images(final Map<?, ThemeImage> images) {
		return new DefaultResourceProvider() {
			@Override
			public ThemeImage getImage(Object object, Flavor aFlavor) {
				return images.get(object);
			}
		};
	}

	private void addDependencies(FormContainer container) {
		final Calendar calendar = CalendarUtil.createCalendar();
		calendar.set(2009, 1, 10);
		final Date startDate = calendar.getTime();
		calendar.add(Calendar.YEAR, 1);
		final Date endDate = calendar.getTime();
		final ComplexField start = FormFactory.newDateField("start", startDate, false);
		final ComplexField end = FormFactory.newDateField("end", endDate, false);
		ComparisonDependency.buildStartEndDependency(start, end);
		container.addMember(start);
		container.addMember(end);
	}

	private SelectField createFixedOptionSelectField(String fieldName, boolean fixed, boolean large, boolean multi, boolean customOrder) {
		List options = createOptionList(large);
		SelectField result = FormFactory.newSelectField(fieldName, options, multi, false);
		result.setCustomOrder(customOrder);
		if (fixed) {
			result.setAsSelection(createSelectValues(16, 28));
			result.setFixedOptions(FILTER_1);
		}
		return result;
	}

	private SelectField createFixedOptionSelectFieldWithFixedSelection(String fieldName, boolean multi,
			boolean customOrder) {
		List options = Arrays.asList(new String[] { "1", "3", "5", "7", "9", "11", "17" });
		SelectField result = FormFactory.newSelectField(fieldName, options, multi, false);
		result.setCustomOrder(customOrder);
		if (multi) {
			result.setAsSelection(Arrays.asList(new String[] { "7", "17" }));
		}
		else {
			result.setAsSelection(Arrays.asList(new String[] { "7" }));
		}
		result.setFixedOptions(FILTER_1);
		return result;
	}

	private List createOptionList(boolean large) {
		final int optionCount = large ? 5000 : 200;
		return new LazyListUnmodifyable() {
			@Override
			protected List initInstance() {
				int start = 0;
				int stop = optionCount;
				return createSelectValues(start, stop);
			}
		};
	}

	/*package protected*/ static List createSelectValues(int start, int stop) {
		ArrayList result = new ArrayList(stop - start);
		for (int n = start; n < stop; n++) {
			result.add(Integer.toString(n));
		}
		return result;
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	/**
	 * Must react on on changes of the displayed {@link WebFolder}
	 * 
	 * @see #addFolderFields(FormContainer)
	 */
	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
		WebFolderUtils.updateWebfolder(this, aModel);
		return super.receiveModelChangedEvent(aModel, changedBy);
	}

	static void updateLabel(FormField field, boolean addPrefix, String prefix) {
		if (addPrefix) {
			field.setLabel(prefix + field.getLabel());
		} else {
			int idx = field.getLabel().indexOf(prefix);
			if (idx >= 0) {
				field.setLabel(field.getLabel().substring(0, idx) + field.getLabel().substring(idx + prefix.length()));
			}
		}
	}
	
	/**
	 * Component shown in dialog. Only creates a command field to go to
	 * {@link PersonManager#getCurrentPerson() current person}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class GotoPersonComponent extends SimpleFormComponent {

		private static final Property<LayoutComponent> COMPONENT = TypedAnnotatable.property(LayoutComponent.class, "component");
		
		public GotoPersonComponent(InstantiationContext context, Config atts) throws ConfigurationException {
			super(context, atts);
		}

		@Override
		public FormContext createFormContext() {
			FormContext ctx = new FormContext(this);
			
			CommandField gotoField = new CommandField("gotoField") {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					final LayoutComponent surroundingComponent = get(COMPONENT);
					surroundingComponent.gotoTarget(PersonManager.getManager().getCurrentPerson());
					return HandlerResult.DEFAULT_RESULT;

				}
			};
			gotoField.set(COMPONENT, this);
			ctx.addMember(gotoField);
			return ctx;
		}

	}

	/**
	 * Workaround for JavaC in Linux: During nightly build all JSPs are compiled. Especially the JSP
	 * TestControlsForm.jsp which includes this component. If the Provider is defined inline the
	 * compiler of the JSP does not finish its work.
	 * 
	 * @see "Ticket 17323"
	 * 
	 * @since 5.7.5
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	abstract class PopupMenuFieldProvider implements Provider<List<List<CommandModel>>> {

	}

}
