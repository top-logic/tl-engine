/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.text.DateFormat;

import com.top_logic.base.chart.ImageControl;
import com.top_logic.basic.Logger;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.FormModeModelAdapter;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.BooleanChoiceControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.CheckboxControl;
import com.top_logic.layout.form.control.ColorChooserControl;
import com.top_logic.layout.form.control.DataItemControl;
import com.top_logic.layout.form.control.DateInputControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.ExpandableTextInputControl;
import com.top_logic.layout.form.control.IconInputControl;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.control.PasswordInputControlProvider;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.format.ThemeImageFormat;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.ExpandableStringField;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.form.model.FormArray;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.ImageField;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.PasswordField;
import com.top_logic.layout.form.model.PopupMenuField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectionTableField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.TreeField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.layout.image.gallery.GalleryControl;
import com.top_logic.layout.list.ListControl;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeData;

/**
 * {@link ControlProvider} that translates {@link FormMember}s to compatible
 * {@link Control}s for display.
 * 
 * <p>
 * The way, {@link FormMember}s are translated into {@link Control}s can be
 * customized by subclasses by overriding the corresponding
 * {@link FormMemberVisitor visit choice}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFormFieldControlProvider extends AbstractFormFieldControlProvider
		implements FormMemberVisitor<Control, Void> {

	/**
	 * This method returns an {@link AbstractCompositeControl} which updates its visibility
	 * depending on the visibility of the given {@link FormMember}.
	 * 
	 * @param member
	 *        the member which is use to control the visibility of the returned {@link Control}.
	 */
	public static OnVisibleControl createOnVisibleControl(final FormMember member) {
		return new OnVisibleControl(member);
	}
	
	private static final TagTemplate DEFAULT_COMPOSITE_TEMPLATE = div(items(self()));

	/**
	 * Default {@link DefaultFormFieldControlProvider} instance.
	 */
	public static final ControlProvider INSTANCE = new DefaultFormFieldControlProvider();
	
	private final boolean preferRadio;
	
	/**
	 * Creates a {@link DefaultFormFieldControlProvider}.
	 */
    public DefaultFormFieldControlProvider() {
        this(false);
    }

	/**
	 * Creates a {@link DefaultFormFieldControlProvider}.
	 *
	 * @param preferRadio
	 *        See {@link #isPreferRadio()}.
	 */
    public DefaultFormFieldControlProvider(boolean preferRadio) {
		this.preferRadio = preferRadio;
	}

	/**
	 * Whether radio buttons are preferred over check boxes for booleans.
	 */
	public boolean isPreferRadio() {
		return preferRadio;
	}

	@Override
	public Control visitBooleanField(BooleanField member, Void arg) {
		if (preferRadio) {
			return new BooleanChoiceControl(member);
		} else {
			return new CheckboxControl(member);
		}
	}

	@Override
	public Control visitFormContext(FormContext member, Void arg) {
		return visitFormGroup(member, arg);
	}

	@Override
	public Control visitFormArray(FormArray member, Void arg) {
		return visitFormContainer(member, arg);
	}

	@Override
	public Control visitFormGroup(FormGroup member, Void arg) {
		return visitFormContainer(member, arg);
	}

	@Override
	public Control visitIntField(IntField member, Void arg) {
		return visitFormField(member, arg);
	}

	@Override
	public Control visitSelectionTableField(SelectionTableField member, Void arg) {
		return visitFormField(member, arg);
	}

	@Override
	public Control visitStringField(StringField member, Void arg) {
		return visitFormField(member, arg);
	}

	@Override
	public Control visitPasswordField(PasswordField member, Void arg) {
		return PasswordInputControlProvider.createPasswordControl(member, 0, 0, 0);
	}

	@Override
	public Control visitFormField(FormField member, Void arg) {
		return new TextInputControl(member);
	}

	@Override
	public Control visitHiddenField(HiddenField member, Void arg) {
		return null;
	}

	@Override
	public Control visitCommandField(CommandField member, Void arg) {
		return new ButtonControl(member);
	}
	
	@Override
	public Control visitPopupMenuField(PopupMenuField member, Void arg) {
		return new PopupMenuButtonControl(member);
	}

	@Override
	public Control visitListField(ListField member, Void arg) {
		return new ListControl(member);
	}
	
	@Override
	public Control visitFolderField(FolderField member, Void arg) {
		return WebFolderUIFactory.createControl(member);
	}

	@Override
	public Control visitSelectField(SelectField member, Void arg) {
		if (member.isMultiple()) {
			return new SelectionControl(member);
		} else {
			return new DropDownControl(member);
		}
	}

	@Override
	public Control visitComplexField(ComplexField member, Void arg) {
		if (member.getFormat() instanceof DateFormat) {
			return new DateInputControl(member);
		} else if (member.getFormat() instanceof ColorFormat) {
			return new ColorChooserControl(member);
		} else if (member.getFormat() instanceof ThemeImageFormat) {
			return new IconInputControl(member);
		}
		
		return visitFormField(member, arg);
	}
	
	@Override
	public Control visitImageField(ImageField member, Void arg) {
		return new ImageControl(member);
	}

	@Override
	public Control visitGalleryField(GalleryField member, Void arg) {
		return new GalleryControl(member, new FormModeModelAdapter(member), !member.isImmutable());
	}

	@Override
	public Control visitExpandableStringField(ExpandableStringField member, Void arg) {
        return new ExpandableTextInputControl(member);
    }

	@Override
	public Control visitFormContainer(final FormContainer member, Void arg) {
		return new TemplateControl(member, this, DEFAULT_COMPOSITE_TEMPLATE);
	}
	
	@Override
	public Control visitFormMember(FormMember member, Void arg) {
		Logger.error("Unimplemented choice: " + member.getClass(), this);
		return null;
	}
	
	@Override
	public Control visitDataField(DataField member, Void arg) {
		return new DataItemControl(member);
	}
	
	@Override
	public Control visitFormTree(FormTree member, Void arg) {
		return visitTreeData(member, member, arg);
	}
	
	@Override
	public Control visitTreeField(TreeField treeField, Void arg) {
		return visitTreeData(treeField, treeField, arg);
	}
	
	/**
	 * Common visit case for {@link TreeField}s and {@link FormTree}s.
	 * 
	 * @param arg
	 *        Nonsense argument.
	 */
	protected Control visitTreeData(FormMember member, TreeData data, Void arg) {
		OnVisibleControl result = createOnVisibleControl(member);
		result.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
		result.addChild(new TreeControl(data));
		return result;
	}

    @Override
	public Control visitTableField(TableField aMember, Void arg) {
		return TableTag.createTableControl(aMember);
    }
    
	/**
	 * Creates the default view for the given {@link FormMember} based on the field type.
	 */
	@Override
	protected Control createInput(FormMember member) {
		return member.visit(this, null);
	}

}
