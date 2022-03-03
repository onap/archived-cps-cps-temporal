#!/bin/bash
#
# ============LICENSE_START=======================================================
# Copyright (C) 2021-2022 Bell Canada.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================

# Copy docker-compose.yml and application.yml to archives
mkdir -p $WORKSPACE/archives/docker-compose
cp $WORKSPACE/../*.yml $WORKSPACE/archives/docker-compose
cd $WORKSPACE/archives/docker-compose

# properties file with docker-compose service properties
source $WORKSPACE/plans/default/setup.properties

# download docker-compose of a required version (1.25.0 supports configuration of version 3.7)
curl -L https://github.com/docker/compose/releases/download/1.25.0/docker-compose-`uname -s`-`uname -m` > docker-compose
chmod +x docker-compose

# Set environment variables for docker compose
export CPS_TEMPORAL_DOCKER_REPO=
export CPS_CORE_VERSION=3.0.0-SNAPSHOT-20220303T233031Z
# start CPS Temporal, cps-core, timescaledb, PostgresSQL and kafka containers with docker compose
./docker-compose up -d
python3 --version
# Validate CPS service initialization completed via periodic log checking for line like below:
# org.onap.cps.temporal.Application ... Started Application in X.XXX seconds

while [ "$TIME" -le "$TIME_OUT" ]; do
  LOG_FOUND_CPS_TEMPORAL=$( ./docker-compose logs --tail="all" | grep "org.onap.cps.temporal.Application" | egrep -c "Started Application in" )
  LOG_FOUND_CPS=$( ./docker-compose logs --tail="all" | grep "org.onap.cps.Application" | egrep -c "Started Application in" )

  if [ "$LOG_FOUND_CPS" -gt 0 ] && [ "$LOG_FOUND_CPS_TEMPORAL" -gt 0 ]; then
    echo "CPS Service started"
    break;
  fi

  echo "Sleep $INTERVAL seconds before next check for CPS initialization (waiting $TIME seconds; timeout is $TIME_OUT seconds)"
  sleep $INTERVAL
  TIME=$((TIME + INTERVAL))
done

if [ "$TIME" -gt "$TIME_OUT" ]; then
   echo "TIME OUT: CPS services did not start in $TIME_OUT seconds, setup failed."
   exit 1;
fi

# Pass variables required for Robot test suites in ROBOT_VARIABLES
ROBOT_VARIABLES="-v  CPS_TEMPORAL_HOST:$CPS_TEMPORAL_HOST -v CPS_TEMPORAL_PORT:$CPS_TEMPORAL_PORT -v CPS_HOST:$CPS_HOST -v CPS_PORT:$CPS_PORT -v MANAGEMENT_PORT:$MANAGEMENT_PORT -v DATADIR:$WORKSPACE/data"
