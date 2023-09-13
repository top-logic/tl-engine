#!/bin/bash
# Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.

mvn --help >/dev/null 2>&1
[[ $(echo $?) != "0" ]] && echo "Maven is missing. Pls install with 'apt install maven'." && exit 0;
docker --help >/dev/null 2>&1
DOCKER=$(echo $?)
podman --help >/dev/null 2>&1
PODMAN=$(echo $?)

if [[ "$DOCKER" == "0" ]]; then
RUN="sudo docker"
elif [[ "$PODMAN" == "0" ]]; then
RUN="podman"
else
echo "Docker and Podman are missing. Pls install one of these container engines.
Use 'apt install docker.io'
Or 'apt install podman'";
exit 0;
fi

error_param(){
echo -e "Usage:\tcreateDocker.sh [OPTIONS...]
Run parameter:
  -h \t\t\tThis help.
  -d \t\t\tDry-run. Create docker config and exit. Default off
  -s \t\t\tStart the App locally.\t\t\t Default on
  -f [PathToFile]\tThe Path to the parameter file to load. Default: ./BuildParameters
  -t [Tag]\t\tTag for the docker image.\t\tDefault: latest
  -p \t\t\tPush image to the configured Docker registry";
exit 0;
}

PARAMETER_FILE=""

while getopts hdsf:t: opt
do
   case $opt in
   	   h) error_param;;
       d) CREATE_ONLY="true";;
       s) START_LOCAL="true";;
	   f) PARAMETER_FILE="$OPTARG";;
	   t) DOCKER_TAG="$OPTARG";;
	   p) DOCKER_PUSH="true";;
       *) error_param;;
   esac
done

[ -z "$PARAMETER_FILE" ] && PARAMETER_FILE="./BuildParameters"
# Load build parameter file
if [ -f "$PARAMETER_FILE"  ] ; then
	. $PARAMETER_FILE
fi

# Set defaults for empty parameters
[ -z "$APPNAME" ]		&& APPNAME="toplogic"
[ -z "$CONTEXT" ]		&& CONTEXT="ROOT"
[ -z "$HTTP_PORT" ]		&& HTTP_PORT="8080"
[ -z "$FILES" ]			&& FILES="$APPNAME"
[ -z "$START_LOCAL" ]	&& START_LOCAL="true"
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
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT/$DB_SCHEME"
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
  cd $BUILD_PATH/
  wget https://go.microsoft.com/fwlink/?linkid=2222954 -O mssql.tar.gz
  tar -xvzf mssql.tar.gz
  rm -f mssql.tar.gz
  mv ./sqljdbc_12.2/enu/mssql-jdbc-12.2.0.jre11.jar ./
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
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT/$DB_SCHEME"
  sed -i -e "s/{dbLibrary}/RUN apt install libpostgresql-jdbc-java/g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.postgresql.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:postgresql:\/\/$DB_URL/g" $BUILD_PATH/context.xml
}

oracle(){
  echo "Configuring Oracle database"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$APPNAME
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_HOST" ] && DB_HOST=$LOCAL_IP
  [ -z "$DB_PORT" ] && DB_PORT=1521
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT$(sed 's/\//\\\//g' <<<$DB_SCHEME)"
# HowTo https://docs.oracle.com/en/cloud/paas/autonomous-database/dedicated/adbbz/#articletitle
# Download https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
  wget https://download.oracle.com/otn-pub/otn_software/jdbc/232-DeveloperRel/ojdbc11.jar -O ojdbc11.jar
  sed -i -e "s/{dbLibrary}/COPY --chown=root .\/ojdbc11.jar \/usr\/share\/java\//g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/oracle.jdbc.driver.OracleDriver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:oracle:thin:@$DB_URL/g" $BUILD_PATH/context.xml
}


# Build & deploy
LOCAL_IP="$(hostname -I | awk '{print $1}')"
SRC_PATH=$(dirname $(readlink -f "$0"))
ROOT_PATH=$(realpath -s "$SRC_PATH/../../..")
TARGET_PATH="$ROOT_PATH/target"
BUILD_PATH="$TARGET_PATH/docker"

rm "$BUILD_PATH"/*.war 2> /dev/null
mkdir -p "$BUILD_PATH"

echo
echo "=== Building application WAR ==="
( cd "$ROOT_PATH" ; mvn package -Dtl.deb.skip=true )
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
sed -i -e "s/{timeZone}/$(sed 's/\//\\\//g' <<<$(cat /etc/timezone))/g" "$BUILD_PATH/Dockerfile"

DRY_RUN=""
if [[ "$CREATE_ONLY" == "true" ]]; then
    echo "Stopping. The rest of the output is only a hint for possible commands"
    DRY_RUN="echo "
fi

echo
echo "=== Log-in to docker registry ==="
$DRY_RUN $RUN login docker.top-logic.com -u guest -p guest

echo
echo "=== Pulling base image ==="
$DRY_RUN $RUN pull docker.top-logic.com/tomcat9-java11:latest 2> /dev/null

echo
echo "=== Building docker image ==="
$DRY_RUN $RUN build -t "$APPNAME" "$BUILD_PATH/"

if [[ "DOCKER_PUSH" == "true" ]]; then
	echo
	echo "=== Push to docker registry ==="
	$DRY_RUN $RUN image tag "$APPNAME" "$DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$DOCKER_TAG"
	$DRY_RUN $RUN image push "$DOCKER_REGISTRY/$DOCKER_IMAGE_NAME:$DOCKER_TAG"

	if [[ "$CREATE_ONLY" != "true" && "$START_LOCAL" != "true" ]]; then
		echo "Stopping. The rest of the output is only a hint for possible commands"
		DRY_RUN="echo "
	fi
fi

echo
echo "=== Removing old docker container ==="
$DRY_RUN $RUN rm -f "$APPNAME" || echo "Nothing to delete"

echo
echo "=== Starting docker container ==="
[ -z "$HTTP_PORT" ] && HTTP_PORT=8080
$DRY_RUN sleep 5

# Open browser to log-in to started app.
(
  if [[ "$CONTEXT" == "ROOT" ]]; then
	  contextPath="/"
  else
	  contextPath="/$CONTEXT"
  fi

  echo
  echo "=== Opening browser ==="

  # Wait until container startup is at least in progress
  $DRY_RUN sleep 10
  $DRY_RUN xdg-open "http://localhost:${HTTP_PORT}${contextPath}"
) &

mkdir -p "${FILES}"

# Start docker container
$DRY_RUN $RUN run \
  -tdi -p $HTTP_PORT:8080 \
  -v "$FILES":"/var/lib/tomcat9/work/${APPNAME}" \
  --restart=unless-stopped \
  --name="$APPNAME" \
  --hostname="$APPNAME" "$APPNAME" && $DRY_RUN $RUN logs -f "$APPNAME"

# When the control flow reaches this point, the user has pressed Ctrl-C, stop the contaner.

echo
echo "=== Stopping docker container ==="
$DRY_RUN $RUN stop "$APPNAME"

