package AndroidDetector;

public class AbscenceOfAnArchitecture implements SmellsInterface {
    @Override
    public void run() {
        System.out.println(this.getClass().toString());
    }
}
