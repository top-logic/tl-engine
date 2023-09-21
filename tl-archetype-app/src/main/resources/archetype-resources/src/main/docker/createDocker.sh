#!/bin/bash

# SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0

# Determine paths
SRC_PATH=$(dirname $(readlink -f "$0"))
ROOT_PATH=$(realpath -s "$SRC_PATH/../../..")
TARGET_PATH="$ROOT_PATH/target"
BUILD_PATH="$TARGET_PATH/docker"

# Check Maven installation
mvn --help >/dev/null 2>&1
[[ $(echo $?) != "0" ]] && echo "Maven is missing. Please install with 'sudo apt install maven'." && exit 0;

# Test Docker availibility
docker --help >/dev/null 2>&1
DOCKER=$(echo $?)

# Test Podman availibility
podman --help >/dev/null 2>&1
PODMAN=$(echo $?)

set -e

# Decide which container command to use
if [[ "$DOCKER" == "0" ]] ; then
	RUN="sudo docker"
elif [[ "$PODMAN" == "0" ]] ; then
	RUN="podman"
else
	echo "Docker and Podman are missing. Pls install one of these container engines.
	Use 'sudo apt install docker.io'
	Or 'sudo apt install podman'";
	exit 0;
fi

show_help(){
echo -e "Usage:\tcreateDocker.sh [OPTIONS...]
Available options:
  -h|--help \t\t\tThis help.
  -d|--dry-run \t\t\tDry-run. Create docker config and exit.
  -f|--config [PathToFile]\tPath to the parameter file to load. 
                          \tDefault: $PARAMETER_FILE
  --mvn-options \t\tOptions passed to the Maven build.
  --skip-build \t\t\tSkip maven build, assume WAR already in place.
  --skip-container \t\tSkip container build, reuse existing container.
  -s|--start \t\t\tStart the container locally.\t\t\t
  -t|--tag [Tag]\t\tTag for the docker image.\t\t
  -p|--push \t\t\tPush image to the configured docker registry";
}

PARAMETER_FILE="$SRC_PATH/BuildParameters"

# Load default build parameters
if [ -f "$PARAMETER_FILE"  ] ; then
	. "$PARAMETER_FILE"
fi

while [[ $# -gt 0 ]]; do
	case $1 in
		-h|--help) 
			show_help
			exit 0
			;;
	    -d|--dry-run) 
	    	CREATE_ONLY="true"
	    	shift
	    	;;
	    --mvn-options)
	    	MVN_OPTS="$2"
	    	shift
	    	shift
	    	;;
	    --skip-build)
	    	SKIP_BUILD="true"
	    	shift
	    	;;
	    --skip-container)
	    	SKIP_BUILD="true"
	    	SKIP_CONTAINER="true"
	    	shift
	    	;;
		-s|--start) 
			START_LOCAL="true"
	    	shift
			;;
		-f|--config) 
			PARAMETER_FILE="$2"

			# Load build parameter file
			if [ "$PARAMETER_FILE" != "" ] ; then
				if [ -f "$PARAMETER_FILE"  ] ; then
					. "$PARAMETER_FILE"
				else
					echo "ERROR: Parameter file not found: $PARAMETER_FILE"
					exit 1
				fi
			fi
			
	    	shift
	    	shift
			;;
		-t|--tag) 
			DOCKER_TAG="$2"
	    	shift
	    	shift
			;;
		-p|--push) 
			DOCKER_PUSH="true"
	    	shift
			;;
		*)	echo "Unknown option: $1"
			echo
		    show_help
		    exit 1
			;;
	esac
done

# Set defaults for empty parameters
[ -z "$APPNAME" ]		&& APPNAME="toplogic"
[ -z "$CONTEXT" ]		&& CONTEXT="ROOT"
[ -z "$HTTP_PORT" ]		&& HTTP_PORT="8080"
[ -z "$FILES" ]			&& FILES="$APPNAME"
[ -z "$START_LOCAL" ]	&& START_LOCAL="false"
[ -z "$CREATE_ONLY" ]	&& CREATE_ONLY="false"
[ -z "$DATABASE" ]		&& DATABASE="h2"
[ -z "$JAVA_XMS" ]		&& JAVA_XMS="256"
[ -z "$JAVA_XMX" ]		&& JAVA_XMX="1024"
[ -z "$CPUS" ]			&& CPUS="1.0"
[ -z "$DOCKER_REGISTRY" ] && DOCKER_REGISTRY="hub.docker.com"
[ -z "$DOCKER_IMAGE_NAME" ]	&& DOCKER_IMAGE_NAME="$APPNAME"
[ -z "$DOCKER_TAG" ]	&& DOCKER_TAG="latest"
[ -z "$DOCKER_PUSH" ]	&& DOCKER_PUSH="false"
DOCKER_MEMORY=$(( JAVA_XMX*2/10 > 200 ? JAVA_XMX+200 : JAVA_XMX*12/10 ))


