package AndroidLinter.smells;

public class SmellsFactory {

    private String pathApp;

    public SmellsFactory(String pathApp) {
        this.pathApp = pathApp;
    }

    public BrainUIComponent buildBrainUIComponent() {
        return new BrainUIComponent(pathApp);
    }

    public FlexAdapter buildFlexAdapter() {
        return new FlexAdapter(pathApp);
    }

    public FoolAdapter buildFoolAdapter() {
        return new FoolAdapter(pathApp);
    }

}
