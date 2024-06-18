package auto.create.oci_vm.config;

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OracleCloudConfig {

    @Getter
    private ComputeClient computeClient;

    @Getter
    private String publicKey;

    @Value("${oracle.cloud.apiKey.profile}")
    private String profile;

    @Value("${oracle.cloud.apiKey.user}")
    private String user;

    @Value("${oracle.cloud.apiKey.fingerprint}")
    private String fingerPrint;

    @Value("${oracle.cloud.apiKey.tenancy}")
    private String tenancy;

    @Value("${oracle.cloud.apiKey.region}")
    private String region;

    @Value("${oracle.cloud.apiKey.privKey.path}")
    private String privKeyPath;

    @Value("${oracle.cloud.apiKey.pubKey.path}")
    private String pubKeyPath;

    @PostConstruct
    public void init() {
        this.computeClient = initComputeClient();
        this.publicKey = initPublicKey();
    }

    private String initPublicKey() {
        try {
            return Files.readString(new File(pubKeyPath).toPath());
        } catch (IOException ex) {
            log.error("경로가 잘못되었습니다.");
            throw new RuntimeException(ex);
        }
    }

    private ComputeClient initComputeClient() {

        String configContext = "[" + profile + "]\n" +
                               "user=" + user + "\n" +
                               "fingerprint=" + fingerPrint + "\n" +
                               "tenancy=" + tenancy + "\n" +
                               "region=" + region + "\n" +
                               "key_file=" + privKeyPath;

        FileWriter writer = null;

        try {
            // 임시 파일 생성
            File tmpFile = File.createTempFile("oci-config", ".tmp");

            // 임시 파일에 설정 Write
            writer = new FileWriter(tmpFile);
            writer.write(configContext);
            writer.close();

            // ComputeClient 생성
            ConfigFileAuthenticationDetailsProvider provider =
                    new ConfigFileAuthenticationDetailsProvider(tmpFile.getAbsolutePath(), profile);

            ComputeClient client = ComputeClient.builder()
                    .build(provider);

            // 임시 파일 삭제
            tmpFile.delete();

            // ComputeClient 리턴
            return client;

        } catch (IOException ex) {
            log.error("설정 파일 생성 중 에러가 발생하였습니다.");
            throw new RuntimeException(ex);
        }
    }
}
