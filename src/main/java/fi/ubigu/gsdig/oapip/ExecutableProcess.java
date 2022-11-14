package fi.ubigu.gsdig.oapip;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import fi.ubigu.gsdig.oapip.model.InputDescription;
import fi.ubigu.gsdig.oapip.model.OutputDescription;
import fi.ubigu.gsdig.oapip.model.StatusInfo;

public interface ExecutableProcess {

    public String getId();
    public String getVersion();
    public String getTitle();
    public String getDescription();
    public List<String> getKeywords();

    public Map<String, InputDescription> getInputs();
    public Map<String, OutputDescription> getOutputs();

    public StatusInfo execute(Map<String, Object> inputs, Principal principal);

}
