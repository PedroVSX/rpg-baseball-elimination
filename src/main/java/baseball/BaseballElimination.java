package baseball;

import edu.princeton.cs.algs4.*;
import java.util.*;

public class BaseballElimination {

    private final int numberOfTeams;

    // <nomeTime, numPosicao> para achar rapidamente a posição do time
    // usado para achar o índice em vitórias, derrotas, etc
    private final Map<String, Integer> teamIndex;

    private final String[] teamNames;

    private final int[] wins;

    private final int[] losses;

    private final int[] remaining;

    private final int[][] games;

    // Cache de times eliminados
    private Map<String, List<String>> eliminatedTeams;

    public BaseballElimination(String filename) {
        In in = new In(filename); // Ler o caminho do arquivo
        numberOfTeams = in.readInt(); // Lê e armazena a quantidade de times no .txt indicado
        teamIndex = new HashMap<>(); // Cria um HashMap dos times
        teamNames = new String[numberOfTeams]; // Cria uma lista com os nomes dos times
        wins = new int[numberOfTeams]; // Cria uma lista de vitórias de cada time
        losses = new int[numberOfTeams]; // Cria uma lista de derrotas de cada time
        remaining = new int[numberOfTeams]; // Cria uma de jogos restantes de cada time
        games = new int[numberOfTeams][numberOfTeams]; // Cria uma matriz de jogos, proporcional a quantidade de times presentes
        eliminatedTeams = new HashMap<>(); // Cria a cache de times eliminados para otimização

        for (int i = 0; i < numberOfTeams; i++) {
            String name = in.readString(); // Armazena o nome do time
            teamNames[i] = name; // Adiciona o nome do time na lista de times
            teamIndex.put(name, i); // Adiciona o nome do time e a posição do mesmo //
            wins[i] = in.readInt(); // Adiciona a quantidade de vitórias de cada time na lista de vitórias
            losses[i] = in.readInt(); // Adiciona a quantidade de derrotas de cada time na lista de derrotas
            remaining[i] = in.readInt(); // Adiciona a quantidade de jogos restantes de cada time na lista de jogos restantes
            for (int j = 0; j < numberOfTeams; j++) {
                games[i][j] = in.readInt(); // Adiciona a quantidade de jogos com os outros times na matriz
            }
        }

        in.close();
    }

    public int numberOfTeams() {
        return numberOfTeams;
    }

    public Iterable<String> teams() {
        return teamIndex.keySet();
    }

    public int wins(String team) throws IllegalArgumentException {
        if (!teamIndex.containsKey(team)) {
            throw new IllegalArgumentException("Não existe esse time!");
        }

        return wins[teamIndex.get(team)];
    }

    public int losses(String team) throws IllegalArgumentException {
        if (!teamIndex.containsKey(team)) {
            throw new IllegalArgumentException("Não existe esse time!");
        }

        return losses[teamIndex.get(team)];
    }

    public int remaining(String team) throws IllegalArgumentException {
        if (!teamIndex.containsKey(team)) {
            throw new IllegalArgumentException("Não existe esse time!");
        }

        return remaining[teamIndex.get(team)];
    }

    public int against(String team1, String team2) throws IllegalArgumentException {
        if (!teamIndex.containsKey(team1) || !teamIndex.containsKey(team2)) {
            throw new IllegalArgumentException("Alguns dos times não existem!");
        }

        return games[teamIndex.get(team1)][teamIndex.get(team2)];
    }

    public boolean isEliminated(String team) throws IllegalArgumentException {
        if (!teamIndex.containsKey(team)) {
            throw new IllegalArgumentException("Não existe esse time!");
        }

        if (eliminatedTeams.containsKey(team)) return true;

        List<String> certificate = (List<String>) certificateOfElimination(team);
        if (certificate != null) {
            eliminatedTeams.put(team, certificate);
            return true;
        }

        return false;
    }

    public Iterable<String> certificateOfElimination(String team) throws IllegalArgumentException {
        if (!teamIndex.containsKey(team)) {
            throw new IllegalArgumentException("Não existe esse time!");
        }

        if (eliminatedTeams.containsKey(team)) {
            return new ArrayList<>(eliminatedTeams.get(team));
        }

        int x = teamIndex.get(team);

        // Checa eliminação trivial
        List<String> trivial = checkTrivialElimination(x);
        if (trivial != null) {
            eliminatedTeams.put(team, trivial);
            return trivial;
        }

        // Checa eliminação por fluxo (não trivial)
        List<String> nonTrivial = checkNonTrivialElimination(x);
        if (nonTrivial != null) {
            eliminatedTeams.put(team, nonTrivial);
            return nonTrivial;
        }

        return null;
    }

