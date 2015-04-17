Avete mai sognato di installare un vostro applicativo scritto in Java come servizio di Windows? Sognato forse no, ma avuto bisogno probabilmente si! In questo post vedremo come fare.
I servizi di windows

Nonostante tutto il male che si possa volere a Windows, sia per buoni motivi che per prese di posizione, certe cose le fa davvero bene. Una di queste è, a mio avviso, la gestione dei servizi. Se non ci siete mai stati, andiamo a dare un’occhiata in Pannello di Controllo -> Strumenti di Amministrazione -> Servizi.
Vi trovate davanti tutto l’elenco degli strumenti utili al sistema operativo per funzionare. Windows li chiama servizi perché effettivamente non sono applicazioni che richiedono l’interazione con l’utente, ma appunto “servono” ad eseguire certi scopi o a supportare altre applicazioni. Infatti le voci marcate con Avvio Automatico sono quelle che verranno eseguite all’avvio della macchina, ancor prima che qualsiasi utente effettui il log in.
Andando poi nelle proprietà di uno di questi servizi, una delle voci più interessanti è quella di Ripristino: Windows infatti può attivare un sistema di watchdog sui suoi servizi:

e decidere cosa fare nel caso in cui un servizio termini inaspettatamente. Le opzioni sono:

> Non fare niente;
> Riavvia il servizio;
> Esegui programma;
> Riavvia il Computer.

Con pochi passaggi si riesce quindi a configurare un servizio per riavviarsi nel caso un cui fallisca.

Il concetto analogo nel mondo Unix è quello dei demoni, ma il sistema di gestione richiede diverse skills in materia e varia da distribuzione a distribuzione.
Demoni benevoli


Anche in Java viene usata la terminologia di demone: il termine indica un certo tipo di thread che fa da provider di servizi agli altri thread (chiamati user thread) che girano sullo stesso processo. Il metodo run() del thread demone di solito contiene un loop infinito in attesa di richieste da parte di altri thread che deve servire. E’ possibile trasformare un thread in un demone impostando setDaemon(true) prima che venga lanciato. Un thread marcato come demone non viene considerato dalla JVM come un processo utente: infatti se ad un certo punto sono attivi solo thread demoni, la JVM termina. Questo ha senso perché se non ci sono più thread da servire, i demoni non sono più utili e il programma può terminare. Per questo motivo è bene non usare i thread demoni per implementare la logica di un applicativo (che appunto sarebbe user thread), ma eventualmente solo per qualche suo servizio sottostante che può essere terminato in modo sicuro e improvviso.

Nonostante il termine demone sia comune al mondo Unix e Java, non lasciamoci trarre in inganno dalla terminologia. I demoni (e quindi i servizi nel mondo Windows) sono fornitori di servizi per il sistema operativo. Nel mondo Java invece un thread demone è un fornitore di servizi per gli altri thread: non si può quindi presupporre che i demoni Java siano servizi per il sistema operativo!! Per ottenere in Java un comportamento di questo tipo abbiamo bisogno di due elementi:

> una classe sempre attiva (con un loop infinito per esempio)
> un modo per dialogare con il sistema dei servizi in Windows (o demoni in Unix)

Una classe sempre attiva

Sin dalla notte dei tempi è noto che per avere un codice interminabile, qualsiasi sia il linguaggio, basta scrivere un loop infinito del tipo:



---

while (true) {
> System.out.println("I'm not going to stop!");
}

---


Questo utilissimo loop scritto in Java ci ricorderà svariate volte al secondo che non terminerà mai! In realtà un meccanismo del genere era davvero usato anche in Java fino alla versione 1.4 con qualche accorgimento: l’argomento del ciclo non era una costante ma una variabile synchronized che poteva assumere il valore false per permettere di terminare il loop.

Ammettiamo di avere bisogno di un servizio che scrive in console ogni 30 secondi. Il servizio sarà esclusivamente composto dalla classe:


---

public class JavaService {

> private static final Logger logger =
> > Logger.getLogger(JavaService.class);


> private static JavaService
> > service = new JavaService();


> public static void main(String args[.md](.md)) {
> > if(args.length == 1 && "start".equalsIgnoreCase(args[0](0.md))) {
> > > service.start();

> > }
> > else if(args.length == 1 && "stop".equalsIgnoreCase(args[0](0.md))) {
> > > service.stop();

> > }
> > else {
> > > logger.info("Required param: start or stop");

> > }

> }

> private boolean stopped = false;

> public void start() {
> > stopped = false;


> while(!stopped) {

> synchronized(this) {
> > try {
> > > logger.info("I'm alive!");
> > > this.wait(30000);

> > }
> > catch(InterruptedException e){
> > > logger.error(e.getMessage());

> > }

> }
> }
> }

> public void stop() {
> > stopped = true;
> > synchronized(this) {
> > > this.notify();

> > }

> }
}

