# How to build after an update

For Eclipse to index class files during compilation, the file `type-indexer.jar` must be build containing the module with all its dependencies. To achive this, do the following steps: 

## Build dependencies and tl-build_processor and move the result to `type-indexer.jar`

```
cd com.top_logic.basic.shared && mvn clean install && cd ..
cd com.top_logic.common.json && mvn clean install && cd ..
cd com.top_logic.xref && mvn clean install && cd ..
cd tl-build_processor && mvn clean install && mv target/tl-build-processor-7.6.0-SNAPSHOT-jar-with-dependencies.jar type-indexer.jar && cd ..
```



