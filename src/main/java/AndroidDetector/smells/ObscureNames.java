package AndroidDetector.smells;

public class ObscureNames implements SmellsInterface {
    @Override
    public void run() {
        System.out.println(this.getClass().toString());
    }
}
