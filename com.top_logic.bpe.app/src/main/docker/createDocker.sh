#!/bin/bash
# Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.

error_param(){
echo -e "Usage:\t./createDocker.sh [OPTION]...
Parameter:
\t-n [AppContextName]
\t-d [DatabaseType]\tOptions: h2, mysql, postgre
Optional additional database parameter:
\t-l [ip|hostname]:[port]\tDefault: [IP of this machine]:[DB default], H2 default:/var/lib/tomcat9/work
\t-s [DB scheme]\t\tDefault: [AppContextName]
\t-u [DB username]\tDefault: user
\t-p [DB password]\tDefault: passwd
\t-r\t\t\tStart app local";
exit 0;
}

while getopts n:d:l:s:u:p:r opt
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
       s) DB_SCHEME="$OPTARG";;
       u) DB_USER="$OPTARG";;
       p) DB_PASSWD="$OPTARG";;
       r) START_LOCAL="true";;
       *) error_param;;
   esac
done
[ -z "$DATABASE" ] && error_param

config_h2(){
  [ -z "$DB_URL" ] && DB_URL="\/var\/lib\/tomcat9\/work" || DB_URL="$(sed 's/\//\\\//g' <<<$DB_URL)"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  sed -i -e "s/{dbLibrary}/RUN apt install libh2-java/g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.h2.Driver/g' $BUILD_PATH/context.xml
  sed -i -e 's/{dbURL}/jdbc:h2:\/var\/lib\/tomcat9\/work/g' $BUILD_PATH/context.xml
}

config_mysql(){
  [ -z "$DB_URL" ] && DB_URL="$LOCAL_IP:3306"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  sed -i -e 's/{dbLibrary}/RUN apt install libmariadb-java/g' $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.mariadb.jdbc.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:mysql:\/\/$DB_URL/g" $BUILD_PATH/context.xml

}

config_mssql(){
echo "TODO"
exit 0
#  [ -z "$DB_URL" ] && DB_URL="$LOCAL_IP:1433"
#  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
#  [ -z "$DB_USER" ] && DB_USER="user"
#  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
## Download v12.2.0 from MS
#  wget https://go.microsoft.com/fwlink/?linkid=2222954 -O mssql.tar.gz
#  tar -xvzf mssql.tar.gz
# ...
# rm -rf sqljdbc*
}

config_postgre(){
  [ -z "$DB_URL" ] && DB_URL="$LOCAL_IP:5432"
  [ -z "$DB_SCHEME" ] && DB_SCHEME=$CONTEXT
  [ -z "$DB_USER" ] && DB_USER="user"
  [ -z "$DB_PASSWD" ] && DB_PASSWD="passwd"
  sed -i -e "s/{dbLibrary}/RUN apt install libpostgresql-jdbc-java/g" $BUILD_PATH/Dockerfile
  sed -i -e 's/{dbDriver}/org.postgresql.Driver/g' $BUILD_PATH/context.xml
  sed -i -e "s/{dbURL}/jdbc:postgresql:\/\/$DB_URL/g" $BUILD_PATH/context.xml
}

config_oracle(){
echo "TODO"
exit 0
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
sed -i -e "s/{dbSchema}/$DB_SCHEME/g" $BUILD_PATH/context.xml
sed -i -e "s/{dbUser}/$DB_USER/g" $BUILD_PATH/context.xml
sed -i -e "s/{dbPasswd}/$DB_PASSWD/g" $BUILD_PATH/context.xml
sed -i -e "s/{contextName}/$CONTEXT/g" $BUILD_PATH/context.xml
sed -i -e "s/{timeZone}/$(sed 's/\//\\\//g' <<<$(cat /etc/timezone))/g" $BUILD_PATH/Dockerfile

docker pull docker.top-logic.com/tomcat9-java11:latest
docker build -t $CONTEXT $BUILD_PATH/

rm -f $BUILD_PATH/$CONTEXT.war
rm -f $BUILD_PATH/context.xml
rm -f $BUILD_PATH/Dockerfile
if [[ "$START_LOCAL" == "true" ]]; then
	docker rm -f $CONTEXT || echo "Nothing to delete"
	docker run -tdi -p 8080:8080 --restart=unless-stopped --name=$CONTEXT --hostname=$CONTEXT $CONTEXT && docker logs -f $CONTEXT
fi

