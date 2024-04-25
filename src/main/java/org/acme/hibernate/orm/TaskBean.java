package org.acme.hibernate.orm;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.acme.hibernate.orm.configuration.context.TenantContext;
import org.jboss.logging.Logger;
import org.quartz.*;

import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;


@ApplicationScoped
public class TaskBean {

    private static final Logger LOGGER = Logger.getLogger(TaskBean.class.getName());

    @Inject
    org.quartz.Scheduler quartz;

    void onStart(@Observes StartupEvent event) throws SchedulerException {
//        Stream.of("mycompany", "quarkus_test", "testinho").forEach(this::createJobByTenantId);
        quartz.clear();
    }


    private void createJobByTenantId(String tenantId) {
        try {
            var job = JobBuilder.newJob(MyJob.class)
                    .withIdentity("myJob" + tenantId, "myGroup")
                    .usingJobData(TenantContext.TENANT_HEADER, tenantId)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("myTrigger"+ tenantId, "myGroup")
                    .startAt(Date.from(now().plusSeconds(10).atZone(ZoneId.systemDefault()).toInstant()))
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(5)
                                    .repeatForever())
                    .build();
            quartz.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            LOGGER.error(e.getMessage());
        }

    }

}