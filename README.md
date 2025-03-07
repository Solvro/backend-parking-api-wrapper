# Parkingi API
![img.png](icon.png)

## Wrapper do API parkingów Politechniki Wrocławskiej
### Żadne rozwiązanie informatycznie nie jest wieczne. Nie inaczej przedstawia się sprawa z API do parkingów naszej Uczelni. Dane w przestarzałym formacie, jeden endpoint, który zwraca je wszystkie bez ładu i składu… Na szczęście powstało narzędzie, które opakowuje, modernizuje i znacząco ulepsza system Politechniki.



#### Czas trwania: Od 11.2024 do 03.2025
#### Kategoria: wrapper do API
#### Technologie: Spring Boot 3.3, PostgreSQL

### Jakie ulepszenia wprowadzamy?
Udostępniamy wszystkie dane w ustrukturyzowanej formie JSONów, upewniając się, że brakujące parametry parkingów i ujemne wolne miejsca parkingowe nie będą straszyć użytkownika, a wiadomością o możliwym błędzie będzie więcej niż złowrogie 400 BAD_REQUEST.
Pozwalamy filtrować i sortować dostępne parkingi ze względu na ich parametry (nazwę, symbol, ilość wolnych miejsc parkingowych, godziny otwarcia) oraz ze względu na ich lokalizację.
### Jakie nowe funkcjonalności wprowadzamy?
Tutaj najlepiej przedstawić serię krótkich historii i odpowiedzi na zawarte w nich problemy…

- H1: Jesteś studentem i od zawsze parkujesz na swoim ulubionym parkingu, ale pewnego dnia nagle zajęcia masz w innym budynku po drugiej stronie miasta. Gdzie zaparkujesz? Gdzie znajdziesz wolne miejsce o 13:05 we wtorek lub 17:30 w piątek?
- A1: Nic trudnego, dzięki naszemu narzędziu z łatwością uzyskasz średnią ilość wolnych miejsc z ostatnich kilku tygodni na danym parkingu w dany dzień i godzinę.
  

- H2: Zajmujesz się zbadaniem wpływu wydarzeń organizowanych na Politechnice na życie studentów i pracowników uczelni. Albo planujesz nowe wydarzenie i nie wiesz czy parkingi Politechniki wystarczą dla planowanej liczby gości, ale za to wiesz, że kilka miesięcy temu odbyło się coś podobnego… A może po prostu jesteś ciekawy, ile było wolnych miejsc na parkingu Wrońskiego o godzinie 18:41 dnia 1 stycznia ubiegłego roku?
- A2: Takie informacje również udostępniamy, oczywiście, zaczynając od czasu od kiedy nasza aplikacja działa.
  

- H3: Jesteś ciekaw o co ludzie pytają nasz serwer najczęściej? Kiedy pytają najczęściej?
- A3: Nie powiemy ci kto dokładnie, ale ktoś\* średnio przynajmniej raz w tygodniu o trzeciej w nocy szuka najbliższego
  mu otwartego parkingu naszej uczelni i dostaje odpowiedź 404 NOT_FOUND. Nie wierzysz? Skorzystaj z statystyk zapytań
  do serwera i zobacz sam o co, kiedy i z jakim skutkiem inni pytają najczęściej.  
  \* - „ktoś” został wymyślony na rzecz historii, ale naprawdę możesz takie rzeczy sprawdzić.

### Zespół:
- Ignacy Smoliński; GitHub: Leadman5555 (Project Manager, Tech Lead)
- Mateusz Płaska; GitHub: mateusz-plaska (Backend developer)
-  Dominik Korwek; GitHub: dominikkorwek  (Backend developer)
- Jan Samokar; GitHub: sxlecquer (Backend developer)
- Aliaksei Samoshyn; GitHub: Kawaban (Wsparcie w tworzeniu tasków)
 

