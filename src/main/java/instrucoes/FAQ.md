## Perguntas Frequentes

**Como faço para ler os dados?** Recomendamos utilizar os métodos `readInt()` e `readString()` da classe [In.java](https://introcs.cs.princeton.edu/java/stdlib/In.java.html).

**Quão eficiente meu programa deve ser?** Ele deve ser capaz de lidar com todos os arquivos de entrada de teste fornecidos (digamos, em menos de um minuto). Não se preocupe em superotimizar seu programa, pois os conjuntos de dados usados em aplicações reais são pequenos.

**O que devo retornar se houver mais de um certificado de eliminação?** Retorne qualquer subconjunto válido.

**Preciso imprimir os times na mesma ordem do arquivo de entrada (como faz a solução de referência)?** Não, qualquer ordem é aceitável.

**O método `certificateOfElimination()` deve funcionar mesmo que `isEliminated()` não tenha sido chamado antes?** Com certeza. É uma má prática (e violação da API) que o sucesso de uma chamada dependa de outra chamada prévia.

**Como calculo o mincut?** O método `inCut()` na classe [FordFulkerson.java](https://github.com/kevin-wayne/algs4/blob/master/src/main/java/edu/princeton/cs/algs4/FordFulkerson.java) recebe um vértice como argumento e retorna `true` se esse vértice estiver no lado da origem do corte mínimo (mincut).

**Como especifico uma capacidade infinita para uma aresta?** Use `Double.POSITIVE_INFINITY`.

**O que pode causar uma exceção em tempo de execução com a mensagem "Edge does not satisfy capacity constraints: ..."?** Verifique se você criou uma aresta com capacidade negativa.

**Meu programa está executando muito mais rápido na prática do que a análise teórica sugere. Devo me preocupar?** Não. Existem várias razões pelas quais seu programa pode ter desempenho melhor do que o pior caso teórico:

* Se um time for eliminado por uma razão trivial, o código pode evitar o cálculo do fluxo máximo.
* Se houver poucos jogos entre os pares de times, a rede de fluxo terá menos arestas que no pior caso.
* Redes de fluxo do problema de eliminação no baseball têm estrutura especial: são bipartidas e suas capacidades são inteiros pequenos. Isso faz com que o algoritmo de Ford-Fulkerson funcione bem mais rápido que sua garantia de pior caso (V·E²).

## Entrada e Testes

**Entrada.** O arquivo `baseball.zip` contém arquivos de entrada de exemplo.

**Testes.** Para referência, os times abaixo estão matematicamente eliminados por razões não triviais (isto é, o certificado de eliminação exige dois ou mais times). Se um time é eliminado de forma trivial, ele não aparece na lista:

* `teams4.txt`: Philadelphia
* `teams4a.txt`: Ghaddafi
* `teams5.txt`: Detroit
* `teams7.txt`: Ireland
* `teams24.txt`: Team13
* `teams32.txt`: Team25, Team29
* `teams36.txt`: Team21
* `teams42.txt`: Team6, Team15, Team25
* `teams48.txt`: Team6, Team23, Team47
* `teams54.txt`: Team3, Team29, Team37, Team50

Para verificar se você está retornando um certificado válido de eliminação $R$, calcule $a(R) = (w(R) + g(R)) / |R|$, onde w(R): total de vitórias dos times em $R$, $g(R)$: total de jogos restantes entre times em $R$ e $|R|$: número de times em $R$. Verifique que $a(R)$ é maior que o número máximo de jogos que o time eliminado pode vencer.

## Etapas Sugeridas

Estas são apenas sugestões para avançar no desenvolvimento. Você não é obrigado a segui-las:

1. Escreva o código para ler o arquivo de entrada e armazenar os dados.
2. Desenhe manualmente a rede `FlowNetwork` que você deseja construir para alguns exemplos pequenos. Escreva o código para construí-la, imprima usando `toString()` e confira se corresponde à sua intuição.
3. Calcule o `maxflow` e o `mincut` usando a classe `FordFulkerson`. Use `value()` para acessar o valor do fluxo e `inCut()` para identificar os vértices do lado da origem.
4. A API `BaseballElimination` contém os métodos públicos que você irá implementar. Por modularidade, inclua alguns métodos auxiliares privados.
5. Seu programa não precisa ser longo - o nosso tem menos de 200 linhas. Se ficar complicado, pare e repense a abordagem.