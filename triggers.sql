CREATE OR ALTER TRIGGER TR_BLOCK_EXTREME_MAIN
ON Ocena
FOR INSERT, UPDATE
AS
BEGIN
	DECLARE @brojEkstremnih int
	DECLARE @brojNeutralnih int

	DECLARE @korisnik int
	DECLARE @zanr int
	DECLARE @ocena int

	SELECT @korisnik = i.idKorisnik, @zanr= p.idZanr, @ocena=i.ocena
	FROM inserted i JOIN Pripada p ON i.idFilm = p.idFilm

	SELECT @brojEkstremnih=COUNT(*)
	FROM Korisnik k JOIN Ocena o ON k.idKorisnik=o.idKorisnik JOIN Film f ON o.idFilm=f.idFilm JOIN Pripada p ON f.idFilm=p.idFilm JOIN Zanr z ON p.idZanr=z.idZanr
	WHERE o.ocena IN(1,10) AND k.idKorisnik=@korisnik AND z.idZanr=@zanr AND o.idFilm <> (SELECT idFilm FROM inserted);

	SELECT @brojNeutralnih=COUNT(*)
	FROM Korisnik k JOIN Ocena o ON k.idKorisnik=o.idKorisnik JOIN Film f ON o.idFilm=f.idFilm JOIN Pripada p ON f.idFilm=p.idFilm JOIN Zanr z ON p.idZanr=z.idZanr
	WHERE o.ocena BETWEEN 6 AND 8 AND k.idKorisnik=@korisnik AND z.idZanr=@zanr AND o.idFilm <> (SELECT idFilm FROM inserted);

	IF @ocena IN (1,10)
	BEGIN
		IF @brojEkstremnih>3 AND @brojNeutralnih<3
		BEGIN
			ROLLBACK TRANSACTION;
		END
	END
END;
GO


CREATE OR ALTER TRIGGER TR_UPDATE_MOVIE_TREND_MAIN
ON Ocena
FOR INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @film INT;
    DECLARE @brojOcena INT;
    DECLARE @trenutniKorisnik INT;
    DECLARE @prosekOcena DECIMAL(10,3);
    DECLARE @prosekPoslednjih5 DECIMAL(10,3);

    IF EXISTS (SELECT 1 FROM inserted)
        SELECT TOP 1 @film = idFilm FROM inserted;
    ELSE
        SELECT TOP 1 @film = idFilm FROM deleted;

    IF @film IS NULL RETURN;

    SELECT 
        @brojOcena = COUNT(*),
        @prosekOcena = ISNULL(AVG(ocena * 1.0), 0.000)
    FROM Ocena
    WHERE idFilm = @film;

    UPDATE Film 
    SET prosecnaOcena = @prosekOcena 
    WHERE idFilm = @film;

    IF @brojOcena = 0
    BEGIN
        UPDATE Film SET status = NULL WHERE idFilm = @film;
        RETURN;
    END

    SELECT @prosekPoslednjih5 = AVG(poslednjih5.ocena * 1.0)
    FROM (
        SELECT TOP 5 ocena 
        FROM Ocena 
        WHERE idFilm = @film 
        ORDER BY vreme DESC
    ) AS poslednjih5;
	
    IF @brojOcena >= 3 AND @prosekOcena >= 8.0
    BEGIN
        UPDATE Film SET status = 'Classic' WHERE idFilm = @film;
    END
    ELSE IF @prosekPoslednjih5 >= @prosekOcena + 1.0
    BEGIN
        UPDATE Film SET status = 'Rising' WHERE idFilm = @film;
    END
    ELSE IF @prosekPoslednjih5 <= @prosekOcena - 1.0 
    BEGIN
        UPDATE Film SET status = 'Falling' WHERE idFilm = @film;
    END
    ELSE
    BEGIN
        UPDATE Film SET status = 'Regular' WHERE idFilm = @film;
    END


END
GO

CREATE OR ALTER TRIGGER TR_UPDATE_USER_DESCRIPTION_MAIN
ON Ocena
FOR INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @korisnik INT;
    DECLARE @brojFilmova INT;
    DECLARE @brojTagova INT;

    SELECT @korisnik = idKorisnik FROM inserted;

    IF @korisnik IS NULL RETURN;

    SELECT @brojFilmova = COUNT(DISTINCT idFilm)
    FROM Ocena
    WHERE idKorisnik = @korisnik;

    SELECT @brojTagova = COUNT(DISTINCT ft.idTag)
    FROM Ocena o
    JOIN FilmTag ft ON o.idFilm = ft.idFilm
    WHERE o.idKorisnik = @korisnik;

    IF @brojFilmova >= 10 AND @brojTagova >= 10
    BEGIN
        UPDATE Korisnik 
        SET opis = 'curious' 
        WHERE idKorisnik = @korisnik;
    END
    ELSE IF @brojFilmova >= 10 AND @brojTagova < 10
    BEGIN
        UPDATE Korisnik 
        SET opis = 'focused' 
        WHERE idKorisnik = @korisnik;
    END
    ELSE
    BEGIN
        UPDATE Korisnik 
        SET opis = 'undefined'
        WHERE idKorisnik = @korisnik;
    END
END
GO