# Étape 23 — préparation intégration backend réelle

## 23.1 Revue des repositories existants

- **Auth (`AuthService`)**: déjà exposé par interface, mais la construction dans `KoriApp` était couplée à `MockAuthService`.
- **Profiles (`ProfileRepository`)**: contrat déjà propre côté domaine (`RoleProfilePayload`), mais implémentation mock directement injectée.
- **Dashboard (`DashboardRepository`)**: contrat clair, couplage mock au niveau composition racine.
- **Transactions (`TransactionRepository`)**: `TransactionQuery` est déjà un bon modèle de requête domaine.
- **Payments/Actions (`ClientTransferRepository`, `MerchantTransferRepository`, `AgentActionRepository`)**: API repository cohérente, mais pas de séparation explicite entre repository et source de données.

## 23.2 Séparation DTO / Domain / UI

Dans la base actuelle, les modèles `core.model.*` jouent le rôle de modèles domaine partagés. Pour éviter de sur-architecturer:

- On conserve les modèles actuels en tant que **modèles domaine**.
- On prépare des **data sources** dédiées pour accueillir ensuite des DTO Retrofit.
- Les mappings DTO -> domaine seront localisés dans les futures implémentations `remote` des data sources.

## 23.3 Structure remplaçable des data sources

Ajouts:

- `data.datasource.AuthDataSource`
- `data.datasource.ProfileDataSource`
- `data.datasource.DashboardDataSource`
- `data.datasource.TransactionDataSource`
- `data.datasource.ClientTransferDataSource`
- `data.datasource.MerchantTransferDataSource`
- `data.datasource.AgentActionDataSource`

Et des repositories d’orchestration `data.repository.impl.*`.

Conséquence: le passage mock -> réel devient mécanique en remplaçant un binding data source, sans modifier ViewModels/UseCases.

## 23.4 Gestion d’erreurs réseau standardisée

Ajout d’un socle commun:

- `NetworkError` (HTTP, parsing, timeout, connectivité, métier backend, unknown)
- `NetworkErrorMapper.fromThrowable`
- `NetworkErrorMapper.toPresentation` pour séparer message technique et UX
- `BackendBusinessException` pour erreurs métier backend

Ce pattern est prêt à être branché dans chaque future data source Retrofit.

## 23.5 DI et transition progressive

Ajout d’un container d’application:

- `KoriAppContainer` expose les dépendances applicatives via interfaces.
- `KoriAppContainerFactory` + `RepositoryBindingMode` centralisent les bindings.
- `MockKoriAppContainer` garde les mocks fonctionnels.

Le switch futur se fera par mode/binding (global ou feature par feature), sans couplage ViewModels -> mocks.
