package org.acme.hibernate.orm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.acme.hibernate.orm.configuration.context.TenantContext;
import org.acme.hibernate.orm.repository.CommonRepo;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@ApplicationScoped
public class MyJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(MyJob.class.getName());

    @Inject
    CommonRepo commonRepo;

    @ActivateRequestContext
    public void execute(JobExecutionContext context) {
        LOGGER.info(commonRepo.listAll() + TenantContext.getCurrentTenant());
    }

}
