package id.co.javara.app.health;

import id.co.javara.core.port.TransactionPort;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class T24HealthIndicator implements HealthIndicator {

    private final TransactionPort transactionPort;

    public T24HealthIndicator(TransactionPort transactionPort) {
        this.transactionPort = transactionPort;
    }

    @Override
    public Health health() {
        try {
            if (transactionPort.isHealthy()) {
                return Health.up()
                    .withDetail("adapter", transactionPort.getClass().getSimpleName())
                    .build();
            }
            return Health.down().withDetail("reason", "T24 unreachable").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
