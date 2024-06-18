package auto.create.oci_vm.config;

import com.oracle.bmc.core.model.CreateVnicDetails;
import com.oracle.bmc.core.model.InstanceSourceViaImageDetails;

public abstract class InstanceConfig {

    protected CreateVnicDetails createVnicDetails(String subnetId) {
        return CreateVnicDetails.builder()
                .subnetId(subnetId)
                .assignPublicIp(true)
                .assignPrivateDnsRecord(true)
                .assignIpv6Ip(false)
                .build();
    }

    protected InstanceSourceViaImageDetails createImageDetails(String imageId, Long volumeSize) {
        return InstanceSourceViaImageDetails.builder()
                .imageId(imageId)
                .bootVolumeSizeInGBs(volumeSize)
                .bootVolumeVpusPerGB(10L)
                .build();
    }
}
