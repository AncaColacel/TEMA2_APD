# TEMA2_APD_Planificarea de task-uri intr-un datacenter

## DETALII IMPLEMENTARE
In vederea realizarii acestei teme am lucrat in fisierele MyDispatcher.java si MyHost.java unde am implementat logica enuntata in cerinte.

## DETALII IMPLEMENTARE
Voi detalia in cele ce urmeaza codul scris in cele 2 fisiere mentionate mai sus.


### MyDispatcher
Aceasta clasa implementeaza clasa abstracta Dispatcher. Am scris in cadrul metodei addTask pentru fiecare din cele 4 politici. Pun cate un if in care verific daca algoritmul este unul din cei patru specifici celor 4 politici din ENUM-ul pus la dispozitie.

***1) ROUND ROBIN (RR)***
Daca s-a ales aceasta politica incep cu prezumtia cum ca primul task este asignat primului host din lista de hosti si marchez cu o variabila faptul ca am asignat primul task. Ulterior daca nu e vorba de primul, ma asigur ca asignez taskuri hostilor tinand seama de formula precizata in cerinta.
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

Metoda de add este o metoda sincronizata pentru ca obtineam race condition atunci cand alegeam hostii.

### MyHost
In cadrul acestei clase metoda de addTask este de asemenea esentiala. Practic in dispatcher cand un task este asignat unui host se apeleaza aceasta metoda care adauga taskul in coada acelui host.
Hostul fiind un Thread avem o metoda de run() unde se implementeaza logica hostului. Folosesc cozi blocante, BlockingQueue, ca sa ma ajute cu sincronizarea. Se scoate un task un coada si se introduce in coada de executie. Parcurg cu un iterator coada de taskuri ca sa verific prioritatile taskurilor viitoare in cazul in care trebuie sa se execute un task cu prioritate mai mare in fata celui curent sau daca cel curent e preemtibil si atunci se executa partial si lasa unul mai important in fata. Daca nu e indeplinita niciuna din aceste conditii se executa taskul si ma folosesc de un while in care verific ca getLeft ul sa fie mai mare decat 0 si calculez timpul ramas utilizand functia System.currentTimeMillis() din Java ca sa setez mereu timpul ramas. Cand un task e gata il scot din coada de executie si ii dau finish() ca sa mi se actualizeze timpul. Executia in sine e o asteptare si dau wait() cand e gata taskul su dupa ce dau finish() dau si notify() pentru a incheia asteptarea.

La partea de ***prioritate*** ma asigur ca nu am un task cu prioritate mai mare care asteapta in coada si daca am il execut pe acesta si atunci task_curent devine next_task si bag taskul curent inapoi in coada ca sa-l execut mai tarziu.

La partea de ***preemty*** verific ca taskul curent sa fie preemtibil si daca da, las un task cu prioritate mai mare si am facut o functie de execute_partial() pentru asta. Cand taskul e in executie verific daca apare vreun task cu prioritate mai mare si daca da, opresc taskul curent ca sa-l las pe acesta sa se execute si fac sleep() pt el.

Alte functii auxiliare implementate sunt:

#### -> public int lungime_cozi() pentru lungimea celor 2 cozi

#### -> public void executePartialTask(Task task_curr, Task next_task) pentru preemty

#### -> public  boolean isPreemptible(Task task) True/False daca e preemty sau nu un task
