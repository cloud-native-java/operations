#!/usr/bin/env bash
cf push --no-start
cf set-env actuator HOSTEDGRAPHITE_APIKEY $HOSTEDGRAPHITE_APIKEY
cf set-env actuator HOSTEDGRAPHITE_URL $HOSTEDGRAPHITE_URL
cf set-env actuator HOSTEDGRAPHITE_PORT $HOSTEDGRAPHITE_PORT
cf set-env actuator HOSTEDGRAPHITE_PORT $HOSTEDGRAPHITE_PORT
cf restart actuator