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

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.PreferredVehicle;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;

import java.util.List;

/**
 * Soft route-level constraint that penalizes or rewards vehicle assignment
 * based on preferred vehicle definitions.
 * <p>
 * When a job has preferred vehicles defined:
 * <ul>
 *   <li>If the assigned vehicle is preferred, a cost bonus (negative cost) is applied
 *       based on its priority (priority 1 = largest bonus, priority 10 = smallest bonus).</li>
 *   <li>If the assigned vehicle is NOT preferred but preferences exist, a penalty cost is applied.</li>
 * </ul>
 * <p>
 * The penalty and bonus magnitudes can be configured. By default:
 * <ul>
 *   <li>Penalty for non-preferred vehicle: {@code penaltyForNonPreferred} (default 1000)</li>
 *   <li>Bonus for preferred vehicle: {@code maxBonus * (11 - priority) / 10} (default maxBonus = 500)</li>
 * </ul>
 */
public class SoftPreferredVehicleConstraint implements SoftRouteConstraint {

    private final double penaltyForNonPreferred;
    private final double maxBonus;

    /**
     * Creates a constraint with default penalty (1000) and bonus (500).
     */
    public SoftPreferredVehicleConstraint() {
        this(1000.0, 500.0);
    }

    /**
     * Creates a constraint with custom penalty and bonus values.
     *
     * @param penaltyForNonPreferred penalty cost added when a non-preferred vehicle is used
     *                                and preferences exist
     * @param maxBonus               maximum bonus (cost reduction) for priority-1 preferred vehicle
     */
    public SoftPreferredVehicleConstraint(double penaltyForNonPreferred, double maxBonus) {
        this.penaltyForNonPreferred = penaltyForNonPreferred;
        this.maxBonus = maxBonus;
    }

    @Override
    public double getCosts(JobInsertionContext insertionContext) {
        Job job = insertionContext.getJob();
        List<PreferredVehicle> preferredVehicles = job.getPreferredVehicles();

        if (preferredVehicles == null || preferredVehicles.isEmpty()) {
            return 0.0;
        }

        String vehicleId = insertionContext.getNewVehicle().getId();

        for (PreferredVehicle pv : preferredVehicles) {
            if (pv.getVehicleId().equals(vehicleId)) {
                // Preferred vehicle found: return bonus (negative cost).
                // Priority 1 -> full bonus, priority 10 -> 10% of bonus
                return -maxBonus * (11.0 - pv.getPriority()) / 10.0;
            }
        }

        // Vehicle is not in the preferred list: apply penalty
        return penaltyForNonPreferred;
    }
}
