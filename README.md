# QR Code Generator

API REST em **Java 21 + Spring Boot** que recebe um texto, gera um QR Code em PNG e armazena a imagem em um bucket do **AWS S3**, devolvendo o link público do arquivo. A aplicação roda em container **Docker**.

Projeto de estudo, baseado no vídeo da [Fernanda Kipper](https://www.youtube.com/@Fernanda.Kipper) e adaptado por mim durante meu primeiro contato com a AWS.

## Como funciona

1. O cliente envia um `POST /qrcode` com o texto desejado.
2. A aplicação gera o QR Code em memória usando a biblioteca ZXing.
3. A imagem PNG é enviada ao S3 com um nome único (UUID).
4. A API responde com a URL pública da imagem.

## Tecnologias

- Java 21
- Spring Boot 3.4.4 (Spring Web)
- ZXing 3.5.2 (geração do QR Code)
- AWS SDK for Java v2 2.24.12 (cliente S3)
- Maven
- Docker (build multi-stage)

## Arquitetura

O projeto segue a ideia de **ports and adapters** para isolar o armazenamento do resto da aplicação:

- `StoragePort`: interface que define o contrato de upload de arquivos.
- `S3StorageAdapter`: implementação da porta usando o AWS S3.
- `QrCodeGeneratorService`: gera o QR Code e depende apenas da porta, sem conhecer o S3.

Assim, trocar o S3 por outro armazenamento exige criar um novo adapter, sem alterar o service.

## Estrutura do projeto

```
src/main/java/com/github/obrenoxs/qrcode/generator
├── controller      # Endpoint REST
├── dto             # Request e response (records)
├── infrastructure  # Adapter do S3
├── ports           # Interface de armazenamento
├── service         # Regra de geração do QR Code
└── Application.java
```

## Pré-requisitos

- Docker instalado
- Conta na AWS
- (Opcional, para rodar sem Docker) JDK 21 e Maven

## Configuração da AWS

1. **Bucket no S3**: crie um bucket na mesma região configurada na aplicação (padrão `us-east-1`).
2. **Acesso público de leitura** (necessário para o link da imagem abrir no navegador): desative o bloqueio de acesso público do bucket e adicione a política abaixo, trocando `SEU-BUCKET` pelo nome do seu bucket.

   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Principal": "*",
         "Action": "s3:GetObject",
         "Resource": "arn:aws:s3:::SEU-BUCKET/*"
       }
     ]
   }
   ```

   > Atenção: isso torna todos os arquivos do bucket legíveis por quem tiver o link. Use um bucket dedicado ao projeto e não envie informações sensíveis.

3. **Usuário IAM** exclusivo para a aplicação, sem acesso ao console, com a política em linha abaixo (permite apenas enviar arquivos ao bucket):

   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": "s3:PutObject",
         "Resource": "arn:aws:s3:::SEU-BUCKET/*"
       }
     ]
   }
   ```

4. Gere uma **chave de acesso** para esse usuário e guarde o ID e a chave secreta.

## Variáveis de ambiente

Copie o arquivo de exemplo e preencha com seus valores:

```bash
cp .env.example .env
```

| Variável | Descrição |
|---|---|
| `AWS_ACCESS_KEY_ID` | ID da chave de acesso do usuário IAM |
| `AWS_SECRET_ACCESS_KEY` | Chave secreta do usuário IAM |
| `AWS_S3_REGION` | Região do bucket (ex.: `us-east-1`) |
| `AWS_S3_BUCKET_NAME` | Nome do bucket |

O arquivo `.env` está no `.gitignore` e **nunca deve ser commitado**.

## Como executar

```bash
docker build -t qrcode-generator .
docker run --env-file .env -p 8080:8080 qrcode-generator
```

A API fica disponível em `http://localhost:8080`.

## Usando a API

**Requisição**

```bash
curl -X POST http://localhost:8080/qrcode \
  -H "Content-Type: application/json" \
  -d '{"text":"https://github.com/obrenoxs"}'
```

**Resposta**

```json
{
  "url": "https://SEU-BUCKET.s3.us-east-1.amazonaws.com/<uuid>"
}
```

Abra a URL no navegador para ver o QR Code ou escaneie com o celular.

## Segurança

- As credenciais da AWS são passadas ao container apenas em tempo de execução (`--env-file`), e não ficam gravadas na imagem Docker.
- O usuário IAM da aplicação tem permissão mínima: somente `s3:PutObject` no bucket do projeto.
- Se uma chave for exposta por engano, desative e apague-a imediatamente no IAM.

## Próximos passos

- Tratamento de erros com `@RestControllerAdvice`
- Validação do texto recebido
- Testes automatizados
- Autenticação na API
- Deploy em um servidor na nuvem

## Créditos

Projeto inspirado no tutorial da Fernanda Kipper.

## Autor

**Breno Oliveira de Souza** · [github.com/obrenoxs](https://github.com/obrenoxs)
