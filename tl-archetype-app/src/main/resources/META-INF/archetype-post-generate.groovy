def file = new File(request.outputDirectory, request.getArtifactId()+"/src/main/docker/createDocker.sh" );
file.setExecutable(true, false);