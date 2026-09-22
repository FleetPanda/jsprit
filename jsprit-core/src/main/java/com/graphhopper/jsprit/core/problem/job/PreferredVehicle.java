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
package com.graphhopper.jsprit.core.problem.job;

/**
 * Represents a preferred vehicle for a job with a priority level.
 * <p>
 * Priority is a number between 1 and 10 where 1 indicates the highest priority
 * and 10 the lowest. Default priority is 2.
 * <p>
 * Equality and hash code are based only on {@code vehicleId}. This means two
 * {@code PreferredVehicle} instances with the same vehicle id are considered
 * equal even if they have different priority values.
 */
public class PreferredVehicle {

    private final String vehicleId;
    private final int priority;

    /**
     * Creates a preferred vehicle with the given id and default priority (2).
     *
     * @param vehicleId the vehicle id
     */
    public PreferredVehicle(String vehicleId) {
        this(vehicleId, 2);
    }

    /**
     * Creates a preferred vehicle with the given id and priority.
     *
     * @param vehicleId the vehicle id
     * @param priority  priority between 1 (highest) and 10 (lowest)
     * @throws IllegalArgumentException if priority is not between 1 and 10
     */
    public PreferredVehicle(String vehicleId, int priority) {
        if (vehicleId == null || vehicleId.isEmpty()) {
            throw new IllegalArgumentException("Vehicle id must not be null or empty.");
        }
        if (priority < 1 || priority > 10) {
            throw new IllegalArgumentException("Priority must be between 1 (highest) and 10 (lowest). Got: " + priority);
        }
        this.vehicleId = vehicleId;
        this.priority = priority;
    }

    /**
     * Returns the vehicle id.
     *
     * @return vehicle id
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * Returns the priority. 1 is highest, 10 is lowest.
     *
     * @return priority
     */
    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "[vehicleId=" + vehicleId + "][priority=" + priority + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreferredVehicle that = (PreferredVehicle) o;
        return vehicleId.equals(that.vehicleId);
    }

    @Override
    public int hashCode() {
        return vehicleId.hashCode();
    }
}
