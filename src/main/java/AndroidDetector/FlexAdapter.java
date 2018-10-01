package AndroidDetector;

public class FlexAdapter implements SmellsInterface {
    @Override
    public void run() {
        System.out.println(this.getClass().toString());
    }
}
