package fi.ubigu.gsdig.oapip;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fi.ubigu.gsdig.oapi.model.ConformanceClasses;
import fi.ubigu.gsdig.oapi.model.Link;
import fi.ubigu.gsdig.oapif.model.LandingPage;
import fi.ubigu.gsdig.oapip.model.Execute;
import fi.ubigu.gsdig.oapip.model.InlineOrRefData;
import fi.ubigu.gsdig.oapip.model.InputDescription;
import fi.ubigu.gsdig.oapip.model.JobControlOptions;
import fi.ubigu.gsdig.oapip.model.Metadata;
import fi.ubigu.gsdig.oapip.model.OutputDescription;
import fi.ubigu.gsdig.oapip.model.Process;
import fi.ubigu.gsdig.oapip.model.ProcessList;
import fi.ubigu.gsdig.oapip.model.ProcessSummary;
import fi.ubigu.gsdig.oapip.model.ProcessesConformanceClass;
import fi.ubigu.gsdig.oapip.model.ProcessesException;
import fi.ubigu.gsdig.oapip.model.StatusInfo;
import fi.ubigu.gsdig.oapip.model.TransmissionMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;

@RestController
@RequestMapping("/processes")
public class ProcessesController {

    @Value("${endpoint}")
    private String endpoint;

    @Value("${endpoint}/processes")
    private String root;

    @Autowired
    private AsyncProcessExecutor executor;

    @Autowired
    private Map<String, ExecutableProcess> processes;

    @Bean
    public GroupedOpenApi processesOpenApi() {
        String paths[] = { "/processes/**" };
        String packagesToScan[] = { "fi.ubigu.gsdig.oapip" };
        return GroupedOpenApi.builder()
                .group("processes")
                .pathsToMatch(paths)
                .packagesToScan(packagesToScan)
                .addOpenApiCustomiser(fixServerURLAndPaths())
                .build();
    }

    private OpenApiCustomiser fixServerURLAndPaths() {
        return openApi -> {
            openApi.setServers(Arrays.asList(new Server().url(root)));
            Paths paths = openApi.getPaths();
            List<String> keys = new ArrayList<>(paths.keySet());
            for (String key : keys) {
                if (key.startsWith("/processes")) {
                    String newPath = key.substring("/processes".length());
                    if (newPath.isEmpty()) {
                        newPath = "/";
                    }
                    paths.put(newPath, paths.remove(key));
                }
            }
        };
    }

    @GetMapping
    @Operation(tags = "Capabilities", summary = "landing page")
    public LandingPage getLandingPage() {
        Link self = new Link("self", "application/json", "This document", root);
        Link service_desc = new Link("service-desc", "application/vnd.oai.openapi+json;version=3.0", "Definition of the API in OpenAPI 3.0", endpoint + "/api");
        Link conformance = new Link("http://www.opengis.net/def/rel/ogc/1.0/conformance", "application/json", "OGC API - Processes conformance classes implemented by this server", root + "/conformance");
        Link data = new Link("http://www.opengis.net/def/rel/ogc/1.0/processes", "application/json", "Metadata about the processes", root + "/processes");

        LandingPage landingPage = new LandingPage();
        landingPage.setTitle("GSDIG Processes");
        landingPage.setDescription("GSDIG Processes OGC API Processes service");
        landingPage.setLinks(Arrays.asList(self, service_desc, conformance, data));
        return landingPage;
    }

    @GetMapping("/conformance")
    @Operation(tags = "Capabilities", summary = "conformance declaration")
    public ConformanceClasses getConformance() {
        List<String> conformanceURIs = Arrays.stream(ProcessesConformanceClass.values())
                .map(it -> it.url)
                .collect(Collectors.toList());

        ConformanceClasses classes = new ConformanceClasses();
        classes.setConformsTo(conformanceURIs);
        return classes;
    }

    @GetMapping("/processes")
    @Operation(tags = "Metadata about the processes", summary = "Metadata about the processes")
    public ProcessList getProcesses(Principal principal) throws Exception {
        List<ProcessSummary> summaries = processes.values().stream()
                .map(this::toProcessSummary)
                .collect(Collectors.toList());

        List<Link> links = List.of(new Link("self", "application/json", "This document", root + "/processes"));

        return new ProcessList(summaries, links);
    }

