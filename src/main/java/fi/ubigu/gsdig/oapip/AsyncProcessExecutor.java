package fi.ubigu.gsdig.oapip;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import fi.ubigu.gsdig.oapip.model.StatusInfo;

public interface AsyncProcessExecutor {

    public StatusInfo execute(ExecutableProcess process, Map<String, Object> inputs, Principal principal);
    public Optional<StatusInfo> getStatus(UUID jobId);
    public Optional<ProcessResponse> getResponse(UUID jobId);

}
