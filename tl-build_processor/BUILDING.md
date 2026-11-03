# How to build after an update

For Eclipse to index class files during compilation, the file `type-indexer.jar` must be built containing the module with all its dependencies. To achieve this, do the following steps from the project root:

## Build dependencies and tl-build_processor and copy the result to `type-indexer.jar`

```
mvn clean install -pl com.top_logic.basic.shared,com.top_logic.common.json,com.top_logic.xref,tl-build_processor
cp tl-build_processor/target/tl-build-processor-*-jar-with-dependencies.jar tl-build_processor/type-indexer.jar
```



