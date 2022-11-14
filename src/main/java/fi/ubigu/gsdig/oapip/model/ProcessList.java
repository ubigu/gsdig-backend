package fi.ubigu.gsdig.oapip.model;

import java.util.List;

import fi.ubigu.gsdig.oapi.model.Link;

public class ProcessList {

    private final List<ProcessSummary> processes;
    private final List<Link> links;

    public ProcessList(List<ProcessSummary> processes, List<Link> links) {
        this.processes = processes;
        this.links = links;
    }

    public List<ProcessSummary> getProcesses() {
        return processes;
    }

    public List<Link> getLinks() {
        return links;
    }

}
