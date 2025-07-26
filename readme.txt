Cześć :)
W ramach zadania rekrutacyjnego zrobiłem:
1. Bugfix(ish) c902e0c6632d458085ce344124cd6c7c49d17629
    Użyłem remeberSaveable przy polu input aby jego zawartość przetrwała zmiany konfiguracyjne
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