# Database setup functions
h2(){
  echo "Configuring H2 database"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$APPNAME
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_URL" ] && DB_URL="\/var\/lib\/tomcat9\/work\/$APPNAME\/h2" || DB_URL="$(sed 's/\//\\\//g' <<<$DB_URL)"
  sed -i -e "s/{dbLibrary}/RUN apt install libh2-java/g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.h2.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:h2:$DB_URL/g" $BUILD_PATH/context.xml
}

mysql(){
  echo "Configuring MySQL database"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$APPNAME
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_HOST" ] && DB_HOST=$LOCAL_IP
  [ -z "$DB_PORT" ] && DB_PORT=3306
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT\/$DB_SCHEME"
  sed -i -e 's/{dbLibrary}/RUN apt install libmariadb-java/g' $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.mariadb.jdbc.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:mysql:\/\/$DB_URL/g" $BUILD_PATH/context.xml
}

mssql(){
  echo "Configuring MSSQL database"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$APPNAME
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_HOST" ] && DB_HOST=$LOCAL_IP
  [ -z "$DB_PORT" ] && DB_PORT=1433
  [ -z "$DB_URL" ] && DB_URL=";serverName=$DB_HOST;port=$DB_PORT;databaseName=$DB_SCHEME;encrypt=false;"
# HowTo https://learn.microsoft.com/en-us/sql/connect/jdbc/using-the-jdbc-driver?view=sql-server-ver16#make-a-simple-connection-to-a-database
# Download v12.2.0 from https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-ver16
  wget https://go.microsoft.com/fwlink/?linkid=2222954 -O $BUILD_PATH/mssql.tar.gz
  tar -xvzf $BUILD_PATH/mssql.tar.gz -C $BUILD_PATH/
  rm -f $BUILD_PATH/mssql.tar.gz
  mv $BUILD_PATH/sqljdbc_12.2/enu/mssql-jdbc-12.2.0.jre11.jar $BUILD_PATH/
  rm -rf $BUILD_PATH/sqljdbc_12.2
  sed -i -e "s/{dbLibrary}/COPY --chown=root .\/mssql-jdbc-12.2.0.jre11.jar \/usr\/share\/java\//g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/com.microsoft.sqlserver.jdbc.SQLServerDriver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:sqlserver:\/\/$DB_URL/g" $BUILD_PATH/context.xml
}

postgre(){
  echo "Configuring PostgreSQL database"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$APPNAME
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_HOST" ] && DB_HOST=$LOCAL_IP
  [ -z "$DB_PORT" ] && DB_PORT=5432
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT\/$DB_SCHEME"
  sed -i -e "s/{dbLibrary}/RUN apt install libpostgresql-jdbc-java/g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.postgresql.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:postgresql:\/\/$DB_URL/g" $BUILD_PATH/context.xml
}

oracle(){
  echo "Configuring Oracle database"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=/$APPNAME
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_HOST" ] && DB_HOST=$LOCAL_IP
  [ -z "$DB_PORT" ] && DB_PORT=1521
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT$(sed 's/\//\\\//g' <<<$DB_SCHEME)"
# HowTo https://docs.oracle.com/en/cloud/paas/autonomous-database/dedicated/adbbz/#articletitle
# Download https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
  wget https://download.oracle.com/otn-pub/otn_software/jdbc/232-DeveloperRel/ojdbc11.jar -O $BUILD_PATH/ojdbc11.jar
  sed -i -e "s/{dbLibrary}/COPY --chown=root .\/ojdbc11.jar \/usr\/share\/java\//g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/oracle.jdbc.driver.OracleDriver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:oracle:thin:@$DB_URL/g" $BUILD_PATH/context.xml
}


# Build & deploy
LOCAL_IP="$(hostname -I | awk '{print $1}')"

