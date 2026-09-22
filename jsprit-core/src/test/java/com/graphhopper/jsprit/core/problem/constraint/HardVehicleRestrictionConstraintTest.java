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
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Hard Vehicle Restriction Constraint Test")
class HardVehicleRestrictionConstraintTest {

    private HardVehicleRestrictionConstraint constraint;
    private VehicleImpl vehiclePeter;
    private VehicleImpl vehicleStefan;
    private VehicleImpl vehicleDriverA;
    private VehicleRoute emptyRoute;

    @BeforeEach
    void setUp() {
        constraint = new HardVehicleRestrictionConstraint();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        vehiclePeter = VehicleImpl.Builder.newInstance("technician_peter")
            .setStartLocation(Location.newInstance("start")).setType(type).build();
        vehicleStefan = VehicleImpl.Builder.newInstance("technician_stefan")
            .setStartLocation(Location.newInstance("start")).setType(type).build();
        vehicleDriverA = VehicleImpl.Builder.newInstance("driver-A")
            .setStartLocation(Location.newInstance("start")).setType(type).build();

        emptyRoute = VehicleRoute.emptyRoute();
    }

    @Nested
    @DisplayName("Allowed Vehicles")
    class AllowedVehiclesTest {

        @Test
        @DisplayName("when no allowed vehicles set, any vehicle should be allowed")
        void whenNoAllowedVehiclesSet_anyVehicleShouldBeAllowed() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc")).build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
            assertTrue(constraint.fulfilled(ctx));
        }

        @Test
        @DisplayName("when vehicle is in allowed list, it should be allowed")
        void whenVehicleIsInAllowedList_itShouldBeAllowed() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc"))
                .setAllowedVehicles("technician_peter", "technician_stefan")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
            assertTrue(constraint.fulfilled(ctx));
        }

        @Test
        @DisplayName("when vehicle is not in allowed list, it should be rejected")
        void whenVehicleIsNotInAllowedList_itShouldBeRejected() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc"))
                .setAllowedVehicles("technician_peter", "technician_stefan")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehicleDriverA, null, 0.);
            assertFalse(constraint.fulfilled(ctx));
        }
    }

    @Nested
    @DisplayName("Disallowed Vehicles")
    class DisallowedVehiclesTest {

        @Test
        @DisplayName("when no disallowed vehicles set, any vehicle should be allowed")
        void whenNoDisallowedVehiclesSet_anyVehicleShouldBeAllowed() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc")).build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehicleDriverA, null, 0.);
            assertTrue(constraint.fulfilled(ctx));
        }

        @Test
        @DisplayName("when vehicle is in disallowed list, it should be rejected")
        void whenVehicleIsInDisallowedList_itShouldBeRejected() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc"))
                .setDisallowedVehicles("driver-A")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehicleDriverA, null, 0.);
            assertFalse(constraint.fulfilled(ctx));
        }

        @Test
        @DisplayName("when vehicle is not in disallowed list, it should be allowed")
        void whenVehicleIsNotInDisallowedList_itShouldBeAllowed() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc"))
                .setDisallowedVehicles("driver-A")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
            assertTrue(constraint.fulfilled(ctx));
        }
    }

    @Nested
    @DisplayName("Combined Allowed and Disallowed")
    class CombinedTest {

        @Test
        @DisplayName("when vehicle is allowed but also disallowed, it should be rejected")
        void whenVehicleIsAllowedButAlsoDisallowed_itShouldBeRejected() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc"))
                .setAllowedVehicles("technician_peter", "driver-A")
                .setDisallowedVehicles("driver-A")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehicleDriverA, null, 0.);
            assertFalse(constraint.fulfilled(ctx));
        }

        @Test
        @DisplayName("when vehicle is allowed and not disallowed, it should be allowed")
        void whenVehicleIsAllowedAndNotDisallowed_itShouldBeAllowed() {
            Service service = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance("loc"))
                .setAllowedVehicles("technician_peter", "driver-A")
                .setDisallowedVehicles("driver-A")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, service, vehiclePeter, null, 0.);
            assertTrue(constraint.fulfilled(ctx));
        }
    }

    @Nested
    @DisplayName("Shipment Vehicle Restrictions")
    class ShipmentTest {

        @Test
        @DisplayName("when shipment has allowed vehicles and vehicle matches, it should be allowed")
        void whenShipmentHasAllowedVehiclesAndVehicleMatches_itShouldBeAllowed() {
            Shipment shipment = Shipment.Builder.newInstance("ship1")
                .setPickupLocation(Location.newInstance("pickup"))
                .setDeliveryLocation(Location.newInstance("delivery"))
                .setAllowedVehicles("technician_peter")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, shipment, vehiclePeter, null, 0.);
            assertTrue(constraint.fulfilled(ctx));
        }

        @Test
        @DisplayName("when shipment has allowed vehicles and vehicle does not match, it should be rejected")
        void whenShipmentHasAllowedVehiclesAndVehicleDoesNotMatch_itShouldBeRejected() {
            Shipment shipment = Shipment.Builder.newInstance("ship1")
                .setPickupLocation(Location.newInstance("pickup"))
                .setDeliveryLocation(Location.newInstance("delivery"))
                .setAllowedVehicles("technician_peter")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, shipment, vehicleDriverA, null, 0.);
            assertFalse(constraint.fulfilled(ctx));
        }

        @Test
        @DisplayName("when shipment has disallowed vehicles and vehicle matches, it should be rejected")
        void whenShipmentHasDisallowedVehiclesAndVehicleMatches_itShouldBeRejected() {
            Shipment shipment = Shipment.Builder.newInstance("ship1")
                .setPickupLocation(Location.newInstance("pickup"))
                .setDeliveryLocation(Location.newInstance("delivery"))
                .setDisallowedVehicles("driver-A")
                .build();
            JobInsertionContext ctx = new JobInsertionContext(emptyRoute, shipment, vehicleDriverA, null, 0.);
            assertFalse(constraint.fulfilled(ctx));
        }
    }
}