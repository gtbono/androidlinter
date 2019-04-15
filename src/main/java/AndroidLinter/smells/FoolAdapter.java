package AndroidLinter.smells;

import AndroidLinter.FileManager;
import AndroidLinter.Parser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FoolAdapter implements SmellsInterface {

    public ArrayList<String> foundSmellMessage;
    private boolean smelly;
    private String pathApp;

    public FoolAdapter(String pathApp) {
        this.foundSmellMessage = new ArrayList<>();
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
                        if (statement.toString().contains("findViewById(") || statement.toString().contains("inflate(")) {
                            //Detecta se é um If, pois estamos procurando a seguinte condicional
                            // if (convertView == null)
                            if (statement.isIfStmt()) {
                                if (!statement.asIfStmt().getCondition().isBinaryExpr()) {
                                    break;
                                }
                                var ifStatement = statement.asIfStmt().getCondition().asBinaryExpr();
                                    if (!ifStatement.getLeft().isNameExpr()) {
                                        break;
                                    }
                                    var leftSide = ifStatement.getLeft().asNameExpr().getName().getIdentifier();
                                    var rightSide = ifStatement.getRight();
                                    if (leftSide.equals(nomeVariavelConvertView) && rightSide.isNullLiteralExpr()) {
                                        //Se estiver aqui, está liberado ter as chamadas de getViewById e inflate, pois só acontece uma vez, quando o convertView estiver nulo.
                                        continue;
                                    }
                            }
                            //Se em alguma dessas expressões tiver o texto findViewById
                            // Quer dizer que o ViewHolder não está sendo utilizado, o que caracteriza o smell
                            this.foundSmellMessage.add("Fool Adapter: Não foi detectado o uso do padrão ViewHolder na classe " + classe.getName().getIdentifier());
                            this.smelly = true;
                        }
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
            for (var message : foundSmellMessage) {
                System.out.println(message);
            }
        }
    }

}
