Help Desk — Tema 6 (GRUAPIM)

Sobre o projeto

Nosso projeto consiste em uma API REST desenvolvida em Java 21, utilizando Spring Boot 3.5.7, para ajudar a organizar os chamados de suporte de TI de uma rede de escolas.

A ideia surgiu a partir do problema apresentado no Tema 6: muitas solicitações de suporte acabam sendo feitas por e-mail, telefone ou pessoalmente, o que dificulta o acompanhamento dos atendimentos. Com isso, alguns chamados podem ser esquecidos, atendidos por mais de um técnico ou ficar sem um responsável definido.

Para resolver essa situação, desenvolvemos um sistema que permite registrar e acompanhar os chamados desde sua abertura até a resolução.

O sistema possui três tipos de usuários: solicitante, técnico e supervisor. Cada um tem suas próprias permissões e responsabilidades.

Nosso foco foi atender aos requisitos do trabalho da maneira mais simples possível, utilizando os conceitos aprendidos em aula, como Spring Security, JWT, DTOs, serviços, repositórios, validações e testes automatizados.

Como a proposta do trabalho é desenvolver apenas o back-end, não criamos uma interface gráfica própria. Para visualizar e testar as funcionalidades, utilizamos o Swagger.

1. Como o sistema funciona

O funcionamento do Help Desk é relativamente simples.

Quando um funcionário da escola precisa de suporte, ele pode se cadastrar e abrir um chamado, informando o problema, a categoria e a prioridade.

A partir da categoria escolhida, o sistema identifica qual equipe será responsável pelo atendimento.

O chamado entra na fila dessa equipe e fica disponível para que um técnico possa assumi-lo.

Também implementamos algumas regras para organizar melhor os atendimentos. Por exemplo, cada técnico pode ter no máximo cinco chamados em atendimento ao mesmo tempo.

Além disso, quando um técnico precisa de mais informações, ele pode colocar o chamado em espera. Nesse período, o prazo de atendimento fica pausado e só volta a contar quando o solicitante responde.

Depois que o problema é resolvido, o solicitante pode avaliar o atendimento com uma nota de 1 a 5 ou reabrir o chamado dentro de sete dias.

O supervisor acompanha todo esse processo, podendo alterar prioridades, redistribuir chamados, cadastrar equipes e consultar relatórios.

2. Tecnologias utilizadas

Para desenvolver o projeto, utilizamos:

Java 21: linguagem principal utilizada no desenvolvimento.

Spring Boot 3.5.7: facilita a criação e a organização da API.

Spring Security: controla a autenticação e as permissões dos usuários.

JWT: permite identificar o usuário autenticado e seu perfil.

Spring Data JPA: facilita a comunicação com o banco de dados.

H2 Database: banco de dados escolhido por ser simples de configurar.

Swagger: permite visualizar e testar os endpoints.

JUnit e Mockito: utilizados nos testes automatizados.

JaCoCo: gera relatórios de cobertura dos testes.

Maven: gerencia as dependências e a compilação do projeto.

Docker: permite executar a aplicação em um ambiente configurado.

Escolhemos essas tecnologias porque atendem aos requisitos do professor e permitem desenvolver uma aplicação organizada, sem adicionar complexidades desnecessárias.

3. Como executar o projeto

O projeto pode ser iniciado de duas formas: utilizando o Docker ou executando diretamente pelo Maven.

Opção 1 — Executar pelo Docker

Para essa opção, é necessário ter o Docker Desktop instalado.

Primeiro, copie o arquivo .env.example e crie um arquivo chamado .env.

Nesse arquivo, configure a variável JWT_SECRET com uma chave de pelo menos 32 caracteres. A chave de exemplo não deve ser utilizada em produção.

Depois, abra o terminal na pasta do projeto e execute:

docker compose up --build

Esse comando prepara o ambiente e inicia a aplicação.

Quando estiver funcionando, o Swagger poderá ser acessado pelo endereço:

http://localhost:8080/swagger-ui.html

