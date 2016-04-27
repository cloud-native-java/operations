#!/usr/bin/env bash

set -e

c=`dirname $0`
echo $c

source $BUILD_DIRECTORY/utils/cf-common.sh

cd $c

bac=spring-boot-admin-client
bas=spring-boot-admin-server

cf d -f $bac
cf d -f $bas
cf ds -f $bas


deploy_app $bas
deploy_service $bas

deploy_app $bac
