package AndroidLinter;

import AndroidLinter.smells.SmellsInterface;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SmellsIterator implements Iterable<SmellsInterface> {

    private Set<SmellsInterface> smellsList;

    public SmellsIterator (Set<SmellsInterface> smellsList) {
        this.smellsList = smellsList;
    }

    @Override
    public Iterator<SmellsInterface> iterator() {
        return this.smellsList.iterator();
    }

    @Override
    public void forEach(Consumer<? super SmellsInterface> action) {

    }

    @Override
    public Spliterator<SmellsInterface> spliterator() {
        return null;
    }
}
