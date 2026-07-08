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
 * Extension point for querying a vehicle's available initial inventory during insertion evaluation.
 *
 * <p>The fork knows nothing about compartments or skills — that logic lives in the private project.
 * Registered on the StateManager via
 * {@link StateManager#setVehicleInventoryProvider(VehicleInventoryProvider)}.
 *
 * <p>All methods must be thread-safe — they are called concurrently during multi-threaded
 * insertion evaluation.
 */
public interface VehicleInventoryProvider {

    /**
     * Returns the total available inventory for the given vehicle and skill group,
     * considering what has already been committed to previously inserted jobs.
     *
     * @param vehicleId the vehicle being evaluated
     * @param skill     the required skill of the shipment being inserted
     * @return available inventory quantity, or 0 if none
     */
    int getAvailableInventory(String vehicleId, String skill);

    /**
     * Commits inventory consumption when a split insertion is confirmed by the Inserter.
     * Called once per confirmed onVehicle split, not during evaluation.
     *
     * @param vehicleId   the vehicle
     * @param skill       the skill consumed
     * @param quantity    the amount consumed from the vehicle's inventory
     */
    void commitInventoryConsumption(String vehicleId, String skill, int quantity);

    /**
     * Releases inventory when a route is destroyed during the ruin phase.
     * Called by RuinListener to restore committed inventory for jobs that were removed.
     *
     * @param vehicleId the vehicle
     * @param skill     the skill to release
     * @param quantity  the amount to release back to available inventory
     */
    void releaseInventory(String vehicleId, String skill, int quantity);
}