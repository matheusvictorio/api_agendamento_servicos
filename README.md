# Sistema de Controle de Acesso Baseado em Roles

## 📋 Visão Geral

Este documento descreve o sistema completo de autenticação e autorização implementado na API de Agendamento de Serviços, utilizando Spring Security com JWT e controle de acesso baseado em roles.

## 🔐 Arquitetura de Segurança

### Componentes Principais

1. **JWT (JSON Web Token)** - Autenticação stateless
2. **Spring Security** - Framework de segurança
3. **@PreAuthorize** - Controle de acesso em nível de método
4. **UserDetails** - Interface padrão do Spring Security
5. **Role-Based Access Control (RBAC)** - Controle baseado em papéis

### Roles Disponíveis

- **CLIENT** - Clientes que agendam serviços
- **PROVIDER** - Prestadores de serviços

## 🚀 Endpoints de Autenticação

### Registro Unificado
```http
POST /auth/register
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123",
  "phone": "11999999999",
  "userType": "CLIENT", // ou "PROVIDER"
  "cep": "01234-567",
  "number": "123",
  "complement": "Apt 45",
  "specialty": "LIMPEZA" // Obrigatório apenas para PROVIDER
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userType": "CLIENT",
  "userId": 1,
  "name": "João Silva",
  "email": "joao@email.com"
}
```

## 🔒 Matriz de Controle de Acesso

### ScheduleController (`/schedules`)

| Endpoint | Método | CLIENT | PROVIDER | Público |
|----------|--------|--------|----------|---------|
| `/` | POST | ✅ | ❌ | ❌ |
| `/{id}/confirm` | GET | ❌ | ❌ | ✅ |
| `/{id}/cancel` | DELETE/GET | ❌ | ❌ | ✅ |
| `/{id}/finalize` | POST | ❌ | ✅ | ❌ |
| `/{id}/reschedule` | PUT | ✅ | ✅ | ❌ |
| `/client` | GET | ✅ | ❌ | ❌ |
| `/provider` | GET | ❌ | ✅ | ❌ |
| `/provider/{id}/available` | GET | ✅ | ✅ | ❌ |
| `/rate` | GET | ❌ | ❌ | ✅ |

### ClientController (`/clients`)

| Endpoint | Método | CLIENT | PROVIDER | Público |
|----------|--------|--------|----------|---------|
| `/` | POST | ❌ | ❌ | ✅ |
| `/` | GET | ❌ | ✅ | ❌ |
| `/{id}` | GET | ✅* | ✅ | ❌ |
| `/{id}` | PUT | ✅* | ❌ | ❌ |
| `/{id}` | DELETE | ✅* | ❌ | ❌ |
| `/{id}/activate` | PATCH | ✅* | ❌ | ❌ |
| `/email/{email}` | GET | ❌ | ✅ | ❌ |

*✅* = Apenas o próprio cliente (authentication.principal.id == #id)

### ProviderController (`/providers`)

| Endpoint | Método | CLIENT | PROVIDER | Público |
|----------|--------|--------|----------|---------|
| `/` | POST | ❌ | ❌ | ✅ |
| `/` | GET | ✅ | ✅ | ❌ |
| `/{id}` | GET | ✅ | ✅* | ❌ |
| `/{id}` | PUT | ❌ | ✅* | ❌ |
| `/{id}` | DELETE | ❌ | ✅* | ❌ |
| `/{id}/activate` | PATCH | ❌ | ✅* | ❌ |
| `/email/{email}` | GET | ✅ | ❌ | ❌ |

*✅* = Apenas o próprio provider (authentication.principal.id == #id)

### WorkController (`/works`)

| Endpoint | Método | CLIENT | PROVIDER | Público |
|----------|--------|--------|----------|---------|
| `/` | POST | ❌ | ✅ | ❌ |
| `/` | GET | ✅ | ✅ | ❌ |
| `/{id}` | GET | ✅ | ✅ | ❌ |
| `/{id}` | PUT | ❌ | ✅ | ❌ |
| `/{id}` | DELETE | ❌ | ✅ | ❌ |

## 🔧 Como Usar no Swagger

1. **Registre-se** usando `/auth/register`
2. **Faça login** usando `/auth/login` e copie o token
3. **Clique no botão "Authorize"** (🔒) no Swagger UI
4. **Digite**: `Bearer SEU_TOKEN_AQUI`
5. **Clique "Authorize"** - agora todas as requisições incluirão o token automaticamente

## 📝 Regras de Negócio Implementadas

### Agendamentos
- **Apenas CLIENTS** podem criar agendamentos
- **Apenas PROVIDERS** podem finalizar agendamentos
- **Ambos** podem reagendar (com notificações inteligentes)
- **CLIENTS** veem apenas seus próprios agendamentos
- **PROVIDERS** veem apenas seus próprios agendamentos

### Perfis de Usuário
- **Usuários** só podem editar/excluir seus próprios perfis
- **PROVIDERS** podem ver lista de clientes
- **CLIENTS** podem ver lista de providers
- **CLIENTS** podem buscar providers por email
- **PROVIDERS** podem buscar clientes por email

### Serviços (Works)
- **Apenas PROVIDERS** podem criar/editar/excluir serviços
- **Ambos** podem visualizar serviços disponíveis

## 🔐 Segurança Implementada

### Autenticação
- **JWT Tokens** com expiração de 24 horas
- **Senhas criptografadas** com BCrypt
- **Tokens stateless** - sem sessões no servidor

### Autorização
- **@PreAuthorize** em todos os endpoints protegidos
- **Validação de propriedade** - usuários só acessam seus próprios dados
- **Roles específicas** para cada operação

### Proteções Adicionais
- **CSRF desabilitado** (API stateless)
- **CORS configurado** para desenvolvimento
- **Endpoints públicos** apenas para registro, login e confirmações por email

## 🚨 Tratamento de Erros

### Erros Comuns
- **401 Unauthorized** - Token inválido ou expirado
- **403 Forbidden** - Usuário sem permissão para a operação
- **400 Bad Request** - Dados de registro inválidos

### Mensagens de Erro
- "Credenciais inválidas" - Login incorreto
- "Email já está em uso!" - Email duplicado no registro
- "Especialidade é obrigatória para prestadores de serviço!" - Registro de provider sem specialty
- "Apenas clientes podem criar agendamentos!" - Client tentando acessar endpoint de provider

## 🔄 Fluxo de Reagendamento Inteligente

### Lógica Implementada
1. **Client reagenda** → Provider recebe email de confirmação
2. **Provider reagenda** → Client recebe email de confirmação
3. **Validação de permissão** - apenas donos do agendamento podem reagendar
4. **Status automático** - volta para "AGUARDANDO_CONFIRMACAO"

## 📊 Monitoramento e Logs

### Informações no Token JWT
- **userId** - ID do usuário autenticado
- **userType** - CLIENT ou PROVIDER
- **email** - Email do usuário (username)
- **authorities** - ROLE_CLIENT ou ROLE_PROVIDER

### Validações Automáticas
- **Token expirado** - Renovação necessária
- **Usuário ativo** - Conta não desativada
- **Role válida** - CLIENT ou PROVIDER

## 🎯 Próximos Passos

1. **Implementar refresh tokens** para renovação automática
2. **Adicionar rate limiting** para prevenir ataques
3. **Implementar auditoria** de ações dos usuários
4. **Adicionar 2FA** para maior segurança
5. **Configurar HTTPS** em produção

---

**Desenvolvido com Spring Security + JWT + @PreAuthorize**
*Sistema completo de autenticação e autorização baseado em roles*