Para encerrar a execução, utilize Ctrl + C ou:

docker compose down

Os dados ficam armazenados no banco H2, utilizando o volume helpdesk-dados.

Caso seja necessário apagar todos os dados desse ambiente de desenvolvimento, execute:

docker compose down -v

Observação: a configuração do Docker faz parte do projeto, mas sua execução ainda precisa ser validada em um ambiente com Docker disponível.

Opção 2 — Executar pelo Maven

Para executar sem Docker, é necessário ter o Java e o Maven instalados.

Abra o terminal na pasta helpdesk_tema6, onde está localizado o arquivo pom.xml.

Execute:

mvn spring-boot:run

Esse comando inicia a aplicação pelo Spring Boot.

Após a inicialização, acesse:

http://localhost:8080/swagger-ui.html

No nosso ambiente de desenvolvimento, também utilizamos o Java 25 para compilar e executar os testes, mantendo o projeto configurado para Java 21.

4. Usuários de teste

Para facilitar os testes, o sistema já possui um supervisor e um técnico cadastrados inicialmente.

Perfil:
Supervisor

E-mail:
supervisor@escola.com

Senha:
Senha123!

Perfil:
Técnico

E-mail:
tecnico@escola.com

Senha:
Senha123!

Perfil:
Solicitante

E-mail:
Definido no cadastro

Senha:
Definida no cadastro

O solicitante precisa criar sua própria conta utilizando o endpoint /api/auth/cadastro.

Na inicialização, o sistema também cria uma equipe chamada TI, uma categoria chamada Computadores e os prazos padrão de atendimento.

As senhas são armazenadas utilizando BCrypt, para evitar que fiquem salvas diretamente em texto.

Esses usuários foram criados apenas para facilitar os testes e a apresentação do trabalho. Em um ambiente real, seria necessário alterar as credenciais e reforçar as configurações de segurança.

5. Como testar as funcionalidades pelo Swagger

O Swagger permite testar os recursos da API sem precisar desenvolver um site.

Para começar, podemos simular um funcionário que precisa de suporte.

Primeiro, realizamos o cadastro pelo endpoint /api/auth/cadastro, informando nome, e-mail, setor e senha.

Depois, utilizamos /api/auth/login para entrar no sistema.

Quando o login é realizado corretamente, a API devolve um token JWT. Esse token deve ser informado no botão Authorize do Swagger para acessar os endpoints protegidos.

Com o solicitante autenticado, podemos consultar as categorias disponíveis e abrir um chamado.

Por exemplo, podemos cadastrar um problema com o título "Computador não liga", informar a descrição "A tela permanece preta" e escolher a prioridade ALTA.

Depois disso, podemos entrar no sistema como técnico e assumir o chamado.

O técnico poderá adicionar comentários, registrar notas internas, colocar o chamado em espera ou informar a solução.

Quando o atendimento estiver resolvido, o solicitante poderá avaliar o serviço ou reabrir o chamado dentro do prazo permitido.

Por último, podemos entrar como supervisor para consultar os relatórios, alterar prioridades e acompanhar a distribuição dos chamados.

Essa sequência permite demonstrar o funcionamento das principais funcionalidades do sistema.

6. Endpoints disponíveis

Os endpoints representam as operações que podem ser realizadas pela API.

Todos utilizam o prefixo /api.

Método:
POST

Endpoint:
/auth/cadastro

Responsável:
Público

Método:
POST

Endpoint:
/auth/login

Responsável:
Público

Método:
POST

Endpoint:
/config/equipes

Responsável:
Supervisor

Método:
GET

Endpoint:
/config/equipes

Responsável:
Autenticado

Método:
POST

Endpoint:
/config/categorias

Responsável:
Supervisor

Método:
GET

Endpoint:
/config/categorias

Responsável:
Autenticado

Método:
POST

Endpoint:
/config/tecnicos

Responsável:
Supervisor

Método:
PUT

Endpoint:
/config/prazos/{prioridade}

