package org.acme.hibernate.orm.configuration;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.narayana.NarayanaTransactionIntegration;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.acme.hibernate.orm.model.DTO.DBConnectionInfo;
import org.acme.hibernate.orm.client.DbClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.flywaydb.core.Flyway;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidator;
import static java.time.Duration.ofSeconds;

@ApplicationScoped
@PersistenceUnitExtension
public class CustomDatabaseTenant implements TenantConnectionResolver {

    private final DbClient client;
    private final TransactionManager txManager;
    private final TransactionSynchronizationRegistry txSyncRegistry;


    public CustomDatabaseTenant(TransactionManager txManager, TransactionSynchronizationRegistry txSyncRegistry
            , @RestClient DbClient client) {
        this.txManager = txManager;
        this.txSyncRegistry = txSyncRegistry;
        this.client = client;
    }

    private final Map<String, ConnectionProvider> cache = new HashMap<>();

    @Override
    public ConnectionProvider resolve(String tenant) {
        if (!cache.containsKey(tenant)) {
            DBConnectionInfo dbConnectionInfo = client.getList(tenant);
            QuarkusConnectionProvider quarkusConnectionProvider = validateConnection(dbConnectionInfo);
            cache.put(tenant, quarkusConnectionProvider);
            return quarkusConnectionProvider;
        }
        return cache.get(tenant);
    }

    private QuarkusConnectionProvider validateConnection(DBConnectionInfo dbConnectionInfo) {
        String jdbcUrl = "jdbc:postgresql://" + dbConnectionInfo.host() + ":" +
                         dbConnectionInfo.port() + "/" + dbConnectionInfo.db();

        try {
            var flywayDataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();
            flywayDataSourceConfiguration
                    .connectionPoolConfiguration()
                    .maxSize(2)
                    .connectionValidator(defaultValidator())
                    .connectionFactoryConfiguration()
                    .principal(new NamePrincipal(dbConnectionInfo.username()))
                    .credential(new SimplePassword(dbConnectionInfo.password()))
                    .connectionProviderClassName("org.postgresql.Driver")
                    .jdbcUrl(jdbcUrl);
            var flyway = Flyway
                    .configure()
                    .dataSource(AgroalDataSource.from(flywayDataSourceConfiguration))
                    .cleanDisabled(false)
                    .load();

            flyway.migrate();
            flyway.validate();

            var txIntegration = new NarayanaTransactionIntegration(txManager, txSyncRegistry, null, false, null);
            var dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier()
                    .dataSourceImplementation(AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL)
                    .metricsEnabled(false)
                    .connectionPoolConfiguration(cp ->
                            cp
                                    .minSize(2)
                                    .maxSize(8)
                                    .connectionValidator(defaultValidator())
                                    .acquisitionTimeout(ofSeconds(5))
                                    .leakTimeout(ofSeconds(5))
                                    .validationTimeout(ofSeconds(50))
                                    .reapTimeout(ofSeconds(500))
                                    .transactionIntegration(txIntegration)
                                    .connectionFactoryConfiguration(cf -> cf
                                            .jdbcUrl("jdbc:postgresql://" + dbConnectionInfo.host() + ":" + dbConnectionInfo.port() + "/" + dbConnectionInfo.db())
                                            .connectionProviderClassName("org.postgresql.Driver")
                                            .principal(new NamePrincipal(dbConnectionInfo.username()))
                                            .credential(new SimplePassword(dbConnectionInfo.password()))
                                    )
                    );

            return new QuarkusConnectionProvider(AgroalDataSource.from(dataSourceConfiguration.get()));
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

//        Flyway load = Flyway.configure()
//                .dataSource("jdbc:postgresql://" + dbConnectionInfo.host() + ":" +
//                            dbConnectionInfo.port() + "/" + dbConnectionInfo.db(),
//                        dbConnectionInfo.username(), dbConnectionInfo.password())
//                .locations("classpath:database/base")
//                .target(LATEST)
//                .baselineOnMigrate(true)
//                .load();
//        load.migrate();
    }


}
