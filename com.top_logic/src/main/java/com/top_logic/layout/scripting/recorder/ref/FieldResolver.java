/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ref.field.AttributeFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.BusinessObjectFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.FieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.LabeledFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.NamedFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.TableFieldRef;
import com.top_logic.layout.scripting.recorder.ref.field.TreeFieldRef;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeRef;
import com.top_logic.layout.scripting.recorder.ref.value.StringValue;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;

/**
 * {@link FieldRefVisitor} that resolves {@link FieldRef}s to {@link FormMember}s.
 * 
 * <p>
 * The argument to the visit is the context {@link FormContainer} within which the visited
 * {@link FieldRef} is resolved.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldResolver implements FieldRefVisitor {

	private FormMember _valueContext;

	private final ActionContext actionContext;

	/**
	 * Creates a {@link FieldResolver} for the given {@link ActionContext}, which is needed to
	 * resolve {@link ValueRef}s.
	 */
	public FieldResolver(ActionContext actionContext) {
		this.actionContext = actionContext;
	}

	@Override
	public Object visitLabeledFieldRef(LabeledFieldRef ref, Object arg) {
		String label = ref.getLabel();
		return findFieldByLabel(container(arg), label);
	}

	private FormMember findFieldByLabel(FormContainer container, String label) {
		SearchResult<FormMember> result = new SearchResult<>();
		findFieldByLabel(container, label, result);
		return result.getSingleResult("Failed to find field with label '" + label + "' in '" + container + "'.");
	}

	private void findFieldByLabel(FormContainer container, String label, SearchResult<FormMember> result) {
		Iterator<? extends FormMember> members = container.getMembers();
		while (members.hasNext()) {
			FormMember member = members.next();

			String candidateLabel = getCandidateLabel(member);
			if (candidateLabel != null) {
				result.addCandidate(candidateLabel);

				if (label.equals(candidateLabel)) {
					result.add(member);
				}
			}

			if (member instanceof FormContainer) {
				findFieldByLabel(((FormContainer) member), label, result);
			}
		}
	}

	private String getCandidateLabel(FormMember member) {
		if (!member.hasLabel()) {
			return null;
		}
		return StringServices.nonEmpty(Resources.getInstance().getStringOptional(member.getLabel()));
	}

	@Override
	public Object visitAttributeFieldRef(AttributeFieldRef ref, Object arg) {
		AttributeRef attributeRef = ref.getAttributeRef();
		try {
			Object self = resolveValue(ref.getSelfRef());
			TLStructuredTypePart part = resolveAttributeRev(attributeRef);

			Class<?> MetaAttributeGUIHelper = Class.forName("com.top_logic.element.meta.gui.MetaAttributeGUIHelper");
			Method getAttributeID =
				MetaAttributeGUIHelper.getMethod("getAttributeID", new Class[] { TLStructuredTypePart.class,
					TLObject.class });

			String fieldName = (String) getAttributeID.invoke(null, new Object[] { part, self });
			
			return container(arg).getMember(fieldName);
		} catch (Exception ex) {
			throw ApplicationAssertions.fail(ref, "Field for attribute '" + attributeRef.getAttributeName()
				+ "' not found.", ex);
		}
	}

	private TLStructuredTypePart resolveAttributeRev(AttributeRef attributeRef) {
		ModelName typeRef = attributeRef.getType();
		TLClass type = (TLClass) resolveValue(typeRef);
		if (type == null) {
			throw ApplicationAssertions.fail(typeRef, "Type '" + typeRef + "' not found.");
		}
		TLStructuredTypePart part = type.getPart(attributeRef.getAttributeName());
		if (part == null) {
			throw ApplicationAssertions.fail(typeRef, "Part '" + attributeRef.getAttributeName()
				+ "' not found in '" + TLModelUtil.qualifiedName(type) + "'.");
		}
		return part;
	}

	@Override
	public Object visitNamedFieldRef(NamedFieldRef ref, Object arg) {
		return container(arg).getMember(ref.getFieldName());
	}

	@Override
	public Object visitTableFieldRef(TableFieldRef tableFieldRef, Object tableGroupContainer) {
		try {
			TableModel tableModel = getTableModel((FormContainer) tableGroupContainer);
			Object resolvedRowObject =
				ModelResolver.locateModel(actionContext, tableModel, tableFieldRef.getRowObject());

			// Check that the resolved row object is in deed a valid row of the underlying table
			// model.
			int row = tableModel.getRowOfObject(resolvedRowObject);
			if (row < 0) {
				ApplicationAssertions.fail(null, "Row object in table model not found: " + resolvedRowObject);
			}

			String columnName = tableFieldRef.getColumnName();
			return tableModel.getValueAt(row, columnName);
		} catch (Exception exception) {
			throw (AssertionError) new AssertionError("Table field resolution failed.").initCause(exception);
		}
	}

	private TableModel getTableModel(FormContainer tableGroupContainer) {
		assert tableGroupContainer.hasStableIdSpecialCaseMarker() : "The tableGroupContainer has no stableIdSpecialCaseMarker set that would give me the FormTableModel I need.";
		Object model = tableGroupContainer.getStableIdSpecialCaseMarker();
		assert model instanceof TableModel : "Expected a table model but got: '" + model + "'";
		return (TableModel) model;
	}

	@Override
	public FormContainer visitTreeFieldRef(TreeFieldRef treeFieldRef, Object arg) {
		FormTree tree = tree(arg);
		Object node;
		List<ModelName> path = treeFieldRef.getPath();
		TreeUIModel treeModel = tree.getTreeApplicationModel();
		ResourceProvider labelProvider = ScriptingUtil.getResourceProvider(tree);
		/* Its just an heuristic, that a path consisting only of StringValues is a label path. It
		 * might be an ValuePath as well, but that cannot be detected afterwards. */
		if (CollectionUtil.containsOnly(StringValue.class, path)) {
			List<String> labelPath = resolveCollection(path, String.class);
			node = ScriptingUtil.findNodeByLabelPath(labelPath, treeModel, labelProvider);
		} else {
			node = ScriptingUtil.findNodeByValuePath(path, treeModel, labelProvider, actionContext);
		}
		return (FormContainer) tree.getTreeModel().getUserObject(node);
	}

	private FormTree tree(Object arg) {
		return (FormTree) arg;
	}

	private FormContainer container(Object arg) {
		return (FormContainer) arg;
	}

	/**
	 * Resolve the {@link FormMember} represented by the path in the given {@link FormContext}.
	 */
	public FormMember resolveFormMember(FormContext context, List<? extends FieldRef> path) {
		FormMember member = context;
		for (FieldRef fieldRef : path) {
			member = resolveMember(member, fieldRef);
		}
		return member;
	}

	/**
	 * Resolves the {@link FormMember} identified by the given {@link FieldRef} in the given parent
	 * {@link FormMember}.
	 */
	public FormMember resolveMember(FormMember parent, FieldRef fieldRef) {
		_valueContext = parent;
		return (FormMember) fieldRef.visit(this, parent);
	}

	@Override
	public Object visitSubstituteIdFieldRef(BusinessObjectFieldRef value, Object arg) {
		Object substituteId = resolveValue(value.getBusinessObject());
		return findObjectMeantBySubstituteId((FormContainer) arg, substituteId);
	}

	private FormMember findObjectMeantBySubstituteId(FormContainer container, Object substituteId) {
		SearchResult<FormMember> result = new SearchResult<>();
		for (FormMember formMember : CollectionUtil.toIterable(container.getMembers())) {
			result.addCandidate(getCandidateSubstituteId(formMember));
			if (isMeantBySubstituteId(formMember, substituteId)) {
				result.add(formMember);
			}
		}
		return result.getSingleResult("Could not find object with substitute-id '" + substituteId + "' in container '"
			+ container + "'.");
	}

	private Object getCandidateSubstituteId(FormMember child) {
		if (!child.hasStableIdSpecialCaseMarker()) {
			return "[No substitute-id: " + child.getName() + "]";
		}
		return child.getStableIdSpecialCaseMarker();
	}

	private boolean isMeantBySubstituteId(FormMember formMember, Object substituteId) {
		return formMember.hasStableIdSpecialCaseMarker()
			&& formMember.getStableIdSpecialCaseMarker().equals(substituteId);
	}

	private Object resolveValue(ModelName name) {
		return ModelResolver.locateModel(actionContext, _valueContext, name);
	}

	private <T> List<T> resolveCollection(Collection<? extends ModelName> names, Class<T> type) {
		return ModelResolver.locateModels(actionContext, type, _valueContext, names);
	}

}