---

Fino a quando qualche altro thread non interviene a impostare stopped=true, il servizio rimane sempre attivo. Un occhio attento potrebbe obiettare: “Ma che stai di’?? Se richiamo il main() passando il parametro stop sono in un’altra istanza! Come faccio a fermare quella che è partita prima??”. Ricordate che abbiamo detto che abbiamo bisogno di un sistema per dialogare con i servizi di Windows? Ebbene, il sistema che discuteremo a breve riesce a fare questa magia: richiama il main() su un altro thread della stessa JVM passando il parametro stop sulla stessa istanza avviata precedentemente con il parametro start.
Executors

Dalla versione 5 di Java in poi si riesce ad ottenere lo stesso servizio in modo molto più elegante e con un maggior controllo sui thread grazie all’Executors Framework. Si tratta di una nuova caratteristica di Java 5 pensata per semplificare la vita nella creazione di applicazioni multithread. Permette infatti di gestire facilmente la concorrenza senza mai invocare direttamente la classe Thread.
L’entry point del framework è la classe statica Executors: non è altro che una factory di diverse tipologie di thread che restituiscono due tipi di servizi:

> ExecutorService: gestiscono singoli thread o pool di thread che accettano task da esguire immediatamente;
> ScheduledExecutorService: gestiscono singoli thread o pool di thread che accettano task eseguiti ogni intervallo di tempo predefinito o dopo un certo ritardo.

In entrambe i casi è possibile gestire situazioni di questo tipo:

> è possibile far eseguire task di tipo Runnable quando non ci si aspetta nessun risultato dal task oppure Callable quando invece si necessita del risultato dell’elaborazione;
> nei casi di pool di thread, ogni richiesta che arriva e che eccede il numero di thread disponibili viene messa automaticamente in coda, liberando il programmatore da questa problematica.

Come si usano

Per creare un singolo thread basta eseguire:


---

ExecutorService executor =
> Executors.newSingleThreadExecutor();
executor.execute(runnable);
executor.shutdown();


---

Invece per pianificare un task che si ripete ogni minuto:



---

ScheduledExecutorService executor =
> Executors.newSingleThreadScheduledExecutor();
executor.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.MINUTES);
executor.shutdown();

---


Da notare la chiamata al metodo executor.shutdown(): se infatti il servizio executor non viene chiuso, questo rimane sempre attivo e di conseguenza il thread che esegue questi frammenti di codice non viene mai rilasciato e l’applicazione non terminerà mai!

Vediamo adesso come applicarlo al nostro esempio:

---

public class JavaService {

> private static final Logger logger =
> > Logger.getLogger(JavaService.class);


> private static JavaService
> > service = new JavaService();


> private ScheduledExecutorService executor;

> public static void main(String args[.md](.md)) {
> > if(args.length == 1 && "start".equalsIgnoreCase(args[0](0.md))) {
> > > service.start();

> > }
> > else if(args.length == 1 && "stop".equalsIgnoreCase(args[0](0.md))) {
> > > service.stop();

> > }
> > else {
> > > logger.info("Required param: start or stop");

> > }

> }

> public void start() {
> > executor = Executors.newSingleThreadScheduledExecutor();
> > executor.scheduleAtFixedRate(new ScheduledService(), 0, 30, TimeUnit.SECONDS);

> }

> public void stop() {
> > if (executor != null) {
> > > executor.shutdown();

> > }

> }
}

---

public class ScheduledService implements Runnable {

> private static final Logger logger =
> > Logger.getLogger(ScheduledService.class);


> @Override
> public void run() {
> > logger.info("I'm alive!");

> }
}

---


Confrontandola con la precedente, si nota immediatamente che non abbiamo più bisogno della variabile semaforo per terminare il servizio: basta chiamare in modo naturale il metodo stop(); per terminare il servizio. Inoltre abbiamo anche disaccoppiato il codice che gestisce il servizio (JavaService) da quello che implementa il task da eseguire (ScheduledService).

Abbiamo quindi creato una classe che rimane sempre attiva senza ricorrere ad un ciclo infinito: adesso è pronta per essere installata come servizio Windows. Ci manca però un adapter che permetta a Windows di comunicare con la nostra classe e passare i parametri giusti a seconda degli eventi start/stop del servizio. In circolazione ne esistono diversi, ma quello che già dal nome non ha certo bisogno di presentazioni è Apache Common Daemons, lo stesso usato per installare Tomcat come servizio sotto Windows.
Dialogare con il sistema operativo: Common Daemons

