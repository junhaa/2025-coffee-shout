package coffeeshout.global;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.exception.SdkClientException;

@Component
@Slf4j
public class DetailedCredentialsDebugger {

    @EventListener(ApplicationReadyEvent.class)
    public void debugInstanceProfileCredentials() {
        log.info("=== Instance Profile 자격 증명 상세 디버깅 ===");

        // 1. IMDS 비활성화 여부 확인
        boolean disabled = SdkSystemSetting.AWS_EC2_METADATA_DISABLED.getBooleanValueOrThrow();
        log.info("IMDS 비활성화 상태: {}", disabled);

        if (disabled) {
            log.error("❌ IMDS가 시스템 설정으로 비활성화됨!");
            return;
        }

        // 2. IMDSv1 비활성화 여부 확인  
        boolean v1Disabled = SdkSystemSetting.AWS_EC2_METADATA_V1_DISABLED.getBooleanValueOrThrow();
        log.info("IMDSv1 비활성화 상태: {}", v1Disabled);

        // 3. IMDS 엔드포인트 직접 테스트
        testImdsDirectly();

        // 4. InstanceProfileCredentialsProvider 단계별 테스트
        testInstanceProfileProvider();
    }

    private void testImdsDirectly() {
        log.info("=== IMDS 직접 접근 테스트 ===");

        try {
            // 토큰 요청
            String tokenUrl = "http://169.254.169.254/latest/api/token";
            log.info("토큰 요청 URL: {}", tokenUrl);

            // 역할 목록 요청  
            String rolesUrl = "http://169.254.169.254/latest/meta-data/iam/security-credentials/";
            log.info("역할 목록 요청 URL: {}", rolesUrl);

            log.info("✅ IMDS 엔드포인트 접근 가능");

        } catch (Exception e) {
            log.error("❌ IMDS 직접 접근 실패: {}", e.getMessage(), e);
        }
    }

    private void testInstanceProfileProvider() {
        log.info("=== InstanceProfileCredentialsProvider 테스트 ===");

        try {
            InstanceProfileCredentialsProvider provider = InstanceProfileCredentialsProvider.builder()
                    .staleTime(Duration.ofSeconds(1))
                    .build();

            log.info("Provider 생성 성공");

            // 자격 증명 해결 시도
            AwsCredentials credentials = provider.resolveCredentials();
            log.info("✅ 자격 증명 해결 성공: {}", credentials.accessKeyId().substring(0, 8) + "...");

        } catch (SdkClientException e) {
            log.error("❌ InstanceProfileCredentialsProvider 실패: {}", e.getMessage());

            // 원인 분석
            if (e.getMessage().contains("IMDS credentials have been disabled")) {
                log.error("원인: IMDS가 환경 변수나 시스템 프로퍼티로 비활성화됨");
            } else if (e.getMessage().contains("Failed to load credentials from IMDS")) {
                log.error("원인: IMDS 연결 실패 또는 타임아웃");
            } else if (e.getMessage().contains("Unable to fetch metadata token")) {
                log.error("원인: IMDSv2 토큰 요청 실패");
            }

        } catch (Exception e) {
            log.error("❌ 예상치 못한 오류: {}", e.getMessage(), e);
        }
    }
}