Responsável:
Supervisor

Método:
GET

Endpoint:
/config/prazos

Responsável:
Autenticado

Método:
POST

Endpoint:
/chamados

Responsável:
Solicitante

Método:
GET

Endpoint:
/chamados

Responsável:
Autenticado

Método:
GET

Endpoint:
/chamados/{id}

Responsável:
Usuário autorizado

Método:
POST

Endpoint:
/chamados/{id}/assumir

Responsável:
Técnico

Método:
POST

Endpoint:
/chamados/{id}/comentarios

Responsável:
Solicitante ou técnico

Método:
GET

Endpoint:
/chamados/{id}/comentarios

Responsável:
Usuário autorizado

Método:
GET

Endpoint:
/chamados/{id}/historico

Responsável:
Usuário autorizado

Método:
POST

Endpoint:
/chamados/{id}/aguardar

Responsável:
Técnico

Método:
POST

Endpoint:
/chamados/{id}/transferir

Responsável:
Técnico

Método:
POST

Endpoint:
/chamados/{id}/resolver

Responsável:
Técnico

Método:
POST

Endpoint:
/chamados/{id}/fechar

Responsável:
Solicitante

Método:
POST

Endpoint:
/chamados/{id}/reabrir

Responsável:
Solicitante

Método:
PATCH

Endpoint:
/chamados/{id}/prioridade

Responsável:
Supervisor

Método:
PATCH

Endpoint:
/chamados/{id}/atribuir

Responsável:
Supervisor

Método:
POST

Endpoint:
/chamados/processar-pendentes

Responsável:
Supervisor

Método:
GET

Endpoint:
/relatorios/prazos

Responsável:
Supervisor

Método:
GET

Endpoint:
/relatorios/tempos

Responsável:
Supervisor

Método:
GET

Endpoint:
/relatorios/notas

Responsável:
Supervisor

Os endpoints possuem diferentes permissões para garantir que cada usuário consiga realizar apenas as operações permitidas ao seu perfil.

7. Exemplos de informações enviadas à API

Para realizar algumas operações, precisamos enviar informações em formato JSON.

Cadastro de solicitante:

{"nome":"Ana","email":"ana@escola.com","setor":"Secretaria","senha":"Senha123!"}

Login:

{"email":"ana@escola.com","senha":"Senha123!"}

Cadastro de equipe:

{"nome":"Redes","descricao":"Equipe responsável pela rede"}

Cadastro de categoria:

{"nome":"Internet","descricao":"Problemas de conexão","equipeId":1}

Cadastro de técnico:

{"nome":"Caio","email":"caio@escola.com","setor":"TI","senha":"Senha123!","equipeId":1}

Alteração de prazo:

{"horas":24}

Abertura de chamado:

{"titulo":"Sem internet","descricao":"Computador da sala 3 sem conexão","categoriaId":1,"prioridade":"ALTA"}

Comentário:

{"texto":"Verifiquei a conexão do computador","interno":false}

Registro de solução:

{"texto":"O cabo de rede foi substituído"}

Transferência:

{"equipeId":2,"justificativa":"O problema precisa ser atendido pela equipe de redes"}

Avaliação:

{"nota":5}

Alteração de prioridade:

{"prioridade":"CRITICA","justificativa":"O problema está impedindo a realização das aulas"}

Reatribuição:

{"tecnicoId":2,"justificativa":"Redistribuição dos atendimentos"}

Esses exemplos podem ser utilizados diretamente no Swagger, ajustando os IDs conforme os dados cadastrados.

8. Como atendemos aos requisitos do Tema 6

Durante o desenvolvimento, utilizamos os 14 requisitos funcionais do enunciado como referência para organizar as funcionalidades.

Requisito

O que foi implementado

Onde está no projeto

RF01

Cadastro de solicitantes com validação dos dados

AuthService e Dto

RF02

Login e autenticação por JWT

AuthService e SecurityConfig

RF03

Cadastro de equipes, técnicos e categorias

