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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Preferred Vehicle Test")
class PreferredVehicleTest {

    @Test
    @DisplayName("should create with id and default priority")
    void shouldCreateWithIdAndDefaultPriority() {
        PreferredVehicle pv = new PreferredVehicle("v1");
        assertEquals("v1", pv.getVehicleId());
        assertEquals(2, pv.getPriority());
    }

    @Test
    @DisplayName("should create with id and custom priority")
    void shouldCreateWithIdAndCustomPriority() {
        PreferredVehicle pv = new PreferredVehicle("v1", 5);
        assertEquals("v1", pv.getVehicleId());
        assertEquals(5, pv.getPriority());
    }

    @Test
    @DisplayName("should throw for priority 0")
    void shouldThrowForPriority0() {
        assertThrows(IllegalArgumentException.class, () -> new PreferredVehicle("v1", 0));
    }

    @Test
    @DisplayName("should throw for priority 11")
    void shouldThrowForPriority11() {
        assertThrows(IllegalArgumentException.class, () -> new PreferredVehicle("v1", 11));
    }

    @Test
    @DisplayName("should throw for negative priority")
    void shouldThrowForNegativePriority() {
        assertThrows(IllegalArgumentException.class, () -> new PreferredVehicle("v1", -1));
    }

    @Test
    @DisplayName("should throw for null id")
    void shouldThrowForNullId() {
        assertThrows(IllegalArgumentException.class, () -> new PreferredVehicle(null));
    }

    @Test
    @DisplayName("should throw for empty id")
    void shouldThrowForEmptyId() {
        assertThrows(IllegalArgumentException.class, () -> new PreferredVehicle(""));
    }

    @Test
    @DisplayName("should accept priority 1")
    void shouldAcceptPriority1() {
        PreferredVehicle pv = new PreferredVehicle("v1", 1);
        assertEquals(1, pv.getPriority());
    }

    @Test
    @DisplayName("should accept priority 10")
    void shouldAcceptPriority10() {
        PreferredVehicle pv = new PreferredVehicle("v1", 10);
        assertEquals(10, pv.getPriority());
    }

    @Test
    @DisplayName("two preferred vehicles with same id should be equal")
    void twoPreferredVehiclesWithSameIdShouldBeEqual() {
        PreferredVehicle pv1 = new PreferredVehicle("v1", 1);
        PreferredVehicle pv2 = new PreferredVehicle("v1", 5);
        assertEquals(pv1, pv2);
        assertEquals(pv1.hashCode(), pv2.hashCode());
    }

    @Test
    @DisplayName("two preferred vehicles with different id should not be equal")
    void twoPreferredVehiclesWithDifferentIdShouldNotBeEqual() {
        PreferredVehicle pv1 = new PreferredVehicle("v1", 1);
        PreferredVehicle pv2 = new PreferredVehicle("v2", 1);
        assertNotEquals(pv1, pv2);
    }
}