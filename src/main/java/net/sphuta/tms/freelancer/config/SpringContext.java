package net.sphuta.tms.freelancer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SpringContext
 *
 * <p>This utility class provides a static way to access Spring-managed beans
 * from non-Spring-managed classes such as JPA entity listeners or static contexts.
 *
 * <p>It safely stores a reference to the {@link ApplicationContext} at startup
 * and allows beans to be retrieved dynamically using {@link #getBean(Class)}.
 *
 * <p>Example usage:
 * <pre>
 * MyService service = SpringContext.getBean(MyService.class);
 * </pre>
 *
 * <p>⚙️ Typical use case: JPA Listeners (like {@code AuthUserListener})
 * that need repository access but cannot be directly injected by Spring.
 *
 * @author ChatGPT
 * @since 2025-10-13
 */
@Slf4j
@Component
public class SpringContext {

    /** Holds the reference to Spring's application context */
    private static ApplicationContext context;

    /**
     * Constructor-based injection of the {@link ApplicationContext}.
     * <p>This initializes the static context once the Spring container starts.
     *
     * @param ctx the active Spring ApplicationContext
     */
    @Autowired
    public SpringContext(ApplicationContext ctx) {
        context = ctx;
        log.info("✅ SpringContext initialized — ApplicationContext captured successfully.");
    }

    /**
     * Retrieve a Spring-managed bean dynamically by its class type.
     *
     * @param clazz The class of the desired bean
     * @param <T>   The bean type
     * @return The Spring-managed bean instance
     */
    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {
            log.error("❌ Attempted to get bean [{}] before ApplicationContext was initialized.", clazz.getSimpleName());
            throw new IllegalStateException("ApplicationContext not initialized yet");
        }

        // -------------------------------------------------------------
        // Fetch the requested bean from the Spring ApplicationContext
        // -------------------------------------------------------------
        try {
            T bean = context.getBean(clazz);
            log.debug("🔍 Retrieved bean [{}] successfully from ApplicationContext.", clazz.getSimpleName());
            return bean;
        } catch (Exception e) {
            log.error("❌ Failed to retrieve bean [{}]: {}", clazz.getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }
}
