package org.acme.hibernate.orm.configuration.schedule;

import org.acme.hibernate.orm.configuration.context.TenantContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class MultiTenantQuartzJobListener implements JobListener {

    private static final String NAME = "tenantContext";


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        TenantContext.setCurrentTenant(jobExecutionContext.getJobDetail().getJobDataMap().getString(TenantContext.TENANT_HEADER));
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        TenantContext.clear();
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        TenantContext.clear();
    }
}
