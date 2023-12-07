# TEMA2_APD_Planificarea de task-uri intr-un datacenter

## DETALII IMPLEMENTARE
In vederea realizarii acestei teme am lucrat in fisierele MyDispatcher.java si MyHost.java unde am implementat logica enuntata in cerinte.

## DETALII IMPLEMENTARE
Voi detalia in cele ce urmeaza codul scris in cele 2 fisiere mentionate mai sus.


***1) MyDispacther***
Aceasta clasa implementeaza clasa abstracta Dispatcher. Am scris in cadrul metodei addTask pentru fiecare din cele 4 politici. Pun cate un if in care verific daca algoritmul este unul din cei patru specifici celor 4 politici din ENUM-ul pus la dispozitie.
***-> ROUND ROBIN (RR)***
Daca s-a ales aceasta politica incep cu prezumtia cum ca primul task este asignat primului host din lista de hosti si marchez cu o variabila faptul ca am asignat primul task. Ulterior daca nu e vorba de primul, ma asigura ca asignez taskuri hostilor tinand seama de formula precizata in cerinta.
```
(i + 1)%n

```
i este id-ul hostului ales anterior, iar n este numarul de hosti in total.
Updatez apoi indicele pentru a fi ok calculul la taskul urmator si asa mai departe.


***2) Shortest Queue (SQ)***
Daca s-a ales aceasta politica ma asigur ca aleg hostul cu coada minima si in felul asta am creat in MyHost o metoda care calculeaza lungimea celor 2 cozi, intrucat fiecare host detine o coada cu taskuri asignate lui dar inca in asteptare, precum si o coada unde sunt introduse taskurile cand intra in executie si in felul acesta tin cont de toate taskurile, asa cum mentioneaza politica.
Se calculeaza minimul si se face asignarea.

***3) Size Interval Task Assignment (SITA)***
Pentru aceasta politica lucrurile stau si mai simplu. Avem 3 tipuri de taskuri, scurte, medii & lungi si tot atatia hosti. Fiecare task este asignat unui host in felul urmator:
-> H0 primeste taskurile usoare
-> H1 primeste taskurile medii
-> H2 primeste  taskurile lungi
Fac asta cu 3 if-uri.

***4) Least Work Left (LWL)***
La aceasta politica asignez taskul curent acelui host cu cel mai putin de executat. Am o metoda de getWorkLweft care calculeaza cantitatea de munca ramasa pentru acel thread si ma folosesc de aceasta metoda ca sa aleg hostul. Aceasta metoda parcurge mai intai coada cu taskuri aflate in asteptare si aduna getDuration-ul la fiecare dupa care parcurge coada cu taskuri aflate in executie si pentru un task aflat in executie nu ma mai intereseaza durata initiala pentru ca din acesta se scade in timp ce taskul se executa asa ca mi-o scot din suma si adun in schimb getLeft adica punctual cat mai are taskul meu pana e gata si in felul asta am la orice moment de timp o durata pt un host anume.
