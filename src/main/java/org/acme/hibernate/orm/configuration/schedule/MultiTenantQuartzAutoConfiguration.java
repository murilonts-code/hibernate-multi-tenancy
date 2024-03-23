package org.acme.hibernate.orm.configuration.schedule;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

@ApplicationScoped
public class MultiTenantQuartzAutoConfiguration {

    @Inject
    Scheduler scheduler;

    public void schedulerFactory(@Observes StartupEvent ev) {
        try {
            scheduler.getListenerManager().addJobListener(new MultiTenantQuartzJobListener());
        } catch (SchedulerException e) {
            throw new RuntimeException("Falha ao registrar o JobListener no Quartz", e);
        }
    }
}