if [[ "$SKIP_BUILD" != "true" ]] ; then
	echo
	echo "=== Building application WAR ==="
	( cd "$ROOT_PATH" ; mvn clean package -Dtl.deb.skip=true $MVN_OPTS )
fi

mkdir -p "$BUILD_PATH"
cp -f "$TARGET_PATH"/*app.war "$BUILD_PATH/$CONTEXT.war"
cp -f "$ROOT_PATH/src/main/deb/data/tomcat/context.xml" "$BUILD_PATH/"

echo
echo "=== Creating dockerfile ==="
cp -f "$SRC_PATH/Dockerfile_template" "$BUILD_PATH/Dockerfile"
echo "Created dockerfile: $BUILD_PATH/Dockerfile"

echo
echo "=== Configuring database ==="
$DATABASE
sed -i -e "s/{dbUser}/$DB_USER/g" "$BUILD_PATH/context.xml"
sed -i -e "s/{dbPasswd}/$DB_PASSWD/g" "$BUILD_PATH/context.xml"
sed -i -e "s/{contextName}/$CONTEXT/g" "$BUILD_PATH/context.xml"
sed -i -e "s/{JAVA_XMS}/$JAVA_XMS/g" "$BUILD_PATH/Dockerfile"
sed -i -e "s/{JAVA_XMX}/$JAVA_XMX/g" "$BUILD_PATH/Dockerfile"
sed -i -e "s/{timeZone}/$(sed 's/\//\\\//g' <<<$(cat /etc/timezone))/g" "$BUILD_PATH/Dockerfile"

DRY_RUN=""
if [[ "$CREATE_ONLY" == "true" ]]; then
    echo "Stopping. The rest of the output is only a hint for possible commands"
    DRY_RUN="echo "
fi

if [[ "$SKIP_CONTAINER" != "true" ]] ; then
	echo
	echo "=== Log-in to docker registry ==="
	$DRY_RUN $RUN login docker.top-logic.com -u guest -p guest
	
	echo
	echo "=== Pulling base image ==="
	$DRY_RUN $RUN pull docker.top-logic.com/tomcat9-java11:latest 2> /dev/null
	
	echo
	echo "=== Building docker image ==="
	$DRY_RUN $RUN build -t "$APPNAME" "$BUILD_PATH/"
fi

if [[ "$DOCKER_PUSH" == "true" ]]; then
	echo
	echo "=== Push to docker registry ==="
	$DRY_RUN $RUN image tag "$APPNAME" "$DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$DOCKER_TAG"
	$DRY_RUN $RUN image push "$DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$DOCKER_TAG"

fi

if [[ "$CREATE_ONLY" != "true" && "$START_LOCAL" != "true" ]] ; then
	echo "Stopping. The rest of the output is only a hint for possible commands"
	DRY_RUN="echo "
fi

echo
echo "=== Removing old container ==="
$DRY_RUN $RUN rm -f "$APPNAME" || echo "Nothing to delete"

open_browser() {
  echo
  echo "=== Opening browser ==="

  if [[ "$CONTEXT" == "ROOT" ]]; then
	  contextPath="/"
  else
	  contextPath="/$CONTEXT"
  fi

  # Wait until container startup is at least in progress
  $DRY_RUN sleep 10
  
  # Open browser to log-in to started app.
  $DRY_RUN xdg-open "http://localhost:${HTTP_PORT}${contextPath}"
}

if [[ "$DRY_RUN" == "" ]] ; then
	# Start in background
	( open_browser ) &
else 
	open_browser
fi

echo
echo "=== Creating container ==="
[ -z "$HTTP_PORT" ] && HTTP_PORT=8080

mkdir -p "${FILES}"

function finish {
	echo
	echo "=== Stopping container ==="
	$DRY_RUN $RUN stop "$APPNAME"
}
trap finish EXIT 2

# Start container
$DRY_RUN $RUN run \
  -tdi -p $HTTP_PORT:8080 \
  -v "$FILES":"/var/lib/tomcat9/work/${APPNAME}" \
  --restart=unless-stopped \
  --memory ${DOCKER_MEMORY}m \
  --cpus $CPUS \
  --name="$APPNAME" \
  --hostname="$APPNAME" "$APPNAME" && $DRY_RUN $RUN logs -f "$APPNAME"

# When the control flow reaches this point, the user has pressed Ctrl-C, stop the contaner.