ConfiguracaoService

RF04

Definição e alteração dos prazos por prioridade

ConfiguracaoService e PrazoPrioridade

RF05

Abertura de chamados e definição da equipe

ChamadoService.abrir()

RF06

Técnico assume chamado respeitando o limite de cinco

ChamadoService.assumir()

RF07

Comentários públicos e notas internas

ChamadoService.comentar()

RF08

Pausa do prazo e tratamento de chamados abandonados

ChamadoService.aguardar() e processar()

RF09

Transferência entre equipes e registro no histórico

ChamadoService.transferir()

RF10

Resolução, avaliação, fechamento e reabertura

ChamadoService

RF11

Alteração de prioridade e reatribuição

ChamadoService

RF12

Relatório de chamados próximos ou fora do prazo

RelatorioService.alertas()

RF13

Tempo médio de resolução por equipe e categoria

RelatorioService.tempos()

RF14

Nota média dos técnicos

RelatorioService.notas()

Também implementamos regras para proteger as informações dos usuários.

Por exemplo, um solicitante só pode acessar seus próprios chamados e não consegue visualizar as notas internas registradas pelos técnicos.

As operações que alteram chamados utilizam transações para manter a consistência dos dados e do histórico.

9. Organização dos arquivos

Organizamos o projeto em pastas para separar as responsabilidades de cada parte do sistema.

config — Configurações

Contém as classes responsáveis pela segurança, documentação da API e criação dos dados iniciais.

SecurityConfig.java: configura a autenticação JWT e as permissões.

SwaggerConfig.java: configura a documentação da API.

CargaInicial.java: cria os usuários, equipes, categorias e prazos iniciais.

model — Modelos

Contém as classes que representam as principais informações do sistema, como usuário, chamado, equipe, categoria, comentário e histórico.

dto — Transferência de dados

A pasta DTO contém os objetos utilizados para receber e devolver informações pela API.

Isso permite controlar quais dados serão enviados ao usuário, evitando, por exemplo, que a senha seja devolvida na resposta de um cadastro.

repository — Acesso ao banco

Contém as classes e interfaces responsáveis pela consulta e pelo armazenamento das informações no banco de dados.

service — Regras de negócio

É uma das partes mais importantes do projeto.

Nela estão as regras que determinam o que o sistema pode ou não fazer.

Por exemplo, o ChamadoService.java controla a abertura, atribuição, transferência, resolução e reabertura dos chamados.

Também temos o RegraDePrazos.java, responsável pelos cálculos de prazos, e o RelatorioService.java, que organiza as informações dos relatórios.

controller — Endpoints

Contém as classes que recebem as requisições feitas à API e encaminham as operações para os serviços responsáveis.

exception — Tratamento de erros

Centraliza as exceções e define as respostas quando ocorre algum problema, como dados inválidos, falta de permissão ou chamado inexistente.

src/test — Testes

Contém os testes automatizados utilizados para verificar as regras e funcionalidades do sistema.

src/main/resources — Configurações da aplicação

Contém o arquivo application.properties, utilizado para configurar o banco de dados e outras propriedades do Spring Boot.

diagramas — Diagramas UML

Contém os diagramas de casos de uso e de classes, incluindo os arquivos editáveis e as imagens.

pom.xml

É o arquivo utilizado pelo Maven para gerenciar as dependências, configurar a compilação, executar os testes e gerar o relatório JaCoCo.

Dockerfile e docker-compose.yml

São os arquivos responsáveis pela configuração do ambiente Docker.

README.md

É este documento, que reúne as principais informações sobre o projeto e explica como executar e testar a aplicação.

10. Decisões que tomamos no desenvolvimento

Durante o projeto, procuramos utilizar soluções simples, mas que fossem suficientes para atender às regras do sistema.

Uma dessas decisões foi utilizar o banco de dados H2, porque ele não exige a instalação de um servidor de banco separado. Como o trabalho é acadêmico, essa escolha facilita os testes e a execução.

