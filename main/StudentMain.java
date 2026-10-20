package org.example;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;

public class StudentMain {
    public static void main(String[] args) throws Exception {
          GeneralOperations generalOperations = new GeneralOperations();
          GenresOperations genresOperations = new GenresOperations();
          MoviesOperations moviesOperations = new MoviesOperations();
          RatingsOperations ratingsOperation = new RatingsOperations();
          TagsOperations tagsOperations = new TagsOperations();
          UsersOperations usersOperations = new UsersOperations();
          WatchlistsOperations watchlistsOperations = new WatchlistsOperations();

        TestHandler.createInstance(genresOperations,
                moviesOperations,
                ratingsOperation,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations);
        TestRunner.runTests();
    }
}