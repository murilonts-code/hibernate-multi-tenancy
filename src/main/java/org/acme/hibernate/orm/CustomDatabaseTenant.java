package org.acme.hibernate.orm;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.flywaydb.core.Flyway;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidator;
import static java.time.Duration.ofSeconds;
import static org.flywaydb.core.api.MigrationVersion.LATEST;

@Unremovable
@ApplicationScoped
@PersistenceUnitExtension
public class CustomDatabaseTenant implements TenantConnectionResolver {

    @Inject
    @RestClient
    DbClient client;

    private final Map<String, ConnectionProvider> cache = new HashMap<>();

    private static AgroalDataSourceConfiguration createDataSourceConfiguration(DBConnectionInfo dbConnectionInfo) {
        return new AgroalDataSourceConfigurationSupplier()
                .dataSourceImplementation(AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL)
                .metricsEnabled(false)
                .connectionPoolConfiguration(cp -> cp
                        .minSize(2)
                        .maxSize(8)
                        .connectionValidator(defaultValidator())
                        .acquisitionTimeout(ofSeconds(5))
                        .leakTimeout(ofSeconds(5))
                        .validationTimeout(ofSeconds(50))
                        .reapTimeout(ofSeconds(500))
                        .connectionFactoryConfiguration(cf -> cf
                                .jdbcUrl("jdbc:postgresql://" + dbConnectionInfo.getHost() + ":" + dbConnectionInfo.getPort() + "/" + dbConnectionInfo.getDb())
                                .connectionProviderClassName("org.postgresql.Driver")
                                .principal(new NamePrincipal(dbConnectionInfo.getUsername()))
                                .credential(new SimplePassword(dbConnectionInfo.getPassword()))
                        )
                ).get();
    }

    @Override
    public ConnectionProvider resolve(String tenant) {
        if (!cache.containsKey(tenant)) {
            try {
                DBConnectionInfo dbConnectionInfo = client.getList(tenant);
                AgroalDataSource agroalDataSource = AgroalDataSource.from(createDataSourceConfiguration(dbConnectionInfo));
                QuarkusConnectionProvider quarkusConnectionProvider = new QuarkusConnectionProvider(agroalDataSource);
                validateConnection(dbConnectionInfo);
                cache.put(tenant, quarkusConnectionProvider);
                return quarkusConnectionProvider;
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to create a new data source based on the tenantId: " + tenant, ex);
            }
        }
        return cache.get(tenant);
    }

    private void validateConnection(DBConnectionInfo dbConnectionInfo) {
        Flyway load = Flyway.configure()
                .dataSource("jdbc:postgresql://" + dbConnectionInfo.getHost() + ":" +
                            dbConnectionInfo.getPort() + "/" + dbConnectionInfo.getDb(),
                        dbConnectionInfo.getUsername(), dbConnectionInfo.getPassword())
                .locations("classpath:database/base")
                .target(LATEST)
                .baselineOnMigrate(true)
                .load();
        load.migrate();
    }


}