Si tratta di un progetto costituito da due componenti: una scritta in C che ha il compito di interfacciarsi con il sistema operativo e una API scritta in Java per modellare demoni. La API è molto esigua (viene dal bootstrap di Tomcat 4) e non è necessaria al funzionamento della parte scritta in C per cui ne possiamo anche fare a meno. Le piattaforme supportate sono:

> Windows, tramite Procrun;
> Unix, tramite Jsvc.

Adesso sappiamo cosa studiare per dialogare con Windows!
Wrapping con Procrun

Procrun è un insieme di applicazioni costituito da 2 eseguibili:

> Prunmgr: è un pannello di controllo che permette di monitorare il servizio in modo grafico. Ricordate il pannello di gestione del servizio di Tomcat? Si è proprio lui…
> Prunsrv: è il servizio vero e proprio. Windows crederà di dialogare esclusivamente con questo eseguibile quando il servizio è installato: in realtà è solo un wrapper della nostra classe Java.

La documentazione sul sito è molto dettagliata per cui sarebbe inutile farne un copia/incolla. Merita invece, partendo da un esempio pratico, focalizzarci su alcuni parametri importanti da usare durante l’installazione del servizio.

> La prima cosa da fare è rinominare il file prunsrv.exe con un nome che ricorda il nostro servizio (per esempio JavaService.exe) perché è questo che vedremo nel TaskManager di Windows. Creiamo poi una cartella, per esempio windowsService, dove copieremo gli eseguibili di Procrun e i file batch che andremo a creare.
> Creiamo poi un file di nome install.bat che conterrà le istruzioni necessarie all’installazione del servizio.
> Per installare la nostra classe come servizio il nostro batch conterrà il seguente codice:


---


> set JAVA\_SERVICE\_PATH=C:\CoseNonJaviste\JavaService
> set PR\_CLASSPATH=%JAVA\_SERVICE\_PATH%\dist\JavaService.jar;%JAVA\_SERVICE\_PATH%\dist\lib\**> JavaService.exe //IS//JavaService --Install=%JAVA\_SERVICE\_PATH%\dist\windowsService\JavaService.exe --Description="Java Service" --StartPath=%JAVA\_SERVICE\_PATH% --Jvm=auto --StartMode=jvm --StartClass=it.cosenonjaviste.daemons.JavaService --StartMethod=main --StartParams=start --StopMode=jvm --StopClass=it.cosenonjaviste.daemons.JavaService --StopMethod=main --StopParams=stop --LogPath=%JAVA\_SERVICE\_PATH%\log --LogLevel=Error --StdOutput=auto --StdError=auto**


---

> La prima cosa che si nota è che i percorsi devono essere assoluti, altrimenti il servizio non funziona. Per semplicità quindi è stata creata la variabile JAVA\_SERVICE\_PATH.
> Un attributo fondamentale è --StartPath: definisce il percorso di root del vostro servizio (che altrimenti andrebbe a finire sotto system32 della cartella windows!!).
> Se si vuole, come in questo caso, che i messaggi di start e stop vadano sullo stesso processo in corso, ovvero sulla stessa istanza della classe JavaService come accennato precedentemente, usare --StartMode e --StopMode sempre valorizzati a jvm.
> Per quanto riguarda il log, il file definito da --StdOutput corrisponde alla console del vostro ambiente di sviluppo, o alla finestra del DOS se si eseguisse la nostra classe normalmente. Attenzione quindi ad impostare correttamente gli appenders del log4j.
> Una volta installato, possiamo avviarlo dal pannello dei servizi di Windows o tramite il monitor di Apache. In questo caso creiamo un altro file, per esempio monitor.bat, con il seguente codice:

---


> start prunmgr.exe //MS//JavaService /B

---


> in modo tale che apra il pannello di controllo del servizio di nome JavaService come definito durante l’installazione e chiuda immediatamente la finestra DOS.

Conclusioni

Abbiamo visto che con l’Executors Framework è molto facile scrivere classi che si prestano ad essere installate come servizi. Una volta poi presa dimestichezza con procrun vedrete che ha tutto il necessario per riuscire a controllare e personalizzare il vostro nuovo servizio Windows scritto in Java!
Potrebbero anche interessarti:

> I 10 articoli di CoseNonJaviste più letti nel 2013
> Partecipa al concorso "Scrivi un articolo per CoseNonJaviste e vinci un premio"
> Java 8: lambda in 7 minuti (o quasi)
> JSONP e jQuery: conosciamoli meglio

Related posts:

> Logging in Java applications using Log4j Log4J è una libreria Java sviluppata dalla Apache Software Foundation...
> Java Collections – Parte I In questa prima parte del tutorial sulle collection Java dopo...
> Sviluppare Applicazioni (Java) Web Cluster Aware – Parte I Molti Application Server (da IBM WebSphere a JBoss AS) offrono...