/*
 * Copyright (C) 2014  Batav B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cafienne.model.cmmn.instance.sentry;

import org.cafienne.model.cmmn.definition.DefinitionElement;
import org.cafienne.model.cmmn.definition.ItemDefinition;
import org.cafienne.model.cmmn.definition.sentry.CriterionDefinition;
import org.cafienne.model.cmmn.instance.CMMNElement;
import org.cafienne.model.cmmn.instance.PlanItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public abstract class CriteriaListener<T extends CriterionDefinition, C extends Criterion<T>> extends CMMNElement<ItemDefinition> {
    protected final PlanItem<?> item;
    protected final Collection<C> criteria = new ArrayList<>();
    protected final Collection<T> definitions;
    private final String logDescription;
    private boolean isConnected = false;

    protected CriteriaListener(PlanItem<?> item, Collection<T> definitions) {
        super(item, item.getItemDefinition());
        this.item = item;
        this.definitions = definitions;
        // LogDescription basically contains 'entry criteria', 'exit criteria', or 'reactivation criteria'
        this.logDescription = getClass().getSimpleName().substring(8).toLowerCase(Locale.ROOT) + " criteria";
    }

    /**
     * Start listening to the sentry network
     */
    public void startListening() {
        this.isConnected = true;
        this.definitions.forEach(this::addCriterion);
        if (!criteria.isEmpty()) {
            item.getCaseInstance().addDebugInfo(() -> "Connected " + item + " to " + criteria.size() + " " + logDescription);
        }
    }

    /**
     * Indicates whether the PlanItem is no longer awaiting events on any of the criteria.
     */
    protected boolean isDisconnected() {
        return !isConnected;
    }

    private void addCriterion(T definition) {
        addDebugInfo(() -> " - creating " + definition.toString());
        C criterion = createCriterion(definition);
        criteria.add(criterion);
    }

    protected abstract C createCriterion(T definition);

    /**
     * Stop listening to the sentry network, typically when the criterion is satisfied.
     */
    public void stopListening() {
        this.isConnected = false;
        if (!criteria.isEmpty()) {
            addDebugInfo(() -> "Disconnecting " + item + " from " + criteria.size() + " " + logDescription);
        }
        new ArrayList<>(criteria).forEach(this::release);
    }

    /**
     * Removes the criterion from our collection and tells the criterion to disconnect from the network.
     */
    private void release(C criterion) {
        criteria.remove(criterion);
        criterion.release();
    }

    void satisfied(Criterion<?> criterion) {
        if (isConnected) {
            satisfy(criterion);
        } else {
            addDebugInfo(() -> "Skip handling a satisified " + criterion.getClass().getSimpleName() + " in the " + getClass().getSimpleName() + " of " + item + ". The " + getClass().getSimpleName() + " is already disconnected");
        }
    }

    protected abstract void satisfy(Criterion<?> criterion);

    protected abstract void migrateCriteria(ItemDefinition newItemDefinition, boolean skipLogic);

    protected void migrateCriteria(Collection<T> newDefinitions, Collection<T> existingDefinitions, boolean skipLogic) {
        if (isDisconnected()) {
            addDebugInfo(() -> "Skipping " + logDescription + " migration of " + item + " as they are disconnected");
            return;
        }
        addDebugInfo(() -> {
            if (criteria.isEmpty() && newDefinitions.isEmpty()) {
                return "";
            } else {
                return "Migrating " + logDescription + " of " + item;
            }
        });
        Collection<C> existingCriteria = new ArrayList<>(criteria);

        existingCriteria.forEach(criterion -> migrateCriterion(criterion, newDefinitions, skipLogic));
        findNewDefinitions(newDefinitions, existingDefinitions).forEach(this::addCriterion);
    }

    private Collection<T> findNewDefinitions(Collection<T> newDefinitions, Collection<T> existingDefinitions) {
        return newDefinitions.stream().filter(newDefinition -> existingDefinitions.stream().allMatch(existingDefinition -> existingDefinition.differs(newDefinition))).toList();
    }

    private void migrateCriterion(C criterion, Collection<T> newDefinitions, boolean skipLogic) {
        T oldDefinition = criterion.getDefinition();
        T newDefinition = DefinitionElement.findDefinition(oldDefinition, newDefinitions);
        if (newDefinition != null) {
            criterion.migrateDefinition(newDefinition, skipLogic);
        } else {
            // Not sure what to do here. Remove the criterion?
            // Search for a 'nearby' alternative?
            addDebugInfo(() -> " - dropping " + criterion);
            this.release(criterion);
        }
    }
}
