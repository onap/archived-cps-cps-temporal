#!/bin/bash -x
# ============LICENSE_START=======================================================
# Copyright (C) 2021 Bell Canada.
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

# $1 test options (passed on to run-csit.sh as such)

ROBOT_VENV=$(mktemp -d --suffix=robot_venv)
echo "ROBOT_VENV=${ROBOT_VENV}" >> "${WORKSPACE}/env.properties"

echo "Python version is: $(python3 --version)"

python3 -m venv "${ROBOT_VENV}"
source "${ROBOT_VENV}/bin/activate"

set -exu

# Make sure pip3 itself us up-to-date.
python3 -m pip install --upgrade pip

echo "Installing Python Requirements"
python3 -m pip install -r ${WORKSPACE}/pylibs.txt
python3 -m pip freeze