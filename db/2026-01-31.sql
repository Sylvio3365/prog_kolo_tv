-- ajouter colonne numero string a la table mvtCaisse
ALTER TABLE MOUVEMENTCAISSE ADD numero VARCHAR2(250);

ALTER TABLE MOUVEMENTCAISSE ADD reference VARCHAR2(250);


INSERT INTO menudynamique (id, libelle, icone, rang, niveau, ID_PERE)
VALUES ('MNDN000000200', 'Mymenu', 'fas fa-arrow-right', 1, 1, NULL);
