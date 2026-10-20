CREATE OR ALTER PROCEDURE SP_REWARD_USER_MAIN
    @idKorisnik INT,
    @idFilm INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @brojFilmova INT;
    DECLARE @globalniProsek DECIMAL(10,3);

    SELECT @brojFilmova = COUNT(*)
    FROM Ocena
    WHERE idKorisnik = @idKorisnik;

    IF @brojFilmova < 10 RETURN;

    IF EXISTS (
        SELECT 1
        FROM Pripada p1
        JOIN Pripada p2 ON p1.idZanr = p2.idZanr
        JOIN Ocena o ON o.idFilm = p2.idFilm
        WHERE p1.idFilm = @idFilm 
          AND o.idKorisnik = @idKorisnik
        GROUP BY p1.idZanr
        HAVING AVG(o.ocena * 1.0) >= 8.0
    )
    BEGIN
        SELECT @globalniProsek = AVG(ocena * 1.0)
        FROM Ocena
        WHERE idFilm = @idFilm 
          AND idKorisnik <> @idKorisnik;

        IF @globalniProsek < 6.0
        BEGIN
            UPDATE Korisnik
            SET brojNagrada = brojNagrada + 1
            WHERE idKorisnik = @idKorisnik;
        END
    END
END;
GO