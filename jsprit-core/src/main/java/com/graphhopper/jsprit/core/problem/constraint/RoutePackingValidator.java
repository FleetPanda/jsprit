/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.List;

/**
 * Optional, application-supplied check for whether a *complete candidate activity sequence* can be
 * physically packed on a vehicle — used for constraints that cannot be expressed as a simple
 * capacity dimension because they depend on the whole assembled route (e.g. per-compartment loading
 * with a no-mixing rule, where feasibility depends on the full set and order of jobs, not on any
 * single (job, vehicle) pair).
 *
 * <p>The core builds the candidate sequence (the current route's activities with the job's pickup
 * and delivery already spliced in at the positions being evaluated) and asks the validator whether
 * it packs. Because the core re-evaluates this for every candidate position and for every insertion,
 * a job that becomes infeasible only in combination with later-inserted jobs is caught as well — the
 * check always sees the full sequence, so it is not vulnerable to insertion order the way a
 * per-activity constraint is.
 *
 * <p>This hook is entirely opt-in. When no validator is registered on the {@link ConstraintManager}
 * the insertion calculators run exactly as before, with zero additional work — problems that do not
 * use this feature pay nothing.
 */
public interface RoutePackingValidator {

    /**
     * @param vehicle            the vehicle the candidate route would run on
     * @param candidateActivities the full, ordered activity sequence of the route with the job's
     *                            pickup and delivery already inserted at the positions being evaluated
     * @return {@code true} if every activity in the sequence can be packed (feasible),
     *         {@code false} if this exact sequence cannot be packed and must be rejected
     */
    boolean isPackable(Vehicle vehicle, List<TourActivity> candidateActivities);
}