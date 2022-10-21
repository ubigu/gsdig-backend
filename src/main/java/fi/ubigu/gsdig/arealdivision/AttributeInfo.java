package fi.ubigu.gsdig.arealdivision;

public class AttributeInfo {

    private final String title;
    private final Class<?> binding;

    public AttributeInfo(String title, Class<?> binding) {
        this.title = title;
        this.binding = binding;
    }

    public String getTitle() {
        return title;
    }

    public Class<?> getBinding() {
        return binding;
    }

}
