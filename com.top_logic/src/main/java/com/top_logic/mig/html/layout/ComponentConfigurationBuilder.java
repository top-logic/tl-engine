/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.I18NConstants;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.processor.ConstantLayout;
import com.top_logic.layout.processor.ElementInAttributeContextError;
import com.top_logic.layout.processor.Expansion;
import com.top_logic.layout.processor.Expansion.NodeBuffer;
import com.top_logic.layout.processor.LayoutInline;
import com.top_logic.layout.processor.LayoutModelUtils;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.ParameterValue;
import com.top_logic.layout.processor.ResolveFailure;
import com.top_logic.layout.processor.TemplateLayout;
import com.top_logic.util.error.TopLogicException;

/**
 * Creates a {@link LayoutComponent.Config}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ComponentConfigurationBuilder {

	private InstantiationContext _context;

	private Map<String, ? extends ParameterValue> _templateArguments;

	private LayoutResolver _resolver;

	private String _layoutName;

	private String _layoutScope;

	private List<BinaryData> _overlays;

	/**
	 * Context to log errors when creating the {@link LayoutComponent.Config}.
	 */
	public void setContext(InstantiationContext context) {
		_context = context;
	}

	/**
	 * Loads layout definitions.
	 */
	public void setLayoutResolver(LayoutResolver resolver) {
		_resolver = resolver;
	}

	/**
	 * Arguments mapping for layout parameters.
	 */
	public void setTemplateArguments(Map<String, ? extends ParameterValue> templateArguments) {
		_templateArguments = templateArguments;
	}

	/**
	 * Scope of the {@link LayoutComponent}.
	 */
	public void setLayoutScope(String layoutScope) {
		_layoutScope = LayoutUtils.normalizeLayoutKey(layoutScope);
	}

	/**
	 * {@link LayoutComponent} name.
	 */
	public void setLayoutName(String layoutName) {
		_layoutName = layoutName;
	}

	/**
	 * Overlays for the layout given by {@link #setLayoutName(String)}.
	 */
	public void setOverlays(List<BinaryData> overlays) {
		_overlays = overlays;
	}

	/**
	 * Creates the {@link LayoutComponent.Config}.
	 */
	public TLLayout build() throws ConfigurationException, IOException {
		BinaryData layout = _resolver.resolveLayoutData(_layoutName, false);
		if (layout == null) {
			throw new IOException("No file found for layout " + _layoutName);
		} else {
			return createLayout(layout);
		}
	}

	private TLLayout createLayout(BinaryData baseLayoutFile) throws IOException, ConfigurationException {
		ConstantLayout baseLayout =
			createConstantLayout(_layoutName, _templateArguments, baseLayoutFile);
		Element rootNode = baseLayout.getLayoutDocument().getDocumentElement();

		if (LayoutModelUtils.isTemplateCall(rootNode)) {
			return createLayoutFromTemplate(rootNode);
		} else {
			return createFixedLayout(baseLayout);
		}
	}

	private ConstantLayout createConstantLayout(String layoutName,
			Map<String, ? extends ParameterValue> templateArguments, BinaryData layoutFile) throws IOException {
		ConstantLayout layout;

		try {
			if (templateArguments != null) {
				layout = _resolver.getLayout(layoutName, layoutFile);
				Document document = DOMUtil.newDocument();
				TemplateLayout template = new TemplateLayout(layout);
				Expansion expander = template.createContentExpander(null, null, templateArguments, null);
				try {
					expander.expandTemplate(new NodeBuffer(document, null), template.getContent());
				} catch (ElementInAttributeContextError ex) {
					throw new UnreachableAssertion(ex);
				}
				return new ConstantLayout(
					_resolver, layout.getLayoutName(), layout.getLayoutData(), document);
			} else {
				return _resolver.getLayout(layoutName, layoutFile);
			}
		} catch (ResolveFailure exception) {
			throw new IOException("Layout loading failed.", exception);
		}
	}

	private TLLayout createLayoutFromTemplate(Element rootNode) throws ConfigurationException {
		String templateName = LayoutTemplateUtils.getTemplateName(rootNode);
		DynamicComponentDefinition definition = LayoutTemplateUtils.getComponentDefinition(templateName);
		String baseArguments = getTemplateCallArguments(rootNode);


		List<Content> argumentOverlaysContent;
		List<BinaryData> componentOverlays;
		boolean isFinal = LayoutTemplateUtils.isFinal(rootNode);
		if (isFinal) {
			componentOverlays = Collections.emptyList();
			argumentOverlaysContent = Collections.emptyList();
		} else {
			Pair<List<BinaryData>, List<BinaryData>> overlayPartition = partitionLayoutOverlays(_overlays);
			
			List<BinaryData> argumentOverlays = overlayPartition.getFirst();
			componentOverlays = overlayPartition.getSecond();
			
			argumentOverlaysContent = getOverlaysContent(argumentOverlays);
		}

		String scope = _layoutScope;
		if (componentOverlays.isEmpty()) {
			return createTemplateCallLayout(scope, argumentOverlaysContent, templateName, definition, baseArguments);
		} else {
			ConfigurationItem arguments =
				LayoutTemplateUtils.deserializeTemplateArguments(argumentOverlaysContent, scope,
					definition, baseArguments);
			return createLayout(scope, componentOverlays, templateName, arguments);
		}
	}

	@SuppressWarnings("unchecked")
	private Pair<List<BinaryData>, List<BinaryData>> partitionLayoutOverlays(List<BinaryData> overlays) {
		List<BinaryData> argumentOverlays = new ArrayList<>();
		List<BinaryData> componentOverlays = new ArrayList<>();

		boolean canApplyArgumentOverlays = true;

		for (int i = 0; i < overlays.size(); i++) {
			BinaryData path = overlays.get(i);

			if (isTemplateArgumentOverlay(path)) {
				if (canApplyArgumentOverlays) {
					argumentOverlays.add(path);
				} else {
					warn("Argument overlay " + path + " could not be applied after a component overlay.");
				}
			} else {
				componentOverlays.add(path);
				canApplyArgumentOverlays = false;
			}
		}

		return (Pair<List<BinaryData>, List<BinaryData>>) TupleFactory.newTuple(argumentOverlays, componentOverlays);
	}

	private boolean isTemplateArgumentOverlay(BinaryData path) {
		return LayoutModelUtils.isTemplateArgument(resolveRootNode(path));
	}

	private void warn(String message) {
		Logger.warn(message, ComponentConfigurationBuilder.class);
	}

	private Element resolveRootNode(BinaryData layoutData) {
		try {
			ConstantLayout layout = _resolver.getLayout(layoutData.getName(), layoutData);

			return layout.getLayoutDocument().getDocumentElement();
		} catch (ResolveFailure exception) {
			throw new TopLogicException(I18NConstants.LAYOUT_RESOLVE_ERROR.fill(layoutData.toString()), exception);
		}
	}

	private List<Content> getOverlaysContent(List<BinaryData> overlays) {
		if (CollectionUtil.isEmptyOrNull(overlays)) {
			return Collections.emptyList();
		}

		Function<BinaryData, Content> contentMapping = createLayoutPathToContentMapper();

		return overlays.stream().map(contentMapping).collect(Collectors.toList());
	}

	private Function<BinaryData, Content> createLayoutPathToContentMapper() {
		return layoutData -> {
			try {
				ConstantLayout overlayLayout = createConstantLayout(layoutData.getName(), null, layoutData);

				inlineLayout(overlayLayout);
				
				return createLayoutConfigContent(false, overlayLayout);
			} catch (IOException exception) {
				_context.error("Unable to create layout configuration content from " + layoutData.getName(), exception);

				return CharacterContents.newContent("<" + LayoutComponent.XML_TAG_COMPONENT_NAME + "/>");
			}
		};
	}

	private Collection<BinaryData> inlineLayout(ConstantLayout layout) throws IOException {
		try {
			if (!LayoutModelUtils.isInlined(layout.getLayoutDocument())) {
				LayoutInline inliner = new LayoutInline(_resolver);
				try {
					inliner.inline(layout);
					layout.markLayoutDocumentInlined();
				} catch (RuntimeException exception) {
					throw new ResolveFailure("Unable to inline layout.", exception);
				}
				return inliner.getFiles();
			} else {
				return Collections.singletonList(layout.getLayoutData());
			}
		} catch (ResolveFailure exception) {
			throw new IOException("Layout loading failed.", exception);
		}
	}

	private BinaryContent createLayoutConfigContent(boolean hasTemplateArguments, ConstantLayout layout) {
		if (layout.isLayoutDocumentInlined() || hasTemplateArguments) {
			Element rootNode = layout.getLayoutDocument().getDocumentElement();
			return DOMUtil.toBinaryContent(rootNode, layout.getLayoutData().toString());
		} else {
			return layout.getLayoutData();
		}
	}

	private String getTemplateCallArguments(Element documentElement) {
		return DOMUtil.toString(LayoutTemplateUtils.getTemplateArgumentElement(documentElement));
	}

	private TLLayout createTemplateCallLayout(String scope,
			List<Content> overlays,
			String templateName, DynamicComponentDefinition definition, String baseArguments) {
		try {
			ConfigurationItem templateArguments =
				LayoutTemplateUtils.deserializeTemplateArguments(overlays, scope, definition, baseArguments);
			return new LayoutTemplateCall(templateName, templateArguments, scope);
		} catch (ConfigurationException exception) {
			return new LazyParsingTemplateCall(templateName,
				new ComputationEx<ConfigurationItem, ConfigurationException>() {

					@Override
					public ConfigurationItem run() throws ConfigurationException {
						return LayoutTemplateUtils.deserializeTemplateArguments(overlays,
							scope, definition, baseArguments);
					}
				}, scope);
		}
	}

	private TLLayout createLayout(
			String scope, List<BinaryData> componentOverlays, String templateName, ConfigurationItem arguments)
			throws ConfigurationException {
		LayoutComponent.Config config = LayoutTemplateUtils.createUnqualifiedComponentConfig(templateName,
			arguments, scope);

		if (!config.isFinal()) {
			config = addOverlay(config, componentOverlays);
		}

		LayoutUtils.qualifyComponentNames(scope, config);

		return new FixedLayout(config);
	}

	private LayoutComponent.Config addOverlay(LayoutComponent.Config result, List<BinaryData> actualOverlays) {
		ComponentOverlay overlayBase = TypedConfiguration.newConfigItem(ComponentOverlay.class);

		LayoutUtils.modify(result, config -> {
			ComponentName configName = config.getName();
			if (!LayoutConstants.isSyntheticName(configName)) {
				overlayBase.getComponents().put(configName, config);
			} else {
				/* Ignore components without actual name. This is currently needed because the
				 * ComponentOverlay is copied and a copy of a component with synthetic name gets a
				 * new synthetic name, such that the map becomes inconsistent: old name as key but
				 * value has a new key. */
			}
			return config;
		});
		ObjectFlag<Collection<BinaryData>> touchedFiles = new ObjectFlag<>(null);

		Set<ComponentName> originalComponents = new HashSet<>(overlayBase.getComponents().keySet());
		ConfigurationReader reader = new ConfigurationReader(
			_context,
			Collections.singletonMap(ComponentOverlay.DEFAULT_TAG_NAME, overlayBase.descriptor()));
		reader.setBaseConfig(overlayBase);
		reader.setVariableExpander(LayoutVariables.layoutExpander(_resolver.getTheme(), _layoutName));
		reader.setSources(actualOverlays
			.stream()
			.map(overlayData -> {
				try {
					ConstantLayout layout = createConstantLayout(overlayData.getName(), null, overlayData);
							return createLayoutConfigContent(false, files -> touchedFiles.set(
								files),
								layout);
				} catch (IOException ex) {
					_context.error("Unable to create layout configuration content from " + overlayData.getName(), ex);
					return CharacterContents.newContent("<" + LayoutComponent.XML_TAG_COMPONENT_NAME + "/>");
				}
			})
			.collect(Collectors.toList()));
		ComponentOverlay overlays;
		try {
			overlays = (ComponentOverlay) reader.read();
		} catch (ConfigurationException | RuntimeException problem) {
			_context.error(
				"Unable to load component overlays: "
				+ actualOverlays.stream().map(s -> s.toString()).collect(Collectors.joining(", ")), problem);
			return result;
		}
		if (overlays == null) {
			return result;
		}
		Set<ComponentName> removeComponents = new HashSet<>(originalComponents);
		removeComponents.removeAll(overlays.getComponents().keySet());
		Set<ComponentName> addedComponents = new HashSet<>(overlays.getComponents().keySet());
		addedComponents.removeAll(originalComponents);
		if (!addedComponents.isEmpty()) {
			_context.error("Unable to add components '" + addedComponents + "' defined in one of '"
				+ actualOverlays
				+ "'. Don't know where to set it.");
		}
		boolean removeTopLevelComponent = removeComponents.remove(result.getName());
		if (removeTopLevelComponent) {
			_context.error("Unable to remove top level component '" + result
				.getName()
				+ "' component defined in one of '" + actualOverlays + "'.");
		}
		result = LayoutUtils.modify(result, config -> {
			ComponentName configName = config.getName();
			if (removeComponents.contains(configName)) {
				return null;
			}
			LayoutComponent.Config overlay = overlays.getComponents().get(configName);
			if (overlay != null) {
				return overlay;
			}
			return config;
		});
		return result;
	}

	private TLLayout createFixedLayout(ConstantLayout layout)
			throws UnreachableAssertion, IOException, ConfigurationException {
		StopWatch sw = StopWatch.createStartedWatch();
		ObjectFlag<Collection<BinaryData>> touchedFiles = new ObjectFlag<>(null);
		BinaryContent binaryContent = createLayoutConfigContent(
			_templateArguments != null,
			files -> touchedFiles.set(files), layout);
		LayoutComponent.Config configuration = LayoutUtils.createLayoutConfigurationInternal(
			_context, _resolver.getTheme(), _layoutName, binaryContent);
		if (sw.getElapsedMillis() > 500) {
			_context.info("Creating layout from '" + _layoutName + "' took " + sw + ".");
		}

		if (!configuration.isFinal()) {
			List<BinaryData> fixedLayoutOverlays = getAllOverlaysForFixedLayouts(_overlays);

			if (!CollectionUtil.isEmptyOrNull(fixedLayoutOverlays)) {
				configuration = addOverlay(configuration, fixedLayoutOverlays);
			}
		}

		LayoutUtils.qualifyComponentNames(_layoutScope, configuration);

		return new FixedLayout(configuration);
	}

	private List<BinaryData> getAllOverlaysForFixedLayouts(List<BinaryData> overlays) {
		List<BinaryData> fixedLayoutOverlays = new ArrayList<>();

		for (BinaryData overlay : overlays) {
			if (isTemplateArgumentOverlay(overlay)) {
				warn("Argument overlay " + overlay + " could not be applied on a fixed layout.");
			} else {
				fixedLayoutOverlays.add(overlay);
			}
		}

		return fixedLayoutOverlays;
	}

	private BinaryContent createLayoutConfigContent(
			boolean hasTemplateArguments,
			Consumer<? super Collection<BinaryData>> touchedFilesConsumer,
			ConstantLayout layout) throws UnreachableAssertion, IOException {
		Collection<BinaryData> touchedFiles = inlineLayout(layout);

		if (touchedFilesConsumer != null) {
			touchedFilesConsumer.accept(touchedFiles);
		}

		return createLayoutConfigContent(hasTemplateArguments, layout);
	}

}