    private ProcessSummary toProcessSummary(ExecutableProcess process) {
        String id = process.getId();
        String version = process.getVersion();
        String title = process.getTitle();
        String description = process.getDescription();
        List<String> keywords = process.getKeywords();
        List<Metadata> metadata = List.of();
        List<JobControlOptions> jobControlOptions = List.of(JobControlOptions.async);
        List<TransmissionMode> outputTransmission = List.of(TransmissionMode.value);
        List<Link> links = List.of();
        return new ProcessSummary(id, version, title, description, keywords, metadata, jobControlOptions, outputTransmission, links);
    }

    @GetMapping("/processes/{processId}")
    @Operation(tags = "Metadata about the processes", summary = "Retrieve a process description")
    public Process getProcessById(
            @PathVariable String processId,
            Principal principal) throws Exception {
        ExecutableProcess process = processes.get(processId);
        if (process == null) {
            throw ProcessesException.noSuchProcess();
        }
        return toProcess(process);
    }

    private Process toProcess(ExecutableProcess process) {
        String id = process.getId();
        String version = process.getVersion();
        String title = process.getTitle();
        String description = process.getDescription();
        List<String> keywords = process.getKeywords();
        List<Metadata> metadata = List.of();
        List<JobControlOptions> jobControlOptions = List.of(JobControlOptions.async);
        List<TransmissionMode> outputTransmission = List.of(TransmissionMode.value);
        List<Link> links = List.of();
        Map<String, InputDescription> inputs = process.getInputs();
        Map<String, OutputDescription> outputs = process.getOutputs();
        return new Process(id, version, title, description, keywords, metadata, jobControlOptions, outputTransmission, links, inputs, outputs);
    }

    @PostMapping(value = "/processes/{processId}/execution")
    @Operation(tags = "Execute process")
    @ResponseStatus(code = HttpStatus.CREATED)
    public StatusInfo execute(
            @PathVariable String processId,
            @RequestBody Execute execute,
            Principal principal) throws Exception {
        ExecutableProcess process = processes.get(processId);
        if (process == null) {
            throw ProcessesException.noSuchProcess();
        }

        Map<String, Object> inlineInputs = validateInputs(execute.getInputs(), process.getInputs());

        StatusInfo info = process.execute(inlineInputs, principal);

        List<Link> links = List.of(new Link("self", "application/geo+json", "This document", ""));

        return info.withLinks(links);
    }

    private Map<String, Object> validateInputs(Map<String, InlineOrRefData> executeInputs, Map<String, InputDescription> processInputs) {
        checkMissingRequiredInputs(executeInputs, processInputs);

        Map<String, Object> inlineInputs = new HashMap<>();
        executeInputs.forEach((key, executeInput) -> {
            InputDescription processInput = processInputs.get(key);
            if (processInput == null) {
                throw new ProcessesException("Unknown input", "Unknown input", 400, null, null);
            }
            Object inlineInput = resolveInput(executeInput);
            validateInput(inlineInput, processInput);
            inlineInputs.put(key, inlineInput);
        });
        return inlineInputs;
    }

    private void checkMissingRequiredInputs(Map<String, InlineOrRefData> executeInputs, Map<String, InputDescription> processInputs) {
        processInputs.forEach((key, processInput) -> {
            Integer minOccurs = processInput.getMinOccurs();
            if (minOccurs != null && minOccurs > 0) {
                if (!executeInputs.containsKey(key)) {
                    throw new ProcessesException("Missing required input", "Unknown input", 400, null, null);
                }
            }
        });
    }

    private Object resolveInput(InlineOrRefData executeInput) {
        if (executeInput instanceof Link) {
            // TODO: Fetch from Link
            throw new IllegalArgumentException("Inputs not inline not yet supported!");
        }
        return executeInput;
    }

    private void validateInput(Object inlineInput, InputDescription processInput) {

    }

    @GetMapping(value = "/jobs/{jobId}")
    @Operation(tags = "Get job status info")
    public StatusInfo getJobStatusInfo(@PathVariable UUID jobId) throws Exception {
        return executor.getStatus(jobId)
                .orElseThrow(() -> ProcessesException.noSuchJob());
    }

    @GetMapping(value = "/jobs/{jobId}/results")
    @Operation(tags = "Get job results")
    public ResponseEntity<?> getJobResults(@PathVariable UUID jobId) throws Exception {
        return executor.getResponse(jobId)
                .map(this::toResponse)
                .orElseThrow(() -> ProcessesException.noSuchJob());
    }

    private ResponseEntity<?> toResponse(ProcessResponse resp) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, resp.getMimeType());
        return new ResponseEntity<Map<String, Object>>(resp.getOutputs(), responseHeaders, HttpStatus.OK); 
    }

}
