package AndroidLinter;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("AndroidLinter - Erro:");
            System.out.println("Executar com a seguinte sintaxe");
            System.out.println("androidlinter.jar <diretorio>");
            System.out.println("Onde <diretorio> é a pasta raiz de um projeto Android");
            System.exit(0);
        }

        //Cria um objeto do tipo arquivo com o primeiro argumento, que deve ser o nome de um diretório.
        var pathApp = new File(args[0]);

        if(pathApp.exists() && pathApp.isDirectory()) {
            System.out.println("Linter running...");
            var app = new App(pathApp.getPath());
            app.run();
        }
    }
}
