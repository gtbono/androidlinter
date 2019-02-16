package AndroidLinter.smells;

public class SmellsFactory {

    private String pathApp;

    public SmellsFactory(String pathApp) {
        this.pathApp = pathApp;
    }

    public SmellsInterface buildBrainUIComponent() {
        return new BrainUIComponent(pathApp);
    }

    public SmellsInterface buildFlexAdapter() {
        return new FlexAdapter(pathApp);
    }

    public SmellsInterface buildFoolAdapter() {
        return new FoolAdapter(pathApp);
    }

}
