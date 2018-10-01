package AndroidDetector;

import java.util.HashSet;
import java.util.Set;

public class App {
    
    public void run () {

        SmellsIterator iterator = new SmellsIterator(getSmells());

        for(SmellsInterface smell : iterator) {
            smell.run();
        }

    }

    private Set<SmellsInterface> getSmells() {
        Set<SmellsInterface> smellsList = new HashSet<>();
        smellsList.add(new FlexAdapter());
        smellsList.add(new FoolAdapter());
        smellsList.add(new BrainUIComponent());
        smellsList.add(new ObscureNames());
        smellsList.add(new AbscenceOfAnArchitecture());
        return smellsList;
    }
}
