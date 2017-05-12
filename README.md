# gmd

## TODO
1. _Indexation full text de HPO, ATC, STITCH et OMIM_
2. _Recherche full text dans HPO, ATC, STITCH et OMIM_
3. Accès aux bases de données OrphaData, Sider et HPO Annotations
4. Mapping des données pour retrouver maladies et médicaments associés à des noms de signes et symptômes
5. Écriture d'une requête avec l'opérateur logique ET
6. Présentation des résultats à l'utilisateur
7. Fonctionnalités supplémentaires

## Initialisation de l'application
1. Créer un fichier de configuration ```.gmd``` en modifiant les chemins de façon appropriée :
```
omimData: /Users/moshi/data/omim/omim.txt
omimOntoData: /Users/moshi/data/omim/omim_onto.csv
stitchData: /Users/moshi/data/stitch/chemical.sources.v5.0.tsv
atcData: /Users/moshi/data/atc/br08303.keg
hpoData: /Users/moshi/data/hpo/hp.obo
omimIndex: /Users/moshi/data/index/omim/
omimOntoIndex: /Users/moshi/data/index/omimOnto/
stitchIndex: /Users/moshi/data/index/stitch/
atcIndex: /Users/moshi/data/index/atc/
hpoIndex: /Users/moshi/data/index/hpo/
```
2. Ajouter les librairies ```.jar``` se trouvant dans le répertoire ```lib/```
3. Dans la classe ```App.java```, appeler la méthode ```initialize()```

## Champs indexés

### HPO
1. id
2. name
3. altId
4. def
5. comment
6. synonym
7. xref
8. isA

### ATC
1. ATCCode
2. label

### STITCH
1. compoundId
2. ATCCode

### OMIM

#### OMIM_ONTO
1. label
2. synonyms
3. CUIs

#### OMIM
1. content