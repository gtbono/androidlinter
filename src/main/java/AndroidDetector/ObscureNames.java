package AndroidDetector;

public class ObscureNames implements SmellsInterface {
    @Override
    public void run() {
        System.out.println(this.getClass().toString());
    }
}
