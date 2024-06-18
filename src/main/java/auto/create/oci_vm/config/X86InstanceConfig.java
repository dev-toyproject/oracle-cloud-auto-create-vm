package auto.create.oci_vm.config;

import com.oracle.bmc.core.model.CreateVnicDetails;
import com.oracle.bmc.core.model.InstanceSourceViaImageDetails;
import com.oracle.bmc.core.model.LaunchInstanceDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class X86InstanceConfig extends InstanceConfig {

    private final OracleCloudConfig oracleConfig;

    @Value("${oracle.cloud.instance.x86.imageId}")
    private String imageId;

    @Value("${oracle.cloud.instance.x86.shape}")
    private String shape;

    @Value("${oracle.cloud.instance.x86.volume}")
    private String volumeSize;

    @Value("${oracle.cloud.instance.x86.availabilityDomain}")
    private String availabilityDomain;

    @Value("${oracle.cloud.instance.x86.compartmentId}")
    private String compartmentId;

    @Value("${oracle.cloud.instance.x86.subnetId}")
    private String subnetId;

    public LaunchInstanceDetails createX86InstanceDetails(String displayName) {
        CreateVnicDetails vnicDetails = createVnicDetails(subnetId);
        InstanceSourceViaImageDetails sourceViaImageDetails = createImageDetails(imageId, Long.valueOf(volumeSize));

        return LaunchInstanceDetails.builder()
                .isPvEncryptionInTransitEnabled(true)
                .compartmentId(compartmentId)
                .displayName(displayName)
                .availabilityDomain(availabilityDomain)
                .shape(shape)
                .sourceDetails(sourceViaImageDetails)
                .createVnicDetails(vnicDetails)
                .metadata(Map.of("ssh_authorized_keys", oracleConfig.getPublicKey()))
                .build();
    }
}
