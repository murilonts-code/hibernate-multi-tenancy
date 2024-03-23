package org.acme.hibernate.orm.configuration.context;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Optional;

@Unremovable
@PersistenceUnitExtension
public class CustomTenantResolver implements TenantResolver {


    @Inject
    RoutingContext context;

    @Override
    public String getDefaultTenantId() {
        return "quarkus_test";
    }

    @Override
    public String resolveTenantId() {
        return Optional.ofNullable(TenantContext.getCurrentTenant())
                .orElseGet(() -> {
                    String path = context.request().path();
                    System.out.println("absolute path is " + context.request().absoluteURI());
                    final String tenantId;
                    if (path.startsWith("/mycompany")) {
                        tenantId = "mycompany";
                    } else if (path.startsWith("/testinho")) {
                        tenantId = "testinho";
                    } else {
                        tenantId = getDefaultTenantId();
                    }
                    return tenantId;
                });

    }

}
