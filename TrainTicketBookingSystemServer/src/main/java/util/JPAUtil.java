package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    private static final String DEFAULT_PERSISTENCE_UNIT = "mariadb-pu";
    private static final EntityManagerFactory factory;

    static {
        String persistenceUnit = resolve("db.persistenceUnit", "DB_PERSISTENCE_UNIT", DEFAULT_PERSISTENCE_UNIT);
        Map<String, Object> overrides = new HashMap<>();

        putIfNotBlank(overrides, "jakarta.persistence.jdbc.url", resolve("db.url", "DB_URL", null));
        putIfNotBlank(overrides, "jakarta.persistence.jdbc.user", resolve("db.user", "DB_USER", null));
        putIfNotBlank(overrides, "jakarta.persistence.jdbc.password", resolve("db.password", "DB_PASSWORD", null));

        try {
            factory = overrides.isEmpty()
                    ? Persistence.createEntityManagerFactory(persistenceUnit)
                    : Persistence.createEntityManagerFactory(persistenceUnit, overrides);
        } catch (Exception e) {
            String configuredUrl = (String) overrides.getOrDefault("jakarta.persistence.jdbc.url", "<from persistence.xml>");
            String configuredUser = (String) overrides.getOrDefault("jakarta.persistence.jdbc.user", "<from persistence.xml>");
            throw new RuntimeException(
                    "Failed to initialize EntityManagerFactory. "
                            + "Check database credentials and connection. "
                            + "persistenceUnit=" + persistenceUnit
                            + ", url=" + configuredUrl
                            + ", user=" + configuredUser,
                    e
            );
        }
    }

    private static String resolve(String systemPropertyKey, String envKey, String defaultValue) {
        String value = System.getProperty(systemPropertyKey);
        if (value != null && !value.isBlank()) {
            return value;
        }

        value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            return value;
        }

        return defaultValue;
    }

    private static void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    public static EntityManagerFactory getFactory() {
        return factory;
    }

    public static EntityManager  getEntityManager() {
        return factory.createEntityManager();
    }
}
