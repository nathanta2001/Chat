# 🌐 Chat - Distributed Systems Project

Este projeto consiste em um sistema de chat multiusuário desenvolvido como parte da disciplina de **Sistemas Distribuídos**. O objetivo principal é demonstrar a comunicação entre processos em rede, gerenciamento de sessões de usuários e persistência de mensagens em um ambiente distribuído.

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3
* **Persistência:** Spring Data JPA (H2/MySQL)
* **Comunicação:** API REST com JSON
* **Gerenciamento de Dependências:** Maven

## ✨ Funcionalidades Técnicas

* **Arquitetura Cliente-Servidor:** O servidor centraliza a lógica de roteamento de mensagens e gerenciamento de grupos.
* **Persistência de Dados:** Armazenamento de nomes de usuários, mensagens e grupos.
* **Gerenciamento de Clientes Ativos:** Serviço dedicado para monitorar e listar usuários online no sistema (`ActiveClientService`).
* **Interface do Cliente:** Inclui uma implementação de interface gráfica em Java Swing para interação direta com a API.
* **Endpoints de Teste:** Arquivo `.http` incluso para validação rápida das rotas da API.

## 🏗️ Estrutura do Projeto

O sistema está dividido em pacotes que seguem as responsabilidades de um sistema distribuído:
* `server`: Contém os controladores REST e a lógica de serviço do backend.
* `client`: Implementação do lado do cliente, incluindo o serviço de consumo da API (`ChatApiService`) e diálogos de interface.
* `common`: Modelos de dados compartilhados (Mensagem, Grupo, Nomes) para garantir a consistência na serialização JSON.

## 🚀 Como Executar

1.  **Servidor:**
    Navegue até a pasta `demo` e execute:
    ```bash
    ./mvnw spring-boot:run
    ```
    O servidor iniciará por padrão na porta `8080`.

2.  **Cliente:**
    Execute a classe `ChatWindow` ou `ChatServerApplication` dentro do seu ambiente de desenvolvimento para abrir a interface de chat.

---
Projeto desenvolvido para fins acadêmicos.
Desenvolvido por [Nathan](https://github.com/nathanta2001) 🚀
