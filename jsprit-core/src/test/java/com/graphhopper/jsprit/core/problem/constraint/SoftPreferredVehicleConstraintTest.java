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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Soft Preferred Vehicle Constraint Test")
class SoftPreferredVehicleConstraintTest {

    private SoftPreferredVehicleConstraint constraint;
    private VehicleImpl vehiclePeter;
    private VehicleImpl vehicleStefan;
    private VehicleImpl vehicleDriverA;
    private VehicleRoute emptyRoute;

    @BeforeEach
    void setUp() {
        constraint = new SoftPreferredVehicleConstraint(1000.0, 500.0);
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        vehiclePeter = VehicleImpl.Builder.newInstance("technician_peter")
            .setStartLocation(Location.newInstance("start")).setType(type).build();
        vehicleStefan = VehicleImpl.Builder.newInstance("technician_stefan")
            .setStartLocation(Location.newInstance("start")).setType(type).build();
        vehicleDriverA = VehicleImpl.Builder.newInstance("driver-A")
            .setStartLocation(Location.newInstance("start")).setType(type).build();
        emptyRoute = VehicleRoute.emptyRoute();
    }

    @Test
    @DisplayName("when no preferred vehicles, cost should be zero")
    void whenNoPreferredVehicles_costShouldBeZero() {
        Service service = Service.Builder.newInstance("s1")
            .setLocation(Location.newInstance("loc")).build();
        JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
        assertEquals(0.0, constraint.getCosts(ctx), 0.001);
    }

    @Test
    @DisplayName("when vehicle is preferred with priority 1, cost should be maximum negative bonus")
    void whenVehicleIsPreferredWithPriority1_costShouldBeMaxNegativeBonus() {
        Service service = Service.Builder.newInstance("s1")
            .setLocation(Location.newInstance("loc"))
            .addPreferredVehicle("technician_peter", 1)
            .build();
        JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
        double cost = constraint.getCosts(ctx);
        // priority 1: bonus = -500 * (11-1)/10 = -500.0
        assertEquals(-500.0, cost, 0.001);
    }

    @Test
    @DisplayName("when vehicle is preferred with priority 10, cost should be minimum negative bonus")
    void whenVehicleIsPreferredWithPriority10_costShouldBeMinNegativeBonus() {
        Service service = Service.Builder.newInstance("s1")
            .setLocation(Location.newInstance("loc"))
            .addPreferredVehicle("technician_peter", 10)
            .build();
        JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
        double cost = constraint.getCosts(ctx);
        // priority 10: bonus = -500 * (11-10)/10 = -50.0
        assertEquals(-50.0, cost, 0.001);
    }

    @Test
    @DisplayName("when vehicle is preferred with default priority 2, cost should reflect that")
    void whenVehicleIsPreferredWithDefaultPriority_costShouldReflect() {
        Service service = Service.Builder.newInstance("s1")
            .setLocation(Location.newInstance("loc"))
            .addPreferredVehicle("technician_peter")
            .build();
        JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
        double cost = constraint.getCosts(ctx);
        // default priority 2: bonus = -500 * (11-2)/10 = -450.0
        assertEquals(-450.0, cost, 0.001);
    }

    @Test
    @DisplayName("when vehicle is not preferred but preferences exist, cost should be penalty")
    void whenVehicleIsNotPreferred_costShouldBePenalty() {
        Service service = Service.Builder.newInstance("s1")
            .setLocation(Location.newInstance("loc"))
            .addPreferredVehicle("technician_peter", 1)
            .build();
        JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehicleDriverA, null, 0.);
        double cost = constraint.getCosts(ctx);
        assertEquals(1000.0, cost, 0.001);
    }

    @Test
    @DisplayName("higher priority preferred vehicle should get bigger bonus")
    void higherPriorityVehicleShouldGetBiggerBonus() {
        Service service = Service.Builder.newInstance("s1")
            .setLocation(Location.newInstance("loc"))
            .addPreferredVehicle("technician_peter", 1)
            .addPreferredVehicle("technician_stefan", 5)
            .build();

        JobInsertionContext ctxPeter = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
        JobInsertionContext ctxStefan = new JobInsertionContext(emptyRoute, service, vehicleStefan, null, 0.);

        double costPeter = constraint.getCosts(ctxPeter);
        double costStefan = constraint.getCosts(ctxStefan);

        assertTrue(costPeter < costStefan, "Peter (priority 1) should get a bigger bonus (more negative) than Stefan (priority 5)");
    }

    @Test
    @DisplayName("custom penalty and bonus values should work")
    void customPenaltyAndBonusShouldWork() {
        SoftPreferredVehicleConstraint customConstraint = new SoftPreferredVehicleConstraint(2000.0, 800.0);
        Service service = Service.Builder.newInstance("s1")
            .setLocation(Location.newInstance("loc"))
            .addPreferredVehicle("technician_peter", 1)
            .build();

        JobInsertionContext ctxPreferred = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
        JobInsertionContext ctxNonPreferred = new JobInsertionContext(emptyRoute, service, vehicleDriverA, null, 0.);

        // priority 1: bonus = -800 * (11-1)/10 = -800.0
        assertEquals(-800.0, customConstraint.getCosts(ctxPreferred), 0.001);
        assertEquals(2000.0, customConstraint.getCosts(ctxNonPreferred), 0.001);
    }

    @Test
    @DisplayName("shipment preferred vehicles should work the same way")
    void shipmentPreferredVehiclesShouldWork() {
        Shipment shipment = Shipment.Builder.newInstance("ship1")
            .setPickupLocation(Location.newInstance("pickup"))
            .setDeliveryLocation(Location.newInstance("delivery"))
            .addPreferredVehicle("technician_peter", 1)
            .addPreferredVehicle("technician_stefan", 3)
            .build();

        JobInsertionContext ctxPeter = new JobInsertionContext(emptyRoute, shipment, vehiclePeter, null, 0.);
        JobInsertionContext ctxNonPreferred = new JobInsertionContext(emptyRoute, shipment, vehicleDriverA, null, 0.);

        assertTrue(constraint.getCosts(ctxPeter) < 0, "Preferred vehicle should get a bonus (negative cost)");
        assertEquals(1000.0, constraint.getCosts(ctxNonPreferred), 0.001);
    }
}