#!/usr/bin/env bash

set -e

root=`dirname $0`

mvn  -DskipTests=true clean install

source ./build/utils/cf-common.sh

# create Redis MQ
redis=redis-bus
cf s | grep ${redis} && echo "found ${redis}"  || cf cs rediscloud 30mb ${redis}

# create MySQL DB
mysql=cnj-mysql
cf s | grep ${mysql} && echo "found ${mysql}" || cf cs cleardb spark ${mysql}

# deploy zipkin-query-service
zq=zipkin-query-service
cf d -f $zq
cd $root/zipkin-query-service
cf push

# deploy zipkin-web
zw=zipkin-web
cf d -f $zw
zqs_name=`app_domain $zq`
echo zipkin-query-service URL: $zqs_name
curl ${zqs_name}/api/v1/services
cd $root/zipkin-web
cf push --no-start
jcjm=`$root/deploy-helper.py $zqs_name`
cf set-env $zw JBP_CONFIG_JAVA_MAIN "${jcjm}"
cf restart $zw


# deploy clients
zc_b=zipkin-client-b
zc_a=zipkin-client-a

cf s | grep $zc_b && cf ds -f $zc_b
deploy_app $zc_b
deploy_service $zc_b

deploy_app $zc_a