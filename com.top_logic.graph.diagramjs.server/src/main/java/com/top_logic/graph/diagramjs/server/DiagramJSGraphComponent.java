/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.remote.listener.AttributeListener;
import com.top_logic.element.layout.meta.TLEnumerationFormBuilder;
import com.top_logic.element.layout.meta.TLPropertyFormBuilder.PropertyModel;
import com.top_logic.element.layout.meta.TLReferenceFormBuilder.ReferenceModel;
import com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.EditModel;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.PartModel;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.model.DiagramJSGraphModel;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSGraphModel;
import com.top_logic.graph.diagramjs.server.commands.CreateClassCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateEnumerationCommand;
import com.top_logic.graph.diagramjs.server.handler.DiagramHandler;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.graph.diagramjs.server.util.layout.Bounds;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritance;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritanceImpl;
import com.top_logic.graph.diagramjs.util.GraphLayoutConstants;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.TechnicalNamesLabelProvider;
import com.top_logic.graph.server.component.builder.GraphModelBuilder;
import com.top_logic.graph.server.model.DefaultGraphData;
import com.top_logic.graph.server.model.GraphData;
import com.top_logic.graph.server.ui.AbstractGraphComponent;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * Component to display a dynamic automated layouted graph using library <code>UmlJS</code>.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DiagramJSGraphComponent extends AbstractGraphComponent implements DiagramHandler, Selectable {

	/**
	 * Graph component configuration.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractGraphComponent.Config, Selectable.SelectableConfig {

		/**
		 * Name of form component inside the dialog to create an association.
		 */
		public static final String CREATE_TYPE_PART_FORM_NAME = "createTypePartFormName";

		/**
		 * Name of dialog component to create an association.
		 */
		public static final String CREATE_TYPE_PART_DIALOG_NAME = "createTypePartDialogName";

		/**
		 * Name of form component inside the dialog to create a type.
		 */
		public static final String CREATE_TYPE_FORM_NAME = "createTypeFormName";

		/**
		 * Name of dialog component to create a type.
		 */
		public static final String CREATE_TYPE_DIALOG_NAME = "createTypeDialogName";

		/**
		 * Name of form component inside the dialog to create a type.
		 */
		public static final String CREATE_ENUMERATION_FORM_NAME = "createEnumerationFormName";

		/**
		 * Name of dialog component to create a type.
		 */
		public static final String CREATE_ENUMERATION_DIALOG_NAME = "createEnumerationDialogName";

		/**
		 * Name of the flag for using incremental graph component updates.
		 */
		public static final String USE_INCREMENTAL_UPDATES_NAME = "useIncrementalUpdates";

		@Override
		@ItemDefault(DiagramJSGraphControlProvider.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		@ItemDefault
		@ImplementationClassDefault(DiagramJSGraphBuilder.class)
		PolymorphicConfiguration<? extends GraphModelBuilder> getModelBuilder();

		/**
		 * @see #CREATE_TYPE_PART_DIALOG_NAME
		 */
		@Name(CREATE_TYPE_PART_DIALOG_NAME)
		ComponentName getCreateTypePartDialogName();

		/**
		 * @see #CREATE_TYPE_PART_FORM_NAME
		 */
		@Name(CREATE_TYPE_PART_FORM_NAME)
		ComponentName getCreateTypePartFormName();

		/**
		 * @see #CREATE_TYPE_DIALOG_NAME
		 */
		@Name(CREATE_TYPE_DIALOG_NAME)
		ComponentName getCreateTypeDialogName();

		/**
		 * @see #CREATE_TYPE_FORM_NAME
		 */
		@Name(CREATE_TYPE_FORM_NAME)
		ComponentName getCreateTypeFormName();

		/**
		 * @see #CREATE_ENUMERATION_DIALOG_NAME
		 */
		@Name(CREATE_ENUMERATION_DIALOG_NAME)
		ComponentName getCreateEnumerationDialogName();

		/**
		 * @see #CREATE_ENUMERATION_FORM_NAME
		 */
		@Name(CREATE_ENUMERATION_FORM_NAME)
		ComponentName getCreateEnumerationFormName();

		/**
		 * @see #USE_INCREMENTAL_UPDATES_NAME
		 */
		@Name(USE_INCREMENTAL_UPDATES_NAME)
		boolean useIncrementalUpdates();
	}

	private static final String SHOW_TECHNICAL_NAMES_PROPERTY_NAME = "showTechnicalNames";

	private static final String SHOW_TABLE_INTERFACE_TYPES_PROPERTY_NAME = "showTableInterfaceTypes";

	private static final Property<Boolean> SHOW_TECHNICAL_NAMES = getShowTechnicalNamesProperty();

	private static final Property<Boolean> SHOW_TABLE_INTERFACE_TYPES = getShowTableInterfacesTypesProperty();

	/**
	 * Component property for flagging if technical names should be displayed.
	 */
	private static Property<Boolean> getShowTechnicalNamesProperty() {
		return TypedAnnotatable.property(Boolean.class, SHOW_TECHNICAL_NAMES_PROPERTY_NAME, Boolean.FALSE);
	}

	/**
	 * Component property for flagging if table interface types should be displayed.
	 */
	private static Property<Boolean> getShowTableInterfacesTypesProperty() {
		return TypedAnnotatable.property(Boolean.class, SHOW_TABLE_INTERFACE_TYPES_PROPERTY_NAME, Boolean.FALSE);
	}

	private static final ComponentChannel.ChannelListener SELECTION_LISTENER = new ComponentChannel.ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) sender.getComponent();
			graphComponent.onSelectionChange(newValue);
		}
	};

	TLModule _currentDisplayedModule;

	GraphData _graphData = new DefaultGraphData(null);
	
	/**
	 * Creates an {@link DiagramJSGraphComponent} instance.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        {@link DiagramJSGraphComponent} configuration.
	 */
	public DiagramJSGraphComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_graphData.setDropTarget(context.getInstance(config.getGraphDrop()));
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		selectionChannel().addListener(SELECTION_LISTENER);
	}

	final void onSelectionChange(Object newValue) {
		if (!isGraphValid(newValue)) {
			TLModule enclosingModule = GraphModelUtil.getEnclosingModule(newValue);
			setModel(enclosingModule);
			setGraphModel(createSharedGraphModel(enclosingModule));

			_currentDisplayedModule = enclosingModule;
		}

		DiagramJSGraphModel graphModel = (DiagramJSGraphModel) getGraphModel();
		if (graphModel != null) {
			if (!isGraphPartWithSameModelSelected(graphModel, newValue)) {
				selectGraphPart(graphModel, newValue);
			}
		}

	}

	private boolean isGraphPartWithSameModelSelected(DiagramJSGraphModel graphModel, Object newValue) {
		for (GraphPart selectedGraphPart : graphModel.getSelectedGraphParts()) {
			if (selectedGraphPart.getTag() == newValue) {
				return true;
			}
		}

		return false;
	}

	private void selectGraphPart(SharedGraph graph, Object businessModel) {
		GraphPart graphPart = graph.getGraphPart(businessModel);

		if (graphPart != null) {
			graph.setSelectedGraphParts(Collections.singleton(graphPart));
		} else {
			graph.setSelectedGraphParts(Collections.emptyList());
		}
	}

	/**
	 * The transient {@link SharedGraph}. If the desired {@link GraphModel} does not exist,
	 *         set it.
	 */
	public SharedGraph getGraphModel() {
		if (!hasGraphModel()) {
			setGraphModel(createSharedGraphModel(_currentDisplayedModule));
		}

		return _graphData.getGraph();
	}

	private SharedGraph createSharedGraphModel(TLModule module) {
		SharedGraph model = (SharedGraph) getBuilder().getModel(module, this);

		initSelectedGraphParts(model);

		return model;
	}

	private void initSelectedGraphParts(SharedGraph graphModel) {
		if (graphModel != null) {
			GraphPart graphPart = graphModel.getGraphPart(getSelected());

			if (graphPart != null) {
				DiagramJSGraphModel diagramGraphModel = (DiagramJSGraphModel) graphModel;
				diagramGraphModel.setSelectedGraphParts(Arrays.asList(graphPart));
			}
		}
	}

	/**
	 * The transient {@link GraphData} object of {@link DiagramJSGraphComponent}.
	 */
	public GraphData getGraphData() {
		return _graphData;
	}

	/**
	 * @see #getGraphModel()
	 */
	public void setGraphModel(SharedGraph sharedGraph) {
		_graphData.setGraph(sharedGraph);

		if (sharedGraph != null) {
			addGraphPartAttributeListeners(sharedGraph);
		}
	}

	private void addGraphPartAttributeListeners(SharedGraph sharedGraph) {
		sharedGraph.addAttributeListener(DefaultDiagramJSGraphModel.SELECTED_GRAPH_PARTS, new AttributeListener() {

			@Override
			public void handleAttributeUpdate(Object sender, String property) {
				DefaultDiagramJSGraphModel graphModel = (DefaultDiagramJSGraphModel) sender;
				Collection<? extends GraphPart> getSelectedGraphParts = graphModel.getSelectedGraphParts();

				Set<Object> tlGraphParts = getSelectedTLGraphParts(getSelectedGraphParts);

				if (tlGraphParts.isEmpty()) {
					Object selected = getSelected();

					if (isInvalid(selected) || hasGraphPart(graphModel, selected)) {
						setSelected(_currentDisplayedModule);
					}
				} else if (tlGraphParts.size() == 1) {
					setSelected(tlGraphParts.iterator().next());
				} else {
					setSelected(tlGraphParts);
				}
			}

			private boolean isInvalid(Object object) {
				if (object instanceof TLObject) {
					return !((TLObject) object).tValid();
				}

				return true;
			}

			private Set<Object> getSelectedTLGraphParts(Collection<? extends GraphPart> graphParts) {
				if (graphParts == null) {
					return Collections.emptySet();
				}

				return graphParts.stream().map(graphPart -> graphPart.getTag())
					.collect(Collectors.toCollection(LinkedHashSet::new));
			}
		});
	}

	private boolean isGraphValid(Object model) {
		boolean displayedModuleValid = isDisplayedModuleValid(model);

		if (model instanceof TLModule) {
			return displayedModuleValid;
		}

		return hasGraphPart(model);
	}

	private boolean hasGraphPart(Object object) {
		SharedGraph graph = _graphData.getGraph();

		return graph != null && hasGraphPart(graph, object);
	}

	boolean hasGraphPart(GraphModel model, Object object) {
		return model.getGraphPart(object) != null;
	}

	private boolean isDisplayedModuleValid(Object model) {
		return _currentDisplayedModule != null && _currentDisplayedModule == GraphModelUtil.getEnclosingModule(model);
	}

	private boolean hasGraphModel() {
		return _graphData.getGraph() != null;
	}

	/**
	 * Delete the current {@link SharedGraph} and create a new one.
	 */
	public void resetGraphModel() {
		setGraphModel(createSharedGraphModel(_currentDisplayedModule));
	}

	@Override
	public void createReference(String type, TLType source, TLType target) {
		DiagramJSGraphComponent.Config config = (Config) getConfig();

		LayoutComponent dialog = getDialog(config.getCreateTypePartDialogName());
		FormComponent formComponent = (FormComponent) dialog.getComponentByName(config.getCreateTypePartFormName());
		formComponent.setModel(source);

		setTypePartEditModel(formComponent, createReferenceModel(type, target));

		openDialog(dialog, I18NConstants.CREATE_REFERENCE_CONNECTION_TITLE);
	}

	private void openDialog(LayoutComponent dialog, ResKey titleResKey) {
		OpenModalDialogCommandHandler.openDialog(dialog, Fragments.message(titleResKey));
	}

	private ReferenceModel createReferenceModel(String type, TLType target) {
		ReferenceModel partModel = TypedConfiguration.newConfigItem(ReferenceModel.class);

		partModel.setTypeSpec(TLModelUtil.qualifiedName(target));

		if (GraphLayoutConstants.EDGE_COMPOSITION_TYPE.equals(type)) {
			partModel.setComposite(true);
		}

		return partModel;
	}

	@Override
	public void createInheritance(TLClass sourceClass, TLClass targetClass) {
		if (TLModelUtil.getReflexiveTransitiveGeneralizations(targetClass).contains(sourceClass)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_CYCLIC_INHERITANCE);
		}

		DiagramJSGraphModel graphModel = (DiagramJSGraphModel) getGraphModel();

		try (Transaction trans = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			List<TLClass> generalizations = sourceClass.getGeneralizations();
			generalizations.add(targetClass);
			switch (generalizations.size()) {
				case 0: {
					assert false : "New class was added.";
					break;
				}
				case 1: {
					// No redundant extension to TLObject.
					break;
				}
				default: {
					TLClass tlObjectType = TLModelUtil.tlObjectType(sourceClass.getModel());
					if (generalizations.remove(tlObjectType)) {
						// Redundant extension removed. Remove existing GUI reference.
						Edge edge = graphModel.getEdge(new TLInheritanceImpl(sourceClass, tlObjectType));
						if (edge != null) {
							// There may be no GUI edge, because the TLObject is not included in the
							// diagram.
							graphModel.remove(edge);
						}
					}
					break;
				}
			}

			trans.commit();
		}

		TLInheritance inheritance = new TLInheritanceImpl(sourceClass, targetClass);

		Node source = graphModel.getNode(sourceClass);
		Node target = graphModel.getNode(targetClass);

		GraphModelUtil.createDiagramJSEdge(getLabelProvider(), graphModel, inheritance, source, target);
		setSelected(inheritance);
	}

	@Override
	public void createClassProperty(TLClass clazz) {
		DiagramJSGraphComponent.Config config = (Config) getConfig();

		LayoutComponent dialog = getDialog(config.getCreateTypePartDialogName());
		FormComponent formComponent = (FormComponent) dialog.getComponentByName(config.getCreateTypePartFormName());
		formComponent.setModel(clazz);

		setTypePartEditModel(formComponent, createAttributeModel());

		openDialog(dialog, I18NConstants.CREATE_CLASS_PROPERTY_TITLE);
	}

	private PropertyModel createAttributeModel() {
		return TypedConfiguration.newConfigItem(PropertyModel.class);
	}

	private void setTypePartEditModel(FormComponent formComponent, PartModel partModel) {
		TLStructuredTypePartFormBuilder.EditModel model = (EditModel) EditorFactory.getModel(formComponent);

		model.setPartModel(partModel);
		model.setCreating(true);
	}

	@Override
	public void createClass(Bounds bounds) {
		DiagramJSGraphComponent.Config config = (Config) getConfig();

		LayoutComponent dialog = getDialog(config.getCreateTypeDialogName());
		FormComponent formComponent = (FormComponent) dialog.getComponentByName(config.getCreateTypeFormName());
		formComponent.set(CreateClassCommand.BOUNDS, bounds);

		setTypeEditModel(formComponent);

		openDialog(dialog, I18NConstants.CREATE_CLASS_TITLE);
	}

	@Override
	public void createEnumeration(Bounds bounds) {
		DiagramJSGraphComponent.Config config = (Config) getConfig();

		LayoutComponent dialog = getDialog(config.getCreateEnumerationDialogName());
		FormComponent formComponent = (FormComponent) dialog.getComponentByName(config.getCreateEnumerationFormName());
		formComponent.set(CreateEnumerationCommand.BOUNDS, bounds);

		setEnumerationEditModel(formComponent);

		openDialog(dialog, I18NConstants.CREATE_ENUMERATION_TITLE);
	}

	private void setEnumerationEditModel(FormComponent formComponent) {
		TLEnumerationFormBuilder.EditModel editModel =
			(com.top_logic.element.layout.meta.TLEnumerationFormBuilder.EditModel) EditorFactory
				.getModel(formComponent);

		editModel.setModule(_currentDisplayedModule);
		editModel.setCreate(true);
	}

	private void setTypeEditModel(FormComponent formComponent) {
		TLStructuredTypeFormBuilder.EditModel model =
			(com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder.EditModel) EditorFactory.getModel(formComponent);

		model.setModule(_currentDisplayedModule);
		model.setCreate(true);
	}

	@Override
	protected boolean receiveMyModelChangeEvent(Object changedBy) {
		if (((Config) getConfig()).useIncrementalUpdates()) {
			return true;
		} else {
			return super.receiveMyModelChangeEvent(changedBy);
		}
	}

	/**
	 * True, if technical names should be displayed, otherwise false.
	 */
	public boolean showTechnicalNames() {
		return get(SHOW_TECHNICAL_NAMES).booleanValue();
	}

	/**
	 * True, if table interface types should be displayed, otherwise false.
	 */
	public boolean showTableInterfaceTypes() {
		return get(SHOW_TABLE_INTERFACE_TYPES).booleanValue();
	}

	/**
	 * @see #showTechnicalNames()
	 */
	public Object setShowTechnicalNames(boolean newValue) {
		return set(SHOW_TECHNICAL_NAMES, Boolean.valueOf(newValue));
	}

	/**
	 * @see #showTableInterfaceTypes()
	 */
	public Object setShowTableInterfaceTypes(boolean newValue) {
		return set(SHOW_TABLE_INTERFACE_TYPES, Boolean.valueOf(newValue));
	}

	/**
	 * {@link LayoutContext} containing the {@link LayoutDirection} and a
	 *         {@link LabelProvider} for {@link TLModelPart}s.
	 */
	public LayoutContext getLayoutContext() {
		return new LayoutContext(LayoutDirection.VERTICAL_FROM_SINK, getLabelProvider(), showTableInterfaceTypes());
	}

	/**
	 * @see #getLayoutContext()
	 */
	public LabelProvider getLabelProvider() {
		if (showTechnicalNames()) {
			return TechnicalNamesLabelProvider.INSTANCE;
		}

		return MetaResourceProvider.INSTANCE;
	}

	@Override
	public void gotoDefinition(TLModelPart modelPart) {
		TLModule enclosingModule = GraphModelUtil.getEnclosingModule(modelPart);

		boolean newModelSet = setModel(enclosingModule);

		if (newModelSet) {
			SharedGraph graph = createSharedGraphModel(enclosingModule);

			selectGraphPart(graph, modelPart);
			setGraphModel(graph);

			_currentDisplayedModule = enclosingModule;
		}
	}

}