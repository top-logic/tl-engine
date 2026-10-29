def properties = request.getProperties();
def ui = properties.getProperty("ui");
def useBPE = properties.getProperty("useBPE");
def useAI = properties.getProperty("useAI");
def useAISearch = properties.getProperty("useAISearch");
def moduleType = properties.getProperty("moduleType");

def uiValues = ["react", "classic", "both"];
if (!uiValues.contains(ui)) {
	throw new IllegalArgumentException("Invalid value ui=" + ui + ", expected one of " + uiValues.join(", ") + ".");
}
def moduleTypes = ["app", "fragment"];
if (!moduleTypes.contains(moduleType)) {
	throw new IllegalArgumentException("Invalid value moduleType=" + moduleType + ", expected one of " + moduleTypes.join(", ") + ".");
}
if (ui == "react" && useBPE == "true") {
	throw new IllegalArgumentException("BPE has no React UI, use ui=both (or useBPE=false).");
}
if (useAISearch == "true" && useAI != "true") {
	throw new IllegalArgumentException("The AI search is a part of the AI support, use useAI=true (or useAISearch=false).");
}

def projectDir = new File(request.outputDirectory, request.getArtifactId());

def remove = { path ->
	def file = new File(projectDir, path);
	if (file.isDirectory()) {
		file.deleteDir();
	} else {
		file.delete();
	}
};

// The template contains the files of all user interfaces; those not belonging to the chosen one are removed.
def uiSpecific = [
	"react": [
		"src/main/webapp/WEB-INF/views",
	],
	"classic": [
		"src/main/webapp/WEB-INF/layouts",
		"bin/launch/Layout Inline Selected.launch",
	],
];
uiSpecific.each { owner, paths ->
	if (ui != owner && ui != "both") {
		paths.each(remove);
	}
}

// The template is an application; a fragment has no deployment, no start configuration and no shell of its own.
def appOnly = [
	"src/main/webapp/WEB-INF/web.xml",
	"src/main/webapp/WEB-INF/views/app.view.xml",
	"deploy",
	"src/main/deb",
	"src/main/docker",
	"bin/image.sh",
	"local.conf.xml.template",
	".mvn/jvm.config",
	"bin/launch/Start " + properties.getProperty("DisplayName") + ".launch",
	"bin/launch/Setup " + properties.getProperty("DisplayName") + ".launch",
];
def fragmentOnly = [
	"src/main/java/META-INF/web-fragment.xml",
];
(moduleType == "fragment" ? appOnly : fragmentOnly).each(remove);

// The Maven configuration directory holds the JVM options starting the application only.
def mvnDir = new File(projectDir, ".mvn");
if (mvnDir.isDirectory() && mvnDir.list().length == 0) {
	mvnDir.delete();
}

def imageScript = new File(projectDir, "bin/image.sh");
if (imageScript.isFile()) {
	imageScript.setExecutable(true, false);
}
