# Sistema de Documentação de Roles e Tasks para IA

Este diretório contém a estrutura de documentação para gerenciar **roles** (papéis/habilidades) da IA e suas **tasks** (tarefas).

## Estrutura

```
docs/
├── README.md (este arquivo)
├── roles/
│   ├── role_main.md          # ⭐ ROLE ATIVO - habilidades e conhecimentos da IA
│   └── role_template.md      # Template para criar novos roles
└── tasks/
    ├── README.md             # Índice de tasks
    ├── template.md           # Template para criar novas tasks
    └── *.md                  # Tarefas individuais
```

## Como Funciona?

### 1. Roles (Habilidades da IA)

Os arquivos em `roles/` definem o perfil, habilidades, conhecimentos e comportamentos esperados da IA.

- **`role_main.md`**: Role principal ativo que a IA deve seguir
  - Define perfil técnico (ex: "Desenvolvedor Kotlin Sênior")
  - Lista tecnologias que a IA domina
  - Estabelece padrões de código
  - Define comportamento esperado

- **`role_template.md`**: Template para criar novos roles quando necessário

**⚠️ IMPORTANTE**: A IA sempre consulta `roles/role_main.md` antes de executar qualquer tarefa.

### 2. Tasks (Tarefas)

Cada task é um arquivo markdown separado em `tasks/` contendo:
- Descrição detalhada da tarefa
- Status e prioridade
- Requisitos técnicos e funcionais
- Detalhamento da implementação
- Notas e dúvidas

**Formato de nomeação**: Use nomes descritivos, ex: `nome_da_funcionalidade.md`

## Workflow Passo a Passo

### Para Definir/Atualizar o Role da IA:
1. Edite `roles/role_main.md`
2. Ajuste perfil, habilidades, padrões ou comportamentos
3. A IA usará essas informações em todas as próximas tasks

### Para Criar uma Nova Task:
1. Copie `tasks/template.md` como base
2. Renomeie com nome descritivo (ex: `criar_tela_login.md`)
3. Preencha todos os campos:
   - Descrição do que fazer
   - Requisitos técnicos
   - Status (Pending/In Progress/Completed/Blocked)
   - Prioridade (Baixa/Média/Alta/Crítica)
   - Detalhamento da implementação

### Para Executar uma Task:
1. A IA lê automaticamente `roles/role_main.md` → entende suas habilidades
2. A IA lê a task específica em `tasks/` → entende o que fazer
3. A IA executa a task seguindo as diretrizes do role

## Exemplo Prático

### 1. Definir Role
Edite `docs/roles/role_main.md`:
```markdown
## Perfil
Você é um desenvolvedor Android especialista em Kotlin e Jetpack Compose.
```

### 2. Criar Task
Crie `docs/tasks/criar_tela_login.md`:
```markdown
# Criar Tela de Login
- Status: Pending
- Prioridade: Alta

## Objetivo
Criar uma tela de login usando Jetpack Compose seguindo os padrões do projeto.
```

### 3. Solicitar Execução
"Por favor, execute a task em `docs/tasks/criar_tela_login.md`"

A IA lerá ambos os arquivos e executará a task!

## Arquivos Principais

| Arquivo | Descrição | Quando Usar |
|---------|-----------|-------------|
| `roles/role_main.md` | Habilidades e perfil da IA | Sempre que quiser ajustar o perfil da IA |
| `roles/role_template.md` | Template para novos roles | Ao criar roles alternativos |
| `tasks/template.md` | Template de task | Ao criar uma nova task |
| `tasks/README.md` | Índice de tasks | Para ver quais tasks existem e seus status |

## Dicas Importantes

- ✅ **Mantenha o `role_main.md` atualizado** com as tecnologias e padrões do projeto
- ✅ **Use nomes descritivos** para as tasks (ex: `login_screen.md`, `ble_communication.md`)
- ✅ **Preencha todos os campos** do template para melhor clareza
- ✅ **Use a seção "Dúvidas"** nas tasks quando houver incertezas que precisam ser resolvidas
- ✅ **Atualize o status** da task conforme o progresso (Pending → In Progress → Completed)

## Templates Disponíveis

- `roles/role_template.md` - Para criar novos roles
- `tasks/template.md` - Para criar novas tasks

---

**Última atualização**: 23/11/2025

