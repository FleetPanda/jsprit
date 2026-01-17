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
package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Interface for overall routing and operation costs.
 * <p>
 * <p>This calculates activity and leg-based costs. If you want to consider for example costs incurred by missed time-windows, you can do it here.
 *
 * @author schroeder
 */
public interface VehicleRoutingActivityCosts {

    public static class Time {

        public static double TOUREND = -2.0;

        public static double TOURSTART = -1.0;

        public static double UNDEFINED = -3.0;

    }

    public static interface Parameter {
        public double getPenaltyForMissedTimeWindow();
    }

    /**
     * Calculates and returns the activity cost at tourAct.
     * <p>
     * <p>Here waiting-times, service-times and missed time-windows can be considered.
     *
     * @param tourAct
     * @param arrivalTime is actually the arrival time at this tourActivity, which must not necessarily be the operation start time. If the theoretical earliest
     *                    operation start time at this activity is later than actualStartTime, the driver must wait at this activity.
     * @param driver
     * @param vehicle     if earliestStartTime > latestStartTime activity operations cannot be conducted within the given time-window.
     * @return
     */
    public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle);

    /**
     * Returns the activity duration for the given activity.
     * <p>
     * This is the original method without previous activity context.
     * For backward compatibility, this method should delegate to getActivityDuration with null prevAct.
     *
     * @param tourAct     the current activity
     * @param arrivalTime the arrival time at this activity
     * @param driver      the driver
     * @param vehicle     the vehicle
     * @return the duration of the activity
     */
    public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle);

    /**
     * Returns the activity duration for the given activity, considering the previous activity.
     * <p>
     * This method allows for dynamic duration calculation based on the previous activity,
     * such as applying preparation time only on first visit to a location.
     * <p>
     * Default implementation delegates to the original getActivityDuration method.
     *
     * @param prevAct     the previous activity (can be null for first activity after start)
     * @param tourAct     the current activity
     * @param arrivalTime the arrival time at this activity
     * @param driver      the driver
     * @param vehicle     the vehicle
     * @return the duration of the activity
     */
    default double getActivityDuration(TourActivity prevAct, TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
        // Default implementation for backward compatibility
        return getActivityDuration(tourAct, arrivalTime, driver, vehicle);
    }

}
