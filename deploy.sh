#!/usr/bin/env bash

set -e

reset(){
    app_name=$1
    echo "going to remove ${app_name} if it exists"
    cf a | grep $app_name && cf d -f $app_name
    echo "deleted ${app_name}"
}

cd `dirname $0`
root=`pwd`
echo $root

mvn -DskipTests=true clean install

source ./build/utils/cf-common.sh

## create Redis MQ
#redis=cnj-redis-bus
#cf s | grep ${redis} && echo "found ${redis}" || cf cs rediscloud 100mb ${redis}

# create RabbitMQ
rabbit=cnj-rabbitmq
cf s | grep ${rabbit} && echo "found ${rabbit}" || cf cs cloudamqp tiger ${rabbit}


# create MySQL DB
mysql=cnj-mysql
cf s | grep ${mysql} && echo "found ${mysql}" || cf cs cleardb spark ${mysql}

# deploy zipkin-query-service
zq=zipkin-query-service
cd $root/$zq
reset $zq
cf d -f $zq
cd $root/zipkin-query-service
cf push

# deploy zipkin-web
zw=zipkin-web
reset $zw
cf d -f $zw
zqs_name=`app_domain $zq`
cd $root/zipkin-web
cf push --no-start
jcjm=`$root/deploy-helper.py $zqs_name`
cf set-env $zw JBP_CONFIG_JAVA_MAIN "${jcjm}"
cf restart $zw

# deploy clients
cd $root
zc_a=zipkin-client-a
zc_b=zipkin-client-b
reset $zc_a
reset $zc_b

cf s | grep $zc_b && cf ds -f $zc_b
deploy_app $zc_b
deploy_service $zc_b

deploy_app $zc_a

cf delete-orphaned-routes