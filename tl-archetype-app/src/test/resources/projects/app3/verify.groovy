// Checks that the generated project contains exactly the files of the application with ui=both.
// The archetype integration test generates the project into the folder "project" of the test.
def project = new File(basedir, "project/both-app");
def expected = [
	".gitignore": true,
	"src/main/webapp/WEB-INF/web.xml": true,
	"deploy": true,
	"src/main/deb": true,
	"src/main/docker": true,
	"local.conf.xml.template": true,
	".mvn/jvm.config": true,
	"src/main/java/META-INF/web-fragment.xml": false,
	"src/main/webapp/WEB-INF/model": true,
	"src/main/webapp/WEB-INF/kbase": true,
	"src/main/webapp/WEB-INF/views/app.view.xml": true,
	"src/main/webapp/WEB-INF/layouts": true,
	"bin/launch/Layout Inline Selected.launch": true
];
expected.each { path, present ->
	def file = new File(project, path);
	if (file.exists() != present) {
		throw new AssertionError((present ? "Missing " : "Unexpected ") + file);
	}
}

// The child elements of a DOM element with the given name.
def children = { element, name ->
	def nodes = element == null ? [] : (0..<element.childNodes.length).collect { element.childNodes.item(it) };
	nodes.findAll { it.nodeType == org.w3c.dom.Node.ELEMENT_NODE && (name == null || it.nodeName == name) }
};
def child = { element, name -> children(element, name).find { true } };
def text = { element, name -> def c = child(element, name); c == null ? "" : c.textContent.trim() };

def pom = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
	.parse(new File(project, "pom.xml")).documentElement;
if (text(pom, "packaging") != "tl-app") {
	throw new AssertionError("Expected packaging tl-app, found '" + text(pom, "packaging") + "'.");
}
if (child(pom, "parent") != null) {
	throw new AssertionError("Unexpected parent in " + project + "/pom.xml.");
}
def dependencies = children(child(pom, "dependencies"), "dependency").collect { text(it, "artifactId") } as Set;
def expectedDependencies = [
	"tl-starter-react": true,
	"tl-starter-classic": true,
	"tl-bpe-app": true,
	"tl-starter-ai": false,
	"tl-chat-classic": false,
	"tl-starter-ai-search": false,
	"tl-security-auth-pac4j": false,
	"tl-layout-scripting-template": true
];
expectedDependencies.each { artifactId, present ->
	if (dependencies.contains(artifactId) != present) {
		throw new AssertionError((present ? "Missing" : "Unexpected") + " dependency " + artifactId + " in " + project + "/pom.xml.");
	}
}
def buildPlugins = children(child(child(pom, "build"), "plugins"), "plugin").collect { text(it, "artifactId") };
if (buildPlugins != ["tl-maven-plugin"]) {
	throw new AssertionError("Expected only the build plugin tl-maven-plugin, found " + buildPlugins + ".");
}

// The artifact IDs of all dependencies the build resolved, transitive ones included (written by
// the goal dependency:list into target/dependencies.txt).
def resolved = new File(project, "target/dependencies.txt").readLines()
	.collect { (it =~ /^\s+[^:\s]+:([^:\s]+):/) }
	.findAll { it.find() }
	.collect { it.group(1) } as Set;
if (resolved.isEmpty()) {
	throw new AssertionError("No resolved dependencies found in " + project + "/target/dependencies.txt.");
}
// Modules of the classic UI an application with the classic UI must contain.
def classicModules = [
	"tl-layout-wysiwyg",
	"tl-model-wysiwyg",
	"ext-com-ckeditor"
];
def missing = classicModules.findAll { !resolved.contains(it) };
if (!missing.isEmpty()) {
	throw new AssertionError("Classic UI modules " + missing + " missing in the dependencies of " + project + ".");
}
