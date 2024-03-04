package com.application.bookService.author;

import com.application.bookService.author.dto.response.CreateAuthorResponse;
import com.application.bookService.author.dto.response.GetAuthorResponse;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {
  private final AuthorRepository authorRepository;

  @Autowired
  public AuthorService(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
  }

  @Transactional()
  public CreateAuthorResponse createAuthor(String firstName, String lastName) {
    var createdAuthor = this.authorRepository.save(new Author(firstName, lastName));

    return new CreateAuthorResponse(
        createdAuthor.getId(), createdAuthor.getFirstName(), createdAuthor.getLastName());
  }

  @Transactional()
  public GetAuthorResponse getAuthorById(Long id) throws AuthorNotFoundException {
    Author author = this.authorRepository.findById(id).orElse(null);

    if (author == null) throw new AuthorNotFoundException(id);
    return new GetAuthorResponse(author.getId(), author.getFirstName(), author.getLastName());
  }

  @Transactional()
  public void updateAuthor(Long id, String firstName, String lastName)
      throws AuthorNotFoundException {
    Author author = this.authorRepository.findById(id).orElse(null);

    if (author == null) throw new AuthorNotFoundException(id);

    author.setFirstName(firstName);
    author.setLastName(lastName);

    authorRepository.save(author);
  }

  @Transactional()
  public void deleteAuthor(Long id) throws AuthorNotFoundException {
    authorRepository.deleteById(id);
  }
}
