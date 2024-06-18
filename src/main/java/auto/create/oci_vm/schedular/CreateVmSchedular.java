package auto.create.oci_vm.schedular;

import auto.create.oci_vm.config.ArmInstanceConfig;
import auto.create.oci_vm.config.OracleCloudConfig;
import auto.create.oci_vm.config.X86InstanceConfig;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.*;
import com.oracle.bmc.core.requests.LaunchInstanceRequest;
import com.oracle.bmc.core.responses.LaunchInstanceResponse;
import com.oracle.bmc.model.BmcException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateVmSchedular {

    private final OracleCloudConfig config;

    private final X86InstanceConfig x86InstanceConfig;

    private final ArmInstanceConfig armInstanceConfig;

    @Value("${oracle.cloud.instance.x86.displayName}")
    private String x86DisplayName;

    @Value("${oracle.cloud.instance.arm.displayName}")
    private String armDisplayName;

    private final List<LaunchInstanceDetails> srvList = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (x86DisplayName != null && !x86DisplayName.equals("null")) {
            Arrays.stream(x86DisplayName.split(","))
                    .forEach(displayName -> {
                        LaunchInstanceDetails x86InstanceDetails = x86InstanceConfig.createX86InstanceDetails(displayName);
                        srvList.add(x86InstanceDetails);
                    }
            );
        }

        if (armDisplayName != null && !armDisplayName.equals("null")) {
            Arrays.stream(armDisplayName.split(","))
                    .forEach(displayName -> {
                        LaunchInstanceDetails armInstanceDetails = armInstanceConfig.createArmInstanceDetails(displayName);
                        srvList.add(armInstanceDetails);
                    }
            );
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void cron() throws InterruptedException {
        ComputeClient computeClient = config.getComputeClient();

        if (srvList.isEmpty()) {
            log.info("서버 생성이 모두 완료되었습니다.");
            return;
        }
        createVM(srvList, computeClient);
    }


    private void createVM(List<LaunchInstanceDetails> srvList, ComputeClient computeClient) throws InterruptedException {

        List<LaunchInstanceDetails> tmpList = new ArrayList<>();

        for (LaunchInstanceDetails srv : srvList) {
            String displayName = srv.getDisplayName();

            LaunchInstanceRequest request = LaunchInstanceRequest.builder()
                    .launchInstanceDetails(srv)
                    .build();

            LaunchInstanceResponse response = null;

            try {
                Thread.sleep(7500);
                response = computeClient.launchInstance(request);

                Date timeCreated = response.getInstance().getTimeCreated();

                if (timeCreated != null) {
                    log.info("{} 서버가 생성되었습니다. 확인해주세요.", displayName);
                    tmpList.add(srv);
                }

            } catch (BmcException ex) {
                String message = ex.getMessage();

                if (message.contains("Out of host capacity")) {
                    log.error("{} 서버 생성에 실패하였습니다. (Out of host capacity)", displayName);
                }

                else {
                    log.error(message);
                }
            }
        }
        srvList.removeAll(tmpList);
    }
}
