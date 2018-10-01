package AndroidDetector;

public class BrainUIComponent implements SmellsInterface {
    @Override
    public void run() {
        System.out.println(this.getClass().toString());
    }
}
