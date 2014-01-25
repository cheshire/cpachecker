/*
 *  CPAchecker is a tool for configurable software verification.
 *  This file is part of CPAchecker.
 *
 *  Copyright (C) 2007-2014  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 *  CPAchecker web page:
 *    http://cpachecker.sosy-lab.org
 */
package org.sosy_lab.cpachecker.appengine.json;

import org.sosy_lab.cpachecker.appengine.entity.JobStatistic;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;


public abstract class JobStatisticMixinAnnotations {

  @JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE)

  public abstract class Minimal extends JobStatistic {
    @Override
    @JsonProperty
    public abstract String getKey();

    @JsonProperty
    long latency;

    @JsonProperty
    String host;

    @Override
    @JsonProperty("CPUTime")
    public abstract double getMcyclesInSeconds();
  }

  public abstract class Full extends Minimal {
    @JsonProperty
    double cost;
    @JsonProperty
    long endTime;
    @JsonProperty
    long startTime;
    @JsonProperty
    long pendingTime;
  }
}
