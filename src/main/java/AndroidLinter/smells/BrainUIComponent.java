package AndroidLinter.smells;

import AndroidLinter.FileManager;
import AndroidLinter.Parser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BrainUIComponent implements SmellsInterface {

    private boolean smelly;
    public ArrayList<String> foundSmellMessage;
    private String pathApp;
    private ArrayList<String> importsProibidosNaUi;

    public BrainUIComponent(String pathApp) {
        this.importsProibidosNaUi = new ArrayList<>();
        importsProibidosNaUi.add("java.io");
        importsProibidosNaUi.add("android.database");
        importsProibidosNaUi.add("androidx.sqlite.db");
        importsProibidosNaUi.add("android.support.v4.database");
        importsProibidosNaUi.add("android.support.v4.net");

        this.foundSmellMessage = new ArrayList<>();
        this.smelly = false;
        this.pathApp = pathApp;
    }

    private boolean isSmelly() {
        return this.smelly;
    }

    private void findSmell(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<FieldDeclaration>> fieldDeclarations,
                           LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ImportDeclaration>> classesEImports
    ) {

        for (var entry : fieldDeclarations.entrySet()) {
            var classe = entry.getKey();
            var fields = entry.getValue();

            for (var field : fields) {
                var modificadores = field.getModifiers();
                var variaveis = field.getVariables();
                for (var modificador : modificadores) {
                    if(modificador.name().equals("STATIC")) {
                        this.smelly = true;
                        //Tem que adicionar para cada variável pois pode ter mais de uma variável em uma declaração
                        for (var variavel : variaveis) {
                            this.foundSmellMessage.add("Brain UI Component: Encontrado modificador estático [" + variavel.getName().getIdentifier() + "] na classe " + classe.getName().getIdentifier());
                        }
                    }
                }
            }
        }

        for (var entry : classesEImports.entrySet()) {
            var classe = entry.getKey();
            var importsDaClasse = entry.getValue();

            for (var importDaClasse : importsDaClasse) {
                for (var importProibido : this.importsProibidosNaUi) {
                    if(importDaClasse.toString().contains(importProibido)) {
                        this.foundSmellMessage.add("Brain UI Component: Encontrado import com métodos de I/O [" + importProibido + "] na classe " + classe.getName().getIdentifier());
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

        var fieldDeclarations = parser.getUiComponentFieldDeclarations(arquivos);

        var classesEImports = parser.getImports(arquivos);

        findSmell(fieldDeclarations, classesEImports);

        if(isSmelly()) {
            for (var message : foundSmellMessage) {
                System.out.println(message);
            }
        }
    }

}
