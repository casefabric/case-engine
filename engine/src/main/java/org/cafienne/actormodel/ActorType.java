package org.cafienne.actormodel;

import org.cafienne.actormodel.identity.CaseUserIdentity;
import org.cafienne.actormodel.identity.ConsentGroupUser;
import org.cafienne.actormodel.identity.TenantUser;
import org.cafienne.actormodel.identity.UserIdentity;
import org.cafienne.actormodel.message.event.ModelEvent;
import org.cafienne.cmmn.actorapi.event.CaseEvent;
import org.cafienne.cmmn.instance.Case;
import org.cafienne.processtask.actorapi.event.ProcessEvent;
import org.cafienne.processtask.instance.ProcessTaskActor;
import org.cafienne.usermanagement.consentgroup.ConsentGroupActor;
import org.cafienne.usermanagement.consentgroup.actorapi.event.ConsentGroupEvent;
import org.cafienne.usermanagement.tenant.TenantActor;
import org.cafienne.usermanagement.tenant.actorapi.event.TenantEvent;
import org.cafienne.util.json.ValueMap;

import java.util.function.Function;

public enum ActorType {
    ModelActor(ModelActor.class, ModelEvent.class, UserIdentity::deserialize),
    Case(Case.class, CaseEvent.class, CaseUserIdentity::deserialize),
    Process(ProcessTaskActor.class, ProcessEvent.class, CaseUserIdentity::deserialize),
    Group(ConsentGroupActor.class, ConsentGroupEvent.class, ConsentGroupUser::deserialize),
    Tenant(TenantActor.class, TenantEvent.class, TenantUser::deserialize);

    public final String value;
    public final Class<? extends ModelActor> actorClass;
    public final Class<? extends ModelEvent> actorEventClass;
    public final boolean isCase;
    public final boolean isProcess;
    public final boolean isGroup;
    public final boolean isTenant;
    public final boolean isGeneric;
    public final boolean isModel;
    private final Function<ValueMap, UserIdentity> userReader;

    ActorType(Class<? extends ModelActor> actorClass, Class<? extends ModelEvent> actorEventClass, Function<ValueMap, UserIdentity> userReader) {
        this.actorClass = actorClass;
        this.actorEventClass = actorEventClass;
        this.userReader = userReader;
        this.value = actorClass.getSimpleName();
        this.isCase = actorClass == Case.class;
        this.isProcess = actorClass == ProcessTaskActor.class;
        this.isGroup = actorClass == ConsentGroupActor.class;
        this.isTenant = actorClass == TenantActor.class;
        this.isGeneric = actorClass == ModelActor.class;
        this.isModel = this.isCase || this.isProcess;
    }

    public <U extends UserIdentity> U readUser(ValueMap json) {
        return (U) this.userReader.apply(json);
    }

    public static ActorType getEnum(String value) {
        if (value == null) return null;
        for (ActorType type : values()) {
            if (type.value.equalsIgnoreCase(value)) return type;
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