Também decidimos utilizar uma única classe Usuario e diferenciar os usuários pelo atributo Perfil.

Assim, não precisamos criar três estruturas de autenticação diferentes para solicitante, técnico e supervisor.

Outra decisão foi separar as regras de negócio dos controllers. Dessa forma, os controllers ficam responsáveis por receber as requisições, enquanto os serviços verificam as condições necessárias para realizar cada operação.

Para controlar os prazos, utilizamos datas e durações. Quando um chamado entra em espera, registramos o início da pausa. Quando o solicitante responde, calculamos quanto tempo ficou parado e acrescentamos esse período ao vencimento.

Também consideramos que um técnico não pode ultrapassar cinco chamados em atendimento. Se ele já estiver no limite quando o solicitante responder a um chamado em espera, esse chamado volta para a fila da equipe.

No caso dos chamados abandonados, o processamento é realizado pelo supervisor, conforme previsto no enunciado.

Todas essas decisões foram tomadas pensando em manter o código organizado, compreensível e fácil de modificar.

11. Testes automatizados e JaCoCo

Para verificar o funcionamento do sistema, criamos testes unitários e funcionais.

Os testes unitários verificam regras específicas, como cálculos de prazo e restrições de acesso.

Já os testes funcionais simulam requisições à API, permitindo conferir se os endpoints respondem corretamente às operações realizadas.

Utilizamos JUnit, Mockito e MockMvc para essa etapa.

Os testes ficam organizados principalmente nos arquivos:

RegraDePrazosTest.java

AutorizacaoServiceTest.java

ApiFluxoTest.java

Para executá-los, utilizamos o comando:

mvn clean verify

Após corrigirmos uma inconsistência encontrada em um teste de prazo, conseguimos executar a compilação e os testes automatizados com sucesso, obtendo a mensagem BUILD SUCCESS.

Também utilizamos o JaCoCo para acompanhar a cobertura do código.

No relatório mais recente, alcançamos:

88% de cobertura de instruções.

57% de cobertura de decisões.

Esses resultados mostram que boa parte do código foi executada durante os testes, mas ainda existem caminhos alternativos que podem ser mais bem explorados.

O relatório pode ser encontrado em:

target/site/jacoco/index.html

Consideramos essa etapa importante porque não basta apenas desenvolver as funcionalidades. Também precisamos verificar se elas apresentam o comportamento esperado, principalmente em situações em que alguma regra do sistema é violada.

12. Considerações finais

Com o desenvolvimento do Help Desk, conseguimos aplicar na prática vários conceitos estudados durante a disciplina, principalmente a criação de APIs REST, organização em camadas, autenticação, persistência de dados e testes automatizados.

Uma das partes mais importantes foi implementar as regras de negócio, já que o sistema precisava fazer mais do que simplesmente cadastrar, consultar e atualizar informações.

Precisamos considerar diferentes perfis de acesso, limites de atendimento, prioridades, pausas de prazo, transferências e avaliações.

Também percebemos a importância dos testes durante o desenvolvimento. Foi por meio deles que identificamos uma inconsistência na expectativa de prazo de um teste e conseguimos corrigi-la.

O projeto está compilando e passando pelos testes automatizados que implementamos. Ainda é importante realizar a validação completa da execução pelo Docker e conferir os fluxos da aplicação no Swagger.

De modo geral, buscamos desenvolver uma solução que atendesse ao Tema 6, mantendo o código organizado e utilizando uma estrutura que conseguíssemos compreender, explicar e modificar durante a apresentação.

13. Referências

O projeto foi desenvolvido com base no enunciado Avaliação GRUAPIM — Tema 6: API de Help Desk, disponibilizado pelo professor Giovani Disperati.

Também utilizamos os conteúdos e exemplos apresentados no material da disciplina:

https://giovanidisperati.github.io/GRUAPIM/

Os diagramas de classes e casos de uso utilizados como referência estão disponíveis na pasta diagramas do projeto.