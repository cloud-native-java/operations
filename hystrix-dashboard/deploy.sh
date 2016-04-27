#!/usr/bin/env bash

set -e

c=`dirname $0`
echo $c

source $BUILD_DIRECTORY/utils/cf-common.sh
cd $c

t=turbine
hc=hystrix-client
hdu=hystrix-dashboard-ui
rb=rabbitmq-bus

cf a | grep $t  && cf d -f $t
cf a | grep $hc  && cf d -f $hc
cf a | grep $hdu  && cf d -f $hdu


cf s | grep $rb || cf cs cloudamqp lemur $rb


deploy_app $t
deploy_app $hdu
deploy_app $hc
