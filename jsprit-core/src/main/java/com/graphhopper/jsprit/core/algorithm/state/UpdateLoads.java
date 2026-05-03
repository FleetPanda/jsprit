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
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity.JobActivity;

import java.util.Collection;


/**
 * Updates load at start and end of route as well as at each activity. And update is triggered when either
 * activityVisitor has been started, the insertion process has been started or a job has been inserted.
 * <p>
 * <p>Note that this only works properly if you register this class as ActivityVisitor AND InsertionStartsListener AND JobInsertedListener.
 * The reason behind is that activity states are dependent on route-level states and vice versa. If this is properly registered,
 * this dependency is solved automatically.
 *
 * @author stefan
 */
class UpdateLoads implements ActivityVisitor, StateUpdater, InsertionStartsListener, JobInsertedListener {

    private final StateManager stateManager;

    /*
     * default has one dimension with a value of zero
     */
    private Capacity currentLoad;

    private Capacity defaultValue;

    /**
     * The route currently being visited.
     * Set in begin(), cleared in finish().
     * Used to look up minLoad adjustments keyed by routeId.
     */
    private VehicleRoute currentRoute;

    public UpdateLoads(StateManager stateManager) {
        super();
        this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
    }

    @Override
    public void begin(VehicleRoute route) {
        currentRoute = route;
        currentLoad = stateManager.getRouteState(route, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
        if (currentLoad == null) currentLoad = defaultValue;
    }

    @Override
    public void visit(TourActivity act) {
        Capacity size = effectiveSize(act);
        currentLoad = Capacity.addup(currentLoad, size);
        stateManager.putInternalTypedActivityState(act, InternalStates.LOAD, currentLoad);
    }

    @Override
    public void finish() {
        currentLoad = Capacity.Builder.newInstance().build();
        currentRoute = null;
    }

    /**
     * Returns the effective size of the activity, applying any minLoad adjustment
     * via the MinLoadAdjustmentProvider registered on the StateManager.
     *
     * Only dimension 0 is adjusted (quantity); other dimensions are preserved.
     * If no provider is registered or no adjustment applies, returns the original size.
     */

    private Capacity effectiveSize(TourActivity act) {
        Capacity original = act.getSize();
        if (currentRoute == null || !(act instanceof JobActivity)) return original;

        // MinLoad adjustments apply ONLY to pickups — the extra product is taken
        // from the terminal and stays on the vehicle as retain after delivery.
        // Deliveries always subtract the original ordered quantity.
        if (!(act instanceof com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity)) {
            return original;
        }

        MinLoadAdjustmentProvider provider = stateManager.getMinLoadAdjustmentProvider();
        if (provider == null) return original;

        String jobId = ((JobActivity) act).getJob().getId();
        int adjustedDim0 = provider.getAdjustedSize(currentRoute.getRouteId(), jobId);
        if (adjustedDim0 < 0) return original;

        // Rebuild Capacity with adjusted dimension 0, preserving all other dimensions
        Capacity.Builder builder = Capacity.Builder.newInstance();
        builder.addDimension(0, adjustedDim0);
        for (int i = 1; i < original.getNuOfDimensions(); i++) {
            builder.addDimension(i, original.get(i));
        }
        return builder.build();
    }

    void insertionStarts(VehicleRoute route) {
        Capacity loadAtDepot = Capacity.Builder.newInstance().build();
        Capacity loadAtEnd = Capacity.Builder.newInstance().build();
        for (Job j : route.getTourActivities().getJobs()) {
            if (j.isPickedUpAtVehicleStart()) {
                loadAtDepot = Capacity.addup(loadAtDepot, j.getSize());
            }
            if (j.isDeliveredToVehicleEnd()) {
                loadAtEnd = Capacity.addup(loadAtEnd, j.getSize());
            }
        }
        stateManager.putTypedInternalRouteState(route, InternalStates.LOAD_AT_BEGINNING, loadAtDepot);
        stateManager.putTypedInternalRouteState(route, InternalStates.LOAD_AT_END, loadAtEnd);
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        for (VehicleRoute route : vehicleRoutes) {
            insertionStarts(route);
        }
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, InsertionData insertionData) {
        if (job2insert.isPickedUpAtVehicleStart()) {
            Capacity loadAtDepot = stateManager.getRouteState(inRoute, InternalStates.LOAD_AT_BEGINNING, Capacity.class);
            if (loadAtDepot == null) loadAtDepot = defaultValue;
            stateManager.putTypedInternalRouteState(inRoute, InternalStates.LOAD_AT_BEGINNING, Capacity.addup(loadAtDepot, job2insert.getSize()));
        }
        if (job2insert.isDeliveredToVehicleEnd()) {
            Capacity loadAtEnd = stateManager.getRouteState(inRoute, InternalStates.LOAD_AT_END, Capacity.class);
            if (loadAtEnd == null) loadAtEnd = defaultValue;
            stateManager.putTypedInternalRouteState(inRoute, InternalStates.LOAD_AT_END, Capacity.addup(loadAtEnd, job2insert.getSize()));
        }
    }

    public void informRouteChanged(VehicleRoute route){
        insertionStarts(route);
    }


}
