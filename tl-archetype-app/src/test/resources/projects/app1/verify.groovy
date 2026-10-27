// Checks that the generated project contains exactly the files of the application with ui=react.
// The archetype integration test generates the project into the folder "project" of the test.
def project = new File(basedir, "project/my-app");
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
	"src/main/webapp/WEB-INF/layouts": false,
	"bin/launch/Start My App.launch": true,
	"bin/launch/Setup My App.launch": true,
	"bin/launch/Layout Inline Selected.launch": false
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
	"tl-starter-classic": false,
	"tl-layout-view-selfservice": true,
	"tl-layout-react-chartjs": true,
	"tl-bpe-app": false,
	"tl-starter-ai": true,
	"tl-chat-classic": false,
	"tl-starter-ai-search": true,
	"tl-security-auth-pac4j": true,
	"tl-service-openapi-client": true,
	"tl-service-openapi-server": true
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
def metaConf = new File(project, "src/main/webapp/WEB-INF/conf/metaConf.txt").readLines()*.trim();
if (metaConf != ["db-default.xml", "myapp.conf.xml"]) {
	throw new AssertionError("Unexpected metaConf.txt " + metaConf + ".");
}
// The browser tab is titled by the application title tl.title.
def viewConfigs = { conf ->
	def configs = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(conf).documentElement;
	children(child(configs, "configs"), "config").findAll { it.getAttribute("config:interface") == "com.top_logic.layout.view.ViewConfig" }
};
def appViewConfigs = viewConfigs(new File(project, "src/main/webapp/WEB-INF/conf/myapp.conf.config.xml"));
if (appViewConfigs*.getAttribute("title") != ["tl.title"]) {
	throw new AssertionError("Expected the view title tl.title in myapp.conf.config.xml.");
}
