# Movie Rating Platform

A Java application for rating and tracking movies. The data access layer is built with **JDBC** on top of a **Microsoft SQL Server** database, and an important part of the business logic lives in the database itself as **stored procedures and triggers**. Everything is verified with automated tests.

## What it does

The platform allows you to:

- manage **genres** and **movies** (a movie can belong to multiple genres),
- attach **tags** to movies,
- let users **rate** movies and keep a personal **watchlist**,
- automatically track each movie's **average rating** and **trend** (Classic, Rising, Falling, Regular),
- automatically assign each user a **profile description** (curious, focused, undefined),
- reward users and block suspicious rating behavior using **stored procedures and triggers**,
- compute **thematic specializations** and **movie recommendations**.

## Project structure

### Java classes

Each class implements one interface from the test library:

| Class | Responsibility |
|---|---|
| `GeneralOperations` | Wipes all data from the database (`eraseAll`) |
| `GenresOperations` | Add, update, delete, and look up genres |
| `MoviesOperations` | Movies, their genres, directors, and movie trend |
| `RatingsOperations` | Add, update, delete, and look up ratings |
| `TagsOperations` | Movie tags (add, remove, search) |
| `UsersOperations` | Users, rewards, descriptions, specializations, and recommendations |
| `WatchlistsOperations` | User watchlists |
| `StudentMain` | Test entry point (`TestHandler` + `TestRunner`) |

### SQL scripts

| Script | Content |
|---|---|
| `create_db.sql` | Drops and creates all tables |
| `procedures.sql` | Stored procedure `SP_REWARD_USER_MAIN` |
| `triggers.sql` | Triggers `TR_BLOCK_EXTREME_MAIN`, `TR_UPDATE_MOVIE_TREND_MAIN`, `TR_UPDATE_USER_DESCRIPTION_MAIN` |

## Database model

| Table | Description |
|---|---|
| `Korisnik` | Users: `korisnickoIme` (username), `brojNagrada` (number of rewards, default `0`), `opis` (description, default `'undefined'`) |
| `Zanr` | Genres: `naziv` (name) |
| `Tag` | Tags: `naziv` (name) |
| `Film` | Movies: `naslov` (title), `reziser` (director), `status` (trend, nullable), `prosecnaOcena` (average rating, default `0.000`) |
| `Pripada` | Movie ↔ genre link |
| `FilmTag` | Movie ↔ tag link |
| `Ocena` | Ratings: `ocena` (score), `idKorisnik`, `idFilm`, `vreme` (timestamp, defaults to the current time) |
| `ListaGledanja` | Watchlist: `idKorisnik`, `idFilm`, `status` |

All foreign keys use `ON UPDATE CASCADE ON DELETE NO ACTION`, so dependent rows must be deleted before their parents. The Java layer takes care of this order (for example when removing a movie or a user, or when erasing all data).

## Stored procedures and triggers

A significant part of the logic is implemented on the database side rather than in Java.

### `SP_REWARD_USER_MAIN(idKorisnik, idFilm)`

Called from `RatingsOperations` after every successfully added or updated rating. It increments the user's `brojNagrada` (rewards) by one when **all** of the following hold:

1. the user has rated **at least 10 movies**,
2. in at least one genre of the rated movie, the user's **average rating in that genre is 8.0 or higher**,
3. the **average rating given by all other users** to this movie is **below 6.0**.

If nobody else has rated the movie, no reward is given.

### `TR_BLOCK_EXTREME_MAIN` (on `Ocena`, insert/update)

Prevents users from repeatedly giving only extreme scores within a genre. A new or updated rating is **rejected** (the transaction is rolled back) when:

- the rating is **extreme** (1 or 10), and
- the user already has **more than 3 extreme ratings** (1 or 10) on other movies of the same genre, and
- the user has **fewer than 3 neutral ratings** (6 to 8) on other movies of the same genre.

The Java layer reports such an operation as unsuccessful.

### `TR_UPDATE_MOVIE_TREND_MAIN` (on `Ocena`, insert/update/delete)

Keeps the movie's `prosecnaOcena` and `status` up to date after any change to its ratings:

- `prosecnaOcena` is recalculated as the average of all ratings (`0` if there are none).
- If the movie has **no ratings**, `status` is set to `NULL`.
- Otherwise `status` becomes:
  - **`Classic`**: at least 3 ratings and an average of 8.0 or higher,
  - **`Rising`**: the average of the 5 most recent ratings is at least 1.0 **above** the overall average,
  - **`Falling`**: the average of the 5 most recent ratings is at least 1.0 **below** the overall average,
  - **`Regular`**: none of the above.

The checks are applied in that order, so `Classic` takes priority over `Rising` and `Falling`.

### `TR_UPDATE_USER_DESCRIPTION_MAIN` (on `Ocena`, insert/update)

Updates the user's `opis` (description) based on how many different movies and tags the user has rated:

| Distinct movies rated | Distinct tags across rated movies | Description |
|---|---|---|
| 10 or more | 10 or more | `curious` |
| 10 or more | fewer than 10 | `focused` |
| fewer than 10 | any | `undefined` |

## Business rules (Java layer)

- A **new movie** is inserted with status `Inicijalan` and an average rating of `0.0`, and must have at least one genre. Its status is later managed by `TR_UPDATE_MOVIE_TREND_MAIN`.
- A new **user** starts with `0` rewards and the description `undefined`.
- A user can **rate** a movie only once; the rating can be updated afterwards.
- **Watchlist**: a movie is added with status `Gledace`, with no duplicates.
- **Deleting a movie** also deletes all its links (genres, tags, ratings, watchlist entries).
- **Deleting a user** also deletes their ratings and watchlist.
- A user's **thematic specializations** are tags for which the user rated at least 2 movies with a score of 8 or higher.
- **Recommendations from favorite genres**: movies from genres where the user's average rating is at least 8, which the user has neither rated nor added to their watchlist, and which are:
  - rated at least 4 times with an average of at least 7.5, or
  - rated fewer than 4 times with an average of at least 9.0.

  Results are sorted by average rating descending, then by ID ascending.

## Tech stack

- Java
- JDBC
- Microsoft SQL Server (T-SQL tables, stored procedure, and triggers)
