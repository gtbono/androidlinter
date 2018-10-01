package AndroidDetector;

public class FoolAdapter implements SmellsInterface {
    @Override
    public void run() {
        System.out.println(this.getClass().toString());
    }
}
