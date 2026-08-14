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
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vehicle Restriction Integration Test")
class VehicleRestriction_IT {

    @Test
    @DisplayName("service with allowed vehicles should only be served by allowed vehicle")
    void serviceWithAllowedVehicles_shouldOnlyBeServedByAllowedVehicle() {
        VehicleType type1 = VehicleTypeImpl.Builder.newInstance("type1")
            .addCapacityDimension(0, 10).build();
        VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type2")
            .addCapacityDimension(0, 10).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0, 0)).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(0, 0)).setType(type2).build();

        Service s1 = Service.Builder.newInstance("s1")
            .addSizeDimension(0, 1)
            .setLocation(Location.newInstance(10, 10))
            .setAllowedVehicles("v1")
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .setFleetSize(FleetSize.FINITE)
            .addVehicle(v1).addVehicle(v2).addJob(s1).build();

        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);

        assertNotNull(best);
        assertTrue(best.getUnassignedJobs().isEmpty(), "Job should be assigned");

        for (VehicleRoute route : best.getRoutes()) {
            if (!route.getTourActivities().isEmpty()) {
                assertEquals("v1", route.getVehicle().getId(),
                    "Service s1 should only be served by v1");
            }
        }
    }

    @Test
    @DisplayName("service with disallowed vehicle should not be served by that vehicle")
    void serviceWithDisallowedVehicle_shouldNotBeServedByThatVehicle() {
        VehicleType type1 = VehicleTypeImpl.Builder.newInstance("type1")
            .addCapacityDimension(0, 10).build();
        VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type2")
            .addCapacityDimension(0, 10).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0, 0)).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(0, 0)).setType(type2).build();

        Service s1 = Service.Builder.newInstance("s1")
            .addSizeDimension(0, 1)
            .setLocation(Location.newInstance(10, 10))
            .setDisallowedVehicles("v1")
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .setFleetSize(FleetSize.FINITE)
            .addVehicle(v1).addVehicle(v2).addJob(s1).build();

        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);

        assertNotNull(best);
        assertTrue(best.getUnassignedJobs().isEmpty(), "Job should be assigned");

        for (VehicleRoute route : best.getRoutes()) {
            if (!route.getTourActivities().isEmpty()) {
                assertNotEquals("v1", route.getVehicle().getId(),
                    "Service s1 should not be served by v1");
            }
        }
    }

    @Test
    @DisplayName("shipment with allowed vehicles should only be served by allowed vehicle")
    void shipmentWithAllowedVehicles_shouldOnlyBeServedByAllowedVehicle() {
        VehicleType type1 = VehicleTypeImpl.Builder.newInstance("type1")
            .addCapacityDimension(0, 10).build();
        VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type2")
            .addCapacityDimension(0, 10).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0, 0)).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(0, 0)).setType(type2).build();

        Shipment ship = Shipment.Builder.newInstance("ship1")
            .addSizeDimension(0, 1)
            .setPickupLocation(Location.newInstance(5, 5))
            .setDeliveryLocation(Location.newInstance(10, 10))
            .setAllowedVehicles("v2")
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .setFleetSize(FleetSize.FINITE)
            .addVehicle(v1).addVehicle(v2).addJob(ship).build();

        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);

        assertNotNull(best);
        assertTrue(best.getUnassignedJobs().isEmpty(), "Shipment should be assigned");

        for (VehicleRoute route : best.getRoutes()) {
            if (!route.getTourActivities().isEmpty()) {
                assertEquals("v2", route.getVehicle().getId(),
                    "Shipment should only be served by v2");
            }
        }
    }

    @Test
    @DisplayName("multiple services with different vehicle restrictions")
    void multipleServicesWithDifferentVehicleRestrictions() {
        VehicleType type1 = VehicleTypeImpl.Builder.newInstance("type1")
            .addCapacityDimension(0, 10).build();
        VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type2")
            .addCapacityDimension(0, 10).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(Location.newInstance(0, 0)).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(Location.newInstance(0, 0)).setType(type2).build();

        Service s1 = Service.Builder.newInstance("s1")
            .addSizeDimension(0, 1)
            .setLocation(Location.newInstance(5, 5))
            .setAllowedVehicles("v1")
            .build();

        Service s2 = Service.Builder.newInstance("s2")
            .addSizeDimension(0, 1)
            .setLocation(Location.newInstance(10, 10))
            .setAllowedVehicles("v2")
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .setFleetSize(FleetSize.FINITE)
            .addVehicle(v1).addVehicle(v2).addJob(s1).addJob(s2).build();

        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(200);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);

        assertNotNull(best);
        assertTrue(best.getUnassignedJobs().isEmpty(), "All jobs should be assigned");

        for (VehicleRoute route : best.getRoutes()) {
            String vehicleId = route.getVehicle().getId();
            route.getTourActivities().getJobs().forEach(job -> {
                if (job.getId().equals("s1")) {
                    assertEquals("v1", vehicleId, "s1 must be on v1");
                } else if (job.getId().equals("s2")) {
                    assertEquals("v2", vehicleId, "s2 must be on v2");
                }
            });
        }
    }
}