package Auth.service.security;

/**
 * Yeh class "ThreadLocal" ka use karti hai.
 * ThreadLocal har ek user request ke liye memory mein alag storage banata hai,
 * taaki ek user ki tenantId dusre user se mix na ho.
 */
public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    // Jab user login kare ya request aaye, tab ID set karne ke liye
    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    // Database query ke waqt ID nikalne ke liye
    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    // Request khatam hone par memory saaf karne ke liye (Very Important)
    public static void clear() {
        currentTenant.remove();
    }
}