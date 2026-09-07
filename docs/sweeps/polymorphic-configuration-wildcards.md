# Polymorphic configuration references missing the wildcard bound

Ticket: #29433

A property referencing a polymorphic configuration must name its base type as a
wildcard bound: `PolymorphicConfiguration<? extends X> getFoo()`, not
`PolymorphicConfiguration<X> getFoo()`. A configuration declaring its own
implementation (`interface Config extends PolymorphicConfiguration<MyImpl>`) and a
parameterization by a type variable are correct and are not listed.

Regenerate with:

```
grep -rnE "(Named)?PolymorphicConfiguration<[A-Z]" --include=*.java . | grep -v /target/ \
  | grep -v "extends PolymorphicConfiguration<" | grep -E "(get|set|is)[A-Z]\w*\s*\("
```

Count: TOTAL 279 in 21 modules


com.top_logic (150)
com.top_logic :: util/monitor/ApplicationMonitor.java:51  Map<String, NamedPolymorphicConfiguration<MonitorComponent>> getComponents();
com.top_logic :: util/sched/layout/table/results/TaskResultTreeComponent.java:52  PolymorphicConfiguration<TreeBuilder<DefaultTreeTableNode>> getTreeBuilder();
com.top_logic :: util/sched/layout/table/results/failed/FailedTaskResultTreeComponent.java:53  PolymorphicConfiguration<TreeBuilder<DefaultTreeTableNode>> getTreeBuilder();
com.top_logic :: util/model/ModelService.java:182  PolymorphicConfiguration<PersistentQuery> getQueryImplementation();
com.top_logic :: knowledge/monitor/CheckUnusedAccountsTask.java:48  PolymorphicConfiguration<UnusedAccountCheck> getChecker();
com.top_logic :: knowledge/event/convert/AttributeValueConversion.java:73  PolymorphicConfiguration<Mapping<Object, ?>> getValueMapping();
com.top_logic :: knowledge/service/DBSetupActions.java:42  List<PolymorphicConfiguration<DBSetupAction>> getActions();
com.top_logic :: knowledge/service/db2/AbstractIndexColumnsStrategy.java:46  public PolymorphicConfiguration<IndexColumnsStrategy> getConfig() {
com.top_logic :: knowledge/service/db2/migration/db/transformers/TableRowSkip.java:35  PolymorphicConfiguration<Filter<? super RowValue>> getMatcher();
com.top_logic :: knowledge/service/db2/migration/db/transformers/TableRowSkip.java:40  void setMatcher(PolymorphicConfiguration<Filter<? super RowValue>> matcher);
com.top_logic :: knowledge/service/db2/migration/rewriters/MoveTLTypeToTable.java:76  PolymorphicConfiguration<ItemEventVisitor<?, Void>> getValueRewriter();
com.top_logic :: knowledge/service/db2/migration/rewriters/AttributePatternValueConversion.java:58  PolymorphicConfiguration<Mapping<Object, ?>> getValueMapping();
com.top_logic :: knowledge/service/db2/migration/rewriters/AttributePatternValueConversion.java:63  void setValueMapping(PolymorphicConfiguration<Mapping<Object, ?>> value);
com.top_logic :: knowledge/service/db2/migration/rewriters/ConfigurationAttributeChange.java:49  PolymorphicConfiguration<Mapping<? super ConfigurationItem, Object>> getMapping();
com.top_logic :: knowledge/service/db2/migration/rewriters/ForeignKeyConverter.java:46  PolymorphicConfiguration<ReferenceConversion> getReferenceConversion();
com.top_logic :: knowledge/service/db2/migration/processor/StringReplacementProcessor.java:49  List<PolymorphicConfiguration<Mapping<? super String, String>>> getReplacements();
com.top_logic :: layout/basic/ResourceRenderer.java:145  PolymorphicConfiguration<ContextMenuProvider> getContextMenu();
com.top_logic :: layout/basic/ResourceRenderer.java:150  void setContextMenu(PolymorphicConfiguration<ContextMenuProvider> value);
com.top_logic :: layout/basic/ResourceRenderer.java:361  PolymorphicConfiguration<ContextMenuProvider> contextMenuConfig =
com.top_logic :: layout/basic/ResourceRenderer.java:362  (PolymorphicConfiguration<ContextMenuProvider>) InstanceAccess.INSTANCE.getConfig(getContextMenu());
com.top_logic :: layout/basic/ConfiguredDelegatingCommandModel.java:41  PolymorphicConfiguration<Command> getCommand();
com.top_logic :: layout/basic/ConfiguredDelegatingCommandModel.java:46  void setCommand(PolymorphicConfiguration<Command> command);
com.top_logic :: layout/basic/component/BreadcrumbComponent.java:55  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: layout/basic/component/BreadcrumbComponent.java:64  PolymorphicConfiguration<BreadcrumbRenderer> getRenderer();
com.top_logic :: layout/table/filter/CellExistenceTesterProxy.java:37  PolymorphicConfiguration<CellExistenceTester> getTester();
com.top_logic :: layout/table/filter/CellExistenceTesterProxy.java:42  void setTester(PolymorphicConfiguration<CellExistenceTester> tester);
com.top_logic :: layout/table/tree/TreeTableComponent.java:187  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: layout/table/tree/compare/CompareTreeTableComponent.java:64  PolymorphicConfiguration<TreeBuilder<DefaultTreeTableNode>> getTreeBuilder();
com.top_logic :: layout/table/provider/generic/TableConfigModelService.java:60  PolymorphicConfiguration<TableConfigModelInfoProvider> getModelInfoProvider();
com.top_logic :: layout/table/component/AbstractTableFilterProvider.java:189  public PolymorphicConfiguration<AbstractTableFilterProvider> getConfig() {
com.top_logic :: layout/table/component/TableComponent.java:162  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: layout/table/model/ColumnBaseConfig.java:727  List<PolymorphicConfiguration<HTMLFragmentProvider>> getAdditionalHeaders();
com.top_logic :: layout/table/model/ColumnBaseConfig.java:730  void setAdditionalHeaders(List<PolymorphicConfiguration<ControlProvider>> value);
com.top_logic :: layout/table/model/ColumnBaseConfig.java:737  List<PolymorphicConfiguration<ColumnConfigurator>> getConfigurators();
com.top_logic :: layout/table/model/TableConfigUtil.java:516  for (PolymorphicConfiguration<ColumnConfigurator> configuratorConfig : config.getConfigurators()) {
com.top_logic :: layout/table/model/TableConfig.java:480  PolymorphicConfiguration<TableDragSource> getDragSource();
com.top_logic :: layout/table/model/TableConfig.java:487  List<PolymorphicConfiguration<TableDropTarget>> getDropTargets();
com.top_logic :: layout/table/model/SimpleTableDataExport.java:89  PolymorphicConfiguration<Function<TableData, ExecutableState>> getExecutability();
com.top_logic :: layout/table/model/SimpleTableDataExport.java:94  void setExecutability(PolymorphicConfiguration<Function<TableData, ExecutableState>> value);
com.top_logic :: layout/form/boxes/tag/BoxLayoutTag.java:101  PolymorphicConfiguration<BoxLayout> getImplementation();
com.top_logic :: layout/form/boxes/tag/BoxLayoutTag.java:118  Map<String, PolymorphicConfiguration<BoxLayout>> configByName =
com.top_logic :: layout/form/boxes/reactive_tag/BoxLayoutTag.java:103  PolymorphicConfiguration<BoxLayout> getImplementation();
com.top_logic :: layout/form/boxes/reactive_tag/BoxLayoutTag.java:120  Map<String, PolymorphicConfiguration<BoxLayout>> configByName =
com.top_logic :: layout/form/values/ImplOptionMapping.java:54  (PolymorphicConfiguration<Object>) TypedConfiguration.newConfigItem(configType);
com.top_logic :: layout/form/control/WikiTextRenderer.java:42  PolymorphicConfiguration<LabelProvider> getTextExtractor();
com.top_logic :: layout/form/component/FormStateRecorder.java:66  public PolymorphicConfiguration<ComponentResolver> getConfig() {
com.top_logic :: layout/form/component/ValueTransformation.java:31  PolymorphicConfiguration<ValueTransformation> config) {
com.top_logic :: layout/form/component/WithPostCreateActions.java:95  List<PolymorphicConfiguration<PostCreateAction>> getPostCreateActions();
com.top_logic :: layout/form/component/PostCreateAction.java:577  PolymorphicConfiguration<ValueTransformation> getInput();
com.top_logic :: layout/form/component/AbstractSelectorComponent.java:159  PolymorphicConfiguration<SelectionUpdater> getSelectionOnModelChange();
com.top_logic :: layout/form/component/DeclarativeFormComponent.java:83  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: layout/form/component/edit/CanLock.java:51  PolymorphicConfiguration<LockHandler> getLockHandler();
com.top_logic :: layout/form/component/edit/CanLock.java:102  PolymorphicConfiguration<LockHandler> handlerConfig = config.getLockHandler();
com.top_logic :: layout/form/declarative/DeclarativeFormBuilder.java:171  PolymorphicConfiguration<Function<Object, ConfigurationItem>> getModelToFormMapping();
com.top_logic :: layout/form/treetable/component/StructureEditComponent.java:177  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: layout/form/decorator/SimpleConfiguredCompareCommandHandler.java:44  PolymorphicConfiguration<CompareObjectCreator> getAlgorithm();
com.top_logic :: layout/xml/LayoutControlComponent.java:215  PolymorphicConfiguration<LayoutControlProvider> customProvider =
com.top_logic :: layout/tree/renderer/NavigationRenderer.java:47  PolymorphicConfiguration<ResourceProvider> getResourceProvider();
com.top_logic :: layout/tree/component/TreeComponent.java:314  PolymorphicConfiguration<TreeRenderer> getTreeRenderer();
com.top_logic :: layout/tree/component/TreeComponent.java:327  PolymorphicConfiguration<ResourceProvider> getResourceProvider();
com.top_logic :: layout/tree/component/TreeComponent.java:342  PolymorphicConfiguration<TreeDragSource> getDragSource();
com.top_logic :: layout/tree/component/TreeComponent.java:349  List<PolymorphicConfiguration<TreeDropTarget>> getDropTargets();
com.top_logic :: layout/tree/breadcrumb/BreadcrumbContentRenderer.java:106  PolymorphicConfiguration<ResourceProvider> getResourceProvider();
com.top_logic :: layout/tree/model/TreeTableBuilderAdapter.java:36  PolymorphicConfiguration<TreeView<?>> getTreeView();
com.top_logic :: layout/renderers/PDFRendererAdapter.java:37  PolymorphicConfiguration<Renderer<Object>> getRenderer();
com.top_logic :: layout/renderers/PDFRendererAdapter.java:42  void setRenderer(PolymorphicConfiguration<Renderer<Object>> value);
com.top_logic :: layout/provider/LabelProviderService.java:106  PolymorphicConfiguration<LabelProvider> getDefaultProvider();
com.top_logic :: layout/provider/label/JSONLabelProvider.java:52  PolymorphicConfiguration<ValueAnalyzer> getAnalyzer();
com.top_logic :: layout/provider/label/JSONLabelProvider.java:57  void setAnalyzer(PolymorphicConfiguration<ValueAnalyzer> value);
com.top_logic :: layout/inspector/history/GotoSelection.java:49  PolymorphicConfiguration<Mapping<Object, Object>> getSelectionMapping();
com.top_logic :: layout/component/configuration/DirtyCheckingCommandModelConfiguration.java:60  PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();
com.top_logic :: layout/component/configuration/CommandModelViewConfiguration.java:63  PolymorphicConfiguration<CommandModelConfiguration> getCommandConfiguration();
com.top_logic :: layout/component/configuration/ConfiguredCommandModelConfiguration.java:40  PolymorphicConfiguration<CommandModel> getCommandModel();
com.top_logic :: layout/component/configuration/ConfiguredCommandModelConfiguration.java:45  void setCommandModel(PolymorphicConfiguration<CommandModel> model);
com.top_logic :: layout/scripting/recorder/ref/ValueNamingSchemeRegistry.java:57  PolymorphicConfiguration<ValueNamingScheme<?>> getProvider();
com.top_logic :: layout/scripting/action/ActionFactory.java:241  PolymorphicConfiguration<LabelProvider> config =
com.top_logic :: layout/scripting/action/SelectObject.java:24  PolymorphicConfiguration<Filter<Object>> getMatcherConfig();
com.top_logic :: layout/scripting/action/SelectObject.java:26  void setMatcherConfig(PolymorphicConfiguration<Filter<Object>> value);
com.top_logic :: layout/progress/AJAXProgressComponent.java:122  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: layout/accessors/AccessorProxy.java:37  PolymorphicConfiguration<Accessor<T>> getImpl();
com.top_logic :: layout/accessors/AccessorProxy.java:42  void setImpl(PolymorphicConfiguration<Accessor<T>> impl);
com.top_logic :: layout/accessors/MappingAccessor.java:40  PolymorphicConfiguration<Mapping<Object, Object>> getMapping();
com.top_logic :: layout/accessors/MappingAccessor.java:45  void setMapping(PolymorphicConfiguration<Mapping<Object, Object>> mapping);
com.top_logic :: layout/accessors/ConfiguredTypeSafeAccessor.java:53  PolymorphicConfiguration<Accessor<?>> getDefaultAccessor();
com.top_logic :: layout/accessors/ConfiguredTypeSafeAccessor.java:83  PolymorphicConfiguration<Accessor<?>> defautConfig = config.getDefaultAccessor();
com.top_logic :: model/io/bindings/PrimitiveAttributeValueBinding.java:38  PolymorphicConfiguration<ConfigurationValueProvider<?>> getFormat();
com.top_logic :: model/io/annotation/TLExportBinding.java:43  PolymorphicConfiguration<AttributeValueBinding<?>> getImpl();
com.top_logic :: model/config/DatatypeConfig.java:59  PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();
com.top_logic :: model/config/DatatypeConfig.java:62  void setStorageMapping(PolymorphicConfiguration<StorageMapping<?>> value);
com.top_logic :: model/cache/TLModelCacheService.java:53  PolymorphicConfiguration<TLModelCache> getCache();
com.top_logic :: model/annotate/TLDynamicVisibility.java:25  PolymorphicConfiguration<ModeSelector> getModeSelector();
com.top_logic :: model/annotate/TLObjectInitializers.java:40  List<PolymorphicConfiguration<TLObjectInitializer>> getInitializers();
com.top_logic :: model/annotate/DisplayAnnotations.java:729  PolymorphicConfiguration<DefaultProvider> config = annotation.getProvider();
com.top_logic :: model/annotate/TLDefaultValue.java:37  PolymorphicConfiguration<DefaultProvider> getProvider();
com.top_logic :: model/annotate/TLDefaultValue.java:42  void setProvider(PolymorphicConfiguration<DefaultProvider> value);
com.top_logic :: model/annotate/ui/PDFRendererAnnotation.java:27  PolymorphicConfiguration<PDFRenderer> getImpl();
com.top_logic :: model/annotate/persistency/TLDeleteConstraints.java:55  List<PolymorphicConfiguration<DeleteConstraint>> getValue();
com.top_logic :: mig/html/SecurityListModelBuilder.java:45  PolymorphicConfiguration<ListModelBuilder> getModelBuilder();
com.top_logic :: mig/html/FilteringListModelBuilder.java:40  PolymorphicConfiguration<Filter<Object>> getFilter();
com.top_logic :: mig/html/layout/DialogInfo.java:147  PolymorphicConfiguration<TitleProvider> getTitle();
com.top_logic :: mig/html/layout/DialogInfo.java:152  void setTitle(PolymorphicConfiguration<TitleProvider> value);
com.top_logic :: mig/html/layout/LayoutUtils.java:1027  PolymorphicConfiguration<TitleProvider> titleConfig = dialog.getDialogInfo().getTitle();
com.top_logic :: mig/html/layout/MainLayout.java:285  PolymorphicConfiguration<LayoutFactory> getLayoutFactory();
com.top_logic :: mig/html/layout/LayoutComponent.java:352  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: mig/html/layout/LayoutComponent.java:505  List<PolymorphicConfiguration<ComponentResolver>> getComponentResolvers();
com.top_logic :: mig/html/layout/LayoutComponent.java:2700  List<PolymorphicConfiguration<ComponentResolver>> resolvers = _config.getComponentResolvers();
com.top_logic :: mig/html/layout/LayoutComponent.java:2702  PolymorphicConfiguration<ComponentResolver> config = resolvers.get(index);
com.top_logic :: mig/html/layout/tiles/StepOutCommand.java:40  PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();
com.top_logic :: mig/html/layout/tiles/GroupTileComponent.java:65  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: mig/html/layout/tiles/ComponentBuilder.java:93  PolymorphicConfiguration<DialogFormBuilder<? super ComponentParameters>> getFormBuilder();
com.top_logic :: mig/html/layout/tiles/RootTileComponent.java:142  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: mig/html/layout/tiles/RootTileComponent.java:152  PolymorphicConfiguration<TitleProvider> getTitle();
com.top_logic :: mig/html/layout/tiles/ContextTileComponent.java:91  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: mig/html/layout/tiles/component/TileListComponent.java:88  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: mig/html/layout/tiles/component/TileListComponent.java:96  PolymorphicConfiguration<TilePreview> getTilePreview();
com.top_logic :: mig/html/layout/tiles/component/InlinedTileComponent.java:77  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: mig/html/layout/tiles/breadcrumb/RootTileBreadcrumbControlProvider.java:159  PolymorphicConfiguration<BreadcrumbRenderer> getRenderer();
com.top_logic :: tool/boundsec/DispatchingSecurityObjectProvider.java:51  PolymorphicConfiguration<SecurityObjectProvider> getDefault();
com.top_logic :: tool/boundsec/DispatchingSecurityObjectProvider.java:56  void setDefault(PolymorphicConfiguration<SecurityObjectProvider> config);
com.top_logic :: tool/boundsec/DispatchingSecurityObjectProvider.java:98  PolymorphicConfiguration<SecurityObjectProvider> getImpl();
com.top_logic :: tool/boundsec/DispatchingSecurityObjectProvider.java:101  void setImpl(PolymorphicConfiguration<SecurityObjectProvider> config);
com.top_logic :: tool/boundsec/CommandHandler.java:596  PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();
com.top_logic :: tool/boundsec/AbstractCommandHandler.java:185  PolymorphicConfiguration<CheckScopeProvider> providerConfig = config.getCheckScopeProvider();
com.top_logic :: tool/boundsec/LayoutSecurityConfiguration.java:31  PolymorphicConfiguration<CompoundSecurityLayoutCommandGroupCollector> getCommandGroupCollector();
com.top_logic :: tool/boundsec/SecurityObjectProviderManager.java:107  PolymorphicConfiguration<SecurityObjectProvider> getImpl();
com.top_logic :: tool/boundsec/assistent/AssistentComponent.java:91  PolymorphicConfiguration<AbstractAssistentController> getController();
com.top_logic :: tool/boundsec/assistent/AssistentComponent.java:93  void setController(PolymorphicConfiguration<AbstractAssistentController> aController);
com.top_logic :: tool/boundsec/commandhandlers/BookmarkService.java:53  PolymorphicConfiguration<BookmarkHandler> getDefaultBookmarkHandler();
com.top_logic :: tool/boundsec/commandhandlers/BookmarkService.java:78  PolymorphicConfiguration<BookmarkHandler> getImpl();
com.top_logic :: tool/dataImport/AutomaticDataImportTask.java:65  PolymorphicConfiguration<AbstractDataImporter> getImporter();
com.top_logic :: tool/dataImport/AutomaticDataImportTask.java:70  void setImporter(PolymorphicConfiguration<AbstractDataImporter> value);
com.top_logic :: tool/execution/service/ConfiguredCommandApprovalService.java:104  List<PolymorphicConfiguration<CommandApprovalRule>> getRules();
com.top_logic :: tool/execution/service/ConfiguredCommandApprovalService.java:124  for (PolymorphicConfiguration<CommandApprovalRule> checkerConfig : typeCheck.getRules()) {
com.top_logic :: tool/execution/service/AbstractConfiguredApprovalRule.java:54  List<PolymorphicConfiguration<ExecutionContextFilter>> getContexts();
com.top_logic :: tool/execution/service/AbstractConfiguredApprovalRule.java:65  List<PolymorphicConfiguration<ExecutionContextFilter>> getExcludedContexts();
com.top_logic :: tool/execution/service/AbstractConfiguredApprovalRule.java:93  for (PolymorphicConfiguration<ExecutionContextFilter> filter : contexts) {
com.top_logic :: tool/export/ExportRegistryFactory.java:44  PolymorphicConfiguration<ExportRegistry> getExportRegistry();
com.top_logic :: base/locking/handler/SharedLockHandler.java:57  PolymorphicConfiguration<LockHandler> getDefaultHandler();
com.top_logic :: base/security/password/hashing/PasswordHashingService.java:69  PolymorphicConfiguration<PasswordHashingAlgorithm> getImplementation();
com.top_logic :: base/accesscontrol/Login.java:77  PolymorphicConfiguration<PasswordHashingService> getPasswordHashing();
com.top_logic :: base/accesscontrol/Login.java:106  PolymorphicConfiguration<LoginHook> getLoginHook();
com.top_logic :: base/services/SearchFactory.java:49  List<PolymorphicConfiguration<SearchEngine>> getSearchEngines();
com.top_logic :: common/folder/AbstractFolderTreeBuilder.java:48  PolymorphicConfiguration<Filter<Named>> getFileFilter();
com.top_logic :: common/folder/ui/FolderComponent.java:87  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic :: common/folder/ui/FolderComponent.java:94  PolymorphicConfiguration<TreeBuilder<FolderNode>> getTreeBuilder();
com.top_logic :: common/webfolder/ui/WebFolderUIFactory.java:114  PolymorphicConfiguration<LabelProvider> getZipDownloadFileNameProvider();
com.top_logic :: common/webfolder/ui/WebFolderUIFactory.java:125  PolymorphicConfiguration<LabelProvider> getZipFolderNameProvider();
com.top_logic :: common/webfolder/ui/WebFolderUIFactory.java:131  PolymorphicConfiguration<BreadcrumbRenderer> getBreadcrumbRenderer();
com.top_logic/src/test/java/test/com/top_logic/tool/boundsec/TestDispatchingSecurtyObjectProvider.java:85  config.setDefault((PolymorphicConfiguration<SecurityObjectProvider>) TypedConfiguration
com.top_logic/src/test/java/test/com/top_logic/tool/boundsec/TestDispatchingSecurtyObjectProvider.java:90  (PolymorphicConfiguration<SecurityObjectProvider>) TypedConfiguration

com.top_logic.basic (23)
com.top_logic.basic :: basic/TypeKeyRegistry.java:45  PolymorphicConfiguration<TypeKeyProvider> getProvider();
com.top_logic.basic :: basic/i18n/I18NCheck.java:46  Map<Class<?>, PolymorphicConfiguration<I18NChecker>> getCheckers();
com.top_logic.basic :: basic/i18n/I18NCheck.java:66  Map<Class<?>, PolymorphicConfiguration<I18NChecker>> checkerConfigs = getConfig().getCheckers();
com.top_logic.basic :: basic/sql/AbstractConfiguredConnectionPoolBase.java:57  PolymorphicConfiguration<DBHelper> getSQLDialect();
com.top_logic.basic :: basic/sql/AbstractConfiguredConnectionPoolBase.java:62  void setSQLDialect(PolymorphicConfiguration<DBHelper> value);
com.top_logic.basic :: basic/sql/AbstractConfiguredConnectionPoolBase.java:198  PolymorphicConfiguration<DBHelper> sqlDialectConfig = config().getSQLDialect();
com.top_logic.basic :: basic/html/SafeHTML.java:83  Map<String, NamedPolymorphicConfiguration<AttributeChecker>> getAttributeCheckers();
com.top_logic.basic :: basic/format/NormalizingFormatDefinition.java:36  PolymorphicConfiguration<FormatDefinition<?>> getFormat();
com.top_logic.basic :: basic/format/NormalizingFormatDefinition.java:49  PolymorphicConfiguration<FormatDefinition<?>> getParser();
com.top_logic.basic :: basic/format/PercentFormatDefinition.java:39  PolymorphicConfiguration<FormatDefinition<?>> getFormat();
com.top_logic.basic :: basic/col/ComparatorProxy.java:36  PolymorphicConfiguration<Comparator<S>> getBaseComparator();
com.top_logic.basic :: basic/col/ComparatorProxy.java:41  void setBaseComparator(PolymorphicConfiguration<Comparator<S>> base);
com.top_logic.basic :: basic/col/mapping/ConfigAsString.java:76  PolymorphicConfiguration<Mapping<String, String>> getInner();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/jsp/TestJSPContent.java:85  List<PolymorphicConfiguration<JSPContentChecker>> getContentCheckers();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestTypedConfigurationEquality.java:49  PolymorphicConfiguration<CI> getAConfig();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestNamedInstanceConfig.java:82  public PolymorphicConfiguration<Foo> getConfig() {
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestNamedInstanceConfig.java:150  List<NamedPolymorphicConfiguration<Foo>> getListGenerics();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestNamedInstanceConfig.java:153  Collection<NamedPolymorphicConfiguration<Foo>> getCollectionGenerics();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestNamedInstanceConfig.java:156  Map<String, NamedPolymorphicConfiguration<Foo>> getMapGenerics();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestConfigurationReader.java:359  PolymorphicConfiguration<Object> getInner();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestDeclarativeConfigDescriptor.java:290  PolymorphicConfiguration<Mapping<?, ?>> getConfigProperty();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/config/TestDeclarativeConfigDescriptor.java:304  List<PolymorphicConfiguration<Mapping<?, ?>>> getPolymorphicListProperty();
com.top_logic.basic/src/test/java/test/com/top_logic/basic/col/filter/configurable/TestConfigurableOrFilter.java:65  PolymorphicConfiguration<FalseFilter> result = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);

com.top_logic.bpe (1)
com.top_logic.bpe :: bpe/layout/execution/command/FinishTaskCommand.java:99  PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();

com.top_logic.demo (1)
com.top_logic.demo :: demo/layout/form/demo/TestTableViewPaneComponent.java:78  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

com.top_logic.dob (1)
com.top_logic.dob :: dob/attr/storage/ConfiguredInstanceStorage.java:135  (PolymorphicConfiguration<Object>) TypedConfiguration.newConfigItem(configurationInterface);

com.top_logic.doc (2)
com.top_logic.doc :: doc/command/GenerateDocumentationCommand.java:72  PolymorphicConfiguration<DocumentationGenerator> getGenerator();
com.top_logic.doc :: doc/command/validation/PageValidators.java:25  List<PolymorphicConfiguration<PageValidator>> getValidators();

com.top_logic.dsa (1)
com.top_logic.dsa :: dsa/repos/RepositoryDataSourceAdaptor.java:59  PolymorphicConfiguration<Repository> getRepository();

com.top_logic.element (60)
com.top_logic.element :: element/boundsec/manager/StorageAccessManager.java:112  PolymorphicConfiguration<SecurityStorage> getStorage();
com.top_logic.element :: element/boundsec/manager/StorageAccessManager.java:117  PolymorphicConfiguration<ElementSecurityUpdateManager> getUpdateManager();
com.top_logic.element :: element/config/annotation/TLConstraint.java:45  PolymorphicConfiguration<AttributedValueFilter> getFilter();
com.top_logic.element :: element/config/annotation/TLConstraint.java:48  void setFilter(PolymorphicConfiguration<AttributedValueFilter> value);
com.top_logic.element :: element/config/annotation/TLOptions.java:59  PolymorphicConfiguration<Generator> getGenerator();
com.top_logic.element :: element/config/annotation/TLOptions.java:62  void setGenerator(PolymorphicConfiguration<Generator> value);
com.top_logic.element :: element/layout/table/StructureReferenceListModelBuilder.java:60  PolymorphicConfiguration<StructureView<TLObject>> getStructure();
com.top_logic.element :: element/layout/table/StructuredElementAttributeListModelBuilder.java:36  PolymorphicConfiguration<StructureView<TLObject>> getStructure();
com.top_logic.element :: element/layout/table/tree/compare/AdapterRevisionCompareComponent.java:69  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic.element :: element/layout/formeditor/definition/GroupDefinition.java:51  PolymorphicConfiguration<ModeSelector> getModeSelector();
com.top_logic.element :: element/layout/formeditor/definition/GroupDefinition.java:56  void setModeSelector(PolymorphicConfiguration<ModeSelector> modeSelector);
com.top_logic.element :: element/layout/formeditor/implementation/GroupDefinitionTemplateProvider.java:78  PolymorphicConfiguration<ModeSelector> modeAnnotation = config.getModeSelector();
com.top_logic.element :: element/layout/create/CreateFormBuilder.java:125  PolymorphicConfiguration<CreateTypeOptions> getTypeOptions();
com.top_logic.element :: element/layout/structured/DefaultStructuredElementTreeModelBuilder.java:67  List<PolymorphicConfiguration<Filter<? super StructuredElement>>> getTreeFilters();
com.top_logic.element :: element/layout/structured/DefaultStructuredElementTreeModelBuilder.java:87  for (PolymorphicConfiguration<Filter<? super StructuredElement>> filter : treeFilters) {
com.top_logic.element :: element/layout/grid/AbstractGridCreateHandler.java:78  PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();
com.top_logic.element :: element/layout/grid/TableGridBuilder.java:67  PolymorphicConfiguration<ListModelBuilder> getBuilder();
com.top_logic.element :: element/layout/grid/GridComponent.java:263  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic.element :: element/layout/grid/GridComponent.java:266  PolymorphicConfiguration<FormContextModificator> getModifier();
com.top_logic.element :: element/layout/grid/GridComponent.java:269  PolymorphicConfiguration<GridApplyHandler> getGridApplyHandlerClass();
com.top_logic.element :: element/layout/grid/GridComponent.java:272  PolymorphicConfiguration<GridRowSecurityObjectProvider> getRowSecurityProviderClass();
com.top_logic.element :: element/layout/grid/GridComponent.java:584  PolymorphicConfiguration<FormContextModificator> modifierConfig) {
com.top_logic.element :: element/layout/grid/GenericStructureCreateHandler.java:53  PolymorphicConfiguration<CreateContextSelector> getContextSelector();
com.top_logic.element :: element/layout/grid/GridExcelExportHandler.java:53  PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();
com.top_logic.element :: element/layout/grid/TreeGridBuilder.java:43  PolymorphicConfiguration<TreeModelBuilder<Object>> getBuilder();
com.top_logic.element :: element/layout/grid/StructureGridBuilder.java:46  PolymorphicConfiguration<StructureModelBuilder<Object>> getBuilder();
com.top_logic.element :: element/layout/meta/ControlProviderModificator.java:60  PolymorphicConfiguration<ControlProvider> getControlProvider();
com.top_logic.element :: element/layout/meta/search/AttributedSearchResultComponent.java:57  PolymorphicConfiguration<SearchModelBuilder> getMetaElementBuilder();
com.top_logic.element :: element/layout/meta/search/AttributedSearchResultComponent.java:76  PolymorphicConfiguration<SearchModelBuilder> metaElementBuilder = someAttrs.getMetaElementBuilder();
com.top_logic.element :: element/model/diff/config/UpdateStorageMapping.java:34  PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();
com.top_logic.element :: element/model/diff/config/UpdateStorageMapping.java:37  void setStorageMapping(PolymorphicConfiguration<StorageMapping<?>> value);
com.top_logic.element :: element/model/diff/compare/CreateModelPatch.java:450  (PolymorphicConfiguration<StorageMapping<?>>) InstanceAccess.INSTANCE
com.top_logic.element :: element/model/cache/ElementModelCacheService.java:28  PolymorphicConfiguration<TLModelCache> getCache();
com.top_logic.element :: element/model/export/ModelConfigExtractor.java:148  PolymorphicConfiguration<StorageMapping<?>> storageConfig =
com.top_logic.element :: element/model/export/ModelConfigExtractor.java:149  (PolymorphicConfiguration<StorageMapping<?>>) InstanceAccess.INSTANCE.getConfig(model.getStorageMapping());
com.top_logic.element :: element/model/migration/Ticket27999InsertAbstractColumn.java:38  PolymorphicConfiguration<MigrationProcessor> getCreateColumnProcessor();
com.top_logic.element :: element/model/migration/model/UpdateTLDataTypeProcessor.java:95  PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();
com.top_logic.element :: element/model/migration/model/UpdateTLDataTypeProcessor.java:100  void setStorageMapping(PolymorphicConfiguration<StorageMapping<?>> value);
com.top_logic.element :: element/model/migration/model/UpdateTLDataTypeProcessor.java:157  PolymorphicConfiguration<StorageMapping<?>> storageMapping = getConfig().getStorageMapping();
com.top_logic.element :: element/model/migration/model/CreateTLDatatypeProcessor.java:76  PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();
com.top_logic.element :: element/model/migration/model/CreateTLDatatypeProcessor.java:81  void setStorageMapping(PolymorphicConfiguration<StorageMapping<?>> value);
com.top_logic.element :: element/model/migration/model/refactor/MakeColumnAttributeProcessor.java:109  PolymorphicConfiguration<ValueConverter> getValueConverter();
com.top_logic.element :: element/meta/kbbased/TLConstraintFactory.java:38  PolymorphicConfiguration<AttributedValueFilter> filterConfig = tlAnnotation.getFilter();
com.top_logic.element :: element/meta/kbbased/TLOptionsFactory.java:45  PolymorphicConfiguration<Generator> generatorConfig = tlAnnotation.getGenerator();
com.top_logic.element :: element/meta/kbbased/TLDefaultProviderFactory.java:41  PolymorphicConfiguration<DefaultProvider> providerConfig = tlAnnotation.getProvider();
com.top_logic.element :: element/meta/kbbased/filtergen/FilterFactory.java:65  PolymorphicConfiguration<AttributedValueFilter> getImpl();
com.top_logic.element :: element/meta/kbbased/filtergen/GeneratorFactory.java:92  PolymorphicConfiguration<GeneratorSpi> getImpl();
com.top_logic.element :: element/meta/kbbased/filtergen/GeneratorFactory.java:115  PolymorphicConfiguration<Generator> getImpl();
com.top_logic.element :: element/meta/kbbased/storage/PrimitiveStorage.java:74  PolymorphicConfiguration<StorageMapping<?>> getStorageMapping();
com.top_logic.element :: element/meta/kbbased/storage/PrimitiveStorage.java:77  void setStorageMapping(PolymorphicConfiguration<StorageMapping<?>> value);
com.top_logic.element :: element/meta/kbbased/storage/DirectStorage.java:38  PolymorphicConfiguration<StorageImplementation> getDefaultStorage();
com.top_logic.element :: element/meta/kbbased/storage/mappings/JSONStorageMapping.java:39  PolymorphicConfiguration<ValueAnalyzer> getAnalyzer();
com.top_logic.element :: element/meta/kbbased/storage/mappings/JSONStorageMapping.java:47  PolymorphicConfiguration<ValueFactory> getFactory();
com.top_logic.element :: element/meta/form/component/EditAttributedComponent.java:102  PolymorphicConfiguration<FormContextModificator> getModifier();
com.top_logic.element :: element/meta/form/component/EditAttributedComponent.java:172  PolymorphicConfiguration<FormContextModificator> modifierConfig) {
com.top_logic.element :: element/meta/form/fieldprovider/ValueDisplayFieldProvider.java:39  PolymorphicConfiguration<Renderer<?>> getRenderer();
com.top_logic.element :: element/meta/form/fieldprovider/form/TLFormType.java:33  PolymorphicConfiguration<FormTypeResolver> getFunction();
com.top_logic.element :: element/meta/form/fieldprovider/format/JSONFormatProvider.java:43  PolymorphicConfiguration<ValueAnalyzer> getAnalyzer();
com.top_logic.element :: element/meta/form/fieldprovider/format/JSONFormatProvider.java:51  PolymorphicConfiguration<ValueFactory> getFactory();
com.top_logic.element :: element/meta/gui/CreateAttributedComponent.java:65  PolymorphicConfiguration<FormContextModificator> getModifier();

com.top_logic.graph.diagramjs.server (1)
com.top_logic.graph.diagramjs.server :: graph/diagramjs/server/DiagramJSGraphComponent.java:138  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

com.top_logic.graph.server (1)
com.top_logic.graph.server :: graph/server/ui/AbstractGraphComponent.java:41  PolymorphicConfiguration<GraphDropTarget> getGraphDrop();

com.top_logic.importer (1)
com.top_logic.importer :: importer/excel/AbstractExcelFileImportParser.java:77  PolymorphicConfiguration<ImportParserPostProcessor> getPostProcessor();

com.top_logic.kafka (6)
com.top_logic.kafka :: kafka/layout/sensors/ProgressTableComponent.java:44  PolymorphicConfiguration<TableComponentValueUpdater> getUpdater();
com.top_logic.kafka :: kafka/layout/sensors/ProgressTableComponent.java:104  PolymorphicConfiguration<TableComponentValueUpdater> theUpdater = this.getConfig().getUpdater();
com.top_logic.kafka :: kafka/layout/kafka/ProgressTreeTableComponent.java:46  PolymorphicConfiguration<TreeTableComponentValueUpdater> getUpdater();
com.top_logic.kafka :: kafka/layout/kafka/ProgressTreeTableComponent.java:110  PolymorphicConfiguration<TreeTableComponentValueUpdater> theUpdater = getConfig().getUpdater();
com.top_logic.kafka :: kafka/services/common/CommonClientConfig.java:852  PolymorphicConfiguration<KafkaLogWriter<? super V>> getLogWriter();
com.top_logic.kafka :: kafka/services/consumer/ConsumerDispatcherConfiguration.java:239  List<PolymorphicConfiguration<ConsumerProcessor<K,V>>> getProcessors();

com.top_logic.kafka.sync (4)
com.top_logic.kafka.sync :: kafka/sync/serialization/ChangeSetDeserializer.java:56  PolymorphicConfiguration<TypeMapping> getTypeMapping();
com.top_logic.kafka.sync :: kafka/sync/knowledge/service/KafkaExportImportConfiguration.java:41  PolymorphicConfiguration<KafkaImportConfiguration> getImport();
com.top_logic.kafka.sync :: kafka/sync/knowledge/service/KafkaExportImportConfiguration.java:47  PolymorphicConfiguration<KafkaExportConfiguration> getExport();
com.top_logic.kafka.sync :: kafka/sync/knowledge/service/importer/KBDataProcessor.java:57  List<PolymorphicConfiguration<EventRewriter>> getRewriters();

com.top_logic.layout.scripting.template (3)
com.top_logic.layout.scripting.template :: layout/scripting/template/gui/ScriptRecorderTree.java:114  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic.layout.scripting.template :: layout/scripting/template/gui/ScriptRecorderTree.java:121  PolymorphicConfiguration<TreeDragSource> getTreeDragSource();
com.top_logic.layout.scripting.template :: layout/scripting/template/gui/ScriptRecorderTree.java:128  PolymorphicConfiguration<TreeDropTarget> getTreeDropTarget();

com.top_logic.layout.view (2)
com.top_logic.layout.view :: layout/view/element/TreeElement.java:241  PolymorphicConfiguration<ReactControlProvider> getNodeContent();
com.top_logic.layout.view :: layout/view/element/FormElement.java:198  PolymorphicConfiguration<LockHandler> getLockHandler();

com.top_logic.model.search (8)
com.top_logic.model.search :: model/search/tiles/ModelBasedTilePreview.java:85  PolymorphicConfiguration<TilePreviewPartProvider.Text> getLabel();
com.top_logic.model.search :: model/search/tiles/ModelBasedTilePreview.java:97  PolymorphicConfiguration<TilePreviewPartProvider.Text> getDescription();
com.top_logic.model.search :: model/search/tiles/ModelBasedTilePreview.java:109  PolymorphicConfiguration<TilePreviewPartProvider.Content> getContentPreview();
com.top_logic.model.search :: model/search/expr/config/SearchBuilder.java:147  List<PolymorphicConfiguration<MethodResolver>> getMethodResolvers();
com.top_logic.model.search :: model/search/providers/DropTargetByExpressionConfig.java:72  List<PolymorphicConfiguration<PostCreateAction>> getPostCreateActions();
com.top_logic.model.search :: model/search/providers/ColumnAdditionalHeaders.java:58  List<PolymorphicConfiguration<HTMLFragmentProvider>> getAdditionalHeaders();
com.top_logic.model.search :: model/search/providers/LabelByExpression.java:78  PolymorphicConfiguration<ResourceProvider> getInner();
com.top_logic.model.search :: model/search/providers/GridCreateHandlerByExpression.java:60  PolymorphicConfiguration<CreateTypeOptions> getTypeOptions();

com.top_logic.monitoring (1)
com.top_logic.monitoring :: monitoring/log/LogParser.java:160  PolymorphicConfiguration<LogLineFilter> getFilter();

com.top_logic.reporting (5)
com.top_logic.reporting :: reporting/data/processing/transformator/TransformatorFactory.java:50  interface Transformator extends NamedConfigMandatory, PolymorphicConfiguration<EntryExtractor> {
com.top_logic.reporting :: reporting/report/view/component/ExtendedProducerChartComponent.java:80  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic.reporting :: reporting/chart/gantt/component/GanttChartFilterComponent.java:88  PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
com.top_logic.reporting :: reporting/chart/gantt/component/GanttComponent.java:84  PolymorphicConfiguration<AbstractGanttChartCreator> getChartCreator();
com.top_logic.reporting :: reporting/chart/gantt/component/GanttComponent.java:88  PolymorphicConfiguration<GanttChartExporter> getChartExporter();

com.top_logic.reporting.flex (1)
com.top_logic.reporting.flex :: reporting/flex/chart/config/datasource/ListModelBuilderProducer.java:52  public PolymorphicConfiguration<ListModelBuilder> getListModelBuilder();

com.top_logic.xio (6)
com.top_logic.xio :: xio/importer/handlers/PropertyImport.java:46  PolymorphicConfiguration<ConfigurationValueProvider<?>> getFormat();
com.top_logic.xio :: xio/importer/handlers/TextContentImport.java:40  PolymorphicConfiguration<ConfigurationValueProvider<?>> getFormat();
com.top_logic.xio :: xio/importer/handlers/SwitchImportHandler.java:43  List<PolymorphicConfiguration<ConditionalHandler<?>>> getCases();
com.top_logic.xio :: xio/importer/handlers/Handler.java:45  PolymorphicConfiguration<Handler> getHandler();
com.top_logic.xio :: xio/importer/handlers/Linker.java:60  List<PolymorphicConfiguration<ObjectLinking>> getLinkings();
com.top_logic.xio :: xio/importer/handlers/Linker.java:78  List<PolymorphicConfiguration<ObjectLinking>> linkingConfigs = config.getLinkings();

tl-service-jms (1)
tl-service-jms :: services/jms/JMSService.java:72  PolymorphicConfiguration<MQSystemClient> getMQSystemClient();