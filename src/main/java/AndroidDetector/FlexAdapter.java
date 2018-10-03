package AndroidDetector;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FlexAdapter implements SmellsInterface {

    private final String pathApp;

    private boolean smelly;

    public FlexAdapter(String pathApp) {
        this.pathApp = pathApp;
        this.smelly = false;
    }

    private boolean isSmelly() {
        return smelly;
    }

    private File[] findFiles() {
        return new File(this.pathApp).listFiles();
    }

    private ArrayList<CompilationUnit> parseClasses(File[] files) {
        var compilationUnitList = new ArrayList<CompilationUnit>();
        for (var file : files) {
            try {
                compilationUnitList.add(JavaParser.parse(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return compilationUnitList;
    }

    private NodeList<TypeDeclaration<?>> getTypesList (ArrayList<CompilationUnit> compilationUnitList) {
        var types = new NodeList<TypeDeclaration<?>>();
        for (var compilationUnit : compilationUnitList) {
            types.addAll(compilationUnit.getTypes());
        }
        return types;
    }

    private NodeList<ClassOrInterfaceDeclaration> getClasses(NodeList<TypeDeclaration<?>> typesList) {
        var classes = new NodeList<ClassOrInterfaceDeclaration>();
        for (var type : typesList) {
            classes.add((ClassOrInterfaceDeclaration) type);
        }
        return classes;
    }

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ClassOrInterfaceType>> getExtendedTypes(NodeList<ClassOrInterfaceDeclaration> classes) {
        var classesESuperclasses = new LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ClassOrInterfaceType>>();
        for (var classe : classes) {
            classesESuperclasses.put(classe, classe.getExtendedTypes());
        }
        return classesESuperclasses;
    }

    //Retorna todas as classes que são Adapters, ou seja, que herdam de BaseAdapter
    private NodeList<ClassOrInterfaceDeclaration> getAdapters(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ClassOrInterfaceType>> classesESuperclasses) {
        var adapters = new NodeList<ClassOrInterfaceDeclaration>();
        for (var entry : classesESuperclasses.entrySet()) {
            var classe = entry.getKey();
            var implementacoes = entry.getValue();

            for (var implementacao : implementacoes) {
                if (implementacao.getName().getIdentifier().equals("BaseAdapter")) {
                    adapters.add(classe);
                }
            }
        }
        return adapters;
    }

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> getMembers(NodeList<ClassOrInterfaceDeclaration> classes) {
        var classeEMembros = new LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>>();
        for (var classe : classes) {
            classeEMembros.put(classe, classe.getMembers());
        }
        return classeEMembros;
    }

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> getMetodos (LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> membros) {
        var classeEMetodos = new LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>>();
        for (var entry : membros.entrySet()) {
            var classe = entry.getKey();
            var membrosDaClasse = entry.getValue();
            var metodosDaClasse = new NodeList<BodyDeclaration<?>>();

            for (var membro : membrosDaClasse) {
                if (membro.isMethodDeclaration()) {
                    metodosDaClasse.add(membro);
                }
            }
            classeEMetodos.put(classe, metodosDaClasse);
        }
        return classeEMetodos;
    }

        private void findSmell(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> metodos) {
        for (var entry : metodos.entrySet()) {
            var metodosDaClasse = entry.getValue();

            for (var metodo : metodosDaClasse) {
                var annotations = metodo.getAnnotations();
                if (annotations.size() == 0) {
                    this.smelly = true;
                }
                for (var annotation : annotations) {
                    if (!annotation.getName().getIdentifier().equals("Override")) {
                        this.smelly = true;
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        System.out.println(this.getClass().toString());

            var arquivos = findFiles();

            var compilationUnitList = parseClasses(arquivos);

            var typesList = getTypesList(compilationUnitList);

            var classes = getClasses(typesList);

            var classesESuperclasses = getExtendedTypes(classes);

            var adapters = getAdapters(classesESuperclasses);

            var membros = getMembers(adapters);

            var metodos = getMetodos(membros);

            findSmell(metodos);

            if(isSmelly()) {
                System.out.println("Flex Adapter detectado");
            }
    }

}
