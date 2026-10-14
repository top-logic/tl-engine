// The binding of module 'it.app' was generated in generate-sources and compiled in compile.
['it/app/Item', 'it/app/ItAppFactory', 'it/app/impl/ItemBase', 'it/app/impl/ItemImpl'].each { name ->
    File source = new File(basedir, "src/main/java/${name}.java")
    assert source.isFile() : "Not generated: ${source}"

    File compiled = new File(basedir, "target/classes/${name}.class")
    assert compiled.isFile() : "Not compiled: ${compiled}"
}

// The goal resolved the web fragments of the engine modules into the initially empty local
// repository.
String log = new File(basedir, 'build.log').text
assert !log.contains('Skipping web fragment') : 'A web fragment of an engine module was not resolved.'

File coreDir = new File(localRepositoryPath, 'com/top-logic/tl-core')
List<File> coreFragments = []
coreDir.eachFileRecurse { file ->
    if (file.name.endsWith('-web-fragment.war') && !file.name.endsWith('-tests-web-fragment.war')) {
        coreFragments << file
    }
}
assert !coreFragments.isEmpty() : "Web fragment of tl-core not resolved into ${coreDir}."
