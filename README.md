# JPA BOX OS EXTREME — JPA Store

Primeira versão Android TV nativa em Kotlin, Android 6/API 23 ou superior. Interface horizontal, menu ASSISTIR TV, seleção persistente do aplicativo principal, busca de apps com atividade de abertura, informações de versão, solicitação de desinstalação pelo sistema, painel técnico com PIN criado no primeiro acesso, armazenamento/RAM, histórico local e limpeza do cache próprio.

O PIN possui salt aleatório e PBKDF2 (120 mil iterações); não existe PIN padrão. O painel bloqueia ao sair do aplicativo. A primeira configuração deve ser realizada pelo proprietário. O PIN protege configurações locais; não substitui bloqueio do sistema, administração de dispositivos nem proteção contra acesso físico/ADB. O histórico é local, limitado e não é uma trilha de auditoria inviolável.

## Baixar e instalar pelo celular

1. Abra https://github.com/lendajpa/Jpastore/actions e selecione uma execução **Android APK** com resultado verde.
2. Em Artifacts, baixe **JPA-BOX-TESTE** com sua conta GitHub e extraia o ZIP.
3. Transfira o APK à TV Box e abra pelo gerenciador de arquivos. Autorize esse gerenciador como fonte de instalação quando o Android solicitar.
4. Abra **JPA BOX • TESTE**, entre em Configurações, crie e confirme um PIN e selecione o app de TV instalado.
5. Selecione **Definir como launcher padrão** e confirme no Android, quando suportado.

O APK debug é exclusivamente de testes. A disponibilidade dos artefatos depende da conclusão do Actions e do prazo de retenção do GitHub. Nenhum APK está incluído no código-fonte.

## Build e validação

O CI fornece Java 17, Android SDK e Gradle 8.9; executa testes JVM do PIN, lint Android e assembleDebug. Localmente, com esses requisitos instalados: `gradle :android-launcher:testDebugUnitTest :android-launcher:lintDebug :android-launcher:assembleDebug`.

Para release, configure secrets `JPA_KEYSTORE_BASE64`, `JPA_STORE_PASSWORD`, `JPA_KEY_ALIAS`, `JPA_KEY_PASSWORD` e execute manualmente o workflow com release habilitado. Mantenha cópia segura da chave; atualizações exigem a mesma assinatura. Debug e release usam identificadores separados.

## Teste no aparelho

- Navegue por setas/OK/Voltar em todas as telas; verifique foco azul e rolagem com muitos apps.
- Configure o app de TV, reinicie o launcher e confirme persistência e abertura.
- Remova o app de TV e verifique a mensagem de indisponibilidade.
- Confirme rejeição de PIN incorreto, troca de PIN e bloqueio após sair.
- Teste HOME e reinicialização: o Android inicia o HOME escolhido; não forçamos abertura em segundo plano. Alguns firmwares impedem launcher padrão.
- Confira armazenamento, versões, cancelamento de desinstalação e limpeza somente do cache próprio.

## Escopo e arquitetura

Views Android nativas foram escolhidas para reduzir dependências e manter compatibilidade com boxes antigas; controles focáveis permitem D-pad sem toque. Sem firmware modificado, root, instalação silenciosa ou remoção de componentes. A lista mostra apps com atividade LAUNCHER/LEANBACK visível ao Android, não inventário irrestrito de serviços/pacotes. Não há permissão ampla QUERY_ALL_PACKAGES.

O repositório estava vazio e não continha o mascote oficial; foi usado um símbolo geométrico de reprodução, sem criar um personagem substituto.

Gerenciamento remoto, pareamento, catálogo, instalação de APK, políticas ESSENCIAL/PLUS/OPCIONAL, otimização avançada e laboratório ficam para próximas etapas. Sincronizar informa explicitamente que não há serviço configurado. Um backend futuro deverá usar HTTPS, autenticação por dispositivo, autorização por função, hashes de APK e estados separados para solicitado/aguardando confirmação/instalado. Nenhuma instalação remota é simulada.
