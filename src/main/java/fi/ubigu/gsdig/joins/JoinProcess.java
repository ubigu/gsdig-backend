package fi.ubigu.gsdig.joins;

import java.security.Principal;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.ubigu.gsdig.oapi.model.Link;
import fi.ubigu.gsdig.oapip.AsyncProcessExecutor;
import fi.ubigu.gsdig.oapip.ExecutableProcess;
import fi.ubigu.gsdig.oapip.ProcessResponse;
import fi.ubigu.gsdig.oapip.model.InputDescription;
import fi.ubigu.gsdig.oapip.model.OutputDescription;
import fi.ubigu.gsdig.oapip.model.ProcessesException;
import fi.ubigu.gsdig.oapip.model.StatusInfo;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

@Component("join")
public class JoinProcess implements ExecutableProcess, AsyncProcessExecutor {

    @Autowired
    private JoinService joinService;

    @Autowired
    private ObjectMapper om;

    @Override
    public String getId() {
        return "join";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getTitle() {
        return "Join areal division with unit dataset";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<String> getKeywords() {
        return List.of("join");
    }

    @Override
    public Map<String, InputDescription> getInputs() {
        StringSchema aggregateFunctionSchema = new StringSchema()
                ._enum(Arrays.stream(AggregateFunction.values())
                .map(AggregateFunction::name)
                .toList());
        
        ObjectSchema joinAttributeSchema = new ObjectSchema();
        joinAttributeSchema.addProperty("property", new StringSchema());
        joinAttributeSchema.addProperty("aggregate", new ArraySchema().items(aggregateFunctionSchema));
        
        Map<String, InputDescription> inputs = new LinkedHashMap<>();
        inputs.put("title",
                new InputDescription("Title for new areal division",
                        null, null, null, 1, 1, new StringSchema()));
        inputs.put("description",
                new InputDescription("Description for new areal division",
                        null, null, null, 0, 1, new StringSchema()));
        inputs.put("areaAttributes",
                new InputDescription("Attributes from areal division to select into areal division",
                        null, null, null, 0, 1, new ArraySchema().items(new StringSchema())));
        inputs.put("unitDataset",
                new InputDescription("id of unitdataset",
                        null, null, null, 1, 1, new UUIDSchema()));
        inputs.put("dataAttributes",
                new InputDescription("Attributes from unit dataset to select into areal division",
                        null, null, null, 1, 1, new ArraySchema().items(joinAttributeSchema)));
        inputs.put("additionalGroupingProperty",
                new InputDescription("Property to use for additional grouping (in addition to grouping by areal division)",
                        null, null, null, 0, 1, new StringSchema()));
        
        return inputs;
    }

    @Override
    public Map<String, OutputDescription> getOutputs() {
        return Map.of("uuid",
                new OutputDescription("uuid of the generated arealdivision",
                        null, List.of(), List.of(), new UUIDSchema())); 
    }

    @Override
    public StatusInfo execute(Map<String, Object> inputs, Principal principal) {
        JoinRequest request;
        try {
            request = createRequest(inputs);
        } catch (Exception e) {
            throw new ProcessesException("Invalid inputs", null, 400, null, null);
        }

        try {
            JoinJob job = joinService.create(request, principal);
            return jobToStatusInfo(job);
        } catch (Exception e) {
            throw new ProcessesException("Unexpected error", null, 500, null, null);
        }
    }

    private JoinRequest createRequest(Map<String, Object> inputs) {
        return om.convertValue(inputs, JoinRequest.class);
    }

    private StatusInfo jobToStatusInfo(JoinJob job) {
        String processID = getId();
        String jobID = job.getUuid().toString();
        StatusCode status = job.getStatus();
        String message = job.getMessage();
        Instant created = job.getCreated();
        Instant started = job.getStarted();
        Instant finished = job.getFinished();
        Instant updated = job.getUpdated();
        Integer progress = null;
        List<Link> links = null;
        return new StatusInfo(processID, jobID, status, message, created, started, finished, updated, progress, links);
    }

    @Override
    public Optional<StatusInfo> getStatus(UUID jobId) {
        try {
            return joinService.findByUuid(jobId).map(this::jobToStatusInfo);
        } catch (Exception e) {
            throw new ProcessesException("Unexpected error", null, 500, null, null);
        }
    }

    @Override
    public Optional<ProcessResponse> getResponse(UUID jobId) {
        try {
            return joinService.findByUuid(jobId)
                .map(it -> it.getStatus())
                .filter(status -> status == StatusCode.succesful)
                .map(__ -> new ProcessResponse("application/json", Map.of("uuid", jobId), StatusCode.succesful));
        } catch (Exception e) {
            throw new ProcessesException("Unexpected error", null, 500, null, null);
        }
    }

    @Override
    public StatusInfo execute(ExecutableProcess process, Map<String, Object> inputs, Principal principal) {
        return process.execute(inputs, principal);
    }

}
