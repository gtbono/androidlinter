package AndroidDetector.smells;

import AndroidDetector.FileManager;
import AndroidDetector.Parser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FlexAdapter implements SmellsInterface {

    public ArrayList<String> smellLocation;
    private boolean smelly;
    private String pathApp;

    public FlexAdapter(String pathApp) {
        this.smellLocation = new ArrayList<>();
        this.smelly = false;
        this.pathApp = pathApp;
    }

    private boolean isSmelly() {
        return this.smelly;
    }

    private void findSmell(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> metodos) {
        for (var entry : metodos.entrySet()) {
            var classe = entry.getKey();
            var metodosDaClasse = entry.getValue();

            for (var metodo : metodosDaClasse) {
                var annotations = metodo.getAnnotations();
                if (annotations.size() == 0) {
                    this.smellLocation.add(classe.getName().getIdentifier());
                    this.smelly = true;
                }
                for (var annotation : annotations) {
                    if (!annotation.getName().getIdentifier().equals("Override")) {
                        this.smellLocation.add(classe.getName().getIdentifier());
                        this.smelly = true;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        System.out.println(this.getClass().toString());

            var parser = new Parser();

            var fileManager = new FileManager(pathApp);

            var arquivos = fileManager.findJavaFiles();

            var metodos = parser.getAdapterMethods(arquivos);

            findSmell(metodos);

            if(isSmelly()) {
                System.out.println("Flex Adapter detectado nas classes:");
                for (var smellyClass : this.smellLocation) {
                    System.out.println(smellyClass);
                }
                System.out.println();
            }
    }

}
