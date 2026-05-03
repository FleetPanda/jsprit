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
package com.graphhopper.jsprit.core.algorithm.state;

/**
 * Extension point for minimum loading adjustments.
 *
 * <p>Implementations compute the adjusted size (dimension 0) for a given job
 * within a given route. The fork knows nothing about how adjustments are
 * calculated — that logic lives in the private project.
 *
 * <p>Registered on the StateManager via
 * {@link StateManager#setMinLoadAdjustmentProvider(MinLoadAdjustmentProvider)}.
 * UpdateLoads reads it via
 * {@link StateManager#getMinLoadAdjustmentProvider()}.
 *
 * <p>Returns -1 when no adjustment applies for the given (routeId, jobId) pair.
 */
public interface MinLoadAdjustmentProvider {

    /**
     * @param routeId the UUID of the VehicleRoute being evaluated
     * @param jobId   the ID of the job whose pickup is being visited
     * @return adjusted size for dimension 0, or -1 if no adjustment applies
     */
    int getAdjustedSize(String routeId, String jobId);
}