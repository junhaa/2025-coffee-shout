package coffeeshout.global.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Configuration
@EnableConfigurationProperties
public class ManualCloudWatchConfig {

    private static final Logger log = LoggerFactory.getLogger(ManualCloudWatchConfig.class);
    private final ApplicationContext applicationContext;

    public ManualCloudWatchConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @Primary
    public CloudWatchMeterRegistry cloudWatchMeterRegistry() {
        log.info("=== 수동으로 CloudWatch MeterRegistry 생성 시작 ===");

        try {
            // CloudWatch 설정
            CloudWatchConfig config = new CloudWatchConfig() {
                @Override
                public String get(String key) {
                    // 기본값 사용
                    return null;
                }

                @Override
                public String namespace() {
                    return "coffee-shout-dev";
                }

                @Override
                public Duration step() {
                    return Duration.ofMinutes(1);
                }

                @Override
                public int batchSize() {
                    return 20;
                }

                @Override
                public boolean enabled() {
                    return true;
                }
            };

            // AWS CloudWatch 클라이언트 생성
            CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            log.info("CloudWatch 클라이언트 생성 완료");

            // MeterRegistry 생성
            CloudWatchMeterRegistry registry = new CloudWatchMeterRegistry(
                    config,
                    Clock.SYSTEM,
                    cloudWatchClient
            );

            log.info("✅ CloudWatch MeterRegistry 생성 완료!");
            log.info("   - Namespace: {}", config.namespace());
            log.info("   - Step: {}", config.step());
            log.info("   - BatchSize: {}", config.batchSize());

            return registry;

        } catch (Exception e) {
            log.error("❌ CloudWatch MeterRegistry 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("CloudWatch MeterRegistry 생성 실패", e);
        }
    }

    // 시작 시 상태 확인
    @EventListener(ApplicationReadyEvent.class)
    public void checkCloudWatchSetup(ApplicationReadyEvent event) {
        MeterRegistry meterRegistry = applicationContext.getBean(MeterRegistry.class);
        log.info("=== CloudWatch 설정 확인 ===");
        log.info("주입된 MeterRegistry: {}", meterRegistry.getClass().getName());

        // 테스트 메트릭 생성
        Counter testCounter = Counter.builder("startup.test")
                .description("애플리케이션 시작 테스트")
                .tag("source", "manual-config")
                .register(meterRegistry);

        testCounter.increment();
        log.info("테스트 메트릭 생성 완료: {}", testCounter.count());

        // AWS 자격 증명 확인
        try {
            DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
            AwsCredentials credentials = credentialsProvider.resolveCredentials();
            log.info(
                    "✅ AWS 자격 증명 확인됨 (AccessKey: {}...)",
                    credentials.accessKeyId().substring(0, Math.min(8, credentials.accessKeyId().length()))
            );
        } catch (Exception e) {
            log.error("❌ AWS 자격 증명 확인 실패: {}", e.getMessage());
        }
    }
}

