package AndroidDetector;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImportantSmells {
    private static long qtdSubelementos = 0;
    private static File arquivos[];
    private static File diretorio = null;
    private static SAXBuilder sb = new SAXBuilder();

    public static void CoupledUIComponent(String pathApp) {
        try {
            diretorio = new File(pathApp);
            arquivos = diretorio.listFiles();

            for (int cont = 0; cont < arquivos.length; cont++) {
                System.out.println("Arquivo analisado:" + arquivos[cont]);
                System.out.println("---------------------------------------------------------------------------------------");

                File f = new File(arquivos[cont].toString());
                Document d = sb.build(f);
                Element rootElmnt = d.getRootElement();
                List elements = rootElmnt.getChildren();

                for (int i = 0; i < elements.size(); i++) {
                    org.jdom2.Element el = (org.jdom2.Element) elements.get(i);

                    List<Element> SubElements = el.getChildren();

                    for (int j = 0; j < SubElements.size(); j++) {
                        org.jdom2.Element elChildren = (org.jdom2.Element) SubElements.get(j);
                        List<org.jdom2.Attribute> listAttr = (List<org.jdom2.Attribute>) elChildren.getAttributes();

                        if(elChildren.getName() == "fragment") {
                            Boolean style = false;
                            for (org.jdom2.Attribute item : listAttr) {
                                if (item.getName() == "name") {
                                    style = true;
                                }
                            }
                            if (!style) {
                                System.out.println("Componente de UI Acoplado " + elChildren.getName() + " - Considere utilizar viewGroup");
                            }
                        }
                    }
                }


                System.out.println("---------------------------------------------------------------------------------------");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void SuspiciousBehavior(String pathApp) throws FileNotFoundException {
        diretorio = new File(pathApp);
        arquivos = diretorio.listFiles();

        for (int cont = 0; cont < arquivos.length; cont++) {
            System.out.println("Arquivo analisado:" + arquivos[cont]);
            System.out.println("---------------------------------------------------------------------------------------");

            File f = new File(arquivos[cont].toString());
            CompilationUnit cu = JavaParser.parse(f);
            ClassOrInterfaceDeclaration n = new ClassOrInterfaceDeclaration();
            List<LocalClassDeclarationStmt> classes = cu.findAll(LocalClassDeclarationStmt.class);

            for (LocalClassDeclarationStmt item : classes){
                System.out.println("Comportamento suspeito encontrado  na classe " + item.getClassDeclaration().getName() + " - " + item.getRange());
            }
        }

    }

    public static void FoolAdapter(String pathApp) {
        try {
            File arquivos[];
            File diretorio = new File(pathApp);
            arquivos = diretorio.listFiles();

            for (File arquivo : arquivos) {
                CompilationUnit compilationunit = JavaParser.parse(arquivo);

                //Extrai cada Classe analisada pelo CompilationUnit
                ArrayList<ClassOrInterfaceDeclaration> classes = new ArrayList<ClassOrInterfaceDeclaration>();
                NodeList<TypeDeclaration<?>> types = compilationunit.getTypes();
                for (int i = 0; i < types.size(); i++) {
                    classes.add((ClassOrInterfaceDeclaration) types.get(i));
                }

                //Para cada uma dessas classes, verifica se ela é um Adapter (ou seja, se ela extende de BaseAdapter).
                for (ClassOrInterfaceDeclaration classe : classes) {

                    //Como a classe vai ser analisada ainda, não contém smells por enquanto
                    Boolean isFoolAdapter = false;

                    //Para ver se a classe é um Adapter, precisamos ver se ela extende de BaseAdapter
                    //Pegamos todas as classes que ela implementa
                    NodeList<ClassOrInterfaceType> implementacoes = classe.getExtendedTypes();
                    for (ClassOrInterfaceType implementacao : implementacoes) {
                        if (implementacao.getName().getIdentifier().equals("BaseAdapter")) {
                            //Se chegou até aqui, temos certeza de que é um adapter.
                            //Se a classe que extende do BaseAdapter tiver algum método que não seja sobrescrever um método de interface, é um FlexAdapter.
                            //Pegamos todos os membros da classe
                            NodeList<BodyDeclaration<?>> membros = classe.getMembers();
                            //Verifica se o membro é um método
                            for (BodyDeclaration<?> membro : membros)
                                if (membro.isMethodDeclaration()) {
                                    MethodDeclaration metodo = (MethodDeclaration) membro;
                                    //Verifica se este método chama getView
                                    if (metodo.getName().getIdentifier().equals("getView")) {

                                        //Pega o parametro do tipo View e armazena o nome dele
                                        //Pode ser útil para verificar por findViewById dentro de laços
                                        Parameter viewParameter = metodo.getParameter(1);
                                        String nomeParametroView = viewParameter.getName().getIdentifier();

                                        //Pega o bloco de declarações dentro método getView
                                        BlockStmt body = metodo.getBody().get();
                                        NodeList<Statement> statements = body.getStatements();

                                        //Itera sobre as declarações até achar expressões
                                        for (Statement statement : statements) {
                                            if (statement.isExpressionStmt()) {
                                                //Se em alguma dessas expressões tiver o texto findViewById
                                                //Quer dizer que o ViewHolder não está sendo utilizado, o que caracteriza o smell
                                                if(statement.toString().contains("findViewById(")) {
                                                    isFoolAdapter = true;
                                                }

                                                //Se ele infla um Layout em toda chamada ao getView, isso também caracteriza o smell
                                                if(statement.toString().contains("getLayoutInflater(")) {
                                                    isFoolAdapter = true;
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                    }

                    //Se a classe for um foolAdapter, imprime o erro na tela
                    if (isFoolAdapter) {
                        System.out.println("Fool Adapter detectado na classe " + classe.getName().getIdentifier());
                    }
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void FlexAdapter(String pathApp) {
        try {
            File arquivos[];
            File diretorio = new File(pathApp);
            arquivos = diretorio.listFiles();

            for (File arquivo : arquivos) {
                CompilationUnit compilationunit = JavaParser.parse(arquivo);

                //Extrai cada Classe analisada pelo CompilationUnit
                ArrayList<ClassOrInterfaceDeclaration> classes = new ArrayList<ClassOrInterfaceDeclaration>();
                NodeList<TypeDeclaration<?>> types = compilationunit.getTypes();
                for (int i = 0; i < types.size(); i++) {
                    classes.add((ClassOrInterfaceDeclaration) types.get(i));
                }

                //Para cada uma dessas classes, verifica se ela é um Adapter (ou seja, se ela extende de BaseAdapter).
                for (ClassOrInterfaceDeclaration classe : classes) {

                    //Como a classe vai ser analisada ainda, não contém smells por enquanto
                    Boolean isFlexAdapter = false;

                    //Para ver se a classe é um Adapter, precisamos ver se ela extende de BaseAdapter
                    //Pegamos todas as classes que ela implementa
                    NodeList<ClassOrInterfaceType> implementacoes = classe.getExtendedTypes();
                    for (ClassOrInterfaceType implementacao : implementacoes) {
                        if (implementacao.getName().getIdentifier().equals("BaseAdapter")) {
                            //Se chegou até aqui, temos certeza de que é um adapter.
                            //Se a classe que extende do BaseAdapter tiver algum método que não seja sobrescrever um método de interface, é um FlexAdapter.
                            //Pegamos todos os membros da classe
                            NodeList<BodyDeclaration<?>> membros = classe.getMembers();
                            for (BodyDeclaration<?> membro : membros) {
                                //Verifica se o membro é um método
                                if (membro.isMethodDeclaration()) {
                                    //Para cada método, pega suas annotações, se não tiver, é lógica de negócio e é um flexAdapter
                                    NodeList<AnnotationExpr> annotations = membro.getAnnotations();

                                    //Sem annotations, dá erro
                                    if(annotations.size() == 0) {
                                        isFlexAdapter = true;
                                    }

                                    for (AnnotationExpr annotation : annotations ) {
                                        //Se tiver annotacoes, mas nenhuma dessas anotações forem Override, é um método que não implementa método de interface, ou seja, é lógica de negócio e é um FlexAdapter
                                        if (!annotation.getName().getIdentifier().equals("Override")) {
                                            isFlexAdapter = true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //Se a classe for um flexAdapter, imprime o erro na tela
                    if (isFlexAdapter) {
                        System.out.println("Flex Adapter detectado na classe " + classe.getName().getIdentifier());
                    }
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void GodStyleResource(String pathApp) {
        try {
            diretorio = new File(pathApp);
            arquivos = diretorio.listFiles();
            int numStyles = 0;


            for (int cont = 0; cont < arquivos.length; cont++) {
                if (arquivos[cont].toString().contains("styles")) {
                    numStyles = numStyles + 1;
                }
            }
            if (numStyles <= 1) {
                System.out.println("Longo recurso de Estilo detectado (existe apenas um arquivo para estilos no aplicativo)");
                System.out.println("---------------------------------------------------------------------------------------");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void DeepNestedLayout(String pathApp) {
        try {
            diretorio = new File(pathApp);
            arquivos = diretorio.listFiles();

            for (int cont = 0; cont < arquivos.length; cont++) {
                System.out.println("Arquivo analisado:" + arquivos[cont]);
                System.out.println("---------------------------------------------------------------------------------------");

                File f = new File(arquivos[cont].toString());

                //LER TODA A ESTRUTURA DO XML
                Document d = sb.build(f);

                //ACESSAR O ROOT ELEMENT
                Element rootElmnt = d.getRootElement();

                //BUSCAR ELEMENTOS FILHOS DA TAG
                List elements = rootElmnt.getChildren();

                recursiveChildrenElement(elements);

                System.out.println("---------------------------------------------------------------------------------------");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void DuplicateStyleAttributes(String pathApp) {
        try {
            diretorio = new File(pathApp);
            arquivos = diretorio.listFiles();

            for (int cont = 0; cont < arquivos.length; cont++) {
                System.out.println("Arquivo analisado:" + arquivos[cont]);
                System.out.println("---------------------------------------------------------------------------------------");

                File f = new File(arquivos[cont].toString());
                Document d = sb.build(f);
                Element rootElmnt = d.getRootElement();
                List elements = rootElmnt.getChildren();

                for (int i = 0; i < elements.size(); i++) {
                    org.jdom2.Element el = (org.jdom2.Element) elements.get(i);

                    List<Element> SubElements = el.getChildren();

                    for (int j = 0; j < SubElements.size(); j++) {
                        org.jdom2.Element elChildren = (org.jdom2.Element) SubElements.get(j);
                        List<org.jdom2.Attribute> listAttr = (List<org.jdom2.Attribute>) elChildren.getAttributes();

                        Boolean style = false;
                        for (org.jdom2.Attribute item : listAttr) {
                            if (item.getName() == "style") {
                                style = true;
                            }
                        }
                        if (!style) {
                            System.out.println("Duplicate Style Attributes " + elChildren.getName() + " - Considere colocar a formatação das propriedades em um recurso de estilo:");
                        }
                    }
                }


                System.out.println("---------------------------------------------------------------------------------------");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


        public static void teste() throws Exception {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            parser.parse("C:\\Users\\julio\\Desktop\\Amostragem de Apps a Analisar\\Bucket\\app\\src\\main\\res\\layout\\activity_detailed.xml", new SampleOfXmlLocator());
        }

        private static void recursiveChildrenElement(List elements) {
            for (int i = 0; i < elements.size(); i++) {

                org.jdom2.Element el = (org.jdom2.Element) elements.get(i);
                List SubElements = el.getChildren();

                if (SubElements.size() > 0) {
                    qtdSubelementos = qtdSubelementos + 1;
                    recursiveChildrenElement(SubElements);
                } else {
                    if (qtdSubelementos > 3) {
                        System.out.println("Layout Profundamente Aninhado encontrado " + el.getName());
                        break;
                    }
                }
            }

        }

    }

