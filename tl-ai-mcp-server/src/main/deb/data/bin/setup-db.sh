#!/bin/bash

echo 'CREATE DATABASE `{dbSchema}`;' | mariadb
echo "CREATE USER '{dbUser}'@localhost IDENTIFIED BY '$(cat /root/{dbSchema}.db.passwd)';" | mariadb
echo 'GRANT ALL PRIVILEGES ON `{dbSchema}`.* TO "{dbUser}"@localhost;' | mariadb
