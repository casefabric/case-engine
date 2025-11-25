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

package org.cafienne.persistence.infrastructure.lastmodified;

import org.cafienne.actormodel.message.event.ActorModified;
import org.cafienne.actormodel.message.response.ActorLastModified;
import org.cafienne.persistence.infrastructure.lastmodified.registration.ActorWaitingList;
import scala.concurrent.Promise;

import java.time.Instant;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registration of the last modified timestamp per case instance. Can be used by writers to and query actors to get notified about CaseLastModified.
 */
public class LastModifiedRegistration {
    /**
     * Global startup moment of the whole JVM for last modified requests trying to be jumpy.
     */
    public final static Instant startupMoment = Instant.now();
    public Instant previousCleaningRound = startupMoment;
    public final String name;
    private final ConcurrentHashMap<String, ActorWaitingList> actorLists = new ConcurrentHashMap<>();
    public final static long MONITOR_PERIOD = 10 * 60 * 1000; // Every 10 minutes

    public LastModifiedRegistration(String name) {
        this.name = name;

        Timer timer = new Timer(this.name + "-monitor", true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Collection<ActorWaitingList> lists = actorLists.values();
                if (lists.isEmpty()) {
                    return;
                }
                Instant now = Instant.now();
                // Tell all actor lists that they should remove all waiters created _before_ the previous cleaning round
                // This will always avoid that "too recent" actor waiters get removed.
                // It only removes waiters that have waited for more than 10 minutes and are still waiting.
                lists.forEach(list -> list.cleanup(previousCleaningRound));
                previousCleaningRound = now;
//                System.out.println("Cleaning " + name +" remained with " + actorLists.size() +" elements");
            }
        };
        timer.schedule(task, MONITOR_PERIOD, MONITOR_PERIOD);  // Start only after 10 minutes
    }

    private ActorWaitingList getWaitingList(String actorId) {
        return actorLists.computeIfAbsent(actorId, id -> new ActorWaitingList(this, actorId));
    }

    public void removeWaitingList(String actorId) {
        actorLists.remove(actorId);
    }

    public Promise<String> waitFor(ActorLastModified notBefore) {
        return getWaitingList(notBefore.actorId).waitForLastModified(notBefore);
    }

    public Promise<String> waitFor(String actorId, String correlationId) {
        return getWaitingList(actorId).waitForCorrelationId(correlationId);
    }

    public void handle(ActorModified<?, ?> event) {
        getWaitingList(event.actorId()).handle(event);
    }
}
