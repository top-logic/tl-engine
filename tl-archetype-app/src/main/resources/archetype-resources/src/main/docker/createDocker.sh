#!/bin/bash
# Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.

mvn --help >/dev/null 2>&1
[[ $(echo $?) != "0" ]] && echo "Maven is missing. Pls install with 'apt install maven'." && exit 0;
docker --help >/dev/null 2>&1
[[ $(echo $?) != "0" ]] && echo "Docker is missing. Pls install with 'apt install docker.io'." && exit 0;

error_param(){
echo -e "Usage:\t./createDocker.sh [OPTION]...
Parameter:
\t-n [AppContextName]
\t-d [DatabaseType]\tOptions: h2, mysql, postgre, mssql, oracle

Optional additional database parameter:
\t-l [JDBC URL]\t\tURL format dependent on the choosen database. Please refer to the specific documentations. Overrides hostname, port and scheme.
\t-h [ip|hostname]\tDefault: [IP of this machine] - H2 default:/var/lib/tomcat9/work/
\t-o [port]\t\tDefault: DB dependent default port
\t-s [DB scheme]\t\tDefault: [AppContextName] - In case of Oracle this is the prefixed (/)SERVICE or (:)SID name. Example: :SIDNAME or /SERVICENAME.
\t-u [DB username]\tDefault: user
\t-p [DB password]\tDefault: passwd

Start local parameter:
\t-w [HTTP PORT]\t\tDefault: 8080 - The http port where the app should be accessible
\t-r\n";
exit 0;
}

while getopts n:d:l:h:o:s:u:p:r opt
do
   case $opt in
       n) CONTEXT="$OPTARG";;
       d) case $OPTARG in
              h2) DATABASE="config_h2";;
              mysql) DATABASE="config_mysql";;
              mssql) DATABASE="config_mssql";;
              postgre) DATABASE="config_postgre";;
              oracle) DATABASE="config_oracle";;
              *) error_param;;
          esac;;
	   l) DB_URL="$OPTARG";;
       h) DB_HOST="$OPTARG";;
	   o) DB_PORT="$OPTARG";;
       s) DB_SCHEME="$OPTARG";;
       u) DB_USER="$OPTARG";;
       p) DB_PASSWD="$OPTARG";;
	   w) HTTP_PORT="$OPTARG";;
       r) START_LOCAL="true";;
       *) error_param;;
   esac
done

config_h2(){
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_URL" ] && DB_URL="\/var\/lib\/tomcat9\/work\/$DB_SCHEME" || DB_URL="$(sed 's/\//\\\//g' <<<$DB_URL)"
  sed -i -e "s/{dbLibrary}/RUN apt install libh2-java/g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.h2.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:h2:$DB_URL/g" $BUILD_PATH/context.xml
}

config_mysql(){
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_HOST" ] && DB_HOST=$LOCAL_IP
  [ -z "$DB_PORT" ] && DB_PORT=3306
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT/$DB_SCHEME"
  sed -i -e 's/{dbLibrary}/RUN apt install libmariadb-java/g' $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.mariadb.jdbc.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:mysql:\/\/$DB_URL/g" $BUILD_PATH/context.xml

}

config_mssql(){
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
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

config_postgre(){
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  [ -z "$DB_HOST" ] && DB_HOST=$LOCAL_IP
  [ -z "$DB_PORT" ] && DB_PORT=5432
  [ -z "$DB_URL" ] && DB_URL="$DB_HOST:$DB_PORT/$DB_SCHEME"
  sed -i -e "s/{dbLibrary}/RUN apt install libpostgresql-jdbc-java/g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.postgresql.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:postgresql:\/\/$DB_URL/g" $BUILD_PATH/context.xml
}

config_oracle(){
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
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

LOCAL_IP=$(hostname -I | awk '{print $1}')
BUILD_PATH=$(dirname $(readlink -f "$0"))
cp -f $BUILD_PATH/Dockerfile_template $BUILD_PATH/Dockerfile
cd $BUILD_PATH/../../..
mvn package
cp -f ./target/*app.war $BUILD_PATH/$CONTEXT.war
cp -f ./src/main/deb/data/tomcat/context.xml $BUILD_PATH/
cd $BUILD_PATH

$DATABASE
sed -i -e "s/{dbUser}/$DB_USER/g" $BUILD_PATH/context.xml
sed -i -e "s/{dbPasswd}/$DB_PASSWD/g" $BUILD_PATH/context.xml
sed -i -e "s/{contextName}/$CONTEXT/g" $BUILD_PATH/context.xml
sed -i -e "s/{timeZone}/$(sed 's/\//\\\//g' <<<$(cat /etc/timezone))/g" $BUILD_PATH/Dockerfile

docker login docker.top-logic.com -u guest -p guest
docker pull docker.top-logic.com/tomcat9-java11:latest
docker build -t $CONTEXT $BUILD_PATH/

if [[ "$START_LOCAL" == "true" ]]; then
	docker rm -f $CONTEXT || echo "Nothing to delete"
	sleep 5
	[ -z "$HTTP_PORT" ] && HTTP_PORT=8080
	docker run -tdi -p $HTTP_PORT:8080 --restart=unless-stopped --name=$CONTEXT --hostname=$CONTEXT $CONTEXT && docker logs -f $CONTEXT
fi

