package AndroidDetector;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Parser {

    private ArrayList<CompilationUnit> parseFiles(File[] files) {
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

    private NodeList<TypeDeclaration<?>> parseTypesList(ArrayList<CompilationUnit> compilationUnitList) {
        var types = new NodeList<TypeDeclaration<?>>();
        for (var compilationUnit : compilationUnitList) {
            types.addAll(compilationUnit.getTypes());
        }
        return types;
    }

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ImportDeclaration>> parseImports(ArrayList<CompilationUnit> compilationUnitList) {
        //var types = new NodeList<ClassOrInterfaceDeclaration>();
        var classesEImports = new LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ImportDeclaration>>();
        for (var compilationUnit : compilationUnitList) {
            //Só executa se tiver imports
            if (compilationUnit.getImports().size() > 0) {
                //Se tiver imports, vamos pegar a classe que possui eles e adicionar em um hashmap
                for (var type : compilationUnit.getTypes()) {
                    if (type.isClassOrInterfaceDeclaration()) {
                        classesEImports.put((ClassOrInterfaceDeclaration) type, compilationUnit.getImports());
                    }
                }
            }
        }
        return classesEImports;
    }

    private NodeList<ClassOrInterfaceDeclaration> parseClasses(NodeList<TypeDeclaration<?>> typesList) {
        var classes = new NodeList<ClassOrInterfaceDeclaration>();
        for (var type : typesList) {
            if(type.isClassOrInterfaceDeclaration()) {
                classes.add((ClassOrInterfaceDeclaration) type);
            }
        }
        return classes;
    }

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ClassOrInterfaceType>> parseExtendedTypes(NodeList<ClassOrInterfaceDeclaration> classes) {
        var classesESuperclasses = new LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ClassOrInterfaceType>>();
        for (var classe : classes) {
            classesESuperclasses.put(classe, classe.getExtendedTypes());
        }
        return classesESuperclasses;
    }

    private NodeList<ClassOrInterfaceDeclaration> parseAdapters(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ClassOrInterfaceType>> classesESuperclasses) {
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

    private NodeList<ClassOrInterfaceDeclaration> parseUiComponents(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ClassOrInterfaceType>> classesESuperclasses) {
        var adapters = new NodeList<ClassOrInterfaceDeclaration>();
        for (var entry : classesESuperclasses.entrySet()) {
            var classe = entry.getKey();
            var implementacoes = entry.getValue();

            for (var implementacao : implementacoes) {
                if (implementacao.getName().getIdentifier().matches("BaseAdapter|Fragment|Activity")) {
                    adapters.add(classe);
                }
            }
        }
        return adapters;
    }

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> parseMembers(NodeList<ClassOrInterfaceDeclaration> classes) {
        var classeEMembros = new LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>>();
        for (var classe : classes) {
            classeEMembros.put(classe, classe.getMembers());
        }
        return classeEMembros;
    }

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> parseMethods(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> membros) {
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

    private LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<FieldDeclaration>> parseFieldDeclarations(LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> membros) {
        var classeEMembros = new LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<FieldDeclaration>>();
        for (var entry : membros.entrySet()) {
            var classe = entry.getKey();
            var membrosDaClasse = entry.getValue();
            var camposDaClasse = new NodeList<FieldDeclaration>();

            for (var membro : membrosDaClasse) {
                if (membro.isFieldDeclaration()) {
                    camposDaClasse.add((FieldDeclaration) membro);
                }
            }
            classeEMembros.put(classe, camposDaClasse);
        }
        return classeEMembros;
    }

    public LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> getAdapterMethods(File[] files) {
        var compilationUnitList = parseFiles(files);
        var typesList = parseTypesList(compilationUnitList);
        var classes = parseClasses(typesList);
        var classesESuperclasses = parseExtendedTypes(classes);
        var adapters = parseAdapters(classesESuperclasses);
        var membros = parseMembers(adapters);
        return parseMethods(membros);
    }

    public LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<FieldDeclaration>> getUiComponentFieldDeclarations(File[] files) {
        var compilationUnitList = parseFiles(files);
        var typesList = parseTypesList(compilationUnitList);
        var classes = parseClasses(typesList);
        var classesESuperclasses = parseExtendedTypes(classes);
        var uiComponents = parseUiComponents(classesESuperclasses);
        var membros = parseMembers(uiComponents);
        return parseFieldDeclarations(membros);
    }

    public LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<BodyDeclaration<?>>> getUiComponentMethods(File[] files) {
        var compilationUnitList = parseFiles(files);
        var typesList = parseTypesList(compilationUnitList);
        var classes = parseClasses(typesList);
        var classesESuperclasses = parseExtendedTypes(classes);
        var uiComponents = parseUiComponents(classesESuperclasses);
        var membros = parseMembers(uiComponents);
        return parseMethods(membros);
    }

    public LinkedHashMap<ClassOrInterfaceDeclaration, NodeList<ImportDeclaration>> getImports(File[] files) {
        var compilationUnitList = parseFiles(files);
        return parseImports(compilationUnitList);
    }

}
