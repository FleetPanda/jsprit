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
 */
package com.graphhopper.jsprit.core.algorithm.listener;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

/**
 * Listener notified whenever a new best-ever solution is found during optimization.
 *
 * Fired inside memorizeIfBestEver(), so it is called at most once per iteration
 * and only when the solution actually improves.
 */
public interface BestSolutionUpdatedListener extends VehicleRoutingAlgorithmListener {

    /**
     * @param newBestSolution the new best solution just recorded
     */
    void onBestSolutionUpdated(VehicleRoutingProblemSolution newBestSolution);
}