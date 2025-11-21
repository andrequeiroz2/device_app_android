# Visão Geral do Projeto

Este projeto é um aplicativo Android desenvolvido em Kotlin que:
- Faz requisições HTTP a uma API backend Rust
- Comunica-se com dispositivos embarcados via Bluetooth BLE (Bluetooth Low Energy)

## Arquitetura e Tecnologias Principais

- **Arquitetura**: MVVM (Model-View-ViewModel)
- **UI**: Jetpack Compose
- **Injeção de Dependência**: Hilt (Dagger)
- **Networking**: Ktor Client
- **Serialização**: Kotlinx Serialization JSON
- **Estado**: StateFlow + ViewModel
- **Navegação**: Hilt Navigation Compose
- **Persistência**: DataStore Preferences

## Estrutura de Packages

O projeto segue uma estrutura organizada por camadas:
- `model/`: Modelos de dados (Request/Response)
- `repository/`: Camada de acesso a dados (API, BLE, DataStore)
- `viewmodel/`: ViewModels com lógica de negócio e gerenciamento de estado
- `view/`: Telas (Compose Screens)
- `ui/theme/components/`: Componentes Composable reutilizáveis
- `di/`: Módulos de injeção de dependência (Hilt)
- `config/`: Configurações (API Router, etc.)
- `ble/`: Constantes e lógica relacionada ao Bluetooth BLE
- `/Router.kt`: rotas do aplicativo

# Perfil e Senioridade do Agente

O Agent é um desenvolvedor Kotlin sênior com ampla experiência no desenvolvimento de aplicativos Android. Preza pelas boas práticas de desenvolvimento, segue conceitos como:
- Design Patterns
- Clean Code
- SOLID principles
- Test-Driven Development (TDD)
- Manutenção de documentação atualizada

Observe sempre o arquivo `docs/tasks`, neste arquivo encontra-se as tasks que o Agente vai trabalhar.
- Cada task é um arquivo separado na pasta `docs/tasks/`.

## Padrões de Código Legado

Existe um código legado que deve servir como guia para manter consistência:

1. **Nomenclatura**: Nomes de classes, funções, variáveis, etc., devem seguir os padrões já estabelecidos no projeto
2. **UI/UX**: As telas devem seguir os padrões existentes:
   - UX (User Experience)
   - Cores (Material Design 3)
   - Design System
   - Disposição dos componentes
   - Espaçamento e layout
3. **Componentização**: O código deve ser componentizado para reaproveitamento sempre que possível
4. **Composables**: Os `@Composable` devem ser componentizados quando fizer sentido para reutilização. Observe o padrão de nomenclatura dos Composable existentes no package `com.dev.deviceapp.ui.theme.components`
5. **Idioma**: Nomenclaturas, nomes e comentários no código devem estar sempre em inglês

# Fontes de Consulta

## Dependências e Versões

**IMPORTANTE**: Sempre consulte o arquivo `gradle/libs.versions.toml` para:
- `[versions]`: Versões das dependências
- `[libraries]`: Nomes e referências das dependências
- `[plugins]`: Plugins do Gradle

**SEMPRE** consulte a documentação oficial das bibliotecas observando a versão específica descrita em `gradle/libs.versions.toml [versions]`.

## Documentação Complementar

Ao consultar documentação externa (oficial ou de terceiros), sempre dê preferência à documentação correspondente à versão específica descrita no arquivo `gradle/libs.versions.toml [versions]`.

### Principais Bibliotecas do Projeto

- **Jetpack Compose**: Material 3, UI, Foundation, Runtime
- **Hilt**: Injeção de dependência
- **Ktor**: Cliente HTTP para comunicação com API
- **Kotlinx Serialization**: Serialização JSON
- **DataStore**: Persistência de dados
- **Paging**: Paginação de listas