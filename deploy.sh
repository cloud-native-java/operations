#!/usr/bin/env bash

set -e

operations=`dirname $0`

source $BUILD_DIRECTORY/utils/cf-common.sh

cd ${operations}/tracing

z_c=zipkin-client-
z_a=${z_c}a
z_b=${z_c}b

cf d -f $z_a
cf d -f $z_b
cf ds -f $z_b

deploy_app zipkin-service

deploy_app $z_b
deploy_service $z_b

deploy_app $z_a

cd ${operations}/actuator

cf push --no-start
cf set-env actuator HOSTEDGRAPHITE_APIKEY $HOSTEDGRAPHITE_APIKEY
cf set-env actuator HOSTEDGRAPHITE_URL $HOSTEDGRAPHITE_URL
cf set-env actuator HOSTEDGRAPHITE_PORT $HOSTEDGRAPHITE_PORT
cf set-env actuator HOSTEDGRAPHITE_PORT $HOSTEDGRAPHITE_PORT
cf restart actuator

${operations}/spring-boot-admin/deploy.sh
${operations}/hystrix-dashboard/deploy.sh
