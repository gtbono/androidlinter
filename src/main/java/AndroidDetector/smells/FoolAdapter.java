package AndroidDetector.smells;

import AndroidDetector.FileManager;
import AndroidDetector.Parser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;

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
                    var convertView = metodo.getParameterByType("View");
                    var nomeVariavelConvertView = convertView.get().getName().getIdentifier();
                    var body = metodo.getBody().orElse(null);
                    var statements = body.getStatements();
                    for (var statement : statements) {
                        if (statement.toString().contains("findViewById(")) {
                            //Detecta se é um If, pois estamos procurando a seguinte condicional
                            // if (convertView == null)
                            if (statement.isIfStmt()) {
                                var ifStatement = statement.asIfStmt().getCondition().asBinaryExpr();
                                    var leftSide = ifStatement.getLeft().asNameExpr().getName().getIdentifier();
                                    var rightSide = ifStatement.getRight();
                                    if (leftSide.equals(nomeVariavelConvertView) && rightSide.isNullLiteralExpr()) {
                                        //Se estiver aqui, está liberado ter as chamadas de getViewById e inflate, pois só acontece uma vez, quando o convertView estiver nulo.
                                        continue;
                                    }
                            }
                            //Se em alguma dessas expressões tiver o texto findViewById
                            // Quer dizer que o ViewHolder não está sendo utilizado, o que caracteriza o smell
                            this.smellLocation.add(classe.getName().getIdentifier());
                            this.smelly = true;
                        }

                        //Se ele infla um Layout em toda chamada ao getView, isso também caracteriza o smell
                        if(statement.toString().contains("inflate(")) {
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
