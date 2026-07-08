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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.algorithm.state.MinLoadAdjustmentProvider;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * Constraint that ensures capacity constraint at each activity.
 * <p>
 * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
 * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
 *
 * @author schroeder
 */
public class PickupAndDeliverShipmentLoadActivityLevelConstraint implements HardActivityConstraint {

    // --- FP instrumentation (log-only, enabled via env FP_DEBUG_JOB=SO123,SO456) ---
    private static final java.util.Set<String> DEBUG_JOBS;
    static {
        String env = System.getenv("FP_DEBUG_JOB");
        if (env == null || env.trim().isEmpty()) DEBUG_JOBS = java.util.Collections.emptySet();
        else DEBUG_JOBS = new java.util.HashSet<>(java.util.Arrays.asList(env.trim().split("\\s*,\\s*")));
    }
    private static boolean fpDebug(JobInsertionContext iFacts) {
        return !DEBUG_JOBS.isEmpty() && iFacts.getJob() != null && DEBUG_JOBS.contains(iFacts.getJob().getId());
    }
    private static String fpActName(TourActivity act) {
        if (act instanceof TourActivity.JobActivity) {
            return act.getName() + "[" + ((TourActivity.JobActivity) act).getJob().getId() + "]";
        }
        return act.getName();
    }
    // -------------------------------------------------------------------------------

    private RouteAndActivityStateGetter stateManager;

    private Capacity defaultValue;

    /**
     * Constructs the constraint ensuring capacity constraint at each activity.
     * <p>
     * <p>This is critical to consistently calculate pd-problems with capacity constraints. Critical means
     * that is MUST be visited. It also assumes that pd-activities are visited in the order they occur in a tour.
     *
     * @param stateManager the stateManager
     */
    public PickupAndDeliverShipmentLoadActivityLevelConstraint(RouteAndActivityStateGetter stateManager) {
        super();
        this.stateManager = stateManager;
        defaultValue = Capacity.Builder.newInstance().build();
    }

    /**
     * Checks whether there is enough capacity to insert newAct between prevAct and nextAct.
     */
    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (!isShipmentPickup(newAct) && !isShipmentDelivery(newAct)) {
            return ConstraintsStatus.FULFILLED;
        }
        Capacity loadAtPrevAct;
        if (prevAct instanceof Start) {
            loadAtPrevAct = stateManager.getRouteState(iFacts.getRoute(), InternalStates.LOAD_AT_BEGINNING, Capacity.class);
            if (loadAtPrevAct == null) loadAtPrevAct = defaultValue;
        } else {
            loadAtPrevAct = stateManager.getActivityState(prevAct, InternalStates.LOAD, Capacity.class);
            if (loadAtPrevAct == null) loadAtPrevAct = defaultValue;
        }
        if (isShipmentPickup(newAct)) {
            Capacity effSize = effectivePickupSize(iFacts, newAct);
            Capacity addUp = Capacity.addup(loadAtPrevAct, effSize);
            boolean fits = addUp.isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions());
            if (fpDebug(iFacts)) {
                System.err.println("[LOADC-DEBUG] job=" + iFacts.getJob().getId()
                    + " PICKUP prev=" + fpActName(prevAct) + (prevAct instanceof Start ? "(LOAD_AT_BEGINNING)" : "")
                    + " loadAtPrev=" + loadAtPrevAct
                    + " effSize=" + effSize + " nominal=" + newAct.getSize()
                    + " cap=" + iFacts.getNewVehicle().getType().getCapacityDimensions()
                    + " verdict=" + (fits ? "OK" : "NOT_FULFILLED"));
            }
            if (!fits) {
                return ConstraintsStatus.NOT_FULFILLED;
            }
        }
        if (isShipmentDelivery(newAct)) {
            // Load on the segment carrying this shipment = loadAtPrevAct + what the sibling
            // pickup ACTUALLY adds (effective size). Note DeliverShipment.getSize() is
            // NEGATIVE (-nominal); in vanilla jSprit the term Capacity.invert(newAct.getSize())
            // (= +nominal) was precisely the in-flight pickup contribution, since stored LOAD
            // states do not include the not-yet-inserted shipment. Keeping BOTH the invert term
            // and an inFlightPickup term double-counts the shipment (effective + nominal) and
            // makes any job with effective + nominal > capacity uninsertable at every position.
            Capacity inFlightPickup = Capacity.invert(newAct.getSize()); // +nominal fallback
            if (iFacts.getJob() instanceof com.graphhopper.jsprit.core.problem.job.Shipment) {
                for (TourActivity assoc : iFacts.getAssociatedActivities()) {
                    if (assoc instanceof PickupShipment) {
                        inFlightPickup = effectivePickupSize(iFacts, assoc);
                    }
                }
            }
            Capacity addUp = Capacity.addup(loadAtPrevAct, inFlightPickup);
            boolean fits = addUp.isLessOrEqual(iFacts.getNewVehicle().getType().getCapacityDimensions());
            if (fpDebug(iFacts)) {
                System.err.println("[LOADC-DEBUG] job=" + iFacts.getJob().getId()
                    + " DELIVERY prev=" + fpActName(prevAct) + (prevAct instanceof Start ? "(LOAD_AT_BEGINNING)" : "")
                    + " loadAtPrev=" + loadAtPrevAct
                    + " inFlightPickup=" + inFlightPickup
                    + " deliverNominal=" + newAct.getSize()
                    + " cap=" + iFacts.getNewVehicle().getType().getCapacityDimensions()
                    + " verdict=" + (fits ? "OK" : "NOT_FULFILLED_BREAK"));
            }
            if (!fits)
                return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }
        return ConstraintsStatus.FULFILLED;
    }

    /**
     * Effective pickup size for capacity evaluation. Consults the
     * MinLoadAdjustmentProvider (if registered on a full StateManager) which
     * unifies minLoad increases and initial-inventory reductions computed by
     * the private project. Falls back to the nominal activity size.
     */
    private Capacity effectivePickupSize(JobInsertionContext iFacts, TourActivity newAct) {
        Capacity original = newAct.getSize();
        if (!(stateManager instanceof StateManager)) return original;
        MinLoadAdjustmentProvider provider = ((StateManager) stateManager).getMinLoadAdjustmentProvider();
        if (provider == null) return original;
        if (!(newAct instanceof TourActivity.JobActivity)) return original;
        String jobId = ((TourActivity.JobActivity) newAct).getJob().getId();
        String routeId = iFacts.getRoute().getRouteId();
        int adjustedDim0 = provider.getAdjustedSize(routeId, iFacts.getNewVehicle().getId(), jobId);
        if (adjustedDim0 < 0) return original;
        Capacity.Builder builder = Capacity.Builder.newInstance();
        builder.addDimension(0, adjustedDim0);
        for (int i = 1; i < original.getNuOfDimensions(); i++) {
            builder.addDimension(i, original.get(i));
        }
        return builder.build();
    }

    private static boolean isShipmentDelivery(TourActivity newAct) {
        return newAct instanceof DeliverShipment;
    }

    private static boolean isShipmentPickup(TourActivity newAct) {
        return newAct instanceof PickupShipment;
    }


}
