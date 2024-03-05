package com.application.bookService.book;

import com.application.bookService.author.dto.response.GetAuthorResponse;
import com.application.bookService.author.exceptions.AuthorNotFoundException;
import com.application.bookService.book.dto.response.CreateBookResponse;
import com.application.bookService.book.dto.response.GetBookResponse;
import com.application.bookService.book.exceptions.BookNotFoundException;
import com.application.bookService.tag.Tag;
import com.application.bookService.tag.TagRepository;
import com.application.bookService.tag.dto.response.GetTagResponse;
import com.application.bookService.tag.exceptions.TagNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
  private final BookRepository bookRepository;
  private final TagRepository tagRepository;

  @Autowired
  public BookService(BookRepository bookRepository, TagRepository tagRepository) {
    this.bookRepository = bookRepository;
    this.tagRepository = tagRepository;
  }

  @Transactional(
      propagation = Propagation.REQUIRES_NEW,
      rollbackFor = {Throwable.class})
  public CreateBookResponse createBook(String title, Long authorId) throws AuthorNotFoundException {
    try {
      var createdBook = this.bookRepository.save(new Book(title, authorId));
      return new CreateBookResponse(
          createdBook.getId(), createdBook.getTitle(), createdBook.getAuthorId());
    } catch (DataIntegrityViolationException e) {
      throw new AuthorNotFoundException(authorId);
    }
  }

  @Transactional
  public GetBookResponse getBookById(Long id) throws BookNotFoundException {
    var book = bookRepository.findById(id).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(id);
    }
    var author = book.getAuthor();

    return new GetBookResponse(
        book.getId(),
        book.getTitle(),
        new GetAuthorResponse(author.getId(), author.getFirstName(), author.getLastName()),
        book.getTags().stream()
            .map(tag -> new GetTagResponse(tag.getId().toString(), tag.getName()))
            .toList());
  }

  @Transactional
  public List<GetBookResponse> getAllBooks() {
    var books = bookRepository.findAll();
    return books.stream()
        .map(
            (book ->
                new GetBookResponse(
                    book.getId(),
                    book.getTitle(),
                    new GetAuthorResponse(
                        book.getAuthor().getId(),
                        book.getAuthor().getFirstName(),
                        book.getAuthor().getLastName()),
                    book.getTags().stream()
                        .map(tag -> new GetTagResponse(tag.getId().toString(), tag.getName()))
                        .toList())))
        .toList();
  }

  @Transactional
  public void updateBook(Long id, String title, Long authorId) throws BookNotFoundException {
    var book = bookRepository.findById(id).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(id);
    }

    book.setTitle(title);
    book.setAuthorId(authorId);

    bookRepository.save(book);
  }

  @Transactional
  public void addTag(Long bookId, String tagId) throws BookNotFoundException, TagNotFoundException {
    var book = bookRepository.findById(bookId).orElse(null);
    var tag = tagRepository.findById(UUID.fromString(tagId)).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(bookId);
    }
    if (tag == null) {
      throw new TagNotFoundException(tagId);
    }

    book.addTag(new Tag(UUID.fromString(tagId), tag.getName()));

    bookRepository.save(book);
  }

  @Transactional
  public void removeTag(Long bookId, String tagId)
      throws BookNotFoundException, TagNotFoundException {
    var book = bookRepository.findById(bookId).orElse(null);
    var tag = tagRepository.findById(UUID.fromString(tagId)).orElse(null);

    if (book == null) {
      throw new BookNotFoundException(bookId);
    }
    if (tag == null) {
      throw new TagNotFoundException(tagId);
    }

    book.getTags().remove(tag);

    bookRepository.save(book);
  }

  @Transactional
  public void deleteBook(Long id) {
    bookRepository.deleteById(id);
  }
}
