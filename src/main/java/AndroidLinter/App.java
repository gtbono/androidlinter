package AndroidLinter;

import AndroidLinter.smells.*;

import java.util.HashSet;
import java.util.Set;

public class App {

    private String appDirectory;

    public App(String appDirectory) {
        this.appDirectory = appDirectory;
    }

    public void run () {

        SmellsIterator iterator = new SmellsIterator(getSmells());

        for(SmellsInterface smell : iterator) {
            smell.run();
        }

    }

    private Set<SmellsInterface> getSmells() {
        Set<SmellsInterface> smellsList = new HashSet<>();
        SmellsFactory smellsFactory = new SmellsFactory(this.appDirectory);
        smellsList.add(smellsFactory.buildFlexAdapter());
        smellsList.add(smellsFactory.buildFoolAdapter());
        smellsList.add(smellsFactory.buildBrainUIComponent());
        return smellsList;
    }
}
