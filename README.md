# Travailler avec AWS en local

## SAM

### Présentation

- Acronyme
- SAM peut créer un conteneur Docker pour héberger la lambda.

### Initialisation

- Les modèles d'initialisation disponibles sont fonction du langage
- On peut aussi simplement déposer un `template.yml` dans le dossier racine du projet

### Template

- `CodeUri`: emplacement du `build.gradle`
- `Handler`: `package.class::method`

### Cycle de développement

- `sam validate`: validation du `template.yml`
- `sam build`: création du `.aws-sam` qui contient le nécessaire pour un déploiement,
- `sam local invoke HelloWorldFunction --event events/some-event.json`

Le refresh automatique ne fonctionne que pour les [langages interprétés](https://github.com/aws/aws-sam-cli/issues/921),
donc il faut relancer un build à chaque fois que l'on modifie le template ou le code de la lambda

### Intégration dans l'IDE

Deux manières de faire:

- depuis le template
- depuis le handler

Dans les deux cas bien vérifier la configuration d'exécution si on a l'impression que ça ne fonctionne pas bien...

### Debugging distant

Utiliser l'option `--debug-port 5005`, puis s'attacher au port exposé par le debugger

## Développement sur une vraie lambda: `ManageSubscriptionState`

- Pour ne pas avoir la plupart des fichiers qui concernent SAM au même endroit, j'ai créé à la racine
  du projet
  - un dossier `local` qui contient tous les fichiers nécessaires
  - un fichier de configuration `samconfig.toml` qui permet aux lignes de commande `sam build` et `sam local invoke` de
    continuer à fonctionner sans avoir à saisir trop d'arguments dans la CLI
- Utilisation de profils Spring pour activer des bouchons sur Athena, DynamoDB, SQS, ...
- Utilisation d'outils qui simulent en local certains services:
  - DynamoDB: utiliser l'image fournie
    par [Amazon](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html)
  - Autres services: pour l'instant pas de solution fournie par Amazon. L'outil le plus avancé semble être
    [LocalStack](https://localstack.cloud/), mais il a plusieurs inconvénients
    - Il est open source pour certains services seulement (Athena par exemple n'est inclus que dans la version pro)
    - Ça fait un outil de plus à maitriser
    - Il change encore souvent et contient pas mal de bugs

Pour passer des variables d'environnement à la lambda, il faut

- qu'elles soient définies dans le `template.yaml`
- utiliser l'argument `--env-vars myvars.json`

Par exemple:

```shell
sam local invoke ManageSubscriptionState --event local/events/manage-subscription-state-trigger.json --env-vars local/environments/env.json
```

## Utilisation de `dynamodb-local`

On veut que le conteneur dynamodb et le conteneur créé par SAM soient dans le même réseau docker pour pouvoir
communiquer sans problème.

### Créer un réseau docker

```shell
docker network create sam-local
```

### Créer le conteneur dynamodb

```shell
docker run -p 8000:8000 --name dynamodb-local --network=sam-local amazon/dynamodb-local
```

### Créer une table dans dynamodb

```shell
aws dynamodb create-table --table-name feelings_table --attribute-definitions AttributeName=feelingId,AttributeType=S --key-schema AttributeName=feelingId,KeyType=HASH --billing-mode PAY_PER_REQUEST --endpoint-url http://localhost:8000
```

### Lister les tables

```shell
aws dynamodb list-tables --endpoint-url http://localhost:8000
```

### Scanner une table

```shell
aws dynamodb scan --table-name feelings_table --endpoint-url http://localhost:8000
```

Questions:

- On voit l'image avec un `docker images` mais pas le conteneur créé par SAM en faisant un `docker container ls -a`
- Si message du genre `No response from invoke container for ManageSubscriptionState`, utiliser `Timeout`
- Changement du port du serveur Tomcat de Spring, sinon erreur port déjà utilisé en debug. Le lancement du debug rentre
  en conflit avec le serveur Tomcat ?
- Pas trouvé comment désactiver l'utilisation du parameter store

A creuser:

- Initialisation
  avec [CDK](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-cdk-getting-started.html)
  permettrait de déclarer l'infra avec du code plutôt que du YAML
- Utilisation
  de [TF](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/terraform-support.html)
- [AWS Toolkit for Jetbrains](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)

## Resources

- [SAM reference documentation](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html)
- [Repository des templates SAM](https://github.com/aws/aws-sam-cli-app-templates)
- [A Java lambda project](https://github.com/aws-samples/aws-sam-java-rest)
- [DynamoDb local](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
