package coffeeshout.global;

import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MetricsTestComponent {

    private final MeterRegistry meterRegistry;

    public MetricsTestComponent(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void testMetrics() {
        log.info("=== 메트릭 테스트 시작 ===");
        log.info("MeterRegistry 클래스: {}", meterRegistry.getClass().getName());

        log.info("=== 클래스패스 확인 ===");

        try {
            Class.forName("io.micrometer.cloudwatch2.CloudWatchMeterRegistry");
            log.info("✅ CloudWatch MeterRegistry 클래스 존재");
        } catch (ClassNotFoundException e) {
            log.error("❌ CloudWatch MeterRegistry 클래스 없음");
        }

        try {
            Class.forName("software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient");
            log.info("✅ AWS SDK CloudWatch 클라이언트 존재");
        } catch (ClassNotFoundException e) {
            log.error("❌ AWS SDK CloudWatch 클라이언트 없음");
        }

        // 테스트 카운터 생성
        Counter testCounter = Counter.builder("test.ec2.counter")
                .description("EC2에서 테스트 메트릭")
                .tag("environment", "production")
                .register(meterRegistry);

        testCounter.increment();
        log.info("테스트 메트릭 전송 완료: {}", testCounter.count());

        // MeterRegistry 상태 확인
        if (meterRegistry instanceof CloudWatchMeterRegistry) {
            log.info("✅ CloudWatch MeterRegistry가 정상적으로 주입됨");
        } else {
            log.warn("❌ CloudWatch MeterRegistry가 아님: {}", meterRegistry.getClass());
        }
    }
}

