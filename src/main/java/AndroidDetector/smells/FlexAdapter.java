package AndroidDetector.smells;

import AndroidDetector.FileManager;
import AndroidDetector.Parser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FlexAdapter implements SmellsInterface {

    public ArrayList<String> foundSmellMessage;
    private boolean smelly;
    private String pathApp;

    public FlexAdapter(String pathApp) {
        this.foundSmellMessage = new ArrayList<>();
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
                    this.foundSmellMessage.add("Flex Adapter: Encontrado método com lógica de negócio [" + metodo.asMethodDeclaration().getNameAsString() + "] na classe " + classe.getName().getIdentifier());
                    this.smelly = true;
                }
                for (var annotation : annotations) {
                    if (!annotation.getName().getIdentifier().equals("Override")) {
                        this.foundSmellMessage.add("Flex Adapter: Encontrado método com lógica de negócio [" + metodo.asMethodDeclaration().getNameAsString() + "] na classe " + classe.getName().getIdentifier()
                        );
                        this.smelly = true;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
            var parser = new Parser();

            var fileManager = new FileManager(pathApp);

            var arquivos = fileManager.findJavaFiles();

            var metodos = parser.getAdapterMethods(arquivos);

            findSmell(metodos);

            if(isSmelly()) {
                for (var message : this.foundSmellMessage) {
                    System.out.println(message);
                }
            }
    }

}
