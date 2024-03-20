package com.application.authorRegistryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Repository
public class AuthorRegistryRepository {
  private final Map<String, AuthorInfo> bookAuthor;

  public AuthorRegistryRepository() {
    bookAuthor = new HashMap<>();

    bookAuthor.put("The Chronicles of Eternity", new AuthorInfo("Emily", "Williams"));
    bookAuthor.put("Beyond the Horizon", new AuthorInfo("Daniel", "Miller"));
    bookAuthor.put("Whispers in the Shadows", new AuthorInfo("Olivia", "Johnson"));
    bookAuthor.put("Echoes of Destiny", new AuthorInfo("Nathan", "Taylor"));
    bookAuthor.put("Serenade of the Stars", new AuthorInfo("Sophia", "Martin"));
    bookAuthor.put("Mysteries of the Moonlight", new AuthorInfo("Ethan", "White"));
    bookAuthor.put("Threads of Fate", new AuthorInfo("Isabella", "Clark"));
    bookAuthor.put("A Symphony of Secrets", new AuthorInfo("Liam", "Thomas"));
    bookAuthor.put("Waltz of Illusions", new AuthorInfo("Ava", "Brown"));
    bookAuthor.put("Legends of the Lost Realm", new AuthorInfo("Gabriel", "Smith"));
    bookAuthor.put("Brave New World", new AuthorInfo("Aldous", "Huxley"));
  }

  public boolean isAuthor(String bookName, AuthorInfo authorInfo) {
    System.out.println(bookName + " -   -- - - - -- - - - - - - - - -- ----- - - -");
    var author = this.bookAuthor.getOrDefault(bookName, null);

    if (author == null) {
      return false;
    }

    return Objects.equals(author.firstName(), authorInfo.firstName())
        && Objects.equals(author.lastName(), authorInfo.lastName());
  }
}
