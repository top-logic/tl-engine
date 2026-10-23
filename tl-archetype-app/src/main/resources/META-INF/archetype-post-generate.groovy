def properties = request.getProperties();
def ui = properties.getProperty("ui");
def useBPE = properties.getProperty("useBPE");

def uiValues = ["react", "classic", "both"];
if (!uiValues.contains(ui)) {
	throw new IllegalArgumentException("Invalid value ui=" + ui + ", expected one of " + uiValues.join(", ") + ".");
}
if (ui == "react" && useBPE == "true") {
	throw new IllegalArgumentException("BPE has no React UI, use ui=both (or useBPE=false).");
}

def projectDir = new File(request.outputDirectory, request.getArtifactId());

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
		paths.each { path ->
			def file = new File(projectDir, path);
			if (file.isDirectory()) {
				file.deleteDir();
			} else {
				file.delete();
			}
		}
	}
}

def file = new File(projectDir, "src/main/docker/createDocker.sh");
file.setExecutable(true, false);
