#!/usr/bin/env bash

set -e

c=`dirname $0`
echo $c

source $BUILD_DIRECTORY/utils/cf-common.sh

cd $c

cf d -f spring-boot-admin-client
cf d -f spring-boot-admin-server
cf ds -f spring-boot-admin-server


deploy_app spring-boot-admin-server
deploy_service spring-boot-admin-server


deploy_app spring-boot-admin-client
