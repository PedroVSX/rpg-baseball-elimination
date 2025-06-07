import baseball.BaseballElimination;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        int opcao;
        final String caminho = "src/main/java/baseball/teams/";

        final String[] times = {
                "teams1.txt",
                "teams10.txt",
                "teams12-allgames.txt",
                "teams12.txt",
                "teams24.txt",
                "teams29.txt",
                "teams30.txt",
                "teams32.txt",
                "teams36.txt",
                "teams4.txt",
                "teams42.txt",
                "teams48.txt",
                "teams4a.txt",
                "teams4b.txt",
                "teams5.txt",
                "teams50.txt",
                "teams54.txt",
                "teams5a.txt",
                "teams5b.txt",
                "teams5c.txt",
                "teams60.txt",
                "teams7.txt",
                "teams8.txt",
        };


        for(int i = 0;i < times.length;i++){
            System.out.printf("%d - %s\n",i,times[i]);
        }

        System.out.println("Selecione um arquivo: ");
        opcao = input.nextInt();

        String arquivoEscolhido = caminho + times[opcao];
        BaseballElimination.main(new String[]{arquivoEscolhido});
    }
}