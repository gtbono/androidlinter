package AndroidDetector.smells;

import AndroidDetector.FileManager;
import AndroidDetector.Parser;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BrainUIComponent implements SmellsInterface {

    private boolean smelly;
    public ArrayList<String> smellLocation;
    private String pathApp;
    private ArrayList<String> importsProibidosNaUi;

    public BrainUIComponent(String pathApp) {
        this.importsProibidosNaUi = new ArrayList<>();
        importsProibidosNaUi.add("java.io");
        importsProibidosNaUi.add("android.database");
        importsProibidosNaUi.add("androidx.sqlite.db");
        importsProibidosNaUi.add("android.support.v4.database");
        importsProibidosNaUi.add("android.support.v4.net");

        this.smellLocation = new ArrayList<>();
        this.smelly = false;
        this.pathApp = pathApp;
    }

    private boolean isSmelly() {
        return this.smelly;
    }

    private void findSmell(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<FieldDeclaration>> fieldDeclarations,
                           LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> uiComponentMetodos,
                           LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ImportDeclaration>> classesEImports
    ) {

        for (var entry : fieldDeclarations.entrySet()) {
            var classe = entry.getKey();
            var fields = entry.getValue();

            for (var field : fields) {
                var modificadores = field.getModifiers();
                for (var modificador : modificadores) {
                    if(modificador.name().equals("static")) {
                        this.smelly = true;
                        this.smellLocation.add(classe.getName().getIdentifier());
                    }
                }
            }
        }

        for (var entry : uiComponentMetodos.entrySet()) {
            var classe = entry.getKey();
            var metodosUi = entry.getValue();

            for (var metodo : metodosUi) {
                var body = (MethodDeclaration) metodo;
                var statements = body.getBody().get().getStatements();

                for (var statement : statements) {
                    if (statement.isExpressionStmt()) {
                        //Se tiver chamada ao método com jdbc não pode
                        if (statement.toString().contains("jdbc")) {
                            this.smellLocation.add(classe.getName().getIdentifier());
                            this.smelly = true;
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
                    //if (importDaClasse.getName().getQualifier().contains(importProibido)) {
                    if (importProibido.contains(importDaClasse.getName().getIdentifier())) {
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

        var fieldDeclarations = parser.getUiComponentFieldDeclarations(arquivos);

        var uiComponentMetodos = parser.getUiComponentMethods(arquivos);

        var classesEImports = parser.getImports(arquivos);

        findSmell(fieldDeclarations, uiComponentMetodos, classesEImports);

        if(isSmelly()) {
            System.out.println("Brain UI Component detectado nas classes:");
            for (var smellyClass : smellLocation) {
                System.out.println(smellyClass);
            }
        }
        System.out.println();
    }

}
