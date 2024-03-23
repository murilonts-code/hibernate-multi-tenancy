package org.acme.hibernate.orm.configuration.context;

public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    public static final String TENANT_HEADER = "tenantId";

    /**
     * Define o ID do tenant atual para o contexto (thread) atual.
     *
     * @param tenantId O ID do tenant a ser definido.
     */
    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Obtém o ID do tenant atual para o contexto (thread) atual.
     *
     * @return O ID do tenant atual.
     */
    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * Limpa o ID do tenant do contexto (thread) atual.
     */
    public static void clear() {
        currentTenant.remove();
    }
}