    private List<String> checkTrivialElimination(int teamIndex) {
        List<String> certificate = new ArrayList<>(); // Lista para armazenar o time que elimina trivialmente

        for (int i = 0; i < numberOfTeams; i++) {
            if (i == teamIndex) continue;

            // Se o número máximo de vitórias possíveis do time (vitórias atuais + jogos restantes)
            // for menor que o número de vitórias de algum outro time, ele está trivialmente eliminado.
            if (wins[teamIndex] + remaining[teamIndex] < wins[i]) {
                certificate.add(teamNames[i]); // Esse time elimina o time atual trivialmente
                return certificate; // Retorna o "certificado de eliminação" (lista com esse time)
            }
        }

        return null; // Não foi eliminado trivialmente
    }

    private List<String> checkNonTrivialElimination(int teamIndex) {
        FlowNetwork network = buildFlowNetwork(teamIndex); // Cria a rede fluxo do time atual

        int s = 0; // Define nó de inicio
        int t = network.V() - 1; // Define nó de destino

        FordFulkerson ff = new FordFulkerson(network, s, t); // Executa o algoritmo para calcular o fluxo máximo entre inicio-destino

        double totalCapacity = totalSourceCapacity(network, s); // Soma da capacidades das fontes

        // Se o fluxo total não atingir a capacidade máxima o time é eliminado
        if (ff.value() < totalCapacity) {
            List<String> certificate = new ArrayList<>(); // Lista dos times responsáveis pela eliminação
            Map<String, Integer> teamNodes = getTeamNodes(teamIndex);

            // Os times que estão do lado dá fonte no corte mínimo formas o certificado de eliminação
            for (String team : teamNodes.keySet()) {
                if (ff.inCut(teamNodes.get(team))) {
                    certificate.add(team);
                }
            }

            return certificate; // Retorna o "certificado de eliminação" (lista com esse time)
        }

        return null; // Não foi eliminado não-trivialmente
    }

    private FlowNetwork buildFlowNetwork(int teamIndex) {
        int gameNodes = (numberOfTeams - 1) * (numberOfTeams - 2) / 2; // Calcula o total de nós
        int V = 2 + gameNodes + (numberOfTeams - 1); // Calcula o total de vertices
        int s = 0; // Define o nó de inicio
        int t = V - 1; // Define o nó de destino

        FlowNetwork network = new FlowNetwork(V);

        Map<String, Integer> teamNodes = getTeamNodes(teamIndex); // Cria os caminhos dos times com valores

        int gameNodeId = 1; // Começa a nomear os nós a partir do indice 1

        // Conecta os jogos entre os times
        for (int i = 0; i < numberOfTeams; i++) {
            if (i == teamIndex) continue;

            for (int j = i + 1; j < numberOfTeams; j++) {
                if (j == teamIndex) continue;

                String team1 = teamNames[i];
                String team2 = teamNames[j];

                // Fonte -> Nó de jogo
                network.addEdge(new FlowEdge(s, gameNodeId, games[i][j]));

                // Jogo -> Nós de times
                // Isso permite qualquer distribuição de vitórias entre os dois times
                network.addEdge(new FlowEdge(gameNodeId, teamNodes.get(team1), Double.POSITIVE_INFINITY));
                network.addEdge(new FlowEdge(gameNodeId, teamNodes.get(team2), Double.POSITIVE_INFINITY));

                gameNodeId++; // Avança para o próximo nó de jogo
            }
        }

        // Nó de time -> destino
        for (String team : teamNodes.keySet()) {
            int cap = wins[teamIndex] + remaining[teamIndex] - wins[this.teamIndex.get(team)]; // Calcula a capacidade máxima de vitórias que esse time pode ter sem ultrapassar o time analisado
            cap = Math.max(0, cap); // Garante que a capacidade seja não-negativa
            network.addEdge(new FlowEdge(teamNodes.get(team), t, cap)); // Cria a aresta do time para o destino com essa capacidade
        }

        return network; // Retorna a rede construida
    }

    private Map<String, Integer> getTeamNodes(int teamIndex) {
        Map<String, Integer> teamNodes = new HashMap<>();

        int gameNodes = (numberOfTeams - 1) * (numberOfTeams - 2) / 2;
        int nodeId = 1 + gameNodes;

        for (int i = 0; i < numberOfTeams; i++) {
            if (i == teamIndex) continue;
            teamNodes.put(teamNames[i], nodeId++);
        }

        return teamNodes;
    }

    private double totalSourceCapacity(FlowNetwork network, int source) {
        double total = 0;

        for (FlowEdge edge : network.adj(source)) {
            total += edge.capacity();
        }

        return total;
    }

    /*
    *
    * Pra compilar o código:
    * javac -cp "lib/algs4.jar:src/main/java" src/main/java/baseball/BaseballElimination.java
    *
    * Pra rodar o código:
    * java -cp "lib/algs4.jar;src/main/java" baseball.BaseballElimination /baseball/teams/teams54.txt
    *
    *  */
    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
