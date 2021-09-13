#!/bin/bash
#
# Copyright 2016-2017 Huawei Technologies Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Copyright (C) 2021 Bell Canada.
#
# Branched from ccsdk/distribution to this repository Feb 23, 2021
#

# Copy docker-compose.yml and application.yml to archives
mkdir -p $WORKSPACE/archives/docker-compose
cp $WORKSPACE/../docker-compose/*.yml $WORKSPACE/archives/docker-compose
cd $WORKSPACE/archives/docker-compose

# download docker-compose of a required version (1.25.0 supports configuration of version 3.7)
curl -L https://github.com/docker/compose/releases/download/1.25.0/docker-compose-`uname -s`-`uname -m` > docker-compose
chmod +x docker-compose

# start CPS Temporal, cps-core, timescaledb, PostgresSQL and kafka containers with docker compose
./docker-compose up -d
python --version
# Validate CPS service initialization completed via periodic log checking for line like below:
# org.onap.cps.temporal.Application ... Started Application in X.XXX seconds

TIME_OUT=300
INTERVAL=5
TIME=0

while [ "$TIME" -le "$TIME_OUT" ]; do
  LOG_FOUND=$( ./docker-compose logs --tail="all" | grep "org.onap.cps.temporal.Application" | egrep -c "Started Application in" )

  if [ "$LOG_FOUND" -gt 0 ]; then
    echo "CPS Temporal Service started"
    break;
  fi

  echo "Sleep $INTERVAL seconds before next check for CPS Temporal initialization (waiting $TIME seconds; timeout is $TIME_OUT seconds)"
  sleep $INTERVAL
  TIME=$((TIME + INTERVAL))
done

if [ "$TIME" -gt "$TIME_OUT" ]; then
   echo "TIME OUT: CPS Temporal Service wasn't able to start in $TIME_OUT seconds, setup failed."
   exit 1;
fi

# The CPS host according to docker-compose.yml
CPS_HOST="localhost"
CPS_PORT="8083"

# The CPS temporal host according to docker-compose.yml
CPS_TEMPORAL_HOST="localhost"
CPS_TEMPORAL_PORT="8082"
MANAGEMENT_PORT="8081"

# Pass variables required for Robot test suites in ROBOT_VARIABLES
ROBOT_VARIABLES="-v  CPS_TEMPORAL_HOST:$CPS_TEMPORAL_HOST -v CPS_TEMPORAL_PORT:$CPS_TEMPORAL_PORT -v CPS_HOST:$CPS_HOST -v CPS_PORT:$CPS_PORT -v MANAGEMENT_PORT:$MANAGEMENT_PORT -v DATADIR:$WORKSPACE/data"

