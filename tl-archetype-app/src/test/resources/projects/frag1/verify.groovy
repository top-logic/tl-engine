// Checks that the generated project contains exactly the files of the fragment with ui=react.
// The archetype integration test generates the project into the folder "project" of the test.
def project = new File(basedir, "project/my-fragment");
def expected = [
	".gitignore": true,
	"src/main/java/META-INF/web-fragment.xml": true,
	"src/main/webapp/WEB-INF/web.xml": false,
	"deploy": false,
	"src/main/deb": false,
	"src/main/docker": false,
	"local.conf.xml.template": false,
	".mvn": false,
	"src/main/webapp/WEB-INF/views/app.view.xml": false,
	"src/main/webapp/WEB-INF/views/README.md": true,
	"src/main/webapp/WEB-INF/layouts": false,
	"src/main/webapp/WEB-INF/model": true,
	"src/main/webapp/WEB-INF/kbase": true,
	"src/main/webapp/WEB-INF/conf/myfragment.conf.config.xml": true,
	"bin/launch/Start My Fragment.launch": false,
	"bin/launch/Setup My Fragment.launch": false,
	"bin/launch/Test My Fragment.launch": true,
	"bin/launch/Generate my.fragment.launch": true
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
if (text(pom, "packaging") != "tl-fragment") {
	throw new AssertionError("Expected packaging tl-fragment, found '" + text(pom, "packaging") + "'.");
}
if (child(pom, "parent") != null) {
	throw new AssertionError("Unexpected parent in " + project + "/pom.xml.");
}
def dependencies = children(child(pom, "dependencies"), "dependency").collect { text(it, "artifactId") } as Set;
def expectedDependencies = [
	"tl-starter-react": true,
	"tl-license-professional": false,
	"tl-starter-ai": false,
	"tl-layout-scripting-template": false
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
if (children(child(pom, "properties"), null).any { it.nodeName.startsWith("tl.deb.") }) {
	throw new AssertionError("Unexpected Debian package properties in " + project + "/pom.xml.");
}
def metaConf = new File(project, "src/main/webapp/WEB-INF/conf/metaConf.txt").readLines()*.trim();
if (metaConf != ["myfragment.conf.xml"]) {
	throw new AssertionError("Expected only the fragment's own configuration in metaConf.txt, found " + metaConf + ".");
}
