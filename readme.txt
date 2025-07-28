Cześć :)
W ramach zadania rekrutacyjnego zrobiłem:
1. Mniejsze usprawnienia
    Użyłem remeberSaveable przy polu input aby jego zawartość przetrwała zmiany konfiguracyjne
    Zmodyfikowałem logikę mierzenia bottomPadding - aby nie było przestrzeni między inputem a klawiaturą
2. Bugfix 2f60d3397129f027a883f3e5d73583674cd60e82
    Uzyłem remeber zamiast rememberSaveable przy usuwania Note'a
    dismissState był powiązany z `note.id` więc przy dodaniu nowego Note'a był on od razu usuwany
3. Bugfix 6b2a5f4a1aebfb97716cd4141a44adae640b2aa8
    Api calle były wykonywane na `Dispatchers.Main` co jest niezalecane. Może to powodować ANR'y.
    Dodatkowo użyłem `viewModelScope`, jest to wbudowane w viewModel rozwiązanie, które wyręcza nas
    w wielu rzeczach które przy pierwotnym rozwiązaniu mogły pójść nie tak. (wycieki pamięci)

4. Dodanie biblioteki Room
    Zależności, plik Database, Dao, DbNote
5. Dodanie biblioteki Hilt - dependency injection

6. Rozszerzenie modelu danych bazy SQLDelight o nowe pola:
    isFavourite - flaga służąca do obsługi funkcjonalności "ulubione"
    title - dodatek ode mnie w celu subtelnego rozbudowania widoku notatki
        o atrybut który ułatwia czytelność takich notatek na liście i identyfikacje :)
    + update pola isFavourite na backendzie.

7. Obsługa offline mode:
    - rozszerzenie encji DbNote:
        + lastModified <- timestamp kiedy notatka była edytowana
        + syncStatus <- pole które będzie wykorzystywane przez WorkManager
            w celu rozpoznania notatek które trzeba zsynchronizować z backendem
    - rozszerzenie Note.sq: pole lastModified

