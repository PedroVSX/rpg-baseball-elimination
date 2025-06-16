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
        In in = new In(filename);
        numberOfTeams = in.readInt();
        teamIndex = new HashMap<>();
        teamNames = new String[numberOfTeams];
        wins = new int[numberOfTeams];
        losses = new int[numberOfTeams];
        remaining = new int[numberOfTeams];
        games = new int[numberOfTeams][numberOfTeams];
        eliminatedTeams = new HashMap<>();

        for (int i = 0; i < numberOfTeams; i++) {
            String name = in.readString();
            teamNames[i] = name;
            teamIndex.put(name, i);
            wins[i] = in.readInt();
            losses[i] = in.readInt();
            remaining[i] = in.readInt();
            for (int j = 0; j < numberOfTeams; j++) {
                games[i][j] = in.readInt();
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
        List<String> certificate = new ArrayList<>();

        for (int i = 0; i < numberOfTeams; i++) {
            if (i == teamIndex) continue;

            if (wins[teamIndex] + remaining[teamIndex] < wins[i]) {
                certificate.add(teamNames[i]);
                return certificate;
            }
        }

        return null;
    }

    private List<String> checkNonTrivialElimination(int teamIndex) {
        FlowNetwork network = buildFlowNetwork(teamIndex);

        int s = 0;
        int t = network.V() - 1;

        FordFulkerson ff = new FordFulkerson(network, s, t);

        double totalCapacity = totalSourceCapacity(network, s);

        if (ff.value() < totalCapacity) {
            List<String> certificate = new ArrayList<>();
            Map<String, Integer> teamNodes = getTeamNodes(teamIndex);

            for (String team : teamNodes.keySet()) {
                if (ff.inCut(teamNodes.get(team))) {
                    certificate.add(team);
                }
            }

            return certificate;
        }

        return null;
    }

    private FlowNetwork buildFlowNetwork(int teamIndex) {
        int gameNodes = (numberOfTeams - 1) * (numberOfTeams - 2) / 2;
        int V = 2 + gameNodes + (numberOfTeams - 1);
        int s = 0;
        int t = V - 1;

        FlowNetwork network = new FlowNetwork(V);

        Map<String, Integer> teamNodes = getTeamNodes(teamIndex);

        int gameNodeId = 1;
        for (int i = 0; i < numberOfTeams; i++) {
            if (i == teamIndex) continue;

            for (int j = i + 1; j < numberOfTeams; j++) {
                if (j == teamIndex) continue;

                String team1 = teamNames[i];
                String team2 = teamNames[j];

                // Fonte -> Nó de jogo
                network.addEdge(new FlowEdge(s, gameNodeId, games[i][j]));

                // Jogo -> Nós de times
                network.addEdge(new FlowEdge(gameNodeId, teamNodes.get(team1), Double.POSITIVE_INFINITY));
                network.addEdge(new FlowEdge(gameNodeId, teamNodes.get(team2), Double.POSITIVE_INFINITY));

                gameNodeId++;
            }
        }

        // Nó de time -> destino
        for (String team : teamNodes.keySet()) {
            int cap = wins[teamIndex] + remaining[teamIndex] - wins[this.teamIndex.get(team)];
            cap = Math.max(0, cap);
            network.addEdge(new FlowEdge(teamNodes.get(team), t, cap));
        }

        return network;
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
    * javac -cp "lib/algs4.jar;src/main/java" src/main/java/baseball/BaseballElimination.java
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
