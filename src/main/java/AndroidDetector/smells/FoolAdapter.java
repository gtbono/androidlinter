package AndroidDetector.smells;

import AndroidDetector.FileManager;
import AndroidDetector.Parser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FoolAdapter implements SmellsInterface {

    public ArrayList<String> smellLocation;
    private boolean smelly;
    private String pathApp;

    public FoolAdapter(String pathApp) {
        this.smellLocation = new ArrayList<>();
        this.smelly = false;
        this.pathApp = pathApp;
    }

    private boolean isSmelly() {
        return smelly;
    }

    private void findSmell(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> metodos) {
        for (var entry : metodos.entrySet()) {
            var classe = entry.getKey();
            var metodosDaClasse = entry.getValue();

            for (var metodoDaClasse : metodosDaClasse) {
                var metodo = (MethodDeclaration) metodoDaClasse;
                if (metodo.getName().getIdentifier().equals("getView")) {
                    var body = metodo.getBody().orElse(null);
                    var statements = body.getStatements();
                    for (var statement : statements) {
                        if (statement.toString().contains("findViewById")) {
                            //Se em alguma dessas expressões tiver o texto findViewById
                            // Quer dizer que o ViewHolder não está sendo utilizado, o que caracteriza o smell
                            this.smellLocation.add(classe.getName().getIdentifier());
                            this.smelly = true;
                        }

                        //Se ele infla um Layout em toda chamada ao getView, isso também caracteriza o smell
                        if(statement.toString().contains("getLayoutInflater(")) {
                            this.smellLocation.add(classe.getName().getIdentifier());
                            this.smelly = true;
                        }
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
            System.out.println("Fool Adapter detectado nas classes: ");
            for (var smellyClass : smellLocation) {
                System.out.println(smellyClass);
            }
        }
        System.out.println();
    }

}
