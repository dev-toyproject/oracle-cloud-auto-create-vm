package auto.create.oci_vm.config;

import com.oracle.bmc.core.model.CreateVnicDetails;
import com.oracle.bmc.core.model.InstanceSourceViaImageDetails;
import com.oracle.bmc.core.model.LaunchInstanceDetails;
import com.oracle.bmc.core.model.LaunchInstanceShapeConfigDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ArmInstanceConfig extends InstanceConfig {

    private final OracleCloudConfig oracleConfig;

    @Value("${oracle.cloud.instance.arm.imageId}")
    private String imageId;

    @Value("${oracle.cloud.instance.arm.shape}")
    private String shape;

    @Value("${oracle.cloud.instance.arm.vcpu}")
    private String vcpu;

    @Value("${oracle.cloud.instance.arm.memory}")
    private String memory;

    @Value("${oracle.cloud.instance.arm.volume}")
    private String volumeSize;

    @Value("${oracle.cloud.instance.arm.availabilityDomain}")
    private String availabilityDomain;

    @Value("${oracle.cloud.instance.arm.compartmentId}")
    private String compartmentId;

    @Value("${oracle.cloud.instance.arm.subnetId}")
    private String subnetId;

    public LaunchInstanceDetails createArmInstanceDetails(String displayName) {
        CreateVnicDetails vnicDetails = createVnicDetails(subnetId);
        InstanceSourceViaImageDetails sourceViaImageDetails = createImageDetails(imageId, Long.valueOf(volumeSize));

        LaunchInstanceShapeConfigDetails shapeConfig = LaunchInstanceShapeConfigDetails.builder()
                .ocpus(Float.valueOf(vcpu))
                .memoryInGBs(Float.valueOf(memory))
                .build();

        return LaunchInstanceDetails.builder()
                .isPvEncryptionInTransitEnabled(true)
                .compartmentId(compartmentId)
                .displayName(displayName)
                .availabilityDomain(availabilityDomain)
                .shape(shape)
                .shapeConfig(shapeConfig)
                .sourceDetails(sourceViaImageDetails)
                .createVnicDetails(vnicDetails)
                .metadata(Map.of("ssh_authorized_keys", oracleConfig.getPublicKey()))
                .build();
    }
}
