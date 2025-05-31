<img src="../imgs/UNIFOR_logo1b.png" width="400"> 

# Resolução de Problemas em Grafos
**Orientador**: Prof. Me Ricardo Carubbi

## Eliminação no Baseball

Dadas as classificações em uma divisão esportiva em determinado ponto da temporada, determine quais times foram matematicamente eliminados de vencer sua divisão.

### O problema da eliminação no baseball

No [problema da eliminação no baseball](https://en.wikipedia.org/wiki/Maximum_flow_problem#Baseball_elimination), existe uma divisão composta por *n* times. Em certo ponto da temporada, o time *i* possui *w\[i]* vitórias, *l\[i]* derrotas, *r\[i]* jogos restantes, e *g\[i]\[j]* jogos restantes a serem disputados contra o time *j*. Um time está matematicamente eliminado se não pode, sob nenhuma circunstância, terminar a temporada em primeiro lugar (isolado ou empatado). O objetivo é determinar exatamente quais times estão matematicamente eliminados. Para simplificar, assumimos que nenhum jogo termina empatado (como na Major League Baseball) e que não há cancelamentos (ou seja, todos os jogos agendados serão jogados).

O problema não é tão simples quanto muitos jornalistas esportivos fazem parecer, em parte porque a resposta depende não apenas da quantidade de vitórias e jogos restantes, mas também do calendário de jogos restantes. Para ver essa complicação, considere o seguinte cenário:

| i | Time         | w\[i] | l\[i] | r\[i] | Atl | Phi | NY | Mon |
| - | ------------ | ----- | ----- | ----- | --- | --- | -- | --- |
| 0 | Atlanta      | 83    | 71    | 8     | -   | 1   | 6  | 1   |
| 1 | Philadelphia | 80    | 79    | 3     | 1   | -   | 0  | 2   |
| 2 | New York     | 78    | 78    | 6     | 6   | 0   | -  | 0   |
| 3 | Montreal     | 77    | 82    | 3     | 1   | 2   | 0  | -   |

Montreal está matematicamente eliminado, pois pode terminar com no máximo 80 vitórias, e Atlanta já possui 83 vitórias. Essa é a razão mais simples para eliminação. No entanto, podem haver razões mais complexas. Por exemplo, Philadelphia também está matematicamente eliminada. Ela pode terminar a temporada com até 83 vitórias, o que parece suficiente para empatar com Atlanta. Mas isso exigiria que Atlanta perdesse todos os seus jogos restantes, incluindo os 6 contra New York, caso em que New York terminaria com 84 vitórias. Notamos que New York ainda não está matematicamente eliminado, apesar de ter menos vitórias que Philadelphia.

Nem sempre é fácil para um jornalista esportivo explicar por que um determinado time está matematicamente eliminado. Considere o seguinte cenário da American League East em 30 de agosto de 1996:

| i | Time      | w\[i] | l\[i] | r\[i] | NY | Bal | Bos | Tor | Det |
| - | --------- | ----- | ----- | ----- | -- | --- | --- | --- | --- |
| 0 | New York  | 75    | 59    | 28    | -  | 3   | 8   | 7   | 3   |
| 1 | Baltimore | 71    | 63    | 28    | 3  | -   | 2   | 7   | 7   |
| 2 | Boston    | 69    | 66    | 27    | 8  | 2   | -   | 0   | 3   |
| 3 | Toronto   | 63    | 72    | 27    | 7  | 7   | 0   | -   | 3   |
| 4 | Detroit   | 49    | 86    | 27    | 3  | 7   | 3   | 3   | -   |

Pode parecer que Detroit tem uma pequena chance de alcançar New York e vencer a divisão, pois Detroit pode terminar com até 76 vitórias se vencer os 27 jogos restantes, o que é uma a mais do que New York teria se perdesse todos os seus 28 jogos restantes. Tente se convencer de que Detroit já está matematicamente eliminado.

### Uma formulação com fluxo máximo

Agora resolvemos o problema da eliminação no baseball reduzindo-o ao [problema de fluxo máximo](https://www.youtube.com/watch?v=vYFHNGGOlKE). Para verificar se o time *x* está eliminado, consideramos dois casos.

#### Eliminação trivial

Se o número máximo de jogos que o time *x* pode vencer é menor que o número de vitórias de algum outro time *i*, então *x* está trivialmente eliminado (como Montreal no exemplo acima). Isto é, se $w[x] + r[x] < w[i]$ então o time *x* está matematicamente eliminado.

#### Eliminação não trivial

Caso contrário, criamos uma rede de fluxo (flow network) e resolvemos um problema de fluxo máximo nela. Na rede, os fluxos integrais viáveis correspondem a resultados possíveis do calendário restante. Existem vértices correspondentes aos jogos restantes (não envolvendo o time *x*) e aos demais times. Intuitivamente, cada unidade de fluxo na rede representa um jogo restante. À medida que esse fluxo percorre a rede de *s* até *t*, ele passa por um vértice de jogo (por exemplo, entre os times *i* e *j*), depois por um dos vértices dos times *i* ou *j*, classificando este jogo como vencido por esse time.

Mais precisamente, a rede de fluxo inclui as seguintes arestas e capacidades:

* Conectamos um vértice artificial de origem *s* a cada vértice de jogo *i-j* e atribuímos capacidade *g\[i]\[j]*. Se um fluxo usa toda a capacidade *g\[i]\[j]* nessa aresta, então interpretamos isso como todos os jogos sendo jogados, com as vitórias distribuídas entre os vértices dos times *i* e *j*.

* Conectamos cada vértice de jogo *i-j* aos dois vértices de times adversários, garantindo que um dos dois times ganhe o jogo. Não é necessário restringir a quantidade de fluxo nessas arestas.

* Finalmente, conectamos cada vértice de time a um vértice artificial de destino *t*. Queremos saber se existe alguma maneira de completar todos os jogos de forma que o time *x* termine com pelo menos tantas vitórias quanto qualquer outro time. Como *x* pode alcançar até *w\[x] + r\[x]* vitórias, impedimos que qualquer outro time *i* tenha mais do que esse total, incluindo uma aresta do vértice do time *i* até *t* com capacidade *w\[x] + r\[x] - w\[i]*.

Se todas as arestas saindo de *s* estiverem saturadas no fluxo máximo, isso corresponde a uma atribuição de vencedores para todos os jogos restantes de forma que nenhum time vença mais jogos que *x*. Se algumas arestas saindo de *s* não estiverem saturadas, então não há nenhum cenário em que *x* possa vencer a divisão. Na rede de fluxo abaixo, Detroit é o time *x = 4*.

<img src="../imgs/baseball.png" height="320">

#### O que o min cut nos diz

Ao resolver um problema de fluxo máximo, podemos determinar se um dado time está matematicamente eliminado. Também gostaríamos de explicar a razão para a eliminação de um time a um amigo de forma não técnica (usando apenas aritmética básica). Aqui está tal explicação para a eliminação de Detroit no exemplo da American League East acima:

Com a melhor sorte possível, Detroit termina a temporada com *49 + 27 = 76* vitórias. Considere o subconjunto de times *R = { New York, Baltimore, Boston, Toronto }*. Coletivamente, eles já têm *75 + 71 + 69 + 63 = 278* vitórias; existem ainda *3 + 8 + 7 + 2 + 7 = 27* jogos restantes entre eles, portanto esses quatro times devem vencer pelo menos mais 27 jogos. Assim, em média, os times em *R* vencerão no mínimo *305 / 4 = 76.25* jogos. Independentemente do resultado, um dos times de *R* terá pelo menos 77 vitórias, eliminando assim Detroit.

De fato, quando um time está matematicamente eliminado, sempre existe tal certificado de eliminação convincente, onde *R* é algum subconjunto dos outros times da divisão. Além disso, sempre é possível encontrar esse subconjunto *R* escolhendo os vértices de time que estão no lado da origem de um corte mínimo *s-t* na rede de eliminação do baseball. Note que, embora tenhamos resolvido um problema de fluxo máximo/corte mínimo para encontrar o subconjunto *R*, uma vez que o temos, o argumento para a eliminação do time envolve apenas aritmética básica.

### Sua tarefa

Implemente um tipo de dado imutável `BaseballElimination` que representa uma divisão esportiva e determina quais times estão matematicamente eliminados, implementando a seguinte API:

```java
public BaseballElimination(String filename)
public              int numberOfTeams()
public Iterable<String> teams()
public              int wins(String team)
public              int losses(String team)
public              int remaining(String team)
public              int against(String team1, String team2)
public          boolean isEliminated(String team)
public Iterable<String> certificateOfElimination(String team)
```

Os últimos seis métodos devem lançar uma `IllegalArgumentException` se um (ou ambos) dos argumentos de entrada forem times inválidos.

### Formato de entrada

O formato de entrada é o número de times da divisão *n*, seguido por uma linha para cada time. Cada linha contém o nome do time (sem espaços internos), o número de vitórias, o número de derrotas, o número de jogos restantes, e o número de jogos restantes contra cada time da divisão. Por exemplo:

```bash
4
Atlanta       83 71  8  0 1 6 1
Philadelphia  80 79  3  1 0 0 2
New_York      78 78  6  6 0 0 0
Montreal      77 82  3  1 2 0 0
```

### Formato de saída

Use a seguinte função `main()`:

```java
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
```

### Análise

Analise o uso de memória e o tempo de execução no pior caso do seu algoritmo.

* Qual é a ordem de crescimento da quantidade de memória (no pior caso) usada pelo seu programa para determinar se um time está eliminado? Em particular, quantos vértices e arestas há na rede de fluxo em função do número de times *n*?

* Qual é a ordem de crescimento do tempo de execução (no pior caso) para determinar se um time está eliminado em função do número de times *n*? Assuma que o tempo de execução do algoritmo de fluxo máximo em uma rede com *V* vértices e *E* arestas é *V·E²*.

Além disso, use a saída do seu programa para responder à seguinte pergunta:

* Considere a divisão esportiva definida no arquivo `teams12.txt`. Explique, em termos não técnicos (usando os resultados do `certificateOfElimination` e aritmética básica), por que o Japão (Japan) está matematicamente eliminado.

### Submissão Web

Envie um arquivo `.zip` contendo `BaseballElimination.java` e quaisquer outros arquivos de suporte (excluindo aqueles do `algs4.jar`). Você não pode usar bibliotecas além das disponíveis em `java.lang`, `java.util` e `algs4.jar`.

Este exercício foi desenvolvido por Kevin Wayne.
Copyright © 2003.
