create table parametre (
    id varchar2(50) primary key,
    jour number(2) not null,
    heuredebut varchar2(8),  -- format 'HH:mm:ss'
    heurefin varchar2(8),    -- format 'HH:mm:ss'
    pourcentage number(5,2)
);

-- Insertion des parametres par jour (1=Lundi, 2=Mardi, ..., 7=Dimanche)
-- Exemple: majoration de 20% pour les heures de pointe (18h-21h) en semaine

-- Lundi
INSERT INTO parametre (id, jour, heuredebut, heurefin, pourcentage) VALUES ('PAR001', 1, '06:00:00', '12:00:00', 10.00);
