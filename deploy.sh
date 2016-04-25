#!/usr/bin/env bash

set -e

source $BUILD_DIRECTORY/utils/cf-common.sh


cd tracing

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
